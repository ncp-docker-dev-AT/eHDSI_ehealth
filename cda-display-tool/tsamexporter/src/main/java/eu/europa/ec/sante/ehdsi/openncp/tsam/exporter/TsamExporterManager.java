package eu.europa.ec.sante.ehdsi.openncp.tsam.exporter;

import eu.europa.ec.sante.ehdsi.openncp.tsam.exporter.domain.Concept;
import eu.europa.ec.sante.ehdsi.openncp.tsam.exporter.domain.Designation;
import eu.europa.ec.sante.ehdsi.openncp.tsam.exporter.domain.ValueSet;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import java.io.*;
import java.nio.file.Paths;
import java.util.List;

@Service
public class TsamExporterManager {

    private final Logger logger = LoggerFactory.getLogger(TsamExporterManager.class);

    private final JdbcTemplate jdbcTemplate;

    private final Environment environment;

    @Autowired
    public TsamExporterManager(Environment environment, JdbcTemplate jdbcTemplate) {
        this.environment = environment;
        this.jdbcTemplate = jdbcTemplate;
    }

    @SuppressWarnings("WeakerAccess")
    public void export() {

        String applicationPath = environment.getRequiredProperty("openncp.root.path");
        if (StringUtils.isBlank(applicationPath)) {
            throw new TsamExporterException("Environment variable 'EPSOS_PROPS_PATH' is not defined!");
        }

        logger.debug("[TSAM Exporter] Retrieving Terminology Value Sets...");
        List<ValueSet> valueSets = jdbcTemplate.query(
                "SELECT id, oid, epsos_name FROM value_set",
                (resultSet, i) -> new ValueSet(
                        resultSet.getLong(1),
                        resultSet.getString(2),
                        resultSet.getString(3)));

        logger.info("{} value sets retrieved from the database", valueSets.size());
        File terminologyRepository = Paths.get(applicationPath, "EpsosRepository").toFile();
        if (terminologyRepository.exists()) {
            logger.info("[TSAM Exporter] Deleting previous Local Terminology Repository");
            FileSystemUtils.deleteRecursively(terminologyRepository);
        }
        boolean directoryCreated = terminologyRepository.mkdir();
        logger.info("[TSAM Exporter] Local Terminology Repository folder created: '{}'", directoryCreated);
        valueSets.forEach(valueSet -> {

            try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(new File(terminologyRepository, valueSet.getOid() + ".xml"))))) {
                out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                out.println("");
                out.println("<ValueSet oid=\"" + valueSet.getOid() + "\" name=\"" + valueSet.getName() + "\">");

                logger.debug("Retrieving concepts for value set '{}' ...", valueSet.getName());

                List<Concept> concepts = jdbcTemplate.query(
                        // @formatter:off
                        "SELECT c.id, c.code, cs.oid, cs.name " +
                                "FROM code_system_concept c " +
                                "INNER JOIN code_system_version csv ON c.code_system_version_id = csv.id " +
                                "INNER JOIN code_system cs ON csv.code_system_id = cs.id " +
                                "INNER JOIN x_concept_value_set cvsm ON cvsm.code_system_concept_id = c.id " +
                                "INNER JOIN value_set_version vsv ON vsv.id = cvsm.value_set_version_id " +
                                "WHERE vsv.value_set_id = ?", new Object[]{valueSet.getId()},
                        // @formatter:on
                        (resultSet, i) -> new Concept(
                                resultSet.getLong(1),
                                resultSet.getString(2),
                                resultSet.getString(3),
                                resultSet.getString(4)));

                logger.info("[{}, {}]: {} concepts retrieved from the database", valueSet.getName(), valueSet.getOid(), concepts.size());

                concepts.forEach(concept -> {
                    out.println("    <concept codeSystem=\"" + concept.getCodeSystemOid() +
                            "\" codeSystemName=\"" + concept.getCodeSystemName() +
                            "\" code=\"" + StringEscapeUtils.escapeXml10(concept.getCode()) + "\">");

                    List<Designation> designations = jdbcTemplate.query(
                            "SELECT language_code, designation FROM designation WHERE code_system_concept_id = ?",
                            new Object[]{concept.getId()},
                            (resultSet, i) -> new Designation(resultSet.getString(1), resultSet.getString(2)));

                    designations.forEach(designation -> out.println("        <designation lang=\"" + designation.getLanguage() + "\">" +
                            StringEscapeUtils.escapeXml10(designation.getValue()) + "</designation>"));

                    out.println("    </concept>");
                });
                out.println("</ValueSet>");
            } catch (IOException e) {
                throw new TsamExporterException("An IOException has occurred!", e);
            }
        });
    }
}
