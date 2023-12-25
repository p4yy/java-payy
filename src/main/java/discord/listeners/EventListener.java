package discord.listeners;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import discord.utils.UtilsDiscord;

import java.sql.Connection;
import java.util.List;


public class EventListener extends ListenerAdapter {
    private final String prefix;
    private final List<String> adminIDs;

    private final Connection connection;

    private final String emojiWL;

    private final String emojiLine;
    private final String emojiArrow;

    public EventListener(Connection connection, String prefix, List<String> adminIDs, String emojiWL, String emojiLine, String emojiArrow) {
        this.connection = connection;
        this.prefix = prefix;
        this.adminIDs = adminIDs;
        this.emojiWL = emojiWL;
        this.emojiLine = emojiLine;
        this.emojiArrow = emojiArrow;
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

            if ((command_admin[0].equals("help") || command_admin[0].equals("helppublic")) && command_admin.length == 1) {
                boolean isAdmin = command_admin[0].equals("help");
                UtilsDiscord.help(event, isAdmin, prefix);
                return;
            } else if (command_admin[0].equals("addstock") && command_admin.length == 2) {
                UtilsDiscord.addStock(connection, event, command_admin[1]);
            } else if (command_admin[0].equals("addproduct") && command_admin.length == 3) {
                String product = command_admin[1];
                String strPrice = command_admin[2];
                UtilsDiscord.addProduct(connection, event, product, strPrice);
            } else if (command_admin[0].equals("deleteproduct") && command_admin.length == 2){
                String idProduct = command_admin[1];
                UtilsDiscord.deleteProduct(connection, event, idProduct);
            } else if ((command_admin[0].equals("addbalbyid") || command_admin[0].equals("reducebalbyid")) && command_admin.length == 3) {
                String idUsername = command_admin[1];
                String strCount = command_admin[2];
                double count = Double.parseDouble(strCount);
                if (command_admin[0].equals("addbalbyid")) {
                    UtilsDiscord.addUserBalanceByID(connection, event, idUsername, count);
                } else {
                    UtilsDiscord.decrementUserBalanceByID(connection, event, idUsername, count);
                }
            } else if ((command_admin[0].equals("addbalbygrowid") || command_admin[0].equals("reducebalbygrowid")) && command_admin.length == 3) {
                String growid = command_admin[1];
                String strCount = command_admin[2];
                double count = Double.parseDouble(strCount);
                if (command_admin[0].equals("addbalbygrowid")) {
                    UtilsDiscord.addUserBalanceByUsername(connection, event, growid, count);
                } else {
                    UtilsDiscord.decrementUserBalanceByUsername(connection, event, growid, count);
                }
            } else if ((command_admin[0].equals("setbot") || command_admin[0].equals("setworld")) && command_admin.length == 2) {
                String s = command_admin[1];
                if (command_admin[0].equals("setbot")) {
                    UtilsDiscord.setBot(connection, event, s);
                } else {
                    UtilsDiscord.setWorld(connection, event, s);
                }
            } else if (command_admin[0].equals("changeprice") && command_admin.length == 3) {
                String product = command_admin[1];
                String strPrice = command_admin[2];
                UtilsDiscord.changePrice(connection, event, product, strPrice);
            } else if (command_admin[0].equals("setdescription") && command_admin.length >= 3) {
                StringBuilder s = new StringBuilder();
                String product = command_admin[1];
                for (int i = 2; i < command_admin.length ; i++) {
                    s.append(command_admin[i]).append(" ");
                }
                UtilsDiscord.setDescription(connection, event, product, String.valueOf(s));
            }
        }

        // public
        if (message.startsWith(prefix)) {
            String[] command = lowerCase.split(" ");
            if (command[0].equals("help") && command.length == 1) {
                UtilsDiscord.help(event, false, prefix);
            } else if ((command[0].equals("world") || command[0].equals("bot")) && command.length == 1) {
                String cmd = command[0];
                UtilsDiscord.showWorldInformation(connection, event, cmd);
            } else if (command[0].equals("setuser") && command.length == 2) {
                String username = command[1];
                UtilsDiscord.setUser(connection, event, authorID,username);
            } else if ((command[0].equals("checkuser") || command[0].equals("balance") ) && command.length == 1) {
                String cmd = command[0];
                UtilsDiscord.checkUserAndBalance(connection, event, cmd ,authorID, emojiWL);
            } else if (command[0].equals("stock") && command.length == 1) {
                UtilsDiscord.showStock(connection, event, emojiLine, emojiArrow);
            } else if (command[0].equals("buy") && command.length == 3) {
                String idProduct = command[1];
                String strCount = command[2];
                UtilsDiscord.buy(connection, event, authorID, idProduct, strCount, emojiWL);
            }
        }
    }
}
