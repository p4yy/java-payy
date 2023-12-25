package discord.listeners;

import discord.utils.UtilsDiscord;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.sql.Connection;
import java.util.List;

public class WebhookListener extends ListenerAdapter {

    private final String channelDeposit;
    private final Connection connection;

    public WebhookListener(Connection connection, String channelDeposit) {
        this.connection = connection;
        this.channelDeposit = channelDeposit;
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
                    Double depositValue = null;

                    for (String line : lines) {
                        String toLower = line.toLowerCase();
                        if (toLower.startsWith("growid:")) {
                            // take value from growid
                            growidValue = toLower.substring("growid:".length()).trim();
                        } else if (toLower.startsWith("deposit:")) {
                            // take value from Deposit
                            String depositString = toLower.substring("deposit:".length()).trim();
                            try {
                                depositValue = Double.parseDouble(depositString);
                            } catch (NumberFormatException e) {
                                System.out.println(e.getMessage());
                            }
                        }
                    }

                    if (growidValue != null && depositValue != null) {
                        UtilsDiscord.addUserBalanceByUsername(connection, event, growidValue, depositValue);
                    } else {
                        System.out.println("Can't find Growid value");
                    }
                } else {
                    System.out.println("Embed Message don't have description");
                }
            } else {
                System.out.println("Webhook don't have embed message");
            }
        }
    }
}
