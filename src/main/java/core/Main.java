package core;

import database.DatabaseConnector;
import database.Utils;
import logger.LogUtil;

import javax.security.auth.login.LoginException;
import java.sql.Connection;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws ClassNotFoundException {
        LogUtil.showAsciiArt();
        String pathToFile = "D:\\Programming\\java-jar\\JavaPayy\\config.json";

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
            LogUtil.logError("JsonPath", "Error file json not found");
            return;
        }

        try {
            DatabaseConnector connector = new DatabaseConnector(config);
            Connection connection = connector.getConnection();
            if (connection != null) {
                LogUtil.logInfo("DatabaseConnector", "Connected to the database successfully.");
                try {
                    Utils.createTableInformation(connection, "INFORMATION");
                    Utils.createTableWorld(connection);
                    Utils.createTableUser(connection);
                    Utils.createTableHistory(connection);
                    Utils.insertDataHistory(connection);
                    Utils.insertWorldData(connection);
                    JDABot bot = new JDABot(connection, config);
                } catch (LoginException | InterruptedException e) {
                    LogUtil.logError("BotToken","Provided bot token is invalid!", e);
                }
            } else {
                LogUtil.logError("DatabaseConnector", "Failed to connect to the database.");
            }
        } catch (SQLException e) {
            LogUtil.logError("DatabaseConnector", "Error connecting to the database", e);
        }
    }
}
