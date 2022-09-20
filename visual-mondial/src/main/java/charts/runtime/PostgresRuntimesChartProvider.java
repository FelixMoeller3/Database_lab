package charts.runtime;

import charts.ChartUpdateException;
import java.awt.Color;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.IntervalBarRenderer;
import org.jfree.data.category.DefaultIntervalCategoryDataset;
import postgres.DbConnector;
import stats.ConfidenceInterval;


/**
 * This class is used to provide a chart comparing the runtimes of two queries.
 */
public class PostgresRuntimesChartProvider implements RuntimesChartProvider {
  private static final String[] categories = {"Query 1", "Query 2", "Diff"};
  private static final String plotTitle = "Comparison of Execution Times";
  private static final String xAxisTitle = "Measurement Type";
  private static final String yAxisTitle = "Time [ms]";
  private final DbConnector connector;
  private JFreeChart runTimesChart;

  public PostgresRuntimesChartProvider(DbConnector connector) {
    this.connector = connector;
  }

  @Override
  public JFreeChart getRuntimesChart() {
    if (runTimesChart != null) {
      return runTimesChart;
    }
    double [][] dummy1 = {{0.4, 0.5, 0.6}};
    double [][] dummy2 = {{0.5, 0.6, 0.7}};
    DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(dummy1, dummy2);
    dataset.setCategoryKeys(categories);
    CategoryAxis xaxis = new CategoryAxis(xAxisTitle);
    NumberAxis yaxis = new NumberAxis(yAxisTitle);
    IntervalBarRenderer renderer = new IntervalBarRenderer();
    renderer.setSeriesPaint(0, new Color(51, 102, 153));
    CategoryPlot runTimesPlot = new CategoryPlot(dataset, xaxis, yaxis, renderer);
    runTimesChart = new JFreeChart(plotTitle, runTimesPlot);
    return runTimesChart;
  }

  /**
   *  Executes a query numObservations times and
   *  returns an array containing execution times of each execution.
   *
   * @param query The sql-query which is executed.
   * @param numObservations The amount of executions to be done
   * @param conn a connection to the sql database named "mondial"
   * @return an array containing execution times of each execution
   * @throws SQLException an SQLException is thrown in case any error happens during execution
   *     of the query (e.g. when the query is not a valid sql query).
   */
  public double[] getObservationsForQuery(String query, int numObservations, Connection conn)
          throws SQLException {
    double [] executionTimes = new double[numObservations];
    for (int i = 0; i < numObservations; i++) {
      try (PreparedStatement stmt = conn.prepareStatement("EXPLAIN ANALYZE " + query,
              ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
        try (ResultSet res = stmt.executeQuery()) {
          res.last();
          executionTimes[i] = Double.parseDouble(
                  res.getString(1).split(" ")[2]);
        }
      }
    }
    return executionTimes;
  }

  @Override
  public void update(String query1, String query2, int numObservations, double conf)
          throws ChartUpdateException {
    if (numObservations < 1 || Math.abs(1 - conf) > 0.5) {
      throw new ChartUpdateException("Number of observations or confidence level invalid");
    }
    try (Connection conn = connector.getConnection()) {
      double [] executionTimes1 = getObservationsForQuery(query1, numObservations, conn);
      double [] executionTimes2 = getObservationsForQuery(query2, numObservations, conn);
      ConfidenceInterval confIntervalQuery1 = ConfidenceInterval.forMean(executionTimes1, conf);
      ConfidenceInterval confIntervalQuery2 = ConfidenceInterval.forMean(executionTimes2, conf);
      ConfidenceInterval confIntervalDiff =
              ConfidenceInterval.forMeanDifference(executionTimes1, executionTimes2, conf);

      CategoryPlot runTimesPlot = runTimesChart.getCategoryPlot();
      double[][] lowerBoundaries =
            {{confIntervalQuery1.getLower(), confIntervalQuery2.getLower(),
                confIntervalDiff.getLower()}};
      double[][] upperBoundaries =
            {{confIntervalQuery1.getUpper(), confIntervalQuery2.getUpper(),
                confIntervalDiff.getUpper()}};
      DefaultIntervalCategoryDataset runTimesDataset =
            new DefaultIntervalCategoryDataset(lowerBoundaries, upperBoundaries);
      runTimesDataset.setCategoryKeys(categories);
      runTimesPlot.setDataset(runTimesDataset);
    } catch (SQLException exc) {
      throw new ChartUpdateException("Failed to update chart. Reason: " + exc.getMessage());
    }
  }

}
