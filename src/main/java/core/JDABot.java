package core;

import discord.listeners.EventListener;
import discord.listeners.WebhookListener;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;

import javax.security.auth.login.LoginException;
import java.sql.Connection;
import java.util.List;

public class JDABot {

    private final ShardManager shardManager;

    public JDABot(Connection connection, String token, List<String> adminIDs, String channelDeposit, String statusWatching, String emojiWL, String emojiLine, String emojiArrow) throws LoginException {
        // build shard manager using the provided token
        DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(token);
        builder.setStatus(OnlineStatus.ONLINE);
        builder.setActivity(Activity.watching("JavaPayy is Great"));
        builder.enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_WEBHOOKS);
        shardManager = builder.build();

        shardManager.addEventListener(new EventListener(connection, "!", adminIDs, emojiWL, emojiLine, emojiArrow));
        shardManager.addEventListener(new WebhookListener(connection, channelDeposit));
    }

    // @return the ShardManager instance for the bot.
    public ShardManager getShardManager() {
        return shardManager;
    }
}
