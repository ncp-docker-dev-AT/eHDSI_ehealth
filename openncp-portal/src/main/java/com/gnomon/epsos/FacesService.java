package com.gnomon.epsos;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.portlet.LiferayPortletResponse;
import com.liferay.portal.kernel.portlet.LiferayPortletSession;
import com.liferay.portal.kernel.portlet.LiferayPortletURL;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.model.Company;
import com.liferay.portal.model.Layout;
import com.liferay.portal.model.LayoutTypePortlet;
import com.liferay.portal.model.User;
import com.liferay.portal.security.permission.PermissionChecker;
import com.liferay.portal.service.LayoutLocalServiceUtil;
import com.liferay.portal.service.RoleLocalServiceUtil;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.util.LayoutTypePortletFactoryUtil;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portlet.PortletURLFactoryUtil;
import org.apache.commons.lang.LocaleUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.portlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public class FacesService {

    public static final int URL_MODE_RENDER = 0;
    public static final int URL_MODE_ACTION = 1;
    public static final int URL_MODE_RESOURCE = 2;
    public static final String BUNDLE_LOCATION = "content.Language";
    private static final Logger LOGGER = LoggerFactory.getLogger(FacesService.class);

    public static PortletRequest getPortletRequest() {

        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();
        return (PortletRequest) externalContext.getRequest();
    }

    /**
     * @return
     */
    public static PortletResponse getPortletResponse() {

        LOGGER.info("getPortletResponse");
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();

        if (externalContext != null) {
            LOGGER.info("externalContext ?  '{}'", externalContext.getContextName());

            Object xyz = externalContext.getResponse();
            if (xyz != null) {
                LOGGER.info("**!@!! '{}', '{}'", xyz, xyz.getClass().getName());
            }
            return (PortletResponse) externalContext.getResponse();
        }
        return null;
    }

    public static HttpServletRequest getHttpServletRequest() {

        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();
        PortletRequest portletRequest = (PortletRequest) externalContext.getRequest();
        return PortalUtil.getHttpServletRequest(portletRequest);
    }
    
    public static Object getFromSession(String param) {

        Object ret = null;
        try {
            LOGGER.info("Try to get from session the parameter: '{}'", param);
            FacesContext facesContext = FacesContext.getCurrentInstance();
            ExternalContext externalContext = facesContext.getExternalContext();
            PortletRequest portletRequest = (PortletRequest) externalContext.getRequest();
            HttpServletRequest req = PortalUtil.getHttpServletRequest(portletRequest);
            HttpSession session = req.getSession();
            ret = session.getAttribute(param);
        } catch (Exception e) {
            LOGGER.error("ERROR: While trying to get from session the parameter : " + param + ": " + e.getMessage());
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }
        return ret;
    }

    public static boolean userHasRole(long userId, long companyId, String rolename) {

        boolean hasRole = false;
        try {
            hasRole = RoleLocalServiceUtil.hasUserRole(userId, companyId, rolename, false);
            if (hasRole) {
                LOGGER.info("User has role: '{}'", rolename);
            }
        } catch (PortalException | SystemException e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }
        return hasRole;
    }

    public static DateFormat getPortalUserDateFormat() {

        User user;
        DateFormat df = null;
        PortletRequest portletRequest = getPortletRequest();
        try {
            user = PortalUtil.getUser(portletRequest);
            df = DateFormat.getDateInstance(SimpleDateFormat.LONG, user.getLocale());
        } catch (PortalException | SystemException e1) {
            LOGGER.error(ExceptionUtils.getStackTrace(e1));
        }
        return df;
    }

    public static ExternalContext getContext() {

        FacesContext facesContext = FacesContext.getCurrentInstance();
        return facesContext.getExternalContext();
    }

    public static User getPortalUser() {

        User user = null;
        PortletRequest portletRequest = getPortletRequest();
        try {
            user = PortalUtil.getUser(portletRequest);
        } catch (PortalException | SystemException e1) {
            LOGGER.error(ExceptionUtils.getStackTrace(e1));
        }
        return user;
    }

    public static Company getPortalCompany() {

        Company company = null;
        PortletRequest portletRequest = getPortletRequest();
        try {
            company = PortalUtil.getCompany(portletRequest);
        } catch (PortalException | SystemException e1) {
            LOGGER.error(ExceptionUtils.getStackTrace(e1));
        }
        return company;
    }

    public static String getPortalLanguage() {

        PortletRequest portletRequest = getPortletRequest();
        return portletRequest.getLocale().getLanguage() + "-" + portletRequest.getLocale().getCountry();
    }

    public static String generateURL(String page) {

        PortletRequest req = getPortletRequest();
        ThemeDisplay themeDisplay = (ThemeDisplay) req.getAttribute(WebKeys.THEME_DISPLAY);

        PortletURL url = PortletURLFactoryUtil.create(req, PortalUtil.getPortletId(req), themeDisplay.getLayout().getPlid(), PortletRequest.ACTION_PHASE);
        url.setParameter("_facesViewIdRender", "/" + page + ".xhtml");
        try {
            url.setWindowState(WindowState.NORMAL);
            url.setPortletMode(PortletMode.VIEW);
        } catch (Exception e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }
        return url.toString();
    }

    public static Layout scanLayout(long groupId, boolean isPrivatePage, String frUrlPart) {

        try {
            List<Layout> layouts = LayoutLocalServiceUtil.getLayouts(groupId, isPrivatePage);
            if (!layouts.isEmpty()) {
                for (Layout ll : layouts) {
                    String frUrl = ll.getFriendlyURL();
                    if (frUrl.endsWith(frUrlPart)) {
                        return ll;
                    }
                }
            }
        } catch (SystemException e) {
            LOGGER.error("Could not scan for layout: '{}'", e.getMessage(), e);
        }
        return null;
    }

    /**
     * Scans for a page that matches a given name, and creates a portlet url to a portlet hosted in there
     *
     * @param portletId             eg "DebtUser_WAR_Debtportlet"
     * @param urlPhase              0,1 or 2 for render, action or resource URL, respectively
     * @param isPrivatePage         if the page is inside a private site
     * @param pageFrUrl             the 'friendlyurl' of the target Layout
     * @param groupId               : the GroupId for the site that hosts that page
     * @param isInstanceablePortlet hopefully false, else, it will return the first matching istance
     * @return
     */
    public static LiferayPortletURL getUrltoPortletInOtherPage(String portletId, int urlPhase, boolean isPrivatePage, String pageFrUrl, long groupId, boolean isInstanceablePortlet) {

        Layout targetLayout;
        targetLayout = scanLayout(groupId, isPrivatePage, pageFrUrl);

        PortletResponse res = getPortletResponse();
        LiferayPortletResponse rr = PortalUtil.getLiferayPortletResponse(res);
        LiferayPortletURL ddUrl;
        if (urlPhase == URL_MODE_ACTION) {
            ddUrl = rr.createActionURL(portletId);

        } else {
            ddUrl = rr.createRenderURL(portletId);

        }
        if (isInstanceablePortlet) {
            try {
                PortletRequest request = getPortletRequest();
                ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);

                String secName = "/" + pageFrUrl;
                LayoutTypePortlet layoutTypePortlet = LayoutTypePortletFactoryUtil.create(LayoutLocalServiceUtil.getFriendlyURLLayout(themeDisplay.getLayout().getGroupId(), false, secName));
                List<String> portletIdList = layoutTypePortlet.getPortletIds();
                if (portletIdList != null && !portletIdList.isEmpty()) {
                    for (String prtId : portletIdList) {
                        if (prtId.contains(portletId)) {
                            ddUrl.setPortletId(prtId);
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Could not resolve portlet id for instantiable portlet '{}'", e.getMessage(), e);
            }
        }

        if (targetLayout == null) {
            LOGGER.error("Page with given name and groupid could not be found. null Layout");

        } else {
            ddUrl.setPlid(targetLayout.getPlid());
        }
        return ddUrl;
    }

    public static String translate(String key, String lang) {

        String language = lang.replace("-", "_");
        Locale locale = LocaleUtils.toLocale(language);
        ResourceBundle rb = ResourceBundle.getBundle(BUNDLE_LOCATION, locale);
        String str = key;
        try {
            str = rb.getString(key);
            str = StringUtils.toEncodedString(str.getBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            LOGGER.error("Key: '{}' not found in message bundle. Using key as value", key, e);
        }
        return str;
    }

    /**
     * Translates a key to the proper locale
     *
     * @param key the dictionary key
     * @return the translated text
     * Note : message bundle should be 'content.Language'
     */
    public static String translate(String key) {

        FacesContext ctx = FacesContext.getCurrentInstance();
        Locale locale = ctx.getViewRoot().getLocale();
        ResourceBundle rb = ResourceBundle.getBundle(BUNDLE_LOCATION, locale);
        String str = key;
        try {
            str = rb.getString(key);
            str = StringUtils.toEncodedString(str.getBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            LOGGER.error("Key: '{}' not found in message bundle. Using key as value", key, e);
        }
        return str;
    }

    public static Locale getLocale() {

        FacesContext ctx = FacesContext.getCurrentInstance();
        return ctx.getViewRoot().getLocale();
    }

    /**
     * Adds A message to the current Context message component (p:growl or h:message )
     *
     * @param severity retreived as FacesMessage.SEVERITY_XX Constants
     * @param mainMsg  the message Summary (translated)
     * @param infoMsg  the message details (translated)
     */
    public static void addMessage(Severity severity, String mainMsg, String infoMsg) {

        FacesMessage facesMessage = new FacesMessage(severity, mainMsg, infoMsg);
        FacesContext.getCurrentInstance().addMessage(null, facesMessage);
    }

    public static PermissionChecker getPermissionChecker() {

        PortletRequest request = FacesService.getPortletRequest();
        ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
        return themeDisplay.getPermissionChecker();
    }

    /**
     * Checks if Logged Portal User is allowed to perform given action
     *
     * @param actionKey the 'action-key' value as defined in resource.actions.configs
     * @return
     */
    public static boolean hasUserPermission(String actionKey) {

        try {
            PortletRequest request = ((PortletRequest) (FacesContext.getCurrentInstance().getExternalContext().getRequest()));
            ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
            PermissionChecker permissionChecker = themeDisplay.getPermissionChecker();

            String name = PortalUtil.getPortletId(request);
            String primKey = themeDisplay.getLayout().getPlid() + LiferayPortletSession.LAYOUT_SEPARATOR + name;
            long groupId = themeDisplay.getScopeGroupId();
            return permissionChecker.hasPermission(groupId, name, primKey, actionKey);
        } catch (Exception e) {
            LOGGER.error("Exception: '{}'", e.getMessage(), e);
            return false;
        }
    }

    public static String getParameter(String key) {

        PortletRequest request = FacesService.getPortletRequest();
        Map<String, String[]> params = request.getParameterMap();
        String portletId = PortalUtil.getPortletId(request);
        String fullKey = "_" + portletId + "_" + key;

        try {
            String[] vals = params.get(fullKey);
            return vals[0];
        } catch (Exception e) {
            LOGGER.error("Exception: '{}'", e.getMessage(), e);
        }
        return null;
    }

    /**
     * Check the permission for the given actions
     *
     * @param actionKey String
     * @return boolean
     */
    public static boolean checkPermission(String actionKey) {

        PortletRequest request = ((PortletRequest) (FacesContext.getCurrentInstance().getExternalContext().getRequest()));

        ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
        PermissionChecker permissionChecker = themeDisplay.getPermissionChecker();

        String name = PortalUtil.getPortletId(request);
        String primKey = themeDisplay.getLayout().getPlid() + LiferayPortletSession.LAYOUT_SEPARATOR + name;
        long groupId = themeDisplay.getScopeGroupId();
        request.isUserInRole("administrator");
        return permissionChecker.hasPermission(groupId, name, primKey, actionKey);
    }

    public static boolean isAdministrator() {

        PortletRequest request = ((PortletRequest) (FacesContext.getCurrentInstance().getExternalContext().getRequest()));
        return request.isUserInRole("administrator");
    }
}
