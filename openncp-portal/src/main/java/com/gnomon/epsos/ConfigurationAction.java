package com.gnomon.epsos;

import com.liferay.portal.kernel.portlet.DefaultConfigurationAction;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portlet.PortletPreferencesFactoryUtil;
import org.apache.commons.lang.StringUtils;

import javax.portlet.*;

public class ConfigurationAction extends DefaultConfigurationAction {

    public static String getConfigParam(PortletRequest request, String key) {

        PortletPreferences prefs = request.getPreferences();
        return prefs.getValue(key, "");
    }

    @Override
    public void processAction(PortletConfig portletConfig, ActionRequest actionRequest, ActionResponse actionResponse) throws Exception {

        String clientConnectorUrl = ParamUtil.getString(actionRequest, "client_connector_url");
        String checkPermissions = ParamUtil.getString(actionRequest, "check_permissions");
        String portletResource = ParamUtil.getString(actionRequest, "portletResource");

        PortletPreferences prefs;
        if ((portletResource != null) && (StringUtils.isNotBlank(portletResource))) {
            prefs = PortletPreferencesFactoryUtil.getPortletSetup(actionRequest, portletResource);
            prefs.setValue("client_connector_url", clientConnectorUrl);
            prefs.setValue("check_permissions", checkPermissions);
            prefs.store();
        }

        super.processAction(portletConfig, actionRequest, actionResponse);
    }

    @Override
    public String render(PortletConfig portletConfig, RenderRequest renderRequest, RenderResponse renderResponse) throws Exception {

        PortletConfig selPortletConfig = getSelPortletConfig(renderRequest);
        String configTemplate = selPortletConfig.getInitParameter("config-template");

        if (Validator.isNotNull(configTemplate)) {
            return configTemplate;
        }

        String configJSP = selPortletConfig.getInitParameter("config-jsp");

        if (Validator.isNotNull(configJSP)) {
            return configJSP;
        }
        return "/html/configuration.jsp";
    }
}
