package io.github.Cruisoring.helpers;

import io.github.cruisoring.function.ConsumerThrowable;
import io.github.cruisoring.function.SupplierThrowable;
import sun.security.pkcs11.wrapper.Functions;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Generic helper class to run periodically or parallelly.
 */
public class Executor{

    public final static long defaultInitialDelayMills = 200;
    public final static long defaultDelayMills = 1000;
    public final static long MaxSleepMills = 300*1000;
    public final static  int MaxAttemptTimes = 100;
    public final static int DefaultAttemptTimes = 2;

    public final static BooleanSupplier AlwaysFalse = () -> false;
    public final static BooleanSupplier AlwaysTrue = () -> true;


    /**
     * Running actions rotately until expected condition is met.
     * @param actions   Actions to be executed one by one.
     * @param times     Total numbers to be executed.
     * @param evaluator Predicate to check if expected condition is met.
     * @param delayMills Initial delay in mills.
     * @return          True if expected condition is met, otherwise false.
     */
    public static Boolean attempt(Runnable[] actions, int times, BooleanSupplier evaluator, long delayMills) {
        times = Math.min(times, MaxAttemptTimes);
        int count = 0;
        int actionsCount= actions.length;
        Boolean result = false;
        do {
            result = evaluator.getAsBoolean();
            if (result == true || count > times) {
                return result;
            }
            actions[count++ % actionsCount].run();
            result = evaluator.getAsBoolean();
            if (result == true || count > times) {
                return result;
            }
            sleep(delayMills);
        }while (true);
    }

    /**
     * Running actions rotately until expected condition is met with no delay.
     * @param action   Action to be executed.
     * @param times     Total numbers to be executed.
     * @param evaluator Predicate to check if expected condition is met.
     * @param delayMills Initial delay in mills.
     * @return          True if expected condition is met, otherwise false.
     */
    public static Boolean attempt(Runnable action, int times, BooleanSupplier evaluator, long delayMills) {
        return attempt(new Runnable[]{action}, times, evaluator, delayMills);
    }

    /**
     * Running actions rotately until expected condition is met with no delay and zero retry.
     * @param actions   Actions to be executed one by one.
     * @param evaluator Predicate to check if expected condition is met.
     * @return          True if expected condition is met, otherwise false.
     */
    public static Boolean attempt(Runnable[] actions, BooleanSupplier evaluator) {
        return attempt(actions, DefaultAttemptTimes, evaluator, defaultDelayMills);
    }

    /**
     * Execute action until expected condition is met with no delay and zero retry.
     * @param action   Action to be executed.
     * @param evaluator Predicate to check if expected condition is met.
     * @return          True if expected condition is met, otherwise false.
     */
    public static Boolean attempt(Runnable action, BooleanSupplier evaluator) {
        return attempt(new Runnable[]{action}, evaluator);
    }

    /**
     * Test with given evaluator until it is true or timeout.
     * @param evaluator         Predicate to check if expected condition is met.
     * @param timeoutMillis     Timeout in mills.
     * @return
     */
    public static Boolean testUntil(BooleanSupplier evaluator, long timeoutMillis){
        return testUntil(evaluator, timeoutMillis, defaultDelayMills, defaultInitialDelayMills);
    }

    /**
     * Test with given evaluator until it is true or timeout.
     * @param evaluator         Predicate to check if expected condition is met.
     * @param timeoutMillis     Timeout in mills.
     * @param delayMills        Delay between tests in mills.
     * @param initialDelayMills Initial dealy in mills.
     * @return
     */
    public static Boolean testUntil(BooleanSupplier evaluator, long timeoutMillis, long delayMills, long initialDelayMills) {
        if(initialDelayMills > 0)
            sleep(initialDelayMills);
        LocalDateTime until = DateTimeHelper.getSystemLocalDateTime().plus(Duration.ofMillis(timeoutMillis));
        Exception lastException = null;
        while (DateTimeHelper.getSystemLocalDateTime().isBefore(until)){
            try {
                Boolean result = evaluator.getAsBoolean();

                if (result)
                    return result;
                sleep(delayMills);
            }catch (Exception e){
                if(lastException == null || lastException.getMessage() != e.getMessage()){
                    lastException = e;
                    Logger.E(e);
                } else {
                    throw e;
                }
            }
        }
        return false;
    }

