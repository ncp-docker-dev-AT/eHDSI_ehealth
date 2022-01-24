package eu.europa.ec.sante.ehdsi.openncp.gateway.module.atna;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import eu.europa.ec.sante.ehdsi.openncp.gateway.module.atna.domain.MessageWrapper;
import eu.europa.ec.sante.ehdsi.openncp.gateway.module.atna.domain.old.Message;
import eu.europa.ec.sante.ehdsi.openncp.gateway.module.atna.persistence.model.MessageEntity;
import eu.europa.ec.sante.ehdsi.openncp.gateway.module.atna.persistence.repository.MessageRepository;
import eu.europa.ec.sante.ehdsi.openncp.gateway.module.atna.support.MessageMapper;
import eu.europa.ec.sante.ehdsi.openncp.gateway.module.atna.support.MessagePredicatesBuilder;
import generated.AuditMessage;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.io.StringWriter;
import java.time.Instant;

@Service
@Transactional(readOnly = true)
public class MessageService {

    private final MessageMapper messageMapper = Mappers.getMapper(MessageMapper.class);

    private final MessageRepository messageRepository;

    private final Jaxb2Marshaller marshaller;

    public MessageService(MessageRepository messageRepository, Jaxb2Marshaller marshaller) {
        this.messageRepository = messageRepository;
        this.marshaller = marshaller;
    }

    public Page<Message> findMessages(Pageable pageable) {
        Page<MessageEntity> page = messageRepository.findAllMessages(new BooleanBuilder(), pageable);
        return new PageImpl<>(messageMapper.map(page), pageable, page.getTotalElements());
    }

    public Page<Message> findMessages(String searchEventId, Instant searchEventStartDate, Instant searchEventEndDate,
                                      Pageable pageable) {

        MessagePredicatesBuilder builder = new MessagePredicatesBuilder();
        builder.with("eventId.code", ":", searchEventId);
        builder.with("eventStartDate", ":", searchEventStartDate);
        builder.with("eventEndDate", ":", searchEventEndDate);

        BooleanExpression exp = builder.build();

        Page<MessageEntity> page = messageRepository.findAllMessages(exp, pageable);
        return new PageImpl<>(messageMapper.map(page), pageable, page.getTotalElements());
    }

    public MessageWrapper getMessage(Long id) {

        MessageEntity entity = messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Not found"));
        AuditMessage auditMessage = (AuditMessage) marshaller.unmarshal(
                new StreamSource(new StringReader(entity.getMessageContent())));
        StringWriter outWriter = new StringWriter();
        StreamResult result = new StreamResult(outWriter);
        marshaller.marshal(auditMessage, result);

        MessageWrapper messageWrapper = new MessageWrapper();
        messageWrapper.setAuditMessage(auditMessage);
        messageWrapper.setXmlMessage(outWriter.toString());
        return messageWrapper;
    }
}
