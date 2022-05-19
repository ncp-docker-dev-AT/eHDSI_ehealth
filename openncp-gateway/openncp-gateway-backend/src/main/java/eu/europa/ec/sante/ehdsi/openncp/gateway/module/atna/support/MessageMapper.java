package eu.europa.ec.sante.ehdsi.openncp.gateway.module.atna.support;

import eu.europa.ec.sante.ehdsi.openncp.gateway.module.atna.domain.old.Message;
import eu.europa.ec.sante.ehdsi.openncp.gateway.module.atna.domain.old.MessageDetails;
import eu.europa.ec.sante.ehdsi.openncp.gateway.module.atna.persistence.model.Code;
import eu.europa.ec.sante.ehdsi.openncp.gateway.module.atna.persistence.model.MessageEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper
public interface MessageMapper {

    @Mapping(target = "eventId", source = "eventId.codeName")
    @Mapping(target = "eventTypes", source = "eventTypes")
    Message map(MessageEntity entity);

    List<Message> map(Iterable<MessageEntity> entities);

    MessageDetails mapToMessageDetails(MessageEntity entity);

    default String mapEventTypes(Set<Code> codes) {

        return codes.stream()
                .map(Code::getCodeName)
                .collect(Collectors.joining(", ", "", ""));
    }
}
