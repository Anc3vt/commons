package com.ancevt.util.net;

import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class ConnectionIdManager {
    private final AtomicInteger counter = new AtomicInteger(1);
    private final Queue<Integer> freeIds = new ConcurrentLinkedQueue<>();
    private final ConcurrentHashMap<Integer, Boolean> used = new ConcurrentHashMap<>();

    public int acquireId() {
        Integer id = freeIds.poll();
        if (id != null) {
            used.put(id, true);
            return id;
        }
        int newId;
        do {
            newId = counter.getAndIncrement();
            if (newId == Integer.MAX_VALUE) counter.set(1);
        } while (used.containsKey(newId));
        used.put(newId, true);
        return newId;
    }

    public void releaseId(int id) {
        if (used.remove(id) != null)
            freeIds.offer(id);
    }
}

