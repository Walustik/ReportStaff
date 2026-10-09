package com.walustik.reportstaff.listeners;

import com.walustik.reportstaff.ReportStaffPlugin;
import com.walustik.reportstaff.gui.StaffPanelGUI;
import com.walustik.reportstaff.models.Report;
import com.walustik.reportstaff.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

/**
 * Handles inventory click events for reports and category selection.
 * Supports database-backed report operations with async callbacks.
 *
 * @author Walustik
 * @version 1.1.0
 */
public final class InventoryClickListener implements Listener {

    private final ReportStaffPlugin plugin;

    public InventoryClickListener(ReportStaffPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        ItemStack current = event.getCurrentItem();
        if (current == null || current.getType() == Material.AIR) {
            return;
        }

        ItemMeta meta = current.getItemMeta();
        if (meta == null) {
            return;
        }

        String action = meta.getPersistentDataContainer().get(plugin.getActionKey(), PersistentDataType.STRING);
        if (action != null) {
            event.setCancelled(true);
            handleCategoryAction(player, action);
            return;
        }

        String reportId = meta.getPersistentDataContainer().get(plugin.getReportIdKey(), PersistentDataType.STRING);
        if (reportId != null) {
            event.setCancelled(true);
            handleStaffAction(player, reportId, event.isLeftClick(), event.isRightClick(), event.isShiftClick());
        }
    }

    /**
     * Handles category selection for new reports.
     */
    private void handleCategoryAction(Player player, String action) {
        String targetName = player.getOpenInventory().getTitle().replace("Report ", "").trim();
        Player target = Bukkit.getPlayerExact(targetName);
        if (target == null) {
            player.sendMessage(ColorUtils.colorize(plugin.getLanguageManager().getMessage("prefix") + plugin.getLanguageManager().getMessage("no-player")));
            return;
        }

        if (action.equalsIgnoreCase("custom")) {
            plugin.getPendingCustomReasons().put(player.getUniqueId(), target.getUniqueId());
            player.closeInventory();
            player.sendMessage(ColorUtils.colorize(plugin.getLanguageManager().getMessage("prefix") + plugin.getLanguageManager().getMessage("custom-reason")));
            return;
        }

        String reason = switch (action.toLowerCase()) {
            case "combat" -> "Combat";
            case "movement" -> "Movement";
            case "chat" -> "Chat";
            case "bug" -> "Bug";
            default -> "Custom";
        };

        // Create report asynchronously
        plugin.getReportManager().createReport(player.getUniqueId(), target.getUniqueId(), reason)
                .thenRun(() -> {
                    player.closeInventory();
                    player.sendMessage(ColorUtils.colorize(plugin.getLanguageManager().getMessage("prefix") + plugin.getLanguageManager().getMessage("report-created")));
                })
                .exceptionally(e -> {
                    player.sendMessage(ColorUtils.colorize(plugin.getLanguageManager().getMessage("prefix") + plugin.getLanguageManager().getMessage("database-error")));
                    plugin.getLogger().warning("Failed to create report: " + e.getMessage());
                    return null;
                });
    }

    /**
     * Handles staff actions on reports (teleport, spectate, resolve).
     */
    private void handleStaffAction(Player player, String reportId, boolean leftClick, boolean rightClick, boolean shiftClick) {
        try {
            UUID id = UUID.fromString(reportId);
            plugin.getReportManager().getReportById(id).thenAccept(report -> {
                if (report == null) {
                    player.sendMessage(ColorUtils.colorize(plugin.getLanguageManager().getMessage("prefix") + plugin.getLanguageManager().getMessage("invalid-uuid")));
                    return;
                }

                if (leftClick && !shiftClick && !rightClick) {
                    handleTeleportAction(player, report);
                    return;
                }

                if (rightClick && !shiftClick) {
                    handleSpectatorAction(player, report);
                    return;
                }

                if (shiftClick && leftClick && !rightClick) {
                    handleResolveAction(player, report);
                }
            }).exceptionally(e -> {
                player.sendMessage(ColorUtils.colorize(plugin.getLanguageManager().getMessage("prefix") + plugin.getLanguageManager().getMessage("database-error")));
                return null;
            });
        } catch (IllegalArgumentException ignored) {
            player.sendMessage(ColorUtils.colorize(plugin.getLanguageManager().getMessage("prefix") + plugin.getLanguageManager().getMessage("invalid-uuid")));
        }
    }

    /**
     * Handles teleporting to reported player.
     */
    private void handleTeleportAction(Player player, Report report) {
        Player target = Bukkit.getPlayer(report.targetUUID());
        if (target != null) {
            player.sendMessage(ColorUtils.colorize(plugin.getLanguageManager().getMessage("prefix") + plugin.getLanguageManager().getMessage("tp")));
            player.teleportAsync(target.getLocation());
        }
    }

    /**
     * Handles spectating reported player.
     */
    private void handleSpectatorAction(Player player, Report report) {
        Player target = Bukkit.getPlayer(report.targetUUID());
        if (target != null) {
            player.setGameMode(GameMode.SPECTATOR);
            player.setSpectatorTarget(target);
            player.sendMessage(ColorUtils.colorize(plugin.getLanguageManager().getMessage("prefix") + plugin.getLanguageManager().getMessage("spectator")));
        }
    }

    /**
     * Handles resolving a report.
     */
    private void handleResolveAction(Player player, Report report) {
        plugin.getReportManager().removeReport(report.id())
                .thenRun(() -> {
                    player.sendMessage(ColorUtils.colorize(plugin.getLanguageManager().getMessage("prefix") + plugin.getLanguageManager().getMessage("resolved")));
                    StaffPanelGUI.open(player, plugin);
                })
                .exceptionally(e -> {
                    player.sendMessage(ColorUtils.colorize(plugin.getLanguageManager().getMessage("prefix") + plugin.getLanguageManager().getMessage("database-error")));
                    return null;
                });
    }
}
