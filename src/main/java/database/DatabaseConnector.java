package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnector {
    private final String jdbcUrl;
    private final String jdbcUsername;
    private final String jdbcPassword;

    public DatabaseConnector(String jdbcUrl, String jdbcUsername, String jdbcPassword, boolean isPostgreSQL) throws ClassNotFoundException {
        this.jdbcUrl = jdbcUrl;
        this.jdbcUsername = jdbcUsername;
        this.jdbcPassword = jdbcPassword;
        if (isPostgreSQL) Class.forName("org.postgresql.Driver");
        else Class.forName("com.mysql.cj.jdbc.Driver");
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, jdbcUsername, jdbcPassword);
    }
}