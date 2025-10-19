package com.ancevt.util.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.*;

import static com.ancevt.util.net.PacketCodec.*;

abstract class UdpEndpoint implements AutoCloseable {
    protected final NetConfig cfg;
    protected DatagramChannel ch;
    protected Selector selector;
    protected final Map<SocketAddress, Session> sessions = new ConcurrentHashMap<>();
    protected final Random rng = new Random();
    private final List<NetListener> listeners = new CopyOnWriteArrayList<>();

    private final ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "udp-timer");
        t.setDaemon(true);
        return t;
    });

    protected UdpEndpoint(NetConfig cfg) {
        this.cfg = cfg;
    }

    public void addNetListener(NetListener listener) {
        listeners.add(listener);
    }

    public void removeNetListener(NetListener listener) {
        listeners.remove(listener);
    }

    protected void notifyConnected(Session s) {
        for (NetListener l : listeners) l.onConnected(s);
    }

    protected void notifyMessage(Session s, byte[] payload, Reliability rel) {
        for (NetListener l : listeners) l.onMessage(s, payload, rel);
    }

    protected void notifyDisconnected(Session s, String reason) {
        for (NetListener l : listeners) l.onDisconnected(s, reason);
    }

    protected void notifyError(Throwable e) {
        for (NetListener l : listeners) l.onError(e);
    }

    public void startLoop() {
        new Thread(this::loop, getClass().getSimpleName() + "-loop").start();
    }

    protected void openAndConfigure(InetSocketAddress bind) throws IOException {
        ch = DatagramChannel.open(StandardProtocolFamily.INET);
        ch.configureBlocking(false);
        ch.setOption(StandardSocketOptions.SO_RCVBUF, cfg.rcvBuf);
        ch.setOption(StandardSocketOptions.SO_SNDBUF, cfg.sndBuf);
        ch.bind(bind);
        selector = Selector.open();
        ch.register(selector, SelectionKey.OP_READ);
        timer.scheduleAtFixedRate(this::tick, 100, 50, TimeUnit.MILLISECONDS);
    }

    protected abstract void onDatagram(SocketAddress from, Decoded p) throws IOException;

    protected Session getOrCreate(SocketAddress addr, Integer forceConnId) {
        return sessions.computeIfAbsent(addr, a -> new Session(a, forceConnId != null ? forceConnId : rng.nextInt()));
    }

    public void loop() {
        ByteBuffer buf = ByteBuffer.allocateDirect(64 * 1024);
        try {
            while (ch != null && ch.isOpen() && selector != null && selector.isOpen()) {
                selector.select(200);
                for (Iterator<SelectionKey> it = selector.selectedKeys().iterator(); it.hasNext(); ) {
                    SelectionKey key = it.next();
                    it.remove();
                    if (!key.isValid()) continue;
                    if (key.isReadable()) {
                        buf.clear();
                        SocketAddress from = ch.receive(buf);
                        if (from == null) continue;
                        buf.flip();
                        byte[] dat = new byte[buf.remaining()];
                        buf.get(dat);
                        try {
                            Decoded d = decode(dat);
                            onDatagram(from, d);
                        } catch (Exception e) {
                            notifyError(e);
                        }
                    }
                }
            }
        } catch (ClosedSelectorException ignored) {
            // тихо выходим — это нормальное завершение
        } catch (Exception e) {
            notifyError(e);
        }
    }


    protected void send(Session s, Reliability rel, byte[] payload, int extraFlags) throws IOException {
        if (s == null) return;
        int flags = extraFlags;
        int seq = 0;
        if (rel == Reliability.REL) {
            seq = s.nextSeq++;
            flags |= F_REL;
        }
        byte[] pkt = PacketCodec.build(flags, s.getConnId(), seq, s.recvMax, s.recvBitmap, payload, cfg.mtu);
        ch.send(ByteBuffer.wrap(pkt), s.getAddr());
        if ((flags & F_REL) != 0) {
            long at = System.currentTimeMillis() + jitter(cfg.rtoMillis);
            s.pending.put(seq, new Session.Pending(pkt, at));
        }
    }

    private int jitter(int base) {
        int delta = (int) (base * 0.2);
        return base + rng.nextInt(delta * 2 + 1) - delta;
    }

    private void tick() {
        long now = System.currentTimeMillis();
        for (Session s : sessions.values()) {
            if (s.pending.isEmpty()) continue;
            for (Iterator<Map.Entry<Integer, Session.Pending>> it = s.pending.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<Integer, Session.Pending> e = it.next();
                Session.Pending p = e.getValue();
                if (now >= p.nextAt) {
                    if (++p.retries > cfg.maxRetries) {
                        it.remove();
                        disconnect(s, "retries exceeded");
                        break;
                    }
                    try {
                        ch.send(ByteBuffer.wrap(p.packet), s.getAddr());
                        p.nextAt = now + jitter(cfg.rtoMillis);
                    } catch (IOException ex) {
                        notifyError(ex);
                    }
                }
            }
            if (now - s.lastSeen > cfg.idleTimeoutMillis) {
                disconnect(s, "idle timeout");
            }
            if (now - s.lastHeartbeatSent >= cfg.heartbeatMillis) {
                try {
                    byte[] hb = PacketCodec.build(F_HB, s.getConnId(), 0, s.recvMax, s.recvBitmap, new byte[0], cfg.mtu);
                    ch.send(ByteBuffer.wrap(hb), s.getAddr());
                    s.lastHeartbeatSent = now;
                } catch (IOException e) {
                    notifyError(e);
                }
            }
        }
    }

    protected void noteAcks(Session s, int ack, int mask) {
        if (ack > 0) s.pending.remove(ack);
        for (int i = 1; i <= 32; i++) {
            if ((mask & 1) != 0) s.pending.remove(ack - i);
            mask >>>= 1;
            if (mask == 0) break;
        }
    }

    protected void disconnect(Session s, String reason) {
        sessions.remove(s.getAddr());
        try {
            byte[] fin = PacketCodec.build(F_FIN, s.getConnId(), 0, s.recvMax, s.recvBitmap, new byte[0], cfg.mtu);
            ch.send(ByteBuffer.wrap(fin), s.getAddr());
        } catch (Exception ignored) {
        }
        notifyDisconnected(s, reason);
        s.pending.clear();
    }

    public void stop() {
        timer.shutdownNow();
        try {
            close();
        } catch (Exception ignored) {
        }
    }


    @Override
    public void close() throws Exception {
        if (selector != null) selector.close();
        if (ch != null) ch.close();
    }
}
