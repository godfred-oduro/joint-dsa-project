package gh.ug.hospital.db;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Owner: 22015166
 *
 * Opens and closes the connection to MySQL.
 *
 * The database details live in config.properties, which is not committed to
 * the repository because it holds a password. Copy config.properties.example,
 * rename it, and fill in your own details.
 *
 * Note on java.util.Properties: it is a file reader, not a collection of the
 * kind constraint C19 bans, and it is only used here in the db package, where
 * ordinary Java library code is allowed.
 */
public class ConnectionManager {

    private static final String CONFIG_FILE = "config.properties";

    private static String url;
    private static String user;
    private static String password;
    private static boolean loaded = false;

    /** Reads config.properties. Called automatically the first time it is needed. */
    public static void loadSettings() {
        if (loaded) {
            return;
        }
        Properties settings = new Properties();

        InputStream in = null;
        try {
            in = new FileInputStream(CONFIG_FILE);
            settings.load(in);
        } catch (IOException problem) {
            throw new IllegalStateException(
                    "Could not read " + CONFIG_FILE + ". Copy "
                            + "config.properties.example, rename the copy to "
                            + CONFIG_FILE + ", and put your own database details in it.",
                    problem);
        } finally {
            closeQuietly(in);
        }

        url = settings.getProperty("db.url");
        user = settings.getProperty("db.user");
        password = settings.getProperty("db.password");

        if (url == null || user == null) {
            throw new IllegalStateException(
                    CONFIG_FILE + " must set db.url, db.user and db.password.");
        }
        loaded = true;
    }

    /**
     * Opens a connection. Whoever calls this must close it, and the easiest
     * way is a try-with-resources block.
     */
    public static Connection open() throws SQLException {
        loadSettings();
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Checks the database is reachable and reports back in plain words.
     * Menu option M1 uses this.
     */
    public static String testConnection() {
        Connection connection = null;
        try {
            connection = open();
            String product = connection.getMetaData().getDatabaseProductName();
            String version = connection.getMetaData().getDatabaseProductVersion();
            return "Connected to " + product + " " + version + " at " + url;
        } catch (SQLException problem) {
            return "Could not connect: " + problem.getMessage();
        } catch (IllegalStateException problem) {
            return problem.getMessage();
        } finally {
            closeQuietly(connection);
        }
    }

    /** Lets the tests point at a different database without a config file. */
    public static void useSettings(String newUrl, String newUser, String newPassword) {
        url = newUrl;
        user = newUser;
        password = newPassword;
        loaded = true;
    }

    private static void closeQuietly(InputStream stream) {
        if (stream == null) {
            return;
        }
        try {
            stream.close();
        } catch (IOException ignored) {
            // Nothing useful to do if closing a file fails.
        }
    }

    private static void closeQuietly(Connection connection) {
        if (connection == null) {
            return;
        }
        try {
            connection.close();
        } catch (SQLException ignored) {
            // Same here.
        }
    }
}
