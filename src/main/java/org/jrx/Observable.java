package org.jrx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Predicate;

public class Observable<T> {

    private final List<Observer<T>> observerList = new CopyOnWriteArrayList<>();
    private List<T> valueList = Collections.synchronizedList(new ArrayList<>());
    private Function<T, T> mapper = null;
    private Predicate<T> filter = null;
    private volatile Scheduler subscribeScheduler = null;
    private volatile Scheduler observeScheduler = null;

    /* 1. Реализация базовых компонентов */
    public static <T> Observable<T> create() {
        return new Observable<T>();
    }

    public void sendNewValue(T value) {

        if (filter != null) {
            if (!filter.test(value))
                return;
        }

        if (mapper == null)
            valueList.add(value);
        else
            valueList.add(mapper.apply(value));

        for (var listener : observerList) {
            if (observeScheduler == null) {
                if (mapper == null)
                    listener.onNext(value);
                else
                    listener.onNext(mapper.apply(value));
            } else {
                if (mapper == null)
                    observeScheduler.execute(()->listener.onNext(value));
                else
                    observeScheduler.execute(()->listener.onNext(mapper.apply(value)));
            }
        }
    }

    public void completeAll() {

        for (var listener : observerList) {
            if (observeScheduler == null)
                listener.onComplete();
            else
                observeScheduler.execute(listener::onComplete);
        }
    }

    public Disposable subscribe(Observer<T> observer) {

        observerList.add(observer);

        for (T val : valueList) {
            if (subscribeScheduler == null)
                observer.onNext(val);
            else
                subscribeScheduler.execute(()->observer.onNext(val));
        }

        return new Disposable() {
            private volatile boolean disposed = false;
            @Override
            public void dispose() {
                if (!disposed) {
                    disposed = true;
                    if (subscribeScheduler == null) {
                        observerList.remove(observer);
                    } else {
                        subscribeScheduler.execute(()->observerList.remove(observer));
                    }
                }
            }
        };
    }

    public void sendErrorAll(Throwable t) {
        for (var listener : observerList) {
            if (observeScheduler == null)
                listener.onError(t);
            else
                observeScheduler.execute(()->listener.onError(t));
        }
    }

    /* 2. Операторы преобразования данных */
    public Observable<T> map(Function<T, T> mapper) {

        this.mapper = mapper;
        // Применяем map для уже существующих значений
        valueList.replaceAll(mapper::apply);

        return this;
    }

    public Observable<T> filter(Predicate<T> predicate) {

        this.filter = predicate;

        List<T> newList = new ArrayList<>();

        for (var oldItem : valueList ) {
            if (filter.test(oldItem))
                newList.add(oldItem);
        }

        valueList = newList;

        return this;
    }

    /* 3. Управление потоками выполнения */

    public void subscribeOn(Scheduler scheduler) {
        if (subscribeScheduler == null) {
            subscribeScheduler = scheduler;
        }
    }

    public void observeOn(Scheduler scheduler) {
        if (observeScheduler == null) {
            observeScheduler = scheduler;
        }
    }

    /* 4. Дополнительные операторы и управление подписками */
    public <R> Observable<R> flatMap(Function<T, R> mapper) {
        Observable<R> mappedObservable = Observable.create();

        for (var item : valueList) {
            mappedObservable.sendNewValue(mapper.apply(item));
        }

        return mappedObservable;
    }
}
