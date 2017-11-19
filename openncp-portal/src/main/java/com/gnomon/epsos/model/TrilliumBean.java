package com.gnomon.epsos.model;

import com.gnomon.LiferayUtils;
import com.gnomon.epsos.model.cda.Utils;
import com.gnomon.epsos.service.EpsosHelperService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.servlet.ServletResponseUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.model.User;
import com.liferay.portal.util.PortalUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.primefaces.context.RequestContext;
import org.primefaces.model.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@ManagedBean
@ViewScoped
public class TrilliumBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger("TrilliumBean");
    public static Properties properties;
    private UploadedFile cdafile;
    private byte[] cdafilecontents;
    private String sourcecda;
    private String transformed;
    private String convertedcda;
    private Map<String, String> ltrlanguages = new HashMap<>();
    private String ltrlang;

    public TrilliumBean() {

        log.info("Initializing TrilliumBean ...");
        String epsosPropsPath = System.getenv("EPSOS_PROPS_PATH");
        log.info("EPSOS PROPS PATH IS: '{}'", epsosPropsPath);
        if (!Validator.isNull(epsosPropsPath)) {
            ltrlanguages = EpsosHelperService.getLTRLanguages();
            String lang = "en-US";
            try {
                User user = LiferayUtils.getPortalUser();
                lang = user.getLanguageId().replace("_", "-");

                log.info("#### " + EpsosHelperService.getConfigProperty(EpsosHelperService.PORTAL_DOCTOR_PERMISSIONS));
            } catch (Exception e) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error getting user", ""));
                log.error("Error getting user: '{}'", e.getMessage(), e);
            }

        } else {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "EPSOS_PROPS_PATH not found", ""));
        }
    }

    public static Properties getProperties() {
        return properties;
    }

    public static void setProperties(Properties properties) {
        TrilliumBean.properties = properties;
    }

    public UploadedFile getCdafile() {
        return cdafile;
    }

    public void setCdafile(UploadedFile cdafile) {
        this.cdafile = cdafile;
    }

    public String getConvertedcda() {
        return convertedcda;
    }

    public void setConvertedcda(String convertedcda) {
        this.convertedcda = convertedcda;
    }

    public void export(String type) throws IOException, PortalException, SystemException {

        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();
        String fontpath = externalContext.getRealPath("/") + "WEB-INF/fonts/";
        PortletResponse portletResponse = (PortletResponse) facesContext.getExternalContext().getResponse();
        PortletRequest portletRequest = (PortletRequest) facesContext.getExternalContext().getRequest();
        String filename = "ps_" + PortalUtil.getUser(portletRequest).getLogin();
        HttpServletResponse response = com.liferay.portal.util.PortalUtil.getHttpServletResponse(portletResponse);
        HttpServletRequest request = com.liferay.portal.util.PortalUtil.getHttpServletRequest(portletRequest);
        String contentType = "";
        InputStream stream = null;
        log.info("Export file to '{}' for language '{}'", type, ltrlang);
        byte[] temp = cdafile.getContents();
        if (Validator.isNotNull(temp)) {
            cdafilecontents = temp;
        }
        // if (type.equals("html"))
        FacesMessage msg = new FacesMessage("Successful", "");
        try {
            msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Succesful uploading " + cdafile.getFileName(), cdafile.getFileName() + " is uploaded.");
        } catch (Exception e) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Problem uploading " + cdafile.getFileName() + e.getMessage(), cdafile.getFileName() + " is not uploaded.");
            log.error(ExceptionUtils.getStackTrace(e));
        }
        String decoded = new String(cdafilecontents, StandardCharsets.UTF_8);
        log.info("#### CDA XML Start");
        log.debug(decoded.substring(0, 1000));
        log.info("#### CDA XML End");
        log.debug("Transform document to '{}'", ltrlang);
        String lang1 = ltrlang.replace("_", "-");
        lang1 = lang1.replace("en-US", "en");
        log.info("Transform document to '{}'", lang1);
        Document doc = Utils.createDomFromString(decoded);
        boolean isCDA = false;
        Document doc1;
        try {
            doc1 = Utils.createDomFromString(decoded);
            isCDA = EpsosHelperService.isCDA(doc1);
            log.info("### Document created");
            log.info("########## IS CDA: '{}'", isCDA);
        } catch (Exception e) {
            log.error(ExceptionUtils.getStackTrace(e));
        }

        transformed = decoded;

        if (StringUtils.equals(type, "html")) {

            if (isCDA) {
                log.info(("The document is EPSOS CDA"));
                // translate it
                convertedcda = Utils.getDocumentAsXml(
                        EpsosHelperService.translateDoc(
                                Utils.createDomFromString(transformed), lang1));
                // display it using cda display tool
                convertedcda = EpsosHelperService.styleDoc(convertedcda, lang1, false, "");
                showhideComponentID("form1:p1", true);
                showhideComponentID("form1:button1", true);
                showhideComponentID("form1:button2", true);
                showhideComponentID("form1:button3", true);
                return;
            } else {
                log.info(("The document is CCD"));
                convertedcda = EpsosHelperService.styleDoc(transformed, lang1, true, "");
                showhideComponentID("form1:p1", true);
                showhideComponentID("form1:button1", true);
                showhideComponentID("form1:button2", true);
                showhideComponentID("form1:button3", true);
                return;
            }

        }

        byte[] output;

        log.info("Fontpath: '{}'", fontpath);
        String serviceUrl = EpsosHelperService.getConfigProperty(EpsosHelperService.PORTAL_CLIENT_CONNECTOR_URL);

        if (StringUtils.equals(type, "pdf")) {

            if (isCDA) {
                log.info(("The document is EPSOS CDA"));
                // translate it
                convertedcda = Utils.getDocumentAsXml(
                        EpsosHelperService.translateDoc(
                                Utils.createDomFromString(transformed), lang1));
                // display it using cda display tool
                convertedcda = EpsosHelperService.styleDoc(convertedcda, lang1, false, "");
            } else {
                log.info(("The document is CCD"));
                convertedcda = EpsosHelperService.styleDoc(transformed, lang1, true, "");
            }

            Utils.WriteXMLToFile(convertedcda, "/home/karkaletsis/dev/css/a1.xml");
            stream = new ByteArrayInputStream(convertedcda.getBytes("UTF-8"));
            filename = filename + ".pdf";
            try (ByteArrayOutputStream baos = EpsosHelperService.ConvertHTMLtoPDF(convertedcda, serviceUrl, fontpath)) {

                log.info("Running pdf");
                output = baos.toByteArray();
                contentType = "application/pdf";
                response.setContentType(contentType);
                response.setHeader("Content-disposition", "attachment; filename=\"cda.pdf\"");
                OutputStream OutStream = response.getOutputStream();
                OutStream.write(output);
                OutStream.flush();
                OutStream.close();

            } catch (Exception ex) {
                log.error(ExceptionUtils.getStackTrace(ex));
                log.error(null, ex);
            }
        } else {
            if (StringUtils.equals(type, "ccd")) {
                log.info("Exporting as ccd");

                if (isCDA) {
                    // convert it
                    convertedcda = EpsosHelperService.transformDoc(transformed);
                    // display it using cda display tool
                    convertedcda = EpsosHelperService.styleDoc(convertedcda, lang1, true, "");
                } else {
                    // convert it
                    convertedcda = EpsosHelperService.transformDoc(transformed);
                    // translate it
                    convertedcda = Utils.getDocumentAsXml(EpsosHelperService.translateDoc(Utils.createDomFromString(convertedcda), lang1));

                    convertedcda = EpsosHelperService.styleDoc(convertedcda, lang1, false, "");
                }
                contentType = "text/html";
                stream = new ByteArrayInputStream(convertedcda.getBytes("UTF-8"));
                response.setHeader("Content-disposition", "inline; filename=\"cda.html\"");
                response.setContentType(contentType);
                ServletResponseUtil.sendFile(request, response, filename, stream, contentType);
                return;
            }
            if (StringUtils.equals(type, "xml")) {
                stream = new ByteArrayInputStream(transformed.getBytes("UTF-8"));
                output = transformed.getBytes("UTF-8");
                filename = filename + ".xml";
                contentType = "text/xml";
                response.setHeader("Content-disposition", "attachment; filename=\"cda.xml\"");
                response.setContentType(contentType);
                ServletResponseUtil.sendFile(request, response, filename, stream, contentType);
            }
        }
        response.flushBuffer();
        facesContext.responseComplete();
    }

    private void showhideComponentID(String componentid, boolean show) {

        UIComponent component = FacesContext.getCurrentInstance().getViewRoot().findComponent(componentid);
        if (component != null) {
            component.setRendered(show);
            RequestContext.getCurrentInstance().update(componentid);
        }
    }

    public Map<String, String> getLtrlanguages() {
        return ltrlanguages;
    }

    public void setLtrlanguages(Map<String, String> ltrlanguages) {
        this.ltrlanguages = ltrlanguages;
    }

    public String getLtrlang() {
        return ltrlang;
    }

    public void setLtrlang(String ltrlang) {
        this.ltrlang = ltrlang;
    }

    public String getTransformed() {
        return transformed;
    }

    public void setTransformed(String transformed) {
        this.transformed = transformed;
    }

    public byte[] getCdafilecontents() {
        return cdafilecontents;
    }

    public void setCdafilecontents(byte[] cdafilecontents) {
        this.cdafilecontents = cdafilecontents;
    }

    public String getSourcecda() {
        return sourcecda;
    }

    public void setSourcecda(String sourcecda) {
        this.sourcecda = sourcecda;
    }
}
