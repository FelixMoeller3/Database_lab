package shop;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * This class represents database operations of an online shop.
 * One instantiates this class for a particular database URL and user.
 * Subclasses should use this as a basis to implement operations for particular types of users.
 */
public abstract class ShopOperations implements AutoCloseable {

  protected final Connection conn;

  public ShopOperations(String url, String user, String password)
      throws SQLException {
    this(DriverManager.getConnection(url, user, password));
  }

  public ShopOperations(Connection conn) {
    this.conn = conn;
  }

  protected PreparedStatement prepareStatement(String name) throws SQLException, IOException {
    return this.conn.prepareStatement(ShopResource.getQuery(name));
  }

  protected PreparedStatement prepareSelfClosingStatement(String name)
      throws SQLException, IOException {
    PreparedStatement s = this.prepareStatement(name);
    s.closeOnCompletion();
    return s;
  }

  @Override
  public void close() throws SQLException {
    this.conn.close();
  }
}