    /**
     * Thread sleep in mills.
     * @param timeMills     Sleep time in mills.
     */
    public static void sleep(long timeMills) {
        try {
            if (timeMills > 0) {
                TimeUnit.MILLISECONDS.sleep(Math.min(MaxSleepMills, timeMills));
            }
        } catch (InterruptedException e) {
            Logger.E(e);
        }
    }

    /**
     * Run actions one by one multiple times and with delay.
     * @param actions       All actions to be run one by one.
     * @param attempts      Total times to be run.
     * @param intervalMills Delay between actions.
     * @param count         How many times have been run.
     */
    public static void tryRun(Runnable[] actions, int attempts, Long intervalMills, int count) {
        try {
            actions[count % actions.length].run();
        } catch (Exception ex) {
            Logger.D("Running %d action throw: '%s'", count, ex.getMessage());
            if(count >= attempts) {
                Logger.W("Failed to run actions with last exception of '%s'", count, ex.getMessage());
                throw ex;
            }
            sleep(intervalMills);
            tryRun(actions, attempts, intervalMills, count+1);
        }
    }

    /**
     * Run actions one by one multiple times and with delay.
     * @param actions       All actions to be run one by one.
     * @param attempts      Total times to be run.
     * @param intervalMills Delay between actions.
     */
    public static void tryRun(Runnable[] actions, int attempts, Long intervalMills) {
        tryRun(actions, attempts, intervalMills, 0);
    }

    /**
     * Run actions one by one multiple times with default delay.
     * @param actions       All actions to be run one by one.
     * @param attempts      Total times to be run.
     */
    public static void tryRun(Runnable[] actions, int attempts) {
        tryRun(actions, attempts, defaultDelayMills, 0);
    }

    /**
     * Run actions one by one multiple times with default delay.
     * @param actions       All actions to be run one by one.
     */
    public static void tryRun(Runnable[] actions) {
        tryRun(actions, 0, defaultDelayMills, 0);
    }

    /**
     * Run action multiple times with default delay.
     * @param action        The action to be run.
     * @param attempts      Total times to be run.
     */
    public static void tryRun(Runnable action, int attempts) {
        tryRun(new Runnable[]{action}, attempts);
    }

    /**
     * Run action multiple times with given delay.
     * @param action        The action to be run.
     * @param attempts      Total times to be run.
     * @param intervalMills Delay between actions.
     */
    public static void tryRun(Runnable action, int attempts, Long intervalMills) {
        tryRun(new Runnable[]{action}, attempts, intervalMills);
    }

    /**
     * Generic method to get T value until it meet some criteria or timeout.
     * @param supplier      How to get T value.
     * @param attempts      How many times to try until give up.
     * @param delayMills    Delay between gettings.
     * @param evaluate      Predicate to see if the retrieved value is qualified.
     * @param <T>           Type of the value to be returned.
     * @return              Value returned by the supplier or null if some exception happens.
     */
    public static <T> T tryGet(Supplier<T> supplier, int attempts, long delayMills, Predicate<T> evaluate) {
        T result;
        while(true) {
            try {
                result = supplier.get();
            } catch(Exception ex) {
                //Logger.V(ex);
                result = null;
            }
            if(evaluate.test(result)) {
                return result;
            } else if (attempts-- > 0) {
                if (delayMills > 0) {
                    sleep(delayMills);
                }
            } else {
                break;
            }
        }
        return result;
    }

    /**
     * Generic method to get T value until it is not NULL or timeout.
     * @param supplier      How to get T value.
     * @param attempts      How many times to try until give up.
     * @param delayMills    Delay between gettings.
     * @param <T>           Type of the value to be returned.
     * @return              Value returned by the supplier or null if some exception happens.
     */
    public static <T> T tryGet(Supplier<T> supplier, int attempts, long delayMills) {
        return tryGet(supplier, attempts, delayMills, t -> t!=null);
    }

    /**
     * Generic method to get T value with default interval until it is not NULL or timeout.
     * @param supplier      How to get T value.
     * @param attempts      How many times to try until give up.
     * @param <T>           Type of the value to be returned.
     * @return              Value returned by the supplier or null if some exception happens.
     */
    public static <T> T tryGet(Supplier<T> supplier, int attempts, Predicate<T> evaluate) {
        return tryGet(supplier, attempts, defaultDelayMills, evaluate);
    }

