package com.ancevt.util.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import static com.ancevt.util.net.PacketCodec.*;

public class UdpServer extends UdpEndpoint {

    private final ConnectionIdManager idManager = new ConnectionIdManager();

    public UdpServer(NetConfig cfg) {
        super(cfg);
    }

    public void start() throws IOException {
        openAndConfigure(new InetSocketAddress(cfg.port));
    }

    @Override
    protected void onDatagram(SocketAddress from, Decoded p) throws IOException {
        if ((p.flags & F_HB) != 0) {
            Session s = getOrCreate(from, p.connId);
            s.lastSeen = System.currentTimeMillis();
            return;
        }

        Session s = sessions.get(from);
        if ((p.flags & F_HELLO) != 0) {
            int newId = idManager.acquireId();
            s = getOrCreate(from, newId);
            s.lastSeen = System.currentTimeMillis();
            notifyConnected(s);
            byte[] helloAck = PacketCodec.build(F_HELLO | F_ACK, s.getConnId(), 0, s.recvMax, s.recvBitmap, new byte[0], cfg.mtu);
            ch.send(java.nio.ByteBuffer.wrap(helloAck), from);
            return;
        }
        if (s == null) return;

        s.lastSeen = System.currentTimeMillis();
        noteAcks(s, p.ack, p.ackMask);

        if ((p.flags & F_FIN) != 0) {
            disconnect(s, "peer FIN");
            return;
        }

        if (p.seq > 0) s.noteReceived(p.seq);
        if (p.payload.length > 0) {
            Reliability rel = (p.flags & F_REL) != 0 ? Reliability.REL : Reliability.FAST;
            notifyMessage(s, p.payload, rel);
        }

        // Send ACK back if this was a reliable (REL) message
        if ((p.flags & F_REL) != 0 && s != null) {
            byte[] ackPacket = PacketCodec.build(
                    F_ACK, s.getConnId(), 0, s.recvMax, s.recvBitmap, new byte[0], cfg.mtu
            );
            ch.send(java.nio.ByteBuffer.wrap(ackPacket), s.getAddr());
        }
    }

    @Override
    protected void notifyDisconnected(Session s, String reason) {
        super.notifyDisconnected(s, reason);
        idManager.releaseId(s.getConnId());
    }

    public void send(Session s, Reliability rel, byte[] payload) throws IOException {
        super.send(s, rel, payload, 0);
    }
}
