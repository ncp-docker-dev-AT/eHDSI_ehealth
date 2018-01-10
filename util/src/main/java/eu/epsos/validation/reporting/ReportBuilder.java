package eu.epsos.validation.reporting;

import eu.epsos.validation.datamodel.common.DetailedResult;
import eu.epsos.validation.datamodel.common.NcpSide;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tr.com.srdc.epsos.util.Constants;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author Marcelo Fonseca <marcelo.fonseca@iuz.pt>
 */
@Deprecated
public class ReportBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportBuilder.class);
    private static final String REPORT_FILES_FOLDER = "validation";

    private ReportBuilder() {
    }

    /**
     * This is the main operation in the report building process. It main
     * responsibility is to generate a report based on a supplied model,
     * validation object and detailed result.
     *
     * @param model            the model used in the Web Service invocation.
     * @param validationObject the validated object.
     * @param validationResult the validation result.
     * @return A boolean flag, indicating if the reporting process succeed or
     * not.
     */
    public static boolean build(final String model, final String objectType, final String validationObject, final DetailedResult validationResult, String validationResponse, final NcpSide ncpSide) {

        LOGGER.info("Build report for '{}' Model for '{}' side", objectType, ncpSide.getName());
        String reportFileName;
        String reportDirName;
        String validationTestResult;
        String validationBody;
        File reportFile;

        if (StringUtils.isEmpty(model)) {
            LOGGER.error("The specified model is null or empty.");
            return false;
        }
        if (StringUtils.isEmpty(objectType)) {
            LOGGER.error("The specified objectType is null or empty.");
            return false;
        }
        if (StringUtils.isEmpty(validationObject)) {
            LOGGER.error("The specified validation object is null or empty.");
            return false;
        }
        if (validationResult == null) {
            LOGGER.error("The specified validation result object is null. Assigning empty String to validation result.");
            validationTestResult = "";
        } else {
            validationTestResult = validationResult.getValResultsOverview().getValidationTestResult();
        }
        if (StringUtils.isEmpty(validationResponse)) {
            validationBody = "<!-- Validation report not available -->";
        } else {
            validationBody = validationResponse.replace("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>", "");
        }

        reportDirName = Constants.EPSOS_PROPS_PATH + REPORT_FILES_FOLDER + "/" + ncpSide.getName();
        reportFileName = reportDirName + "/" + buildReportFileName(model, objectType, validationTestResult);

        if (checkReportDir(reportDirName)) {

            LOGGER.info("Writing validation report in: '{}'", reportFileName);
            reportFile = new File(reportFileName);

            if (!reportFile.exists()) {
                try {
                    boolean fileCreated = reportFile.createNewFile();
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("File has been created: '{}'", fileCreated);
                    }
                } catch (IOException ex) {
                    LOGGER.error("An I/O error has occurred while creating the report file, please check the stack trace for more information.", ex);
                    return false;
                }
            }
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(reportFile.getAbsoluteFile()))) {

                bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
                bw.write("\n");
                bw.write("<validationReport>");
                bw.write("\n");
                bw.write("<validatedObject>");
                bw.write(validationObject.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", ""));
                bw.write("</validatedObject>");
                bw.write("\n");
                bw.write("<validationResult>");
                bw.write(validationBody);
                bw.write("</validationResult>");
                bw.write("\n");
                bw.write("</validationReport>");

                LOGGER.info("Validation report written with success");
                return true;

            } catch (IOException ex) {
                LOGGER.error("An I/O error has occurred while writting the report file, please check the stack trace for more information.", ex);
                return false;
            }
        }
        return false;
    }

    /**
     * This method will generate a file name for the report, based on a set of parameters.
     * Format mask: [timestamp]_[validator model]_[validation result].txt
     *
     * @param model                the model used in the validation.
     * @param validationTestResult the validation result object.
     * @return a report file name.
     */

    private static String buildReportFileName(final String model, final String objectType, final String validationTestResult) {

        final String SEPARATOR = "_";
        final String FILE_EXTENSION = ".xml";
        final String modelNormalized = model.replace(" ", "-");

        StringBuilder fileName = new StringBuilder();

        fileName.append(formatDate());

        if (objectType != null && !objectType.isEmpty()) {
            fileName.append(SEPARATOR);
            fileName.append(objectType);
        }

        if (modelNormalized != null && !modelNormalized.isEmpty()) {
            fileName.append(SEPARATOR);
            fileName.append(modelNormalized.toUpperCase());
        }

        if (validationTestResult != null && !validationTestResult.isEmpty()) {
            fileName.append(SEPARATOR);
            fileName.append(validationTestResult.toUpperCase());
        }

        fileName.append(FILE_EXTENSION);

        return fileName.toString();
    }

    /**
     * This method will check if the report directory exists, if not, it will create it.
     *
     * @param reportDirPath the complete path for the report dir.
     * @return a boolean flag stating the success of the operation.
     */
    private static boolean checkReportDir(final String reportDirPath) {

        File reportDir = new File(reportDirPath);

        if (!reportDir.exists()) {
            LOGGER.info("Creating validation report folder in: '{}'", reportDirPath);
            if (!reportDir.mkdirs()) {
                LOGGER.error("An error has occurred during the creation of validation report directory.");
                return false;
            } else {
                return true;
            }
        }
        return true;
    }

    private static String formatDate() {

        TimeZone tz = TimeZone.getTimeZone("UTC");
        //ISO 8601 format: 2017-11-25T10:59:53Z
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(tz);
        return df.format(new Date());
    }
}
