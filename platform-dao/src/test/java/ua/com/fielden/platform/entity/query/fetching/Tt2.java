package ua.com.fielden.platform.entity.query.fetching;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.joda.time.DateTime;

public class Tt2 {

    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        final String connectionUrl = "jdbc:postgresql://localhost:5432/tg_test";
        Class.forName("org.postgresql.Driver");
        Connection conn = DriverManager.getConnection(connectionUrl, "t32", "T32");

        String sql = "SELECT ?, ?, ? ";
        //String sql = "SELECT interval '20 DAY' +   ? ::date ";
        
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, 1);
        ps.setString(2, "'2022-10-17'");
        ps.setDate(3, new Date((new DateTime()).getMillis()));
        ResultSet rs = ps.executeQuery();
        rs.next();
        System.out.println(rs.getMetaData().getColumnType(1));
        System.out.println(rs.getMetaData().getColumnType(2));
        System.out.println(rs.getMetaData().getColumnType(3));
        System.out.println(rs.getInt(1));
        System.out.println(rs.getString(2));
        System.out.println(rs.getDate(3));

        System.out.println("Hello World!");
        System.out.println(JDBCType.valueOf(4).getName());
        System.out.println(JDBCType.valueOf(12).getName());
        System.out.println(JDBCType.valueOf(12).getName());
    }
}
