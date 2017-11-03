package com.gnomon.epsos.filter;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.ParseException;

public class RestAuthenticationFilter implements javax.servlet.Filter {

    public static final String AUTHENTICATION_HEADER = "Authorization";
    private static final Logger LOGGER = LoggerFactory.getLogger(RestAuthenticationFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filter)
            throws IOException, ServletException {

        LOGGER.info("Rest Authentication Filter ...");
        String url = "";
        HttpServletRequest httpServletRequest = null;
        if (request instanceof HttpServletRequest) {
            url = ((HttpServletRequest) request).getRequestURL().toString();
            httpServletRequest = (HttpServletRequest) request;
        }

        if (StringUtils.equalsIgnoreCase(httpServletRequest.getMethod(), "OPTIONS")) {
            LOGGER.info("OPTIONS METHOD ACCEPTED");
            if (response instanceof HttpServletResponse) {
                HttpServletResponse httpServletResponse = (HttpServletResponse) response;
                httpServletResponse
                        .setStatus(HttpServletResponse.SC_ACCEPTED);
            }
            return;
        }

        LOGGER.info("go into authentication filter: '{}'", url);
        if (request instanceof HttpServletRequest) {
            httpServletRequest = (HttpServletRequest) request;

            String authCredentials = httpServletRequest.getHeader(AUTHENTICATION_HEADER);

            LOGGER.info("{}: {}", AUTHENTICATION_HEADER, authCredentials);
            AuthenticationService authenticationService = new AuthenticationService();

            boolean authenticationStatus = false;
            try {
                authenticationStatus = authenticationService
                        .authenticate(authCredentials);
                LOGGER.info("Authentication status: '{}'", authenticationStatus);
            } catch (ParseException ex) {
                LOGGER.error(null, ex);
            }
            if (authenticationStatus) {
                filter.doFilter(request, response);
            } else {
                if (StringUtils.equalsIgnoreCase(httpServletRequest.getMethod(), "OPTIONS")) {
                    if (response instanceof HttpServletResponse) {
                        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
                        httpServletResponse.setStatus(HttpServletResponse.SC_ACCEPTED);
                    }
                } else {
                    if (response instanceof HttpServletResponse) {
                        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
                        httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    }
                }
            }
        }
    }

    @Override
    public void destroy() {
        // Implementation not required.
    }

    @Override
    public void init(FilterConfig arg0) throws ServletException {
        // Implementation not required.
    }
}
