package discord.utils;

import core.Config;
import database.Transaction;
import database.Utils;

import logger.LogUtil;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.sharding.ShardManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;


public class UtilsDiscord {

    public static void setUser(Connection connection, MessageReceivedEvent event, String authorID, String username, boolean isUsePostgreSQL) {
        try{
            LogUtil.logInfo("DiscordCommand", "Set User Command");
            boolean isDuplicate= Helper.isDuplicateUsername(connection, authorID, username);
            if (isDuplicate) {
                event.getChannel().sendMessage("Error username **" + username + "** has already been registered by someone else.").queue();
            } else {
                Utils.insertUserData(connection, authorID, 0.0, 0.0, username, isUsePostgreSQL);
                event.getChannel().sendMessage("Successfully **updated your username** to **" + username + "**").queue();
            }
        } catch (SQLException e) {
            event.getChannel().sendMessage("Something wrong! please contact admin").queue();
            LogUtil.logError("DiscordCommand", "Set User error because SQLException, message from database {}", e.getMessage());
        }
    }

    public static void addUserBalance(
            Connection connection,
            MessageReceivedEvent event,
            ShardManager shardManager,
            String idChannelHistoryDonation,
            String authorID,
            double totalAddBalance,
            String emojiCurrency,
            boolean isUsePostgreSQL,
            boolean isAdmin
    ) {
        if (totalAddBalance <= 0) {
            event.getChannel().sendMessage("Invalid quantity provided!").queue();
            return;
        }
        try {
            LogUtil.logInfo("DiscordCommand", "Add balance user");
            boolean isTag = false;
            if (authorID.startsWith("<@") && authorID.endsWith(">")) {
                authorID = authorID.replaceAll("[^0-9]", "");
                isTag = true;
            }
            boolean isValid;
            String user;
            if (isTag) {
                isValid = Utils.addBalanceUserByID(connection, authorID, totalAddBalance, isUsePostgreSQL);
                user = "<@" + authorID + ">";
            } else {
                isValid = Utils.addBalanceUserByUsername(connection, authorID, totalAddBalance, isUsePostgreSQL);
                user = authorID;
            }
            if (isValid) {
                String[] dataBalance;
                if (isTag) dataBalance = Helper.getBalanceByID(connection, authorID);
                else dataBalance = Helper.getBalanceByUsername(connection, authorID);
                String strCurrentBalance = "\nBalance now: " + dataBalance[1] + " " + emojiCurrency;
                String strTotalDeposit = "\nTotal deposit: " + dataBalance[2] + " " + emojiCurrency;
                event.getChannel().sendMessage("Success to increment " + totalAddBalance + " balance of user " + user + " " + strCurrentBalance + strTotalDeposit).queue();
                TextChannel channel = shardManager.getTextChannelById(idChannelHistoryDonation);
                if (channel != null) {
                    String t = "Success to increment " + totalAddBalance + " balance <@" + dataBalance[0] +"> " + strCurrentBalance + strTotalDeposit;
                    if (isAdmin) {
                        t = "Success to increment " + totalAddBalance + " balance via admin to user " + user + " " + strCurrentBalance + strTotalDeposit;
                    }
                    channel.sendMessage(t).queue();
                    Utils.updateDataTableHistory(connection, 0, totalAddBalance, 0, totalAddBalance, isUsePostgreSQL);
                }
            } else {
                event.getChannel().sendMessage("Failed to increment balance of user <@" + authorID + ">").queue();
            }
        } catch (SQLException e) {
            LogUtil.logError("DiscordCommand", "Failed to increment balance of a user because SQLException {}", e.getMessage());
            event.getChannel().sendMessage("Failed to increment balance of user").queue();
        }
    }

