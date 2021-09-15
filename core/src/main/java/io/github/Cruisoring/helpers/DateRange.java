package io.github.Cruisoring.helpers;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Data structure to represent a inclusive period between fromDate and toDate.
 */
public class DateRange implements Comparable<DateRange>{
    public static final DateRange ALL = new DateRange(LocalDate.MIN, LocalDate.MAX);

    public final LocalDate fromDate;
    public final LocalDate toDate;

    /**
     * Constructor with fromDate and toDate specified.
     * @param fromDate  Inclusive start of the DateRange.
     * @param toDate    Inclusive end of the DateRange.
     */
    public DateRange(LocalDate fromDate, LocalDate toDate) {
        Objects.requireNonNull(fromDate);
        Objects.requireNonNull(toDate);
        if(fromDate.isAfter(toDate)){
            throw new IllegalArgumentException("fromDate cannot be after toDate");
        }

        this.fromDate = fromDate;
        this.toDate = toDate;
    }

    /**
     * Constructor with fromDate to specify a DateRange of a single day.
     * @param fromToDate  The single day to be represented.
     */
    public DateRange(LocalDate fromToDate) {
        this(fromToDate, fromToDate);
    }

    /**
     * Get how many days to run the calculation.
     * @return number of days.
     */
    public int durationInDays(){
        int daysBetween = (int) ChronoUnit.DAYS.between(fromDate, toDate);
        return daysBetween+1;
    }

    /**
     * Helper method to get all distinct dayes between the days of fromDate and toDate inclusively.
     * @return A Set of LocalDate of the natural order: fromDate first and toDate last.
     */
    public Set<LocalDate> getAllDates(){
        int daysBetween = durationInDays();
        Set<LocalDate> allDates = IntStream.rangeClosed(0, daysBetween)
                .mapToObj(i -> fromDate.plusDays(i))
                .collect(Collectors.toSet());
        return allDates;
    }

    /**
     * Helper method to evaluate if the concerned date is in the range of this DateRange instance.
     * @param date Date to be evaluated.
     * @return  <tt>true</tt> if it is in range, otherwise <tt>false</tt>.
     */
    public boolean isInRange(LocalDate date){
        Objects.requireNonNull(date);
        return !date.isBefore(fromDate) && !date.isAfter(toDate);
    }

    /**
     * Get the overlapped part with another DateRange.
     * @param another Another DateRange to be evaluated.
     * @return  <tt>null</tt> if there is no overlapping, otherwise a new DateRange of the shared dates.
     */
    public DateRange intersect(DateRange another){
        if(another == null || this.fromDate.compareTo(another.toDate) > 0 || this.toDate.compareTo(another.fromDate) < 0)
            return null;

        LocalDate maxFrom = this.fromDate.compareTo(another.fromDate) < 0 ? another.fromDate : this.fromDate;
        LocalDate minTo = this.toDate.compareTo(another.toDate) > 0 ? another.toDate : this.toDate;
        return new DateRange(maxFrom, minTo);
    }

    @Override
    public int hashCode() {
        return fromDate.hashCode()*17 + toDate.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) return false;
        if(obj == this) return true;
        if(!(obj instanceof DateRange))
            return false;

        DateRange other = (DateRange)obj;
        return this.fromDate.equals(other.fromDate) && this.toDate.equals(other.toDate);
    }

    @Override
    public String toString() {
        if(fromDate.equals(toDate))
            return "on " + DateTimeHelper.dateString(fromDate);
        else
            return String.format("from %s to %s", DateTimeHelper.dateString(fromDate), DateTimeHelper.dateString(toDate));
    }

    @Override
    public int compareTo(DateRange o) {
        int fromResult = this.fromDate.compareTo(o.fromDate);
        return fromResult != 0 ? fromResult : this.toDate.compareTo(o.toDate);
    }
}
