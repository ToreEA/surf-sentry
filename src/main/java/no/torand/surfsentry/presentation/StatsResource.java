package no.torand.surfsentry.presentation;

import no.torand.surfsentry.presentation.representation.DeviceUsageJson;
import no.torand.surfsentry.presentation.representation.PageVisitCountJson;
import no.torand.surfsentry.service.StatsCollector;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static java.util.stream.Collectors.toList;

@ApplicationScoped
@Path("stats")
public class StatsResource {
    private static DateTimeFormatter FULL_DATE_AND_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private @Inject StatsCollector statsCollector;

    @GET
    @Path("startup")
    @Produces(MediaType.APPLICATION_JSON)
    public String getStartupTime() {
        return statsCollector.getStartupTime().format(FULL_DATE_AND_TIME);
    }

    @GET
    @Path("device-usage")
    @Produces(MediaType.APPLICATION_JSON)
    public List<DeviceUsageJson> getDeviceUsage() {
        return statsCollector.getLastRequestForDevices().entrySet().stream()
                .map(e -> new DeviceUsageJson(e.getKey(), e.getValue().format(FULL_DATE_AND_TIME)))
                .collect(toList());
    }

    @GET
    @Path("page-visit-count")
    @Produces(MediaType.APPLICATION_JSON)
    public List<PageVisitCountJson> getPageVisitCount() {
        return statsCollector.getPageVisits().stream()
                .map(pv -> new PageVisitCountJson(pv.getHost(), pv.getCount(), pv.getLastVisit().format(FULL_DATE_AND_TIME)))
                .collect(toList());
    }
}