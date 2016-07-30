package no.torand.surfsentry.presentation;

import no.torand.surfsentry.service.StatsCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;

@WebServlet("/surfsentry")
public class SurfSentryServlet extends HttpServlet {
    private static final Logger LOGGER = LoggerFactory.getLogger(SurfSentryServlet.class);

    private ServletContext servletContext;
    private @Inject StatsCollector statsCollector;

    @Override
    public String getServletInfo() {
        return "Surf Sentry";
    }

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        servletContext = servletConfig.getServletContext();
    }

    @Override
    public void destroy() {
        super.destroy();
    }

    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
            throws ServletException, IOException
    {
        response.setContentType("text/html");

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        PrintWriter out = response.getWriter();
        out.println("<html><body>");
        out.println("<style>table, th, td { border: 1px solid black; border-collapse: collapse; padding: 3px;}</style>");
        out.println("<h1>Surf Sentry Statistics</h1>");

        out.println("<h2>Page Visit Counts</h2>");
        out.println("<table><tr><th style='text-align:left'>Host</th><th style='text-align:right'>Visits</th><th style='text-align:left'>Last visit</th></tr>");
        statsCollector.getPageVisits()
                .forEach(pvc -> out.println("<tr><td>" + pvc.getHost() + "</td><td style='text-align:right'>" + pvc.getCount() + "</td><td>" + pvc.getLastVisit().format(dateTimeFormatter) + "</td></tr>"));
        out.println("</table>");

        out.println("<h2>Devices in Use</h2>");
        out.println("<table><tr><th style='text-align:left'>Device</th><th style='text-align:left'>Last request</th></tr>");
        statsCollector.getLastRequestForDevices()
                .forEach((device, time) -> out.println("<tr><td style='text-align:left'>" + device + "</td><td>" + time.format(dateTimeFormatter) + "</td></tr>"));
        out.println("</table>");

        out.println("</body></html>");
    }}