package eu.europa.ec.sante.ehdsi.openncp.gateway.cfg;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

import java.io.InputStream;
import java.io.InputStreamReader;

public class ClasspathResourceResolver implements LSResourceResolver {

    private final Logger logger = LoggerFactory.getLogger(ClasspathResourceResolver.class);

    @Override
    public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {

        logger.info("--> method resolveResource({}, {}, {}, {}, {})", type, namespaceURI, publicId, systemId, baseURI);
        LSInputImpl input = new LSInputImpl();

        InputStream stream = ClasspathResourceResolver.class.getResourceAsStream("/" + systemId);
        input.setPublicId(publicId);
        input.setSystemId(systemId);
        input.setBaseURI(baseURI);
        input.setCharacterStream(new InputStreamReader(stream));
        
        return input;
    }
}
