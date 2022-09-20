package shop;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class is a utility class for getting mock data from
 * the .data files and queries from the .sql files.
 */
public class ShopResource {

  /**
   * Returns an input stream to a data resource.
   *
   * @param name name of the data resource
   * @return input stream to the resource
   * @throws IOException if the name is invalid
   */
  public static InputStream getData(String name) throws IOException {
    String path = String.format("/data/%s.data", name);
    return ShopResource.class.getResourceAsStream(path);
  }

  /**
   * Returns the query identified by a name.
   *
   * @param name the name of the query
   * @return the query
   * @throws IOException if the name is invalid
   */
  public static String getQuery(String name) throws IOException {
    String path = String.format("/sql/%s.sql", name);
    InputStream s = ShopResource.class.getResourceAsStream(path);
    assert s != null;
    ByteArrayOutputStream result = new ByteArrayOutputStream();
    byte[] buffer = new byte[1024];
    int length;
    while ((length = s.read(buffer)) != -1) {
      result.write(buffer, 0, length);
    }
    // StandardCharsets.UTF_8.name() > JDK 7
    return result.toString("UTF-8");
  }
}