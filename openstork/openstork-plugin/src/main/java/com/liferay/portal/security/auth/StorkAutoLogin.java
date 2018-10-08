package com.liferay.portal.security.auth;

import com.liferay.portal.NoSuchUserException;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.model.Role;
import com.liferay.portal.model.User;
import com.liferay.portal.service.RoleLocalServiceUtil;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.stork.util.PatientSearchAttributes;
import com.liferay.portal.stork.util.Util;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portlet.expando.model.ExpandoTable;
import com.liferay.portlet.expando.model.ExpandoTableConstants;
import com.liferay.portlet.expando.service.ExpandoTableLocalServiceUtil;
import com.liferay.util.PwdGenerator;
import eu.europa.ec.joinup.ecc.openstork.utils.StorkUtils;
import eu.europa.ec.joinup.ecc.openstork.utils.datamodel.HcpRole;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManagerFactory;
import eu.stork.peps.auth.commons.IPersonalAttributeList;
import eu.stork.peps.auth.commons.PEPSUtil;
import eu.stork.peps.auth.commons.PersonalAttribute;
import eu.stork.peps.auth.commons.STORKAuthnResponse;
import eu.stork.peps.auth.engine.STORKSAMLEngine;
import org.apache.commons.lang.StringUtils;
import org.opensaml.saml.saml2.core.Assertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * Performs autologin based on the assertion provided by Stork Application.
 *
 * @author Kostas Karkaletsis <k.karkaletsis@gnomon.com.gr>
 */
public class StorkAutoLogin implements AutoLogin {

    private static final Logger LOGGER = LoggerFactory.getLogger(StorkHelper.class);
    private static Properties configs;
    private static String providerName;
    private static String homepage = "/SP/";
    private static IPersonalAttributeList attrs;
    private final String USER_AGENT = "Mozilla/5.0";
    private String SAMLResponse;
    private String samlResponseXML;
    private ArrayList<PersonalAttribute> attrList;
    private String eIdentifier;
    private String givenName;
    private String surname;
    private String emailAddress;
    private String HCPInfo;
    private HcpRole hcpRole;

    private void getSAMLAttributes(HttpServletRequest request) {

        LOGGER.info("Getting saml attributes");
        providerName = PropsUtil.get("provider.name");
        SAMLResponse = request.getParameter("SAMLResponse");

        LOGGER.info("SAML RESPONSE IS: '{}'", SAMLResponse);
        byte[] decSamlToken = PEPSUtil.decodeSAMLToken(SAMLResponse);
        samlResponseXML = new String(decSamlToken);
        LOGGER.info("SAML RESPONSE XML IS: '{}'", samlResponseXML);
        request.setAttribute("USER_samlResponseXML", samlResponseXML);

        STORKAuthnResponse authnResponse = null;
        attrs = null;
        STORKSAMLEngine engine = STORKSAMLEngine.getInstance(Constants.SP_CONF);
        String host = request.getRemoteHost();
        LOGGER.info("HOST IS : '{}'", host);
        request.setAttribute("USER_test", "kostas");
        try {
            authnResponse = engine.validateSTORKAuthnResponseWithQuery(decSamlToken, host);
            Assertion onBehalf = StorkUtils.convertOnBehalfStorktoHcpAssertion(authnResponse);
            Map<String, String> onbehalfattrs = StorkUtils.getRepresentedPersonInformation(authnResponse);
            if (onbehalfattrs != null) {
                LOGGER.info("###############: '{}' - '{}'", onbehalfattrs.size(), onbehalfattrs.get("givenName"));
            }

            request.setAttribute("USER_onbehalfassertion", onBehalf);
            request.setAttribute("USER_onbehalfattributes", onbehalfattrs);

        } catch (Exception e) {
            LOGGER.info("Exception: '{}'", e.getMessage(), e);
        }
        if (authnResponse != null) {
            if (authnResponse.isFail()) {
                LOGGER.info("Problem with response");
            } else {
                attrs = authnResponse.getTotalPersonalAttributeList();
                if (attrs.isEmpty()) {
                    attrs = authnResponse.getPersonalAttributeList();
                }
            }
        }
    }

