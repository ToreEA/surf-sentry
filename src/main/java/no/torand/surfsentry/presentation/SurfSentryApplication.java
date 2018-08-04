package no.torand.surfsentry.presentation;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;

@ApplicationPath("/api")
public class SurfSentryApplication extends ResourceConfig {
    public SurfSentryApplication() {
        packages("no.torand.surfsentry.presentation");
        register(JacksonFeature.class);
    }
}