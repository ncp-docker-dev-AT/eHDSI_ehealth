package eu.europa.ec.sante.ehdsi.openncp.gateway.module.atna.persistence.repository;

import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.JPQLQuery;
import eu.europa.ec.sante.ehdsi.openncp.gateway.module.atna.persistence.model.MessageEntity;
import eu.europa.ec.sante.ehdsi.openncp.gateway.module.atna.persistence.model.QMessageEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

@Repository
@Transactional(readOnly = true)
public class CustomMessageRepositoryImpl extends QuerydslRepositorySupport implements CustomMessageRepository {

    public CustomMessageRepositoryImpl() {
        super(MessageEntity.class);
    }

    @Override
    public Page<MessageEntity> findAllMessages(Predicate predicate, Pageable pageable) {
        QMessageEntity qMessage = QMessageEntity.messageEntity;
        JPQLQuery<Long> countQuery = from(qMessage)
                .select(qMessage.id)
                .where(predicate);
        JPQLQuery<MessageEntity> fetchQuery = getQuerydsl().applyPagination(pageable,
                from(qMessage)
                        .innerJoin(qMessage.eventId).fetchJoin()
                        .where(predicate));
        return PageableExecutionUtils.getPage(fetchQuery.fetch(), pageable, countQuery::fetchCount);
    }

    @Override
    @Autowired
    public void setEntityManager(@Qualifier("atnaEntityManagerFactory") EntityManager entityManager) {
        super.setEntityManager(entityManager);
    }
}
