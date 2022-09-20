package charts.runtime;

import charts.ChartUpdateException;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import org.jfree.chart.ChartPanel;
import postgres.DbConnector;
import postgres.DefaultMondialDbConnector;


public class RuntimesChartDisplay extends JFrame {

  private static final int QUERY_ROWS = 10;

  private static final int QUERY_COLS = 0;

  private static final int MIN_OBSERVATIONS = 1;

  private static final int MAX_OBSERVATIONS = Integer.MAX_VALUE;

  private static final int DEFAULT_OBSERVATIONS = 100;

  private static final int OBSERVATIONS_STEP_SIZE = 1;

  private static final double MIN_CONFIDENCE = 0.0;

  private static final double MAX_CONFIDENCE = 1.0;

  private static final double DEFAULT_CONFIDENCE = 0.95;

  private static final double CONFIDENCE_STEP_SIZE = 0.005;

  private static final float MID_ALIGN = 0.5f;

  private  static final Dimension LARGE_GAP = new Dimension(10, 0);

  private  static final Dimension SMALL_GAP = new Dimension(5, 0);

  /**
   * Creates a new window for displaying runtimes charts.
   */
  public RuntimesChartDisplay() {
    super("Runtimes Visualizer");

    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    final JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

    JTextArea query1TextArea = new JTextArea("Enter first query", QUERY_ROWS,
        QUERY_COLS);
    query1TextArea.setLineWrap(true);
    JTextArea query2TextArea = new JTextArea("Enter second query", QUERY_ROWS,
        QUERY_COLS);
    query2TextArea.setLineWrap(true);
    JPanel queriesPanel = new JPanel(new GridLayout());
    queriesPanel.add(query1TextArea);
    queriesPanel.add(query2TextArea);
    mainPanel.add(queriesPanel);

    SpinnerNumberModel numObservationsModel = new SpinnerNumberModel(DEFAULT_OBSERVATIONS,
        MIN_OBSERVATIONS, MAX_OBSERVATIONS, OBSERVATIONS_STEP_SIZE);
    final JSpinner numObservationsSpinner = new JSpinner(numObservationsModel);
    final JLabel numObservationsLabel = new JLabel("Number of observations:");
    SpinnerNumberModel confidenceLevelModel = new SpinnerNumberModel(DEFAULT_CONFIDENCE,
        MIN_CONFIDENCE, MAX_CONFIDENCE, CONFIDENCE_STEP_SIZE);
    final JSpinner confidenceLevelSpinner = new JSpinner(confidenceLevelModel);
    final JLabel confidenceLevelLabel = new JLabel("Confidence level:");
    JButton runButton = new JButton("Compare execution times");
    runButton.setAlignmentX(MID_ALIGN);
    JPanel runPanel = new JPanel();
    runPanel.setLayout(new BoxLayout(runPanel, BoxLayout.X_AXIS));
    runPanel.add(Box.createHorizontalGlue());
    runPanel.add(numObservationsLabel);
    runPanel.add(Box.createRigidArea(SMALL_GAP));
    runPanel.add(numObservationsSpinner);
    runPanel.add(Box.createRigidArea(LARGE_GAP));
    runPanel.add(confidenceLevelLabel);
    runPanel.add(Box.createRigidArea(SMALL_GAP));
    runPanel.add(confidenceLevelSpinner);
    runPanel.add(Box.createRigidArea(LARGE_GAP));
    runPanel.add(runButton);
    mainPanel.add(runPanel);

    JPanel outerChartPanel = new JPanel();
    outerChartPanel.setLayout(new BoxLayout(outerChartPanel, BoxLayout.X_AXIS));
    DbConnector connector = new DefaultMondialDbConnector();
    RuntimesChartProvider chartProvider = new PostgresRuntimesChartProvider(connector);
    ChartPanel chartPanel = new ChartPanel(chartProvider.getRuntimesChart());
    chartPanel.setAlignmentX(MID_ALIGN);
    outerChartPanel.add(Box.createGlue());
    outerChartPanel.add(chartPanel);
    outerChartPanel.add(Box.createGlue());
    mainPanel.add(outerChartPanel);

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

    runButton.addActionListener(e -> {
      try {
        chartProvider.update(query1TextArea.getText(), query2TextArea.getText(),
            numObservationsModel.getNumber().intValue(),
            confidenceLevelModel.getNumber().doubleValue());
      } catch (ChartUpdateException exc) {
        logTextArea.setText(String.format("[Error] %s", exc.getMessage()));
      }
    });
  }

  public static void main(String[] args) {
    new RuntimesChartDisplay();
  }
}
