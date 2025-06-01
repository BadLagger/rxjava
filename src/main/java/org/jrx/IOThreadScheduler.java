package org.jrx;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class IOThreadScheduler implements Scheduler {

    private Executor executor = Executors.newCachedThreadPool();

    @Override
    public Executor execute(Runnable task) {
        executor.execute(task);
        return executor;
    }
}
