package eu.europa.ec.sante.ehdsi.tsam.sync.converter;

import eu.europa.ec.sante.ehdsi.termservice.web.rest.model.sync.CodeSystemConceptModel;
import eu.europa.ec.sante.ehdsi.termservice.web.rest.model.sync.DesignationModel;
import eu.europa.ec.sante.ehdsi.tsam.sync.domain.CodeSystemEntity;
import org.springframework.core.convert.converter.Converter;

import java.time.LocalDateTime;

public class CodeSystemEntityConverter implements Converter<CodeSystemConceptModel, CodeSystemEntity> {

    private final CodeSystemVersionConverter codeSystemVersionConverter = new CodeSystemVersionConverter();

    private final DesignationConverter designationConverter = new DesignationConverter();

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

        for (DesignationModel designation : source.getDesignations()) {
            target.addDesignation(designationConverter.convert(designation));
        }

        return target;
    }
}