    public String[] login(HttpServletRequest req, HttpServletResponse res) throws AutoLoginException {

        LOGGER.info("############### STORK AUTO LOGIN ###############");
        getSAMLAttributes(req);
        LOGGER.info("#### USER IS " + getSurname() + " " + getEmailAddress());
        User user;
        String[] credentials;

        long companyId = PortalUtil.getCompanyId(req);

        try {
            String hcpInfo = getHCPInfo();
            String hcpRole = getHCPRoleString(hcpInfo);
            String epsosRole = getEpsosRole(hcpRole);
            LOGGER.info("HCP INFO: '{}'", hcpInfo);
            LOGGER.info("STORK ROLE: '{}'", hcpRole);
            LOGGER.info("EPSOS ROLE IS: '{}'", epsosRole);

            LOGGER.info("Stork Autologin [modified 1]");

            if (!Util.isEnabled(companyId)) {
                return new String[]{};
            }

            user = loginFromSession(companyId, req);
            if (user == null) {
                return new String[]{};
            }

            credentials = new String[3];
            credentials[0] = String.valueOf(user.getUserId());
            credentials[1] = user.getPassword();
            credentials[2] = Boolean.TRUE.toString();
            return credentials;

        } catch (NoSuchUserException e) {
            logError(e);
        } catch (Exception e) {
            logError(e);
            throw new AutoLoginException(e);
        }
        return new String[]{};
    }

    private User loginFromSession(long companyId, HttpServletRequest request) throws Exception {

        String login;
        String emailAddress;
        User user;
        LOGGER.info("IN login from STORK Saml Assertion");

        login = (getSurname() + getGivenName()).toLowerCase();
        emailAddress = getEmailAddress();
        if (Validator.isNull(login)) {
            return null;
        }
        if (login.contains("@")) {
            login = login.substring(0, login.indexOf('@'));
        }

        Util.getAuthType(companyId);

        try {
            user = UserLocalServiceUtil.getUserByEmailAddress(companyId, emailAddress);
            LOGGER.info("User found: " + user.getScreenName() + " (" + user.getEmailAddress() + ")");
            if (Util.autoUpdateUser(companyId)) {
                LOGGER.error("Auto-updating user...");
            }
        } catch (NoSuchUserException e) {
            LOGGER.error("NoSuchUserException: '{}'", e.getMessage(), e);
            user = createUserFromSession(companyId);
            LOGGER.error("Created user with ID: ", user != null ? user.getUserId() : "N/A (User null)");
        }

        return user;
    }

    private User createUserFromSession(long companyId) throws Exception {

        User user;

        String screenName = (getSurname() + getGivenName()).toLowerCase();

        if (Validator.isNull(screenName)) {
            LOGGER.info("Cannot create user - missing screen name");
            return null;
        }

        /*
         You have to configure LP to not require email address for user
         users.email.address.required=false
         */
        String emailAddress = getEmailAddress();

        String firstname = getGivenName();
        if (Validator.isNull(firstname)) {
            LOGGER.info("Cannot create user - missing firstname");
            return null;
        }

        String surname = getSurname();

        if (Validator.isNull(surname)) {
            LOGGER.info("Cannot create user - missing surname");
            return null;
        }

        LOGGER.info("Creating user: screen name = [{}], emailAddress = [{}], first name = [{}], surname = [{}]",
                screenName, emailAddress, firstname, surname);

        user = addUser(companyId, screenName, emailAddress, firstname, surname);
        return user;
    }

