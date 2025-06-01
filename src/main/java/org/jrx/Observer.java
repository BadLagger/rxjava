package org.jrx;

public interface Observer<T> {
    public void onNext(T item);
    public void onError(Throwable t);
    public void onComplete();
}
