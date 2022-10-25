package ua.com.fielden.platform.entity.query.fetching;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.joda.time.DateTime;

public class Ttt {

    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        final String connectionUrl = "jdbc:postgresql://localhost:5432/tg_test";
        Class.forName("org.postgresql.Driver");
        Connection conn = DriverManager.getConnection(connectionUrl, "t32", "T32");

        String sql = "SELECT interval '20 DAY' +   ? ";
        //String sql = "SELECT interval '20 DAY' +   ? ::date ";
        
        PreparedStatement ps = conn.prepareStatement(sql);

        ps.setDate(1, new Date((new DateTime()).getMillis()));
        //ps.setString(1, "'2022-10-17'");
        ResultSet rs = ps.executeQuery();
        rs.next();
        System.out.println(rs.getTimestamp(1));

        System.out.println("Hello World!");
    }
}
