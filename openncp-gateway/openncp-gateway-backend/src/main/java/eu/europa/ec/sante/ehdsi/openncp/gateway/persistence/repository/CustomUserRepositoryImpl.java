package eu.europa.ec.sante.ehdsi.openncp.gateway.persistence.repository;

import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.JPQLQuery;
import eu.europa.ec.sante.ehdsi.openncp.gateway.persistence.model.QUser;
import eu.europa.ec.sante.ehdsi.openncp.gateway.persistence.model.User;
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
public class CustomUserRepositoryImpl extends QuerydslRepositorySupport implements CustomUserRepository {

    public CustomUserRepositoryImpl() {
        super(User.class);
    }

    @Override
    public Page<User> findAllWithRoles(Predicate predicate, Pageable pageable) {
        QUser qUser = QUser.user;
        JPQLQuery<Long> countQuery = from(qUser)
                .select(qUser.id)
                .where(predicate);
        List<Long> ids = getQuerydsl().applyPagination(pageable,
                from(qUser)
                        .select(qUser.id)
                        .where(predicate))
                .fetch();
        JPQLQuery<User> fetchQuery = getQuerydsl().applySorting(pageable.getSort(),
                from(qUser)
                        .leftJoin(qUser.roles).fetchJoin()
                        .where(qUser.id.in(ids)));
        return PageableExecutionUtils.getPage(fetchQuery.fetch(), pageable, countQuery::fetchCount);
    }

    @Override
    @Autowired
    public void setEntityManager(@Qualifier("entityManagerFactory") EntityManager entityManager) {
        super.setEntityManager(entityManager);
    }
}
