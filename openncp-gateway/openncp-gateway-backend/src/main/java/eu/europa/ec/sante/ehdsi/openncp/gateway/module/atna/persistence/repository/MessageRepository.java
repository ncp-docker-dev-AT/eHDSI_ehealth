package eu.europa.ec.sante.ehdsi.openncp.gateway.module.atna.persistence.repository;

import eu.europa.ec.sante.ehdsi.openncp.gateway.module.atna.persistence.model.MessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface MessageRepository extends JpaRepository<MessageEntity, Long>, CustomMessageRepository {

    @Query(
            // @formatter:off
            "select m " +
            "from MessageEntity m " +
                "inner join fetch m.eventId " +
                "inner join fetch m.eventTypes " +
                "left join fetch m.messageParticipants mp " +
                "left join fetch mp.participant " +
                "left join fetch mp.networkAccessPoint " +
            "where m.id = ?1"
            // @formatter:on
    )
    Optional<MessageEntity> findWithDetailsById(Long id);

}
