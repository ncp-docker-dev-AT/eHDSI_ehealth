package com.liferay.portal.security.auth;


import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.util.PrefsPropsUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.model.ResourceConstants;
import com.liferay.portal.model.User;
import com.liferay.portal.security.permission.ActionKeys;
import com.liferay.portal.service.ResourcePermissionLocalServiceUtil;
import com.liferay.portlet.expando.model.*;
import com.liferay.portlet.expando.service.ExpandoColumnLocalServiceUtil;
import com.liferay.portlet.expando.service.ExpandoTableLocalServiceUtil;
import com.liferay.portlet.expando.service.ExpandoValueLocalServiceUtil;
import eu.stork.peps.auth.commons.*;
import eu.stork.peps.auth.engine.STORKSAMLEngine;
import eu.stork.peps.exceptions.STORKSAMLEngineException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/**
 * @author karkaletsis
 */
public class StorkHelper {

    private static Logger log = LoggerFactory.getLogger(StorkHelper.class.getName());
    private static long companyId = CompanyThreadLocal.getCompanyId();
    private final String USER_AGENT = "Mozilla/5.0";

    public static String getPepsURL() throws com.liferay.portal.kernel.exception.SystemException {
        String pepsurl = PrefsPropsUtil.getString(companyId, "peps.url");
        log.info("### PEPS URL IS : '{}'", pepsurl);
        return pepsurl;
    }

    public static String getPepsCountry() throws com.liferay.portal.kernel.exception.SystemException {
        return PrefsPropsUtil.getString(companyId, "sp.country");
    }

    public static boolean isEnabled(long companyId) throws SystemException {
        return PrefsPropsUtil.getBoolean(companyId, "stork.enabled", false);
    }

    public static String createStorkSAML(StorkProperties properties) {
        log.info("############################");

        IPersonalAttributeList pAttList = new PersonalAttributeList();
        StringBuilder strBld = new StringBuilder();

        log.info("GET STORK ATTRIBUTES");
        log.info("sp.mandatory.personal.attributes:" + properties.getSpPersonalAttributes());
        log.info("sp.mandatory.business.attributes:" + properties.getSpBusinessAttributes());
        log.info("sp.mandatory.legal.attributes:" + properties.getSpLegalAttributes());

        String[] identifiers = properties.getSpPersonalAttributes().split(",");

        for (String identifier : identifiers) {
            if (Validator.isNotNull(identifier)) {
                log.info("PERSONAL IDENTIFIER IS : " + identifier);
                PersonalAttribute attr = new PersonalAttribute();
                attr.setName(identifier);
                attr.setIsRequired(true);
                pAttList.add(attr);
            }
        }

        String[] businessIdentifiers = properties.getSpBusinessAttributes().split(",");

        for (String identifier : businessIdentifiers) {
            if (Validator.isNotNull(identifier)) {
                log.info("BUSINESS IDENTIFIER IS : " + identifier);
                PersonalAttribute attr = new PersonalAttribute();
                attr.setName(identifier);
                attr.setIsRequired(true);
                attr.setType("business");
                pAttList.add(attr);
            }
        }

        String[] legalIdentifiers = properties.getSpLegalAttributes().split(",");

        for (String identifier : legalIdentifiers) {
            if (Validator.isNotNull(identifier)) {
                log.info("LEGAL IDENTIFIER IS : " + identifier);
                PersonalAttribute attr = new PersonalAttribute();
                attr.setName(identifier);
                attr.setIsRequired(true);
                attr.setType("legal");
                pAttList.add(attr);
            }
        }
        String providerName = properties.getProviderName();
        byte[] token = null;
        String pepsUrl = properties.getPepsURL();
        STORKAuthnRequest authnRequest = new STORKAuthnRequest();
        try {
            authnRequest.setDestination(pepsUrl);
            authnRequest.setSpCountry(properties.getSpCountry());
            authnRequest.setProviderName(providerName);
            authnRequest.setQaa(Integer.parseInt(properties.getSpQaLevel()));
            authnRequest.setPersonalAttributeList(pAttList);
            authnRequest.setAssertionConsumerServiceURL(properties.getSpReturnURL());
            authnRequest.setSpSector(properties.getSpSector());
            authnRequest.setSpApplication(properties.getSpApplication());
            authnRequest.setSPID(properties.getProviderName());
            authnRequest.setAssertionConsumerServiceURL(properties.getSpReturnURL());

            log.info("sp.peps.url: " + authnRequest.getDestination());
            log.info("sp.country: " + authnRequest.getSpCountry());
            log.info("sp.provider: " + providerName);
            log.info("sp.qaa: " + authnRequest.getQaa());
            log.info("sp.assertionurl: " + authnRequest.getAssertionConsumerServiceURL());
            log.info("sp.sector: " + authnRequest.getSpSector());
            log.info("sp.application: " + authnRequest.getSpApplication());
            log.info("sp.id: " + authnRequest.getSPID());
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            STORKSAMLEngine engine = STORKSAMLEngine.getInstance(Constants.SP_CONF);
            log.info("ENGINE: " + Validator.isNotNull(engine));
            authnRequest = engine.generateSTORKAuthnRequest(authnRequest);
        } catch (STORKSAMLEngineException e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }

        token = authnRequest.getTokenSaml();

        String SAMLRequest = PEPSUtil.encodeSAMLToken(token);
        String samlRequestXML = new String(token);
        log.info(samlRequestXML);
        return SAMLRequest;
    }

