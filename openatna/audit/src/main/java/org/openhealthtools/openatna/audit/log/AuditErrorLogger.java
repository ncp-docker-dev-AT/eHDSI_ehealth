/*
 * Copyright (c) 2009-2011 University of Cardiff and others
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 * <p>
 * Contributors:
 * University of Cardiff - initial API and implementation
 * -
 */
package org.openhealthtools.openatna.audit.log;

import org.openhealthtools.openatna.anom.AtnaMessage;
import org.openhealthtools.openatna.audit.AuditException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Class Description Here...
 *
 * @author Andrew Harrison
 * @version $Revision:$
 */
public class AuditErrorLogger {

    private static Logger logger = LoggerFactory.getLogger("ATNA.AUDIT_ERROR_LOG");

    private static List<ErrorHandler<AuditException>> handlers = new ArrayList<>();

    private AuditErrorLogger() {
    }

    @SuppressWarnings("unused")
    public static void addErrorHandler(ErrorHandler<AuditException> handler) {
        handlers.add(handler);
    }

    private static void invokeHandlers(AuditException e) {
        for (ErrorHandler<AuditException> handler : handlers) {
            handler.handle(e);
        }
    }

    public static void log(AuditException e) {
        invokeHandlers(e);

        AuditException.AuditError error = e.getError();
        AtnaMessage msg = e.getAtnaMessage();
        if (msg == null) {
            logger.error("===> ATNA EXCEPTION THROWN\n** AUDIT ERROR: {}**\nno message available.", error, e);
        } else {
            logger.error("===> ATNA EXCEPTION THROWN\n** AUDIT ERROR: {}**\nmessage is:\n{}", error, msg, e);
        }
    }
}
