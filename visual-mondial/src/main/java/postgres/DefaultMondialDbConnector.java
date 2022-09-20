package postgres;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Utility class used to connect to the mondial database located at the weatherrtop-server.
 */
public class DefaultMondialDbConnector implements DbConnector {
  private static final String url = "jdbc:postgresql://localhost:63333/mondial";
  private static final String user = "dummy";
  private static final String password = "dummy";


  public Connection getConnection() throws SQLException {
    return DriverManager.getConnection(url, user, password);
  }
}
