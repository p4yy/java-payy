package discord.utils;

import core.Config;
import database.Transaction;
import database.Utils;
import logger.LogUtil;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.sharding.ShardManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class UtilsCommand {

    public static void help(SlashCommandInteractionEvent event, boolean isAdmin, String prefix) {
        String str = Helper.getStrHelpCommand(isAdmin, prefix);
        event.reply(str)
                .setEphemeral(true)
                .queue();
    }

    public static void setUser(Connection connection, SlashCommandInteractionEvent event, String authorID, String username, boolean isUsePostgreSQL) {
        try {
            LogUtil.logInfo("DiscordCommand", "Set User Command");
            boolean isDuplicate = Helper.isDuplicateUsername(connection, authorID, username);
            if (isDuplicate) {
                event.reply("Error username **" + username + "** has already been registered by someone else.")
                        .setEphemeral(true)
                        .queue();
            } else {
                Utils.insertUserData(connection, authorID, 0.0, 0.0, username, isUsePostgreSQL);
                event.reply("Successfully **updated your username** to **" + username + "**")
                        .setEphemeral(true)
                        .queue();
            }
        } catch (SQLException e) {
            event.reply("Something wrong! please contact admin")
                    .setEphemeral(true)
                    .queue();
            LogUtil.logError("DiscordCommand", "Set User error because SQLException, message from database {}", e.getMessage());
        }
    }

    public static void checkUserAndBalance(Connection connection, SlashCommandInteractionEvent event, String command, String authorID, String emojiCurrency) {
        LogUtil.logInfo("DiscordCommand", "Check user and balance");
        String text;
        if (command.equals("checkuser")) {
            text = "Your current username is ";
            emojiCurrency = "";
        } else {
            text = "You have ";
        }
        String result = Helper.commandUser(connection, command, authorID);
        if (result != null) {
            event.reply(text + result + " " + emojiCurrency)
                    .setEphemeral(true)
                    .queue();
        } else {
            event.reply("You are not registered, please use /setuser")
                    .setEphemeral(true)
                    .queue();
        }
    }

    public static void showWorldInformation(Connection connection, SlashCommandInteractionEvent event) {
        try {
            LogUtil.logInfo("DiscordCommand", "Show World Information");
            String s = Helper.commandWorld(connection);
            event.reply(s)
                    .setEphemeral(true)
                    .queue();
        } catch (Exception e) {
            LogUtil.logError("DiscordCommand", "Error when getting data world from database");
            event.reply("Something went wrong while fetching world information.")
                    .setEphemeral(true)
                    .queue();
        }
    }

    public static void showStock(Connection connection, SlashCommandInteractionEvent event, String emojiLine, String emojiArrow, String bannerUrl, String emojiCurrency) {
        try {
            Object[][] objInformation = Utils.getDataFromTableInformation(connection);
            String strContent = Helper.getStrContent(objInformation, emojiLine, emojiArrow, emojiCurrency);
            MessageEmbed embed = Helper.createEmbed("Stock Information", strContent, bannerUrl, "");
            event.replyEmbeds(embed)
                    .setEphemeral(true)
                    .queue();
        } catch (Exception e) {
            event.reply("Error occurred")
                    .setEphemeral(true)
                    .queue();
        }
    }

    public static void buy(Connection connection, SlashCommandInteractionEvent event, ShardManager shardManager, String authorID, String productID, String strCount, Config config) {
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
                event.reply("Invalid quantity provided!")
                        .setEphemeral(true)
                        .queue();
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
                                try {
                                    event.getUser().openPrivateChannel().queue(privateChannel -> {
                                        privateChannel.sendMessage("Here's your " + count + " items of product " + productID + "\nYour current balance: " + currentBalance + " " + emojiCurrency)
                                                .addFile(contentBytes, count + productID + ".txt")
                                                .queue();
                                    });
                                    event.reply("Please check your dm").setEphemeral(true).queue();
                                } catch (Exception e){
                                    event.reply("Can't send you a dm, please enable your dm setting, and chat admin to get your items").setEphemeral(true).queue();
                                }
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
                                event.reply("Something wrong when decrementing user balance, please contact admin.")
                                        .setEphemeral(true)
                                        .queue();
                            }
                        } else {
                            event.reply("Transaction failed!")
                                    .setEphemeral(true)
                                    .queue();
                        }
                    } else {
                        event.reply("You don't have enough balance to buy\nBalance : " + balance + " " + emojiCurrency)
                                .setEphemeral(true)
                                .queue();
                    }
                } else {
                    event.reply("Product is out of stock")
                            .setEphemeral(true)
                            .queue();
                }
            } else {
                event.reply("Product with ID " + productID + " not found.")
                        .setEphemeral(true)
                        .queue();
            }
        } catch (SQLException | NumberFormatException e) {
            event.reply("Something went wrong!")
                    .setEphemeral(true)
                    .queue();
            LogUtil.logError("DiscordCommand", "Something wrong with the database");
        }
    }










}
