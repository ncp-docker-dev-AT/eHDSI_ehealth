package eu.europa.ec.sante.ehdsi.openncp.tsam.sync.converter;

import eu.europa.ec.sante.ehdsi.openncp.tsam.sync.domain.Mapping;
import eu.europa.ec.sante.ehdsi.termservice.web.rest.model.sync.MappingModel;
import org.springframework.core.convert.converter.Converter;

import java.time.LocalDateTime;

public class MappingConverter implements Converter<MappingModel, Mapping> {

    private ConceptConverter conceptConverter = new ConceptConverter();

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
