package discord.utils;

import database.Utils;

import java.sql.Connection;
import java.sql.SQLException;

public class Helper {

    protected static String commandUser(Connection connection, String command, String idUser) {
        String returnedData = null;

        try {
            Object[] userData = Utils.getDataFromTableUser(connection, "ID", idUser);

            if (userData.length > 0 && userData[0] != null) {
                Double balance = (Double) userData[1];
                String username = (String) userData[2];
                if (command.equals("checkuser")) {
                    returnedData = username;
                } else if (command.equals("balance")) {
                    returnedData = balance.toString();
                }


            } else {
                System.out.println("User with ID " + idUser + " not found");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return returnedData;
    }

    protected static String commandWorld(Connection connection, String command) {
        String returnedData = null;

        try {
            Object[] worldData = Utils.getDataFromTableWorld(connection);

            if (worldData.length > 0 && worldData[0] != null) {
                String world = (String) worldData[0];
                String guard = (String) worldData[1];
                if (command.equals("world")) {
                    returnedData = world;
                } else if (command.equals("bot")) {
                    returnedData = guard;
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
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
            System.out.println(e.getMessage());
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
            System.out.println(e.getMessage());
        }
        return false;
    }

    protected static String getStrContent(Object[][] objInformation, String emojiLine, String emojiArrow) {
        String strContent = "";

        for (Object[] informationData : objInformation) {
            String idProduct = (String) informationData[0];
            int stock = (int) informationData[1];
            double price = (double) informationData[2];
            String description = (String) informationData[3];
            strContent += emojiLine + "\n" +
                    emojiArrow + " ID Product: " + idProduct + "\n" +
                    emojiArrow + " Stock: " + stock + "\n" +
                    emojiArrow + " Price: " + price + "\n" +
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
                "5. Use command " + prefix + "bot to see the owner of the bot.\n" +
                "   (WARNING: Don't forget to check bot is online!)\n" +
                "6. Use command " + prefix + "balance to see your balance\n" +
                "7. To buy product use command " + prefix + "buy <product> <count>\n" +
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
                "- " + prefix + "setWorld <world>\n" +
                "- " + prefix + "setBot <name>\n" +
                "- " + prefix + "addBalByID <id_discord> <balance>\n" +
                "- " + prefix + "addBalByGrowid <id_discord> <balance>\n" +
                "- " + prefix + "reduceBalByID <id_discord> <balance>\n" +
                "- " + prefix + "reduceBalByGrowid <id_discord> <balance>```\n" +
                "\n" +
                " **Command Public**\n" +
                "```\n" +
                "- " + prefix + "setuser <growid>\n" +
                "- " + prefix + "checkuser\n" +
                "- " + prefix + "world\n" +
                "- " + prefix + "bot\n" +
                "- " + prefix + "balance\n" +
                "- " + prefix + "buy <product> <count>```\n";

        return isAdmin ? strAdmin : strPublic;
    }



}
