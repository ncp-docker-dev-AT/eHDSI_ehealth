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

import org.openhealthtools.openatna.anom.AtnaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * This class logs errors at the ATNA message parsing layer.
 *
 * @author Andrew Harrison
 * @version $Revision:$
 */
public class AtnaErrorLogger {

    private static Logger logger = LoggerFactory.getLogger("ATNA.ATNA_ERROR_LOG");

    private static List<ErrorHandler<AtnaException>> handlers = new ArrayList<>();

    private AtnaErrorLogger() {
    }

    @SuppressWarnings("unused")
    public static void addErrorHandler(ErrorHandler<AtnaException> handler) {
        handlers.add(handler);
    }

    private static void invokeHandlers(AtnaException e) {
        for (ErrorHandler<AtnaException> handler : handlers) {
            handler.handle(e);
        }
    }

    public static void log(AtnaException e) {
        invokeHandlers(e);

        logger.error("===> ATNA EXCEPTION THROWN\n** ATNA ERROR: {} **", e.getError(), e);
    }
}
