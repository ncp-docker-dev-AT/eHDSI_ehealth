package eu.europa.ec.sante.ehdsi.openncp.gateway.module.atna.persistence.repository;

import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import eu.europa.ec.sante.ehdsi.openncp.gateway.module.atna.persistence.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@Transactional(readOnly = true)
public class CustomMessageRepositoryImpl extends QuerydslRepositorySupport implements CustomMessageRepository {

    public CustomMessageRepositoryImpl() {
        super(MessageEntity.class);
    }

    @Override
    public Page<MessageEntity> findAllMessages(Predicate predicate, Pageable pageable) {

        QMessageEntity qMessage = QMessageEntity.messageEntity;
        QMessageParticipantEntity qMessageParticipant = QMessageParticipantEntity.messageParticipantEntity;
        QParticipantEntity qParticipant = QParticipantEntity.participantEntity;
        QCode qCode = QCode.code;

        JPQLQuery<Long> countQuery = from(qMessage).distinct()
                .join(qMessage.messageParticipants, qMessageParticipant)
                .join(qMessageParticipant.participant, qParticipant)
                .join(qParticipant.participantTypes, qCode)
                .select(qMessage.id)
                .where(predicate);

        JPQLQuery<MessageEntity> query =
                from(qMessage).distinct()
                        .join(qMessage.messageParticipants, qMessageParticipant)
                        .join(qMessageParticipant.participant, qParticipant)
                        .join(qParticipant.participantTypes, qCode)
                        .where(predicate);

        JPQLQuery<MessageEntity> fetchQuery = getQuerydsl().applyPagination(pageable,query);

        return PageableExecutionUtils.getPage(fetchQuery.fetch(), pageable, countQuery::fetchCount);
    }

    @Override
    @Autowired
    public void setEntityManager(@Qualifier("atnaEntityManagerFactory") EntityManager entityManager) {
        super.setEntityManager(entityManager);
    }
}
