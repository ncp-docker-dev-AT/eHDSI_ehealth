/**
 * Copyright (C) 2011, 2012 SRDC Yazilim Arastirma ve Gelistirme ve Danismanlik Tic. Ltd. Sti. <epsos@srdc.com.tr>
 * <p>
 * This file is part of SRDC epSOS NCP.
 * <p>
 * SRDC epSOS NCP is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * SRDC epSOS NCP is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with SRDC epSOS NCP. If not, see <http://www.gnu.org/licenses/>.
 */
package _2007.xds_b.iti.ihe;

import epsos.ccd.gnomon.auditmanager.EventLog;
import eu.epsos.protocolterminators.ws.server.xca.XCAServiceInterface;
import ihe.iti.xds_b._2007.RetrieveDocumentSetRequestType;
import oasis.names.tc.ebxml_regrep.xsd.query._3.AdhocQueryRequest;
import oasis.names.tc.ebxml_regrep.xsd.query._3.AdhocQueryResponse;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tr.com.srdc.epsos.ws.server.xca.impl.XCAServiceImpl;

/**
 * XCA_ServiceSkeleton java skeleton for the axisService
 */
public class XCA_ServiceSkeleton {

    /**
     * Auto generated method signature
     *
     * @param adhocQueryRequest
     */

    public static final Logger logger = LoggerFactory.getLogger(XCA_ServiceSkeleton.class);

    private XCAServiceInterface service = null;

    public XCA_ServiceSkeleton() {
    }

    public AdhocQueryResponse respondingGateway_CrossGatewayQuery(
            AdhocQueryRequest adhocQueryRequest, SOAPHeader sh,
            EventLog eventLog) throws Exception {
        if (service == null) {
            service = new XCAServiceImpl();
        }

        return service.queryDocument(adhocQueryRequest, sh, eventLog);
    }

    /**
     * Auto generated method signature
     *
     * @param retrieveDocumentSetRequest
     */

    public void respondingGateway_CrossGatewayRetrieve(
            RetrieveDocumentSetRequestType retrieveDocumentSetRequest,
            SOAPHeader soapHeader, EventLog eventLog, OMElement omElement)
            throws Exception {
        if (service == null) {
            service = new XCAServiceImpl();
        }

        service.retrieveDocument(retrieveDocumentSetRequest, soapHeader,
                eventLog, omElement);
    }
}
