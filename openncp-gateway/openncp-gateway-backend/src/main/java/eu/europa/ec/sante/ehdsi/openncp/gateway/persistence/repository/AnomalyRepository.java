package eu.europa.ec.sante.ehdsi.openncp.gateway.persistence.repository;

import eu.europa.ec.sante.ehdsi.openncp.gateway.persistence.model.Anomaly;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface AnomalyRepository extends JpaRepository<Anomaly, Long> {
}
