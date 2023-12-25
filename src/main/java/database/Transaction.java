package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Transaction {
    public static List<String> buyProducts(Connection connection, String productTableName, String informationTableName, String productID, int count) {
        List<String> purchasedProducts = new ArrayList<>();
        try {
            connection.setAutoCommit(false); // Set autocommit mode to false for manual transaction control

            String selectQuery = "SELECT id, name FROM " + productTableName + " ORDER BY id LIMIT ?";
            PreparedStatement selectStatement = connection.prepareStatement(selectQuery);
            selectStatement.setInt(1, count);
            ResultSet resultSet = selectStatement.executeQuery();

            System.out.println("Product(s) to buy:");
            while (resultSet.next()) {
                String productName = resultSet.getString("name");
                purchasedProducts.add(productName);
                System.out.println("- ID: " + resultSet.getInt("id") + ", Name: " + productName);
            }

            // Retrieve IDs for deletion
            String selectDeleteIDsQuery = "SELECT id FROM " + productTableName + " ORDER BY id LIMIT ?";
            PreparedStatement selectDeleteIDsStatement = connection.prepareStatement(selectDeleteIDsQuery);
            selectDeleteIDsStatement.setInt(1, count);
            ResultSet deleteIDsResultSet = selectDeleteIDsStatement.executeQuery();

            List<Integer> deleteIDs = new ArrayList<>();
            while (deleteIDsResultSet.next()) {
                deleteIDs.add(deleteIDsResultSet.getInt("id"));
            }

            deleteIDsResultSet.close();
            selectDeleteIDsStatement.close();

            if (!deleteIDs.isEmpty()) {
                StringBuilder deleteQuery = new StringBuilder("DELETE FROM ")
                        .append(productTableName)
                        .append(" WHERE id IN (");

                // Append placeholders for each ID
                for (int i = 0; i < deleteIDs.size(); i++) {
                    deleteQuery.append("?");
                    if (i < deleteIDs.size() - 1) {
                        deleteQuery.append(", ");
                    }
                }
                deleteQuery.append(")");

                PreparedStatement deleteStatement = connection.prepareStatement(deleteQuery.toString());

                // Set ID values to the prepared statement
                for (int i = 0; i < deleteIDs.size(); i++) {
                    deleteStatement.setInt(i + 1, deleteIDs.get(i));
                }

                int rowsAffected = deleteStatement.executeUpdate();

                if (rowsAffected == count) {
                    System.out.println("Transaction successful - " + count + " product(s) purchased and removed.");

                    // Reduce stock in the information table based on ID_PRODUCT
                    String reduceStockQuery = "UPDATE " + informationTableName + " SET STOCK = STOCK - ? WHERE ID_PRODUCT = ?";
                    PreparedStatement reduceStockStatement = connection.prepareStatement(reduceStockQuery);
                    reduceStockStatement.setInt(1, count);
                    reduceStockStatement.setString(2, productID);
                    reduceStockStatement.addBatch();
                    reduceStockStatement.executeBatch();

                    connection.commit(); // Commit the transaction if product deletion and stock reduction were successful
                } else {
                    System.out.println("Transaction failed - Insufficient products available.");
                    connection.rollback(); // Rollback the transaction if product deletion failed
                    purchasedProducts.clear(); // Clear the list if the transaction fails
                }

                deleteStatement.close();
            } else {
                System.out.println("No products found for deletion.");
                connection.rollback(); // Rollback the transaction as no products were found for deletion
                purchasedProducts.clear(); // Clear the list if the transaction fails
            }

            // Close statements and result set
            resultSet.close();
            selectStatement.close();
        } catch (SQLException e) {
            System.out.println("Transaction failed - SQLException occurred: " + e.getMessage());
            try {
                connection.rollback(); // Rollback the transaction if SQLException occurred
            } catch (SQLException ex) {
                System.out.println("Rollback failed: " + ex.getMessage());
            }
            purchasedProducts.clear(); // Clear the list if the transaction fails
        } finally {
            try {
                connection.setAutoCommit(true); // Set autocommit mode back to true
            } catch (SQLException ex) {
                System.out.println("Error setting auto-commit to true: " + ex.getMessage());
            }
        }
        return purchasedProducts;
    }
}
