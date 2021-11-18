package bunny.metrics.jmx.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import bunny.metrics.jmx.entity.MBean;

import java.sql.*;
import java.util.*;

public class TrinoMBeansRepository implements MBeansRepository {
    private static final String TRINO_JDBC_URL = "jdbc:trino://%s:%s";
    public static final String SCHEMA_CURRENT = "current";
    public static final String SCHEMA_HISTORY = "history";
    private final Connection connection;
    private final Logger logger;

    public TrinoMBeansRepository(Connection connection, Logger logger) {
        this.connection = connection;
        this.logger = logger;
    }

    @Override
    public List<MBean> getMBean(String mBeanName, boolean onlyHistory) {
        ArrayList<MBean> mBeans = new ArrayList<>();
        mBeanName = sanitize(mBeanName);
        String source = onlyHistory ? SCHEMA_HISTORY : SCHEMA_CURRENT;

        try {
            ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM jmx." + source + ".\"" + mBeanName + "\"");
            ResultSetMetaData metadata = rs.getMetaData();
            int columnCount = metadata.getColumnCount();

            if (rs.next()) {
                do {
                    HashMap<String, String> properties = new HashMap<>();
                    for (int i = 1; i <= columnCount; ++i) {
                        properties.put(metadata.getColumnName(i), rs.getString(i));
                    }
                    mBeans.add(new MBean(mBeanName, properties));
                } while (rs.next());
            }
        } catch (SQLException e) {
            logger.error("Error while fetching MBean results from Trino", e);
        }
        return mBeans;
    }

    @Override
    public ArrayList<String> getAllMBeansNames() {
        ArrayList<String> arrayList = new ArrayList<>();
        try {
            ResultSet rs = connection.createStatement().executeQuery("SHOW TABLES FROM jmx.current");
            while (rs.next()) {
                arrayList.add(rs.getString(1));
            }
        } catch (SQLException e) {
            logger.error("Error while fetching all mBeans names from Trino", e);
            e.printStackTrace();
        }
        return arrayList;
    }

    private String sanitize(String str) {
        return str.replaceAll("^\"|\"$", "");
    }

    public static TrinoMBeansRepository factory(String host, String port, HashMap<String, String> authentication) throws IllegalStateException {
        Properties properties = new Properties();
        authentication.forEach(properties::setProperty);

        try {
            Connection connection = DriverManager.getConnection(
                String.format(TRINO_JDBC_URL, host, port),
                properties
            );
            if (!connection.isValid(0)
                // Ping to validate connection
                || connection.prepareStatement("SELECT 1 FROM jmx.current") == null
            ) {
                throw new IllegalStateException("Unable to connect to Trino JMX");
            }
            return new TrinoMBeansRepository(connection, LoggerFactory.getLogger(TrinoMBeansRepository.class));
        } catch (Exception e) {
            throw new IllegalStateException("Error while connecting to Trino", e);
        }
    }
}
