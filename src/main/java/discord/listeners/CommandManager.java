package discord.listeners;

import core.Config;
import discord.utils.UtilsCommand;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class CommandManager extends ListenerAdapter {

    private final Connection connection;
    private final ShardManager shardManager;
    private final Config config;

    public CommandManager(Connection connection, ShardManager shardManager, Config config) {
        this.connection = connection;
        this.config = config;
        this.shardManager = shardManager;
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String authorID = event.getUser().getId();
        if (!event.getChannel().getId().equals(config.getChannelIdBotCommandSlash()) && !config.getAdminIds().contains(authorID)) {
            event.reply("Please type in here <#"+config.getChannelIdBotCommandSlash()+">").setEphemeral(true).queue();
            return;
        }
        String command = event.getName();

        switch (command) {
            case "help":
                UtilsCommand.help(event, false, "/");
            case "setuser":
                OptionMapping usernameOption = event.getOption("username");
                assert usernameOption != null;
                String username = usernameOption.getAsString();
                UtilsCommand.setUser(connection, event, authorID, username, config.isPostgreSQL());
                break;
            case "checkuser":
                UtilsCommand.checkUserAndBalance(connection, event, "checkuser", authorID, config.getEmojiCurrency());
                break;
            case "balance":
                UtilsCommand.checkUserAndBalance(connection, event, "balance", authorID, config.getEmojiCurrency());
                break;
            case "world":
                UtilsCommand.showWorldInformation(connection, event);
                break;
            case "stock":
                UtilsCommand.showStock(connection, event, config.getEmojiLine(), config.getEmojiArrow(), config.getBannerUrlStock(), config.getEmojiCurrency());
                break;
            case "buy":
                OptionMapping productIDOption = event.getOption("product_id");
                OptionMapping amountOption = event.getOption("amount");
                assert productIDOption != null && amountOption != null;
                String productID = productIDOption.getAsString();
                String amount = amountOption.getAsString();
                UtilsCommand.buy(connection, event, shardManager, authorID, productID, amount, config);
                break;
            default:
                event.reply("Unknown command!").setEphemeral(true).queue();
                break;
        }
    }


    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        List<CommandData> commandData = new ArrayList<>();

        // help
        commandData.add(Commands.slash("help", "Show help"));

        // check user
        commandData.add(Commands.slash("checkuser", "Check your current username"));

        // check world
        commandData.add(Commands.slash("world", "Check current world deposit"));

        // check balance
        commandData.add(Commands.slash("balance", "Check your current balance"));

        // display stock
        commandData.add(Commands.slash("stock", "Display stock information"));

        // set user
        OptionData optionSetUser = new OptionData(OptionType.STRING, "username", "username you will register", true);
        commandData.add(Commands.slash("setuser", "Register an username").addOptions(optionSetUser));

        // buy product
        OptionData optionBuyProduct1 = new OptionData(OptionType.STRING, "product_id", "Product ID to buy", true);
        OptionData optionBuyProduct2 = new OptionData(OptionType.STRING, "amount", "Amount of item to buy", true);
        commandData.add(Commands.slash("buy", "Buy product").addOptions(optionBuyProduct1, optionBuyProduct2));

        event.getGuild().updateCommands().addCommands(commandData).queue();

    }
}