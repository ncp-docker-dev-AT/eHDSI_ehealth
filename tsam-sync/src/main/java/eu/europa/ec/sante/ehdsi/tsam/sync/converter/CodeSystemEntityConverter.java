package eu.europa.ec.sante.ehdsi.tsam.sync.converter;

import eu.europa.ec.sante.ehdsi.termservice.web.rest.model.sync.CodeSystemConceptModel;
import eu.europa.ec.sante.ehdsi.termservice.web.rest.model.sync.DesignationModel;
import eu.europa.ec.sante.ehdsi.termservice.web.rest.model.sync.MappingModel;
import eu.europa.ec.sante.ehdsi.tsam.sync.domain.CodeSystemEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.time.LocalDateTime;

@Component
public class CodeSystemEntityConverter implements Converter<CodeSystemConceptModel, CodeSystemEntity> {

    private final CodeSystemVersionConverter codeSystemVersionConverter;

    private final DesignationConverter designationConverter;

    private final MappingConverter mappingConverter;

    @Autowired
    public CodeSystemEntityConverter(CodeSystemVersionConverter codeSystemVersionConverter, DesignationConverter designationConverter, MappingConverter mappingConverter) {
        Assert.notNull(codeSystemVersionConverter, "codeSystemVersionConverter must not be null");
        Assert.notNull(designationConverter, "designationConverter must not be null");
        Assert.notNull(mappingConverter, "mappingConverter must not be null");
        this.codeSystemVersionConverter = codeSystemVersionConverter;
        this.designationConverter = designationConverter;
        this.mappingConverter = mappingConverter;
    }

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

        for (MappingModel mapping : source.getMappings()) {
            target.addMapping(mappingConverter.convert(mapping));
        }

        return target;
    }
}
