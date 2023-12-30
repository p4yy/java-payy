package discord.listeners;

import core.Config;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.jetbrains.annotations.NotNull;

import discord.utils.UtilsDiscord;

import java.sql.Connection;
import java.util.List;


public class EventListener extends ListenerAdapter {

    private final Connection connection;
    private final ShardManager shardManager;
    private final Config config;

    public EventListener(Connection connection, ShardManager shardManager, Config config) {
        this.connection = connection;
        this.shardManager = shardManager;
        this.config = config;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.isFromGuild()) {
            // Check if the message is from a guild channel
            // only admins can send message in guild channel
            if (!config.getAdminIds().contains(event.getAuthor().getId())){
                return; // Exit the method if the message is from a guild channel and not from admins
            }
        }

        String message = event.getMessage().getContentRaw();
        String authorID = event.getAuthor().getId();


        String cmdMessage = message.replaceFirst(config.getPrefix(),"");
        String lowerCase = cmdMessage.toLowerCase();

        // Check if the message starts with the defined prefix and if the author is an admin
        if (message.startsWith(config.getPrefix()) && config.getAdminIds().contains(authorID)) {
            String[] command_admin = lowerCase.split(" ");
            String[] rawMessage = cmdMessage.split(" ");

            if ((command_admin[0].equals("help") || command_admin[0].equals("helppublic")) && command_admin.length == 1) {
                boolean isAdmin = command_admin[0].equals("help");
                UtilsDiscord.help(event, isAdmin, config.getPrefix());
                return;
            } else if (command_admin[0].equals("addstock") && command_admin.length == 2) {
                UtilsDiscord.addStock(connection, event, command_admin[1]);
            } else if (command_admin[0].equals("addproduct") && command_admin.length == 3) {
                String product = command_admin[1];
                String strPrice = command_admin[2];
                UtilsDiscord.addProduct(connection, event, product, strPrice, config.isPostgreSQL());
            } else if (command_admin[0].equals("deleteproduct") && command_admin.length == 2){
                String idProduct = command_admin[1];
                UtilsDiscord.deleteProduct(connection, event, idProduct);
            } else if ((command_admin[0].equals("addbal") || command_admin[0].equals("reducebal")) && command_admin.length == 3) {
                String cmd = command_admin[0];
                String s = command_admin[1];
                String strCount = command_admin[2];
                double count = Double.parseDouble(strCount);
                if (cmd.equals("addbal")) {
                    UtilsDiscord.addUserBalance(connection, event, shardManager, config.getChannelIdDepositHistory(), s, count, config.getEmojiCurrency(), config.isPostgreSQL(), true);
                } else {
                    UtilsDiscord.decrementUserBalance(connection, event, s, count, config.isPostgreSQL());
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
                    UtilsDiscord.send(connection, event, shardManager, authorID, product, strCount, receiverID, config);
                }
            }
        }

        // public
        if (message.startsWith(config.getPrefix())) {
            String[] command = lowerCase.split(" ");
            if (command[0].equals("help") && command.length == 1) {
                UtilsDiscord.help(event, false, config.getPrefix());
            } else if (command[0].equals("world") && command.length == 1) {
                UtilsDiscord.showWorldInformation(connection, event);
            } else if (command[0].equals("setuser") && command.length == 2) {
                String username = command[1];
                UtilsDiscord.setUser(connection, event, authorID, username, config.isPostgreSQL());
            } else if ((command[0].equals("checkuser") || command[0].equals("balance") ) && command.length == 1) {
                String cmd = command[0];
                UtilsDiscord.checkUserAndBalance(connection, event, cmd ,authorID, config.getEmojiCurrency());
            } else if (command[0].equals("stock") && command.length == 1) {
                UtilsDiscord.showStock(connection, event, config.getEmojiLine(), config.getEmojiArrow(), config.getBannerUrlStock(), config.getEmojiCurrency());
            } else if (command[0].equals("buy") && command.length == 3) {
                String idProduct = command[1];
                String strCount = command[2];
                UtilsDiscord.buy(connection, event, shardManager, authorID, idProduct, strCount, config);
            }
        }
    }
}
