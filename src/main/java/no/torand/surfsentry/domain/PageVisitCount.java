package no.torand.surfsentry.domain;

import java.time.LocalDateTime;

public class PageVisitCount {
    private final String host;
    private long visitCount;
    private LocalDateTime lastVisit;

    public PageVisitCount(String host) {
        this.host = host;
        this.visitCount = 1;
        this.lastVisit = LocalDateTime.now();
    }

    public PageVisitCount increment() {
        visitCount++;
        lastVisit = LocalDateTime.now();
        return this;
    }

    public String getHost() {
        return host;
    }

    public long getCount() {
        return visitCount;
    }

    public LocalDateTime getLastVisit() {
        return lastVisit;
    }
}