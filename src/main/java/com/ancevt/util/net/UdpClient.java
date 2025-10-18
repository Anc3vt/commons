package com.ancevt.util.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import static com.ancevt.util.net.PacketCodec.*;

public class UdpClient extends UdpEndpoint {
    private final InetSocketAddress serverAddr;
    private Session server;

    public UdpClient(InetSocketAddress serverAddr, NetConfig cfg, NetListener l) {
        super(cfg);
        this.serverAddr = serverAddr;
    }

    public void start() throws IOException {
        openAndConfigure(new InetSocketAddress(0));
        server = getOrCreate(serverAddr, 0);
        byte[] hello = PacketCodec.build(F_HELLO, 0, 0, 0, 0, new byte[0], cfg.mtu);
        ch.send(java.nio.ByteBuffer.wrap(hello), serverAddr);
    }

    @Override
    protected void onDatagram(SocketAddress from, Decoded p) throws IOException {
        if (!from.equals(serverAddr)) return;

        if ((p.flags & F_HB) != 0) {
            Session s = getOrCreate(from, p.connId);
            s.lastSeen = System.currentTimeMillis();
            return;
        }

        Session s = server;
        if ((p.flags & F_HELLO) != 0 && (p.flags & F_ACK) != 0) {
            sessions.remove(from);
            server = getOrCreate(from, p.connId);
            server.lastSeen = System.currentTimeMillis();
            notifyConnected(server);
            return;
        }
        if (s == null) return;

        s.lastSeen = System.currentTimeMillis();
        noteAcks(s, p.ack, p.ackMask);

        if ((p.flags & F_FIN) != 0) {
            disconnect(s, "server FIN");
            return;
        }

        if (p.seq > 0) s.noteReceived(p.seq);
        if (p.payload.length > 0) {
            notifyMessage(s, p.payload, (p.flags & F_REL) != 0 ? Reliability.REL : Reliability.FAST);
        }

        // Send ACK back if this was a reliable (REL) message
        if ((p.flags & F_REL) != 0 && s != null) {
            byte[] ackPacket = PacketCodec.build(
                    F_ACK, s.getConnId(), 0, s.recvMax, s.recvBitmap, new byte[0], cfg.mtu
            );
            ch.send(java.nio.ByteBuffer.wrap(ackPacket), s.getAddr());
        }
    }

    public void send(Reliability rel, byte[] payload) throws IOException {
        super.send(server, rel, payload, 0);
    }
}