    private User addUser(long companyId, String screenName, String emailAddress, String firstName, String lastName)
            throws Exception {

        long creatorUserId = 0;
        boolean autoPassword = true;
        String password1 = PwdGenerator.getPassword();
        String password2 = password1;
        boolean autoScreenName = true;
        long facebookId = 0;
        String openId = StringPool.BLANK;
        Locale locale = Locale.US;
        String middleName = StringPool.BLANK;
        int prefixId = 0;
        int suffixId = 0;
        boolean male = true;
        int birthdayMonth = Calendar.JANUARY;
        int birthdayDay = 1;
        int birthdayYear = 1970;
        String jobTitle = StringPool.BLANK;
        long[] groupIds = null;
        long[] organizationIds = null;
        long[] roleIds = null;
        long[] userGroupIds = null;
        boolean sendEmail = false;
        ServiceContext serviceContext = new ServiceContext();

        User user = UserLocalServiceUtil.addUser(creatorUserId, companyId, autoPassword, password1, password2,
                autoScreenName, screenName, emailAddress, facebookId, openId, locale, firstName, middleName, lastName,
                prefixId, suffixId, male, birthdayMonth, birthdayDay, birthdayYear, jobTitle, groupIds, organizationIds,
                roleIds, userGroupIds, sendEmail, serviceContext);

        String hcpInfo = getHCPInfo();
        String hcpRole = getHCPRoleString(hcpInfo);
        String epsosRole = getEpsosRole(hcpRole);
        LOGGER.info("HCP INFO: '{}'", hcpInfo);
        LOGGER.info("STORK ROLE: '{}'", hcpRole);
        LOGGER.info("EPSOS ROLE IS: '{}'", epsosRole);

        // Add epsos role
        Role role = null;
        try {
            role = RoleLocalServiceUtil.getRole(companyId, epsosRole);
            LOGGER.info("LIFERAY ROLE FOR '{}' IS '{}'", epsosRole, role.getName());
            UserLocalServiceUtil.addRoleUser(role.getRoleId(), user.getUserId());
        } catch (Exception e) {
            LOGGER.error("Problem adding " + epsosRole + " role to the user");
            LOGGER.error("Exception: '{}'", e.getMessage(), e);
        }

        // Add default patient role
        try {
            role = RoleLocalServiceUtil.getRole(companyId, "patient");
            UserLocalServiceUtil.addRoleUser(role.getRoleId(), user.getUserId());
        } catch (Exception e) {
            LOGGER.error("Problem adding patient role to the user");
            LOGGER.error("Exception: '{}'", e.getMessage(), e);
        }

        String countryCode = ConfigurationManagerFactory.getConfigurationManager().getProperty("COUNTRY_CODE");
        LOGGER.info("The country code is: '{}'", countryCode);
        LOGGER.info("Reading the required attributes from International Search Mask");
        Map<String, String> attributes = PatientSearchAttributes.getRequiredAttributesByCountry(countryCode);
        for (Object o : attributes.entrySet()) {
            Map.Entry pairs = (Map.Entry) o;
            LOGGER.info("$$$$ " + pairs.getKey() + " = " + pairs.getValue());
            try {
                //OID: 2.16.470.1.100.1.1.1000.990.1
                String attrName = pairs.getValue().toString();
                String storkKey = pairs.getKey().toString();
                String value = getSamlValue(storkKey);
                if (StringUtils.equalsIgnoreCase(storkKey, "eIdentifier")) {
                    String[] temp = value.split("/");
                    value = temp[2];
                } else {
                    value = getSamlValue(storkKey);
                }
                LOGGER.info("Adding attribute: '{}' with value: '{}'", attrName, value);
                ExpandoTable table;
                table = ExpandoTableLocalServiceUtil.getTable(companyId, User.class.getName(),
                        ExpandoTableConstants.DEFAULT_TABLE_NAME);
                StorkHelper.addExpandoColumn(companyId, user, role.getRoleId(), attrName);
                StorkHelper.updateColumnValue(table.getTableId(), attrName, value, companyId, user.getUserId());
            } catch (Exception e) {
                LOGGER.error("Exception: '{}'", e.getMessage(), e);
            }
        }

        LOGGER.info("After UserLocalServiceUtil.addUser: '{}', '{}', '{}', '{}'", screenName, emailAddress, firstName, lastName);
        UserLocalServiceUtil.updatePasswordReset(user.getUserId(), false);
        UserLocalServiceUtil.updateReminderQuery(user.getUserId(), password1, password2);

        return user;
    }

