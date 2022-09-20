package shop;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class UserShopOperations extends ShopOperations {

  public UserShopOperations(String url, String user, String password) throws SQLException {
    super(url, user, password);
  }

  /**
   * Purchase an article for the user associated to this ShopOperations instance.
   *
   * @param article the article to purchase
   * @param quantity the quantity of this article to purchase
   * @return true, iff the user associated to this ShopOperations instance
   *     has enough balance for the purchase
   *
   * @throws SQLException if a database access error occurs
   * @throws IOException if the required query files could not be read
   */
  public abstract boolean newPurchase(String article, int quantity)
      throws SQLException, IOException;

  /**
   * Cancel a purchase of the user associated to this ShopOperations instance.
   *
   * @param article name of the article to cancel
   * @throws SQLException if a database access error occurs
   * @throws IOException if the required query files could not be read
   */
  public abstract void cancelPurchase(String article) throws SQLException, IOException;

  /**
   * Selects all entries from the purchase history of the user associated with this instance.
   *
   * @return a ResultSet with the history
   * @throws SQLException if a database access error occurs
   * @throws IOException if the required query files could not be read
   */
  public abstract ResultSet selectHistory() throws SQLException, IOException;

  /**
   * Selects today's entries from the purchase history of the user associated with this instance.
   *
   * @return a ResultSet with the history
   * @throws SQLException if a database access error occurs
   * @throws IOException if the required query files could not be read
   */
  public abstract ResultSet selectHistoryToday() throws SQLException, IOException;
}
