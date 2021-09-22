package eu.europa.ec.sante.ehdsi.openncp.gateway.module.smp.smpeditor.web.rest;

import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManagerFactory;
import eu.europa.ec.sante.ehdsi.openncp.gateway.module.smp.cfg.ReadSMPProperties;
import eu.europa.ec.sante.ehdsi.openncp.gateway.module.smp.domain.Countries;
import eu.europa.ec.sante.ehdsi.openncp.gateway.module.smp.domain.SMPFields;
import eu.europa.ec.sante.ehdsi.openncp.gateway.module.smp.domain.SMPFile;
import eu.europa.ec.sante.ehdsi.openncp.gateway.module.smp.domain.SMPType;
import eu.europa.ec.sante.ehdsi.openncp.gateway.module.smp.service.SimpleErrorHandler;
import eu.europa.ec.sante.ehdsi.openncp.gateway.module.smp.smpeditor.service.BdxSmpValidator;
import eu.europa.ec.sante.ehdsi.openncp.gateway.module.smp.smpeditor.service.SMPConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping(path = "/api")
public class SMPGenerateFileController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SMPGenerateFileController.class);

    private final SMPConverter smpconverter;

    private final Environment env;

    private final ReadSMPProperties readProperties;

    public SMPGenerateFileController(SMPConverter smpconverter, Environment env, ReadSMPProperties readProperties) {

        this.smpconverter = smpconverter;
        this.env = env;
        this.readProperties = readProperties;
    }

    @GetMapping(path = "smpeditor/generate/smptypes")
    public ResponseEntity<Map<String, String>> getSmpTypes() {
        Map<String, String> map = new HashMap<>();
        Arrays.stream(SMPType.values()).forEach(smpType -> map.put(smpType.name(), smpType.getDescription()));
        return ResponseEntity.ok(map);
    }

    @GetMapping(path = "smpeditor/generate/countries")
    public ResponseEntity<Map<String, String>> getCountries() {
        Map<String, String> map = new HashMap<>();
        Arrays.stream(Countries.values()).forEach(countries -> map.put(countries.name(), countries.getDescription()));
        return ResponseEntity.ok(map);
    }

    @GetMapping(path = "smpeditor/generate/smpfields")
    public ResponseEntity<SMPFields> getSmpFields(@RequestParam SMPType smpType) {
        return ResponseEntity.ok(readProperties.readProperties(smpType));
    }

    /*
     * Generate SMPFile data and create SMP file (the file itself)
     */
    @PostMapping(value = "smpeditor/generate/smpfile")
    public ResponseEntity generateSmpFile(@RequestBody SMPFile smpfile) throws IOException {

        String timeStamp;
        String fileName;

        final SMPFields smpfields = readProperties.readProperties(smpfile.getType());

        String type = env.getProperty("type." + smpfile.getType().name());

        if ("ServiceInformation".equals(type)) {
            LOGGER.debug("\n****Type Service Information");

            /*Builds final file name*/
            timeStamp = new SimpleDateFormat("yyyyMMdd'T'HHmmss").format(new Date());
            fileName = smpfile.getType().name() + "_" + smpfile.getCountry().toUpperCase() + "_" + timeStamp + ".xml";
            smpfile.setFileName(fileName);

            if (smpfields.getCertificate().isEnable()) {
                String certPath = env.getProperty(smpfile.getType().name() + ".certificate");
                String certificatePath = ConfigurationManagerFactory.getConfigurationManager().getProperty(certPath);
                LOGGER.info("Generating SMP file with certificate: '{}' '{}'", certPath, certificatePath);

                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(certificatePath);
                } catch (FileNotFoundException ex) {
                    LOGGER.error("\n FileNotFoundException - '{}'", SimpleErrorHandler.printExceptionStackTrace(ex));
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(env.getProperty("FileNotFoundException Certificate"));
                }

                smpfile.setCertificateFile(fis);
            } else {
                smpfile.setCertificateFile(null);
            }

            if (!smpfields.getExtension().isEnable()) {
                smpfile.setExtension(null);
            }

            if (smpfile.getIssuanceType() == null) {
                smpfile.setIssuanceType("");
            }

            smpconverter.convertToXml(smpfile.getType().name(),
                    smpfile.getIssuanceType(),
                    smpfile.getCountry(),
                    smpfile.getUri(),
                    smpfile.getServiceDescription(),
                    smpfile.getTechnicalContactUrl(),
                    smpfile.getTechnicalInformationUrl(),
                    smpfile.getServiceActivationDate(),
                    smpfile.getServiceExpirationDate(),
                    smpfile.getExtension(),
                    smpfile.getCertificateFile(),
                    smpfile.getFileName(),
                    smpfields.getRequireBusinessLevelSignature(),
                    smpfields.getMinimumAuthLevel(),
                    null,
                    null);

            if (smpfields.getCertificate().isEnable()) {
                if (smpconverter.getCertificateSubjectName() == null) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(env.getProperty("error.certificate.invalid"));
                }
                smpfile.setCertificate(smpconverter.getCertificateSubjectName());
            }

            if (smpfields.getExtension().isEnable() && smpconverter.isNullExtension()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(env.getProperty("error.extension.invalid"));
            }


        } else if ("Redirect".equals(type)) {
            /*
             * Get documentIdentification and participantIdentification from redirect href.
             * May change if Document or Participant Identifier specification change.
             */
            String href = smpfile.getHref();
            String documentID = "";
            String participantID = "";
            Pattern pattern = Pattern.compile(env.getProperty("ParticipantIdentifier.Scheme") + ".*"); //SPECIFICATION
            Matcher matcher = pattern.matcher(href);
            if (matcher.find()) {
                String result = matcher.group(0);
                result = java.net.URLDecoder.decode(result, StandardCharsets.UTF_8);
                //SPECIFICATION
                String[] ids = result.split("/services/");
                participantID = ids[0];
                //SPECIFICATION May change if Participant Identifier specification change
                String[] cc = participantID.split(":");

                for (Countries country : Countries.values()) {
                    if (cc[4].equals(country.name())) {
                        smpfile.setCountry(cc[4]);
                    }
                }
                if (smpfile.getCountry() == null) {
                    String message = env.getProperty("error.redirect.href.participantID");
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(message);
                }

                String docID = ids[1];
                Map<String, String> propertiesMap = readProperties.readPropertiesFile();
                //SPECIFICATION May change if Document Identifier specification change
                String[] nIDs = docID.split(env.getProperty("DocumentIdentifier.Scheme") + "::");
                String docuID = nIDs[1];
                Set set2 = propertiesMap.entrySet();
                for (Object aSet2 : set2) {

                    Map.Entry mentry2 = (Map.Entry) aSet2;
                    if (docuID.equals(mentry2.getKey().toString())) {
                        String[] docs = mentry2.getValue().toString().split("\\.");
                        documentID = docs[0];
                        break;
                    }
                }
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(env.getProperty("error.redirect.href"));
            }

            // smpeditor.properties
            String smpType = documentID;
            if ("".equals(smpType)) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(env.getProperty("error.redirect.href.documentID"));
            }

            // Builds final file name
            timeStamp = new SimpleDateFormat("yyyyMMdd'T'HHmmss").format(new java.util.Date());
            fileName = smpfile.getType().name() + "_" + smpType + "_" + smpfile.getCountry().toUpperCase() + "_" + timeStamp + ".xml";
            smpfile.setFileName(fileName);

            smpconverter.convertToXml(smpfile.getType().name(), /*0,*/ null, null, null, null, null, null, null, null, null, null,
                    smpfile.getFileName(), null, null, smpfile.getCertificateUID(), smpfile.getHref());
        }

        smpfile.setGeneratedFile(smpconverter.getFile());
        String content = new String(Files.readAllBytes(Paths.get(smpfile.getGeneratedFile().getPath())));
        //boolean valid = XMLValidator.validate(content, "/bdx-smp-201605.xsd");
        if (BdxSmpValidator.validateFile(content)) {
            LOGGER.debug("\n****VALID XML File");
        } else {
            smpfile.getGeneratedFile().deleteOnExit();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(env.getProperty("error.file.xsd"));
        }
        return ResponseEntity.ok(smpfile);
    }

    @PostMapping(value = "smpeditor/generate/download")
    public void downloadFile(@RequestBody SMPFile smpfile, HttpServletResponse response) {
        response.setContentType("application/xml");
        response.setHeader("Content-Disposition", "attachment; filename=" + smpfile.getFileName());
        response.setContentLength((int) smpfile.getGeneratedFile().length());
        try (InputStream inputStream = new BufferedInputStream(new FileInputStream(smpfile.getGeneratedFile()))) {
            FileCopyUtils.copy(inputStream, response.getOutputStream());
        } catch (FileNotFoundException ex) {
            LOGGER.error("\n FileNotFoundException - '{}'", SimpleErrorHandler.printExceptionStackTrace(ex));
        } catch (IOException ex) {
            LOGGER.error("\n IOException - '{}'", SimpleErrorHandler.printExceptionStackTrace(ex));
        }
    }

    @PostMapping(value = "smpeditor/generate/clean")
    public ResponseEntity.BodyBuilder cleanFile(@RequestBody SMPFile smpFile) {
        if (smpFile.getGeneratedFile() != null) {
            boolean delete = smpFile.getGeneratedFile().delete();
            LOGGER.debug("\n****DELETED ? '{}'", delete);
        }
        return ResponseEntity.ok();
    }
}
