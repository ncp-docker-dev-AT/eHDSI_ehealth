package eu.europa.ec.sante.ehdsi.openncp.gateway.module.atna.persistence.repository;

import com.querydsl.core.types.Predicate;
import eu.europa.ec.sante.ehdsi.openncp.gateway.module.atna.persistence.model.MessageEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomMessageRepository {

    Page<MessageEntity> findAllMessages(Pageable pageable);

    Page<MessageEntity> searchMessages(Predicate predicate, Pageable pageable);
}