    /**
     * Generic method to get T value with default interval until it is not NULL or timeout.
     * @param supplier      How to get T value.
     * @param attempts      How many times to try until give up.
     * @param <T>           Type of the value to be returned.
     * @return              Value returned by the supplier or null if some exception happens.
     */
    public static <T> T tryGet(Supplier<T> supplier, int attempts) {
        return tryGet(supplier, attempts, defaultDelayMills, t -> t != null);
    }

    /**
     * Generic method to get T value with default interval until it is not NULL or timeout.
     * @param supplier      How to get T value.
     * @param <T>           Type of the value to be returned.
     * @return              Value returned by the supplier or null if some exception happens.
     */
    public static <T> T tryGet(Supplier<T> supplier) {
        return tryGet(supplier, 1, 0, t -> t != null);
    }

    /**
     * Run commands with one input to get one output in parallel.
     * @param howTo     Method to get T value with given R input.
     * @param tasks     All inputs of R type.
     * @param timeoutMinutes    maximum time in minute to wait a process done
     * @param <T>       Returned Type.
     * @param <R>       Input Type.
     * @return          Returned values as a list.
     * @throws Throwable
     */
    public static <T, R> List<R> runParallel(Function<T, R> howTo, List<T> tasks, long timeoutMinutes)
            throws Exception{
        List<Callable<R>> callables = new ArrayList<>();
        tasks.stream().forEach(t -> {
            Callable<R> callable = () -> howTo.apply(t);
            callables.add(callable);
        });

        ExecutorService EXEC = Executors.newCachedThreadPool();
        try {
            List<R> results;
            results = EXEC.invokeAll(callables)
                    .stream()
                    .map(f -> {
                        try {
                            return f.get(timeoutMinutes, TimeUnit.MINUTES);
                        } catch (Exception e) {
                            Logger.W(e);
                            return null;
//                            throw new IllegalStateException(e);
                        }
                    })
                    .collect(Collectors.toList());
            return results;
        } finally{
            EXEC.shutdown();
        }
    }

    /**
     * Run commands with one input to get one output in parallel.
     * @param howTo     Method to perform action upon a task instance.
     * @param tasks     All inputs of R type.
     * @param timeoutMinutes    maximum time in minute to wait a process done
     * @param <T>       Returned Type.
     * @return          Returned values as a list.
     * @throws Throwable
     */
    public static <T> List<Boolean> runParallel(ConsumerThrowable<T> howTo, List<T> tasks, long timeoutMinutes)
            throws Exception{

        Function<T, Boolean> function = (t) -> {
            try {
                howTo.accept(t);
                return true;
            }catch (Exception ex){
                return false;
            }
        };

        List<Callable<Boolean>> callables = new ArrayList<>();
        tasks.stream().forEach(t -> {
            Callable<Boolean> callable = () -> function.apply(t);
            callables.add(callable);
        });

        ExecutorService EXEC = Executors.newCachedThreadPool();
        try {
            List<Boolean> results;
            results = EXEC.invokeAll(callables)
                    .stream()
                    .map(f -> {
                        try {
                            return f.get(timeoutMinutes, TimeUnit.MINUTES);
                        } catch (Exception e) {
                            Logger.W(e);
                            return null;
//                            throw new IllegalStateException(e);
                        }
                    })
                    .collect(Collectors.toList());
            return results;
        } finally{
            EXEC.shutdown();
        }
    }

    public static <T> List<T> runParallel(int nThreads, List<SupplierThrowable<T>> suppliers){
        ExecutorService executor = Executors.newFixedThreadPool(nThreads);

        List<CompletableFuture<T>> futures = suppliers.stream()
                .map( supplier -> CompletableFuture.supplyAsync(supplier.orElse(null), executor))
                .collect(Collectors.toList());

        CompletableFuture<Void> all = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        CompletableFuture<List<T>> result = all.thenApply(v -> {
            return futures.stream()
                    .map(pageContentFuture -> pageContentFuture.join())
                    .collect(Collectors.toList());
        });
        try {
            return result.get();
        }catch (Exception ex){
            return null;
        }
    }
}
