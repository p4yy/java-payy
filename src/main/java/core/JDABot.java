package core;

import database.Utils;
import discord.listeners.EventListener;
import discord.listeners.WebhookListener;
import discord.utils.Helper;
import logger.LogUtil;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;

import javax.security.auth.login.LoginException;
import java.sql.Connection;
import java.util.List;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class JDABot {

    private final ShardManager shardManager;

    public JDABot(
            Connection connection,
            String token,
            List<String> adminIDs,
            String channelDeposit,
            String channelHistory,
            String channelDepositHistory,
            String statusPlaying,
            String emojiCurrency,
            String emojiLine,
            String emojiArrow,
            String prefix,
            String bannerUrlStock,
            String bannerUrlPurchase,
            String gmtTime,
            String guildID,
            String channelIdAdminLogsPurchaseSetting,
            boolean isUselogsPurchaseSetting,
            boolean isPostgreSQL,
            boolean isUseLiveStock,
            String liveStockChannelID,
            int intervalLiveStock
    ) throws LoginException, InterruptedException {
        // build shard manager using the provided token
        DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(token);
        builder.setStatus(OnlineStatus.ONLINE);
        if (statusPlaying != null) {
            builder.setActivity(Activity.playing(statusPlaying));
        }
        builder.enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_WEBHOOKS, GatewayIntent.GUILD_MEMBERS);
        shardManager = builder.build();

        boolean allShardsReady = false;
        while (!allShardsReady) {
            allShardsReady = true; // Assume all shards are ready

            for (JDA shard : shardManager.getShards()) {
                if (!shard.getStatus().equals(JDA.Status.CONNECTED)) {
                    allShardsReady = false; // There is at least one shard that is not ready
                    break; // Exit the loop because at least one shard is not ready
                }
            }

            if (!allShardsReady) {
                Thread.sleep(1000); // Wait 1 second before checking the status again
            }
        }

        shardManager.addEventListener(
                new EventListener(
                        connection,
                        shardManager,
                        channelHistory,
                        channelDepositHistory,
                        prefix,
                        adminIDs,
                        emojiCurrency,
                        emojiLine,
                        emojiArrow,
                        bannerUrlStock,
                        bannerUrlPurchase,
                        gmtTime,
                        guildID,
                        channelIdAdminLogsPurchaseSetting,
                        isUselogsPurchaseSetting,
                        isPostgreSQL
                )
        );
        shardManager.addEventListener(
                new WebhookListener(
                        connection,
                        shardManager,
                        isPostgreSQL,
                        channelDeposit,
                        channelDepositHistory,
                        emojiCurrency
                )
        );

        if (isUseLiveStock) {
            LogUtil.logInfo("Threading", "Do threading for livestock feature");
            doThreadingLiveStock(connection, shardManager, liveStockChannelID, emojiLine, emojiArrow, emojiCurrency, bannerUrlStock, gmtTime, intervalLiveStock);
        }
    }

    private void liveStock(Connection connection, TextChannel channel, String messageID, String emojiLine, String emojiArrow, String emojiCurrency, String bannerUrl, String gmtTime) {
        try {
            Object[][] objInformation = Utils.getDataFromTableInformation(connection);
            String strContent = Helper.getStrContent(objInformation, emojiLine, emojiArrow, emojiCurrency);
            MessageEmbed embed = Helper.createEmbed("Realtime Stock Information", strContent, bannerUrl, "Last Update: " + Helper.getStrTime(gmtTime));
            channel.retrieveMessageById(messageID).queue(message -> {
                message.editMessageEmbeds(embed).queue();
            });
        } catch (Exception e) {
            LogUtil.logError("LiveStock", "Got exception in livestock feature, {}", e.getMessage());
        }
    }

    private void doThreadingLiveStock(Connection connection, ShardManager shardManager, String channelId, String emojiLine, String emojiArrow, String emojiCurrency, String bannerUrl, String gmtTime, int interval) {
        try {
            TextChannel channel = shardManager.getTextChannelById(channelId);
            AtomicReference<String> messageId = new AtomicReference<>();
            Object[][] objInformation = Utils.getDataFromTableInformation(connection);
            String strContent = Helper.getStrContent(objInformation, emojiLine, emojiArrow, emojiCurrency);
            MessageEmbed embed = Helper.createEmbed("Realtime Stock Information", strContent, bannerUrl, "Last Update: " + Helper.getStrTime(gmtTime));
            if (channel != null) {
                List<Message> messageHistory = channel.getHistory().retrievePast(1).complete();
                if (messageHistory.isEmpty()) {
                    channel.sendMessageEmbeds(embed).queue(newMessage -> {
                        messageId.set(newMessage.getId());
                        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
                        scheduler.scheduleAtFixedRate(() -> liveStock(connection, channel, messageId.get(), emojiLine, emojiArrow, emojiCurrency, bannerUrl, gmtTime), 30, interval, TimeUnit.SECONDS);
                    });
                } else {
                    for (Message message : messageHistory) {
                        if (message.getAuthor().isBot()) {
                            messageId.set(message.getId());
                            channel.editMessageEmbedsById(messageId.get(), embed).queue();
                            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
                            scheduler.scheduleAtFixedRate(() -> liveStock(connection, channel, messageId.get(), emojiLine, emojiArrow, emojiCurrency, bannerUrl, gmtTime), 30, interval, TimeUnit.SECONDS);
                            break;
                        }
                    }
                }
            } else {
                LogUtil.logError("Threading", "Feature livestock error, because channel is NULL");
            }
        } catch (Exception e) {
            LogUtil.logError("LiveStock", "Exception happen, message exception: {}", e.getMessage());
        }
    }
}
