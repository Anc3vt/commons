package com.ancevt.util.net;

import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Session {
    final long createdAt = System.currentTimeMillis();
    volatile long lastSeen = createdAt;
    private final SocketAddress addr;
    private final int connId;

    // seq state
    int nextSeq = 1;
    int recvMax = 0;          // наибольший принятый seq
    int recvBitmap = 0;       // окно на 32
    final Map<Integer, Pending> pending = new ConcurrentHashMap<>();

    static class Pending {
        final byte[] packet;
        int retries;
        long nextAt;

        Pending(byte[] packet, long nextAt) {
            this.packet = packet;
            this.nextAt = nextAt;
        }
    }

    Session(SocketAddress addr, int connId) {
        this.addr = addr;
        this.connId = connId;
    }

    public SocketAddress getAddr() {
        return addr;
    }

    public int getConnId() {
        return connId;
    }

    boolean noteReceived(int seq) {
        if (seq <= 0) return false;
        if (seq > recvMax) {
            int shift = seq - recvMax;
            if (shift >= 32) {
                recvBitmap = 0;
            } else {
                recvBitmap = (recvBitmap << shift);
            }
            recvBitmap |= 1;
            recvMax = seq;
            return true;
        } else {
            int dist = recvMax - seq;
            if (dist >= 32) return false;
            int bit = 1 << dist;
            if ((recvBitmap & bit) != 0) return false;
            recvBitmap |= bit;
            return true;
        }
    }
}
