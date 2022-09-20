package stats;

import org.apache.commons.math3.distribution.TDistribution;

/**
 * This class is used to calculate and save a confidence interval for
 * the mean and the mean difference.
 */
public final class ConfidenceInterval {

  /** Lower value of the interval, inclusive. */
  private final double lower;

  /** Upper value of the interval, inclusive. */
  private final double upper;

  /** The confidence level this interval was computed with. */
  private final double level;

  private ConfidenceInterval(double lower, double upper, double level) {
    this.lower = lower;
    this.upper = upper;
    this.level = level;
  }

  /**
   * This function calculates the mean of a series of real values.
   *
   * @param values the series of real values
   * @return the mean of the series
   */
  public static double getMean(double[] values) {
    int n = values.length;
    double m = 0;
    for (double value : values) {
      m += value;
    }
    return m / n;
  }

  /**
   * Returns the variance of a series of real values.
   *
   * @param values the series of real values
   * @param mean the mean of the series (preferably calculated by the funtion getMean())
   * @return the variance of the sereis
   */
  public static double getVariance(double[] values, double mean) {
    int n = values.length;
    if (n < 2) {
      throw new IllegalArgumentException();
    }
    double var = 0;
    for (double value : values) {
      var += (value - mean) * (value - mean);
    }
    return var / (n - 1);
  }

  /**
   * Computes a new confidence interval for the population mean of the given array of values.
   * The values must have been collected i.i.d..
   *
   * @param values an array of real values
   * @param level the confidence level, strictly between 0 and 1
   * @return a confidence interval for the population mean
   */
  public static ConfidenceInterval forMean(double[] values, double level) {
    if (Math.abs(0.5 - level) > 0.5) {
      throw new IllegalArgumentException();
    }
    int n = values.length;
    double mean = getMean(values);
    double var = getVariance(values, mean);
    double delta = new TDistribution(n - 1).inverseCumulativeProbability(level);
    return new ConfidenceInterval(mean - delta * Math.sqrt(var / n),
            mean + delta * Math.sqrt(var / n), level);
  }

  /**
   * Computes a new confidence interval for the difference
   * between the population means of the given two arrays.
   * The values in each array must have been collected i.i.d..
   *
   * @param values1 an array of real values from one population
   * @param values2 an array of real values from another population
   * @param level the confidence level, strictly between 0 and 1
   * @return a confidence interval for the difference between both population means
   */
  public static ConfidenceInterval forMeanDifference(double[] values1, double[] values2,
                                                     double level) {
    if (Math.abs(0.5 - level) > 0.5) {
      throw new IllegalArgumentException();
    }

    int n = values1.length;
    double mean1 = getMean(values1);
    double var1 = getVariance(values1, mean1);

    int m = values2.length;
    double mean2 = getMean(values2);
    double var2 = getVariance(values2, mean2);

    double d = mean1 - mean2;

    double normedVar1 = var1 / n;
    double normedVar2 = var2 / m;

    double degreesOfFreedom =
            (Math.pow((normedVar1 + normedVar2), 2) / (Math.pow(normedVar1, 2) / (n - 1)
                    + Math.pow(normedVar2, 2) / (m - 1)));
    double delta = new TDistribution(degreesOfFreedom).inverseCumulativeProbability(level);
    return new ConfidenceInterval(d - delta * Math.sqrt(normedVar1 + normedVar2),
            d + delta * Math.sqrt(normedVar1 + normedVar2), level);
  }

  public double getLower() {
    return lower;
  }

  public double getUpper() {
    return upper;
  }

  public double getLevel() {
    return level;
  }
}
