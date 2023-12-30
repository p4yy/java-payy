package core;

import logger.LogUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Config {

    private String jdbcUrl;
    private String dbUsername;
    private String dbPassword;
    private String token;
    private List<String> adminIds;
    private String prefix;
    private String emojiCurrency;
    private String emojiLine;
    private String emojiArrow;
    private String channelIdDeposit;
    private String channelIdDepositHistory;
    private String channelIdHistory;
    private String statusPlaying;
    private String bannerUrlStock;
    private String bannerUrlPurchase;
    private String gmtTime;
    private String guildID;
    private String channelIdAdminLogsPurchaseSetting;
    private String channelIdLiveStock;
    private String channelIdBotCommandSlash;
    private boolean isUselogsPurchaseSetting;
    private boolean isUseLiveStock;
    private int intervalLiveStock;
    private boolean isPostgreSQL;

    public Config(String pathName) {
        loadConfig(pathName);
    }

    private void loadConfig(String pathName) {
        try {
            InputStream inputStream = new FileInputStream(pathName);

            Scanner scanner = new Scanner(inputStream).useDelimiter("\\A");
            String configFileContent = scanner.hasNext() ? scanner.next() : "";

            JSONObject jsonObject = new JSONObject(configFileContent);

            this.jdbcUrl = jsonObject.getString("jdbc_url");
            this.dbUsername = jsonObject.getString("db_username");
            this.dbPassword = jsonObject.getString("db_password");
            this.token = jsonObject.getString("token");
            this.prefix = jsonObject.getString("prefix");
            this.emojiCurrency = jsonObject.getString("emoji_currency");
            this.emojiLine = jsonObject.getString("emoji_line");
            this.emojiArrow = jsonObject.getString("emoji_arrow");
            this.channelIdDeposit = jsonObject.getString("channel_id_deposit");
            this.channelIdDepositHistory = jsonObject.getString("channel_id_history_deposit");
            this.channelIdHistory = jsonObject.getString("channel_id_history_purchase");
            this.bannerUrlStock = jsonObject.getString("banner_url_stock");
            this.bannerUrlPurchase = jsonObject.getString("banner_url_purchase");
            this.gmtTime = jsonObject.getString("timezone");
            this.statusPlaying = jsonObject.getString("status_playing");
            isPostgreSQL = jsonObject.getBoolean("use_postgreSQL");
            this.guildID = jsonObject.getString("guild_id");
            JSONObject logsPurchaseSetting = jsonObject.getJSONObject("logs_purchase_setting");
            this.isUselogsPurchaseSetting = logsPurchaseSetting.getBoolean("use_feature");
            this.channelIdAdminLogsPurchaseSetting = logsPurchaseSetting.getString("channel_id_only_admin");
            JSONObject liveStockSetting = jsonObject.getJSONObject("live_stock_setting");
            this.isUseLiveStock = liveStockSetting.getBoolean("use_feature");
            this.channelIdLiveStock = liveStockSetting.getString("channel_id_live_stock");
            this.intervalLiveStock = liveStockSetting.getInt("interval_in_second");
            this.channelIdBotCommandSlash = jsonObject.getString("channel_id_bot_command_slash");

            JSONArray adminIdsArray = jsonObject.getJSONArray("admin_ids");
            adminIds = new ArrayList<>();
            for (int i = 0; i < adminIdsArray.length(); i++) {
                adminIds.add(adminIdsArray.getString(i));
            }
        } catch (Exception e) {
            LogUtil.logError("JsonObject", e.getMessage());
        }
    }

    public String getJdbcUrl() { return jdbcUrl; }

    public String getDbUsername() { return dbUsername; }

    public String getDbPassword() { return dbPassword; }

    public String getToken() { return token; }

    public List<String> getAdminIds() { return adminIds; }

    public String getPrefix() { return prefix; }

    public String getEmojiCurrency() { return emojiCurrency; }

    public String getEmojiLine() { return emojiLine; }

    public String getEmojiArrow() { return emojiArrow; }

    public String getChannelIdDeposit() { return channelIdDeposit; }

    public String getStatusPlaying() { return statusPlaying; }

    public String getChannelIdHistory() { return channelIdHistory; }

    public String getChannelIdDepositHistory() { return channelIdDepositHistory; }

    public String getBannerUrlStock() { return bannerUrlStock; }

    public String getBannerUrlPurchase() { return bannerUrlPurchase; }

    public String getGmtTime() { return gmtTime; }

    public boolean isPostgreSQL() { return isPostgreSQL; }

    public String getGuildID() { return guildID; }

    public String getChannelIdAdminLogsPurchaseSetting() { return channelIdAdminLogsPurchaseSetting; }

    public boolean isUselogsPurchaseSetting() { return isUselogsPurchaseSetting; }

    public String getChannelIdLiveStock() { return channelIdLiveStock; }

    public boolean isUseLiveStock() { return isUseLiveStock; }

    public int getIntervalLiveStock() { return intervalLiveStock; }

    public String getChannelIdBotCommandSlash() { return channelIdBotCommandSlash; }

}
