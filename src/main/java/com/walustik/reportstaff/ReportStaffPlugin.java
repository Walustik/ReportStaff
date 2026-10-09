package com.walustik.reportstaff;

import com.walustik.reportstaff.commands.ReportCommand;
import com.walustik.reportstaff.commands.ReportsCommand;
import com.walustik.reportstaff.commands.RSReloadCommand;
import com.walustik.reportstaff.database.DatabaseManager;
import com.walustik.reportstaff.listeners.InventoryClickListener;
import com.walustik.reportstaff.listeners.PlayerChatListener;
import com.walustik.reportstaff.managers.CooldownManager;
import com.walustik.reportstaff.managers.DiscordWebhookManager;
import com.walustik.reportstaff.managers.LanguageManager;
import com.walustik.reportstaff.managers.ReportManager;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Main plugin class for ReportStaff.
 * Handles plugin lifecycle, initialization, and component management.
 *
 * @author Walustik
 * @version 1.1.0
 */
public final class ReportStaffPlugin extends JavaPlugin {

    private static ReportStaffPlugin instance;
    private LanguageManager languageManager;
    private ReportManager reportManager;
    private CooldownManager cooldownManager;
    private DiscordWebhookManager discordWebhookManager;
    private DatabaseManager databaseManager;
    private ExecutorService asyncExecutor;
    private final Map<UUID, UUID> pendingCustomReasons = new HashMap<>();
    private NamespacedKey reportIdKey;
    private NamespacedKey actionKey;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        reportIdKey = new NamespacedKey(this, "report_id");
        actionKey = new NamespacedKey(this, "report_action");

        // Initialize async executor with virtual threads (Java 21 feature)
        asyncExecutor = Executors.newVirtualThreadPerTaskExecutor();

        // Initialize database first
        initializeDatabase();

        // Reload plugin configuration and managers
        reloadPlugin();

        // Register commands
        getCommand("report").setExecutor(new ReportCommand(this));
        getCommand("reports").setExecutor(new ReportsCommand(this));
        getCommand("rsreload").setExecutor(new RSReloadCommand(this));

        // Register event listeners
        getServer().getPluginManager().registerEvents(new InventoryClickListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerChatListener(this), this);

        getLogger().info("================================================");
        getLogger().info("ReportStaff v1.1.0 enabled successfully");
        getLogger().info("Database Type: " + databaseManager.getDatabaseType().name());
        getLogger().info("================================================");
    }

    @Override
    public void onDisable() {
        // Save pending reports and cleanup
        if (reportManager != null) {
            reportManager.invalidateCache();
        }

        // Close database connection
        if (databaseManager != null) {
            databaseManager.close();
        }

        // Shutdown async executor
        if (asyncExecutor != null && !asyncExecutor.isShutdown()) {
            asyncExecutor.shutdown();
        }

        pendingCustomReasons.clear();
        getLogger().info("ReportStaff v1.1.0 disabled.");
    }

    /**
     * Initializes the database manager and connection.
     */
    private void initializeDatabase() {
        try {
            databaseManager = new DatabaseManager(this);
            databaseManager.initialize();
            if (databaseManager.isConnected()) {
                getLogger().info("Database connection established successfully");
            } else {
                getLogger().warning("Database connection test failed");
            }
        } catch (Exception e) {
            getLogger().severe("Failed to initialize database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Reloads plugin configuration and all managers.
     */
    public void reloadPlugin() {
        reloadConfig();

        languageManager = new LanguageManager(this);
        cooldownManager = new CooldownManager();
        discordWebhookManager = new DiscordWebhookManager(this);

        if (reportManager == null) {
            reportManager = new ReportManager(this);
        }

        // Load reports from database
        reportManager.loadAllReports().thenRun(() =>
                getLogger().info("Plugin configuration and reports reloaded.")).exceptionally(e -> {
            getLogger().severe("Failed to reload reports: " + e.getMessage());
            return null;
        });
    }

    /**
     * Gets the singleton instance of the plugin.
     */
    public static ReportStaffPlugin getInstance() {
        return instance;
    }

    // Getter methods for component managers

    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    public ReportManager getReportManager() {
        return reportManager;
    }

    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }

    public DiscordWebhookManager getDiscordWebhookManager() {
        return discordWebhookManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public ExecutorService getAsyncExecutor() {
        return asyncExecutor;
    }

    public Map<UUID, UUID> getPendingCustomReasons() {
        return pendingCustomReasons;
    }

    public NamespacedKey getReportIdKey() {
        return reportIdKey;
    }

    public NamespacedKey getActionKey() {
        return actionKey;
    }
}
