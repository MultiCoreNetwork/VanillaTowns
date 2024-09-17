package network.multicore.vt.commands;

import dev.dejvokep.boostedyaml.YamlDocument;
import network.multicore.vt.VanillaTowns;
import network.multicore.vt.data.Town;
import network.multicore.vt.data.TownMember;
import network.multicore.vt.data.TownRepository;
import network.multicore.vt.data.TownRole;
import network.multicore.vt.utils.Cache;
import network.multicore.vt.utils.Messages;
import network.multicore.vt.utils.TabCompleterUtil;
import network.multicore.vt.utils.Text;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

public class VanillaTownsCommand implements CommandExecutor, TabCompleter {
    private final VanillaTowns plugin;
    private final Messages messages = Messages.get();
    private final Cache cache = Cache.get();
    private final YamlDocument config;
    private final Pattern townNamePattern;
    private final TownRepository townRepository;

    public VanillaTownsCommand(VanillaTowns plugin) {
        this.plugin = plugin;
        this.config = plugin.config();
        this.townRepository = plugin.townRepository();
        try {
            this.townNamePattern = Pattern.compile(config.getString("town-name-pattern"));
        } catch (Throwable t) {
            Text.severe("<red>Invalid town name pattern in config.yml");
            plugin.onDisable();
            throw new IllegalStateException("Invalid town name pattern in config.yml");
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("vanillatowns.staff")) {
            Text.send(messages.get("no-permission"), sender);
            return true;
        }

        if (args.length < 1) {
            help(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> reload(sender);
            case "invite" -> {
                if (args.length < 3) help(sender);
                else invite(sender, args[1], args[2]);
            }
            case "join" -> {
                if (args.length < 3) help(sender);
                else join(sender, args[1], args[2]);
            }
            case "kick" -> {
                if (args.length < 3) help(sender);
                else kick(sender, args[1], args[2]);
            }
            case "rename" -> {
                if (args.length < 3) help(sender);
                else rename(sender, args[1], args[2]);
            }
            case "delete" -> {
                if (args.length < 2) help(sender);
                else delete(sender, args[1]);
            }
            case "setmayor" -> {
                if (args.length < 3) help(sender);
                else setMayor(sender, args[1], args[2]);
            }
            case "setofficer" -> {
                if (args.length < 3) help(sender);
                else setOfficer(sender, args[1], args[2]);
            }
            case "setcitizen" -> {
                if (args.length < 3) help(sender);
                else setCitizen(sender, args[1], args[2]);
            }
            case "sethome" -> {
                if (args.length < 2) help(sender);
                else setHome(sender, args[1]);
            }
            case "delhome" -> {
                if (args.length < 2) help(sender);
                else delHome(sender, args[1]);
            }
            case "home" -> {
                if (args.length < 2) help(sender);
                else home(sender, args[1]);
            }
            default -> help(sender);
        }

        return true;
    }

    private void help(CommandSender sender) {
        Text.send(messages.getList("staff-help"), sender);
    }

    private void reload(CommandSender sender) {
        if (!sender.hasPermission("vanillatowns.reload")) {
            Text.send(messages.get("no-permission"), sender);
            return;
        }

        long millis = System.currentTimeMillis();
        plugin.onDisable();
        plugin.onEnable();
        Text.send(messages.get("plugin-reloaded").replace("{time}", String.valueOf(System.currentTimeMillis() - millis)), sender);
    }

    private void invite(CommandSender sender, String townName, String playerName) {
        if (!plugin.hasStaffPermission(sender, "vanillatowns.staff.invite")) {
            Text.send(messages.get("no-permission"), sender);
            return;
        }

        Optional<Town> townOpt = townRepository.findByName(townName);
        if (townOpt.isEmpty()) {
            Text.send(messages.get("town-not-found").replace("{town}", townName), sender);
            return;
        }

        Town town = townOpt.get();

        Player target = plugin.getServer().getPlayer(playerName);
        if (target == null) {
            Text.send(messages.get("player-not-found"), sender);
            return;
        }

        if (!plugin.hasPermission(target, "vanillatowns.join")) {
            Text.send(messages.get("no-join-permission"), sender);
            return;
        }

        if (town.getMember(target) != null) {
            Text.send(messages.get("player-already-in-town"), sender);
            return;
        }

        VanillaTowns.INVITES.put(target.getUniqueId(), town.getId());

        Text.send(messages.getAndReplace("invite-sent", "player", target.displayName()), sender);
        Text.send(messages.getAndReplace("invite-received", "town", town.getName(), "player", sender instanceof Player player ? player.displayName() : messages.get("console")), target);
    }

    private void join(CommandSender sender, String townName, String playerName) {
        if (!plugin.hasStaffPermission(sender, "vanillatowns.staff.join")) {
            Text.send(messages.get("no-permission"), sender);
            return;
        }

        Optional<Town> townOpt = townRepository.findByName(townName);
        if (townOpt.isEmpty()) {
            Text.send(messages.get("town-not-found").replace("{town}", townName), sender);
            return;
        }

        Town town = townOpt.get();

        Player target = plugin.getServer().getPlayer(playerName);
        if (target == null) {
            Text.send(messages.get("player-not-found"), sender);
            return;
        }

        if (!plugin.hasPermission(target, "vanillatowns.join")) {
            Text.send(messages.get("no-join-permission"), sender);
            return;
        }

        if (town.getMember(target) != null) {
            Text.send(messages.get("player-already-in-town"), sender);
            return;
        }

        town.addMember(target);
        town = townRepository.save(town);
        cache.updateTown(town);
        VanillaTowns.INVITES.remove(target.getUniqueId());

        if (config.getBoolean("broadcast.player-joined-town", false)) {
            Text.broadcast(messages.getAndReplace("player-joined-town-broadcast",
                    "player", target.displayName(),
                    "town", town.getName()));
        } else {
            Text.send(messages.getAndReplace("player-joined-town-staff", "player", target.displayName(), "town", town.getName()), sender);
            town.getMembers()
                    .stream()
                    .map(m -> Bukkit.getPlayer(m.getUniqueId()))
                    .filter(Objects::nonNull)
                    .forEach(p -> Text.send(messages.getAndReplace("player-joined-town-members", "player", target.displayName()), p));
        }

        Text.info("Player <aqua>" + target.displayName() + "<reset> has been added to town <aqua>" + town.getName() + "<reset> by <aqua>" + sender.getName());
    }

    private void kick(CommandSender sender, String townName, String playerName) {
        if (!plugin.hasStaffPermission(sender, "vanillatowns.staff.kick")) {
            Text.send(messages.get("no-permission"), sender);
            return;
        }

        Optional<Town> townOpt = townRepository.findByName(townName);
        if (townOpt.isEmpty()) {
            Text.send(messages.get("town-not-found").replace("{town}", townName), sender);
            return;
        }

        Town town = townOpt.get();

        TownMember member = town.getMember(playerName);
        if (member == null) {
            Text.send(messages.get("player-not-in-town"), sender);
            return;
        }

        if (town.getMayor().getUniqueId().equals(member.getUniqueId())) {
            Text.send(messages.get("cannot-kick-mayor"), sender);
            return;
        }

        town.removeMember(member.getUniqueId());
        town = townRepository.save(town);
        cache.updateTown(town);

        if (config.getBoolean("broadcast.player-left", false)) {
            Text.broadcast(messages.getAndReplace("player-kicked-from-town-broadcast",
                    "player", sender instanceof Player player ? player.displayName() : messages.get("console"),
                    "town", town.getName(),
                    "target", member.getName()));
        } else {
            Text.send(messages.getAndReplace("player-kicked-from-town-staff", "town", town.getName(), "player", member.getName()), sender);
            town.getMembers()
                    .stream()
                    .map(m -> Bukkit.getPlayer(m.getUniqueId()))
                    .filter(Objects::nonNull)
                    .forEach(p -> Text.send(messages.getAndReplace("player-kicked-from-town-members", "player", sender instanceof Player player ? player.displayName() : messages.get("console"), "target", member.getName()), p));

            Player target = Bukkit.getPlayer(member.getUniqueId());
            if (target != null) {
                Text.send(messages.getAndReplace("kicked-from-town-target", "town", town.getName(), "player", sender instanceof Player player ? player.displayName() : messages.get("console")), target);
            }
        }

        Text.info("Player <aqua>" + sender.getName() + "<reset> kicked <aqua>" + member.getName() + "<reset> from town <aqua>" + town.getName() + "<reset>");
    }

    private void rename(CommandSender sender, String oldName, String newName) {
        if (!plugin.hasStaffPermission(sender, "vanillatowns.staff.rename")) {
            Text.send(messages.get("no-permission"), sender);
            return;
        }

        Optional<Town> townOpt = townRepository.findByName(oldName);
        if (townOpt.isEmpty()) {
            Text.send(messages.get("town-not-found").replace("{town}", oldName), sender);
            return;
        }

        Town town = townOpt.get();

        if (!townNamePattern.matcher(newName).matches()) {
            Text.send(messages.get("invalid-town-name"), sender);
            return;
        }

        newName = Text.stripFormatting(newName);
        if (townRepository.findByName(newName).isPresent()) {
            Text.send(messages.get("town-already-exists").replace("{town}", newName), sender);
            return;
        }

        town.setName(newName);
        town = townRepository.save(town);
        cache.updateTown(town);

        String finalName = newName;
        if (config.getBoolean("broadcast.town-renamed", false)) {
            Bukkit.getOnlinePlayers().forEach(p -> Text.send(messages.getAndReplace("town-renamed-broadcast",
                    "player", sender instanceof Player player ? player.displayName() : messages.get("console"),
                    "old_name", oldName,
                    "new_name", finalName), p));
        } else {
            Text.send(messages.getAndReplace("town-renamed-staff", "old_name", oldName, "new_name", newName), sender);
            town.getMembers()
                    .stream()
                    .map(m -> Bukkit.getPlayer(m.getUniqueId()))
                    .filter(Objects::nonNull)
                    .forEach(p -> Text.send(messages.getAndReplace("town-renamed-members", "old", oldName, "new", finalName), p));
        }

        Text.info("Town <aqua>" + oldName + "<reset> has been renamed to <aqua>" + newName + "<reset> by <aqua>" + sender.getName());
    }

    private void delete(CommandSender sender, String townName) {
        if (!plugin.hasStaffPermission(sender, "vanillatowns.staff.delete")) {
            Text.send(messages.get("no-permission"), sender);
            return;
        }

        Optional<Town> townOpt = townRepository.findByName(townName);
        if (townOpt.isEmpty()) {
            Text.send(messages.get("town-not-found").replace("{town}", townName), sender);
            return;
        }

        Town town = townOpt.get();

        townRepository.delete(town);
        cache.removeTown(town);

        Text.send(messages.getAndReplace("town-deleted", "town", townName), sender);

        if (config.getBoolean("broadcast.town-deleted-staff", false)) {
            Bukkit.getOnlinePlayers()
                    .stream()
                    .filter(Objects::nonNull)
                    .forEach(p -> Text.send(messages.getAndReplace("town-deleted-broadcast",
                            "player", sender instanceof Player player ? player.displayName() : messages.get("console"),
                            "town", townName), p));
        }

        Text.info("Town <aqua>" + townName + "<reset> has been deleted by <aqua>" + sender.getName() + "<reset>. The town had <aqua>" + town.getBalance() + "$<reset> in its bank account.");
    }

    private void setMayor(CommandSender sender, String townName, String playerName) {
        if (!plugin.hasStaffPermission(sender, "vanillatowns.staff.roles")) {
            Text.send(messages.get("no-permission"), sender);
            return;
        }

        Optional<Town> townOpt = townRepository.findByName(townName);
        if (townOpt.isEmpty()) {
            Text.send(messages.get("town-not-found").replace("{town}", townName), sender);
            return;
        }

        Town town = townOpt.get();

        TownMember member = town.getMember(playerName);
        if (member == null) {
            Text.send(messages.get("player-not-in-town"), sender);
            return;
        }

        town.setMayor(member);
        town = townRepository.save(town);
        cache.updateTown(town);

        Text.send(messages.getAndReplace("mayor-set-staff", "player", member.getName(), "town", town.getName()), sender);
        town.getMembers()
                .stream()
                .map(m -> Bukkit.getPlayer(m.getUniqueId()))
                .filter(Objects::nonNull)
                .forEach(p -> Text.send(messages.getAndReplace("new-mayor", "player", member.getName()), p));

        Text.info("Player <aqua>" + member.getName() + "<reset> has been set as mayor of town <aqua>" + town.getName() + "<reset> by <aqua>" + sender.getName());
    }

    private void setOfficer(CommandSender sender, String townName, String playerName) {
        if (!plugin.hasStaffPermission(sender, "vanillatowns.staff.roles")) {
            Text.send(messages.get("no-permission"), sender);
            return;
        }

        Optional<Town> townOpt = townRepository.findByName(townName);
        if (townOpt.isEmpty()) {
            Text.send(messages.get("town-not-found").replace("{town}", townName), sender);
            return;
        }

        Town town = townOpt.get();

        TownMember member = town.getMember(playerName);
        if (member == null) {
            Text.send(messages.get("player-not-in-town"), sender);
            return;
        }

        if (member.getRole().equals(TownRole.MAYOR)) {
            Text.send(messages.get("cannot-demote-mayor"), sender);
            return;
        }

        if (member.getRole().equals(TownRole.OFFICER)) {
            Text.send(messages.get("already-officer"), sender);
            return;
        }

        member.setRole(TownRole.OFFICER);
        town = townRepository.save(town);
        cache.updateTown(town);

        Text.send(messages.getAndReplace("officer-set-staff", "player", member.getName(), "town", town.getName()), sender);

        Player target = Bukkit.getPlayer(member.getUniqueId());
        if (target != null) {
            Text.send(messages.get("officer-set-target"), target);
        }

        Text.info("Player <aqua>" + member.getName() + "<reset> has been set as officer of town <aqua>" + town.getName() + "<reset> by <aqua>" + sender.getName());
    }

    private void setCitizen(CommandSender sender, String townName, String playerName) {
        if (!plugin.hasStaffPermission(sender, "vanillatowns.staff.roles")) {
            Text.send(messages.get("no-permission"), sender);
            return;
        }

        Optional<Town> townOpt = townRepository.findByName(townName);
        if (townOpt.isEmpty()) {
            Text.send(messages.get("town-not-found").replace("{town}", townName), sender);
            return;
        }

        Town town = townOpt.get();

        TownMember member = town.getMember(playerName);
        if (member == null) {
            Text.send(messages.get("player-not-in-town"), sender);
            return;
        }

        if (member.getRole().equals(TownRole.MAYOR)) {
            Text.send(messages.get("cannot-demote-mayor"), sender);
            return;
        }

        if (member.getRole().equals(TownRole.CITIZEN)) {
            Text.send(messages.get("not-officer"), sender);
            return;
        }

        member.setRole(TownRole.CITIZEN);
        town = townRepository.save(town);
        cache.updateTown(town);

        Text.send(messages.getAndReplace("citizen-set-staff", "player", member.getName(), "town", town.getName()), sender);

        Player target = Bukkit.getPlayer(member.getUniqueId());
        if (target != null) {
            Text.send(messages.get("officer-removed-target"), target);
        }

        Text.info("Player <aqua>" + member.getName() + "<reset> has been set as citizen of town <aqua>" + town.getName() + "<reset> by <aqua>" + sender.getName());
    }

    private void setHome(CommandSender sender, String townName) {
        if (!plugin.hasStaffPermission(sender, "vanillatowns.staff.home.edit")) {
            Text.send(messages.get("no-permission"), sender);
            return;
        }

        if (!(sender instanceof Player player)) {
            Text.send(messages.get("not-player"), sender);
            return;
        }

        Optional<Town> townOpt = townRepository.findByName(townName);
        if (townOpt.isEmpty()) {
            Text.send(messages.get("town-not-found").replace("{town}", townName), sender);
            return;
        }

        Town town = townOpt.get();

        town.setHome(player.getLocation());
        town = townRepository.save(town);
        cache.updateTown(town);

        Text.send(messages.get("home-set"), player);

        Text.info("Home of town <aqua>" + town.getName() + "<reset> has been set by <aqua>" + player.getName());
    }

    private void delHome(CommandSender sender, String townName) {
        if (!plugin.hasStaffPermission(sender, "vanillatowns.staff.home.edit")) {
            Text.send(messages.get("no-permission"), sender);
            return;
        }

        Optional<Town> townOpt = townRepository.findByName(townName);
        if (townOpt.isEmpty()) {
            Text.send(messages.get("town-not-found").replace("{town}", townName), sender);
            return;
        }

        Town town = townOpt.get();

        town.setHome(null);
        town = townRepository.save(town);
        cache.updateTown(town);

        Text.send(messages.get("home-deleted"), sender);

        Text.info("Home of town <aqua>" + town.getName() + "<reset> has been deleted by <aqua>" + sender.getName());
    }

    private void home(CommandSender sender, String townName) {
        if (!plugin.hasStaffPermission(sender, "vanillatowns.staff.home")) {
            Text.send(messages.get("no-permission"), sender);
            return;
        }

        if (!(sender instanceof Player player)) {
            Text.send(messages.get("not-player"), sender);
            return;
        }

        Optional<Town> townOpt = townRepository.findByName(townName);
        if (townOpt.isEmpty()) {
            Text.send(messages.get("town-not-found").replace("{town}", townName), sender);
            return;
        }

        Town town = townOpt.get();

        if (town.getHome() == null || town.getHome().getLocation().isEmpty()) {
            Text.send(messages.getAndReplace("home-not-set-staff", "town", town.getName()), player);
            return;
        }

        player.teleport(town.getHome().getLocation().get());

        Text.send(messages.get("home-teleporting"), player);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!plugin.hasStaffPermission(sender, "vanillatowns.staff")) return List.of();

        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            if (plugin.hasStaffPermission(sender, "vanillatowns.staff.reload")) completions.add("reload");
            if (plugin.hasStaffPermission(sender, "vanillatowns.staff.invite")) completions.add("invite");
            if (plugin.hasStaffPermission(sender, "vanillatowns.staff.join")) completions.add("join");
            if (plugin.hasStaffPermission(sender, "vanillatowns.staff.kick")) completions.add("kick");
            if (plugin.hasStaffPermission(sender, "vanillatowns.staff.rename")) completions.add("rename");
            if (plugin.hasStaffPermission(sender, "vanillatowns.staff.delete")) completions.add("delete");
            if (plugin.hasStaffPermission(sender, "vanillatowns.staff.roles")) {
                completions.add("setMayor");
                completions.add("setOfficer");
                completions.add("setCitizen");
            }
            if (plugin.hasStaffPermission(sender, "vanillatowns.staff.home.edit")) {
                completions.add("setHome");
                completions.add("delHome");
            }
            if (plugin.hasStaffPermission(sender, "vanillatowns.staff.home")) completions.add("home");
        } else if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "invite", "join", "kick", "rename", "delete", "setmayor", "setofficer", "setcitizen", "sethome", "delhome", "home" -> completions.addAll(townRepository.findAll()
                        .stream()
                        .map(Town::getName)
                        .toList());

            }
        } else if (args.length == 3) {
            switch (args[0].toLowerCase()) {
                case "invite", "join" -> {
                    Optional<Town> townOpt = townRepository.findByName(args[1]);
                    townOpt.ifPresent(town -> completions.addAll(Bukkit.getOnlinePlayers()
                            .stream()
                            .map(Player::getName)
                            .filter(p -> town.getMember(p) == null)
                            .toList()));
                }
                case "kick", "setmayor" -> {
                    Optional<Town> townOpt = townRepository.findByName(args[1]);
                    townOpt.ifPresent(town -> completions.addAll(town.getMembers()
                            .stream()
                            .filter(m -> !m.getRole().equals(TownRole.MAYOR))
                            .map(TownMember::getName)
                            .toList()));
                }
                case "setofficer" -> {
                    Optional<Town> townOpt = townRepository.findByName(args[1]);
                    townOpt.ifPresent(town -> completions.addAll(town.getMembers()
                            .stream()
                            .filter(m -> !m.getRole().equals(TownRole.OFFICER) && !m.getRole().equals(TownRole.MAYOR))
                            .map(TownMember::getName)
                            .toList()));
                }
                case "setcitizen" -> {
                    Optional<Town> townOpt = townRepository.findByName(args[1]);
                    townOpt.ifPresent(town -> completions.addAll(town.getMembers()
                            .stream()
                            .filter(m -> !m.getRole().equals(TownRole.CITIZEN) && !m.getRole().equals(TownRole.MAYOR))
                            .map(TownMember::getName)
                            .toList()));
                }
            }
        }

        return TabCompleterUtil.getCompletions(args[args.length - 1], completions);
    }
}