    public static void decrementUserBalance(
            Connection connection,
            MessageReceivedEvent event,
            String authorID,
            double count,
            boolean isUsePostgreSQL
    ) {
        if (count <= 0) {
            event.getChannel().sendMessage("Invalid quantity provided!").queue();
            return;
        }
        try {
            boolean isTag = false;
            String[] userData;
            if (authorID.startsWith("<@") && authorID.endsWith(">")) {
                authorID = authorID.replaceAll("[^0-9]", "");
                userData = Helper.getBalanceByID(connection, authorID);
                isTag = true;
            } else userData = Helper.getBalanceByUsername(connection, authorID);

            LogUtil.logInfo("DiscordCommand", "Reduce balance user");

            String strCurrentBalance = userData[1];
            double currBalance = Double.parseDouble(strCurrentBalance);
            if (currBalance <= 0) {
                event.getChannel().sendMessage("Error to decrement user, because balance is 0").queue();
            } else {
                boolean isValid;
                String user;
                if (isTag) {
                    isValid = Utils.decrementBalanceUserByID(connection, count, authorID, isUsePostgreSQL);
                    user = "<@" + authorID + ">";
                } else {
                    isValid = Utils.decrementBalanceUserByUsername(connection, count, authorID, isUsePostgreSQL);
                    user = authorID;
                }
                if (isValid) {
                    double finalBalance = currBalance - count;
                    event.getChannel().sendMessage("Success to decrement balance of user " + user + " \nBalance now: " + finalBalance).queue();
                    Utils.updateDataTableHistory(connection, 0, 0, count, -count, isUsePostgreSQL);
                } else {
                    LogUtil.logError("DiscordCommand", "Failed to decrement balance of a user");
                    event.getChannel().sendMessage("Failed to decrement balance of user").queue();
                }
            }
        } catch (SQLException e) {
            event.getChannel().sendMessage("Failed to decrement balance of user").queue();
            LogUtil.logError("DiscordCommand", "Failed to decrement balance of a user because SQLException {}", e.getMessage());
        }
    }

    public static void checkUserAndBalance(Connection connection, MessageReceivedEvent event, String command, String authorID, String emojiCurrency) {
        LogUtil.logInfo("DiscordCommand", "Check user and balance");
        String text;
        if (command.equals("checkuser")) {
            text = "Your current username is ";
            emojiCurrency = "";
        } else {
            text = "You have ";
        }
        String result = Helper.commandUser(connection, command ,authorID);
        if (result != null) {
            event.getChannel().sendMessage(text + result + " " + emojiCurrency).queue();
        } else {
            event.getChannel().sendMessage("You are not registered, please use !setuser").queue();
        }
    }

    public static void addProduct(Connection connection, MessageReceivedEvent event, String product, String strPrice, boolean isPostgreSQL) {
        try {
            LogUtil.logInfo("DiscordCommand", "Add product {}", product);
            double price = Double.parseDouble(strPrice);
            Utils.createTableProducts(connection, product, isPostgreSQL);
            Utils.insertInformationData(connection, "INFORMATION", product, 0, price, "");
            event.getChannel().sendMessage("Success add product " + product).queue();
        } catch (SQLException | NumberFormatException e) {
            event.getChannel().sendMessage("Failed add product " + product).queue();
            LogUtil.logError("DiscordCommand", "Failed add product {}", product);
        }
    }

    public static void showStock(Connection connection, MessageReceivedEvent event, String emojiLine, String emojiArrow, String bannerUrl, String emojiCurrency) {
        try {
            Object[][] objInformation = Utils.getDataFromTableInformation(connection);
            String strContent = Helper.getStrContent(objInformation, emojiLine, emojiArrow, emojiCurrency);
            MessageEmbed embed = Helper.createEmbed("Stock Information", strContent, bannerUrl, "");
            event.getChannel().sendMessageEmbeds(embed).queue();
        } catch (Exception e) {
            event.getChannel().sendMessage("Error occurred").queue();
        }
    }

    public static void changePrice(Connection connection, MessageReceivedEvent event, String product, String strPrice) {
        try {
            LogUtil.logInfo("DiscordCommand", "Change price {}", product);
            Double price = Double.parseDouble(strPrice);
            Utils.updateDataField(connection, "INFORMATION", "PRICE", price, "ID_PRODUCT", product);
            event.getChannel().sendMessage("Success change price product " + product + " into " + strPrice).queue();
        } catch (SQLException e) {
            LogUtil.logError("DiscordCommand", "Failed change price product {}, message from database: {}", product, e.getMessage());
            event.getChannel().sendMessage("Failed change price product " + product).queue();
        }
    }

    public static void setDescription(Connection connection, MessageReceivedEvent event, String product, String description) {
        try{
            LogUtil.logInfo("DiscordCommand", "Set Description product {}", product);
            Utils.updateDataField(connection, "INFORMATION", "DESCRIPTION", description, "ID_PRODUCT", product);
            event.getChannel().sendMessage("Success set description for product " + product).queue();
        } catch (SQLException e) {
            LogUtil.logError("DiscordCommand", "Failed to set description for product {}", product);
            event.getChannel().sendMessage("Failed to set description for product " + product).queue();
        }
    }
    
