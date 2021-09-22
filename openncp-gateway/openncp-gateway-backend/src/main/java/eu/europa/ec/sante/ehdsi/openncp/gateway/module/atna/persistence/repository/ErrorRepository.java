package eu.europa.ec.sante.ehdsi.openncp.gateway.module.atna.persistence.repository;

import eu.europa.ec.sante.ehdsi.openncp.gateway.module.atna.persistence.model.Error;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ErrorRepository extends JpaRepository<Error, Long> {
}
