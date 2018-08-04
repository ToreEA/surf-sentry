package no.torand.surfsentry.service;

import no.torand.surfsentry.domain.Device;
import no.torand.surfsentry.domain.PageVisitCount;

import javax.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;
import java.util.*;

import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;

@ApplicationScoped
public class StatsCollector {
    private LocalDateTime startupTime = LocalDateTime.now();
    private Map<String,PageVisitCount> visitsByHost = new HashMap<>();
    private Map<String, LocalDateTime> lastRequestByDevice = new HashMap<>();

    public LocalDateTime getStartupTime() {
        return startupTime;
    }

    public synchronized void onRequest(Device clientDevice, String remoteUri) {
        String host = getHost(remoteUri);
        visitsByHost.compute(host, (h, c) -> isNull(c) ? new PageVisitCount(h) : c.increment());

        lastRequestByDevice.put(clientDevice.getDisplayName(), LocalDateTime.now());
    }

    public synchronized Collection<PageVisitCount> getPageVisits() {
        return visitsByHost.values().stream()
                .sorted(Comparator.comparingLong(PageVisitCount::getCount).reversed())
                .collect(toList());
    }

    public synchronized Map<String, LocalDateTime> getLastRequestForDevices() {
        return Collections.unmodifiableMap(lastRequestByDevice);
    }

    private String getHost(String uri) {
        if (uri.contains("://")) {
            uri = uri.substring(uri.indexOf("://") + 3);
        }

        if (uri.contains(":")) {
            uri = uri.substring(0, uri.indexOf(":"));
        }

        if (uri.contains("/")) {
            uri = uri.substring(0, uri.indexOf("/"));
        }

        return uri;
    }
}
