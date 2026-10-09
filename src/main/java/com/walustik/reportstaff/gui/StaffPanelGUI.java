package com.walustik.reportstaff.gui;

import com.walustik.reportstaff.ReportStaffPlugin;
import com.walustik.reportstaff.models.Report;
import com.walustik.reportstaff.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * GUI for displaying active reports to staff members.
 * Updates asynchronously when reports are added/removed.
 *
 * @author Walustik
 * @version 1.1.0
 */
public final class StaffPanelGUI {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    /**
     * Opens the staff reports panel for a player.
     */
    public static void open(Player player, ReportStaffPlugin plugin) {
        Inventory inventory = Bukkit.createInventory(null, 54, ColorUtils.colorize(plugin.getLanguageManager().getMessage("report-panel.title")));

        // Fetch reports from database asynchronously
        plugin.getReportManager().getReports().thenAccept(reports -> {
            for (int i = 0; i < reports.size() && i < 54; i++) {
                Report report = reports.get(i);
                inventory.setItem(i, createReportItem(report, plugin));
            }
            player.openInventory(inventory);
        }).exceptionally(e -> {
            player.sendMessage(ColorUtils.colorize(plugin.getLanguageManager().getMessage("prefix") + plugin.getLanguageManager().getMessage("database-error")));
            plugin.getLogger().warning("Failed to load reports: " + e.getMessage());
            return null;
        });
    }

    /**
     * Creates an inventory item representing a report.
     */
    private static ItemStack createReportItem(Report report, ReportStaffPlugin plugin) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = head.getItemMeta();
        if (meta == null) {
            return head;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(report.targetUUID());
        if (meta instanceof SkullMeta skullMeta) {
            skullMeta.setOwningPlayer(target);
            skullMeta.setDisplayName(ColorUtils.colorize("&c" + target.getName()));
        }

        List<String> lore = new ArrayList<>();
        lore.add(ColorUtils.colorize(plugin.getLanguageManager().getMessage("report-panel.reporter").replace("{reporter}", Bukkit.getOfflinePlayer(report.reporterUUID()).getName())));
        lore.add(ColorUtils.colorize(plugin.getLanguageManager().getMessage("report-panel.target").replace("{target}", target.getName())));
        lore.add(ColorUtils.colorize(plugin.getLanguageManager().getMessage("report-panel.reason").replace("{reason}", report.reason())));
        lore.add(ColorUtils.colorize(plugin.getLanguageManager().getMessage("report-panel.date").replace("{date}", DATE_FORMAT.format(new Date(report.timestamp())))));
        lore.add(ColorUtils.colorize("&7Age: &f" + report.getFormattedAge()));
        lore.add("");
        lore.add(ColorUtils.colorize("&7Left Click: Teleport"));
        lore.add(ColorUtils.colorize("&7Right Click: Spectate"));
        lore.add(ColorUtils.colorize("&7Shift + Left: Resolve"));

        meta.setLore(lore);
        meta.getPersistentDataContainer().set(plugin.getReportIdKey(), PersistentDataType.STRING, report.id().toString());
        head.setItemMeta(meta);
        return head;
    }
}
