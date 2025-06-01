package org.jrx;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class ObservableTest {

    /* 1. Тесты базовых компонентов */
    @Test
    void sendNewValue_ShouldCallOnNext() {
        Observable<String> observable = Observable.create();
        List<String> receivedValues = new ArrayList<>();

        Observer<String> observer = new Observer<>() {
            @Override
            public void onNext(String value) {
                receivedValues.add(value);
            }

            @Override
            public void onError(Throwable t) {}

            @Override
            public void onComplete() {}
        };

        observable.subscribe(observer);
        observable.sendNewValue("Hello");
        observable.sendNewValue("World");
        assertEquals(List.of("Hello", "World"), receivedValues);
    }

    @Test
    void completeAll_ShouldCallOnComplete() {
        Observable<Integer> observable = Observable.create();
        AtomicBoolean isCompleted = new AtomicBoolean(false);

        Observer<Integer> observer = new Observer<>() {
            @Override
            public void onNext(Integer value) {}

            @Override
            public void onError(Throwable t) {}

            @Override
            public void onComplete() {
                isCompleted.set(true);
            }
        };

        observable.subscribe(observer);
        observable.completeAll();

        assertTrue(isCompleted.get());
    }

    @Test
    void sendErrorAll_ShouldCallOnError() {
        Observable<Double> observable = Observable.create();
        AtomicBoolean errorReceived = new AtomicBoolean(false);

        Observer<Double> observer = new Observer<>() {
            @Override
            public void onNext(Double value) {}

            @Override
            public void onError(Throwable t) {
                errorReceived.set(true);
            }

            @Override
            public void onComplete() {}
        };

        observable.subscribe(observer);
        observable.sendErrorAll(new RuntimeException("Test Error"));

        assertTrue(errorReceived.get());
    }

    /* 2. Тесты операторов преобразования */
    @Test
    void map_ShouldMap() {

        Observable<Integer> numbers = Observable.create();
        List<Integer> results = new ArrayList<>();

        class IntegerObserver implements Observer<Integer> {

            @Override
            public void onNext(Integer item) {
                results.add(item);
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onComplete() {

            }
        }

        numbers.sendNewValue(1);
        numbers.sendNewValue(2);
        numbers.sendNewValue(3);

        numbers.subscribe(new IntegerObserver());

        assertEquals(List.of(1, 2, 3), results);

        results.clear();
        numbers.map(x -> x * 5);

        numbers.sendNewValue(4);
        numbers.sendNewValue(5);

        assertEquals(List.of(20, 25), results);

        results.clear();
        numbers.subscribe(new IntegerObserver());

        assertEquals(List.of(5, 10, 15, 20, 25), results);
    }


    @Test
    void filter_ShouldFiltered() {
        Observable<Integer> numbers = Observable.create();
        List<Integer> results = new ArrayList<>();

        class IntegerObserver implements Observer<Integer> {

            @Override
            public void onNext(Integer item) {
                results.add(item);
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onComplete() {

            }
        }

        numbers.sendNewValue(1);
        numbers.sendNewValue(2);
        numbers.sendNewValue(3);

        numbers.subscribe(new IntegerObserver());

        assertEquals(List.of(1, 2, 3), results);

        results.clear();
        numbers.filter(x -> x % 2 != 0);

        numbers.subscribe(new IntegerObserver());

        assertEquals(List.of(1, 3), results);

        results.clear();
        numbers.sendNewValue(4);
        numbers.sendNewValue(5);

        assertEquals(List.of(5, 5), results);
    }
    /* 3. Управление потоками выполнения */
    @Test
    void subscribeOn_ShouldSubscribeOn() {
        var currentThreadId = Thread.currentThread().getName();
        AtomicReference<String> result = new AtomicReference<>();
        CountDownLatch latch1 = new CountDownLatch(1);
        CountDownLatch latch2 = new CountDownLatch(1);
        Observable<Integer> numbers = Observable.create();

        result.set(currentThreadId);
        numbers.subscribeOn(new IOThreadScheduler());
        numbers.sendNewValue(1);
        numbers.subscribe(new Observer<Integer>() {
            @Override
            public void onNext(Integer item) {
                result.set(Thread.currentThread().getName());
                latch1.countDown();
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onComplete() {
                result.set(Thread.currentThread().getName());
                latch2.countDown();
            }
        });

        try {
            if(!latch1.await(1, TimeUnit.SECONDS)) {
                fail("Test time is too long");
            }
        } catch (InterruptedException e) {
           fail(String.format("Error: %s", e.getMessage()));
        }

        // Потоки при подписке должны быть разные
        assertNotEquals(currentThreadId, result.get());

        numbers.completeAll();

        try {
            if(!latch2.await(1, TimeUnit.SECONDS)) {
                fail("Test time is too long");
            }
        } catch (InterruptedException e) {
            fail(String.format("Error: %s", e.getMessage()));
        }

        // Потоки при завершении должны быть одинаковые
        assertEquals(currentThreadId, result.get());
    }
    @Test
    void observeOn_ShouldObserveOn() {
        var currentThreadId = Thread.currentThread().getName();
        AtomicReference<String> result = new AtomicReference<>();
        CountDownLatch latch1 = new CountDownLatch(1);
        CountDownLatch latch2 = new CountDownLatch(1);
        Observable<Integer> numbers = Observable.create();

        result.set(currentThreadId);
        numbers.observeOn(new IOThreadScheduler());
        numbers.sendNewValue(1);
        numbers.subscribe(new Observer<Integer>() {
            @Override
            public void onNext(Integer item) {
                result.set(Thread.currentThread().getName());
                latch1.countDown();
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onComplete() {
                result.set(Thread.currentThread().getName());
                latch2.countDown();
            }
        });

        try {
            if(!latch1.await(1, TimeUnit.SECONDS)) {
                fail("Test time is too long");
            }
        } catch (InterruptedException e) {
            fail(String.format("Error: %s", e.getMessage()));
        }

        // Потоки при подписке должны быть одинаковые
        assertEquals(currentThreadId, result.get());
        numbers.completeAll();

        try {
            if(!latch2.await(1, TimeUnit.SECONDS)) {
                fail("Test time is too long");
            }
        } catch (InterruptedException e) {
            fail(String.format("Error: %s", e.getMessage()));
        }
        // Потоки при завершении должны быть разные
        assertNotEquals(currentThreadId, result.get());
    }


    /* 4. Дополнительные операторы и управление подписками */
    @Test
    void flatMap_ShouldFlatMapValues() {
        Observable<Integer> numbers = Observable.create();
        List<String> results = new ArrayList<>();

        numbers.sendNewValue(1);
        numbers.sendNewValue(2);

        Observable<String> strings = numbers.flatMap(num -> "Number: " + num);

        strings.subscribe(new Observer<String>() {
            @Override
            public void onNext(String item) {
                results.add(item);
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onComplete() {

            }
        });

        assertEquals(List.of("Number: 1", "Number: 2"), results);
    }

    @Test
    void dispose_ShouldDispose() {
        Observable<Integer> numbers = Observable.create();
        List<String> results = new ArrayList<>();

        class IntegerObserver implements Observer<Integer> {

            private String name;

            IntegerObserver(String name) {
                this.name = name;
            }

            @Override
            public void onNext(Integer item) {
                results.add(name);
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onComplete() {

            }
        }

        numbers.sendNewValue(1);

        Disposable controlOne = numbers.subscribe(new IntegerObserver("First"));
        numbers.subscribe(new IntegerObserver("Second"));

        assertEquals(List.of("First", "Second"), results);

        results.clear();
        controlOne.dispose();

        numbers.sendNewValue(2);
        assertEquals(List.of("Second"), results);
    }
}
