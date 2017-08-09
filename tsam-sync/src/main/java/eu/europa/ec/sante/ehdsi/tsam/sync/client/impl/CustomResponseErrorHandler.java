package eu.europa.ec.sante.ehdsi.tsam.sync.client.impl;

import eu.europa.ec.sante.ehdsi.tsam.sync.client.TermServerClientException;
import org.apache.commons.io.IOUtils;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class CustomResponseErrorHandler implements ResponseErrorHandler {

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        return response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError();
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        throw new TermServerClientException(response.getStatusCode().value(), response.getStatusText(), IOUtils.toString(response.getBody(), StandardCharsets.UTF_8));
    }
}
