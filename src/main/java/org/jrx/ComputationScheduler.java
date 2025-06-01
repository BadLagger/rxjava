package org.jrx;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ComputationScheduler implements Scheduler {

    private Executor executor = null;

    public ComputationScheduler(int threadNumber) {
        executor = Executors.newFixedThreadPool(threadNumber);
    }

    @Override
    public Executor execute(Runnable task) {
        executor.execute(task);
        return executor;
    }
}
