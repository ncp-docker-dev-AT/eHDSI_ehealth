/**
 * Copyright (c) 2009-2011 University of Cardiff and others.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p>
 * Contributors:
 * Cardiff University - intial API and implementation
 */

package org.openhealthtools.openatna.archive;

import org.openhealthtools.openatna.audit.persistence.model.ErrorEntity;
import org.openhealthtools.openatna.audit.persistence.util.Base64;
import org.openhealthtools.openatna.audit.persistence.util.DataConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.util.List;

/**
 * @author Andrew Harrison
 * @version 1.0.0
 */

public class ErrorWriter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorWriter.class);

    public void begin(XMLStreamWriter writer) throws XMLStreamException {

        writer.writeStartDocument();
        writer.writeStartElement(DataConstants.ERRORS);
    }

    public void writeErrors(List<? extends ErrorEntity> errors, XMLStreamWriter writer) throws XMLStreamException {

        for (ErrorEntity error : errors) {
            writer.writeStartElement(DataConstants.ERROR);
            if (error.getSourceIp() != null) {
                writer.writeAttribute(DataConstants.SOURCE_IP, error.getSourceIp());
            }
            if (error.getErrorTimestamp() != null) {
                writer.writeAttribute(DataConstants.ERROR_TIMESTAMP, Archiver.formatDate(error.getErrorTimestamp()));
            }
            if (error.getErrorMessage() != null) {
                writer.writeStartElement(DataConstants.ERROR_MESSAGE);
                writer.writeCharacters(error.getErrorMessage());
                writer.writeEndElement();
            }
            if (error.getStackTrace() != null) {
                writer.writeStartElement(DataConstants.ERROR_STACKTRACE);
                writer.writeCData(Base64.encode(error.getStackTrace()));
                writer.writeEndElement();
            }
            if (error.getPayload() != null) {
                writer.writeStartElement(DataConstants.ERROR_PAYLOAD);
                writer.writeCharacters(Base64.encode(error.getPayload()));
                writer.writeEndElement();
            }

            writer.writeEndElement();
        }
    }


    public void finish(XMLStreamWriter writer) throws IOException {

        try {
            writer.writeEndElement();
            writer.writeEndDocument();
            writer.flush();
            writer.close();
        } catch (XMLStreamException e) {
            LOGGER.error("XMLStreamException: '{}'", e.getMessage(), e);
            throw new IOException(e.getMessage());
        }
    }
}