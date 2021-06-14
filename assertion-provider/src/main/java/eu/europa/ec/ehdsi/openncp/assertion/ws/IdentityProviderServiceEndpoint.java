package eu.europa.ec.ehdsi.openncp.assertion.ws;

import com.message.schemas.message.MessageBody;
import org.apache.xerces.dom.ElementNSImpl;
import eu.epsos.ISecurityTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.jws.WebService;
import java.util.List;

@Component
@WebService(targetNamespace = "http://epsos.eu/", serviceName = "SecurityTokenService",
        portName = "ISecurityTokenService_Port", wsdlLocation = "classpath:/wsdl/TRC-STS.wsdl",
        endpointInterface = "eu.epsos.ISecurityTokenService")
public class IdentityProviderServiceEndpoint extends AbstractServiceEndpoint implements ISecurityTokenService {

    private final Logger logger = LoggerFactory.getLogger(IdentityProviderServiceEndpoint.class);

    @Override
    public MessageBody issueToken(MessageBody messageBody) {

        logger.info("Issuing Security Token");
        List<Object> anyList = messageBody.getAny();
        for (Object o : anyList) {
            logger.info("Element: '{}'", o.getClass());
            org.w3c.dom.Document document = ((ElementNSImpl) o).getOwnerDocument();
        }
        return null;
    }
}
