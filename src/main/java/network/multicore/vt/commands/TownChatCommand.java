package network.multicore.vt.commands;

import dev.dejvokep.boostedyaml.YamlDocument;
import network.multicore.vt.VanillaTowns;
import network.multicore.vt.data.Town;
import network.multicore.vt.utils.Cache;
import network.multicore.vt.utils.Messages;
import network.multicore.vt.utils.Text;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

public class TownChatCommand implements CommandExecutor {
    private final VanillaTowns plugin;
    private final Messages messages = Messages.get();
    private final Cache cache = Cache.get();
    private final YamlDocument config;

    public TownChatCommand(VanillaTowns plugin) {
        this.plugin = plugin;
        this.config = plugin.config();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            Text.send(messages.get("not-player"), sender);
            return true;
        }

        if (!plugin.hasPermission(player, "vanillatowns.chat")) {
            Text.send(messages.get("no-permissions"), player);
            return true;
        }

        if (args.length < 1) {
            help(player);
            return true;
        }

        Optional<Town> townOpt = cache.getTown(player);

        if (townOpt.isEmpty()) {
            Text.send(messages.get("not-in-town"), player);
            return true;
        }

        Town town = townOpt.get();

        String msg = messages.getAndReplace("chat-format",
                "role_color", config.getString("colors." + town.getMember(player).getRole().getName()),
                "role", config.getString("roles." + town.getMember(player).getRole().getName()),
                "player", player.displayName(),
                "message", String.join(" ", args)
        );

        town.getMembers().
                stream()
                .map(m -> Bukkit.getPlayer(m.getUniqueId()))
                .filter(Objects::nonNull)
                .forEach(p -> Text.send(msg, p));

        String socialspy = messages.getAndReplace("socialspy-format",
                "town", town.getName(),
                "role_color", config.getString("colors." + town.getMember(player).getRole().getName()),
                "role", config.getString("roles." + town.getMember(player).getRole().getName()),
                "player", player.displayName(),
                "message", String.join(" ", args)
        );

        Bukkit.getOnlinePlayers()
                .stream()
                .filter(p -> plugin.hasStaffPermission(p, "vanillatowns.socialspy"))
                .forEach(p -> Text.send(socialspy, p));

        return true;
    }

    private void help(Player player) {
        Text.send(messages.get("help"), player);
    }
}
