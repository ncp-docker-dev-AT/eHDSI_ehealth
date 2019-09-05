package eu.europa.ec.sante.ehdsi.openncp.tsam.sync.converter;

import eu.europa.ec.sante.ehdsi.openncp.tsam.sync.domain.Concept;
import eu.europa.ec.sante.ehdsi.termservice.web.rest.model.sync.CodeSystemConceptModel;
import eu.europa.ec.sante.ehdsi.termservice.web.rest.model.sync.DesignationModel;
import eu.europa.ec.sante.ehdsi.termservice.web.rest.model.sync.MappingModel;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ConceptConverter implements Converter<CodeSystemConceptModel, Concept> {

    private final CodeSystemVersionConverter codeSystemVersionConverter = new CodeSystemVersionConverter();

    private final DesignationConverter designationConverter = new DesignationConverter();

    private MappingConverter mappingConverter;

    public ConceptConverter() {
        this(null);
    }

    public ConceptConverter(MappingConverter mappingConverter) {
        this.mappingConverter = mappingConverter;
    }

    @Override
    public Concept convert(CodeSystemConceptModel source) {
        if (source == null) {
            return null;
        }

        Concept target = new Concept();
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
        if (mappingConverter != null && source.getMappings() != null) {
            for (MappingModel mapping : source.getMappings()) {
                target.addMapping(mappingConverter.convert(mapping));
            }
        }

        return target;
    }
}
