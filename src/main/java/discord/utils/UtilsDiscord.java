package discord.utils;

import database.Transaction;
import database.Utils;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;


public class UtilsDiscord {

    public static void setUser(Connection connection, MessageReceivedEvent event, String authorID, String username) {
        try{
            boolean isDuplicate= Helper.isDuplicateUsername(connection, authorID, username);
            if (isDuplicate) {
                event.getChannel().sendMessage("Error username **" + username + "** has already been registered by someone else.").queue();
            } else {
                Utils.insertUserData(connection, authorID, 0.0, username);
                event.getChannel().sendMessage("Successfully **updated your username** to **" + username + "**").queue();
            }
        } catch (SQLException e) {
            event.getChannel().sendMessage("Something wrong! please contact admin").queue();
            System.out.println(e.getMessage());
        }
    }

    public static void addUserBalanceByID(Connection connection, MessageReceivedEvent event, String authorID, double totalAddBalance) {
        if (totalAddBalance <= 0) {
            event.getChannel().sendMessage("Invalid quantity provided!").queue();
            return;
        }
        try {
            if (authorID.startsWith("<@") && authorID.endsWith(">")) {
                authorID = authorID.replaceAll("[^0-9]", "");
            }
            boolean isValid = Utils.addBalanceUserByID(connection, authorID, totalAddBalance);
            if (isValid) {
                event.getChannel().sendMessage("Success to increment balance of user <@" + authorID + ">").queue();
            } else {
                event.getChannel().sendMessage("Failed to increment balance of user <@" + authorID + ">").queue();
            }
        } catch (SQLException e) {
            event.getChannel().sendMessage("Failed to increment balance of user <@" + authorID + ">").queue();
            System.out.println(e.getMessage());
        }
    }

    public static void decrementUserBalanceByID(Connection connection, MessageReceivedEvent event, String authorID, double count) {
        if (count <= 0) {
            event.getChannel().sendMessage("Invalid quantity provided!").queue();
            return;
        }
        try {
            if (authorID.startsWith("<@") && authorID.endsWith(">")) {
                authorID = authorID.replaceAll("[^0-9]", "");
            }
            boolean isValid = Utils.decrementBalanceUserByID(connection, count, authorID);
            if (isValid) {
                event.getChannel().sendMessage("Success to decrement balance of user <@" + authorID + ">").queue();
            } else {
                event.getChannel().sendMessage("Failed to decrement balance of user <@" + authorID + ">").queue();
            }
        } catch (SQLException e) {
            event.getChannel().sendMessage("Failed to decrement balance of user <@" + authorID + ">").queue();
            System.out.println(e.getMessage());
        }
    }

    public static void addUserBalanceByUsername(Connection connection, MessageReceivedEvent event, String username, double totalAddBalance) {
        if (totalAddBalance <= 0) {
            event.getChannel().sendMessage("Invalid quantity provided!").queue();
        }
        try {
            boolean isValid = Utils.addBalanceUserByUsername(connection, username, totalAddBalance);
            if (isValid) {
                event.getChannel().sendMessage("Success to increment balance of user " + username).queue();
            } else {
                event.getChannel().sendMessage("Failed to increment balance of user " + username).queue();
            }
        } catch (SQLException e) {
            event.getChannel().sendMessage("Failed to increment balance of user " + username).queue();
            System.out.println(e.getMessage());
        }
    }

    public static void decrementUserBalanceByUsername(Connection connection, MessageReceivedEvent event, String username, double count) {
        if (count <= 0) {
            event.getChannel().sendMessage("Invalid quantity provided!").queue();
        }
        try {
            boolean isValid = Utils.decrementBalanceUserByUsername(connection, count, username);
            if (isValid) {
                event.getChannel().sendMessage("Success to decrement balance of user " + username).queue();
            } else {
                event.getChannel().sendMessage("Failed to decrement balance of user " + username).queue();
            }
        } catch (SQLException e) {
            event.getChannel().sendMessage("Failed to decrement balance of user " + username).queue();
            System.out.println(e.getMessage());
        }
    }

