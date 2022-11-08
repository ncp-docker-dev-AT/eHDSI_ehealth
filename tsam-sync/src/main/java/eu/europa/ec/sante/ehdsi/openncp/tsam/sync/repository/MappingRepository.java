package eu.europa.ec.sante.ehdsi.openncp.tsam.sync.repository;

import eu.europa.ec.sante.ehdsi.openncp.tsam.sync.domain.Mapping;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MappingRepository extends JpaRepository<Mapping, Long> {
}
