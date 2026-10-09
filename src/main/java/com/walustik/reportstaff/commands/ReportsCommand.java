package com.walustik.reportstaff.commands;

import com.walustik.reportstaff.ReportStaffPlugin;
import com.walustik.reportstaff.gui.StaffPanelGUI;
import com.walustik.reportstaff.utils.ColorUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class ReportsCommand implements CommandExecutor {

    private final ReportStaffPlugin plugin;

    public ReportsCommand(ReportStaffPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ColorUtils.colorize(plugin.getLanguageManager().getMessage("prefix") + "This command can only be used by players."));
            return true;
        }

        if (!player.hasPermission("reportstaff.reports")) {
            player.sendMessage(ColorUtils.colorize(plugin.getLanguageManager().getMessage("prefix") + "&cYou do not have permission."));
            return true;
        }

        StaffPanelGUI.open(player, plugin);
        return true;
    }
}
