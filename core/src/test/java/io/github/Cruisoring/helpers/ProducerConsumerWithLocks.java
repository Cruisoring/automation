package io.github.Cruisoring.helpers;

import io.github.cruisoring.throwables.FunctionThrowable;
import io.github.cruisoring.throwables.SupplierThrowable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ProducerConsumerWithLocks {
    public static final int maxAliveSeconds = 3;

    public static void main(String[] args) throws InterruptedException {

        Queue<Integer> queue = new LinkedList<>();
        int capacity = 10;

        Lock lock = new ReentrantLock();
        Condition notFull = lock.newCondition();
        Condition notEmpty = lock.newCondition();

        class Consumer extends Thread {
            final FunctionThrowable<Integer, Boolean> howToConsume;
            final int count;
            final long interval;

            public Consumer(FunctionThrowable<Integer, Boolean> howToConsume, int count, long interval){
                this.howToConsume = howToConsume;
                this.count = count;
                this.interval = interval;
            }

            boolean awaitResult = false;
            public Boolean consume() {
                try {
                    lock.lock();
                    while (queue.isEmpty()) {
                        LocalDateTime startAwait = LocalDateTime.now();
                        awaitResult = notEmpty.await(maxAliveSeconds, TimeUnit.SECONDS);
                        if(!awaitResult){
                            Duration duration = Duration.between(startAwait, LocalDateTime.now());
                            Logger.I("await() returns false after %s", duration);
                            return false;
                        }
                    }
                    Integer first = queue.remove();
                    if(first == null){
                        Logger.W("Something is wrong here!");
                    }
                    notFull.signal();
                    Logger.D("Consumer@%s: Consumed %d", Thread.currentThread().getId(), first);
                    howToConsume.apply(first);
                    return true;
                }catch (Exception e){
                    Logger.W(e);
                    return false;
                }finally {
                    lock.unlock();
                }
            }

            @Override
            public void run(){
                for (int i = 0; i < count; i++) {
                    if(!consume()){
                        Logger.W("<<<<Consumer@%s failed to consume", Thread.currentThread().getId());
                        break;
                    }
                    Executor.sleep(interval);
                };
                Logger.I("Consumer@%s is stopped.", Thread.currentThread().getId());
            }
        }

        class Producer extends Thread {

            final SupplierThrowable<Integer> howToProduce;
            final int count;
            final long interval;

            public Producer(SupplierThrowable<Integer> howTo, int count, long interval){
                this.howToProduce = howTo;
                this.count = count;
                this.interval = interval;
            }

            public Boolean produce() {
                try {
                    lock.lock();
                    while (queue.size() >= capacity) {
                        LocalDateTime startAwait = LocalDateTime.now();
                        Boolean awaitResult = notFull.await(maxAliveSeconds, TimeUnit.SECONDS);
                        if(!awaitResult){
                            Duration duration = Duration.between(startAwait, LocalDateTime.now());
                            Logger.I("await() returns false after %s", duration);
                            return false;
                        }

                    }

                    Integer product = howToProduce.get();
                    queue.add(product);
                    Logger.D(">>>>Producer@%s: Produce %d", Thread.currentThread().getId(), product);
                    notEmpty.signal();
                    return true;
                } catch (Exception e){
                    Logger.W(e);
                    return false;
                }finally {
                    lock.unlock();
                }
            }

            @Override
            public void run(){
                for (int i = 0; i < count; i++) {
                    if(!produce()){
                        Logger.W("Producer@%s failed to produce, break!", Thread.currentThread().getId());
                        break;
                    }
                    Executor.sleep(interval);
                };
                Logger.I("Producer@%s is stopped.", Thread.currentThread().getId());
            }

            public Boolean asCallable(){
                try {
                    run();
                    return true;
                }catch (Exception e){
                    Logger.D(e);
                    return false;
                }
            }
        }

        AtomicInteger atomicInteger = new AtomicInteger(1);
        Producer producer = new Producer(() -> atomicInteger.getAndIncrement(), 1000, 10);
        AtomicInteger atomicInteger2 = new AtomicInteger(-1);
        Producer producer2 = new Producer(() -> atomicInteger2.getAndDecrement(), 500, 33);
        Consumer consumer1 = new Consumer(number -> number % 2 == 0, 300, 30);
        Consumer consumer2 = new Consumer(number -> number % 3 == 0, 200, 20);
        Consumer consumer3 = new Consumer(number -> number % 5 == 0, 400, 40);
        Consumer consumer4 = new Consumer(number -> number % 2 == 0, 500, 10);

        List<Thread> threads = Arrays.asList(producer, consumer1, consumer2, consumer3, consumer4);
        List<Callable<Boolean>> callables = new ArrayList<>();
        callables.add(() -> runThread(producer));
        callables.add(() -> runThread(producer2));
        callables.add(() -> runThread(consumer1));
        callables.add(() -> runThread(consumer2));
        callables.add(() -> runThread(consumer3));
        callables.add(() -> runThread(consumer4));

        ExecutorService executorService = Executors.newFixedThreadPool(5);
        try {
            List<Future<Boolean>> futures = executorService.invokeAll(callables);

            futures.forEach(
                    future -> {
                        try {
                            System.out.println(future.get());
                        } catch (InterruptedException | ExecutionException e) {
                            System.out.println("Exception: " + e.getMessage());
                        }
                    });

        } finally {
            executorService.shutdown();
            System.out.println("Executor service shut down");
        }

    }

    public static Boolean runThread(Thread thread){
        try {
            thread.run();
            return true;
        }catch (Exception e){
            Logger.W(e);
            return false;
        }
    }

    public static boolean isEmpty(List<Integer> buffer) {
        return buffer.size() == 0;
    }

    public static boolean isFull(List<Integer> buffer) {
        return buffer.size() == 10;
    }
}
