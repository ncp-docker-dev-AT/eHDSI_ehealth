package eu.europa.ec.sante.ehdsi.openncp.tsam.sync.repository;

import eu.europa.ec.sante.ehdsi.openncp.tsam.sync.domain.CodeSystem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CodeSystemRepository extends JpaRepository<CodeSystem, Long> {
}
