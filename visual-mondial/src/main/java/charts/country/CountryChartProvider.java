package charts.country;

import charts.ChartUpdateException;
import org.jfree.chart.JFreeChart;

public interface CountryChartProvider {
  void update(String countryName) throws ChartUpdateException;

  JFreeChart getLanguagesChart();

  JFreeChart getReligionsChart();

  JFreeChart getCitiesChart();
}
