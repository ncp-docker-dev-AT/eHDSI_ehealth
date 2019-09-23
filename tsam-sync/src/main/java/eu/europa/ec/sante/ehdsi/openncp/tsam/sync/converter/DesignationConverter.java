package eu.europa.ec.sante.ehdsi.openncp.tsam.sync.converter;

import eu.europa.ec.sante.ehdsi.openncp.tsam.sync.domain.Designation;
import eu.europa.ec.sante.ehdsi.termservice.web.rest.model.sync.DesignationModel;
import org.springframework.core.convert.converter.Converter;

import java.time.LocalDateTime;

public class DesignationConverter implements Converter<DesignationModel, Designation> {

    @Override
    public Designation convert(DesignationModel source) {
        if (source == null) {
            return null;
        }

        Designation target = new Designation();
        target.setDesignation(source.getName());
        target.setLanguageCode(source.getLanguage());
        target.setType(source.getType());
        target.setStatus("Current");
        target.setStatusDate(LocalDateTime.now());
        target.setPreferred(true);
        return target;
    }
}
