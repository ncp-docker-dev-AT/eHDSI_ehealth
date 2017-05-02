package epsos.ccd.gnomon.tsam;

import epsos.ccd.gnomon.tsam.configuration.Settings;
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
            String query = "SELECT * FROM CODE_SYSTEM";

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

                    cs_id = result.getString("ID");
                    LOG.info("CS_ID IS : '{}'", cs_id);
                    cs_oid = result.getString("OID");
                    cs_name = result.getString("NAME");
                    String cs_name_nopaces = cs_name.replaceAll(" ", "");

                    LOG.info("Writing code with OID = '{}' and name = '{}'", cs_oid, cs_name);
                    output.append("Writing code with OID = ").append(cs_oid).append(" and name ").append(cs_name);
                    output.append("<br/>");
                    sb.append("<?xml version=\'1.0\' encoding=\'utf-8\' ?>\r\n");
                    sb.append("<").
                            append(cs_name_nopaces).
                            append("Information>\r\n");

                    String distinctquery = "SELECT CODE_SYSTEM_CONCEPT.CODE "
                            + "FROM CODE_SYSTEM_VERSION INNER JOIN "
                            + " CODE_SYSTEM_CONCEPT ON CODE_SYSTEM_VERSION.ID = CODE_SYSTEM_CONCEPT.CODE_SYSTEM_VERSION_ID "
                            + "WHERE CODE_SYSTEM_VERSION.CODE_SYSTEM_ID = '" + cs_id + "' "
                            + "AND CODE_SYSTEM_CONCEPT.ID in (SELECT CODE_SYSTEM_CONCEPT_ID FROM X_CONCEPT_VALUE_SET)";

                    try (Statement stat1 = conn.createStatement();
                         ResultSet resultcodes = stat1.executeQuery(distinctquery)) {

                        while (resultcodes.next()) {

                            String csdistinct_id = resultcodes.getString("CODE");
                            String subquery = "SELECT VALUE_SET.OID, "
                                    + " VALUE_SET.EPSOS_NAME, "
                                    + " CODE_SYSTEM_CONCEPT.CODE, "
                                    + " DESIGNATION.DESIGNATION, "
                                    + " DESIGNATION.LANGUAGE_CODE, "
                                    + " CODE_SYSTEM_VERSION.CODE_SYSTEM_ID "
                                    + "FROM VALUE_SET JOIN "
                                    + " VALUE_SET_VERSION ON VALUE_SET.ID = VALUE_SET_VERSION.VALUE_SET_ID JOIN "
                                    + " X_CONCEPT_VALUE_SET ON VALUE_SET_VERSION.ID = X_CONCEPT_VALUE_SET.VALUE_SET_VERSION_ID JOIN "
                                    + " CODE_SYSTEM_CONCEPT ON X_CONCEPT_VALUE_SET.CODE_SYSTEM_CONCEPT_ID = CODE_SYSTEM_CONCEPT.ID JOIN "
                                    + " DESIGNATION ON DESIGNATION.CODE_SYSTEM_CONCEPT_ID = CODE_SYSTEM_CONCEPT.ID JOIN "
                                    + " CODE_SYSTEM_VERSION ON  CODE_SYSTEM_VERSION.ID = CODE_SYSTEM_CONCEPT.CODE_SYSTEM_VERSION_ID "
                                    + "WHERE CODE_SYSTEM_CONCEPT.CODE ='" + csdistinct_id + "' AND "
                                    + " CODE_SYSTEM_VERSION.CODE_SYSTEM_ID = '" + cs_id + "' AND "
                                    + " CODE_SYSTEM_CONCEPT.STATUS = 'Current' AND "
                                    + " VALUE_SET_VERSION.STATUS = 'Current' AND "
                                    + " DESIGNATION.STATUS='Current' AND "
                                    + " DESIGNATION.IS_PREFERRED = 1 AND "
                                    + " VALUE_SET.OID IS NOT NULL "
                                    + "ORDER BY VALUE_SET.EPSOS_NAME";

                            try (Statement stat2 = conn.createStatement();
                                 ResultSet result1 = stat2.executeQuery(subquery)) {
                                ;

                                String code = resultcodes.getString("CODE");

                                if (result1.next()) {
                                    String oid = result1.getString("OID");
                                    String epsosName = result1.getString("EPSOS_NAME");
                                    String lang_code = result1.getString("LANGUAGE_CODE");
                                    String description = result1.getString("DESIGNATION");
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

                                        lang_code = result1.getString("LANGUAGE_CODE");
                                        description = result1.getString("DESIGNATION");
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
                LOG.info("EPSOS Repository created");
            } else {
                LOG.info("EPSOS Repository already exists");
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
