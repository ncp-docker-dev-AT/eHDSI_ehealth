package epsos.ccd.gnomon.tsleditor.model;

import java.io.File;
import java.io.IOException;
import java.security.cert.X509Certificate;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


import org.etsi.uri._02231.v2.ObjectFactory;
import org.etsi.uri._02231.v2.TrustStatusListType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class TrustServiceListFactory {

	private static final Logger LOG = LoggerFactory.getLogger(TrustServiceListFactory.class);

	private TrustServiceListFactory() {
		super();
	}

	/**
	 * Creates a new trust service list from the given file.
	 *
	 * @param tslFile
	 * @return
	 * @throws IOException
	 */
	public static TrustServiceList newInstance(File tslFile) throws IOException {
		if (null == tslFile) {
			throw new IllegalArgumentException();
		}
		Document tslDocument;
		try {
			tslDocument = parseDocument(tslFile);
		} catch (Exception e) {
			throw new IOException("DOM parse error: " + e.getMessage(), e);
		}
		TrustServiceList trustServiceList = newInstance(tslDocument, tslFile);
		return trustServiceList;
	}

	/**
	 * Creates a trust service list from a given DOM document.
	 *
	 * @param tslDocument
	 *            the DOM TSL document.
	 *
	 * @return
	 * @throws IOException
	 */
	public static TrustServiceList newInstance(Document tslDocument, File tslFile) throws IOException {
		if (null == tslDocument) {
			throw new IllegalArgumentException();
		}
		TrustStatusListType trustServiceStatusList;
		try {
			trustServiceStatusList = parseTslDocument(tslDocument);
		} catch (JAXBException e) {
			throw new IOException("TSL parse error: " + e.getMessage(), e);
		}
		return new TrustServiceList(trustServiceStatusList, tslDocument, tslFile);
	}

	public static TrustServiceList newInstance(Document tslDocument) throws IOException {
		return newInstance(tslDocument, null);
	}

	private static Document parseDocument(File file) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document document = documentBuilder.parse(file);
		return document;
	}

	private static TrustStatusListType parseTslDocument(Document tslDocument) throws JAXBException {
		Unmarshaller unmarshaller = getUnmarshaller();
		JAXBElement<TrustStatusListType> jaxbElement = (JAXBElement<TrustStatusListType>) unmarshaller
				.unmarshal(tslDocument);
		TrustStatusListType trustServiceStatusList = jaxbElement.getValue();
		return trustServiceStatusList;
	}

	private static Unmarshaller getUnmarshaller() throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		return unmarshaller;
	}

	/**
	 * Creates a new empty trust service list.
	 *
	 * @return
	 */
	public static TrustServiceList newInstance() {
		return new TrustServiceList();
	}

	public static TrustServiceProvider createTrustServiceProvider(String name, String tradeName) {
		TrustServiceProvider trustServiceProvider = new TrustServiceProvider(name, tradeName);
		return trustServiceProvider;
	}

	public static TrustService createTrustService(X509Certificate certificate) {
		TrustService trustService = new TrustService(certificate);
		return trustService;
	}

	public static TrustService createTrustService(X509Certificate certificate, String... oids) {
		TrustService trustService = new TrustService(certificate, oids);
		return trustService;
	}
}
