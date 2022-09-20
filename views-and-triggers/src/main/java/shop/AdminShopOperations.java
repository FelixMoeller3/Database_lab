package shop;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This class is used as a parent class for operations which the admin
 * of the webshop executes on the database.
 */
public abstract class AdminShopOperations extends ShopOperations {

  public AdminShopOperations(String url, String user, String password) throws SQLException {
    super(url, user, password);
  }

  /**
   * Create a database called 'shop', after deleting any existing database with this name.
   *
   * @throws SQLException if a database access error occurs
   * @throws IOException if the required query files could not be read
   */
  public abstract void createShopDatabase() throws SQLException, IOException;

  /**
   * Create three tables: customer, article and order, in the currently connected database.
   *
   * @throws SQLException if a database access error occurs
   * @throws IOException if the required query files could not be read
   */
  public abstract void createTables() throws SQLException, IOException;

  /**
   * Populate the customer, article and purchase tables with data.
   *
   * @throws SQLException if a database access error occurs
   * @throws IOException if the required data files could not be read
   */
  public abstract void populateTables() throws SQLException, IOException;

  /**
   * Create one user account per customer, with the user name as initial password.
   * Drop existing users with the same name before.
   * Ensure that users have no access to the tables customer, article and purchase.
   *
   * @throws SQLException if a database access error occurs
   * @throws IOException if the required query files could not be read
   */
  public abstract void createUsers() throws SQLException, IOException;

  /**
   * Create a view of the purchase history.
   *
   * @throws SQLException if a database access error occurs
   * @throws IOException if the required query files could not be read
   */
  public abstract void createViewHistory() throws SQLException, IOException;

  /**
   * Create a function for users to make purchases.
   *
   * @throws SQLException if a database access error occurs
   * @throws IOException if the required query files could not be read
   */
  public abstract void createFunctionNewPurchase() throws SQLException, IOException;

  /**
   * Create a rule to let users cancel purchases
   * by deleting them from their personal "history" view.
   *
   * @throws SQLException if a database access error occurs
   * @throws IOException if the required query files could not be read
   */
  public abstract void createRuleDeleteHistory() throws SQLException, IOException;

  /**
   * Returns the account balance of a user.
   *
   * @param ofUser the user
   * @return the balance of this user's account
   * @throws SQLException if a database access error occurs
   * @throws IOException if the required query files could not be read
   */
  public abstract int getBalance(String ofUser) throws SQLException, IOException;

  /**
   * Selects all names of customers of the shop.
   *
   * @return a ResultSet of customer names
   * @throws SQLException if a database access error occurs
   * @throws IOException if the required query files could not be read
   */
  public abstract ResultSet selectCustomerName() throws SQLException, IOException;

  /**
   * Selects all names of articles of the shop.
   *
   * @return a ResultSet of article names
   * @throws SQLException if a database access error occurs
   * @throws IOException if the required query files could not be read
   */
  public abstract ResultSet selectArticleName() throws SQLException, IOException;

  /**
   * Selects all ids of purchases that have been made in the shop.
   *
   * @return a ResultSet of purchase ids
   * @throws SQLException if a database access error occurs
   * @throws IOException if the required query files could not be read
   */
  public abstract ResultSet selectPurchaseId() throws SQLException, IOException;
}
