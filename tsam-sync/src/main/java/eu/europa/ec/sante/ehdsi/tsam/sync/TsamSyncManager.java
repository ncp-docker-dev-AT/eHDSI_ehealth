package eu.europa.ec.sante.ehdsi.tsam.sync;

import eu.europa.ec.sante.ehdsi.termservice.web.rest.model.sync.ValueSetCatalogModel;
import eu.europa.ec.sante.ehdsi.termservice.web.rest.model.sync.ValueSetVersionModel;
import eu.europa.ec.sante.ehdsi.tsam.sync.client.TermServerClient;
import eu.europa.ec.sante.ehdsi.tsam.sync.converter.CodeSystemEntityConverter;
import eu.europa.ec.sante.ehdsi.tsam.sync.converter.ValueSetVersionConverter;
import eu.europa.ec.sante.ehdsi.tsam.sync.db.DatabaseBackupTool;
import eu.europa.ec.sante.ehdsi.tsam.sync.domain.CodeSystemEntity;
import eu.europa.ec.sante.ehdsi.tsam.sync.domain.ValueSetVersion;
import eu.europa.ec.sante.ehdsi.tsam.sync.repository.CodeSystemEntityRepository;
import eu.europa.ec.sante.ehdsi.tsam.sync.repository.CodeSystemRepository;
import eu.europa.ec.sante.ehdsi.tsam.sync.repository.ValueSetRepository;
import eu.europa.ec.sante.ehdsi.tsam.sync.repository.ValueSetVersionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TsamSyncManager {

    private final Logger logger = LoggerFactory.getLogger(TsamSyncManager.class);

    private final ValueSetVersionConverter valueSetVersionConverter = new ValueSetVersionConverter();
    private final CodeSystemEntityConverter codeSystemEntityConverter = new CodeSystemEntityConverter();

    private final TermServerClient termServerClient;
    private final DatabaseBackupTool databaseBackupTool;
    private final CodeSystemRepository codeSystemRepository;
    private final CodeSystemEntityRepository codeSystemEntityRepository;
    private final ValueSetRepository valueSetRepository;
    private final ValueSetVersionRepository valueSetVersionRepository;

    @Autowired
    public TsamSyncManager(TermServerClient termServerClient, DatabaseBackupTool databaseBackupTool,
                           CodeSystemRepository codeSystemRepository, CodeSystemEntityRepository codeSystemEntityRepository,
                           ValueSetRepository valueSetRepository, ValueSetVersionRepository valueSetVersionRepository) {
        this.termServerClient = termServerClient;
        this.databaseBackupTool = databaseBackupTool;
        this.codeSystemRepository = codeSystemRepository;
        this.codeSystemEntityRepository = codeSystemEntityRepository;
        this.valueSetRepository = valueSetRepository;
        this.valueSetVersionRepository = valueSetVersionRepository;
    }

    @SuppressWarnings("WeakerAccess")
    @Transactional
    public void synchronize() {
        StopWatch stopWatch = new StopWatch();

        stopWatch.start("Authentication");
        termServerClient.authenticate();
        stopWatch.stop();
        logger.trace("Task: {} / Elapsed time: {} ms", stopWatch.getLastTaskName(), stopWatch.getLastTaskTimeMillis());
        logger.info("System authenticated to central terminology services");

        stopWatch.start("Checking for updates");
        logger.info("Checking for value set catalog/agreement updates...");

        Optional<ValueSetCatalogModel> updatedValueSetCatalog = termServerClient.retrieveValueSetCatalog(null);
        stopWatch.stop();
        logger.trace("Task: {} / Elapsed time: {} ms", stopWatch.getLastTaskName(), stopWatch.getLastTaskTimeMillis());

        if (!updatedValueSetCatalog.isPresent()) {
            logger.info("Nothing to synchronize");
        } else {
            ValueSetCatalogModel valueSetCatalog = updatedValueSetCatalog.get();
            logger.info("A new value set catalog/agreement has been found [name={},agreementDate={}]", valueSetCatalog.getName(), valueSetCatalog.getAgreementDate());

            logger.info("Backuping database");
            stopWatch.start("Backup database");
            boolean success = databaseBackupTool.backupDatabase();
            stopWatch.stop();
            logger.trace("Task: {} / Elapsed time: {} ms", stopWatch.getLastTaskName(), stopWatch.getLastTaskTimeMillis());
            if (!success) {
                throw new TsamSyncManagerException("Database backup failure");
            }

            logger.info("Clear database content");
            stopWatch.start("Backup database");
            valueSetRepository.deleteAll();
            codeSystemRepository.deleteAll();
            stopWatch.stop();
            logger.trace("Task: {} / Elapsed time: {} ms", stopWatch.getLastTaskName(), stopWatch.getLastTaskTimeMillis());

            int valueSetVersionsCount = valueSetCatalog.getValueSetVersions().size();
            int index = 0;

            for (ValueSetVersionModel valueSetVersionModel : valueSetCatalog.getValueSetVersions()) {
                stopWatch.start("Process value set version");

                ValueSetVersion valueSetVersion = valueSetVersionConverter.convert(valueSetVersionModel);
                valueSetVersionRepository.save(valueSetVersion);

                boolean hasNext = true;
                int page = 0;
                int total = 0;

                while (hasNext) {
                    List<CodeSystemEntity> concepts =
                            termServerClient.retrieveConcepts(valueSetVersionModel.getValueSet().getId(), valueSetVersionModel.getVersionId(), page, 250)
                                    .stream()
                                    .map(codeSystemEntityConverter::convert)
                                    .collect(Collectors.toList());
                    concepts.forEach(concept -> concept.addValueSetVersion(valueSetVersion));

                    codeSystemEntityRepository.save(concepts);

                    total += concepts.size();
                    if (concepts.size() < 250) {
                        hasNext = false;
                    } else {
                        page++;
                    }
                }

                stopWatch.stop();
                logger.trace("Task: {} / Elapsed time: {} ms", stopWatch.getLastTaskName(), stopWatch.getLastTaskTimeMillis());

                logger.info("[{}/{}] Value set version '{}' processed ({} concepts)", ++index, valueSetVersionsCount, valueSetVersionModel.getValueSet().getName(), total);
            }
        }
    }
}
