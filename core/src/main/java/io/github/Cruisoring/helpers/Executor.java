package io.github.Cruisoring.helpers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class Executor{
    public final static long NanosPerMills = 1000000;
    public final static long defaultInitialDelayMills = 200;
    public final static long defaultDelayMills = 1000;
    public final static long MaxSleepMills = 10*1000;
    public final static  int MaxAttemptTimes = 10;
    public final static int DefaultAttemptTimes = 0;

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
            sleep(delayMills);
        }while (true);
    }

    public static Boolean attempt(Runnable[] actions, int times, BooleanSupplier evaluator) {
        return attempt(actions, times, evaluator, defaultDelayMills);
    }

    public static Boolean attemp(Runnable[] actions, BooleanSupplier evaluator) {
        return attempt(actions, DefaultAttemptTimes, evaluator);
    }

    public static Boolean attemp(Runnable action, BooleanSupplier evaluator) {
        return attemp(new Runnable[]{action}, evaluator);
    }

    public static Boolean attemp(Runnable action, int times, BooleanSupplier evaluator) {
        return attemp(action, times, evaluator);
    }

    public static Boolean testUntil(BooleanSupplier evaluator, long timeoutMillis){
        return testUntil(evaluator, timeoutMillis, defaultDelayMills, defaultInitialDelayMills);
    }

    public static Boolean testUntil(BooleanSupplier evaluator, long timeoutMillis, long delayMills, long initialDelayMills) {
        if(initialDelayMills > 0)
            sleep(initialDelayMills);
        LocalDateTime until = LocalDateTime.now().plus(Duration.ofMillis(timeoutMillis));
        while (LocalDateTime.now().isBefore(until)){
            try {
                Boolean result = evaluator.getAsBoolean();

                if (result)
                    return result;
                sleep(delayMills);
            }catch (Exception e){
                Logger.V(e);
            }
        }
        return false;
    }

    public static void sleep(long timeMills) {
        try {
            if (timeMills > 0) {
                TimeUnit.MILLISECONDS.sleep(Math.min(MaxSleepMills, timeMills));
//                Thread.sleep(timeMills);
            }
        } catch (InterruptedException e) {
            Logger.V(e);
        }
    }

    public static void tryRun(Runnable[] actions, int attempts, Long intervalMills, int count) {
        try {
            actions[count % actions.length].run();
        } catch (Exception ex) {
            //Logger.V(ex);
            Logger.V("Running %d action throw: '%s'", count, ex.getMessage());
            if(count >= attempts) {
                Logger.W("Failed to run actions with last exception of '%s'", count, ex.getMessage());
                throw ex;
            }
            sleep(intervalMills);
            tryRun(actions, attempts, intervalMills, count+1);
        }
    }

    public static void tryRun(Runnable[] actions, int attempts, Long intervalMills) {
        tryRun(actions, attempts, intervalMills, 0);
    }

    public static void tryRun(Runnable[] actions, int attempts) {
        tryRun(actions, attempts, defaultDelayMills, 0);
    }

    public static void tryRun(Runnable[] actions) {
        tryRun(actions, 0, defaultDelayMills, 0);
    }

    public static void tryRun(Runnable action, int attempts) {
        tryRun(new Runnable[]{action}, attempts);
    }

    public static void tryRun(Runnable action, int attempts, Long intervalMills) {
        tryRun(new Runnable[]{action}, attempts, intervalMills);
    }

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

    public static <T> T tryGet(Supplier<T> supplier, int attempts, long delayMills) {
        return tryGet(supplier, attempts, delayMills, t -> t!=null);
    }

    public static <T> T tryGet(Supplier<T> supplier, int attempts, Predicate<T> evaluate) {
        return tryGet(supplier, attempts, defaultDelayMills, evaluate);
    }

    public static <T> T tryGet(Supplier<T> supplier, int attempts) {
        return tryGet(supplier, attempts, defaultDelayMills, t -> t != null);
    }

    public static <T> T tryGet(Supplier<T> supplier) {
        return tryGet(supplier, 1, 0, t -> t != null);
    }
}
