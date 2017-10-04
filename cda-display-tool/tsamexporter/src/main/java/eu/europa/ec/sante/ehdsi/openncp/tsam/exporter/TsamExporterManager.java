package eu.europa.ec.sante.ehdsi.openncp.tsam.exporter;

import eu.europa.ec.sante.ehdsi.openncp.tsam.exporter.domain.CodeSystem;
import eu.europa.ec.sante.ehdsi.openncp.tsam.exporter.domain.Concept;
import eu.europa.ec.sante.ehdsi.openncp.tsam.exporter.domain.Designation;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Paths;
import java.util.List;

@Service
public class TsamExporterManager {

    private final Logger logger = LoggerFactory.getLogger(TsamExporterManager.class);

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public TsamExporterManager(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @SuppressWarnings("WeakerAccess")
    public void export() {
        String epsosPropsPath = System.getenv("EPSOS_PROPS_PATH");
        if (StringUtils.isBlank(epsosPropsPath)) {
            throw new TsamExporterException("Environment variable 'EPSOS_PROPS_PATH' is not defined!");
        }

        logger.debug("Retrieving code systems ...");

        List<CodeSystem> codeSystems = jdbcTemplate.query(
                "SELECT id, name FROM code_system",
                (resultSet, i) -> new CodeSystem(
                        resultSet.getLong(1),
                        resultSet.getString(2)));

        logger.info("{} code systems retrieved from the database", codeSystems.size());

        codeSystems.forEach(codeSystem -> {
            String codeSystemName = StringUtils.removeAll(codeSystem.getName(), " ");

            File epsosRepository = Paths.get(epsosPropsPath, "EpsosRepository").toFile();
            if (!epsosRepository.exists()) {
                epsosRepository.mkdir();
            }

            try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(new File(epsosPropsPath, codeSystemName + ".xml"))))) {
                out.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
                out.println("<" + codeSystemName + "Information>");

                logger.debug("Retrieving concepts for code system '{}' ...", codeSystem.getName());

                List<Concept> concepts = jdbcTemplate.query(
                        // @formatter:off
                    "SELECT c.id, c.code, vs.oid, vs.epsos_name " +
                    "FROM code_system_concept c " +
                        "INNER JOIN code_system_version csv ON c.code_system_version_id = csv.id " +
                        "INNER JOIN x_concept_value_set cvsm ON cvsm.code_system_concept_id = c.id " +
                        "INNER JOIN value_set_version vsv ON vsv.id = cvsm.value_set_version_id " +
                        "INNER JOIN value_set vs ON vs.id = vsv.value_set_id " +
                    "WHERE csv.code_system_id = ?", new Object[]{codeSystem.getId()},
                    // @formatter:on
                        (resultSet, i) -> new Concept(
                                resultSet.getLong(1),
                                resultSet.getString(2),
                                resultSet.getString(3),
                                resultSet.getString(4)));

                logger.info("[{}]: {} concepts retrieved from the database", codeSystem.getName(), concepts.size());

                concepts.forEach(concept -> {
                    out.println("<" + codeSystemName + "Entry oid=\"" + concept.getValueSetOid() + "\" epsosName=\"" + concept.getValueSetName() +
                            "\" code=\"" + StringEscapeUtils.escapeXml10(concept.getCode()) + "\">");

                    List<Designation> designations = jdbcTemplate.query(
                            "SELECT language_code, designation FROM designation WHERE code_system_concept_id = ?",
                            new Object[]{concept.getId()},
                            (resultSet, i) -> new Designation(
                                    resultSet.getString(1),
                                    resultSet.getString(2)));

                    designations.forEach(designation -> out.println("<displayName lang=\"" + designation.getLanguage() + "\">" +
                            StringEscapeUtils.escapeXml10(designation.getValue()) + "</displayName>"));

                    out.println("</" + codeSystemName + "Entry>");
                });

            } catch (IOException e) {
                throw new TsamExporterException("An IOException has occurred!", e);
            }
        });
    }
}
