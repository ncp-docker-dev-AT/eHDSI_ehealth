package eu.europa.ec.sante.ehdsi.openncp.gateway.persistence.repository;

import eu.europa.ec.sante.ehdsi.openncp.gateway.persistence.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, CustomUserRepository {

    @Query("select u from User u join fetch u.roles where u.username = ?1")
    Optional<User> findWithRolesByUsername(String username);

    @Query("select u from User u where u.username = ?1")
    Optional<User> findByUsername(String username);

    @Query("select u from User u where u.email = ?1")
    Optional<User> findByEmail(String email);

    @Query("select u from User u where u.resetKey = ?1")
    Optional<User> findByResetKey(String key);
}
