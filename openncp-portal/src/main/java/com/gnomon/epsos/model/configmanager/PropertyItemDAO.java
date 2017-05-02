package com.gnomon.epsos.model.configmanager;

import com.gnomon.epsos.util.ConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PropertyItemDAO {

    private static final Logger log = LoggerFactory.getLogger("PropertyItemDAO");
    private static final String _ADD_ITEM =
            "INSERT INTO property (name,value) " +
                    "VALUES (?,?)";
    private static final String _DELETE_ITEM =
            "DELETE FROM property WHERE name= ?";
    private static final String _GET_ITEM =
            "SELECT name, value FROM property WHERE name = ?";
    private static final String _GET_ITEMS =
            "select name, value from property";

    private PropertyItemDAO() {
    }

    /**
     * @param property
     * @return
     */
    public static CustomResponse deleteItem(String property) {
        CustomResponse cr = new CustomResponse();
        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = ConnectionPool.getConnection();
            ps = con.prepareStatement(_DELETE_ITEM);
            ps.setString(1, property);
            ps.executeUpdate();
            cr.setCode("200");
        } catch (Exception e) {
            cr.setCode("300");
            cr.setDescription(e.getMessage());
            log.error("Exception: {}" + e.getMessage(), e);
        } finally {
            ConnectionPool.cleanUp(con, ps);
        }
        return cr;
    }

    /**
     * @param property
     * @return
     * @throws SQLException
     */
    public static Property getItem(String property) throws SQLException {
        Property item = null;

        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = ConnectionPool.getConnection();
            ps = con.prepareStatement(_GET_ITEM);
            ps.setString(1, property);
            rs = ps.executeQuery();

            if (rs.next()) {
                item = new Property();

                item.setProperty(rs.getString("name"));
                item.setValue(rs.getString("value"));
            }
        } finally {
            ConnectionPool.cleanUp(con, ps, rs);
        }

        return item;
    }

    /**
     * @return
     * @throws SQLException
     */
    public static List getItems() throws SQLException {
        List list = new ArrayList<Property>();
        log.info("Getting properties from database");
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = ConnectionPool.getConnection();
            ps = con.prepareStatement(_GET_ITEMS);
            rs = ps.executeQuery();
            while (rs.next()) {
                Property item = new Property();
                String propname = rs.getString("name");
                item.setProperty(propname);
                item.setValue(rs.getString("value"));
                list.add(item);
            }
        } finally {
            ConnectionPool.cleanUp(con, ps, rs);
        }

        return list;
    }

    /**
     * @param property
     * @param value
     * @return
     */
    public static CustomResponse addItem(String property, String value) {

        CustomResponse cr = new CustomResponse();
        //Connection con = null;
        //Statement ps = null;
        //ResultSet rs = null;

        //Statement statement = null;
        //PreparedStatement preparedStatement = null;
        //String query = "select FNAME, LNAME, SSN from USERS where UNAME=?";

        try {

            /**
             * stmt1 = con.createStatement();
             ResultSet rs1 = stmt1.executeQuery("GETDATE()");

             pstmt = con.prepareStatement(query);
             pstmt.setString(1, user);  // Compliant; PreparedStatements escape their inputs.
             ResultSet rs2 = pstmt.executeQuery();
             */
            //con = ConnectionPool.getConnection();
            String insertSQL = "INSERT INTO property (name, value) VALUES (?,?);";
            log.info(insertSQL);
            try (Connection con = ConnectionPool.getConnection(); PreparedStatement preparedStatement = con.prepareStatement(insertSQL)) {

                preparedStatement.setString(1, property);
                preparedStatement.setString(2, value);
                preparedStatement.executeQuery();
            }
            cr.setCode("200");
        } catch (Exception e) {
            cr.setCode("300");
            cr.setDescription(e.getMessage());
            log.error("Exception: {}" + e.getMessage(), e);
        }
        return cr;
    }

    /**
     * @param property
     * @param value
     * @return
     */
    public static CustomResponse updateItem(String property, String value) {

        CustomResponse cr = new CustomResponse();
//        Connection con = null;
//        Statement ps = null;
//        ResultSet rs = null;

        try {
            //con = ConnectionPool.getConnection();
            //String insertSQL = "UPDATE property set value= '" + value + "' where name = '" + property + "'";
            String insertSQL = "UPDATE property set value= ? where name = ?";

            try (Connection con = ConnectionPool.getConnection(); PreparedStatement preparedStatement = con.prepareStatement(insertSQL)) {
                //preparedStatement = con.prepareStatement(insertSQL);
                preparedStatement.setString(1, value);
                preparedStatement.setString(2, property);
                preparedStatement.executeUpdate();
            }
            cr.setCode("200");
        } catch (Exception e) {
            cr.setCode("300");
            cr.setDescription(e.getMessage());
            log.error("Exception: {}" + e.getMessage(), e);
        }
        return cr;
    }
}
