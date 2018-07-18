package eu.europa.ec.sante.ehdsi.gazelle.validation.impl;

import org.springframework.util.Assert;
import org.springframework.ws.client.core.WebServiceTemplate;

abstract class AbstractValidator {

    final WebServiceTemplate webServiceTemplate;

    AbstractValidator(WebServiceTemplate webServiceTemplate) {
        Assert.notNull(webServiceTemplate, "webServiceTemplate must not be null");
        this.webServiceTemplate = webServiceTemplate;
    }

//    public DetailedResult unmarshal(String xmlDetails) {
//
//        DetailedResult result = null;
//
//        if (StringUtils.isBlank(xmlDetails)) {
//            //LOGGER.error("The provided XML String object to unmarshall is empty.");
//        } else {
//            InputStream is = new ByteArrayInputStream(xmlDetails.getBytes());
//
//            try {
//
//                JAXBContext jc = JAXBContext.newInstance(DetailedResult.class);
//                Unmarshaller unmarshaller = jc.createUnmarshaller();
//                result = (DetailedResult) unmarshaller.unmarshal(is);
//
//            } catch (JAXBException ex) {
//        //        LOGGER.error("JAXBException: '{}'", ex.getMessage(), ex);
//            }
//        }
//        return result;
//    }
//
//    private boolean isValidationEnable() {
//        return ConfigurationManagerFactory.getConfigurationManager().getBooleanProperty("automated.validation.new");
//    }
}
