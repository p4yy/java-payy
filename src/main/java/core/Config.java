package core;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Config {

    private String dbHost;
    private String dbUsername;
    private String dbPassword;
    private String dbName;
    private String token;
    private List<String> adminIds;
    private String prefix;

    private String emojiWL;

    private String emojiLine;
    private String emojiArrow;
    private String channelIdDeposit;

    private String statusWatching;

    public Config(String pathName) {
        loadConfig(pathName);
    }

    private void loadConfig(String pathName) {
        try {
            InputStream inputStream = new FileInputStream(pathName);

            if (inputStream != null) {
                Scanner scanner = new Scanner(inputStream).useDelimiter("\\A");
                String configFileContent = scanner.hasNext() ? scanner.next() : "";

                JSONObject jsonObject = new JSONObject(configFileContent);

                this.dbHost = jsonObject.getString("db_host");
                this.dbUsername = jsonObject.getString("db_username");
                this.dbPassword = jsonObject.getString("db_password");
                this.dbName = jsonObject.getString("db_name");
                this.token = jsonObject.getString("token");
                this.prefix = jsonObject.getString("prefix");
                this.emojiWL = jsonObject.getString("emoji_wl");
                this.emojiLine = jsonObject.getString("emoji_line");
                this.emojiArrow = jsonObject.getString("emoji_arrow");
                this.channelIdDeposit = jsonObject.getString("channel_id_deposit");
                this.statusWatching = jsonObject.getString("status_watching");

                JSONArray adminIdsArray = jsonObject.getJSONArray("admin_ids");
                this.adminIds = new ArrayList<>();
                for (int i = 0; i < adminIdsArray.length(); i++) {
                    this.adminIds.add(adminIdsArray.getString(i));
                }
            } else {
                System.out.println("File config.json not found.");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public String getDbHost() {
        return dbHost;
    }

    public String getDbUsername() {
        return dbUsername;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public String getDbName() {
        return dbName;
    }

    public String getToken() {
        return token;
    }

    public List<String> getAdminIds() {
        return adminIds;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getEmojiWL() {
        return emojiWL;
    }

    public String getEmojiLine() {
        return emojiLine;
    }

    public String getEmojiArrow() {
        return emojiArrow;
    }

    public String getChannelIdDeposit() {
        return channelIdDeposit;
    }

    public String getStatusWatching() {
        return statusWatching;
    }

}
