package com.ancevt.util.net;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class ConnectionIdManager {

    private final AtomicInteger counter = new AtomicInteger(1);
    private final Queue<Integer> freeIds = new ConcurrentLinkedQueue<>();

    public int acquireId() {
        Integer id = freeIds.poll();
        if (id != null) return id;
        return counter.getAndIncrement();
    }

    public void releaseId(int id) {
        freeIds.offer(id);
    }
}