    public static void setWorld(Connection connection, MessageReceivedEvent event, String world, String username, String guard) {
        try {
            LogUtil.logInfo("DiscordCommand", "Set World");
            boolean isValidChangeDataWorld = Utils.changeDataWorld(connection, world, username, guard);
            if (isValidChangeDataWorld) {
                String strText = world + " " + username + " " + guard;
                event.getChannel().sendMessage("Success set deposit world into " + strText).queue();
            } else {
                event.getChannel().sendMessage("Failed to set deposit world").queue();
            }
        } catch (SQLException e) {
            LogUtil.logError("DiscordCommand", "Failed to set world");
            event.getChannel().sendMessage("Failed to set deposit world").queue();
        }
    }

    public static void showWorldInformation(Connection connection, MessageReceivedEvent event) {
        try {
            LogUtil.logInfo("DiscordCommand", "Show World Information");
            String s = Helper.commandWorld(connection);
            event.getChannel().sendMessage(s).queue();
        } catch (Exception e) {
            LogUtil.logError("DiscordCommand", "Error when get data world from database");
        }
    }

    public static void help(MessageReceivedEvent event, boolean isAdmin, String prefix) {
        String str = Helper.getStrHelpCommand(isAdmin, prefix);
        event.getChannel().sendMessage(str).queue();
    }

    public static void deleteProduct(Connection connection, MessageReceivedEvent event, String idProduct) {
        if (idProduct.equalsIgnoreCase("user")) {
            return;
        }
        try {
            LogUtil.logInfo("DiscordCommand", "Delete Product {}", idProduct);
            boolean isValidDeleteTable = Utils.deleteTable(connection, idProduct);
            boolean isValidDeleteProductInfo = Utils.deleteProductInformation(connection, idProduct);
            if (isValidDeleteTable && isValidDeleteProductInfo) {
                event.getChannel().sendMessage("Success delete product " + idProduct).queue();
            } else  {
                event.getChannel().sendMessage("Failed delete product " + idProduct).queue();
            }
        } catch (SQLException e) {
            LogUtil.logError("DiscordCommand", "Failed Delete Product {}", idProduct);
            event.getChannel().sendMessage("Failed delete product " + idProduct).queue();
        }
    }