    public static void checkUserAndBalance(Connection connection, MessageReceivedEvent event, String command, String authorID, String emojiWL) {
        String text = "";
        if (command.equals("checkuser")) {
            text = "Your current username is ";
        } else {
            text = "You have ";
        }
        String result = Helper.commandUser(connection, command ,authorID);
        if (result != null) {
            event.getChannel().sendMessage(text + result + " " + emojiWL).queue();
        } else {
            event.getChannel().sendMessage("You are not registered, please use !setuser").queue();
        }
    }

    public static void addProduct(Connection connection, MessageReceivedEvent event, String product, String strPrice) {
        try {
            double price = Double.parseDouble(strPrice);
            Utils.createTableProducts(connection, product);
            Utils.insertInformationData(connection, "INFORMATION", product, 0, price, "");
            event.getChannel().sendMessage("Success add product " + product).queue();
        } catch (SQLException | NumberFormatException e) {
            event.getChannel().sendMessage("Failed add product " + product).queue();
            System.out.println(e.getMessage());
        }
    }

    public static void showStock(Connection connection, MessageReceivedEvent event, String emojiLine, String emojiArrow) {
        try {
            Object[][] objInformation = Utils.getDataFromTableInformation(connection);

            String strContent = Helper.getStrContent(objInformation, emojiLine ,emojiArrow);

            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("Stock Information");
            embedBuilder.setDescription(strContent);
            embedBuilder.setFooter("");

            MessageEmbed embed = embedBuilder.build();
            event.getChannel().sendMessageEmbeds(embed).queue();

        } catch (Exception e) {
            event.getChannel().sendMessage("Error occurred").queue();
        }
    }

    public static void changePrice(Connection connection, MessageReceivedEvent event, String product, String strPrice) {
        try {
            Double price = Double.parseDouble(strPrice);
            Utils.updateDataField(connection, "INFORMATION", "PRICE", price, "ID_PRODUCT", product);
            event.getChannel().sendMessage("Success change price product " + product + " into " + strPrice).queue();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            event.getChannel().sendMessage("Failed change price product " + product).queue();
        }
    }

    public static void setDescription(Connection connection, MessageReceivedEvent event, String product, String description) {
        try{
            Utils.updateDataField(connection, "INFORMATION", "DESCRIPTION", description, "ID_PRODUCT", product);
            event.getChannel().sendMessage("Success set description for product " + product).queue();
        } catch (SQLException e) {
            event.getChannel().sendMessage("Failed to set description for product " + product).queue();
            System.out.println(e.getMessage());
        }
    }
    
    public static void setWorld(Connection connection, MessageReceivedEvent event, String world) {
        try {
            Utils.updateDataField(connection, "WORLD", "NAME", world, "ID", "DEPOSIT");
            event.getChannel().sendMessage("Success set deposit world into " + world).queue();
        } catch (SQLException e) {
            event.getChannel().sendMessage("Failed to set deposit world").queue();
            System.out.println(e.getMessage());
        }
    }

