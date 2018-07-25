package se.sb.epsos.web.util;

import org.apache.commons.beanutils.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;

public class DateConverter implements Converter {

    protected final Logger logger = LoggerFactory.getLogger(DateConverter.class);
    private SimpleDateFormat dateFormat;

    public DateConverter(String datePattern) {
        dateFormat = new SimpleDateFormat(datePattern);
        dateFormat.setLenient(false);
    }

    public Object convert(Class type, Object value) {
        try {
            return dateFormat.parse((String) value);
        } catch (Exception e) {
            logger.error("Failed to parse date: '{}' with pattern: '{}'", value.toString(), dateFormat.toPattern());
            return null;
        }
    }
}
