package eu.europa.ec.sante.ehdsi.openncp.gateway.persistence.repository;

import com.querydsl.core.types.Predicate;
import eu.europa.ec.sante.ehdsi.openncp.gateway.persistence.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomUserRepository {

    Page<User> findAllWithRoles(Predicate predicate, Pageable pageable);
}
