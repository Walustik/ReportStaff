package com.walustik.reportstaff.listeners;

import com.walustik.reportstaff.ReportStaffPlugin;
import com.walustik.reportstaff.utils.ColorUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;

/**
 * Handles player chat events for custom report reasons.
 * Intercepts chat messages from players with pending custom reports.
 *
 * @author Walustik
 * @version 1.1.0
 */
public final class PlayerChatListener implements Listener {

    private final ReportStaffPlugin plugin;

    public PlayerChatListener(ReportStaffPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChat(PlayerChatEvent event) {
        if (!plugin.getPendingCustomReasons().containsKey(event.getPlayer().getUniqueId())) {
            return;
        }

        event.setCancelled(true);
        String message = event.getMessage();
        if (message == null || message.isBlank()) {
            event.getPlayer().sendMessage(ColorUtils.colorize(plugin.getLanguageManager().getMessage("prefix") + plugin.getLanguageManager().getMessage("custom-reason")));
            return;
        }

        java.util.UUID targetId = plugin.getPendingCustomReasons().remove(event.getPlayer().getUniqueId());
        plugin.getReportManager().createReport(event.getPlayer().getUniqueId(), targetId, message.trim())
                .thenRun(() =>
                        event.getPlayer().sendMessage(ColorUtils.colorize(plugin.getLanguageManager().getMessage("prefix") + plugin.getLanguageManager().getMessage("report-created")))
                )
                .exceptionally(e -> {
                    event.getPlayer().sendMessage(ColorUtils.colorize(plugin.getLanguageManager().getMessage("prefix") + plugin.getLanguageManager().getMessage("database-error")));
                    return null;
                });
    }
}
