package shop;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This class implements all operations that users of the webshop are allowed to execute.
 */
public class UserShopOperationsImpl extends UserShopOperations {
  private static final String NEW_PURCHASE_FILE = "newPurchase";
  private static final String CANCEL_PURCHASE_FILE = "cancelPurchase";
  private static final String SELECT_HISTORY_FILE = "selectHistory";
  private static final String SELECT_HISTORY_TODAY_FILE = "selectHistoryToday";

  public UserShopOperationsImpl(String url, String user, String password) throws SQLException {
    super(url, user, password);
  }

  @Override
  public boolean newPurchase(String article, int quantity) throws SQLException, IOException {
    PreparedStatement stmt = prepareSelfClosingStatement(NEW_PURCHASE_FILE);
    stmt.setString(1, article);
    stmt.setInt(2, quantity);
    try (ResultSet res = stmt.executeQuery()) {
      res.next();
      return res.getBoolean(1);
    }
  }

  @Override
  public void cancelPurchase(String article) throws SQLException, IOException {
    PreparedStatement stmt = prepareSelfClosingStatement(CANCEL_PURCHASE_FILE);
    stmt.setString(1, article);
    stmt.executeUpdate();
  }

  @Override
  public ResultSet selectHistory() throws SQLException, IOException {
    return prepareSelfClosingStatement(SELECT_HISTORY_FILE).executeQuery();
  }

  @Override
  public ResultSet selectHistoryToday() throws SQLException, IOException {
    return prepareSelfClosingStatement(SELECT_HISTORY_TODAY_FILE).executeQuery();
  }
}
