package io.github.Cruisoring.helpers;

import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper utilities to handle DateTime in one place.
 */
public class DateTimeHelper {
    public static final List<String> SysDateIdentifiers = Arrays.asList("%SYSTEM_DATE%", "SYS.DATE", "TODAY", "SYS.TODAY", "SYSTEM.DATE", "SYSTEM_DATE");
    private static final Pattern dateAdjustPattern = Pattern.compile("([+|-])\\s?([\\d]+)");

    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    public static final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter systemDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter systemMonthFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
    public static final DateTimeFormatter systemTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter systemTimeFormatter2 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    public static final DateTimeFormatter shortTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    public static final DateTimeFormatter shortDateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    public static final DateTimeFormatter dayOnlyFormatter = DateTimeFormatter.ofPattern("dd");

    static {
        ReportHelper.reportAsStepLog("COMPUTERNAME=%s", System.getenv("COMPUTERNAME"));
    }

    public static ZoneId SystemTimeZoneId = ZoneId.of("Australia/Brisbane");

    /**
     * Convert the LocalDateTime to Brisbane time that are used by APA EC.
     * @return Converted Brisbane LocalDateTime.
     */
    public static LocalDateTime getSystemLocalDateTime() {
        return LocalDateTime.now().atZone(ZoneId.systemDefault())
                .withZoneSameInstant(SystemTimeZoneId).toLocalDateTime();
    }

    /**
     * Check if the given startTime and current System time is out of the specific timeout
     * @param since     Start of the period
     * @param timeout   Duration not timeout if the current System time is within its scope
     * @return      <tt>true</tt> if timeout, otherwise <tt>false</tt>
     */
    public static boolean isTimeout(LocalDateTime since, Duration timeout){
        return isTimeout(since, timeout.getSeconds()*1000);
    }

    /**
     * Check if the given startTime and current System time is out of the specific timeout
     * @param since     Start of the period
     * @param timeoutMills   Duration of millis not timeout if the current System time is within its scope
     * @return      <tt>true</tt> if timeout, otherwise <tt>false</tt>
     */
    public static boolean isTimeout(LocalDateTime since, long timeoutMills){
        long elapsedMills = ChronoUnit.MILLIS.between(since, getSystemLocalDateTime());
        return elapsedMills > timeoutMills;
    }

    /**
     * Get the LocalDateTime from a given String.
     * @param timeString timeValue string.
     * @return Parsed LocalDateTime.
     */
    public static LocalDateTime parseLocalDateTime(String timeString){
        try {
            return LocalDateTime.parse(timeString, systemTimeFormatter);
        } catch (DateTimeParseException e){
            return LocalDateTime.parse(timeString, systemTimeFormatter2).plusSeconds(59);
        }
    }

    /**
     * Formatted String matched with APA EC.
     * @param time  Time to be formatted.
     * @return  Formatted String.
     */
    public static String dateTimeString(LocalDateTime time) {
        return time==null ? "null" : systemTimeFormatter.format(time);
    }

    /**
     * Format LocalDateTime to show only Hours, Minutes and Seconds.
     * @param time  LocalDateTime instance to be formatted
     * @return      Formatted String representing the time
     */
    public static String shortTimeString(LocalDateTime time) {
        return time == null ? "null" : shortTimeFormatter.format(time);
    }


    /**
     * Formatted String matched with APA EC.
     * @param date  Date to be formatted.
     * @return  Formatted String.
     */
    public static String dateString(LocalDate date) {
        return date == null ? "null" : systemDateFormatter.format(date);
    }

    public static String dateString(Date date) {
        return date == null ? "null" :
                dateTimeFormat.format(date).replace(" 00:00:00", "");
    }

    public static String dateString(YearMonth month) {
        return month == null ? "null" : systemMonthFormatter.format(month);
    }

