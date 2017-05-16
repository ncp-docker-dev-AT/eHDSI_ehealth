package epsos.ccd.gnomon.tsam;

import epsos.ccd.gnomon.tsam.configuration.Settings;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 *
 */
public class XMLExporter {

    private final static Logger LOG = LoggerFactory.getLogger("TSAMExporter");
    private final static String USER_HOME = System.getenv("EPSOS_PROPS_PATH");

    /**
     *
     */
    private XMLExporter() {
        throw new IllegalAccessError("Main class");
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        String exportResult = export();
        LOG.info("TSAM Exporter result: {}", exportResult);
    }

    /**
     * @return
     */
    private static String export() {

        StringBuilder output = new StringBuilder();
        //Statement stat = null;
        //ResultSet result = null;
        //Statement stat1 = null;
        //ResultSet resultcodes = null;
        //Statement stat2 = null;
        //ResultSet result1 = null;
        //Connection conn = null;

        try {
            LOG.info("Starting ...");
            LOG.info("EPSOS Repository in '{}'", USER_HOME);
            output.append("EPSOS Repository in ").append(USER_HOME);
            output.append("<br/>");
            Class.forName(Settings.getInstance().getSettingValue("database.class.name")).newInstance();
            String databaseUrl = Settings.getInstance().getSettingValue("database.url");
            String userName = Settings.getInstance().getSettingValue("database.username");
            String userPassword = Settings.getInstance().getSettingValue("database.password");

            LOG.info("Connecting to database ...");
            String query = "SELECT * FROM code_system";

            try (Connection conn = DriverManager.getConnection(databaseUrl, userName, userPassword);
                 Statement stat = conn.createStatement();
                 ResultSet result = stat.executeQuery(query)) {
                //String codes = Settings.getInstance().getSettingValue("oid.codes");
                //String query = "SELECT * FROM code_system where id in (" + codes +")";

                String cs_id;
                String cs_oid;
                String cs_name;

                while (result.next()) {
                    StringBuilder sb = new StringBuilder();

                    cs_id = result.getString("id");
                    LOG.info("CS_ID IS : '{}'", cs_id);
                    cs_oid = result.getString("oid");
                    cs_name = result.getString("name");
                    String cs_name_nopaces = cs_name.replaceAll(" ", "");

                    LOG.info("Writing code with OID = '{}' and name = '{}'", cs_oid, cs_name);
                    output.append("Writing code with OID = ").append(cs_oid).append(" and name ").append(cs_name);
                    output.append("<br/>");
                    sb.append("<?xml version=\'1.0\' encoding=\'utf-8\' ?>\r\n");
                    sb.append("<").
                            append(cs_name_nopaces).
                            append("Information>\r\n");

                    String distinctquery = "SELECT code_system_concept.code "
                            + "FROM code_system_version INNER JOIN "
                            + " code_system_concept ON code_system_version.id = code_system_concept.code_system_version_id "
                            + "WHERE code_system_version.code_system_id = '" + cs_id + "' "
                            + "AND code_system_concept.id in (SELECT code_system_concept_id FROM x_concept_value_set)";

                    try (Statement stat1 = conn.createStatement();
                         ResultSet resultcodes = stat1.executeQuery(distinctquery)) {

                        while (resultcodes.next()) {

                            String csdistinct_id = resultcodes.getString("code");
                            csdistinct_id = StringUtils.replaceAll(csdistinct_id, "'", "''");
                            //LOG.info("Getting Info for CODE: '{}'", csdistinct_id);
                            String subquery = "SELECT value_set.oid, "
                                    + " value_set.epsos_name, "
                                    + " code_system_concept.code, "
                                    + " designation.designation, "
                                    + " designation.language_code, "
                                    + " code_system_version.code_system_id "
                                    + "FROM value_set JOIN "
                                    + " value_set_version ON value_set.id = value_set_version.value_set_id JOIN "
                                    + " x_concept_value_set ON value_set_version.id = x_concept_value_set.value_set_version_id JOIN "
                                    + " code_system_concept ON x_concept_value_set.code_system_concept_id = code_system_concept.id JOIN "
                                    + " designation ON designation.code_system_concept_id = code_system_concept.id JOIN "
                                    + " code_system_version ON  code_system_version.id = code_system_concept.code_system_version_id "
                                    + "WHERE code_system_concept.code ='" + csdistinct_id + "' AND "
                                    + " code_system_version.code_system_id = '" + cs_id + "' AND "
                                    + " code_system_concept.status = 'Current' AND "
                                    + " value_set_version.status = 'Current' AND "
                                    + " designation.status='Current' AND "
                                    + " designation.is_preferred = 1 AND "
                                    + " value_set.oid IS NOT NULL "
                                    + "ORDER BY value_set.epsos_name";
                            //LOG.info("SUBQUERY: {}", subquery);
                            try (Statement stat2 = conn.createStatement();
                                 ResultSet result1 = stat2.executeQuery(subquery)) {

                                String code = resultcodes.getString("code");

                                if (result1.next()) {
                                    String oid = result1.getString("oid");
                                    String epsosName = result1.getString("epsos_name");
                                    String lang_code = result1.getString("language_code");
                                    String description = result1.getString("designation");

                                    LOG.info("    OID: '{}' epSOS Name: '{}' Language: '{}' Description: '{}", oid, epsosName, lang_code, description);
                                    sb.append("<").
                                            append(cs_name_nopaces).
                                            append("Entry oid='").append(oid).append("'").
                                            append(" epsosName='").append(epsosName).append("'").
                                            append(" code='").append(code).append("'").
                                            append(">\r\n");

                                    sb.append("<displayName").append(" lang='").append(lang_code).append("'>").
                                            append(description).
                                            append("</displayName>\r\n");

                                    while (result1.next()) {

                                        lang_code = result1.getString("language_code");
                                        description = result1.getString("designation");
                                        sb.append("<displayName").append(" lang='").append(lang_code).append("'>").
                                                append(description).
                                                append("</displayName>\r\n");
                                    }

                                    sb.append("</").append(cs_name_nopaces).append("Entry>\r\n");
                                }
                            }
                        }

                        sb.append("</").append(cs_name_nopaces).append("Information>\r\n");

                        output.append(createFile(sb, cs_name_nopaces));
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("Exception '{}'", e.getMessage(), e);
        }
//        finally {
//            // IOUtils.closeQuietly(stat);
//
//            try {
////                //result.close();
////                stat.close();
////                resultcodes.close();
////                stat1.close();
////                result1.close();
////                stat2.close();
//               // conn.close();
//            } catch (SQLException ex) {
//                LOG.error("Exception '{}'", ex.getMessage(), ex);
//            } finally {
//                if (conn != null) {
//                    try {
//                        conn.close();
////                        stat.close();
////                        stat1.close();
////                        resultcodes.close();
//                    } catch (SQLException ex) {
//                        LOG.error(null, ex);
//                    }
//                }
//            }
        //}
        return output.toString();
    }

    /**
     * @param sb
     * @param namespace
     * @return
     */
    private static String createFile(StringBuilder sb, String namespace) {

        StringBuilder output = new StringBuilder();
        BufferedWriter out = null;
        try {
            File f = new File(USER_HOME + "EpsosRepository");

            if (!f.exists()) {
                f.mkdir();
                LOG.info("EPSOS Repository created for namespace {}", namespace);
            } else {
                LOG.info("EPSOS Repository already exists for namespace {}", namespace);
            }

            String outputFile = USER_HOME + "EpsosRepository/" + namespace + ".xml";

            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8"));
            out.write(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException e) {

                e.printStackTrace();
            }
        }
        return output.toString();
    }
}
