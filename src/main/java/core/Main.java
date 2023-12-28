package core;

import database.DatabaseConnector;
import database.Utils;
import logger.LogUtil;

import javax.security.auth.login.LoginException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        LogUtil.showAsciiArt();
        String pathToFile = "D:\\backup\\config.json";

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

        String jdbcUrl = config.getJdbcUrl();
        String dbUsername = config.getDbUsername();
        String dbPassword = config.getDbPassword();
        String botToken = config.getToken();
        String channelDeposit = config.getChannelIdDeposit();
        String  channelHistoryDeposit = config.getChannelIdDepositHistory();
        String channelHistory = config.getChannelIdHistory();
        String emojiCurrency = config.getEmojiCurrency();
        String emojiLine = config.getEmojiLine();
        String emojiArrow = config.getEmojiArrow();
        String prefix = config.getPrefix();
        String statusPlaying = config.getStatusPlaying();
        String bannerUrlStock = config.getBannerUrlStock();
        String bannerUrlPurchase = config.getBannerUrlPurchase();
        String gmtTime = config.getGmtTime();
        String guildID = config.getGuildID();
        String channelIdAdminLogsPurchaseSetting = config.getChannelIdAdminLogsPurchaseSetting();
        String channelIdLiveStock = config.getChannelIdLiveStock();
        int intervalLiveStock = config.getIntervalLiveStock();
        boolean isUselogsPurchaseSetting = config.isUselogsPurchaseSetting();
        boolean isPostgreSQL = config.isPostgreSQL();
        boolean isUseLiveStock = config.isUseLiveStock();
        List<String> adminIDs = config.getAdminIds();

        DatabaseConnector connector = new DatabaseConnector(jdbcUrl,dbUsername,dbPassword);

        try {
            // Database connection
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
                    JDABot bot = new JDABot(
                            connection,
                            botToken,
                            adminIDs,
                            channelDeposit,
                            channelHistory,
                            channelHistoryDeposit,
                            statusPlaying,
                            emojiCurrency,
                            emojiLine,
                            emojiArrow,
                            prefix,
                            bannerUrlStock,
                            bannerUrlPurchase,
                            gmtTime,
                            guildID,
                            channelIdAdminLogsPurchaseSetting,
                            isUselogsPurchaseSetting,
                            isPostgreSQL,
                            isUseLiveStock,
                            channelIdLiveStock,
                            intervalLiveStock
                    );
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
