package discord.listeners;

import discord.utils.UtilsDiscord;
import logger.LogUtil;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.sharding.ShardManager;

import java.sql.Connection;
import java.util.List;

public class WebhookListener extends ListenerAdapter {

    private final String channelDeposit;
    private final Connection connection;
    private final ShardManager shardManager;
    private final String idChannelHistory;
    private final String emojiCurrency;
    private final boolean isPostgreSQL;

    private static int extractNumericValue(String input) {
        String numericPart = input.replaceAll("[^\\d.]", "");

        if (!numericPart.isEmpty()) {
            return Integer.parseInt(numericPart);
        } else {
            return 0;
        }
    }

    public WebhookListener(Connection connection, ShardManager shardManager, boolean isPostgreSQL, String channelDeposit, String idChannelHistory, String emojiCurrency) {
        this.connection = connection;
        this.channelDeposit = channelDeposit;
        this.shardManager = shardManager;
        this.idChannelHistory = idChannelHistory;
        this.emojiCurrency = emojiCurrency;
        this.isPostgreSQL = isPostgreSQL;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.isWebhookMessage() && event.getChannel().getId().equals(this.channelDeposit)) {
            Message webhookMessage = event.getMessage();

            List<MessageEmbed> embeds = webhookMessage.getEmbeds();
            if (!embeds.isEmpty()) {
                MessageEmbed embed = embeds.getFirst();
                if (embed.getDescription() != null && !embed.getDescription().isEmpty()) {
                    String description = embed.getDescription();

                    String[] lines = description.split("\n");

                    String growidValue = null;
                    Integer depositValue = null;

                    for (String line : lines) {
                        String toLower = line.toLowerCase();
                        if (toLower.startsWith("growid:")) {
                            // take value from growid
                            growidValue = toLower.substring("growid:".length()).trim();
                        } else if (toLower.startsWith("deposit:")) {
                            // take value from Deposit
                            String depositString = toLower.substring("deposit:".length()).trim();
                            try {
                                // Extract numeric value from the depositString using the extractNumericValue method
                                int numericValue = extractNumericValue(depositString);

                                // Apply the multipliers based on different lock types
                                if (depositString.toLowerCase().contains("diamond lock")) {
                                    depositValue = numericValue * 100; // Multiply by 100 for Diamond Lock
                                } else if (depositString.toLowerCase().contains("blue gem lock")) {
                                    depositValue = numericValue * 1000; // Multiply by 1000 for Blue Gem Lock
                                } else if (depositString.toLowerCase().contains("world lock")) {
                                    depositValue = numericValue; // Set value to the numeric value for World Lock
                                } else {
                                    // If the lock type is not identified, handle it as needed
                                    event.getChannel().sendMessage("Unknown lock type").queue();
                                    LogUtil.logError("Webhook", "Message from deposit: Unknown lock type");
                                }
                            } catch (NumberFormatException e) {
                                LogUtil.logError("Webhook", "Number format error: {}", e.getMessage());
                            }
                        }
                    }

                    if (growidValue != null && depositValue != null) {
                        UtilsDiscord.addUserBalance(connection, event, shardManager, idChannelHistory, growidValue, depositValue, emojiCurrency, isPostgreSQL, false);
                    } else {
                        LogUtil.logError("Webhook", "Can't find Growid value");
                    }
                } else {
                    LogUtil.logError("Webhook", "Embed message don't have description");
                }
            } else {
                LogUtil.logError("Webhook", "Webhook don't have embed message");
            }
        }
    }
}
