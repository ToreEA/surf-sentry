package no.torand.surfsentry.domain;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.EnumSet;

public class AccessRule {
    public enum RuleType { ALLOW, DENY }

    public static final String WILDCARD = "*";

    private final RuleType type;
    private final String personName;
    private final String host;
    private final EnumSet<DayOfWeek> daysOfWeek;
    private final TimeInterval timeInterval;

    public AccessRule(RuleType type, String personName, String host, EnumSet<DayOfWeek> daysOfWeek, TimeInterval timeInterval) {
        this.type = type;
        this.personName = personName;
        this.host = host;
        this.daysOfWeek = daysOfWeek;
        this.timeInterval = timeInterval;
    }

    public String getPersonName() {
        return personName;
    }

    public String getHost() {
        return host;
    }

    public TimeInterval getTimeInterval() {
        return timeInterval;
    }

    public EnumSet<DayOfWeek> getDaysOfWeek() {
        return daysOfWeek;
    }

    public boolean matches(String remoteUri) {
        DayOfWeek today = DayOfWeek.from(LocalDate.now());
        return daysOfWeek.contains(today) && timeInterval.containsNow() &&
              (remoteUri.contains(host) || WILDCARD.equals(host));
    }

    @Override
    public String toString() {
        return type.name() + " " + personName + " access to '" + host + "' at " + timeInterval + " on " + daysOfWeek;
    }
}
