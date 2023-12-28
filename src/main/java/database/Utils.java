package database;

import logger.LogUtil;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Utils {

    private static void createTable(Connection connection, String Query) throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate(Query);
        statement.close();
    }

    // create table format products
    public static void createTableProducts(Connection connection, String product, boolean isPostgreSQL) throws SQLException {
        String t = "VARCHAR(36)";
        if (isPostgreSQL) {
            t = "UUID";
        }

        String createTableQuery = "CREATE TABLE IF NOT EXISTS " + product + " (" +
                "ID " + t + " NOT NULL," +
                "ID_PRODUCT VARCHAR(50) NOT NULL," +
                "NAME VARCHAR(255) NOT NULL," +
                "TIME_CREATED TIMESTAMP DEFAULT CURRENT_TIMESTAMP," + // Tambahkan kolom time_created dengan nilai default saat ini
                "PRIMARY KEY(ID)" +
                ")";

        if (!isPostgreSQL) {
            UUID uuid = UUID.randomUUID();
            String uuidString = uuid.toString();
            createTableQuery = createTableQuery.replace("ID " + t + " NOT NULL", "ID " + t + " NOT NULL DEFAULT '" + uuidString + "'");
        }

        createTable(connection, createTableQuery);
        LogUtil.logInfo("Database", "Table Products {} created successfully.", product);
    }


    // table WORLD
    public static void createTableWorld(Connection connection) throws SQLException {
        String query = "CREATE TABLE IF NOT EXISTS WORLD (ID VARCHAR(50) NOT NULL, NAME VARCHAR(50) NOT NULL, USERNAME VARCHAR(50) NOT NULL, GUARD VARCHAR(50), PRIMARY KEY(ID))";
        createTable(connection, query);
        LogUtil.logInfo("Database", "Table World created successfully");
    }

    // table history
    public static void createTableHistory(Connection connection) throws SQLException {
        String query = "CREATE TABLE IF NOT EXISTS HISTORY(ID VARCHAR(50) NOT NULL, PURCHASE INT NOT NULL, MONEY_IN DECIMAL(10,2) NOT NULL, MONEY_OUT DECIMAL(10,2) NOT NULL, MONEY_IN_CIRCULATION DECIMAL(10,2) NOT NULL, PRIMARY KEY(ID))";
        createTable(connection, query);
        LogUtil.logInfo("Database", "Table History created successfully");
    }

    // create table format information
    public static void createTableInformation(Connection connection, String tableName) throws SQLException {
        String createTableInformationQuery = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                "ID_PRODUCT VARCHAR(50) NOT NULL," +
                "STOCK INT NOT NULL," +
                "PRICE DECIMAL(10,2) NOT NULL," +
                "DESCRIPTION VARCHAR(255)," +
                "PRIMARY KEY (ID_PRODUCT)" +
                ")";
        createTable(connection, createTableInformationQuery);
        LogUtil.logInfo("Database", "Table Information created successfully.");
    }

    // create table format user
    // username = growid
    public static void createTableUser(Connection connection) throws SQLException {
        String createTableUserQuery = "CREATE TABLE IF NOT EXISTS CUSTOMERS (" +
                "ID VARCHAR(50) NOT NULL," +
                "BALANCE DECIMAL(10,2) NOT NULL," +
                "TOTAL_DEPOSIT DECIMAL(10,2) NOT NULL," +
                "USERNAME VARCHAR(50)," +
                "PRIMARY KEY (ID)" +
                ")";
        createTable((connection), createTableUserQuery);
        LogUtil.logInfo("Database", "Table Customers created successfully");
    }

    // deleting table
    public static boolean deleteTable(Connection connection, String tableName) throws SQLException {
        try {
            String deleteQuery = "DROP TABLE IF EXISTS " + tableName;
            Statement statement = connection.createStatement();
            statement.executeUpdate(deleteQuery);
            statement.close();
            LogUtil.logInfo("Database", "Table {} has been deleted successfully", tableName);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public static boolean deleteProductInformation(Connection connection, String ID) throws SQLException {
        String query = "DELETE FROM INFORMATION WHERE ID_PRODUCT = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, ID);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                LogUtil.logInfo("Database", "Row successfully deleted from table INFORMATION");
                return true;
            } else {
                LogUtil.logError("Database", "Failed to delete ID {} from table INFORMATION", ID);
            }
        }
        return false;
    }

    // insert data into table World
    public static void insertWorldData(Connection connection) throws SQLException {
        String selectQuery = "SELECT COUNT(*) AS count FROM WORLD WHERE ID = ?";
        String insertQuery = "INSERT INTO WORLD (ID, NAME, USERNAME, GUARD) VALUES (?, ?, ?, ?)";

        try (PreparedStatement selectStatement = connection.prepareStatement(selectQuery)) {
            selectStatement.setString(1, "DEPOSIT");

            ResultSet resultSet = selectStatement.executeQuery();
            if (resultSet.next()) {
                int count = resultSet.getInt("count");
                if (count == 0) {
                    try (PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {
                        insertStatement.setString(1, "DEPOSIT");
                        insertStatement.setString(2, "JavaPayy");
                        insertStatement.setString(3, "JavaPayy");
                        insertStatement.setString(4, "JavaPayy");

                        int rowsAffected = insertStatement.executeUpdate();
                        if (rowsAffected > 0) {
                            LogUtil.logInfo("Database", "Data inserted successfully into World table");
                        } else {
                            LogUtil.logError("Database", "Failed to insert data into World table");
                        }
                    }
                } else {
                    LogUtil.logInfo("Database", "Data with ID DEPOSIT already exists in World table. But it's fine");
                }
            }
        }
    }

    // insert data to history if data not exists
    public static void insertDataHistory(Connection connection) throws SQLException {
        int purchaseValue = 0;
        double moneyInValue = 0;
        double moneyOutValue = 0;
        double moneyInCirculationValue = 0;
        String selectQuery = "SELECT COUNT(*) AS count FROM HISTORY WHERE ID = ?";
        String insertQuery = "INSERT INTO HISTORY (ID, PURCHASE, MONEY_IN, MONEY_OUT, MONEY_IN_CIRCULATION) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement selectStatement = connection.prepareStatement(selectQuery)) {
            selectStatement.setString(1, "PURCHASE");

            ResultSet resultSet = selectStatement.executeQuery();
            if (resultSet.next()) {
                int count = resultSet.getInt("count");
                if (count == 0) {
                    try (PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {
                        insertStatement.setString(1, "PURCHASE");
                        insertStatement.setInt(2, purchaseValue);
                        insertStatement.setDouble(3, moneyInValue);
                        insertStatement.setDouble(4, moneyOutValue);
                        insertStatement.setDouble(5, moneyInCirculationValue);

                        int rowsAffected = insertStatement.executeUpdate();
                        if (rowsAffected > 0) {
                            LogUtil.logInfo("Database", "Data inserted successfully into History table");
                        } else {
                            LogUtil.logError("Database", "Failed to insert data into History table");
                        }
                    }
                } else {
                    LogUtil.logInfo("Database", "Data with ID PURCHASE already exists in History table. But it's fine");
                }
            }
        }
    }


    public static boolean changeDataWorld(Connection connection, String world, String username, String guard) throws SQLException {
        String updateQuery = "UPDATE WORLD SET NAME = ?, USERNAME = ?, GUARD = ? WHERE ID = 'DEPOSIT'";

        try (PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {
            preparedStatement.setString(1, world);
            preparedStatement.setString(2, username);
            preparedStatement.setString(3, guard);

            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0;
        }
    }

    public static void insertProductData(Connection connection, String idProduct, String name) throws SQLException {
        String uuid = UUID.randomUUID().toString(); // Generate a new UUID

        String insertQuery = "INSERT INTO " + idProduct + " (ID, ID_PRODUCT, NAME, TIME_CREATED) VALUES (?, ?, ?, CURRENT_TIMESTAMP)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
            preparedStatement.setString(1, uuid);
            preparedStatement.setString(2, idProduct);
            preparedStatement.setString(3, name);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                LogUtil.logInfo("Database", "Data product inserted successfully with UUID: {}", uuid);
            } else {
                LogUtil.logError("Database", "Failed to insert data product");
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            // Handle the case if UUID already exists in the table
            LogUtil.logError("Database", "UUID already exists in the table. Generating a new UUID and retrying...");
            insertProductData(connection, idProduct, name); // Retry insertion with a new UUID
        }
    }

    // insert data into format table information
    public static void insertInformationData(Connection connection, String tableInformationName, String idProduct, int stock, double price, String description) throws SQLException {
        String insertQuery = "INSERT INTO " + tableInformationName + " (ID_PRODUCT, STOCK, PRICE, DESCRIPTION) VALUES (?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
            preparedStatement.setString(1, idProduct);
            preparedStatement.setInt(2, stock);
            preparedStatement.setDouble(3, price);
            preparedStatement.setString(4, description);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                LogUtil.logInfo("Database", "Data inserted successfully into INFORMATION table.");
            } else {
                LogUtil.logError("Database", "Failed to insert data into INFORMATION table.");
            }
        }
    }

    // increase stock in table information
    public static void increaseStock(Connection connection, String informationTableName, String productID, int count) throws SQLException {
        String increaseStockQuery = "UPDATE " + informationTableName + " SET STOCK = STOCK + ? WHERE ID_PRODUCT = ?";

        try (PreparedStatement increaseStockStatement = connection.prepareStatement(increaseStockQuery)) {
            increaseStockStatement.setInt(1, count);
            increaseStockStatement.setString(2, productID);
            increaseStockStatement.executeUpdate();
        }
    }

    // insert data into table CUSTOMERS
    public static void insertUserData(Connection connection, String idUser, Double balance, Double totalDeposit, String username, boolean isPostgreSQL) throws SQLException {
        String insertUserQueryMySql = "INSERT INTO CUSTOMERS (ID, BALANCE, TOTAL_DEPOSIT, USERNAME) VALUES (?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE USERNAME = VALUES(USERNAME)";
        String insertUserQueryPostgre = "INSERT INTO CUSTOMERS (ID, BALANCE, TOTAL_DEPOSIT, USERNAME) VALUES (?, ?, ?, ?) " +
                "ON CONFLICT (ID) DO UPDATE SET USERNAME = EXCLUDED.USERNAME";
        String query;
        if (isPostgreSQL) {
            query = insertUserQueryPostgre;
        } else {
            query = insertUserQueryMySql;
        }

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, idUser);
            preparedStatement.setDouble(2, balance);
            preparedStatement.setDouble(3, totalDeposit);
            preparedStatement.setString(4, username);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                LogUtil.logInfo("Database", "Data inserted successfully into CUSTOMERS table");
            } else {
                LogUtil.logError("Database", "Failed to insert data CUSTOMERS");
            }
        }
    }

    // add balance in table CUSTOMERS
    public static boolean addBalanceUserByID(Connection connection, String idUser, double count, boolean isPostgreSQL) throws SQLException {
        String t = "";
        if (isPostgreSQL) t = "::numeric";

        String queryAddBalance = "UPDATE CUSTOMERS SET BALANCE = BALANCE + ?" + t + " , TOTAL_DEPOSIT = TOTAL_DEPOSIT + ?" + t + " WHERE ID = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(queryAddBalance)) {
            BigDecimal countBigDecimal = BigDecimal.valueOf(count);

            preparedStatement.setObject(1, countBigDecimal);
            preparedStatement.setObject(2, countBigDecimal);
            preparedStatement.setString(3, idUser);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                LogUtil.logInfo("Database", "Success add balance into id {}", idUser);
                return true;
            } else {
                LogUtil.logError("Database", "Failed add balance into id {}", idUser);
            }
        }
        return false;
    }

    public static boolean addBalanceUserByUsername(Connection connection, String username, double count, boolean isPostgreSQL) throws SQLException {
        String t = "";
        if (isPostgreSQL) t = "::numeric";

        String queryAddBalance = "UPDATE CUSTOMERS SET BALANCE = BALANCE + ?" + t + " , TOTAL_DEPOSIT = TOTAL_DEPOSIT + ?" + t + " WHERE USERNAME = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(queryAddBalance)) {
            BigDecimal countBigDecimal = BigDecimal.valueOf(count);

            preparedStatement.setObject(1, countBigDecimal);
            preparedStatement.setObject(2, countBigDecimal);
            preparedStatement.setString(3, username);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                LogUtil.logInfo("Database", "Success add balance into user {}", username);
                return true;
            } else {
                LogUtil.logError("Database", "Failed add balance into user {}", username);
            }
        }
        return false;
    }

    // decrement user balance when transaction
    public static boolean decrementBalanceUserByID(Connection connection, double bill, String idUser, boolean isPostgreSQL) throws SQLException {
        String t = "";
        if (isPostgreSQL) t = "::numeric";

        String query = "UPDATE CUSTOMERS SET BALANCE = BALANCE - ?" + t + " WHERE ID = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            BigDecimal billBigDecimal = BigDecimal.valueOf(bill);
            preparedStatement.setObject(1, billBigDecimal);
            preparedStatement.setString(2, idUser);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                LogUtil.logInfo("Database", "Balance of ID_USER {} update successfully", idUser);
                return true;
            } else {
                LogUtil.logError("Database", "No rows updated. ID {} not found", idUser);
            }
        }
        return false;
    }

    public static boolean decrementBalanceUserByUsername(Connection connection, double bill, String username, boolean isPostgreSQL) throws SQLException {
        String t = "";
        if (isPostgreSQL) t = "::numeric";

        String query = "UPDATE CUSTOMERS SET BALANCE = BALANCE - ?" + t + " WHERE USERNAME = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            BigDecimal billBigDecimal = BigDecimal.valueOf(bill);
            preparedStatement.setObject(1, billBigDecimal);
            preparedStatement.setString(2, username);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                LogUtil.logInfo("Database", "Balance of Username {} update successfully", username);
                return true;
            } else {
                LogUtil.logError("Database", "No rows updated. Username {} not found", username);
            }
        }
        return false;
    }

    // get data from information table with ID_PRODUCT
    public static Object[] getDataFromTableInformationWithID(Connection connection, String ID) throws SQLException {
        String query = "SELECT STOCK, PRICE FROM INFORMATION WHERE ID_PRODUCT = ?";
        Object[] informationData = new Object[2];

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)){
            preparedStatement.setString(1, ID);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    int stock = resultSet.getInt("STOCK");
                    double price = resultSet.getDouble("PRICE");

                    informationData[0] = stock;
                    informationData[1] = price;
                }
            }
        }
        return informationData;
    }

    public static Object[] getDataFromTableHistory(Connection connection) throws SQLException {
        String query = "SELECT PURCHASE, MONEY_IN, MONEY_OUT, MONEY_IN_CIRCULATION FROM HISTORY WHERE ID = 'PURCHASE'";
        Object[] historyData = new Object[4];
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)){
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    int purchase = resultSet.getInt("PURCHASE");
                    double moneyIn = resultSet.getDouble("MONEY_IN");
                    double moneyOut = resultSet.getDouble("MONEY_OUT");
                    double moneyInCirculation = resultSet.getDouble("MONEY_IN_CIRCULATION");

                    historyData[0] = purchase;
                    historyData[1] = moneyIn;
                    historyData[2] = moneyOut;
                    historyData[3] = moneyInCirculation;
                }
            }
        }
        return historyData;
    }

    public static void updateDataTableHistory(Connection connection, int totalPurchase, double moneyIn, double moneyOut, double moneyInCirculation, boolean isPostgreSQL) throws SQLException {
        String t = "";
        if (isPostgreSQL) t = "::numeric";

        String query = "UPDATE HISTORY SET PURCHASE = PURCHASE + ? , MONEY_IN = MONEY_IN + ?" + t + " , MONEY_OUT = MONEY_OUT + ?" + t + " , MONEY_IN_CIRCULATION = MONEY_IN_CIRCULATION + ?" + t + " WHERE ID = 'PURCHASE'";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            BigDecimal moneyInBigDecimal = BigDecimal.valueOf(moneyIn);
            BigDecimal moneyOutBigDecimal = BigDecimal.valueOf(moneyOut);
            BigDecimal moneyInCirculationBigDecimal = BigDecimal.valueOf(moneyInCirculation);

            preparedStatement.setInt(1, totalPurchase);
            preparedStatement.setObject(2, moneyInBigDecimal);
            preparedStatement.setObject(3, moneyOutBigDecimal);
            preparedStatement.setObject(4, moneyInCirculationBigDecimal);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                LogUtil.logInfo("Database", "Table HISTORY updated successfully");
            } else {
                LogUtil.logError("Database", "No rows updated on table HISTORY");
            }
        }
    }

    public static Object[] getDataFromTableWorld(Connection connection) throws SQLException {
        String query = "SELECT NAME, USERNAME, GUARD FROM WORLD WHERE ID = 'DEPOSIT'";
        Object[] worldData = new Object[3];
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)){
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    String world = resultSet.getString("NAME");
                    String username = resultSet.getString("USERNAME");
                    String guard = resultSet.getString("GUARD");

                    worldData[0] = world;
                    worldData[1] = username;
                    worldData[2] = guard;
                }
            }
        }
        return worldData;
    }

    // get data information all products
    public static Object[][] getDataFromTableInformation(Connection connection) throws SQLException {
        String query = "SELECT * FROM INFORMATION";
        List<Object[]> informationDataList = new ArrayList<>();

        try (PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                String idProduct = resultSet.getString("ID_PRODUCT");
                int stock = resultSet.getInt("STOCK");
                double price = resultSet.getDouble("PRICE");
                String description = resultSet.getString("DESCRIPTION");

                Object[] informationData = new Object[4];
                informationData[0] = idProduct;
                informationData[1] = stock;
                informationData[2] = price;
                informationData[3] = description;

                informationDataList.add(informationData);
            }
        }

        return informationDataList.toArray(new Object[0][0]);
    }

    // get data user in table CUSTOMERS
    public static Object[] getDataFromTableUser(Connection connection, String field, String idUser) throws SQLException {
        String querySelect = "SELECT ID, BALANCE, TOTAL_DEPOSIT, USERNAME FROM CUSTOMERS WHERE " + field + " = ?";
        Object[] userData = new Object[4];

        try (PreparedStatement preparedStatement = connection.prepareStatement(querySelect)) {
            preparedStatement.setString(1, idUser);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    String userId = resultSet.getString("ID");
                    double balance = resultSet.getDouble("BALANCE");
                    double totalDeposit = resultSet.getDouble("TOTAL_DEPOSIT");
                    String username = resultSet.getString("USERNAME");

                    userData[0] = userId;
                    userData[1] = balance;
                    userData[2] = totalDeposit;
                    userData[3] = username;
                }
            }
        }
        return userData;
    }

    // updating data field
    // Using this method to update the price of a product or description in the information table.
    public static void updateDataField(Connection connection, String tableName, String keyField, Object newValue, String trigger, String valueTrigger) throws SQLException {
        String updateQuery = "UPDATE " + tableName + " SET " + keyField + " = ? WHERE " + trigger + " = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {
            if (newValue instanceof String) {
                preparedStatement.setString(1, (String) newValue);
            } else if (newValue instanceof Double) {
                preparedStatement.setDouble(1, (Double) newValue);
            } else {
                LogUtil.logError("Database", "Unsupported data type for newValue updateDataField.");
                return;
            }

            preparedStatement.setString(2, valueTrigger);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                LogUtil.logInfo("Database", "Table {} Field {} updated successfully with value {}", tableName, keyField, newValue);
            } else {
                LogUtil.logError("Database", "No rows updated. {} not found", keyField);
            }
        }
    }



}
