package io.github.Cruisoring.helpers;

import io.github.cruisoring.tuple.Tuple;
import io.github.cruisoring.tuple.Tuple3;

import java.time.Duration;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

public class Pipe<T> {
    public final static Duration DefaultMinTimeoutDuration = Duration.ofMillis(100);

    LinkedList<T> queue = new LinkedList<>();
    Lock lock = new ReentrantLock();
    Condition notFull = lock.newCondition();
    Condition notEmpty = lock.newCondition();

    private int capacity;
    private Duration minTimeoutDuration;

    public Pipe(int capacity){
        this.capacity = capacity;
        minTimeoutDuration = DefaultMinTimeoutDuration;
    }

    public Pipe(int capacity, Duration minTimeoutDuration){
        if(minTimeoutDuration.isNegative()||minTimeoutDuration.isZero()){
            minTimeoutDuration = DefaultMinTimeoutDuration;
        }
        this.capacity = capacity;
        this.minTimeoutDuration = minTimeoutDuration;
    }

    //TODO: apply lock
    public int size() {
        return queue.size();
    }

    public boolean isFull(){
        return size() >= capacity;
    }

    public boolean isEmpty(){
        return queue.isEmpty();
    }

    public int indexOf(T item){
        return queue.indexOf(item);
    }

    public Stream<T> stream(){
        return queue.stream();
    }

    public Tuple3<Boolean, T, String> pushIfAbsent(T item){
        int index = indexOf(item);
        if(index == -1){
            return push(item);
        } else {
            return Tuple.create(false, item, "Item already exist");
        }
    }

    public Tuple3<Boolean, T, String> push(T item, Duration timeout) {
        //indicator of if the lock is still held
        boolean isLocked = false;
        if(timeout == null || timeout.compareTo(minTimeoutDuration) < 0){
            timeout = minTimeoutDuration;
        }
        long timeoutNanos = timeout.toNanos();

        try {
            lock.lock();
            isLocked = true;
            while (queue.size() >= capacity) {
                Boolean awaitResult = notFull.await(timeoutNanos, TimeUnit.NANOSECONDS);
                if(!awaitResult){
                    lock.unlock();
                    isLocked = false;
                    return Tuple.create(false, item, "Timeout with full pipe.");
                }
            }

            queue.add(item);
            notEmpty.signal();
            lock.unlock();
            isLocked = false;
            Logger.V("@%d>>>>%s", Thread.currentThread().getId(), item);
            return Tuple.create(true, item, null);
        } catch (Exception e){
            Logger.W(e);
            return Tuple.create(false, item, e.getMessage());
        }finally {
            if(isLocked) {
                lock.unlock();
            }
        }
    }

    public Tuple3<Boolean, T, String> push(T item){
        return push(item, minTimeoutDuration);
    }

    public CompletableFuture<Tuple3<Boolean, T, String>> pushAsync(T item, Duration timeout){
        CompletableFuture<Tuple3<Boolean, T, String>> result = CompletableFuture.supplyAsync(() -> push(item, timeout));
        return result;
    }

    public CompletableFuture<Tuple3<Boolean, T, String>> pushAsync(T item){
        CompletableFuture<Tuple3<Boolean, T, String>> result = CompletableFuture.supplyAsync(() -> push(item));
        return result;
    }

    public Tuple3<Boolean, T, String> pop(T item){
        int index = queue.indexOf(item);
        if(index == -1){
            return Tuple.create(false, null, String.format("No such item: %s", item));
        }

        T removed = queue.remove(index);
        if(removed == item){
            return Tuple.create(true, removed, null);
        }

        return Tuple.create(false, removed, String.format("Removed '%s' is not matched with '%s'", removed, item));
    }

    public Tuple3<Boolean, T, String> pop(Duration timeout) {
        //indicator of if the lock is still held
        boolean isLocked = false;
        if(timeout == null || timeout.compareTo(minTimeoutDuration) < 0){
            timeout = minTimeoutDuration;
        }
        long timeoutNanos = timeout.toNanos();

        try {
            lock.lock();
            isLocked = true;
            while (queue.isEmpty()) {
                Boolean awaitResult = notEmpty.await(timeoutNanos, TimeUnit.NANOSECONDS);
                if(!awaitResult){
                    lock.unlock();
                    isLocked = false;
                    return Tuple.create(false, null, "Timeout with empty pipe.");
                }
            }
            T item = queue.remove();
            notFull.signal();
            lock.unlock();
            isLocked = false;
            Logger.V("@%d <<<< %s", Thread.currentThread().getId(), item);
            return Tuple.create(true, item, null);
        }catch (Exception e){
            Logger.W(e);
            return Tuple.create(false, null, e.getMessage());
        }finally {
            if(isLocked) {
                lock.unlock();
            }
        }
    }

    public Tuple3<Boolean, T, String> pop() {
        return pop(minTimeoutDuration);
    }

    public CompletableFuture<Tuple3<Boolean, T, String>> popAsync(Duration timeout){
        CompletableFuture<Tuple3<Boolean, T, String>> result = CompletableFuture.supplyAsync(() -> pop(timeout));
        return result;
    }

    public CompletableFuture<Tuple3<Boolean, T, String>> popAsync(){
        return popAsync(minTimeoutDuration);
    }
}
