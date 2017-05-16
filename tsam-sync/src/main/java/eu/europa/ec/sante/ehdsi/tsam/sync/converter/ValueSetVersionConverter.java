package eu.europa.ec.sante.ehdsi.tsam.sync.converter;

import eu.europa.ec.sante.ehdsi.termservice.web.rest.model.sync.ValueSetVersionModel;
import eu.europa.ec.sante.ehdsi.tsam.sync.domain.ValueSetVersion;
import org.springframework.core.convert.converter.Converter;

import java.time.LocalDateTime;

public class ValueSetVersionConverter implements Converter<ValueSetVersionModel, ValueSetVersion> {

    private final ValueSetConverter valueSetConverter = new ValueSetConverter();

    @Override
    public ValueSetVersion convert(ValueSetVersionModel source) {
        if (source == null) {
            return null;
        }

        ValueSetVersion target = new ValueSetVersion();
        target.setEffectiveDate(source.getEffectiveDate() == null ? null : LocalDateTime.parse(source.getEffectiveDate()));
        target.setReleaseDate(source.getReleaseDate() == null ? null : LocalDateTime.parse(source.getReleaseDate()));
        target.setStatus("Current");
        target.setStatusDate(LocalDateTime.now());
        target.setValueSet(valueSetConverter.convert(source.getValueSet()));
        return target;
    }
}
