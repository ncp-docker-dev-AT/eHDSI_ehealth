package eu.europa.ec.sante.ehdsi.openncp.gateway.module.eadc.persistence.model;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Converter
public class EadcDbTimeConverter implements AttributeConverter<Instant, String> {

    private  final String DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss Z";
    private final ZoneId zoneId =  ZoneOffset.UTC; //ZoneId.systemDefault()

    @Override
    public String convertToDatabaseColumn(Instant instant) {
        throw new RuntimeException("No data should be right in the eadc db");
        // DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT).withZone(zoneId);
        // return formatter.format(instant);
    }

    @Override
    public Instant convertToEntityAttribute(String s) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
        ZonedDateTime timed = ZonedDateTime.parse(s, formatter);
        timed.format(formatter);
        return Instant.from(timed);
    }
}
