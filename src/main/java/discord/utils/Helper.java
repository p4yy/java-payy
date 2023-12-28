package discord.utils;

import database.Utils;
import logger.LogUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class Helper {

    public static String getStrTime(String gmt) {
        try {
            ZonedDateTime time = ZonedDateTime.now(ZoneId.of(gmt));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return time.format(formatter);
        } catch (Exception e) {
            LogUtil.logError("Helper", "Error getting string time {}", e.getMessage());
            return "";
        }
    }


    protected static String commandUser(Connection connection, String command, String idUser) {
        String returnedData = null;

        try {
            Object[] userData = Utils.getDataFromTableUser(connection, "ID", idUser);

            if (userData.length > 0 && userData[0] != null) {
                Double balance = (Double) userData[1];
                String username = (String) userData[3];
                if (command.equals("checkuser")) {
                    returnedData = username;
                } else if (command.equals("balance")) {
                    returnedData = balance.toString();
                }


            } else {
                LogUtil.logError("User", "User with ID {} not found", idUser);
            }
        } catch (SQLException e) {
            LogUtil.logError("User", "Error when get data from database: {}", e.getMessage());
        }

        return returnedData;
    }

    protected static String[] getDataTableHistory(Connection connection) {
        String[] returnedData = new String[4];

        try {
            Object[] historyData = Utils.getDataFromTableHistory(connection);

            if (historyData.length > 0 && historyData[0] != null) {
                int purchase = (int) historyData[0];
                double moneyIn = (double) historyData[1];
                double moneyOut = (double) historyData[2];
                double moneyInCirculation = (double) historyData[3];
                returnedData[0] = Integer.toString(purchase);
                returnedData[1] = Double.toString(moneyIn);
                returnedData[2] = Double.toString(moneyOut);
                returnedData[3] = Double.toString(moneyInCirculation);
            } else {
                LogUtil.logError("Purchase", "Error when get data Purchase from table HISTORY");
            }
        } catch (SQLException e) {
            LogUtil.logError("Purchase", "Error when get data purchase from database: {}", e.getMessage());
        }

        return returnedData;
    }

    protected static String[] getBalanceByUsername(Connection connection, String username) {
        String[] returnedData = new String[3];

        try {
            Object[] userData = Utils.getDataFromTableUser(connection, "USERNAME", username);

            if (userData.length > 0 && userData[0] != null) {
                String idUser = (String) userData[0];
                Double balance = (Double) userData[1];
                Double totalDeposit = (Double) userData[2];
                returnedData[0] = idUser;
                returnedData[1] = balance.toString();
                returnedData[2] = totalDeposit.toString();
            } else {
                LogUtil.logError("User", "Get Balance Error, Username {} not found", username);
            }
        } catch (SQLException e) {
            LogUtil.logError("User", "Get Balance User Error from database: {}", e.getMessage());
        }

        return returnedData;
    }

    protected static String[] getBalanceByID(Connection connection, String ID) {
        String[] returnedData = new String[3];

        try {
            Object[] userData = Utils.getDataFromTableUser(connection, "ID", ID);

            if (userData.length > 0 && userData[0] != null) {
                String idUser = (String) userData[0];
                Double balance = (Double) userData[1];
                Double totalDeposit = (Double) userData[2];
                returnedData[0] = idUser;
                returnedData[1] = balance.toString();
                returnedData[2] = totalDeposit.toString();
            } else {
                LogUtil.logError("User", "Get Balance Error, ID {} not found", ID);
            }
        } catch (SQLException e) {
            LogUtil.logError("User", "Get Balance Error from database: {}", e.getMessage());
        }

        return returnedData;
    }

    protected static String commandWorld(Connection connection) {
        String returnedData = null;

        try {
            Object[] worldData = Utils.getDataFromTableWorld(connection);

            if (worldData.length > 0 && worldData[0] != null) {
                String world = (String) worldData[0];
                String owner = (String) worldData[1];
                String guard = (String) worldData[2];
                returnedData = "```" + "WORLD: " + world + "\nOWNER: " + owner +"\nBOT: " + guard + "```";
            }
        } catch (SQLException e) {
            LogUtil.logError("World", "Getting Data World Error from database: {}", e.getMessage());
        }

        return returnedData;
    }

    protected static boolean isDuplicateUsername(Connection connection, String authorID, String username) {
        try {
            Object[] userData = Utils.getDataFromTableUser(connection, "USERNAME", username);

            if (userData.length > 0 && userData[0] != null) {
                String ID = (String) userData[0];
                return !authorID.equals(ID);
            }
        } catch (SQLException e) {
            LogUtil.logError("User", "Getting data username failed, message from database: {}", e.getMessage());
            return true;
        }
        return false;
    }

    protected static boolean addStockToTableInformation(Connection connection, String idProduct, String nameProduct) {
        try {
            Utils.insertProductData(connection,idProduct,nameProduct);
            Utils.increaseStock(connection, "INFORMATION", idProduct, 1);
            return true;
        } catch (SQLException e) {
            LogUtil.logError("Product", "Add stock to a product is failed, message from database: {}", e.getMessage());
        }
        return false;
    }

    public static String getStrContent(Object[][] objInformation, String emojiLine, String emojiArrow, String emojiCurrency) {
        String strContent = "";

        for (Object[] informationData : objInformation) {
            String idProduct = (String) informationData[0];
            int stock = (int) informationData[1];
            double price = (double) informationData[2];
            String description = (String) informationData[3];
            strContent += emojiLine + "\n" +
                    emojiArrow + " ID Product: " + idProduct.toUpperCase() + "\n" +
                    emojiArrow + " Stock: " + stock + "\n" +
                    emojiArrow + " Price: " + price + " " + emojiCurrency + "\n" +
                    emojiArrow + " Description: " + description + "\n";
        }
        return strContent;
    }

    protected static String getStrHelpCommand(boolean isAdmin, String prefix) {
        String strPublic = " **Step to buy product**\n" +
                "```\n" +
                "1. Use command " + prefix + "stock to see stock of product\n" +
                "2. Use command " + prefix + "setuser to set your in-game name.\n" +
                "3. Use command " + prefix + "checkuser to verify your in-game name.\n" +
                "4. Use command " + prefix + "world to view current deposit world.\n" +
                "   (WARNING: Don't forget to check bot is online!)\n" +
                "5. Use command " + prefix + "balance to see your balance\n" +
                "6. To buy product use command " + prefix + "buy <product> <count>\n" +
                "\n" +
                "If there is any problems, please tell admin/owner```";

        String strAdmin = " **Command Administrator**\n" +
                "```\n" +
                "- " + prefix + "addProduct <product_id> <price>\n" +
                "- " + prefix + "addStock <product_id>\n" +
                "  (WARNING: You must upload file attachment extension .txt)\n" +
                "- " + prefix + "deleteProduct <product_id>\n" +
                "- " + prefix + "changePrice <product_id> <new_price>\n" +
                "- " + prefix + "setDescription <product_id> <description>\n" +
                "- " + prefix + "setWorld <world> <owner> <bot>\n" +
                "- " + prefix + "send <user> <product_id> <count>\n" +
                "- " + prefix + "addBal <user> <balance>\n" +
                "- " + prefix + "reduceBal <user> <balance>```\n" +
                "\n" +
                " **Command Public**\n" +
                "```\n" +
                "- " + prefix + "setuser <growid>\n" +
                "- " + prefix + "checkuser\n" +
                "- " + prefix + "world\n" +
                "- " + prefix + "balance\n" +
                "- " + prefix + "stock\n" +
                "- " + prefix + "buy <product> <count>```\n";

        return isAdmin ? strAdmin : strPublic;
    }

    public static MessageEmbed createEmbed(String title, String description, String bannerUrl, String strTime) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(title);
        embedBuilder.setDescription(description);
        if (!bannerUrl.isEmpty()) {
            embedBuilder.setImage(bannerUrl);
        }
        if (!strTime.isEmpty()){
            embedBuilder.setFooter(strTime);
        }
        return embedBuilder.build();
    }



}
