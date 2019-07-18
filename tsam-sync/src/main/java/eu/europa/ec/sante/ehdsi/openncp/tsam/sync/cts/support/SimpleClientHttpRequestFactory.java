package eu.europa.ec.sante.ehdsi.openncp.tsam.sync.cts.support;

import org.apache.http.client.HttpClient;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import java.net.URI;

public class SimpleClientHttpRequestFactory extends HttpComponentsClientHttpRequestFactory {

    private final HttpContext httpContext;

    public SimpleClientHttpRequestFactory(HttpClient httpClient) {
        super(httpClient);
        httpContext = new BasicHttpContext();
        httpContext.setAttribute(HttpClientContext.COOKIE_STORE, new BasicCookieStore());
    }

    @Override
    protected HttpContext createHttpContext(HttpMethod httpMethod, URI uri) {
        return this.httpContext;
    }
}
