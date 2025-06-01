package org.jrx;

import logger.LoggerConfig;

import java.util.logging.Level;
import java.util.logging.Logger;

class StringListener implements Observer<String> {

    protected static final Logger logger = Logger.getLogger(StringListener.class.getName());

    static {
        LoggerConfig.init(logger, Level.ALL);
    }

    private String name;

    StringListener(String name) {
        this.name = name;
    }

    @Override
    public void onNext(String item) {
        logger.log(Level.INFO, "{0}", String.format("%s onNext: %s", name, item));
    }

    @Override
    public void onError(Throwable t) {
        logger.log(Level.INFO, "{0}", String.format("%s onError: %s", name, t.getMessage()));
    }

    @Override
    public void onComplete() {
        logger.log(Level.INFO, "onComplete {0}", name);
    }
}

class IntegerListener implements Observer<Integer> {

    protected static final Logger logger = Logger.getLogger(IntegerListener.class.getName());

    static {
        LoggerConfig.init(logger, Level.ALL);
    }

    private String name;

    IntegerListener(String name) {
        this.name = name;
    }

    @Override
    public void onNext(Integer item) {
        logger.log(Level.INFO, "{0}", String.format("%s onNext: %d", name, item));
    }

    @Override
    public void onError(Throwable t) {
        logger.log(Level.INFO, "{0}", String.format("%s onError: %s", name, t.getMessage()));
    }

    @Override
    public void onComplete() {
        logger.log(Level.INFO, "onComplete {0}", name);
    }
}

public class Main {
    protected static final Logger logger = Logger.getLogger(Main.class.getName());

    static {
        LoggerConfig.init(logger, Level.ALL);
    }

    public static void main(String[] args) {
        logger.info("App starts...");

        // 1. Демонстрация базового функционала
        logger.info("1. Basic functional demonstration");
        Observable<String> senderStrings = Observable.create();

        senderStrings.sendNewValue("Cool");
        senderStrings.sendNewValue("Stories");
        senderStrings.sendNewValue("Begin");

        senderStrings.subscribe(new StringListener("First Listener"));

        logger.info("Create first listener");

        senderStrings.subscribe(new StringListener("Second Listener"));

        logger.info("Create second listener");

        senderStrings.sendNewValue("New message");

        logger.info("Send new message");

        senderStrings.subscribe(new StringListener("Third Listener"));

        logger.info("Create third listener");

        senderStrings.completeAll();

        // 2. Демонстрация функционала преобразований
        logger.info("2. Map and filter demonstration");
        Observable<Integer> numbers = Observable.create();
        numbers.sendNewValue(1);
        numbers.sendNewValue(2);

        numbers.subscribe(new IntegerListener("MapChecker1"));

        numbers.map(i -> i * 3);

        numbers.subscribe(new IntegerListener("MapChecker2"));
        numbers.sendNewValue(3);

        numbers.filter(i -> i % 2 == 0);

        numbers.subscribe(new IntegerListener("FilterChecker1"));
        numbers.sendNewValue(4);
        numbers.sendNewValue(5);

        // 3. Управление потоками выолнения
        logger.info("3. Threads control");
        Observable<String> threadStrings = Observable.create();
        threadStrings.subscribeOn(new IOThreadScheduler());
        threadStrings.observeOn(new IOThreadScheduler());

        threadStrings.sendNewValue("One");
        threadStrings.sendNewValue("Two");
        threadStrings.sendNewValue("Three");


        threadStrings.subscribe(new StringListener("FirstThreadListener"));
        threadStrings.subscribe(new StringListener("SecondThreadListener"));
        threadStrings.subscribe(new StringListener("ThirdThreadListener"));

        threadStrings.sendNewValue("Four");
        threadStrings.sendNewValue("Five");


        // 4. Дополнительные операторы и управление подписками
        logger.info("4. Additional operators and subscribe control demonstration");
        Observable<String> strings = numbers.flatMap(num -> "Number: " + num);
        Disposable control = strings.subscribe(new StringListener("IntegerToStringListener"));

        strings.sendNewValue("Independent sender!!!");

        strings.subscribe(new StringListener("IndependentListener"));

        strings.sendNewValue("Last IntegerToStringListener message!");

        control.dispose();

        strings.sendNewValue("Independence wins! We just killed all another )");


        logger.info("App DONE!");
    }
}