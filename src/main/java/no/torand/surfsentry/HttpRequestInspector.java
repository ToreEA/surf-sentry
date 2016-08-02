package no.torand.surfsentry;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import no.torand.surfsentry.domain.Device;
import no.torand.surfsentry.service.StatsCollector;
import no.torand.surfsentry.service.WebAccessManager;
import org.littleshoot.proxy.HttpFilters;
import org.littleshoot.proxy.HttpFiltersAdapter;
import org.littleshoot.proxy.HttpFiltersSourceAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import static io.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;


public class HttpRequestInspector extends HttpFiltersSourceAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpRequestInspector.class);

    private WebAccessManager webAccessManager;
    private StatsCollector statsCollector;

    public HttpRequestInspector(WebAccessManager webAccessManager, StatsCollector statsCollector) {
        this.webAccessManager = webAccessManager;
        this.statsCollector = statsCollector;
    }

    @Override
    public HttpFilters filterRequest(HttpRequest originalRequest, ChannelHandlerContext ctx) {
        String clientAddress = getClientAddress(ctx.channel());
        Device clientDevice = getClientDevice(clientAddress);
        String clientName = String.format("%-30s", clientDevice.getDisplayName());

        String method = originalRequest.getMethod().name();
        String requestUri = originalRequest.getUri();

        return new HttpFiltersAdapter(originalRequest) {
            @Override
            public HttpResponse clientToProxyRequest(HttpObject httpObject) {
                try {
                    if (webAccessManager.isAccessAllowed(clientAddress, requestUri)) {
                        statsCollector.onRequest(clientDevice, requestUri);
                        LOGGER.info("{} > {} {}", clientName, method, requestUri);
                        return null;
                    } else {
                        LOGGER.info("{} > {} {} - BLOCKED!", clientName, method, requestUri);
                        return new DefaultHttpResponse(HTTP_1_1, FORBIDDEN);
                    }
                } catch (RuntimeException e) {
                    LOGGER.error("Exception caught in clientToProxyRequest: {}", e, e);
                    return new DefaultHttpResponse(HTTP_1_1, INTERNAL_SERVER_ERROR);
                }
            }
        };
    }

    private String getClientAddress(Channel channel) {
        SocketAddress socketAddress = channel.remoteAddress();
        String address = socketAddress.toString();

        /*try {
            InetAddress inetAddress = InetAddress.getByName(address);
            address = inetAddress.getHostAddress();
        } catch (UnknownHostException e) {

        }*/

        if (address.startsWith("/")) {
            address = address.substring(1);
        }
        if (address.contains(":")) {
            address = address.substring(0, address.indexOf(":"));
        }

        return address;
    }

    private String getClientHostName(String remoteAddress) {

        String computerName = null;

        try {
            InetAddress inetAddress = InetAddress.getByName(remoteAddress);
            computerName = inetAddress.getHostName();

            if (computerName.equalsIgnoreCase("localhost")) {
                computerName = InetAddress.getLocalHost().getCanonicalHostName();
            }
        } catch (UnknownHostException e) {

        }

        return computerName;
    }

    private Device getClientDevice(String remoteAddress) {
        return webAccessManager.getDevice(remoteAddress).orElse(Device.unknown(remoteAddress));
    }
}