package com.ancevt.util.net;

public class NetConfig {

    public final int port;
    public final int rcvBuf;
    public final int sndBuf;
    public final int mtu;
    public final int heartbeatMillis;
    public final int idleTimeoutMillis;
    public final int rtoMillis;
    public final int maxRetries;

    private NetConfig(int port, int rcvBuf, int sndBuf, int mtu,
                     int heartbeatMillis, int idleTimeoutMillis,
                     int rtoMillis, int maxRetries) {
        this.port = port;
        this.rcvBuf = rcvBuf;
        this.sndBuf = sndBuf;
        this.mtu = mtu;
        this.heartbeatMillis = heartbeatMillis;
        this.idleTimeoutMillis = idleTimeoutMillis;
        this.rtoMillis = rtoMillis;
        this.maxRetries = maxRetries;
    }

    // === Новый builder ===
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int port = 0;
        private int rcvBuf = 1 << 20;
        private int sndBuf = 1 << 20;
        private int mtu = 1200;
        private int heartbeatMillis = 2000;
        private int idleTimeoutMillis = 10_000;
        private int rtoMillis = 200;
        private int maxRetries = 8;

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public Builder rcvBuf(int rcvBuf) {
            this.rcvBuf = rcvBuf;
            return this;
        }

        public Builder sndBuf(int sndBuf) {
            this.sndBuf = sndBuf;
            return this;
        }

        public Builder mtu(int mtu) {
            this.mtu = mtu;
            return this;
        }

        public Builder heartbeatMillis(int heartbeatMillis) {
            this.heartbeatMillis = heartbeatMillis;
            return this;
        }

        public Builder idleTimeoutMillis(int idleTimeoutMillis) {
            this.idleTimeoutMillis = idleTimeoutMillis;
            return this;
        }

        public Builder rtoMillis(int rtoMillis) {
            this.rtoMillis = rtoMillis;
            return this;
        }

        public Builder maxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }

        public NetConfig build() {
            if (mtu < 64) throw new IllegalArgumentException("MTU too small");
            return new NetConfig(
                    port,
                    rcvBuf,
                    sndBuf,
                    mtu,
                    heartbeatMillis,
                    idleTimeoutMillis,
                    rtoMillis,
                    maxRetries
            );
        }
    }

    @Override
    public String toString() {
        return "NetConfig{" +
                "port=" + port +
                ", rcvBuf=" + rcvBuf +
                ", sndBuf=" + sndBuf +
                ", mtu=" + mtu +
                ", heartbeatMillis=" + heartbeatMillis +
                ", idleTimeoutMillis=" + idleTimeoutMillis +
                ", rtoMillis=" + rtoMillis +
                ", maxRetries=" + maxRetries +
                '}';
    }
}
