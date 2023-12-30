package database;

import core.Config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnector {
    private final Connection connection;

    public DatabaseConnector(Config config) throws ClassNotFoundException, SQLException {
        String jdbcUrl = config.getJdbcUrl();
        String jdbcUsername = config.getDbUsername();
        String jdbcPassword = config.getDbPassword();
        if (config.isPostgreSQL()) Class.forName("org.postgresql.Driver");
        else Class.forName("com.mysql.cj.jdbc.Driver");
        connection = DriverManager.getConnection(jdbcUrl, jdbcUsername, jdbcPassword);
    }

    public Connection getConnection() {
        return this.connection;
    }
}