    public static void setBot(Connection connection, MessageReceivedEvent event, String name) {
        try {
            Utils.updateDataField(connection, "WORLD", "GUARD", name, "ID", "DEPOSIT");
            event.getChannel().sendMessage("Success set guard bot into " + name).queue();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void showWorldInformation(Connection connection, MessageReceivedEvent event, String command) {
        try {
            String s = Helper.commandWorld(connection, command);
            String t = "";
            if (command.equals("world")) {
                t = "Deposit world is ";
            } else {
                t = "Bot in deposit world is ";
            }
            event.getChannel().sendMessage(t + s).queue();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void help(MessageReceivedEvent event, boolean isAdmin, String prefix) {
        String str = Helper.getStrHelpCommand(isAdmin, prefix);
        event.getChannel().sendMessage(str).queue();
    }

    public static void deleteProduct(Connection connection, MessageReceivedEvent event, String idProduct) {
        if (idProduct.equalsIgnoreCase("user")) {
            return;
        }
        try {
            boolean isValidDeleteTable = Utils.deleteTable(connection, idProduct);
            boolean isValidDeleteProductInfo = Utils.deleteProductInformation(connection, idProduct);
            if (isValidDeleteTable && isValidDeleteProductInfo) {
                event.getChannel().sendMessage("Success delete product " + idProduct).queue();
            } else  {
                event.getChannel().sendMessage("Failed delete product " + idProduct).queue();
            }
        } catch (SQLException e) {
            event.getChannel().sendMessage("Failed delete product " + idProduct).queue();
            System.out.println(e.getMessage());
        }
    }

    public static void addStock(Connection connection, MessageReceivedEvent event, String product) {
        Message msg = event.getMessage();
        List<Message.Attachment> attachments = msg.getAttachments();

        if (!attachments.isEmpty()) {
            try {
                final int[] totalAddStock = {0};

                for (Message.Attachment attachment : attachments) {
                    String fileName = attachment.getFileName();
                    if (fileName.endsWith(".txt")) {
                        attachment.retrieveInputStream().thenAccept(inputStream -> {
                            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                                String line;

                                while ((line = reader.readLine()) != null) {
                                    boolean isSuccessAddStock = Helper.addStockToTableInformation(connection, product, line);
                                    if (isSuccessAddStock) {
                                        totalAddStock[0] += 1;
                                    }
                                }

                                event.getChannel().sendMessage("Success add " + totalAddStock[0] + " item into product " + product).queue();
                            } catch (IOException e) {
                                event.getChannel().sendMessage("Error reading the file").queue();
                                System.out.println(e.getMessage());
                            }
                        });
                    } else {
                        event.getChannel().sendMessage("Please upload a text file").queue();
                    }
                }
            } catch (Exception e){
                System.out.println(e.getMessage());
                event.getChannel().sendMessage("Error!. Something wrong when adding stock").queue();
            }
        } else {
            event.getChannel().sendMessage("No file attached").queue();
        }
    }

    // transaction buy
    public static void buy(Connection connection, MessageReceivedEvent event, String authorID, String productID, String strCount, String emojiWL) {
        try {
            int count = Integer.parseInt(strCount);
            if (count <= 0) {
                event.getChannel().sendMessage("Invalid quantity provided!").queue();
                return;
            }

            double balance = Double.parseDouble(Helper.commandUser(connection, "balance", authorID));

            Object[] objInformation = Utils.getDataFromTableInformationWithID(connection, productID);

            if (objInformation.length > 0 && objInformation[0] != null && objInformation[1] != null) {
                int stock = (int) objInformation[0];
                Double price = (Double) objInformation[1];
                if (stock >= count) {
                    double finalPrice = count * price;
                    double currentBalance = balance - finalPrice;
                    if (balance >= finalPrice) {
                        List<String> transaction = Transaction.buyProducts(connection, productID, "INFORMATION", productID, count);
                        if (!transaction.isEmpty()) {
                            boolean decrementBalance = Utils.decrementBalanceUserByID(connection, finalPrice, authorID);
                            if (decrementBalance) {
                                StringBuilder textContent = new StringBuilder();
                                for (String data : transaction) {
                                    textContent.append(data).append("\n");
                                }

                                byte[] contentBytes = textContent.toString().getBytes();
                                event.getChannel().sendMessage("Here's your " + count + " items of product " + productID + " \nYour current balance: " + currentBalance + " " + emojiWL)
                                        .addFile(contentBytes, "result.txt")
                                        .queue();
                            } else {
                                event.getChannel().sendMessage("Something wrong when decrement balance user, please tell admin").queue();
                            }
                        } else {
                            event.getChannel().sendMessage("Transaction failed!").queue();
                        }
                    } else {
                        event.getChannel().sendMessage("You don't have enough balance to buy\nBalance : " + balance + " " + emojiWL).queue();
                    }
                } else {
                    event.getChannel().sendMessage("Product is out of stock").queue();
                }
            } else {
                event.getChannel().sendMessage("Product with ID " + productID + " not found.").queue();
            }
        } catch (SQLException | NumberFormatException e) {
            event.getChannel().sendMessage("Something wrong!").queue();
            System.out.println(e.getMessage());
        }
    }


}
