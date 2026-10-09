package com.walustik.reportstaff.utils;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

public final class SchedulerUtils {

    private SchedulerUtils() {
    }

    public static boolean isFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    public static void runTask(Plugin plugin, Runnable task) {
        if (isFolia()) {
            Bukkit.getGlobalRegionScheduler().run(plugin, scheduled -> task.run());
            return;
        }
        Bukkit.getScheduler().runTask(plugin, task);
    }

    public static void runTaskLater(Plugin plugin, Runnable task, long delayTicks) {
        if (isFolia()) {
            Bukkit.getGlobalRegionScheduler().runDelayed(plugin, scheduled -> task.run(), delayTicks);
            return;
        }
        Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
    }

    public static void runAsync(Plugin plugin, Runnable task) {
        if (isFolia()) {
            Bukkit.getGlobalRegionScheduler().run(plugin, scheduled -> Bukkit.getAsyncScheduler().runNow(plugin, ignored -> task.run()));
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
    }

    public static void runTaskForRegion(Plugin plugin, World world, int chunkX, int chunkZ, Runnable task) {
        if (isFolia()) {
            Bukkit.getRegionScheduler().run(plugin, world, chunkX, chunkZ, scheduled -> task.run());
            return;
        }
        Bukkit.getScheduler().runTask(plugin, task);
    }
}
