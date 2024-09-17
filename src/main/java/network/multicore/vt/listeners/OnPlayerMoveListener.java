package network.multicore.vt.listeners;

import network.multicore.vt.VanillaTowns;
import network.multicore.vt.utils.Messages;
import network.multicore.vt.utils.Text;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class OnPlayerMoveListener implements Listener {

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();

        if (VanillaTowns.TELEPORTS.containsKey(player)) {
            VanillaTowns.TELEPORTS.get(player).cancel();
            VanillaTowns.TELEPORTS.remove(player);
            Text.send(Messages.get().get("teleport-cancelled"), player);
        }
    }
}