    private void updateUserFromSession(long companyId, User user, HttpServletRequest request) throws Exception {

        boolean modified = false;

        String emailAddress = (String) request.getAttribute(Util.getEmailHeaderName(companyId));
        LOGGER.info("updateUserFromSession: User [{}]: update email address [{}] --> [{}]", user.getScreenName(),
                user.getEmailAddress(), emailAddress);

        if (!Validator.isNull(emailAddress) && !user.getEmailAddress().equals(emailAddress)) {
            LOGGER.info("User [{}]: update email address [{}] --> [{}]", user.getScreenName(), user.getEmailAddress(),
                    emailAddress);
            user.setEmailAddress(emailAddress);
            modified = true;
        }

        String firstname = getGivenName();
        if (firstname.contains(";")) {
            firstname = firstname.substring(0, firstname.indexOf(';'));
        }
        if (!Validator.isNull(firstname) && !user.getFirstName().equals(firstname)) {
            user.setFirstName(firstname);
            modified = true;
        }

        String surname = getSurname();
        if (surname.contains(";")) {
            surname = surname.substring(0, surname.indexOf(';'));
        }
        if (!Validator.isNull(surname) && !user.getLastName().equals(surname)) {
            LOGGER.info("User [{}]: update last name [{}] --> [{}]", user.getScreenName(), user.getLastName(), surname);
            user.setLastName(surname);
            modified = true;
        }
        LOGGER.info("User has been modified: '{}'", modified);
        UserLocalServiceUtil.updateUser(user);
    }

    private void logError(Exception e) {
        LOGGER.error("Exception message: '{} - Cause: '{}'", e.getMessage(), e.getCause(), e);
    }

    private String getIsoEncoding(String input) throws UnsupportedEncodingException {

        byte[] utf = input.getBytes();
        byte[] b = new String(utf, "UTF-8").getBytes("ISO-8859-1");
        return new String(b);
    }

    public String[] handleException(HttpServletRequest request, HttpServletResponse response, Exception e)
            throws AutoLoginException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getSamlValue_(String key) {

        String ret = "";
        try {
            ret = attrs.get(key).getValue().get(0);
        } catch (Exception e) {
            LOGGER.error("Error with value for key: '{}' - '{}'", key, e.getMessage(), e);
        }
        return ret;
    }

    public String getSamlValue(IPersonalAttributeList attrs, String key) {

        String ret = "";
        try {
            ret = attrs.get(key).getValue().get(0);
        } catch (Exception e) {
            LOGGER.error("Error with value for key: '{}' - '{}'", key, e.getMessage(), e);
        }
        return ret;
    }

    public String getSamlValue(String key) {

        String ret = "";
        try {
            ret = attrs.get(key).getValue().get(0);
        } catch (Exception e) {
            LOGGER.error("Error with value for key: '{}' - '{}'", key, e.getMessage(), e);
        }
        return ret;
    }

    public String geteIdentifier() {
        return getSamlValue("eIdentifier");
    }

    public void seteIdentifier(String eIdentifier) {
        this.eIdentifier = eIdentifier;
    }

    public String getGivenName() {
        return getSamlValue("givenName");
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getSurname() {
        return getSamlValue("surname");
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getEmailAddress() {
        return getSamlValue("eMail");
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getHCPInfo() {
        return getSamlValue("isHealthCareProfessional");
    }

    public String getEpsosRole(String HCPRole) {

        LOGGER.info("Try to find the role from assertion: '{}'", HCPRole);
        if (StringUtils.equalsIgnoreCase(HCPRole, "dentist")) {
            return "dentist";
        }
        if (StringUtils.equalsIgnoreCase(HCPRole, "nurse")) {
            return "nurse";
        }
        if (StringUtils.equalsIgnoreCase(HCPRole, "pharmacist")) {
            return "pharmacist";
        }
        if (StringUtils.equalsIgnoreCase(HCPRole, "physician")) {
            return "doctor";
        }
        return "patient";
    }

    public String getHCPRoleString(String xml) {

        String xmlTmp = xml.replaceAll("^\"|\"$", "");
        String typeOfHCP = "";
        try {
            InputSource source = new InputSource(new StringReader(xmlTmp));
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.parse(source);

            XPathFactory xpathFactory = XPathFactory.newInstance();
            XPath xpath = xpathFactory.newXPath();

            typeOfHCP = xpath.evaluate("/isHealthCareProfessional/typeOfHCP", document);
        } catch (Exception e) {
            LOGGER.error("Error finding xpath value: '{}'", e.getMessage(), e);
        }
        return typeOfHCP;
    }

    public HcpRole getHcpRole() {
        return hcpRole;
    }

    public void setHcpRole(HcpRole hcpRole) {
        this.hcpRole = hcpRole;
    }
}
