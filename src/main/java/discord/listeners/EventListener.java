package discord.listeners;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.jetbrains.annotations.NotNull;

import discord.utils.UtilsDiscord;

import java.sql.Connection;
import java.util.List;


public class EventListener extends ListenerAdapter {
    private final String prefix;
    private final List<String> adminIDs;
    private final Connection connection;
    private final ShardManager shardManager;
    private final String idChannelHistory;
    private final String idChannelDonationHistory;
    private final String emojiCurrency;
    private final String emojiLine;
    private final String emojiArrow;
    private final String bannerUrlStock;
    private final String bannerUrlPurchase;
    private final String gmtTime;
    private final String guildID;
    private final String channelIdAdminLogsPurchaseSetting;
    private final boolean isUselogsPurchaseSetting;
    private final boolean isPostgreSQL;

    public EventListener
            (
                Connection connection,
                ShardManager shardManager,
                String idChannelHistory,
                String idChannelDonationHistory,
                String prefix,
                List<String> adminIDs,
                String emojiCurrency,
                String emojiLine,
                String emojiArrow,
                String bannerUrlStock,
                String bannerUrlPurchase,
                String gmtTime,
                String guildID,
                String channelIdAdminLogsPurchaseSetting,
                boolean isUselogsPurchaseSetting,
                boolean isPostgreSQL
            )
    {
        this.connection = connection;
        this.shardManager = shardManager;
        this.idChannelHistory = idChannelHistory;
        this.idChannelDonationHistory = idChannelDonationHistory;
        this.prefix = prefix;
        this.adminIDs = adminIDs;
        this.emojiCurrency = emojiCurrency;
        this.emojiLine = emojiLine;
        this.emojiArrow = emojiArrow;
        this.bannerUrlStock = bannerUrlStock;
        this.bannerUrlPurchase = bannerUrlPurchase;
        this.gmtTime = gmtTime;
        this.guildID = guildID;
        this.channelIdAdminLogsPurchaseSetting = channelIdAdminLogsPurchaseSetting;
        this.isUselogsPurchaseSetting = isUselogsPurchaseSetting;
        this.isPostgreSQL = isPostgreSQL;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.isFromGuild()) {
            // Check if the message is from a guild channel
            // only admins can send message in guild channel
            if (!adminIDs.contains(event.getAuthor().getId())){
                return; // Exit the method if the message is from a guild channel and not from admins
            }
        }

        String message = event.getMessage().getContentRaw();
        String authorID = event.getAuthor().getId();


        String cmdMessage = message.replaceFirst(prefix,"");
        String lowerCase = cmdMessage.toLowerCase();

        // Check if the message starts with the defined prefix and if the author is an admin
        if (message.startsWith(prefix) && adminIDs.contains(authorID)) {
            String[] command_admin = lowerCase.split(" ");
            String[] rawMessage = cmdMessage.split(" ");

            if ((command_admin[0].equals("help") || command_admin[0].equals("helppublic")) && command_admin.length == 1) {
                boolean isAdmin = command_admin[0].equals("help");
                UtilsDiscord.help(event, isAdmin, prefix);
                return;
            } else if (command_admin[0].equals("addstock") && command_admin.length == 2) {
                UtilsDiscord.addStock(connection, event, command_admin[1]);
            } else if (command_admin[0].equals("addproduct") && command_admin.length == 3) {
                String product = command_admin[1];
                String strPrice = command_admin[2];
                UtilsDiscord.addProduct(connection, event, product, strPrice, isPostgreSQL);
            } else if (command_admin[0].equals("deleteproduct") && command_admin.length == 2){
                String idProduct = command_admin[1];
                UtilsDiscord.deleteProduct(connection, event, idProduct);
            } else if ((command_admin[0].equals("addbal") || command_admin[0].equals("reducebal")) && command_admin.length == 3) {
                String cmd = command_admin[0];
                String s = command_admin[1];
                String strCount = command_admin[2];
                double count = Double.parseDouble(strCount);
                if (cmd.equals("addbal")) {
                    UtilsDiscord.addUserBalance(connection, event, shardManager, idChannelDonationHistory, s, count, emojiCurrency, isPostgreSQL, true);
                } else {
                    UtilsDiscord.decrementUserBalance(connection, event, s, count, isPostgreSQL);
                }
            } else if (command_admin[0].equals("setworld") && command_admin.length == 4) {
                String world = rawMessage[1];
                String username = rawMessage[2];
                String guard = rawMessage[3];
                UtilsDiscord.setWorld(connection, event, world, username, guard);
            } else if (command_admin[0].equals("changeprice") && command_admin.length == 3) {
                String product = command_admin[1];
                String strPrice = command_admin[2];
                UtilsDiscord.changePrice(connection, event, product, strPrice);
            } else if (command_admin[0].equals("setdescription") && command_admin.length >= 3) {
                StringBuilder s = new StringBuilder();
                String product = command_admin[1];
                for (int i = 2; i < rawMessage.length ; i++) {
                    s.append(rawMessage[i]).append(" ");
                }
                UtilsDiscord.setDescription(connection, event, product, String.valueOf(s));
            } else if (command_admin[0].equals("send") && command_admin.length == 4) {
                String s = command_admin[1];
                String product = command_admin[2];
                String strCount = command_admin[3];
                if (s.startsWith("<@") && s.endsWith(">")) {
                    String receiverID = s.replaceAll("[^0-9]", "");
                    UtilsDiscord.send(
                            connection,
                            event,
                            shardManager,
                            idChannelHistory,
                            authorID,
                            product,
                            strCount,
                            bannerUrlPurchase,
                            emojiCurrency,
                            emojiArrow,
                            gmtTime,
                            receiverID,
                            guildID,
                            isUselogsPurchaseSetting,
                            isPostgreSQL,
                            channelIdAdminLogsPurchaseSetting
                    );
                }
            }
        }

        // public
        if (message.startsWith(prefix)) {
            String[] command = lowerCase.split(" ");
            if (command[0].equals("help") && command.length == 1) {
                UtilsDiscord.help(event, false, prefix);
            } else if (command[0].equals("world") && command.length == 1) {
                UtilsDiscord.showWorldInformation(connection, event);
            } else if (command[0].equals("setuser") && command.length == 2) {
                String username = command[1];
                UtilsDiscord.setUser(connection, event, authorID, username, isPostgreSQL);
            } else if ((command[0].equals("checkuser") || command[0].equals("balance") ) && command.length == 1) {
                String cmd = command[0];
                UtilsDiscord.checkUserAndBalance(connection, event, cmd ,authorID, emojiCurrency);
            } else if (command[0].equals("stock") && command.length == 1) {
                UtilsDiscord.showStock(connection, event, emojiLine, emojiArrow, bannerUrlStock, emojiCurrency);
            } else if (command[0].equals("buy") && command.length == 3) {
                String idProduct = command[1];
                String strCount = command[2];
                UtilsDiscord.buy(connection,
                        event, shardManager,
                        idChannelHistory,
                        authorID,
                        idProduct,
                        strCount,
                        bannerUrlPurchase,
                        emojiCurrency,
                        emojiArrow,
                        gmtTime,
                        isUselogsPurchaseSetting,
                        isPostgreSQL,
                        channelIdAdminLogsPurchaseSetting
                );
            }
        }
    }
}
