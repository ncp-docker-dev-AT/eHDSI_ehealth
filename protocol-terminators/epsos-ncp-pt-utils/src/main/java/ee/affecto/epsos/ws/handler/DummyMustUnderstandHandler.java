package ee.affecto.epsos.ws.handler;

import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.handlers.AbstractHandler;

import java.util.Iterator;

public class DummyMustUnderstandHandler extends AbstractHandler {

    public InvocationResponse invoke(MessageContext messageContext) {

        SOAPHeader soapHeader = messageContext.getEnvelope().getHeader();
        if (soapHeader != null) {
            Iterator<?> blocks = soapHeader.examineAllHeaderBlocks();
            while (blocks.hasNext()) {
                SOAPHeaderBlock block = (SOAPHeaderBlock) blocks.next();
                //  if( ... some check to see if this is one of your headers ... )
                block.setProcessed();
            }
        }
        return InvocationResponse.CONTINUE;
    }
}
