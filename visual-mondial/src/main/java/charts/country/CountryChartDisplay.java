package charts.country;

import charts.ChartUpdateException;
import java.awt.BorderLayout;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import org.jfree.chart.ChartPanel;
import postgres.DbConnector;
import postgres.DefaultMondialDbConnector;

public class CountryChartDisplay extends JFrame {

  /**
   * Creates a new window for displaying country charts.
   */
  public CountryChartDisplay() {
    super("Country Visualizer");

    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

    JTextField countryNameTextField = new JTextField();
    JButton queryButton = new JButton("Query");
    JPanel countryNamePanel = new JPanel();
    countryNamePanel.setLayout(new BoxLayout(countryNamePanel, BoxLayout.X_AXIS));
    countryNamePanel.add(countryNameTextField);
    countryNamePanel.add(queryButton);
    mainPanel.add(countryNamePanel);

    DbConnector connector = new DefaultMondialDbConnector();
    CountryChartProvider chartProvider = new MondialCountryChartProvider(connector);
    ChartPanel languagesChartPanel = new ChartPanel(chartProvider.getLanguagesChart());
    ChartPanel religionsChartPanel = new ChartPanel(chartProvider.getReligionsChart());
    ChartPanel citiesChartPanel = new ChartPanel(chartProvider.getCitiesChart());
    JPanel allChartsPanel = new JPanel();
    allChartsPanel.setLayout(new BoxLayout(allChartsPanel, BoxLayout.X_AXIS));
    allChartsPanel.add(languagesChartPanel);
    allChartsPanel.add(religionsChartPanel);
    allChartsPanel.add(citiesChartPanel);
    mainPanel.add(allChartsPanel);

    JTextField logLabelTextField = new JTextField("Status:");
    logLabelTextField.setEditable(false);
    logLabelTextField.setAlignmentX(.0f);
    JTextArea logTextArea = new JTextArea("Waiting for a query.");
    logTextArea.setEditable(false);
    JPanel logPanel = new JPanel(new BorderLayout(10, 0));
    logPanel.add(logLabelTextField, BorderLayout.LINE_START);
    logPanel.add(logTextArea, BorderLayout.CENTER);
    mainPanel.add(logPanel);

    this.setContentPane(mainPanel);
    this.pack();
    this.setVisible(true);

    queryButton.addActionListener(e -> {
      try {
        chartProvider.update(countryNameTextField.getText());
        logTextArea.setText("[Success]");
      } catch (ChartUpdateException exc) {
        logTextArea.setText(String.format("[Error] %s", exc.getMessage()));
      }
    });
  }

  public static void main(String[] args) {
    new CountryChartDisplay();
  }
}
