package charts.country;

import charts.ChartUpdateException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import postgres.DbConnector;


/**
 * This class provides a chart containing information
 * about cities,religions and languages in a given country.
 */
public class MondialCountryChartProvider implements CountryChartProvider {
  public static final String languageQuery = "SELECT language.name, language.percentage "
          + "FROM mondial.language "
          + "WHERE language.country = ("
          + "SELECT country.code "
          + "FROM mondial.country "
          + "WHERE country.name = ?"
          + ")";
  public static final String religionsQuery = "SELECT religion.name, religion.percentage "
          + "FROM mondial.religion "
          + "WHERE religion.country = ("
          + "SELECT country.code "
          + "FROM mondial.country "
          + "WHERE country.name = ?"
          + ")";
  public static final String ethnicGroupsQuery = "SELECT ethnicgroup.name, ethnicgroup.percentage "
          + "FROM mondial.ethnicgroup "
          + "WHERE ethnicgroup.country = ("
          + "SELECT country.code "
          + "FROM mondial.country "
          + "WHERE country.name = ?"
          + ")";
  public static final String citiesQuery = "SELECT city.name, city.population, CASE "
          + "WHEN city.name = (SELECT capital FROM mondial.country WHERE country.name= ?) "
          + "THEN true "
          + "ELSE false "
          + "END AS isCapital "
          + "FROM mondial.city "
          + "WHERE city.country = ("
          + "SELECT country.code "
          + "FROM mondial.country "
          + "WHERE country.name = ?"
          + ") "
          + "AND city.population IS NOT NULL "
          + "ORDER BY city.population DESC "
          + "LIMIT 10";
  private final DbConnector connector;
  private String countryName;
  private JFreeChart languagesChart;
  private JFreeChart religionsChart;
  private JFreeChart citiesChart;

  public MondialCountryChartProvider(DbConnector connector) {
    this.connector = connector;
    this.countryName = " ";
  }

  /**
   * Returns a dataset containing all languages spoken in a given country.
   *
   * @param conn a connection to the sql database named "mondial".
   * @return a dataset containing all languages spoken in a given country
   * @throws SQLException an SQLException is thrown in case any error happens during execution
   *     of the query (e.g. when the query is not a valid sql query).
   */
  public DefaultCategoryDataset getLanguageDataset(Connection conn) throws SQLException {
    try (PreparedStatement stmt = conn.prepareStatement(languageQuery)) {
      stmt.setString(1, countryName);
      try (ResultSet res = stmt.executeQuery()) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        while (res.next()) {
          dataset.setValue(res.getDouble(2), "Language", res.getString(1));
        }
        return dataset;
      }
    }
  }

  /**
   * Returns a dataset containing all religions and ethnic groups present in a given country.
   *
   * @param conn a connection to the sql database named "mondial".
   * @return a dataset containing all religions and ethnic groups present in a given country
   * @throws SQLException an SQLException is thrown in case any error happens during execution
   *     of the query (e.g. when the query is not a valid sql query).
   */
  public DefaultCategoryDataset getReligionsDataset(Connection conn) throws SQLException {
    DefaultCategoryDataset dataset = new DefaultCategoryDataset();
    try (PreparedStatement reliStmt = conn.prepareStatement(religionsQuery)) {
      reliStmt.setString(1, countryName);
      try (ResultSet res = reliStmt.executeQuery()) {
        while (res.next()) {
          dataset.setValue(res.getDouble(2), "Religion", res.getString(1));
        }
      }
    }

    try (PreparedStatement ethnicityStmt = conn.prepareStatement(ethnicGroupsQuery)) {
      ethnicityStmt.setString(1, countryName);
      try (ResultSet res = ethnicityStmt.executeQuery()) {
        while (res.next()) {
          dataset.setValue(res.getDouble(2), "Ethnic Group", res.getString(1));
        }
      }
    }
    return dataset;
  }

  /**
   * Returns a dataset containing the 10 largest cities of a given country.
   *
   * @param conn a connection to the sql database named "mondial".
   * @return a dataset containing the 10 largest cities of a given country
   * @throws SQLException an SQLException is thrown in case any error happens during execution
   *     of the query (e.g. when the query is not a valid sql query).
   */
  public DefaultCategoryDataset getCitiesDataset(Connection conn) throws SQLException {
    DefaultCategoryDataset dataset = new DefaultCategoryDataset();
    try (PreparedStatement cityStmt = conn.prepareStatement(citiesQuery)) {
      cityStmt.setString(1, countryName);
      cityStmt.setString(2, countryName);
      try (ResultSet res = cityStmt.executeQuery()) {
        while (res.next()) {
          String rowKey = res.getBoolean(3) ? "Capital" : "City";
          dataset.setValue(res.getDouble(2), rowKey, res.getString(1));
        }
      }
    }
    return dataset;
  }

  @Override
  public void update(String countryName) throws ChartUpdateException {
    this.countryName = countryName;
    try (Connection conn = connector.getConnection()) {
      if (languagesChart != null) {
        languagesChart.setTitle("Languages spoken in " + countryName);
        CategoryPlot languagePlot = languagesChart.getCategoryPlot();
        languagePlot.setDataset(getLanguageDataset(conn));
      }
      if (religionsChart != null) {
        religionsChart.setTitle("Religions and ethnic groups present in " + countryName);
        CategoryPlot religionsPlot = religionsChart.getCategoryPlot();
        religionsPlot.setDataset(getReligionsDataset(conn));
      }

      if (citiesChart != null) {
        citiesChart.setTitle("10 Largest Cities in " + countryName);
        CategoryPlot citiesPlot = citiesChart.getCategoryPlot();
        citiesPlot.setDataset(getCitiesDataset(conn));
      }
    } catch (SQLException exc) {
      throw new ChartUpdateException("Failed to update charts. Reason: " + exc.getMessage());
    }
  }

  @Override
  public JFreeChart getLanguagesChart() {
    if (languagesChart != null) {
      return languagesChart;
    }
    try (Connection conn = connector.getConnection()) {
      languagesChart = ChartFactory.createBarChart("Languages spoken in " + countryName,
              "Language", "Percentage", getLanguageDataset(conn),
              PlotOrientation.HORIZONTAL, true, true, false);
      return languagesChart;
    } catch (SQLException exc) {
      return null;
    }
  }

  @Override
  public JFreeChart getReligionsChart() {
    if (religionsChart != null) {
      return religionsChart;
    }
    try (Connection conn = connector.getConnection()) {
      religionsChart = ChartFactory.createBarChart(
              "Religions and ethnic groups present in " + countryName, "Religion/Ethnic Group",
              "Percentage", getReligionsDataset(conn), PlotOrientation.HORIZONTAL,
              true, true, false);
      return religionsChart;
    } catch (SQLException exc) {
      return null;
    }
  }

  @Override
  public JFreeChart getCitiesChart() {
    if (citiesChart != null) {
      return citiesChart;
    }
    try (Connection conn = connector.getConnection()) {
      citiesChart = ChartFactory.createBarChart("10 Largest Cities in " + countryName,
              "City", "Population", getCitiesDataset(conn),
              PlotOrientation.HORIZONTAL, true, true, false);
      return citiesChart;
    } catch (SQLException exc) {
      return null;
    }
  }
}
