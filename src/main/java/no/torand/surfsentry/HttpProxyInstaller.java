package no.torand.surfsentry;

import no.torand.surfsentry.service.StatsCollector;
import no.torand.surfsentry.service.WebAccessManager;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.*;
import javax.servlet.annotation.WebListener;

@WebListener
public class HttpProxyInstaller implements ServletContextListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpProxyInstaller.class);

    private HttpProxyServer proxy;

    private @Inject WebAccessManager webAccessManager;
    private @Inject StatsCollector statsCollector;

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        try {
            proxy = DefaultHttpProxyServer.bootstrap()
                    .withPort(8888)
                    .withAllowLocalOnly(false)
                    .withFiltersSource(new HttpRequestInspector(webAccessManager, statsCollector))
                    .start();
        } catch (RuntimeException e) {
            LOGGER.error("Failed to start proxy: {}", e.getMessage(), e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        proxy.stop();
    }
}
