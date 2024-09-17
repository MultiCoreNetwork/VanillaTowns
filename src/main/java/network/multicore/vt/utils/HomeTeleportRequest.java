package network.multicore.vt.utils;

import network.multicore.vt.VanillaTowns;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Date;

public class HomeTeleportRequest {
    private final Plugin plugin;
    private final Player player;
    private final Location destination;
    private final int delay;
    private BukkitTask task;

    public HomeTeleportRequest(Plugin plugin, Player player, Location destination, int delay) {
        this.plugin = plugin;
        this.player = player;
        this.destination = destination;
        this.delay = delay;
    }

    public void teleport() {
        task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.teleport(destination);
            Text.send(Messages.get().get("home-teleporting"), player);
            VanillaTowns.TELEPORTS.remove(player);

            int teleportCooldown = plugin.getConfig().getInt("town-home-teleport-cooldown", 0);
            if (teleportCooldown > 0) {
                VanillaTowns.TELEPORT_COOLDOWN.put(player, new Date());
            }
        }, (long) delay * 20);
    }

    public void cancel() {
        if (task != null) {
            task.cancel();
        }
    }
}
