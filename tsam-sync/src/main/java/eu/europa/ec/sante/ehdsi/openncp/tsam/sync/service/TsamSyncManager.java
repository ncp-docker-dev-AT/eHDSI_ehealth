package eu.europa.ec.sante.ehdsi.openncp.tsam.sync.service;

import eu.europa.ec.sante.ehdsi.openncp.tsam.sync.config.CtsProperties;
import eu.europa.ec.sante.ehdsi.openncp.tsam.sync.converter.ConceptConverter;
import eu.europa.ec.sante.ehdsi.openncp.tsam.sync.converter.ValueSetVersionConverter;
import eu.europa.ec.sante.ehdsi.openncp.tsam.sync.cts.CtsClient;
import eu.europa.ec.sante.ehdsi.openncp.tsam.sync.db.DatabaseTool;
import eu.europa.ec.sante.ehdsi.openncp.tsam.sync.domain.Concept;
import eu.europa.ec.sante.ehdsi.openncp.tsam.sync.repository.CodeSystemRepository;
import eu.europa.ec.sante.ehdsi.openncp.tsam.sync.repository.ConceptRepository;
import eu.europa.ec.sante.ehdsi.openncp.tsam.sync.repository.ValueSetRepository;
import eu.europa.ec.sante.ehdsi.openncp.tsam.sync.repository.ValueSetVersionRepository;
import eu.europa.ec.sante.ehdsi.termservice.web.rest.model.sync.ValueSetCatalogModel;
import eu.europa.ec.sante.ehdsi.termservice.web.rest.model.sync.ValueSetVersionModel;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TsamSyncManager {

    private final Logger logger = LoggerFactory.getLogger(TsamSyncManager.class);
    private final DatabaseTool databaseTool;
    private final CtsProperties ctsProperties;
    private final CtsClient ctsClient;
    private final CodeSystemRepository codeSystemRepository;
    private final ConceptRepository conceptRepository;
    private final ValueSetRepository valueSetRepository;
    private final ValueSetVersionRepository valueSetVersionRepository;
    @Value("${pageable.page-size:250}")
    private Integer pageSize;

    public TsamSyncManager(DatabaseTool databaseTool, CtsProperties ctsProperties, CtsClient ctsClient, CodeSystemRepository codeSystemRepository,
                           ConceptRepository conceptRepository, ValueSetRepository valueSetRepository, ValueSetVersionRepository valueSetVersionRepository) {

        this.databaseTool = databaseTool;
        this.ctsProperties = ctsProperties;
        this.ctsClient = ctsClient;
        this.codeSystemRepository = codeSystemRepository;
        this.conceptRepository = conceptRepository;
        this.valueSetRepository = valueSetRepository;
        this.valueSetVersionRepository = valueSetVersionRepository;
    }

    @Transactional
    public void synchronize() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        logger.info("Authenticating user '{}'", ctsProperties.getUsername());
        ctsClient.authenticate();
        logger.info("User '{}' authenticated successfully to the Central Terminology Services ({})", ctsProperties.getUsername(), ctsProperties.getUrl());

        logger.info("Checking for updates");
        Optional<ValueSetCatalogModel> opt = ctsClient.fetchCatalogue();
        if (!opt.isPresent()) {
            logger.info("Nothing to synchronize");
        } else {
            ValueSetCatalogModel catalogue = opt.get();
            logger.info("Catalogue '{}' retrieved from the server", catalogue.getName());

            logger.info("Starting database backup process");
            String date = ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT);
            date = StringUtils.replace(date, ":", "-");
            databaseTool.backup("backup_" + date + ".sql");
            valueSetRepository.deleteAll();
            codeSystemRepository.deleteAll();
            logger.info("Database backup operation completed");

            ValueSetVersionConverter valueSetVersionConverter = new ValueSetVersionConverter();
            ConceptConverter conceptConverter = new ConceptConverter();

            logger.info("Starting value sets synchronization");
            int index = 1;
            for (ValueSetVersionModel valueSetVersion : catalogue.getValueSetVersions()) {
                valueSetVersionRepository.save(valueSetVersionConverter.convert(valueSetVersion));

                boolean hasNext = false;
                int page = 0, total = 0;
                while (hasNext || page == 0) {
                    List<Concept> concepts = ctsClient.fetchConcepts(valueSetVersion.getValueSet().getId(), valueSetVersion.getVersionId(), page++, pageSize)
                            .stream()
                            .map(conceptConverter::convert)
                            .collect(Collectors.toList());
                    conceptRepository.saveAll(concepts);
                    total += concepts.size();
                    hasNext = concepts.size() == pageSize;
                }

                logger.info("{}/{}: '{}' completed ({} concepts)", index++, catalogue.getValueSetVersions().size(), valueSetVersion.getValueSet().getName(), total);
            }

            stopWatch.stop();
            logger.info("Catalogue '{}' synchronized successfully ({} ms)", catalogue.getName(), stopWatch.getTotalTimeMillis());
        }
    }
}
