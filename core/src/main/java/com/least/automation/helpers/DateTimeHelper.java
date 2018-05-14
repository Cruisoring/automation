package com.least.automation.helpers;

import java.time.*;
import java.util.TimeZone;

public class DateTimeHelper {

    public static LocalDateTime fromTimestamp(long timestamp) {
        if (timestamp == 0) return null;
        return  LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp),
                TimeZone.getDefault().toZoneId());
    }

    public static LocalDate getDateFromTimestamp(long timestamp) {
        LocalDateTime date = fromTimestamp(timestamp);
        return date == null ? null : date.toLocalDate();
    }

    public static long getEpochMills(LocalDateTime time){
        ZonedDateTime zdt = time.atZone(ZoneId.systemDefault());
        return zdt.toEpochSecond();
    }
}
