# JavaPayy

JavaPayy is a Discord bot project designed for facilitating sales within Discord servers. The bot is developed using JDA (Java Discord API) and supports both MySQL and PostgreSQL databases for efficient data management. It's adaptable to various MySQL hosting services or PostgreSQL setups, providing flexibility in database choices based on user preferences or specific requirements.

## Configuration

The `config.json` file plays a crucial role in the bot's functionality, housing various parameters necessary for its operation. Here is a breakdown of the configuration options:

```json
{
  "jdbc_url": "jdbc:mysql://localhost:3306/db_name?sslmode=require", 
  "db_username": "",
  "db_password": "",
  "use_postgreSQL": false,
  "token": "",
  "guild_id": "",
  "admin_ids": [
    ""
  ],
  "prefix": "!",
  "status_playing": "",
  "emoji_currency": "",
  "emoji_line": "**------------**",
  "emoji_arrow": "",
  "channel_id_deposit": "",
  "channel_id_bot_command_slash": "",
  "channel_id_history_deposit": "",
  "channel_id_history_purchase": "",
  "banner_url_stock": "",
  "banner_url_purchase": "",
  "timezone": "GMT+7",
  "logs_purchase_setting": {
    "use_feature": false,
    "channel_id_only_admin": ""
  },
  "live_stock_setting": {
    "use_feature": false,
    "channel_id_live_stock": "",
    "interval_in_second": 30
  }
}
```
## Configuration Details

These configurations manage various aspects of the Discord bot's functionality:

- `jdbc_url`: JDBC URL for connecting to the SQL database. Example: "jdbc:mysql://localhost:3306/db_name?sslmode=require".
- `db_username`: Username required to access the SQL database.
- `db_password`: Password for accessing the SQL database.
- `use_postgreSQL`: Boolean flag indicating whether PostgreSQL is used (default: false).
- `token`: Discord bot token for authentication purposes.
- `guild_id`: Discord guild (server) ID where the bot operates.
- `admin_ids`: An array containing Discord user IDs granted admin privileges.
- `prefix`: Command prefix utilized for invoking bot commands.
- `status_playing`: Discord status the bot displays.
- `emoji_currency`, `emoji_line`, `emoji_arrow`: Emojis employed for specific functionalities.
- `channel_id_deposit`: Discord channel ID designated for deposit.
- `channel_id_bot_command_slash`: Discord channel ID for doing the slash command.
- `channel_id_history_deposit`: Discord channel ID for history deposit.
- `channel_id_history_purchase`: Discord channel ID for purchase transaction history.
- `banner_url_stock`: URL for the banner related to stock information.
- `banner_url_purchase`: URL for the banner related to purchase information.
- `timezone`: Timezone setting for the bot. Example: "GMT+7".

### Logs Purchase Setting

Settings for logging purchase-related activities:

- `use_feature`: Boolean flag indicating whether purchase logging is enabled.
- `channel_id_only_admin`: Discord channel ID where purchase logs are sent (only visible to admins).

### Live Stock Setting

Settings for live stock updates:

- `use_feature`: Boolean flag indicating whether live stock updates are enabled.
- `channel_id_live_stock`: Discord channel ID for live stock updates.
- `interval_in_second`: Interval (in seconds) for updating stock information in the live stock channel. Example: 30.


## Usage

To effectively utilize this bot:

1. Populate the `config.json` file with your specific configuration details.
2. Make sure to insert the path of your `config.json` file in the `Main.java` file.
3. Ensure Java and JDA are correctly set up in your development environment.
4. Run the bot using your preferred Integrated Development Environment (IDE) or Java execution method.

For integrating your configuration file path in the `Main.java` file, locate the section where the bot reads the configuration and specify the path to your `config.json` file. This step is crucial for the proper functioning of the bot.

Ensure that all dependencies are installed, and the necessary permissions are granted before executing the bot within your chosen development environment.

## Contact Me

For any inquiries, feedback, or collaboration opportunities regarding JavaPayy, feel free to contact me via:

- Discord: iqlasss (456374610305220611)

I'm open to discussions, suggestions, and potential collaborations related to the JavaPayy project.

## Support Me

If you find JavaPayy helpful and wish to support its development or express your appreciation, you can:

- Contribute to the project by submitting pull requests or reporting issues.
- Share JavaPayy within your community or Discord servers.
- Or you can buy me a coffee [https://saweria.co/p4yy]