    public static void addStock(Connection connection, MessageReceivedEvent event, String product) {
        Message msg = event.getMessage();
        List<Message.Attachment> attachments = msg.getAttachments();

        if (!attachments.isEmpty()) {
            try {
                final int[] totalAddStock = {0};

                for (Message.Attachment attachment : attachments) {
                    String fileName = attachment.getFileName();
                    if (fileName.endsWith(".txt")) {
                        attachment.retrieveInputStream().thenAccept(inputStream -> {
                            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                                String line;

                                while ((line = reader.readLine()) != null) {
                                    boolean isSuccessAddStock = Helper.addStockToTableInformation(connection, product, line);
                                    if (isSuccessAddStock) {
                                        totalAddStock[0] += 1;
                                    }
                                }

                                event.getChannel().sendMessage("Success add " + totalAddStock[0] + " item into product " + product).queue();
                            } catch (IOException e) {
                                event.getChannel().sendMessage("Error reading the file").queue();
                                LogUtil.logError("DiscordCommand", "Error reading the file, when try to add stock");
                            }
                        });
                    } else {
                        event.getChannel().sendMessage("Please upload a text file").queue();
                    }
                }
            } catch (Exception e){
                LogUtil.logError("DiscordCommand", "Exception happen, {}", e.getMessage());
                event.getChannel().sendMessage("Error!. Something wrong when adding stock").queue();
            }
        } else {
            event.getChannel().sendMessage("No file attached").queue();
        }
    }

    // transaction buy
    public static void buy(Connection connection, MessageReceivedEvent event, ShardManager shardManager, String authorID, String productID, String strCount, Config config) {
        String idChannelHistory = config.getChannelIdHistory();
        String urlBanner = config.getBannerUrlPurchase();
        String emojiCurrency = config.getEmojiCurrency();
        String emojiArrow = config.getEmojiArrow();
        String gmt = config.getGmtTime();
        boolean isUselogsPurchaseSetting = config.isUselogsPurchaseSetting();
        boolean isPostgreSQL = config.isPostgreSQL();
        String channelIdAdminLogsPurchaseSetting = config.getChannelIdAdminLogsPurchaseSetting();
        try {
            int count = Integer.parseInt(strCount);
            if (count <= 0) {
                event.getChannel().sendMessage("Invalid quantity provided!").queue();
                return;
            }

            double balance = Double.parseDouble(Helper.commandUser(connection, "balance", authorID));

            Object[] objInformation = Utils.getDataFromTableInformationWithID(connection, productID);

            if (objInformation.length > 0 && objInformation[0] != null && objInformation[1] != null) {
                LogUtil.logInfo("CommandDiscord", "Buy product {}", productID);
                int stock = (int) objInformation[0];
                Double price = (Double) objInformation[1];
                if (stock >= count) {
                    double finalPrice = count * price;
                    double currentBalance = balance - finalPrice;
                    if (balance >= finalPrice) {
                        List<String> transaction = Transaction.buyProducts(connection, productID, "INFORMATION", productID, count, finalPrice, false, isPostgreSQL);
                        if (!transaction.isEmpty()) {
                            boolean decrementBalance = Utils.decrementBalanceUserByID(connection, finalPrice, authorID, isPostgreSQL);
                            if (decrementBalance) {
                                StringBuilder textContent = new StringBuilder();
                                for (String data : transaction) {
                                    textContent.append(data).append("\n");
                                }

                                byte[] contentBytes = textContent.toString().getBytes();
                                event.getChannel().sendMessage("Here's your " + count + " items of product " + productID + " \nYour current balance: " + currentBalance + " " + emojiCurrency)
                                        .addFile(contentBytes, count + productID + ".txt")
                                        .queue();
                                TextChannel channel = shardManager.getTextChannelById(idChannelHistory);
                                if (channel != null) {
                                    try {
                                        String[] dataHistory = Helper.getDataTableHistory(connection);
                                        String orderID = dataHistory[0];
                                        String strTime = Helper.getStrTime(gmt);
                                        String purchaseContent = " Order ID: # **" + orderID + "**\n" +
                                                emojiArrow + " Buyer: <@" + authorID + "> \n" +
                                                emojiArrow + " Product ID: " + productID.toUpperCase() +"\n" +
                                                emojiArrow + " Total: " + count + "\n" +
                                                emojiArrow + " Price: " + finalPrice +
                                                " " + emojiCurrency;
                                        MessageEmbed embed = Helper.createEmbed("**Purchase History**", purchaseContent, urlBanner, strTime);
                                        channel.sendMessageEmbeds(embed).queue();
                                    } catch (Exception e) {
                                        LogUtil.logError("DiscordCommand", "Exception happen, {}", e.getMessage());
                                    }
                                } else {
                                    LogUtil.logError("DiscordCommand", "Buy command error, because channel history not found");
                                }
                                if (isUselogsPurchaseSetting) {
                                    TextChannel channelOnlyAdminLogsSetting = shardManager.getTextChannelById(channelIdAdminLogsPurchaseSetting);
                                    if (channelOnlyAdminLogsSetting != null) {
                                        try {
                                            channelOnlyAdminLogsSetting.sendMessage("<@" + authorID + "> bought " + count + " items of product " + productID)
                                                    .addFile(contentBytes, count + productID + ".txt")
                                                    .queue();
                                        } catch (Exception e) {
                                            LogUtil.logError("DiscordCommand", "Exception happen: {}", e.getMessage());
                                        }
                                    }
                                }
                            } else {
                                event.getChannel().sendMessage("Something wrong when decrement balance user, please tell admin").queue();
                            }
                        } else {
                            event.getChannel().sendMessage("Transaction failed!").queue();
                        }
                    } else {
                        event.getChannel().sendMessage("You don't have enough balance to buy\nBalance : " + balance + " " + emojiCurrency).queue();
                    }
                } else {
                    event.getChannel().sendMessage("Product is out of stock").queue();
                }
            } else {
                event.getChannel().sendMessage("Product with ID " + productID + " not found.").queue();
            }
        } catch (SQLException | NumberFormatException e) {
            event.getChannel().sendMessage("Something wrong!").queue();
            LogUtil.logError("DiscordCommand", "Something wrong with database");
        }
    }

    // transaction send
    public static void send (Connection connection, MessageReceivedEvent event, ShardManager shardManager, String authorID, String productID, String strCount, String receiverID, Config config) {
        String idChannelHistory = config.getChannelIdHistory();
        String urlBanner = config.getBannerUrlPurchase();
        String emojiCurrency = config.getEmojiCurrency();
        String emojiArrow = config.getEmojiArrow();
        String gmt = config.getGmtTime();
        String guildID = config.getGuildID();
        String channelIdAdminLogsPurchaseSetting = config.getChannelIdAdminLogsPurchaseSetting();
        boolean isUselogsPurchaseSetting = config.isUselogsPurchaseSetting();
        boolean isPostgreSQL = config.isPostgreSQL();
        try {
            int count = Integer.parseInt(strCount);
            if (count <= 0) {
                event.getChannel().sendMessage("Invalid quantity provided!").queue();
                return;
            }


            Object[] objInformation = Utils.getDataFromTableInformationWithID(connection, productID);

            if (objInformation.length > 0 && objInformation[0] != null && objInformation[1] != null) {
                int stock = (int) objInformation[0];
                Double price = (Double) objInformation[1];
                if (stock >= count) {
                    double finalPrice = count * price;
                    List<String> transaction = Transaction.buyProducts(connection, productID, "INFORMATION", productID, count, finalPrice, true, isPostgreSQL);
                    if (!transaction.isEmpty()) {
                        StringBuilder textContent = new StringBuilder();
                        for (String data : transaction) {
                            textContent.append(data).append("\n");
                        }

                        byte[] contentBytes = textContent.toString().getBytes();

                        Guild guild = shardManager.getGuildById(guildID);
                        if (guild != null) {
                            guild.retrieveMemberById(receiverID).queue(
                                    member -> {
                                        User user = member.getUser();
                                        user.openPrivateChannel().queue(privateChannel -> {
                                            privateChannel.sendMessage("Here's your " + count + " items of product " + productID + "\nGifted by: <@" + authorID + ">")
                                                    .addFile(contentBytes, count + productID + ".txt")
                                                    .queue();
                                        });
                                    },
                                    failure -> {
                                        LogUtil.logError("DiscordCommand", "Error when buy product, because user not found");
                                    }
                            );
                        } else {
                            LogUtil.logError("DiscordCommand", "Error when buy product, because guild not found");
                        }

                        TextChannel channel = shardManager.getTextChannelById(idChannelHistory);
                        if (channel != null) {
                            try {
                                String[] dataHistory = Helper.getDataTableHistory(connection);
                                String orderID = dataHistory[0];
                                String strTime = Helper.getStrTime(gmt);
                                String purchaseContent = " Order ID: # **" + orderID + "**\n" +
                                        emojiArrow + " Buyer: <@" + receiverID + "> \n" +
                                        emojiArrow + " Product ID: " + productID.toUpperCase() +"\n" +
                                        emojiArrow + " Total: " + count + "\n" +
                                        emojiArrow + " Price: " + finalPrice + " " + emojiCurrency + "\n" +
                                        emojiArrow + " Via send by <@" + authorID + ">";
                                MessageEmbed embed = Helper.createEmbed("**Purchase History**", purchaseContent, urlBanner, strTime);
                                channel.sendMessageEmbeds(embed).queue();
                            } catch (Exception e) {
                                LogUtil.logError("DiscordCommand", "Happen Exception");
                            }
                        } else {
                            LogUtil.logError("DiscordCommand", "Channel history purchase not found");
                        }

                        if (isUselogsPurchaseSetting) {
                            TextChannel channelHistoryOnlyAdmin = shardManager.getTextChannelById(channelIdAdminLogsPurchaseSetting);
                            if (channelHistoryOnlyAdmin != null) {
                                try {
                                    channelHistoryOnlyAdmin.sendMessage("<@" + authorID + "> give " + count + " items of product " + productID + " to: <@" + receiverID + ">")
                                            .addFile(contentBytes, count + productID + ".txt")
                                            .queue();
                                } catch (Exception e) {
                                    LogUtil.logError("DiscordCommand", "Exception happen: {}", e.getMessage());
                                }
                            }
                        }
                        event.getChannel().sendMessage("Success send " + count + " " + productID + " to <@" + receiverID + ">").queue();
                    } else {
                        event.getChannel().sendMessage("Transaction failed!").queue();
                    }
                } else {
                    event.getChannel().sendMessage("Product is out of stock").queue();
                }
            } else {
                event.getChannel().sendMessage("Product with ID " + productID + " not found.").queue();
            }
        } catch (SQLException | NumberFormatException e) {
            event.getChannel().sendMessage("Something wrong!").queue();
            LogUtil.logError("DiscordCommand","Something wrong");
        }
    }


}
