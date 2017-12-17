package com.liferay.portal.security.auth;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.util.PrefsPropsUtil;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.model.Company;
import com.liferay.portal.util.PortalUtil;
import eu.stork.peps.auth.commons.IPersonalAttributeList;
import eu.stork.peps.auth.commons.PEPSUtil;
import eu.stork.peps.auth.commons.STORKAuthnResponse;
import eu.stork.peps.auth.engine.STORKSAMLEngine;
import eu.stork.peps.exceptions.STORKSAMLEngineException;
import org.apache.commons.httpclient.NameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.*;
import java.util.*;

/**
 * @author karkaletsis
 */
public class StorkServlet extends HttpServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(StorkServlet.class);
    private static final String USER_AGENT = "Mozilla/5.0";
    private static Properties configs;
    private static String homepage = "/SP/";
    private static String allowIP = "127.0.0.1";

    public static String getHomepage() {
        return homepage;
    }

    public static void setHomepage(String homepage) {
        StorkServlet.homepage = homepage;
    }

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     *
     * @param request  servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) {

        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet StorkServlet</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet StorkServlet at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        } catch (IOException e) {
            LOGGER.error("IOException: '{}'", e.getMessage(), e);
        }
    }

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // Check for IP address called this servlet
        String ipAddress;
        if (request.getHeader("x-forwarded-for") != null) {
            try {
                ipAddress = InetAddress.getByName(request.getHeader("x-forwarded-for")).getHostAddress();
            } catch (UnknownHostException e) {
                LOGGER.error("UnknownHostException: '{}'", e);
                ipAddress = "Client IP not found";
            }
        } else {
            ipAddress = request.getRemoteAddr();
        }

        LOGGER.info("IP-Addr: '{}'", ipAddress);
        String STORK_ENABLED = "stork.enabled";
        Company company;

        try {
            company = PortalUtil.getCompany(request);
            String storkEnabled = PrefsPropsUtil.getString(company.getCompanyId(), STORK_ENABLED, "false");
            LOGGER.info("storkEnabled: '{}'", PrefsPropsUtil.getString(company.getCompanyId(), STORK_ENABLED, "false"));
            LOGGER.info("provider.name: '{}'", PrefsPropsUtil.getString(company.getCompanyId(), "provider.name", ""));
            LOGGER.info("sp.sector: {}'", PrefsPropsUtil.getString(company.getCompanyId(), "sp.sector", ""));
            LOGGER.info("sp.aplication: {}'", PrefsPropsUtil.getString(company.getCompanyId(), "sp.aplication", ""));
            LOGGER.info("sp.country: {}'", PrefsPropsUtil.getString(company.getCompanyId(), "sp.country", ""));
            LOGGER.info("sp.qaalevel: {}'", PrefsPropsUtil.getString(company.getCompanyId(), "sp.qaalevel", ""));
            LOGGER.info("peps.url: {}'", PrefsPropsUtil.getString(company.getCompanyId(), "peps.url", ""));
            LOGGER.info("stork.login.url: {}'", PrefsPropsUtil.getString(company.getCompanyId(), "stork.login.url", ""));
            LOGGER.info("sp.mandatory.personal.attributes: {}'", PrefsPropsUtil.getString(company.getCompanyId(), "sp.mandatory.personal.attributes", ""));
            LOGGER.info("sp.mandatory.business.attributes: {}'", PrefsPropsUtil.getString(company.getCompanyId(), "sp.mandatory.business.attributes", ""));
            LOGGER.info("sp.mandatory.legal.attributes: {}'", PrefsPropsUtil.getString(company.getCompanyId(), "sp.mandatory.legal.attributes", ""));
            request.getRequestDispatcher("/stork.jsp").forward(request, response);

        } catch (PortalException | SystemException | UnknownHostException e) {
            LOGGER.error(null, e);
        }
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        LOGGER.info("Stork Servlet Post ...");
        String providerName = PropsUtil.get("provider.name");
        String SAMLResponse = request.getParameter("SAMLResponse");
        byte[] decSamlToken = PEPSUtil.decodeSAMLToken(SAMLResponse);
        String samlResponseXML = new String(decSamlToken);
        LOGGER.info("SAML is: '{}'", samlResponseXML);

        STORKAuthnResponse authnResponse;
        IPersonalAttributeList personalAttributeList = null;
        STORKSAMLEngine engine = STORKSAMLEngine.getInstance(Constants.SP_CONF);
        String host = request.getRemoteHost();
        LOGGER.info("HOST IS: '{}'", host);

        try {
            authnResponse = engine.validateSTORKAuthnResponseWithQuery(decSamlToken, host);

            if (authnResponse.isFail()) {
                LOGGER.error("Problem with response");
            } else {
                personalAttributeList = authnResponse.getPersonalAttributeList();
            }
            response.setContentType("text/html;charset=UTF-8");
            try (PrintWriter out = response.getWriter()) {
                /* TODO output your page here. You may use following sample code. */
                out.println("<!DOCTYPE html>");
                out.println("<html>");
                out.println("<head>");
                out.println("<title>Servlet StorkServlet</title>");
                out.println("</head>");
                out.println("<body>");
                out.println("Servlet StorkServlet at " + (personalAttributeList != null ? personalAttributeList.get("givenName") : "Given Name Not Found"));
                out.println("</body>");
                out.println("</html>");
            }
        } catch (STORKSAMLEngineException e) {
            LOGGER.error("STORKSAMLEngineException: '{}'", e.getMessage(), e);
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }

    private String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException {

        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (NameValuePair pair : params) {
            if (first) {
                first = false;
            } else {
                result.append("&");
            }
            result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
        }

        return result.toString();
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
        StringBuilder content = new StringBuilder();
        for (int i = 0; keyIter.hasNext(); i++) {
            Object key = keyIter.next();
            if (i != 0) {
                content.append("&");
            }
            content.append(key).append("=").append(URLEncoder.encode(data.get(key), "UTF-8"));
        }
        out.writeBytes(content.toString());
        out.flush();
        out.close();
        StringBuilder sb = new StringBuilder();
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        while ((line = in.readLine()) != null) {
            sb.append(line);
        }
        in.close();
        return sb.toString();
    }
}
