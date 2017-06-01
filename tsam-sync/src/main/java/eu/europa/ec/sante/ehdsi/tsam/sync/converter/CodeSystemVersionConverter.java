package eu.europa.ec.sante.ehdsi.tsam.sync.converter;

import eu.europa.ec.sante.ehdsi.termservice.web.rest.model.sync.CodeSystemVersionModel;
import eu.europa.ec.sante.ehdsi.tsam.sync.domain.CodeSystemVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class CodeSystemVersionConverter implements Converter<CodeSystemVersionModel, CodeSystemVersion> {

    private Map<String, CodeSystemVersion> cache = new HashMap<>();

    @Autowired
    private CodeSystemConverter codeSystemConverter;

    @Override
    public CodeSystemVersion convert(CodeSystemVersionModel source) {
        if (source == null) {
            return null;
        }

        String versionKey = source.getCodeSystem().getId() + "-" + source.getVersionId();
        if (cache.containsKey(versionKey)) {
            return cache.get(versionKey);
        }

        CodeSystemVersion target = new CodeSystemVersion();
        target.setFullName(source.getFullName());
        //Version information is managed throw the attribute LocalName.
        //target.setLocalName(source.getLocalName());
        target.setLocalName(source.getVersionId());
        target.setEffectiveDate(source.getEffectiveDate() == null ? null : LocalDateTime.parse(source.getEffectiveDate()));
        target.setReleaseDate(source.getReleaseDate() == null ? null : LocalDateTime.parse(source.getReleaseDate()));
        //Code System Version status should be marked as "Current" in order to work properly with the TSAM component.
        //target.setStatus(source.getStatus());
        target.setStatus("Current");
        target.setStatusDate(source.getStatusDate() == null ? null : LocalDateTime.parse(source.getStatusDate()));
        target.setDescription(source.getDescription());
        target.setCopyright(source.getCopyright());
        target.setSource(source.getSource());
        target.setCodeSystem(codeSystemConverter.convert(source.getCodeSystem()));

        cache.put(versionKey, target);

        return target;
    }
}
