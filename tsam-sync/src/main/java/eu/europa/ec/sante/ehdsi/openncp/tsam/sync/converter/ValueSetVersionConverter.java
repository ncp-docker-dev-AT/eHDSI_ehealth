package eu.europa.ec.sante.ehdsi.openncp.tsam.sync.converter;

import eu.europa.ec.sante.ehdsi.openncp.tsam.sync.domain.ValueSetVersion;
import eu.europa.ec.sante.ehdsi.termservice.web.rest.model.sync.ValueSetVersionModel;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ValueSetVersionConverter implements Converter<ValueSetVersionModel, ValueSetVersion> {

    private final ValueSetConverter valueSetConverter = new ValueSetConverter();

    @Override
    public ValueSetVersion convert(ValueSetVersionModel source) {
        if (source == null) {
            return null;
        }

        ValueSetVersion target = new ValueSetVersion();
        if (source.getEffectiveDate() != null) {
            target.setEffectiveDate(LocalDateTime.parse(source.getEffectiveDate()));
        }
        if (source.getReleaseDate() != null) {
            target.setReleaseDate(LocalDateTime.parse(source.getReleaseDate()));
        }
        target.setStatus("Current");
        target.setStatusDate(LocalDateTime.now());
        target.setValueSet(valueSetConverter.convert(source.getValueSet()));
        return target;
    }
}
