package eu.europa.ec.sante.ehdsi.openncp.gateway.module.eadc.persistence.repository;

import eu.europa.ec.sante.ehdsi.openncp.gateway.module.eadc.persistence.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface TransactionRepository extends JpaRepository<Transaction, String> {
}
