package core;

import database.DatabaseConnector;
import database.Utils;

import javax.security.auth.login.LoginException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        String pathToFile = "C:\\Users\\Administrator\\Desktop\\JavaPayy\\JavaPayy\\config.json";

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--path":
                    pathToFile = args[++i];
                    break;
                default:
                    break;
            }
        }

        Config config;
        if (!pathToFile.isEmpty()) {
            config = new Config(pathToFile);
        } else {
            System.out.println("Error file json not found");
            return;
        }

        String dbHost = config.getDbHost();
        String dbUsername = config.getDbUsername();
        String dbPassword = config.getDbPassword();
        String dbName = config.getDbName();
        String botToken = config.getToken();
        String channelDeposit = config.getChannelIdDeposit();
        String emojiWL = config.getEmojiWL();
        String emojiLine = config.getEmojiLine();
        String emojiArrow = config.getEmojiArrow();
        String statusWatching = config.getStatusWatching();
        List<String> adminIDs = config.getAdminIds();

        DatabaseConnector connector = new DatabaseConnector(dbHost, dbUsername, dbPassword, dbName);

        try {
            // Database connection
            Connection connection = connector.getConnection(true);
            if (connection != null) {
                System.out.println("Connected to the database successfully.");
                try {
                    Utils.createTableInformation(connection, "INFORMATION");
                    Utils.createTableWorld(connection);
                    Utils.createTableUser(connection);
                    Utils.insertWorldData(connection);
                    JDABot bot = new JDABot(connection, botToken, adminIDs, channelDeposit, statusWatching, emojiWL, emojiLine, emojiArrow);
                } catch (LoginException e) {
                    System.out.println("ERROR: Provided bot token is invalid!");
                }
            } else {
                System.out.println("Failed to connect to the database.");
            }
        } catch (SQLException e) {
            System.out.println("Error connecting to the database: " + e.getMessage());
        }
    }
}
