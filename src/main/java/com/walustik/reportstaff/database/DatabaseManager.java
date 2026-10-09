package com.walustik.reportstaff.database;

import com.walustik.reportstaff.ReportStaffPlugin;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

/**
 * Database connection manager with HikariCP connection pooling.
 * Supports SQLite and MySQL databases.
 *
 * @author Walustik
 * @version 1.1.0
 */
public final class DatabaseManager {

    private final ReportStaffPlugin plugin;
    private HikariDataSource dataSource;
    private final DatabaseType databaseType;

    /**
 * Initializes the DatabaseManager based on configuration.
 *
 * @param plugin The ReportStaff plugin instance
     */
    public DatabaseManager(ReportStaffPlugin plugin) {
        this.plugin = plugin;
        String typeStr = plugin.getConfig().getString("database.type", "SQLITE").toUpperCase();
        this.databaseType = DatabaseType.valueOf(typeStr);
    }

    /**
     * Initializes the database connection and creates tables if needed.
     */
    public void initialize() {
        try {
            switch (databaseType) {
                case SQLITE:
                    initializeSQLite();
                    break;
                case MYSQL:
                    initializeMySQL();
                    break;
            }
            createTablesIfNotExist();
            plugin.getLogger().info("Database initialized successfully: " + databaseType.name());
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to initialize database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Initializes SQLite database connection.
     */
    private void initializeSQLite() throws SQLException {
        String filePath = plugin.getConfig().getString("database.sqlite.file-path", "reports.db");
        File dbFile = new File(plugin.getDataFolder(), filePath);
        if (!dbFile.getParentFile().exists()) {
            dbFile.getParentFile().mkdirs();
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setMaximumPoolSize(5);
        config.setMinimumIdle(1);
        config.setAutoCommit(true);

        this.dataSource = new HikariDataSource(config);
    }

    /**
     * Initializes MySQL database connection.
     */
    private void initializeMySQL() throws SQLException {
        String host = plugin.getConfig().getString("database.mysql.host", "localhost");
        int port = plugin.getConfig().getInt("database.mysql.port", 3306);
        String database = plugin.getConfig().getString("database.mysql.database", "reportstaff");
        String username = plugin.getConfig().getString("database.mysql.username", "root");
        String password = plugin.getConfig().getString("database.mysql.password", "password");
        boolean ssl = plugin.getConfig().getBoolean("database.mysql.ssl", false);
        int maxPoolSize = plugin.getConfig().getInt("database.mysql.max-pool-size", 10);
        int minIdle = plugin.getConfig().getInt("database.mysql.min-idle-connections", 2);
        long connectionTimeout = plugin.getConfig().getLong("database.mysql.connection-timeout-ms", 30000);
        long idleTimeout = plugin.getConfig().getLong("database.mysql.idle-timeout-ms", 600000);

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(String.format(
                "jdbc:mysql://%s:%d/%s?useSSL=%s&serverTimezone=UTC&autoReconnect=true",
                host, port, database, ssl
        ));
        config.setUsername(username);
        config.setPassword(password);
        config.setConnectionTimeout(connectionTimeout);
        config.setIdleTimeout(idleTimeout);
        config.setMaxLifetime(1800000);
        config.setMaximumPoolSize(maxPoolSize);
        config.setMinimumIdle(minIdle);
        config.setAutoCommit(true);

        this.dataSource = new HikariDataSource(config);
    }

    /**
     * Creates necessary tables if they don't exist.
     */
    private void createTablesIfNotExist() {
        try (Connection connection = getConnection()) {
            String createTableSQL = databaseType == DatabaseType.SQLITE ? getSQLiteCreateTableSQL() : getMySQLCreateTableSQL();
            connection.createStatement().executeUpdate(createTableSQL);
        } catch (SQLException e) {
            plugin.getLogger().warning("Table creation check failed: " + e.getMessage());
        }
    }

    /**
     * Gets SQLite table creation SQL.
     */
    private String getSQLiteCreateTableSQL() {
        return "CREATE TABLE IF NOT EXISTS reports ("
                + "id TEXT PRIMARY KEY,"
                + "reporter_uuid TEXT NOT NULL,"
                + "target_uuid TEXT NOT NULL,"
                + "reason TEXT NOT NULL,"
                + "timestamp LONG NOT NULL,"
                + "status TEXT DEFAULT 'OPEN'"
                + ")";
    }

    /**
     * Gets MySQL table creation SQL.
     */
    private String getMySQLCreateTableSQL() {
        return "CREATE TABLE IF NOT EXISTS reports ("
                + "id VARCHAR(36) PRIMARY KEY,"
                + "reporter_uuid VARCHAR(36) NOT NULL,"
                + "target_uuid VARCHAR(36) NOT NULL,"
                + "reason LONGTEXT NOT NULL,"
                + "timestamp BIGINT NOT NULL,"
                + "status VARCHAR(20) DEFAULT 'OPEN',"
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "INDEX idx_status (status),"
                + "INDEX idx_target (target_uuid)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
    }

    /**
     * Gets a connection from the connection pool.
     *
     * @return a database connection
     * @throws SQLException if connection fails
     */
    public Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("Database connection pool not initialized");
        }
        return dataSource.getConnection();
    }

    /**
     * Closes the database connection pool.
     */
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            plugin.getLogger().info("Database connection pool closed.");
        }
    }

    /**
     * Gets the current database type.
     *
     * @return the database type
     */
    public DatabaseType getDatabaseType() {
        return databaseType;
    }

    /**
     * Checks if the database connection is healthy.
     *
     * @return true if connection is active, false otherwise
     */
    public boolean isConnected() {
        try (Connection connection = getConnection()) {
            return !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Database type enumeration.
     */
    public enum DatabaseType {
        SQLITE,
        MYSQL
    }
}
