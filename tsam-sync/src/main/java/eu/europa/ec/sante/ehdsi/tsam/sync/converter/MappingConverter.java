package eu.europa.ec.sante.ehdsi.tsam.sync.converter;

import eu.europa.ec.sante.ehdsi.termservice.web.rest.model.sync.MappingModel;
import eu.europa.ec.sante.ehdsi.tsam.sync.domain.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class MappingConverter implements Converter<MappingModel, Mapping> {

    @Autowired
    private CodeSystemEntityConverter codeSystemEntityConverter;

    @Override
    public Mapping convert(MappingModel source) {
        if (source == null) {
            return null;
        }

        Mapping target = new Mapping();
        target.setSource(codeSystemEntityConverter.convert(source.getSource()));
        target.setStatus("Valid");
        target.setStatusDate(LocalDateTime.now());
        return target;
    }
}