    /**
     * Parse the string of system date format to get corresponding LocalDate
     * @param dateString    date string in form of 'yyyy-mm-dd'
     * @return  parsed LocalDate result.
     */
    public static LocalDate dateFromString(String dateString){
        Objects.requireNonNull(dateString);

        try {
            return LocalDate.parse(dateString, systemDateFormatter);
        } catch (DateTimeParseException ex) {
            String bestMatched = MapHelper.containsKeyIgnoreCase(dateString, SysDateIdentifiers);
            if (bestMatched == null)
                return null;

            Matcher matcher = dateAdjustPattern.matcher(dateString);
            int dayToAdjust = 0;
            while (matcher.find()) {
                if (matcher.group(1).equals("+")) {
                    dayToAdjust += Integer.parseInt(matcher.group(2));
                } else {
                    dayToAdjust -= Integer.parseInt(matcher.group(2));
                }
            }
            return LocalDate.now().plusDays(dayToAdjust);
        }
    }

    public static LocalDate dateFromString(String dateString, String... formats){
        for (String formatString : formats)
        {
            try {
                return LocalDate.parse(dateString, DateTimeFormatter.ofPattern(formatString));
            }
            catch (Exception e) {}
        }

        return null;
    }

    /**
     * Parse the string of system date format to get corresponding YearMonth
     * @param dateString    date string in form of 'yyyy-mm'
     * @return  parsed YearMonth result.
     */
    public static YearMonth monthFromString(String dateString){
        return YearMonth.parse(dateString, systemMonthFormatter);
    }

    public static String timeString(LocalTime time) {
        return time.toString();
    }

    /**
     * Convert the duration string of 'HH:mm:ss' to Duration.
     * @param durationString    String in format of 'HH:mm:SS'.
     * @return  Duration value if conversion is success, or Null if failed.
     */
    public static Duration durationOf(String durationString){
        try {
            LocalTime time = LocalTime.parse(durationString);
            return Duration.between(LocalTime.MIN, time);
        } catch (Exception ex){
            return null;
        }
    }

    /**
     * Representing the duration with only Hours, Minutes and Seconds and neglect Hours when it is within one hour.
     * @param duration  Duration instance to be formatted
     * @return          Formatted string hh:mm:ss of the concerned Duration instance.
     */
    public static String durationStringOf(Duration duration){
        long totalSeconds = duration.getSeconds();
        String negativeOrEmpty = totalSeconds < 0 ? "-" : "";
        totalSeconds = Math.abs(totalSeconds);

        return String.format("%s%02d:%02d:%02d", negativeOrEmpty, totalSeconds / 3600, (totalSeconds % 3600) / 60, (totalSeconds % 60));
    }


    public static Long getTotalSeconds(String durationString){
        Duration duration = durationOf(durationString);
//        System.out.println(String.format("'%s' is parsed as %s", durationString, duration));
        return duration == null ? null : duration.getSeconds();
    }

    /**
     * Convert LocalDate instance to Date at the default System Time Zone.
     * @param localDate LocalDate instance to be converted.
     * @return  Converted Date instance.
     */
    public static Date toDate(LocalDate localDate) {
        if (localDate == null)
            return null;

        Date date = Date.from(localDate.atStartOfDay().atZone(SystemTimeZoneId).toInstant());
        return date;
    }

    /**
     * Convert Date instance to LocalDate at the default System Time Zone.
     * @param date Date instance to be converted.
     * @return Converted LocalDate instance.
     */
    public static LocalDate fromDate(Date date){
        if(date == null)
            return null;

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        LocalDate localDate = LocalDate.of(year, month+1, day);
        return localDate;
    }

    /**
     * Get Date instance with given year, month and day.
     * @param year the value used to set the <code>YEAR</code> calendar field in the calendar.
     * @param month the value used to set the <code>MONTH</code> calendar field in the calendar.
     * Month value is 1-based. e.g., 1 for January.
     * @param dayOfMonth the value used to set the <code>DAY_OF_MONTH</code> calendar field in the calendar.
     * @return
     */
    public static Date dateOf(int year, int month, int dayOfMonth){
        Date date = new GregorianCalendar(year, month-1, dayOfMonth).getTime();
        return date;
    }
}
