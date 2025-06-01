package org.jrx;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class SingleThreadScheduler implements Scheduler {

    private Executor executor = Executors.newSingleThreadExecutor();

    @Override
    public Executor execute(Runnable task) {
        executor.execute(task);
        return executor;
    }
}
