package database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Utils {

    private static void createTable(Connection connection, String Query) throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate(Query);
        statement.close();
    }

    // create table format products
    public static void createTableProducts(Connection connection, String product) throws SQLException {
        String createTableQuery = "CREATE TABLE IF NOT EXISTS " + product + " (" +
                "ID INT AUTO_INCREMENT," +
                "ID_PRODUCT VARCHAR(50) NOT NULL," +
                "NAME VARCHAR(255) NOT NULL," +
                "PRIMARY KEY (ID)" +
                ")";
        createTable(connection, createTableQuery);
        System.out.println("Table Products " + product + " created successfully.");
    }

    // table WORLD
    public static void createTableWorld(Connection connection) throws SQLException {
        String query = "CREATE TABLE IF NOT EXISTS WORLD (ID VARCHAR(50) NOT NULL, NAME VARCHAR(50) NOT NULL, GUARD VARCHAR(50), PRIMARY KEY(ID))";
        createTable(connection, query);
        System.out.println("Table World created successfully");
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
        System.out.println("Table Information with table name " + tableName + " created successfully.");
    }

    // create table format user
    // username = growid
    public static void createTableUser(Connection connection) throws SQLException {
        String createTableUserQuery = "CREATE TABLE IF NOT EXISTS USER (" +
                "ID VARCHAR(50) NOT NULL," +
                "BALANCE DECIMAL(10,2) NOT NULL," +
                "USERNAME VARCHAR(50)," +
                "PRIMARY KEY (ID)" +
                ")";
        createTable((connection), createTableUserQuery);
        System.out.println("Table USER created successfully.");
    }

    // deleting table
    public static boolean deleteTable(Connection connection, String tableName) throws SQLException {
        try {
            String deleteQuery = "DROP TABLE IF EXISTS " + tableName;
            Statement statement = connection.createStatement();
            statement.executeUpdate(deleteQuery);
            statement.close();
            System.out.println("Table '" + tableName + "' has been deleted successfully.");
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
                System.out.println("Row successfully deleted from table INFORMATION");
                return true;
            } else {
                System.out.println("Failed to delete ID " + ID + " from table INFORMATION");
            }
        }
        return false;
    }

    // insert data into table World
    public static void insertWorldData(Connection connection) throws SQLException {
        String selectQuery = "SELECT COUNT(*) AS count FROM WORLD WHERE ID = ?";
        String insertQuery = "INSERT INTO WORLD (ID, NAME, GUARD) VALUES (?, ?, ?)";

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

                        int rowsAffected = insertStatement.executeUpdate();
                        if (rowsAffected > 0) {
                            System.out.println("Data inserted successfully into World table.");
                        } else {
                            System.out.println("Failed to insert data into World table.");
                        }
                    }
                } else {
                    System.out.println("Data with ID DEPOSIT already exists in World table.");
                }
            }
        }
    }

    // insert data into format table products
    public static void insertProductData(Connection connection, String idProduct, String name) throws SQLException {
        String insertQuery = "INSERT INTO " + idProduct + " (ID_PRODUCT, NAME) VALUES (?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
            preparedStatement.setString(1, idProduct);
            preparedStatement.setString(2, name);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Data inserted successfully into " + idProduct + " table.");
            } else {
                System.out.println("Failed to insert data into Product table.");
            }
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
                System.out.println("Data inserted successfully into Information table. With the table name " + tableInformationName);
            } else {
                System.out.println("Failed to insert data into Information table.");
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

    // insert data into table USER
    public static void insertUserData(Connection connection, String idUser, Double balance, String username) throws SQLException {
        String insertUserQuery = "INSERT INTO USER (ID, BALANCE, USERNAME) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE USERNAME = VALUES(USERNAME)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertUserQuery)) {
            preparedStatement.setString(1, idUser);
            preparedStatement.setDouble(2, balance);
            preparedStatement.setString(3, username);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Data inserted successfully into USER table");
            } else {
                System.out.println("Failed to insert data USER");
            }
        }
    }

    // add balance in table USER
    public static boolean addBalanceUserByID(Connection connection, String idUser, double count) throws SQLException {
        String queryAddBalance = "UPDATE USER SET BALANCE = BALANCE + ? WHERE ID = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(queryAddBalance)) {
            preparedStatement.setDouble(1,count);
            preparedStatement.setString(2, idUser);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Success add balance into user " + idUser);
                return true;
            } else {
                System.out.println("Failed add balance into user");
            }
        }
        return false;
    }

    public static boolean addBalanceUserByUsername(Connection connection, String username, double count) throws SQLException {
        String queryAddBalance = "UPDATE USER SET BALANCE = BALANCE + ? WHERE USERNAME = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(queryAddBalance)) {
            preparedStatement.setDouble(1,count);
            preparedStatement.setString(2, username);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Success add balance into user " + username);
                return true;
            } else {
                System.out.println("Failed add balance into user");
            }
        }
        return false;
    }

    // decrement user balance when transaction
    public static boolean decrementBalanceUserByID(Connection connection, double bill, String idUser) throws SQLException {
        String query = "UPDATE USER SET BALANCE = BALANCE - ? WHERE ID = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setDouble(1, bill);
            preparedStatement.setString(2, idUser);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.printf("Balance of ID_USER " + idUser + " update successfully");
                return true;
            } else {
                System.out.println("No rows updated. ID " + idUser + " not found.");
            }
        }
        return false;
    }

    public static boolean decrementBalanceUserByUsername(Connection connection, double bill, String username) throws SQLException {
        String query = "UPDATE USER SET BALANCE = BALANCE - ? WHERE USERNAME = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setDouble(1, bill);
            preparedStatement.setString(2, username);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.printf("Balance of ID_USER " + username + " update successfully");
                return true;
            } else {
                System.out.println("No rows updated. ID " + username + " not found.");
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

    public static Object[] getDataFromTableWorld(Connection connection) throws SQLException {
        String query = "SELECT NAME, GUARD FROM WORLD WHERE ID = 'DEPOSIT'";
        Object[] worldData = new Object[2];
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)){
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    String world= resultSet.getString("NAME");
                    String guard = resultSet.getString("GUARD");

                    worldData[0] = world;
                    worldData[1] = guard;
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

    // get data user in table USER
    public static Object[] getDataFromTableUser(Connection connection, String field, String idUser) throws SQLException {
        String querySelect = "SELECT ID, BALANCE, USERNAME FROM USER WHERE " + field + " = ?";
        Object[] userData = new Object[3];

        try (PreparedStatement preparedStatement = connection.prepareStatement(querySelect)) {
            preparedStatement.setString(1, idUser);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    String userId = resultSet.getString("ID");
                    double balance = resultSet.getDouble("BALANCE");
                    String username = resultSet.getString("USERNAME");

                    userData[0] = userId;
                    userData[1] = balance;
                    userData[2] = username;
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
                System.out.println("Unsupported data type for newValue.");
                return;
            }

            preparedStatement.setString(2, valueTrigger);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Table " + tableName + " Field " + keyField + " updated successfully with value " + newValue);
            } else {
                System.out.println("No rows updated. " + keyField + " not found.");
            }
        }
    }



}
