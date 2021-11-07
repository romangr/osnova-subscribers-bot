package loader;

import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import org.quartz.impl.jdbcjobstore.StdJDBCDelegate;
import org.quartz.spi.ClassLoadHelper;
import org.slf4j.Logger;

public class SqlLiteJdbcDelegate extends StdJDBCDelegate {

  public SqlLiteJdbcDelegate(Logger logger, String tablePrefix, String schedName,
      String instanceId, ClassLoadHelper classLoadHelper) {
    super(logger, tablePrefix, schedName, instanceId, classLoadHelper);
  }

  public SqlLiteJdbcDelegate(Logger logger, String tablePrefix, String schedName,
      String instanceId, ClassLoadHelper classLoadHelper, Boolean useProperties) {
    super(logger, tablePrefix, schedName, instanceId, classLoadHelper, useProperties);
  }

  @Override
  protected Object getObjectFromBlob(ResultSet rs, String colName) throws IOException {
    try (InputStream dataStream = rs.getBinaryStream(colName)) {
      Properties properties = new Properties();
      properties.load(dataStream);
      return properties;
    } catch (SQLException e) {
      System.out.println("Exception getting object from the blob");
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  @Override
  protected Object getJobDataFromBlob(ResultSet rs, String colName) throws SQLException {
    return rs.getBinaryStream(colName);
  }
}
