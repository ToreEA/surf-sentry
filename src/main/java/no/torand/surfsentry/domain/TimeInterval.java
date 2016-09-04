package no.torand.surfsentry.domain;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class TimeInterval {
    private final LocalTime from;
    private final LocalTime to;

    public static TimeInterval valueOf(String value) {
        if (value.length() != 5 && value.indexOf("-") != 2) {
            throw new IllegalArgumentException("Not recognizable as a time interval string on the form \"hh-hh\": " + value);
        }
        String hourFrom = value.substring(0, 2);
        String hourTo = value.substring(3, 5);

        return new TimeInterval(Integer.valueOf(hourFrom), Integer.valueOf(hourTo));
    }

    public TimeInterval(int hoursFrom, int hoursTo) {
        this.from = LocalTime.of(hoursFrom, 0, 0);
        this.to = LocalTime.of(hoursTo, 59, 59);
    }

    public boolean containsNow() {
        LocalTime now = LocalTime.now();
        return from.isBefore(now) && to.isAfter(now);
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_TIME;
        return from.format(formatter) + "-" + to.format(formatter);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TimeInterval)) return false;

        TimeInterval that = (TimeInterval) o;

        if (!from.equals(that.from)) return false;
        return to.equals(that.to);

    }

    @Override
    public int hashCode() {
        int result = from.hashCode();
        result = 31 * result + to.hashCode();
        return result;
    }
}
