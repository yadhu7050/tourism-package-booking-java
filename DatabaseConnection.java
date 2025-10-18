import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection{

    private static final String PROPERTIES_FILE = "database.properties";
    private static Properties properties = new Properties();

    static {
        try (FileInputStream fis = new FileInputStream(PROPERTIES_FILE)) {
            properties.load(fis);
        } catch (IOException e) {
            System.err.println("Could not load database.properties file. Using defaults.");
            // Set default properties if file not found
            properties.setProperty("db.url", "jdbc:mysql://localhost:3306/tourism_db");
            properties.setProperty("db.user", "root");
            properties.setProperty("db.password", "");
        }
    }

    public static Connection getConnection() throws SQLException {
        String url = properties.getProperty("db.url", "jdbc:mysql://localhost:3306/tourism_db");
        String user = properties.getProperty("db.user", "root");
        String password = properties.getProperty("db.password", "");
        
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found.", e);
        }
        
        return DriverManager.getConnection(url, user, password);
    }
    
    // Test connection
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
            return false;
        }
    }
}
