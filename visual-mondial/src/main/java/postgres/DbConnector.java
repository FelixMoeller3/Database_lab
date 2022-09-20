package postgres;

import java.sql.Connection;
import java.sql.SQLException;

public interface DbConnector {
  Connection getConnection() throws SQLException;
}
