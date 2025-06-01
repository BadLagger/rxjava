package org.jrx;

import java.util.concurrent.Executor;

public interface Scheduler {
    Executor execute(Runnable task);
}
