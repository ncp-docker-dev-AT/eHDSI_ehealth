package eu.europa.ec.sante.ehdsi.gazelle.validation.impl;

import org.springframework.util.Assert;
import org.springframework.ws.client.core.WebServiceTemplate;

abstract class AbstractValidator {

    final WebServiceTemplate webServiceTemplate;

    AbstractValidator(WebServiceTemplate webServiceTemplate) {
        Assert.notNull(webServiceTemplate, "webServiceTemplate must not be null");
        this.webServiceTemplate = webServiceTemplate;
    }
}
