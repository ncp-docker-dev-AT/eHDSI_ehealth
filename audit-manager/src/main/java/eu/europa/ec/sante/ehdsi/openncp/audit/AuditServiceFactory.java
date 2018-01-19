package eu.europa.ec.sante.ehdsi.openncp.audit;

import epsos.ccd.gnomon.auditmanager.AuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuditServiceFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuditService.class);
    private static final AuditService instance = new AuditService();

    private AuditServiceFactory() {
    }

    public static AuditService getInstance() {

        LOGGER.info("Getting Instance of Audit Service...");
        return instance;
    }
}
