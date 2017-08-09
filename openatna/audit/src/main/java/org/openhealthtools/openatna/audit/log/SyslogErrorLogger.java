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

import org.openhealthtools.openatna.syslog.SyslogException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Logs errors at the syslog level
 *
 * @author Andrew Harrison
 * @version $Revision:$
 */
public class SyslogErrorLogger {

    private static Logger logger = LoggerFactory.getLogger("ATNA.SYSLOG_ERROR_LOG");

    private static List<ErrorHandler<SyslogException>> handlers = new ArrayList<>();

    private SyslogErrorLogger() {
    }

    @SuppressWarnings("unused")
    public static void addErrorHandler(ErrorHandler<SyslogException> handler) {
        handlers.add(handler);
    }

    private static void invokeHandlers(SyslogException e) {
        for (ErrorHandler<SyslogException> handler : handlers) {
            handler.handle(e);
        }
    }

    public static void log(SyslogException e) {
        invokeHandlers(e);

        byte[] bytes = e.getBytes();
        if (bytes.length == 0) {
            logger.error("===> SYSLOG EXCEPTION THROWN\nno bytes available.");
        } else {
            try {
                if (logger.isErrorEnabled()) {
                    logger.error("===> SYSLOG EXCEPTION THROWN\nbytes are:\n{}", new String(bytes, StandardCharsets.UTF_8.name()));
                }
            } catch (UnsupportedEncodingException e1) {
                logger.error("Unsupported encoding exception occurred", e1);
            }
        }
    }
}
