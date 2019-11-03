package se.sb.epsos.web.pages;

import com.gnomon.xslt.EpsosXSLTransformer;
import org.apache.wicket.PageParameters;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sb.epsos.web.EpsosAuthenticatedWebSession;
import se.sb.epsos.web.model.BreadCrumbVO;
import se.sb.epsos.web.model.CdaDocument;
import se.sb.epsos.web.model.LoadableDocumentModel;
import se.sb.epsos.web.service.DocumentCache;
import se.sb.epsos.web.service.DocumentClientDtoCacheKey;
import se.sb.epsos.web.service.MetaDocument;
import se.sb.epsos.web.service.NcpServiceException;

import java.net.URL;
import java.nio.charset.StandardCharsets;

@AuthorizeInstantiation({"ROLE_PHARMACIST", "ROLE_DOCTOR", "ROLE_NURSE"})
public class ViewDocumentPage extends WebPage {

    private static final Logger LOGGER = LoggerFactory.getLogger(ViewDocumentPage.class);

    public ViewDocumentPage() {
        this(null);
    }

    public ViewDocumentPage(PageParameters parameters) {

        DocumentClientDtoCacheKey docKey = new DocumentClientDtoCacheKey(getSession().getId(), parameters.getString("personId"), parameters.getString("id"));
        MetaDocument metaDoc = DocumentCache.getInstance().get(docKey);

        ((EpsosAuthenticatedWebSession) getSession()).addToBreadCrumbList(new BreadCrumbVO<ViewDocumentPage>(getString("viewDocumentPage.title." + parameters.getString("docType"))));
        LoadableDocumentModel<MetaDocument> doc = new LoadableDocumentModel<>(metaDoc);
        LOGGER.debug("New page instance: '{}'", this.getClass().getSimpleName());
        LOGGER.debug("Metadata Document: '{}'", doc.getObject());
        CdaDocument document = null;

        if (doc.getObject() instanceof CdaDocument) {
            document = (CdaDocument) doc.getObject();
        } else {
            try {
                document = ((EpsosAuthenticatedWebSession) getSession()).getServiceFacade().retrieveDocument(doc.getObject());
            } catch (NcpServiceException e) {
                if (e.isCausedByLoginRequired()) {
                    getSession().invalidateNow();
                    throw new RestartResponseAtInterceptPageException(getApplication().getHomePage());
                }
                LOGGER.error("Failed to retreive document", e);
                error(doc.getObject().getType().name() + ".error.service");
            }
        }

        // Starting CDADisplayTool treatment: XSLTransformer
        String xmlfile = null;
        if (document != null) {
            xmlfile = new String(document.getBytes(), StandardCharsets.UTF_8);

            String transformedResult = null;
            try {
                URL xmlResourceUrl = getClass().getClassLoader().getResource("displaytool/EpsosRepository");
                EpsosXSLTransformer xslTransformer;
                if (xmlResourceUrl != null) {
                    xslTransformer = new EpsosXSLTransformer(xmlResourceUrl.getPath());
                    transformedResult = xslTransformer.transform(xmlfile, "sv-SE", null, "css/style.css");
                }
            } catch (Exception e) {
                LOGGER.error("Failed to transform CDA document to HTML", e);
                error("Xslt transformation failed: " + e.getMessage());
            }
            add(new Label("document", transformedResult).setEscapeModelStrings(false));
        } else {
            LOGGER.error("Failed to transform CDA document to HTML because source document is null");
            error("Xslt transformation failed because source document is null");
        }
    }
}
