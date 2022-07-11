package eu.europa.ec.sante.ehdsi.openncp.tsam.sync.converter;

import eu.europa.ec.sante.ehdsi.openncp.tsam.sync.domain.CodeSystemVersion;
import eu.europa.ec.sante.ehdsi.termservice.web.rest.model.sync.CodeSystemVersionModel;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class CodeSystemVersionConverter implements Converter<CodeSystemVersionModel, CodeSystemVersion> {

    private final CodeSystemConverter codeSystemConverter;
    private Map<String, CodeSystemVersion> cache = new HashMap<>();

    public CodeSystemVersionConverter(CodeSystemConverter codeSystemConverter) {
        this.codeSystemConverter = codeSystemConverter;
    }

    @Override
    public CodeSystemVersion convert(CodeSystemVersionModel source) {
        if (source == null) {
            return null;
        }
        String key = source.getCodeSystem().getId() + "-" + source.getVersionId();
        if (cache.containsKey(key)) {
            return cache.get(key);
        }

        CodeSystemVersion target = new CodeSystemVersion();
        target.setFullName(source.getFullName());
        target.setLocalName(source.getVersionId());
        target.setPreviousVersionId(source.getPreviousVersionId());
        if (source.getEffectiveDate() != null) {
            target.setEffectiveDate(LocalDateTime.parse(source.getEffectiveDate()));
        }
        if (source.getReleaseDate() != null) {
            target.setReleaseDate(LocalDateTime.parse(source.getReleaseDate()));
        }
        target.setStatus("Current");
        if (source.getStatusDate() != null) {
            target.setStatusDate(LocalDateTime.parse(source.getStatusDate()));
        }
        target.setDescription(source.getDescription());
        target.setCopyright(source.getCopyright());
        target.setSource(source.getSource());
        target.setCodeSystem(codeSystemConverter.convert(source.getCodeSystem()));

        cache.put(key, target);

        return target;
    }
}
