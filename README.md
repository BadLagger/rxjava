# Отчёт #

## 1. Описание архитектуры ##

Основой проекта является класс Observable. В целом проект реализует архитектуру реактивного программирования.
За основу архитектуры взят паттерн Observable/Observer с элементами Publisher-Subscriber.

* класс Observable - это объект. который может уведомлять подписчиков (Observers) об изменениях. Подписаться на
объект возможно с помощью метода subscribe. Всем подписчикам можно отправить событие трёх типов: 
    - sendNewValue() - вызовет метод onNext() у каждого подписчика и передаст новый объект;
    - completeAll() - вызовет метод onComplete() у каждого подписчика;
    - sendErrorAll() - вызовет метод onError() у каждого подписчика;
  
* класс Observable имеет операторы реактивных потоков: map и filter. Кроме этого с помощью flatMap можно преобразовывать
  Observable в другой тип.
* поддерживается подписка и обработка событий в разных потоках с помощью subscribeOn() и observeOn() и Scheduler.
* потокобезопасность обеспечивается с помощью:
  - CopyOnWriteArrayList для списка наблюдателей;
  - Collections.synchronizedList для хранения потока данных;
* управление жизненным циклом обеспечивается через интерфейс Disposable, который получается при подписке.

## 2. Принципы работы реализаций Scheduler ##

**ComputationScheduler**
* Принцип работы: использует фиксированный пул потоков размер которого задаётся в конструкторе. Все задачи выполняются в этом пуле. Если все потоки заняты, новые задачи ждут в очереди.
* Особенности: подходит для CPU-интенсивных задач, где важно не создавать слишком много потоков (чтобы избежать перегрузки процессора).
* Сферы применения: параллельная обработка данных, где количество задач предсказуемо (математические операции, алгоритмы).

**IOThreadScheduler**
* Принцип работы: использует гибкий пул потоков (CachedThreadPool), который создаёт новые потоки по мере необходимости, и переиспользует свободные.
* Особенности: подходит для операций ввода/вывода. Обладает автомасштабированием в зависимости от нагрузки. Но может привести к "поеданию" ресурсов при высокой нагрузке.
* Сферы применения: HTTP-запросы, чтение/запись файлов, взаимодействие с базами данных.

**SingleThreadScheduler**
* Принцип работы: все задачи выполняются последовательно в порядке их поступления в одном потоке.
* Особенности: гарантирует порядок выполнения.
* Сферы применения: логгирование, обновление GUI, работа с одним ресурсом.

## 3. Описание тестирования ##

Тесты написаны с использованием JUnit. Файл с тестами ObservableTest.java. Покрывают основные методы Observable. Они охватывают:

* Базовые компоненты (подписка, отправка значений, завершение, ошибки)
* Операторы преобразования (map, filter, flatMap).
* Управление потоками выполнения (subscribeOn, observeOn).
* Дополнительные операторы и управление подписками (dispose).

**Тесты базовых компонентов**

* sendNewValue_ShouldCallOnNext - проверяет, что при отправке значения (sendNewValue) подписчик (Observer) получает его через onNext.
* completeAll_ShouldCallOnComplete - проверяет, что вызов completeAll() уведомляет подписчиков через onComplete.
* sendErrorAll_ShouldCallOnError - проверяет, что ошибка (sendErrorAll) доходит до подписчика через onError.

**Тесты операторов преобразования**

* map_ShouldMap - проверяет, что оператор map преобразует значения и применяется к уже отправленным данным.
* filter_ShouldFiltered - проверяет, что filter отсеивает значения по условию.

**Тесты управления потоками выполнения**

* subscribeOn_ShouldSubscribeOn - проверяет, что subscribeOn выполняет подписку в указанном потоке (не в основном).
* observeOn_ShouldObserveOn - проверяет, что observeOn выполняет обработку значений в указанном потоке.

**Тесты дополнительных операторов и подписок**

* flatMap_ShouldFlatMapValues - проверяет, что flatMap преобразует Observable<T> в Observable<R>.
* dispose_ShouldDispose - проверяет, что Disposable отменяет подписку.

## 4. Примеры использования реализованной библиотеки ##

**Бэкенд: Обработка HTTP-запросов**

```java
public Observable<User> getUserById(int id) {
    Observable<User> userObservable = Observable.create();
    
    // Имитация асинхронного запроса к БД
    new Thread(() -> {
        User user = database.loadUser(id); // Блокирующий вызов
        userObservable.sendNewValue(user);
        userObservable.completeAll();
    }).start();
    
    return userObservable;
}

// Использование
getUserById(42)
    .subscribeOn(new IOThreadScheduler()) // I/O-операции в отдельном потоке
    .map(user -> user.toDTO()) // Преобразование в DTO
    .subscribe(
        userDTO -> sendResponse(userDTO),
        error -> log.error("Failed to load user", error)
    );
```

**Игровой движок**
```java
Observable<GameEvent> gameEvents = Observable.create();

// Подписка на события (например, столкновения)
gameEvents
    .filter(event -> event.getType() == EventType.COLLISION)
    .subscribe(event -> {
        applyPhysics(event); // Расчёт физики
        playSoundEffect();   // Звук удара
    });

// Генерация события
player.move();
if (isCollisionDetected()) {
    gameEvents.sendNewValue(new GameEvent(EventType.COLLISION));
}
```