package com.ancevt.util.net;

import com.ancevt.util.io.ByteInput;
import com.ancevt.util.io.ByteOutput;

final class PacketCodec {
    static final int V1 = 1;
    static final int F_REL = 1;
    static final int F_ACK = 2;
    static final int F_HELLO = 4;
    static final int F_FIN = 8;
    static final int F_HB = 16;

    static byte[] build(int flags, int connId, int seq, int ack, int ackMask, byte[] payload, int mtu) {
        int len = Math.min(payload == null ? 0 : payload.length, mtu - 1 - 1 - 4 - 4 - 4 - 4 - 2);
        ByteOutput out = ByteOutput.pooled();
        out.writeByte(V1)
                .writeByte(flags)
                .writeInt(connId)
                .writeInt(seq)
                .writeInt(ack)
                .writeInt(ackMask)
                .writeShort(len);
        if (len > 0) out.write(payload, 0, len);
        return out.toByteArray();
    }

    static Decoded decode(byte[] dat) {
        ByteInput in = ByteInput.wrap(dat);
        int ver = in.readUnsignedByte();
        if (ver != V1) throw new IllegalArgumentException("bad ver");
        int flags = in.readUnsignedByte();
        int connId = in.readInt();
        int seq = in.readInt();
        int ack = in.readInt();
        int ackMask = in.readInt();
        int len = in.readUnsignedShort();
        byte[] pl = len > 0 ? in.readBytes(len) : new byte[0];
        return new Decoded(flags, connId, seq, ack, ackMask, pl);
    }

    static final class Decoded {
        final int flags, connId, seq, ack, ackMask;
        final byte[] payload;
        Decoded(int flags, int connId, int seq, int ack, int ackMask, byte[] payload) {
            this.flags = flags; this.connId = connId; this.seq = seq; this.ack = ack; this.ackMask = ackMask; this.payload = payload;
        }
    }
}
