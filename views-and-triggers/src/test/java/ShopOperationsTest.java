import java.io.IOException;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import shop.AdminShopOperations;
import shop.AdminShopOperationsImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import shop.UserShopOperations;
import shop.UserShopOperationsImpl;


public class ShopOperationsTest {
  private static final List<Integer> expectedIds = new ArrayList<>(Arrays.asList(304,184,120,54));
  private static final List<Date> expectedDates =
          new ArrayList<>(Arrays.asList(Date.valueOf("2014-08-01"),Date.valueOf("2014-05-20"),Date.valueOf("2014-03-25"),Date.valueOf("2014-02-09")));
  private static final List<String> expectedArticles = new ArrayList<>(Arrays.asList("Toner_135","Toner_259","Toner_216","Toner_159"));
  private static final List<Integer> expectedQuantities = new ArrayList<>(Arrays.asList(5,7,2,6));
  private static final List<Integer> expectedPrices = new ArrayList<>(Arrays.asList(135,336,82,282));

  private static final String BASE_URL = "jdbc:postgresql://localhost/";
  private static final String SHOP_URL = BASE_URL + "shop";


  @Test
  public void testScenario() throws SQLException, IOException {
    try (AdminShopOperations op = new AdminShopOperationsImpl(BASE_URL, "admin", "admin")) {
      op.createShopDatabase();
    }

    try (AdminShopOperations op = new AdminShopOperationsImpl(SHOP_URL, "admin", "admin")) {
      op.createTables();

      op.populateTables();

      op.createUsers();
      assertPaulHasNoAccess();

      op.createViewHistory();
      assertEmilieSeesHistory();

      op.createFunctionNewPurchase();
      assertEmilieCanPurchaseToner();

      op.createRuleDeleteHistory();
      assertEmilieCanCancelPurchase(op);
    }
  }

  private void assertEmilieSeesHistory() throws IOException, SQLException {
    List<Integer> id = new ArrayList<>();
    List<Date> date = new ArrayList<>();
    List<String> article = new ArrayList<>();
    List<Integer> quantity = new ArrayList<>();
    List<Integer> price = new ArrayList<>();
    try (UserShopOperations ops = new UserShopOperationsImpl(SHOP_URL,"emilie","emilie")) {
      try(ResultSet res = ops.selectHistory()) {
        while(res.next()) {
          id.add(res.getInt(1));
          date.add(res.getDate(2));
          article.add(res.getString(3));
          quantity.add(res.getInt(4));
          price.add(res.getInt(5));
        }
      }
    }
    Assertions.assertEquals(expectedIds,id);
    Assertions.assertEquals(expectedDates,date);
    Assertions.assertEquals(expectedArticles,article);
    Assertions.assertEquals(expectedQuantities,quantity);
    Assertions.assertEquals(expectedPrices,price);
  }

  private void assertPaulHasNoAccess() throws SQLException {
    try (AdminShopOperations ops = new AdminShopOperationsImpl(SHOP_URL,"paul", "paul")) {
      SQLException currentException = Assertions.assertThrows(SQLException.class, ops::selectArticleName);
      Assertions.assertEquals("ERROR: permission denied for table article", currentException.getMessage());

      currentException = Assertions.assertThrows(SQLException.class, ops::selectCustomerName);
      Assertions.assertEquals("ERROR: permission denied for table customer", currentException.getMessage());

      currentException = Assertions.assertThrows(SQLException.class, ops::selectPurchaseId);
      Assertions.assertEquals("ERROR: permission denied for table purchase", currentException.getMessage());
    }
  }

  private void assertEmilieCanPurchaseToner() throws SQLException, IOException {
    try (UserShopOperations ops = new UserShopOperationsImpl(SHOP_URL,"emilie", "emilie")) {
      Assertions.assertTrue(ops.newPurchase("Toner_216", 10));
      try(ResultSet res = ops.selectHistoryToday()) {
        res.next();
        Assertions.assertEquals("Toner_216", res.getString(3));
        Assertions.assertEquals(10,res.getInt(4));
      }
      Assertions.assertFalse(ops.newPurchase("Toner_159", 2));
      /*
      * we only need to check the first entry because the entries are sorted by
      * process number and a new purchase always has the highest purchase number
      */
      try(ResultSet res = ops.selectHistoryToday()) {
        res.next();
        Assertions.assertEquals("Toner_216", res.getString(3));
        Assertions.assertEquals(10,res.getInt(4));
      }
    }
  }

  private void assertEmilieCanCancelPurchase(AdminShopOperations adminOp) throws IOException, SQLException {
    int previousBalance = adminOp.getBalance("emilie");
    try (UserShopOperations userOp = new UserShopOperationsImpl(SHOP_URL,"emilie", "emilie")) {
      userOp.cancelPurchase("Toner_216");
      try (ResultSet res = userOp.selectHistoryToday()) {
        while(res.next()) {
          Assertions.assertNotEquals("Toner_216", res.getString(3));
        }
      }
    }
    Assertions.assertEquals(previousBalance, adminOp.getBalance("emilie") - 410);

  }
}
