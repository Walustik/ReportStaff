package com.walustik.reportstaff.commands;

import com.walustik.reportstaff.ReportStaffPlugin;
import com.walustik.reportstaff.utils.ColorUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public final class RSReloadCommand implements CommandExecutor {

    private final ReportStaffPlugin plugin;

    public RSReloadCommand(ReportStaffPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("reportstaff.reload")) {
            sender.sendMessage(ColorUtils.colorize(plugin.getLanguageManager().getMessage("prefix") + "&cYou do not have permission."));
            return true;
        }

        plugin.reloadPlugin();
        sender.sendMessage(ColorUtils.colorize(plugin.getLanguageManager().getMessage("prefix") + plugin.getLanguageManager().getMessage("reload-success")));
        return true;
    }
}
