package com.walustik.reportstaff;

import com.walustik.reportstaff.commands.ReportCommand;
import com.walustik.reportstaff.commands.ReportsCommand;
import com.walustik.reportstaff.commands.RSReloadCommand;
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

public final class ReportStaffPlugin extends JavaPlugin {

    private static ReportStaffPlugin instance;
    private LanguageManager languageManager;
    private ReportManager reportManager;
    private CooldownManager cooldownManager;
    private DiscordWebhookManager discordWebhookManager;
    private final Map<UUID, UUID> pendingCustomReasons = new HashMap<>();
    private NamespacedKey reportIdKey;
    private NamespacedKey actionKey;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        reportIdKey = new NamespacedKey(this, "report_id");
        actionKey = new NamespacedKey(this, "report_action");

        reloadPlugin();

        getCommand("report").setExecutor(new ReportCommand(this));
        getCommand("reports").setExecutor(new ReportsCommand(this));
        getCommand("rsreload").setExecutor(new RSReloadCommand(this));

        getServer().getPluginManager().registerEvents(new InventoryClickListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerChatListener(this), this);

        getLogger().info("ReportStaff v1.0.0 enabled successfully.");
    }

    @Override
    public void onDisable() {
        pendingCustomReasons.clear();
        getLogger().info("ReportStaff disabled.");
    }

    public void reloadPlugin() {
        reloadConfig();
        languageManager = new LanguageManager(this);
        reportManager = new ReportManager();
        cooldownManager = new CooldownManager();
        discordWebhookManager = new DiscordWebhookManager(this);
        getLogger().info("Plugin configuration and language files have been reloaded.");
    }

    public static ReportStaffPlugin getInstance() {
        return instance;
    }

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
