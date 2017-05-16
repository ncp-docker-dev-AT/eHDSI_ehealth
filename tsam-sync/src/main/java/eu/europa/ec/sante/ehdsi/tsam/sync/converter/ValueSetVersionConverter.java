package eu.europa.ec.sante.ehdsi.tsam.sync.converter;

import eu.europa.ec.sante.ehdsi.termservice.web.rest.model.sync.ValueSetVersionModel;
import eu.europa.ec.sante.ehdsi.tsam.sync.domain.ValueSetVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.time.LocalDateTime;

@Component
public class ValueSetVersionConverter implements Converter<ValueSetVersionModel, ValueSetVersion> {

    private final ValueSetConverter valueSetConverter;

    @Autowired
    public ValueSetVersionConverter(ValueSetConverter valueSetConverter) {
        Assert.notNull(valueSetConverter, "valueSetConverter must not be null");
        this.valueSetConverter = valueSetConverter;
    }

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
