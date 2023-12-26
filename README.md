# JavaPayy

JavaPayy is a Discord bot project designed for facilitating sales within Discord servers. The bot is developed using JDA (Java Discord API) and MySQL, utilizing PlanetScale for efficient data management. However, it's possible to utilize other MySQL hosting services.

## Configuration

The `config.json` file plays a crucial role in the bot's functionality, housing various parameters necessary for its operation. Here is a breakdown of the configuration options:

```json
{
  "db_host": "Your_MySQL_Database_Hostname",
  "db_username": "Your_Database_Username",
  "db_password": "Your_Database_Password",
  "db_name": "Your_Database_Name",
  "token": "Your_Discord_Bot_Token",
  "admin_ids": [
    "Your_Admin_User_IDs"
  ],
  "prefix": "!",
  "emoji_wl": "Emoji_WL",
  "emoji_line": "Emoji_Line",
  "emoji_arrow": "Emoji_Arrow",
  "channel_id_deposit": "Deposit_Channel_ID",
  "status_watching": ""
}
```

**Configuration Details:**

- `db_host`: Hostname for the MySQL database.
- `db_username`: Username required to access the MySQL database.
- `db_password`: Password for accessing the MySQL database.
- `db_name`: Name of the specific MySQL database.
- `token`: Discord bot token for authentication purposes.
- `admin_ids`: An array containing Discord user IDs granted admin privileges.
- `prefix`: Command prefix utilized for invoking bot commands (default: `!`).
- `emoji_wl`, `emoji_line`, `emoji_arrow`: Emojis employed for specific functionalities.
- `channel_id_deposit`: Discord channel ID designated for deposit.

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
