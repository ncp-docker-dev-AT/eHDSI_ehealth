package org.openhealthtools.openatna.all.test;

import org.openhealthtools.openatna.anom.AtnaException;
import org.openhealthtools.openatna.anom.AtnaMessage;
import org.openhealthtools.openatna.anom.JaxbIOFactory;
import org.openhealthtools.openatna.anom.codes.CodeParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrew Harrison
 */
public abstract class ClientTest {

    public static final Logger LOGGER = LoggerFactory.getLogger(ClientTest.class);

    static {
        URL defCodes = Thread.currentThread().getContextClassLoader().getResource("atnacodes.xml");
        if (defCodes != null) {
            CodeParser.parse(defCodes.toString());
        } else {
            LOGGER.info("could not load codes!!");
        }
    }

    public List<AtnaMessage> getMessages() throws IOException, AtnaException {
        JaxbIOFactory fac = new JaxbIOFactory();
        List<AtnaMessage> messages = new ArrayList<AtnaMessage>();
        URL url = ClientTest.class.getResource("/msgs");
        File f = new File(url.getFile());
        File[] files = f.listFiles();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (file.getName().endsWith(".xml")) {
                messages.add(fac.read(new FileInputStream(file)));
            }
        }

        return messages;
    }
}
