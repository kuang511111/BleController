package com.jrdcom.wearable.smartband2.util;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by xinhua.lin on 2015/6/25.
 */
public class ThreadPool {
    private static ExecutorService cachedThreadPool = null;
    private static ExecutorService fixedThreadPool = null;
    private static ScheduledExecutorService scheduledThreadPool = null;
    private static ExecutorService singleThreadExecutor = null;

    public synchronized static ExecutorService getCachedThreadPool() {
        if (cachedThreadPool == null) cachedThreadPool = Executors.newCachedThreadPool();
        return cachedThreadPool;
    }

    public synchronized static ExecutorService getFixedThreadPool() {
        if (fixedThreadPool == null) fixedThreadPool = Executors.newFixedThreadPool(3);
        return fixedThreadPool;
    }

    public synchronized static ScheduledExecutorService getScheduledThreadPool() {
        if (scheduledThreadPool == null) scheduledThreadPool = Executors.newScheduledThreadPool(5);
        return scheduledThreadPool;
    }

    public synchronized static ExecutorService getSingleThreadExecutor() {
        if (singleThreadExecutor == null) singleThreadExecutor = Executors.newSingleThreadExecutor();
        return singleThreadExecutor;
    }
}