    public static String createStorkSAML() throws com.liferay.portal.kernel.exception.SystemException {
        log.info("############################");
        StorkProperties properties = new StorkProperties();
        long companyId = CompanyThreadLocal.getCompanyId();
        log.info("COMPANY FROM THREAD LOCAL IS " + companyId);
        IPersonalAttributeList pAttList = new PersonalAttributeList();
        StringBuilder strBld = new StringBuilder();

        log.info("GET STORK ATTRIBUTES");
        properties.setSpPersonalAttributes(PrefsPropsUtil.getString(companyId, "sp.mandatory.personal.attributes", ""));
        properties.setSpBusinessAttributes(PrefsPropsUtil.getString(companyId, "sp.mandatory.business.attributes", ""));
        properties.setSpLegalAttributes(PrefsPropsUtil.getString(companyId, "sp.mandatory.legal.attributes", ""));
        properties.setProviderName(PrefsPropsUtil.getString(companyId, "provider.name"));
        properties.setPepsURL(PrefsPropsUtil.getString(companyId, "peps.url"));
        properties.setSpCountry(PrefsPropsUtil.getString(companyId, "sp.country"));
        properties.setSpQaLevel(PrefsPropsUtil.getString(companyId, "sp.qaalevel"));
        properties.setSpReturnURL(PrefsPropsUtil.getString(companyId, "sp.return"));
        properties.setSpSector(PrefsPropsUtil.getString(companyId, "sp.sector"));
        properties.setSpApplication(PrefsPropsUtil.getString(companyId, "sp.aplication"));
        properties.setProviderName(PrefsPropsUtil.getString(companyId, "provider.name"));
        return createStorkSAML(properties);
    }

    public static void updateColumnValue(long expGroupTableId, String columnName, String value, long companyId, long groupId) {
        ExpandoColumn expandoColumn = null;
        ExpandoValue expandoValue = null;
        try {
            expandoColumn = ExpandoColumnLocalServiceUtil.getColumn(expGroupTableId, columnName);
            if (Validator.isNotNull(expandoColumn)) {
                expandoValue = ExpandoValueLocalServiceUtil.getValue(companyId, User.class.getName(), ExpandoTableConstants.DEFAULT_TABLE_NAME, columnName, groupId);
                if (Validator.isNotNull(expandoValue)) {
                    expandoValue.setData(value);
                    ExpandoValueLocalServiceUtil.updateExpandoValue(expandoValue);
                } else {
                    ExpandoValueLocalServiceUtil.addValue(companyId, User.class.getName(), ExpandoTableConstants.DEFAULT_TABLE_NAME, columnName, groupId, value);
                }
            } else {
                ExpandoColumnLocalServiceUtil.addColumn(expGroupTableId, columnName, ExpandoColumnConstants.STRING);
                ExpandoValueLocalServiceUtil.addValue(companyId, User.class.getName(), ExpandoTableConstants.DEFAULT_TABLE_NAME, columnName, groupId, value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ExpandoColumn addExpandoColumn(long companyId, User user, long roleId, String colname) throws PortalException, com.liferay.portal.kernel.exception.SystemException {
        ExpandoTable table = null;
        ExpandoColumn column = null;
        log.info("Adding column " + colname);
        table = ExpandoTableLocalServiceUtil.getTable(companyId,
                User.class.getName(), ExpandoTableConstants.DEFAULT_TABLE_NAME);

        try {
            column = ExpandoColumnLocalServiceUtil.getColumn(table.getPrimaryKey(), colname);

            if (Validator.isNull(column)) {
                column = ExpandoColumnLocalServiceUtil.addColumn(
                        table.getTableId(), colname,
                        ExpandoColumnConstants.STRING);
                //String[] actionsRW = new String[] { ActionKeys.VIEW, ActionKeys.UPDATE };
                String[] actionsRW = new String[]{ActionKeys.VIEW};
                log.info("Try to set permissions for expando column " + colname);
                /*
                 public static void setResourcePermissions(long companyId,
                 String name,
                 int scope,
                 String primKey,
                 long roleId,
                 String[] actionIds) throws PortalException, SystemException
                 */
                ResourcePermissionLocalServiceUtil.
                        setResourcePermissions(companyId,
                                ExpandoColumn.class.getName(), ResourceConstants.SCOPE_INDIVIDUAL,
                                String.valueOf(column.getColumnId()), roleId, actionsRW);
            } else {
                log.info("COLUMN NAME EXISTING: " + column.getName());
            }
        } catch (Exception dcne) {
            dcne.printStackTrace();

        }

        return column;
    }

    public String doSubmit(String url, Map<String, String> data) throws Exception {
        URL siteUrl = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) siteUrl.openConnection();
        conn.setRequestProperty("User-Agent", USER_AGENT);
        conn.setRequestProperty("referer", "localhost");
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setDoInput(true);

        DataOutputStream out = new DataOutputStream(conn.getOutputStream());

        Set keys = data.keySet();
        Iterator keyIter = keys.iterator();
        String content = "";
        for (int i = 0; keyIter.hasNext(); i++) {
            Object key = keyIter.next();
            if (i != 0) {
                content += "&";
            }
            content += key + "=" + URLEncoder.encode(data.get(key), "UTF-8");
        }
        //log.info(content);
        out.writeBytes(content);
        out.flush();
        out.close();
        StringBuilder sb = new StringBuilder();
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line = "";
        while ((line = in.readLine()) != null) {
            sb.append(line);
        }
        in.close();
        return sb.toString();
    }
}
