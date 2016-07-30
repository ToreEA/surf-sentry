package no.torand.surfsentry.domain;

public class AccessRule {
    public enum RuleType { ALLOW, DENY }

    public static final String WILDCARD = "*";

    private final RuleType type;
    private final String personName;
    private final String host;
    private final TimeInterval timeInterval;

    public AccessRule(RuleType type, String personName, String host, TimeInterval timeInterval) {
        this.type = type;
        this.personName = personName;
        this.host = host;
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

    public boolean matches(String remoteUri) {
      return timeInterval.containsNow() &&
              (remoteUri.contains(host) || WILDCARD.equals(host));
    }

    @Override
    public String toString() {
        return type.name() + " " + personName + " access to '" + host + "' at " + timeInterval;
    }
}
