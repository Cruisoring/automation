package io.github.Cruisoring.helpers;

import io.github.Cruisoring.enums.LogLevel;
import io.github.Cruisoring.wrappers.UIObject;
import io.github.cruisoring.tuple.Tuple3;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Logger {
    public final static String PercentageAscii = "&#37";

    public static final String LoggerName = Logger.class.getName();
    public static final String ExecutorName = Executor.class.getName();
    public static final String UIObjectName = UIObject.class.getName();
    public static final String[] automationNeglibles = new String[] { LoggerName }; //, , ExecutorNameUIObjectName};

    public static final String SunReflect = "sun.reflect";
    public static final String[] platformNeglibles = new String[] { SunReflect };

    public static final int DefaultStackCount = 3;
    public static final Boolean MeasurePerformanceEnabled = false;


    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_LIGHT_YELLOW = "\u001B[93m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";
    public static final String ANSI_BOLD = "\u001B[1m";
    public static final String ANSI_UNBOLD = "\u001B[21m";
    public static final String ANSI_UNDERLINE = "\u001B[4m";
    public static final String ANSI_STOP_UNDERLINE = "\u001B[24m";
    public static final String ANSI_BLINK = "\u001B[5m";

    // Reset
    public static final String RESET = "\033[0m";  // Text Reset

    // Regular Colors
    public static final String BLACK = "\033[0;30m";   // BLACK
    public static final String RED = "\033[0;31m";     // RED
    public static final String GREEN = "\033[0;32m";   // GREEN
    public static final String YELLOW = "\033[0;33m";  // YELLOW
    public static final String BLUE = "\033[0;34m";    // BLUE
    public static final String PURPLE = "\033[0;35m";  // PURPLE
    public static final String CYAN = "\033[0;36m";    // CYAN
    public static final String WHITE = "\033[0;37m";   // WHITE

    // Bold
    public static final String BLACK_BOLD = "\033[1;30m";  // BLACK
    public static final String RED_BOLD = "\033[1;31m";    // RED
    public static final String GREEN_BOLD = "\033[1;32m";  // GREEN
    public static final String YELLOW_BOLD = "\033[1;33m"; // YELLOW
    public static final String BLUE_BOLD = "\033[1;34m";   // BLUE
    public static final String PURPLE_BOLD = "\033[1;35m"; // PURPLE
    public static final String CYAN_BOLD = "\033[1;36m";   // CYAN
    public static final String WHITE_BOLD = "\033[1;37m";  // WHITE

    // Underline
    public static final String BLACK_UNDERLINED = "\033[4;30m";  // BLACK
    public static final String RED_UNDERLINED = "\033[4;31m";    // RED
    public static final String GREEN_UNDERLINED = "\033[4;32m";  // GREEN
    public static final String YELLOW_UNDERLINED = "\033[4;33m"; // YELLOW
    public static final String BLUE_UNDERLINED = "\033[4;34m";   // BLUE
    public static final String PURPLE_UNDERLINED = "\033[4;35m"; // PURPLE
    public static final String CYAN_UNDERLINED = "\033[4;36m";   // CYAN
    public static final String WHITE_UNDERLINED = "\033[4;37m";  // WHITE

    // Background
    public static final String BLACK_BACKGROUND = "\033[40m";  // BLACK
    public static final String DARK_GRAY_BACKGROUND = "\033[100m";  // BLACK
    public static final String RED_BACKGROUND = "\033[101m";    // RED
    public static final String GREEN_BACKGROUND = "\033[102m";  // GREEN
    public static final String YELLOW_BACKGROUND = "\033[103m"; // YELLOW
    public static final String BLUE_BACKGROUND = "\033[104m";   // BLUE
    public static final String PURPLE_BACKGROUND = "\033[105m"; // PURPLE
    public static final String CYAN_BACKGROUND = "\033[106m";   // CYAN
    public static final String WHITE_BACKGROUND = "\033[107m";  // WHITE

    static final BiFunction<LogLevel, String, String> defaultMessageFormmater = (level, time) -> {
        String label = String.format("[%s]@%s:", StringUtils.upperCase(level.toString()).charAt(0),
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME));
        switch (level) {
            case verbose:
                return String.format("%s%s %s%s", WHITE_BACKGROUND+BLUE, label, time, ANSI_RESET);
            case debug:
                return String.format("%s%s %s%s", PURPLE_UNDERLINED, label, time, ANSI_RESET);
            case info:
                return String.format("%s%s %s%s", CYAN_BACKGROUND+BLACK, label, time, ANSI_RESET);
            case warning:
                return String.format("%s%s %s%s", YELLOW_BACKGROUND+PURPLE_UNDERLINED, label, time, ANSI_RESET);
            case error:
                return String.format("%s%s %s%s", RED_BACKGROUND+BLACK_BOLD, label, time, ANSI_RESET);
            default:
                return String.format("%s %s%s", label, time, ANSI_RESET);
        }
    };

    static final Function<LogLevel, Integer> getDefaultStackCount = level -> {
        switch (level) {
            case verbose:
                return 5;
            case debug:
                return 3;
            case info:
                return 3;
            case warning:
                return 5;
            case error:
                return 8;
            default:
                return 0;
        }
    };

    public static String getStackTrace() {
        return getStackTrace(DefaultStackCount);
    }

    public static String getStackTrace(int stackCount){
        if (stackCount == 0)
            return  "";

        List<String> stacks = getStackTraceElements(stackCount);
        AtomicInteger counter = new AtomicInteger();

        String result = stacks.stream()
                .map(s -> String.format("%s%s", StringUtils.repeat(" ", 2* counter.getAndIncrement()), s))
                .collect(Collectors.joining("\r\n"));

        return result;
    }

    public static List<String> getStackTraceElements(int stackCount){
        StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
        List<String> stackDescriptions = Arrays.stream(stacks).map(stack -> stack.toString())
                .collect(Collectors.toList());
        int first=-1, last=-1;
        for(int i=0; i<stackDescriptions.size(); i++){
            String desc = stackDescriptions.get(i);
            if (first==-1){
                if (!StringExtensions.containsAny(desc, automationNeglibles)) continue;
            }
            if (last == -1 && StringExtensions.containsAny(desc, automationNeglibles)) {
                first = i;
                continue;
            }
            if (last == -1) {
                first = i;
                last = stackDescriptions.size();
                continue;
            } else if (StringExtensions.containsAny(desc, platformNeglibles)){
                last = i;
                break;
            }
        }

        int total = last-first;
        if (stackCount > 0 && stackCount < total) {
            first = last - stackCount;
        }else if (stackCount < 0 && total > -stackCount) {
            last = first - stackCount;
        }
        stackCount = Math.min(total, Math.abs(stackCount));

        return stackDescriptions.stream()
                .skip(first)
                .limit(stackCount)
                .collect(Collectors.toList());
    }

    private static final Pipe<String> recentExceptionMessages = new Pipe<String>(10);
    static String lastMessage;
    static Logger logException(LogLevel logLevel, Exception ex){
        if(OnlyDefaultLogger != null) {
            String message = ex.getMessage();
            if(message == null)

            if(StringUtils.equalsIgnoreCase(message, lastMessage)){
                return OnlyDefaultLogger;
            }
            Tuple3<Boolean, String, String> tuple = recentExceptionMessages.pushIfAbsent(message);
            if(tuple.getFirst() && StringUtils.isNotEmpty(tuple.getSecond())) {
                OnlyDefaultLogger.log(logLevel, message);
                String stackTrace = getStackTrace(getDefaultStackCount.apply(logLevel));
                OnlyDefaultLogger.log(logLevel, stackTrace);
            } else {
                message = (message == null ? ex.toString() : message).substring(0, message.indexOf('\n'));
                OnlyDefaultLogger.log(logLevel, String.format("Same error as recent one: %s", message));
            }

            if(recentExceptionMessages.isFull()){
                recentExceptionMessages.pop();
            }
        }
        return OnlyDefaultLogger;

    }

    static final Logger OnlyDefaultLogger = new Logger(System.out::println,
            LogLevel.verbose,
            LogLevel.debug,
            LogLevel.info, LogLevel.warning, LogLevel.error);

    public static Timer M(){
        if(OnlyDefaultLogger == null || !MeasurePerformanceEnabled)
            return null;

        Consumer<String> output = OnlyDefaultLogger.output;
        return new Timer(output, Timer.highlightTimerFormatter);
    }

    public static Logger V(Exception ex) {
        return logException(LogLevel.verbose, ex);
    }

    public static Logger D(Exception ex) {
        return logException(LogLevel.debug, ex);
    }

    public static Logger I(Exception ex) {
        return logException(LogLevel.info, ex);
    }

    public static Logger W(Exception ex) {
        return logException(LogLevel.warning, ex);
    }

    public static Logger E(Exception ex) {
        return logException(LogLevel.error, ex);
    }

    public static Logger V(String format, Object... args){
        if(OnlyDefaultLogger != null ){
            //String stack = getStackTrace();
            OnlyDefaultLogger.log(LogLevel.verbose, format, args);
        }
        return OnlyDefaultLogger;
    }

    public static Logger D(String format, Object... args){
        if(OnlyDefaultLogger != null ){
            OnlyDefaultLogger.log(LogLevel.debug, format, args);
        }
        return OnlyDefaultLogger;
    }

    public static Logger I(String format, Object... args){
        if(OnlyDefaultLogger != null ){
            OnlyDefaultLogger.log(LogLevel.info, format, args);
        }
        return OnlyDefaultLogger;
    }

    public static Logger W(String format, Object... args){
        if(OnlyDefaultLogger != null ){
            OnlyDefaultLogger.log(LogLevel.warning, format, args);
        }
        return OnlyDefaultLogger;
    }

    public static Logger E(String format, Object... args){
        if(OnlyDefaultLogger != null ){
            OnlyDefaultLogger.log(LogLevel.error, format, args);
        }
        return OnlyDefaultLogger;
    }

    /**
     * Try to call String.format() and refrain potential IllegalFormatException
     * @param format    template to compose a string with given arguments
     * @param args      arguments to be applied to the above template
     * @return          string formatted with the given or exceptional template.
     */
    public static String tryFormatString(String format, Object... args) {
        Objects.requireNonNull(format);
        try {
            String formatted = String.format(format, args);
            formatted = formatted.replaceAll(PercentageAscii, "%");
            return formatted;
        } catch (Exception e) {
            String[] argStrings = Arrays.stream(args).map(arg -> arg.toString()).toArray(size -> new String[size]);
            return String.format("MalFormated format: '%s'\nargs: '%s'", format, String.join(", ", argStrings));
        }
    }


    public final Consumer<String> output;
