package network.multicore.vt.listeners;

import network.multicore.vt.utils.Cache;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class CacheListener implements Listener {
    private final Cache cache = Cache.get();

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        cache.loadTown(e.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        cache.unloadTown(e.getPlayer());
    }
}
