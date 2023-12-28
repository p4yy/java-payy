package database;

import logger.LogUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Transaction {
    public static List<String> buyProducts(Connection connection, String productTableName, String informationTableName, String productID, int count, double price, boolean isSendCommand, boolean isPostgreSQL) {
        String t = "";
        if (isPostgreSQL) t = "::numeric";

        List<String> purchasedProducts = new ArrayList<>();
        try {
            connection.setAutoCommit(false); // Set autocommit mode to false for manual transaction control

            String selectQuery = "SELECT id, name FROM " + productTableName + " ORDER BY TIME_CREATED ASC LIMIT ?";
            PreparedStatement selectStatement = connection.prepareStatement(selectQuery);
            selectStatement.setInt(1, count);
            ResultSet resultSet = selectStatement.executeQuery();

            LogUtil.logInfo("Transaction", "Product(s) to buy");
            while (resultSet.next()) {
                String productName = resultSet.getString("name");
                purchasedProducts.add(productName);
                LogUtil.logInfo("Transaction", "- UUID: {}, Name: {}", resultSet.getString("ID"), productName);
            }

            // Retrieve UUIDs for deletion
            String selectDeleteIDsQuery = "SELECT ID FROM " + productTableName + " ORDER BY TIME_CREATED ASC LIMIT ?";
            PreparedStatement selectDeleteIDsStatement = connection.prepareStatement(selectDeleteIDsQuery);
            selectDeleteIDsStatement.setInt(1, count);
            ResultSet deleteIDsResultSet = selectDeleteIDsStatement.executeQuery();

            List<Object> deleteUUIDs = new ArrayList<>();
            while (deleteIDsResultSet.next()) {
                String uuidString = deleteIDsResultSet.getString("ID");
                Object uuid;
                if (isPostgreSQL) {
                    uuid = UUID.fromString(uuidString); //  Convert string UUID to UUID object
                } else {
                    uuid = uuidString;
                }
                deleteUUIDs.add(uuid);
            }

            deleteIDsResultSet.close();
            selectDeleteIDsStatement.close();

            if (!deleteUUIDs.isEmpty()) {
                StringBuilder deleteQuery = new StringBuilder("DELETE FROM ")
                        .append(productTableName)
                        .append(" WHERE ID IN (");

                // Append placeholders for each UUID
                for (int i = 0; i < deleteUUIDs.size(); i++) {
                    deleteQuery.append("?");
                    if (i < deleteUUIDs.size() - 1) {
                        deleteQuery.append(", ");
                    }
                }
                deleteQuery.append(")");

                PreparedStatement deleteStatement = connection.prepareStatement(deleteQuery.toString());

                // Set UUID values to the prepared statement
                for (int i = 0; i < deleteUUIDs.size(); i++) {
                    deleteStatement.setObject(i + 1, deleteUUIDs.get(i));
                }

                int rowsAffected = deleteStatement.executeUpdate();

                if (rowsAffected == count) {
                    LogUtil.logInfo("Transaction", "Transaction successful - {} product(s) purchased and removed.", count);

                    // Reduce stock in the information table based on ID_PRODUCT
                    String reduceStockQuery = "UPDATE " + informationTableName + " SET STOCK = STOCK - ? WHERE ID_PRODUCT = ?";
                    PreparedStatement reduceStockStatement = connection.prepareStatement(reduceStockQuery);
                    reduceStockStatement.setInt(1, count);
                    reduceStockStatement.setString(2, productID);
                    reduceStockStatement.addBatch();
                    reduceStockStatement.executeBatch();

                    String updateHistoryQuery = "UPDATE HISTORY SET PURCHASE = PURCHASE + ?, MONEY_IN_CIRCULATION = MONEY_IN_CIRCULATION - ?" + t + ", MONEY_OUT = MONEY_OUT + ?" + t + " WHERE ID = 'PURCHASE'";
                    PreparedStatement updateHistoryStatement = connection.prepareStatement(updateHistoryQuery);
                    if (!isSendCommand) {
                        // Update purchase in HISTORY table and adjust money in circulation and money out
                        BigDecimal priceBigDecimal = BigDecimal.valueOf(price);
                        updateHistoryStatement.setInt(1, 1);
                        updateHistoryStatement.setObject(2, priceBigDecimal);
                        updateHistoryStatement.setObject(3, priceBigDecimal);
                    } else {
                        updateHistoryStatement.setInt(1, 1);
                        updateHistoryStatement.setInt(2, 0);
                        updateHistoryStatement.setInt(3, 0);
                    }
                    updateHistoryStatement.addBatch();
                    updateHistoryStatement.executeBatch();

                    connection.commit(); // Commit the transaction if product deletion and stock reduction were successful
                } else {
                    LogUtil.logError("Transaction", "Transaction failed - Insufficient product available");
                    connection.rollback(); // Rollback the transaction if product deletion failed
                    purchasedProducts.clear(); // Clear the list if the transaction fails
                }

                deleteStatement.close();
            } else {
                LogUtil.logError("Transaction", "No products found for deletion");
                connection.rollback(); // Rollback the transaction as no products were found for deletion
                purchasedProducts.clear(); // Clear the list if the transaction fails
            }

            // Close statements and result set
            resultSet.close();
            selectStatement.close();
        } catch (SQLException e) {
            LogUtil.logError("Transaction", "Transaction failed - SQLException occurred: {}", e.getMessage());
            try {
                connection.rollback(); // Rollback the transaction if SQLException occurred
            } catch (SQLException ex) {
                LogUtil.logError("RollbackTransaction", "Rollback failed: {}", ex.getMessage());
            }
            purchasedProducts.clear(); // Clear the list if the transaction fails
        } finally {
            try {
                connection.setAutoCommit(true); // Set autocommit mode back to true
            } catch (SQLException ex) {
                LogUtil.logError("Transaction", "Error setting auto-commit to true: {}", ex.getMessage());
            }
        }
        return purchasedProducts;
    }
}
