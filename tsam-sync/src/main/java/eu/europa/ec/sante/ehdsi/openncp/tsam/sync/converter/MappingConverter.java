package eu.europa.ec.sante.ehdsi.openncp.tsam.sync.converter;

import eu.europa.ec.sante.ehdsi.openncp.tsam.sync.domain.Mapping;
import eu.europa.ec.sante.ehdsi.termservice.web.rest.model.sync.MappingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class MappingConverter implements Converter<MappingModel, Mapping> {

    @Autowired
    private ConceptConverter conceptConverter;

    @Override
    public Mapping convert(MappingModel source) {
        if (source == null) {
            return null;
        }

        Mapping target = new Mapping();
        target.setSource(conceptConverter.convert(source.getSource()));
        target.setStatus("Valid");
        target.setStatusDate(LocalDateTime.now());
        return target;
    }
}
