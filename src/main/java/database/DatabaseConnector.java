package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnector {
    private final String dbHost;
    private final String dbUsername;
    private final String dbPassword;
    private final String dbName;

    public DatabaseConnector(String dbHost, String dbUsername, String dbPassword, String dbName) {
        this.dbHost = dbHost;
        this.dbUsername = dbUsername;
        this.dbPassword = dbPassword;
        this.dbName = dbName;
    }

    public Connection getConnection(boolean useSSL) throws SQLException {
        // Improve this according to your MySQL database.
        Properties props = new Properties();
        props.setProperty("user", dbUsername);
        props.setProperty("password", dbPassword);
        if (useSSL) props.setProperty("useSSL", "true"); // Enable SSL

        String url = "jdbc:mysql://" + dbHost + "/" + dbName;
        return DriverManager.getConnection(url, props);
    }

}
