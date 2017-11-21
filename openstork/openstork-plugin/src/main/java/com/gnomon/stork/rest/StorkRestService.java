package com.gnomon.stork.rest;

import com.google.gson.Gson;
import com.liferay.portal.datamodel.StorkResponse;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.security.auth.Constants;
import com.liferay.portal.security.auth.StorkAutoLogin;
import com.liferay.portal.security.auth.StorkHelper;
import com.liferay.portal.security.auth.StorkProperties;
import com.liferay.portal.stork.util.PatientSearchAttributes;
import eu.europa.ec.joinup.ecc.openstork.utils.StorkUtils;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManagerFactory;
import eu.stork.peps.auth.commons.IPersonalAttributeList;
import eu.stork.peps.auth.commons.PEPSUtil;
import eu.stork.peps.auth.commons.STORKAuthnResponse;
import eu.stork.peps.auth.engine.STORKSAMLEngine;
import eu.stork.peps.exceptions.STORKSAMLEngineException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author karkaletsis
 */
@Path("/stork")
public class StorkRestService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StorkRestService.class);

    @Context
    private HttpServletRequest servletRequest;

    @GET
    @Path("/peps/url/get")
    public Response getPepsURL() throws SystemException {

        String pepsurl = StorkHelper.getPepsURL();
        LOGGER.info("pepsurl: '{}'", pepsurl);
        return Response.status(200).entity(pepsurl).build();
    }

    @GET
    @Path("/peps/country/get")
    public Response getPepsCountry() throws SystemException {

        String pepscountry = StorkHelper.getPepsCountry();
        LOGGER.info("pepscountry: '{}'", pepscountry);
        return Response.status(200).entity(pepscountry).build();
    }

    @POST
    @Path("/peps/saml/decode")
    public Response decodeSaml(@FormParam("SAMLResponse") String SAMLResponse) throws UnsupportedEncodingException {

        String ret = "";
        LOGGER.info("DECODE SAML: Getting saml attributes");
        LOGGER.info("SAML RESPONSE IS: '{}'", SAMLResponse);
        Map<String, String> ehpattributes = new HashMap<>();

        byte[] decSamlToken = PEPSUtil.decodeSAMLToken(SAMLResponse);
        String samlResponseXML = new String(decSamlToken);
        LOGGER.info("SAML RESPONSE IS: '{}'", samlResponseXML);
        String host = servletRequest.getRemoteHost();
        LOGGER.info("HOST: '{}'", host);

        STORKAuthnResponse authnResponse = null;
        IPersonalAttributeList attrs = null;
        STORKSAMLEngine engine = STORKSAMLEngine.getInstance(Constants.SP_CONF);
        try {
            authnResponse = engine.validateSTORKAuthnResponseWithQuery(decSamlToken, host);
        } catch (STORKSAMLEngineException e) {
            LOGGER.error("Exception: '{}'", e.getMessage(), e);
        }
        StorkResponse resp = new StorkResponse();
        if (Validator.isNotNull(authnResponse)) {
            StorkUtils.convertOnBehalfStorktoHcpAssertion(authnResponse);
            Map<String, String> onbehalfattrs = StorkUtils.getRepresentedPersonInformation(authnResponse);
            Map<String, String> onbehalfdemattrs = StorkUtils.getRepresentedDemographics(authnResponse);

            resp.setRepresentedPersonalData(onbehalfattrs);
            resp.setRepresentedDemographicsData(onbehalfdemattrs);
            try {
                assert onbehalfattrs != null;
                LOGGER.info("###############: '{}'", onbehalfattrs.size());
            } catch (Exception e) {
                LOGGER.error("Error getting onbehalf attributes: '{}'", e.getMessage(), e);
            }
            assert authnResponse != null;
            if (authnResponse.isFail()) {
                LOGGER.error("Problem with response");
            } else {
                attrs = authnResponse.getTotalPersonalAttributeList();
                if (attrs.isEmpty()) {
                    attrs = authnResponse.getPersonalAttributeList();
                }
            }
        }

        StorkAutoLogin sal = new StorkAutoLogin();
        String hcpInfo = sal.getSamlValue(attrs, "isHealthCareProfessional");
        String hcpRole = sal.getHCPRoleString(hcpInfo);
        String epsosRole = sal.getEpsosRole(hcpRole);
        resp.setGivenName(sal.getSamlValue(attrs, "givenName"));
        resp.setEmail(sal.getSamlValue(attrs, "eMail"));
        resp.seteIdentifier(sal.getSamlValue(attrs, "eIdentifier"));
        resp.setSurname(sal.getSamlValue(attrs, "surname"));
        resp.setHcpRole(hcpRole);
        resp.setHcpInfo(hcpInfo);
        resp.setEpsosRole(epsosRole);
        resp.setAttrs(attrs);

        String countryCode = ConfigurationManagerFactory.getConfigurationManager().getProperty("COUNTRY_CODE");
        LOGGER.info("The country code is: '{}'", countryCode);
        LOGGER.info("Reading the required attributes from International Search Mask");
        Map<String, String> attributes = PatientSearchAttributes.getRequiredAttributesByCountry(countryCode);
        assert attributes != null;
        for (Object o : attributes.entrySet()) {
            Map.Entry pairs = (Map.Entry) o;
            LOGGER.info("$$$$ " + pairs.getKey() + " = " + pairs.getValue());
            try {
                String attrName = pairs.getValue().toString(); //"2.16.470.1.100.1.1.1000.990.1";
                String storkKey = pairs.getKey().toString();
                String value = sal.getSamlValue(storkKey);

                if (StringUtils.equalsIgnoreCase(storkKey, "eIdentifier")) {
                    String[] temp = value.split("/");
                    value = temp[2];
                } else {
                    value = sal.getSamlValue(storkKey);
                }
                LOGGER.info("Adding attribute: '{}' with value: '{}'", attrName, value);
                ehpattributes.put(attrName, value);
                resp.setAttributes(ehpattributes);

            } catch (Exception e) {
                LOGGER.error("Exception: '{}'", e.getMessage(), e);
            }
            ret = new Gson().toJson(resp);
            LOGGER.info(ret);
        }
        return Response.status(200).entity(ret).build();
    }

    @POST
    @Path("/peps/saml/create")
    public Response createSaml(@FormParam("providerName") String providerName, @FormParam("spSector") String spSector,
                               @FormParam("spApplication") String spApplication, @FormParam("spCountry") String spCountry,
                               @FormParam("spQaLevel") String spQaLevel, @FormParam("pepsURL") String pepsURL,
                               @FormParam("loginURL") String loginURL, @FormParam("spReturnURL") String spReturnURL,
                               @FormParam("spPersonalAttributes") String spPersonalAttributes,
                               @FormParam("spBusinessAttributes") String spBusinessAttributes,
                               @FormParam("spLegalAttributes") String spLegalAttributes) throws SystemException {

        StorkProperties properties = new StorkProperties();
        properties.setLoginURL(loginURL);
        properties.setPepsURL(pepsURL);
        properties.setProviderName(providerName);
        properties.setSpApplication(spApplication);
        properties.setSpPersonalAttributes(spPersonalAttributes);
        properties.setSpBusinessAttributes(spBusinessAttributes);
        properties.setSpCountry(spCountry);
        properties.setSpLegalAttributes(spLegalAttributes);
        properties.setSpQaLevel(spQaLevel);
        properties.setSpReturnURL(spReturnURL);
        properties.setSpSector(spSector);

        String saml = StorkHelper.createStorkSAML(properties);
        LOGGER.info("saml : '{}'", saml);
        return Response.status(200).entity(saml).build();
    }
}
