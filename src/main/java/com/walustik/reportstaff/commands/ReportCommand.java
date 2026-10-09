package com.walustik.reportstaff.commands;

import com.walustik.reportstaff.ReportStaffPlugin;
import com.walustik.reportstaff.gui.CategoryGUI;
import com.walustik.reportstaff.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public final class ReportCommand implements CommandExecutor {

    private final ReportStaffPlugin plugin;

    public ReportCommand(ReportStaffPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ColorUtils.colorize(plugin.getLanguageManager().getMessage("prefix") + "This command can only be used by players."));
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(ColorUtils.colorize(plugin.getLanguageManager().getMessage("prefix") + "/report <player>"));
            return true;
        }

        if (plugin.getCooldownManager().isCoolingDown(player.getUniqueId())) {
            player.sendMessage(ColorUtils.colorize(plugin.getLanguageManager().getMessage("prefix") + plugin.getLanguageManager().getMessage("cooldown")));
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            player.sendMessage(ColorUtils.colorize(plugin.getLanguageManager().getMessage("prefix") + plugin.getLanguageManager().getMessage("no-player")));
            return true;
        }

        if (player.getUniqueId().equals(target.getUniqueId())) {
            player.sendMessage(ColorUtils.colorize(plugin.getLanguageManager().getMessage("prefix") + plugin.getLanguageManager().getMessage("self-report")));
            return true;
        }

        CategoryGUI.open(player, target);
        return true;
    }
}
