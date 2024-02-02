package eu.europa.ec.sante.ehdsi.openncp.gateway.module.eadc.persistence.repository;

import eu.europa.ec.sante.ehdsi.openncp.gateway.module.eadc.persistence.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface TransactionRepository extends JpaRepository<Transaction, String> {
    @Query(
            // @formatter:off
            "select trim(substr(t.startTime, 12, 5)) as year from Transaction t where trim(substr(t.startTime, 12, 5)) is not null group by trim(substr(t.startTime, 12, 5)) order by trim(substr(t.startTime, 12, 5)) desc"
            // @formatter:on
    )
    List<String> findAllYears();
}
