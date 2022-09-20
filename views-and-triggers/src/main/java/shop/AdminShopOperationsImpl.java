package shop;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;

/**
 * This class implements all shop operations which the admin executes.
 */
public class AdminShopOperationsImpl extends AdminShopOperations {
  private static final String CREATE_SHOP_DATABASE_FILE = "createShopDatabase";
  private static final String CREATE_TABLES_FILE = "createTables";
  private static final String SET_DATESTYLE_FILE = "setDatestyle";
  private static final String CUSTOMER_DATA = "customer";
  private static final String ARTICLE_DATA = "article";
  private static final String PURCHASE_DATA = "purchase";
  private static final String CREATE_ROLE_CUSTOMER_FILE = "createRoleCustomer";
  private static final String GET_CUSTOMERS_FILE = "getCustomers";
  private static final String CREATE_VIEW_HISTORY_FILE = "createViewHistory";
  private static final String GRANT_READ_ON_HISTORY_FILE = "grantReadOnHistory";
  private static final String CREATE_FUNCTION_NEW_PURCHASE_FILE = "createFunctionNewPurchase";
  private static final String CREATE_RULE_DELETE_HISTORY_FILE = "createRuleDeleteHistory";
  private static final String GET_BALANCE_FILE = "getBalance";
  private static final String SELECT_CUSTOMER_NAME_FILE = "selectCustomerName";
  private static final String SELECT_ARTICLE_NAME_FILE = "selectArticleName";
  private static final String SELECT_PURCHASE_ID_FILE = "selectPurchaseId";

  public AdminShopOperationsImpl(String url, String user, String password) throws SQLException {
    super(url, user, password);
  }

  @Override
  public void createShopDatabase() throws SQLException, IOException {
    prepareSelfClosingStatement(CREATE_SHOP_DATABASE_FILE).executeUpdate();
  }

  @Override
  public void createTables() throws SQLException, IOException {
    prepareSelfClosingStatement(CREATE_TABLES_FILE).executeUpdate();
  }

  @Override
  public void populateTables() throws SQLException, IOException {
    CopyManager manager = new CopyManager((BaseConnection) conn);
    prepareStatement(SET_DATESTYLE_FILE).executeUpdate();
    manager.copyIn("COPY customer FROM STDIN", ShopResource.getData(CUSTOMER_DATA));
    manager.copyIn("COPY article FROM STDIN", ShopResource.getData(ARTICLE_DATA));
    manager.copyIn("COPY purchase FROM STDIN", ShopResource.getData(PURCHASE_DATA));
  }

  @Override
  public void createUsers() throws SQLException, IOException {
    PreparedStatement createRoleCustomer = prepareSelfClosingStatement(CREATE_ROLE_CUSTOMER_FILE);
    createRoleCustomer.executeUpdate();

    PreparedStatement getCustomerStmt = prepareSelfClosingStatement(GET_CUSTOMERS_FILE);
    try (ResultSet res = getCustomerStmt.executeQuery()) {
      String customername;

      while (res.next()) {
        customername = res.getString(1);
        conn.prepareStatement(String.format("DROP USER IF EXISTS %s;", customername))
                .executeUpdate();
        conn.prepareStatement(String
                .format("CREATE USER %s WITH PASSWORD '%s' IN ROLE customer;",
                        customername, customername))
                .executeUpdate();
      }
    }
  }

  @Override
  public void createViewHistory() throws SQLException, IOException {
    PreparedStatement stmt = prepareSelfClosingStatement(CREATE_VIEW_HISTORY_FILE);
    stmt.executeUpdate();
    stmt = prepareSelfClosingStatement(GRANT_READ_ON_HISTORY_FILE);
    stmt.executeUpdate();

  }

  @Override
  public void createFunctionNewPurchase() throws SQLException, IOException {
    prepareSelfClosingStatement(CREATE_FUNCTION_NEW_PURCHASE_FILE).executeUpdate();
  }

  @Override
  public void createRuleDeleteHistory() throws SQLException, IOException {
    prepareSelfClosingStatement(CREATE_RULE_DELETE_HISTORY_FILE).executeUpdate();
  }

  @Override
  public int getBalance(String ofUser) throws SQLException, IOException {
    PreparedStatement stmt = prepareSelfClosingStatement(GET_BALANCE_FILE);
    stmt.setString(1, ofUser);
    try (ResultSet res = stmt.executeQuery()) {
      res.next();
      return res.getInt(1);
    }
  }

  @Override
  public ResultSet selectCustomerName() throws SQLException, IOException {
    PreparedStatement stmt = prepareSelfClosingStatement(SELECT_CUSTOMER_NAME_FILE);
    return stmt.executeQuery();
  }

  @Override
  public ResultSet selectArticleName() throws SQLException, IOException {
    PreparedStatement stmt = prepareSelfClosingStatement(SELECT_ARTICLE_NAME_FILE);
    return stmt.executeQuery();
  }

  @Override
  public ResultSet selectPurchaseId() throws SQLException, IOException {
    PreparedStatement stmt = prepareSelfClosingStatement(SELECT_PURCHASE_ID_FILE);
    return stmt.executeQuery();
  }
}
