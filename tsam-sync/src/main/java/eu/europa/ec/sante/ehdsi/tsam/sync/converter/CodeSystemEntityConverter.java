package eu.europa.ec.sante.ehdsi.tsam.sync.converter;

import eu.europa.ec.sante.ehdsi.termservice.web.rest.model.sync.CodeSystemConceptModel;
import eu.europa.ec.sante.ehdsi.termservice.web.rest.model.sync.DesignationModel;
import eu.europa.ec.sante.ehdsi.termservice.web.rest.model.sync.MappingModel;
import eu.europa.ec.sante.ehdsi.tsam.sync.domain.CodeSystemEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class CodeSystemEntityConverter implements Converter<CodeSystemConceptModel, CodeSystemEntity> {

    @Autowired
    private CodeSystemVersionConverter codeSystemVersionConverter;

    @Autowired
    private DesignationConverter designationConverter;

    @Autowired
    private MappingConverter mappingConverter;

    @Override
    public CodeSystemEntity convert(CodeSystemConceptModel source) {
        if (source == null) {
            return null;
        }

        CodeSystemEntity target = new CodeSystemEntity();
        target.setCode(source.getCode());
        target.setDefinition(source.getDescription());
        target.setStatus("Current");
        target.setStatusDate(LocalDateTime.now());
        target.setCodeSystemVersion(codeSystemVersionConverter.convert(source.getCodeSystemVersion()));

        if (source.getDesignations() != null) {
            for (DesignationModel designation : source.getDesignations()) {
                target.addDesignation(designationConverter.convert(designation));
            }
        }

        if (source.getMappings() != null) {
            for (MappingModel mapping : source.getMappings()) {
                target.addMapping(mappingConverter.convert(mapping));
            }
        }
        return target;
    }
}
