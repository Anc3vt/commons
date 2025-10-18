package com.ancevt.util.net;

public interface NetListener {
    default void onConnected(Session s) {
    }

    default void onDisconnected(Session s, String reason) {
    }

    default void onMessage(Session s, byte[] payload, Reliability rel) {
    }

    default void onError(Throwable t) {
        t.printStackTrace();
    }
}
