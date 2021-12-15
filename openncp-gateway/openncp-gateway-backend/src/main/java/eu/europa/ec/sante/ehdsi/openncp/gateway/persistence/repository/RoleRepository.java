package eu.europa.ec.sante.ehdsi.openncp.gateway.persistence.repository;

import eu.europa.ec.sante.ehdsi.openncp.gateway.persistence.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
}
