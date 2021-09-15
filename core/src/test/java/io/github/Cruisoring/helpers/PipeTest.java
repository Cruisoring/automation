package io.github.Cruisoring.helpers;

import io.github.cruisoring.throwables.SupplierThrowable;
import io.github.cruisoring.tuple.Tuple3;
import org.junit.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class PipeTest {

    Pipe<Integer> pipe = new Pipe<>(10);

    public interface Executable {
//        void execute(int count, int interval);
        boolean execute(int count, Duration timeout);
        boolean execute(int count);
    }

    class Producer implements Executable {
        final Pipe<Integer> pipe;
        final AtomicInteger atomicInteger;
        final int delta;
        final String name;

        public Producer(String name, Pipe<Integer> pipe, int delta){
            this.name = name;
            this.pipe = pipe;
            this.atomicInteger = new AtomicInteger();
            this.delta = delta;
        }

//        @Override
//        public void execute(int count, int intervalMills){
//            for (int i = 0; i < count; i++) {
//                int next = (1+i) * delta;
//                Tuple2<Boolean, Object> tuple = pipe.push(next, 20);
//                if(!tuple.getFirst()){
//                    i--;
//                }
//                if(i == count-1){
//                    Logger.D(">>>>%s@%s: produce %s", name, Thread.currentThread().getId(), tuple);
//                } else {
//                    Logger.V(">>>>%s@%s: produce %s", name, Thread.currentThread().getId(), tuple);
//                }
//            }
//            Logger.I("%s@%s finished.", name, Thread.currentThread().getId());
//        }

        public boolean execute(int count, Duration timeout){
            for (int i = 0; i < count; i++) {
                int next = (1+i) * delta;
                Tuple3<Boolean, Integer, String> tuple = pipe.push(next, timeout);
                if(!tuple.getFirst()){
                    i--;
                }
                if(i == count-1){
                    Logger.D(">>>>%s@%s: produce %s", name, Thread.currentThread().getId(), tuple);
                } else {
                    Logger.V(">>>>%s@%s: produce %s", name, Thread.currentThread().getId(), tuple);
                }
            }
            Logger.I("%s@%s finished.", name, Thread.currentThread().getId());
            return true;
        }

        public boolean execute(int count){
            for (int i = 0; i < count; i++) {
                int next = (1+i) * delta;
                Tuple3<Boolean, Integer, String> tuple = pipe.push(next);
                if(!tuple.getFirst()){
                    Logger.D("----%s failed with %d: %s", name, atomicInteger.get(), tuple);
                    i--;
                }
                if(i == count-1){
                    Logger.D(">>>>%s@%s: produce %s", name, Thread.currentThread().getId(), tuple);
                } else {
                    Logger.V(">>>>%s@%s: produce %s", name, Thread.currentThread().getId(), tuple);
                }
            }
            Logger.I("%s@%s finished.", name, Thread.currentThread().getId());
            return true;
        }
    }

    class Consumer implements Executable {
        final Pipe<Integer> pipe;
        final String name;
        final AtomicInteger atomicInteger = new AtomicInteger();

        public Consumer(String name, Pipe<Integer> pipe){
            this.name = name;
            this.pipe = pipe;
        }
//
//        @Override
//        public void execute(int count, int intervalMills){
//            int failCount = 0;
//            for (int i = 0; i < count; i++) {
//                Tuple2<Boolean, Object> tuple = pipe.pop(3000);
//                if(tuple.getFirst()){
//                    Logger.V("----%s@%d: consume %s", name, atomicInteger.incrementAndGet(), tuple);
//                } else {
//                    Logger.D("----%s@%d: consume %s", name, atomicInteger.get(), tuple);
//                    if(++failCount > 5){
//                        break;
//                    }
//                }
//            }
//            Logger.I("%s@%s finished.", name, Thread.currentThread().getId());
//        }

        public boolean execute(int count, Duration timeout){
            int failCount = 0;
            for (int i = 0; i < count; i++) {
                Tuple3<Boolean, Integer, String> tuple = pipe.pop(timeout);
                if(tuple.getFirst()){
                    Logger.V("----%s@%d: consume %s", name, atomicInteger.incrementAndGet(), tuple);
                } else {
                    Logger.D("----%s@%d: consume %s", name, atomicInteger.get(), tuple);
                    if(++failCount > 5){
                        break;
                    }
                }
            }
            Logger.I("%s@%s finished.", name, Thread.currentThread().getId());
            return true;
        }

        public boolean execute(int count){
            int failCount = 0;
            for (int i = 0; i < count; i++) {
                Tuple3<Boolean, Integer, String> tuple = pipe.pop();
                if(tuple.getFirst()){
                    if(i == count-1){
                        Logger.I("----%s@%d: consume %s", name, atomicInteger.incrementAndGet(), tuple);
                    } else {
                        Logger.V("----%s@%d: consume %s", name, atomicInteger.incrementAndGet(), tuple);
                    }
                } else {
                    Logger.D("----%s@%d: consume %s", name, atomicInteger.get(), tuple);
                    if(++failCount > 5){
                        break;
                    }
                }
            }
            Logger.I("%s@%s finished.", name, Thread.currentThread().getId());
            return true;
        }
    }


    Producer producer1 = new Producer("Producer1", pipe, 1);
    Producer producer2 = new Producer("Producer2", pipe, -1);
    Consumer consumer1 = new Consumer("Consumer1", pipe);
    Consumer consumer2 = new Consumer("Consumer2", pipe);
    Consumer consumer3 = new Consumer("Consumer3", pipe);
    Consumer consumer4 = new Consumer("Consumer4", pipe);

    @Test
    public void pushPop2() {

        List<Callable<Boolean>> callables = new ArrayList<>();
        callables.add(() -> producer1.execute(50000, Duration.ofMillis(10)));
        callables.add(() -> producer2.execute(50000, Duration.ofMillis(10)));
        callables.add(() -> consumer1.execute( 30000, Duration.ofMillis(30)));
        callables.add(() -> consumer2.execute( 20000, Duration.ofMillis(20)));
        callables.add(() -> consumer3.execute( 10000, Duration.ofMillis(50)));
        callables.add(() -> consumer4.execute( 40000, Duration.ofMillis(10)));

        ExecutorService executorService = Executors.newFixedThreadPool(8);
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
        } catch (Exception ex){
        } finally {
            executorService.shutdown();
            System.out.println("Executor service shut down");
        }
    }

    @Test
    public void pushPop3() {

        List<SupplierThrowable<Boolean>> suppliers = new ArrayList<>();
        suppliers.add(() -> producer1.execute(500000));
        suppliers.add(() -> producer2.execute(500000));
        suppliers.add(() -> consumer1.execute(10000));
        suppliers.add(() -> consumer2.execute(20000));
        suppliers.add(() -> consumer3.execute(50000));
        suppliers.add(() -> consumer4.execute(20000));
        for (int i = 0; i < 100; i++) {
            Consumer consumer = new Consumer("Consumer"+i, pipe);
            suppliers.add(() -> consumer.execute(10000));
        }

        Executor.getParallel(suppliers);
    }
}