//    public LogLevel lastLogLevel;
    private final EnumSet<LogLevel> levels;
    private final BiFunction<LogLevel, String, String> messageFormatter;
    private final Function<LogLevel, Integer> stackCountFun;

    public Logger(Consumer<String> output,
                  BiFunction<LogLevel, String, String> formatter,
                  Function<LogLevel, Integer> stackCountFunction,
                  LogLevel first, LogLevel... rest) {
        this.output = output;
        this.messageFormatter = formatter;
        this.levels = EnumSet.of(first, rest);
        this.stackCountFun = stackCountFunction;
//        lastLogLevel = LogLevel.verbose;
    }
    public Logger(Consumer<String> output, BiFunction<LogLevel, String, String> formatter, LogLevel first, LogLevel... rest) {
        this(output, formatter, getDefaultStackCount, first, rest);
    }

    public Logger(Consumer<String> output, LogLevel first, LogLevel... rest)
    {
        this(output, defaultMessageFormmater, first, rest);
    }

    LocalDateTime lastMessageUntil = LocalDateTime.MIN;
    public void log(LogLevel level, String message) {
        if(output != null && levels.contains(level)) {
            if(!StringUtils.equals(lastMessage, message) || LocalDateTime.now().isAfter(lastMessageUntil)){
                lastMessage = message;
                lastMessageUntil = LocalDateTime.now().plusSeconds(10);
                String finalOutput = messageFormatter.apply(level, message);
                output.accept(finalOutput);
            }
        }
    }

    public void log(Message message) {
        log(message.level, message.content);
    }

    public void log(LogLevel level, String format, Object... args) {
        log(level, tryFormatString(format, args));
    }

    public void log(LogLevel level, Exception ex) {
        int stackCount = stackCountFun.apply(level);
        if(stackCount != 0) {
            String stackTrace = getStackTrace(stackCountFun.apply(level));
            log(level, tryFormatString("%s:%s\r\n%s", ex.getClass().getSimpleName(), ex.getMessage(), stackTrace));
        } else {
            log(level, tryFormatString("%s: %s", ex.getClass().getSimpleName(), ex.getMessage()));
        }
    }

    public void verbose(String format, Object... args){
        log(LogLevel.verbose, tryFormatString(format, args));
    }

    public void debug(String format, Object... args){
        log(LogLevel.debug, tryFormatString(format, args));
    }

    public void info(String format, Object... args){
        log(LogLevel.info, tryFormatString(format, args));
    }

    public void warning(String format, Object... args){
        log(LogLevel.warning, tryFormatString(format, args));
    }

    public void error(String format, Object... args){
        log(LogLevel.error, tryFormatString(format, args));
    }

    public class Message{
        public final LogLevel level;
        public final String content;
        public Message(LogLevel l, String c){
            level = l;
            content = c;
        }
    }

    public static class Timer implements AutoCloseable {
        private static final Pattern pattern = Pattern.compile("(\\w+\\.\\w+\\(.*?)$");
        public static final Function<String, String> defaultTimerFormatter = s ->
                String.format("%s%s", ANSI_RESET, s);
        public static final Function<String, String> highlightTimerFormatter = s ->
                String.format("%s%s%s%s", ANSI_RED, YELLOW_BACKGROUND, s, ANSI_RESET);

        public static String getClassMethod(String stackTrace){
            Matcher matcher = pattern.matcher(stackTrace);
            return matcher.find() ? matcher.group() : "";
        }

        private final Consumer<String> output;
        private final Function<String, String> formatter;
        private final String entryPoint;
        private final long startTime;

        public Timer(Consumer<String> output, Function<String, String> formatter){
            this.output = output;
            this.formatter = formatter;
            List<String> stacks = getStackTraceElements(-3);
            entryPoint = stacks.stream()
                    .map(Timer::getClassMethod)
                    .collect(Collectors.joining(" <- "));
            startTime = System.currentTimeMillis();
        }

        public Timer(Consumer<String> output){
            this(output, defaultTimerFormatter);
        }

        @Override
        public void close() throws Exception {
            if(output!= null){
                String message = String.format("%dms: %s", System.currentTimeMillis()-startTime, entryPoint);
                message = formatter.apply(message);
                output.accept(message);
            }
        }
    }
}
