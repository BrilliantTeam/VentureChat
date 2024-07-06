package mineverse.Aust1n46.chat.utilities;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.TimeUnit;

public class ScheduleUtil {
    private static boolean isFolia = false;
    static {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            isFolia = true;
        } catch (Exception ignored) {}
    }

    public static void runTask(Plugin plugin, Runnable task) {
        if (isFolia) {
            Bukkit.getGlobalRegionScheduler().run(plugin, (ignored) -> task.run());
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    public static void runTaskLater(Plugin plugin, Runnable task, long delay) {
        if (isFolia) {
            Bukkit.getGlobalRegionScheduler().runDelayed(plugin, (ignored) -> task.run(), delay);
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, task, delay);
        }
    }

    public static void runTaskTimer(Plugin plugin, Runnable task, long delay, long fixedRate) {
        if (isFolia) {
            Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, (ignored) -> task.run(), delay, fixedRate);
        } else {
            Bukkit.getScheduler().runTaskTimer(plugin, task, delay, fixedRate);
        }
    }

    public static void runTaskAsynchronously(Plugin plugin, Runnable task) {
        if (isFolia) {
            Bukkit.getAsyncScheduler().runNow(plugin, (ignored) -> task.run());
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
        }
    }

    public static void runTaskLaterAsynchronously(Plugin plugin, Runnable task, long delay) {
        if (isFolia) {
            Bukkit.getAsyncScheduler().runDelayed(plugin, (ignored) -> task.run(), delay / 20L, TimeUnit.SECONDS);
        } else {
            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, task, delay);
        }
    }

    public static void runTaskTimerAsynchronously(Plugin plugin, Runnable task, long delay, long fixedRate) {
        if (isFolia) {
            Bukkit.getAsyncScheduler().runAtFixedRate(plugin, (ignored) -> task.run(), delay / 20L, fixedRate / 20L, TimeUnit.SECONDS);
        } else {
            Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, delay, fixedRate);
        }
    }
}
