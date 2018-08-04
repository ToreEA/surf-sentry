package no.torand.surfsentry.presentation.representation;

public class PageVisitCountJson {
    private final String host;
    private final long count;
    private final String lastVisitTime;

    public PageVisitCountJson(String host, long count, String lastVisitTime) {

        this.host = host;
        this.count = count;
        this.lastVisitTime = lastVisitTime;
    }

    public String getHost() {
        return host;
    }

    public long getCount() {
        return count;
    }

    public String getLastVisitTime() {
        return lastVisitTime;
    }
}
