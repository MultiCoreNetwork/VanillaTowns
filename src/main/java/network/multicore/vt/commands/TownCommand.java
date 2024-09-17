package network.multicore.vt.commands;

import dev.dejvokep.boostedyaml.YamlDocument;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import network.multicore.vt.VanillaTowns;
import network.multicore.vt.data.Town;
import network.multicore.vt.data.TownMember;
import network.multicore.vt.data.TownRepository;
import network.multicore.vt.data.TownRole;
import network.multicore.vt.utils.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Pattern;

@SuppressWarnings("UnstableApiUsage")
public class TownCommand implements BasicCommand {
    private final VanillaTowns plugin;
    private final Messages messages = Messages.get();
    private final Cache cache = Cache.get();
    private final YamlDocument config;
    private final Pattern townNamePattern;
    private final TownRepository townRepository;

    public TownCommand(VanillaTowns plugin) {
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
    public void execute(@NotNull CommandSourceStack src, @NotNull String[] args) {
        CommandSender sender = src.getSender();

        if (!(sender instanceof Player player)) {
            Text.send(messages.get("not-player"), sender);
            return;
        }

        if (!plugin.hasPermission(player, "vanillatowns.town")) {
            Text.send(messages.get("no-permission"), player);
            return;
        }

        if (args.length < 1) {
            info(player, null);
            return;
        }

        switch (args[0].toLowerCase()) {
            case "help" -> help(player);
            case "create" -> {
                if (args.length < 2) help(player);
                else create(player, args[1]);
            }
            case "invite" -> {
                if (args.length < 2) help(player);
                else invite(player, args[1]);
            }
            case "join" -> join(player);
            case "leave" -> leave(player);
            case "kick" -> {
                if (args.length < 2) help(player);
                else kick(player, args[1]);
            }
            case "rename" -> {
                if (args.length < 2) help(player);
                else rename(player, args[1]);
            }
            case "give" -> {
                if (args.length < 2) help(player);
                else give(player, args[1]);
            }
            case "delete" -> delete(player);
            case "balance" -> {
                if (args.length < 2) balance(player, null);
                else balance(player, args[1]);
            }
            case "deposit" -> {
                if (args.length < 2) help(player);
                else deposit(player, args[1]);
            }
            case "withdraw" -> {
                if (args.length < 2) help(player);
                else withdraw(player, args[1]);
            }
            case "baltop" -> baltop(player);
            case "sethome" -> setHome(player);
            case "home" -> home(player);
            case "delhome" -> delHome(player);
            case "user" -> user(player, args);
            default -> info(player, args[0]);
        }
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack src, @NotNull String[] args) {
        CommandSender sender = src.getSender();

        if (!(sender instanceof Player player)) return List.of();
        if (!plugin.hasPermission(player, "vanillatowns.player")) return List.of();

        List<String> completions = new ArrayList<>();

        if (args.length == 0 || args.length == 1) {
            if (plugin.hasPermission(player, "vanillatowns.create")) {
                completions.add("create");
                completions.add("delete");
            }
            if (plugin.hasPermission(player, "vanillatowns.baltop")) completions.add("baltop");
            if (plugin.hasPermission(player, "vanillatowns.invite")) completions.add("invite");
            if (plugin.hasPermission(player, "vanillatowns.join")) {
                completions.add("join");
                completions.add("leave");
            }
            if (plugin.hasPermission(player, "vanillatowns.kick")) completions.add("kick");
            if (plugin.hasPermission(player, "vanillatowns.give")) completions.add("give");
            if (plugin.hasPermission(player, "vanillatowns.home")) completions.add("home");
            if (plugin.hasPermission(player, "vanillatowns.home.edit")) {
                completions.add("sethome");
                completions.add("delhome");
            }
            if (plugin.hasPermission(player, "vanillatowns.balance")) completions.add("balance");
            if (plugin.hasPermission(player, "vanillatowns.deposit")) completions.add("deposit");
            if (plugin.hasPermission(player, "vanillatowns.withdraw")) completions.add("withdraw");
            if (plugin.hasPermission(player, "vanillatowns.rename")) completions.add("rename");

            completions.add("user");
        } else if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "invite" -> {
                    Optional<Town> townOpt = cache.getTown(player);
                    if (townOpt.isPresent() && townOpt.get().canInvite(player)) {
                        Bukkit.getOnlinePlayers().stream()
                                .filter(p -> townOpt.get().getMember(p) == null && !TabCompleterUtil.isVanished(p))
                                .map(Player::getName)
                                .forEach(completions::add);
                    }
                }
                case "kick", "give" -> {
                    Optional<Town> townOpt = cache.getTown(player);
                    if (townOpt.isPresent()) {
                        Town town = townOpt.get();
                        town.getMembers().stream().map(TownMember::getName).forEach(completions::add);
                    }
                }
                case "user" -> completions.addAll(List.of("setOfficer", "removeOfficer", "deposit", "withdraw"));
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("user")) {
                switch (args[1].toLowerCase()) {
                    case "setofficer", "deposit", "withdraw" -> {
                        Optional<Town> townOpt = cache.getTown(player);
                        if (townOpt.isPresent()) {
                            Town town = townOpt.get();
                            town.getCitizens().stream().map(TownMember::getName).forEach(completions::add);
                        }
                    }
                    case "removeofficer" -> {
                        Optional<Town> townOpt = cache.getTown(player);
                        if (townOpt.isPresent()) {
                            Town town = townOpt.get();
                            town.getOfficers().stream().map(TownMember::getName).forEach(completions::add);
                        }
                    }
                }
            }
        } else if (args.length == 4) {
            if (args[0].equalsIgnoreCase("user") && (args[1].equalsIgnoreCase("deposit") || args[1].equalsIgnoreCase("withdraw"))) {
                completions.addAll(List.of("allow", "deny"));
            }
        }

        if (args.length > 0) return TabCompleterUtil.getCompletions(args[args.length - 1], completions);
        else return completions;
    }

    private void help(Player player) {
        Text.send(messages.get("help"), player);
    }

    private void info(Player player, @Nullable String name) {
        if (!plugin.hasPermission(player, "vanillatowns.info")) {
            Text.send(messages.get("no-permission"), player);
            return;
        }

        Town town;
        boolean self = false;

        if (name == null) {
            town = townRepository.findByMember(player).orElse(null);

            if (town == null) {
                Text.send(messages.get("not-in-town"), player);
                return;
            }

            self = true;
        } else {
            town = townRepository.findByName(name).orElse(null);

            if (town == null) {
                Text.send(messages.get("town-not-found"), player);
                return;
            }

            if (town.getMember(player) != null) self = true;
        }

        if (!self && !plugin.hasPermission(player, "vanillatowns.info.others")) {
            Text.send(messages.get("no-permission"), player);
            return;
        }


        String townHome;
        if (town.getHome() == null) {
            townHome = messages.get("home-not-set-info");
        } else {
            Optional<Location> homeLocationOpt = town.getHome().getLocation();
            if (homeLocationOpt.isEmpty()) {
                townHome = messages.get("home-not-set-info");
            } else {
                Location homeLocation = homeLocationOpt.get();
                townHome = messages.getAndReplace("town-home",
                        "world", homeLocation.getWorld().getName(),
                        "x", String.format("%.1f", homeLocation.getX()),
                        "y", String.format("%.1f", homeLocation.getY()),
                        "z", String.format("%.1f", homeLocation.getZ()),
                        "yaw", String.format("%.1f", homeLocation.getYaw()),
                        "pitch", String.format("%.1f", homeLocation.getPitch())
                );
            }
        }

        if (self) {
            Text.send(messages.getListAndReplace("town-info-self",
                    "town", town,
                    "balance", Utils.formatNumber(town.getBalance()),
                    "mayor", town.getMayor(),
                    "officers", String.join(", ", town.getOfficers().stream().map(TownMember::getName).toList()),
                    "citizens", String.join(", ", town.getCitizens().stream().map(TownMember::getName).toList()),
                    "home", townHome), player);
        } else {
            if (plugin.hasStaffPermission(player, "vanillatowns.staff.info")) {
                Text.send(messages.getListAndReplace("town-info-staff",
                        "town", town,
                        "balance", Utils.formatNumber(town.getBalance()),
                        "mayor", town.getMayor(),
                        "officers", String.join(", ", town.getOfficers().stream().map(TownMember::getName).toList()),
                        "citizens", String.join(", ", town.getCitizens().stream().map(TownMember::getName).toList()),
                        "home", townHome), player);
            } else {
                Text.send(messages.getListAndReplace("town-info-others",
                        "town", town,
                        "balance", Utils.formatNumber(town.getBalance()),
                        "mayor", town.getMayor(),
                        "officers", String.join(", ", town.getOfficers().stream().map(TownMember::getName).toList()),
                        "citizens", String.join(", ", town.getCitizens().stream().map(TownMember::getName).toList()),
                        "home", townHome), player);
            }
        }
    }

    private void create(Player player, String name) {
        if (!plugin.hasPermission(player, "vanillatowns.create")) {
            Text.send(messages.get("no-permission"), player);
            return;
        }

        if (cache.isInTown(player)) {
            Text.send(messages.get("already-in-town"), player);
            return;
        }

        name = Text.stripFormatting(name);
        if (!townNamePattern.matcher(name).matches()) {
            Text.send(messages.get("invalid-name"), player);
            return;
        }

        if (townRepository.findByName(name).isPresent()) {
            Text.send(messages.get("name-not-available"), player);
            return;
        }

        double creationCost = config.getDouble("town-creation-cost", 0.0);
        if (creationCost > 0) {
            if (!plugin.hasEnoughMoney(player, creationCost)) {
                Text.send(messages.get("not-enough-money"), player);
                return;
            }

            if (!plugin.withdrawMoney(player, creationCost)) {
                Text.send(messages.get("town-creation-failed-withdraw"), player);
                return;
            }
        }

        Town town = new Town(name, player);
        town = townRepository.save(town);
        cache.addTown(town);

        if (config.getBoolean("broadcast.town-created", true)) {
            Text.broadcast(messages.getAndReplace("town-created-broadcast",
                    "player", player,
                    "town", town));
        } else {
            Text.send(messages.getAndReplace("town-created", "town", town), player);
        }

        Text.info("Player <aqua>" + player.getName() + "<reset> created town <aqua>" + town.getName() + "<reset>");
    }

    private void invite(Player player, String name) {
        if (!plugin.hasPermission(player, "vanillatowns.invite")) {
            Text.send(messages.get("no-permission"), player);
            return;
        }

        Optional<Town> townOpt = cache.getTown(player);

        if (townOpt.isEmpty()) {
            Text.send(messages.get("not-in-town"), player);
            return;
        }

        Town town = townOpt.get();

        if (!town.canInvite(player)) {
            Text.send(messages.get("no-invite-permission"), player);
            return;
        }

        Player target = Bukkit.getPlayer(name);
        if (target == null) {
            Text.send(messages.get("player-not-found"), player);
            return;
        }

        if (!plugin.hasPermission(target, "vanillatowns.join")) {
            Text.send(messages.get("no-join-permission"), player);
            return;
        }

        if (town.getMember(target) != null) {
            Text.send(messages.get("player-already-in-town"), player);
            return;
        }

        VanillaTowns.INVITES.put(target.getUniqueId(), town.getId());

        Text.send(messages.getAndReplace("invite-sent", "player", target), player);
        Text.send(messages.getAndReplace("invite-received", "town", town, "player", player), target);
    }

    private void join(Player player) {
        if (!plugin.hasPermission(player, "vanillatowns.join")) {
            Text.send(messages.get("no-permission"), player);
            return;
        }

        if (cache.isInTown(player)) {
            Text.send(messages.get("already-in-town"), player);
            return;
        }

        if (!VanillaTowns.INVITES.containsKey(player.getUniqueId())) {
            Text.send(messages.get("no-invites"), player);
            return;
        }

        Town town = townRepository.findById(VanillaTowns.INVITES.get(player.getUniqueId())).orElse(null);

        if (town == null) {
            VanillaTowns.INVITES.remove(player.getUniqueId());
            Text.send(messages.get("no-invites"), player);
            return;
        }

        town.addMember(player);
        town = townRepository.save(town);
        cache.updateTown(town);
        VanillaTowns.INVITES.remove(player.getUniqueId());

        if (config.getBoolean("broadcast.player-joined-town", false)) {
            Text.broadcast(messages.getAndReplace("player-joined-town-broadcast",
                    "player", player,
                    "town", town));
        } else {
            Text.send(messages.getAndReplace("player-joined-town", "town", town), player);

            town.getMembers()
                    .stream()
                    .map(m -> Bukkit.getPlayer(m.getUniqueId()))
                    .filter(p -> p != null && !p.getUniqueId().equals(player.getUniqueId()))
                    .forEach(p -> Text.send(messages.getAndReplace("player-joined-town-members", "player", player), p));
        }

        Text.info("Player <aqua>" + player.getName() + "<reset> joined town <aqua>" + town.getName() + "<reset>");
    }

    private void leave(Player player) {
        if (!plugin.hasPermission(player, "vanillatowns.join")) {
            Text.send(messages.get("no-permission"), player);
            return;
        }

        Optional<Town> townOpt = cache.getTown(player);
        if (townOpt.isEmpty()) {
            Text.send(messages.get("not-in-town"), player);
            return;
        }

        Town town = townOpt.get();

        if (town.getMayor().getUniqueId().equals(player.getUniqueId())) {
            Text.send(messages.get("mayor-cant-leave"), player);
            return;
        }

        town.removeMember(player);
        town = townRepository.save(town);
        cache.updateTown(town);

        if (config.getBoolean("broadcast.player-left-town", false)) {
            Text.broadcast(messages.getAndReplace("player-left-town-broadcast",
                    "player", player,
                    "town", town));
        } else {
            Text.send(messages.getAndReplace("player-left-town", "town", town), player);

            town.getMembers()
                    .stream()
                    .map(m -> Bukkit.getPlayer(m.getUniqueId()))
                    .filter(p -> p != null && !p.getUniqueId().equals(player.getUniqueId()))
                    .forEach(p -> Text.send(messages.getAndReplace("player-left-town-members", "player", player), p));
        }

        Text.info("Player <aqua>" + player.getName() + "<reset> left town <aqua>" + town.getName() + "<reset>");
    }

    private void kick(Player player, String name) {
        if (!plugin.hasPermission(player, "vanillatowns.kick")) {
            Text.send(messages.get("no-permission"), player);
            return;
        }

        Optional<Town> townOpt = cache.getTown(player);
        if (townOpt.isEmpty()) {
            Text.send(messages.get("not-in-town"), player);
            return;
        }

        Town town = townOpt.get();

        TownMember targetMember = town.getMember(name);
        if (targetMember == null) {
            Text.send(messages.get("not-in-your-town"), player);
            return;
        }

        if (!town.canKick(player, targetMember)) {
            Text.send(messages.get("no-kick-permission"), player);
            return;
        }

        town.removeMember(targetMember.getUniqueId());
        town = townRepository.save(town);
        cache.updateTown(town);

        if (config.getBoolean("broadcast.player-left", false)) {
            Text.broadcast(messages.getAndReplace("player-kicked-from-town-broadcast",
                    "player", player,
                    "town", town,
                    "target", targetMember));
        } else {
            Text.send(messages.getAndReplace("player-kicked-from-town", "town", town, "target", targetMember), player);

            town.getMembers()
                    .stream()
                    .map(m -> Bukkit.getPlayer(m.getUniqueId()))
                    .filter(p -> p != null && !p.getUniqueId().equals(player.getUniqueId()) && !p.getUniqueId().equals(targetMember.getUniqueId()))
                    .forEach(p -> Text.send(messages.getAndReplace("player-kicked-from-town-members", "player", player, "target", targetMember), p));

            Player target = Bukkit.getPlayer(targetMember.getUniqueId());
            if (target != null) {
                Text.send(messages.getAndReplace("kicked-from-town-target", "town", town, "player", player), target);
            }
        }

        Text.info("Player <aqua>" + player.getName() + "<reset> kicked <aqua>" + targetMember.getName() + "<reset> from town <aqua>" + town.getName() + "<reset>");
    }

    private void rename(Player player, String name) {
        if (!plugin.hasPermission(player, "vanillatowns.rename")) {
            Text.send(messages.get("no-permission"), player);
            return;
        }

        Optional<Town> townOpt = cache.getTown(player);
        if (townOpt.isEmpty()) {
            Text.send(messages.get("not-in-town"), player);
            return;
        }

        Town town = townOpt.get();

        if (!town.getMayor().getUniqueId().equals(player.getUniqueId())) {
            Text.send(messages.get("not-mayor"), player);
            return;
        }

        name = Text.stripFormatting(name);
        if (!townNamePattern.matcher(name).matches()) {
            Text.send(messages.get("invalid-name"), player);
            return;
        }

        String oldName = town.getName();

        town.setName(name);
        town = townRepository.save(town);
        cache.updateTown(town);

        String finalName = name;
        if (config.getBoolean("broadcast.town-renamed", false)) {
            Bukkit.getOnlinePlayers()
                    .stream()
                    .filter(p -> !p.getUniqueId().equals(player.getUniqueId()))
                    .forEach(p -> Text.send(messages.getAndReplace("town-renamed-broadcast",
                            "player", player,
                            "old_name", oldName,
                            "new_name", finalName), p));
        } else {
            Text.send(messages.getAndReplace("town-renamed", "old", oldName, "new", name), player);
            town.getMembers()
                    .stream()
                    .map(m -> Bukkit.getPlayer(m.getUniqueId()))
                    .filter(p -> p != null && !p.getUniqueId().equals(player.getUniqueId()))
                    .forEach(p -> Text.send(messages.getAndReplace("town-renamed-members", "old", oldName, "new", finalName), p));
        }

        Text.info("Player <aqua>" + player.getName() + "<reset> renamed town <aqua>" + oldName + "<reset> to <aqua>" + name + "<reset>");
    }

    private void give(Player player, String name) {
        if (!plugin.hasPermission(player, "vanillatowns.give")) {
            Text.send(messages.get("no-permission"), player);
            return;
        }

        Optional<Town> townOpt = cache.getTown(player);
        if (townOpt.isEmpty()) {
            Text.send(messages.get("not-in-town"), player);
            return;
        }

        Town town = townOpt.get();

        if (!town.getMayor().getUniqueId().equals(player.getUniqueId())) {
            Text.send(messages.get("no-give-permission"), player);
            return;
        }

        TownMember targetMember = town.getMember(name);
        if (targetMember == null) {
            Text.send(messages.get("not-in-your-town"), player);
            return;
        }

        if (targetMember.getUniqueId().equals(player.getUniqueId())) {
            Text.send(messages.get("cant-give-to-yourself"), player);
            return;
        }

        town.setMayor(targetMember);
        town = townRepository.save(town);
        cache.updateTown(town);

        town.getMembers()
                .stream()
                .map(m -> Bukkit.getPlayer(m.getUniqueId()))
                .filter(Objects::nonNull)
                .forEach(p -> Text.send(messages.getAndReplace("town-given", "player", targetMember), p));

        Text.info("Player <aqua>" + player.getName() + "<reset> gave town <aqua>" + town.getName() + "<reset> to <aqua>" + targetMember.getName() + "<reset>");
    }

    private void delete(Player player) {
        if (!plugin.hasPermission(player, "vanillatowns.create")) {
            Text.send(messages.get("no-permission"), player);
            return;
        }

        Optional<Town> townOpt = cache.getTown(player);
        if (townOpt.isEmpty()) {
            Text.send(messages.get("not-in-town"), player);
            return;
        }

        Town town = townOpt.get();

        if (!town.getMayor().getUniqueId().equals(player.getUniqueId())) {
            Text.send(messages.get("no-delete-permission"), player);
            return;
        }

        townRepository.delete(town);
        cache.removeTown(town);

        if (town.getBalance() > 0) {
            if (!plugin.giveMoney(player, town.getBalance())) {
                Text.warning("Failed to give <yellow>" + town.getBalance() + "$<reset> to player <aqua>" + player.getName() + "<reset> after deleting town <aqua>" + town.getName());
            }
        }

        if (config.getBoolean("broadcast.town-deleted", true)) {
            Bukkit.getOnlinePlayers()
                    .stream()
                    .forEach(p -> Text.send(messages.getAndReplace("town-deleted-broadcast",
                            "player", player,
                            "town", town), p));
        } else {
            Text.send(messages.get("town-deleted-members"), player);
        }

        Text.info("Player <aqua>" + player.getName() + "<reset> deleted town <aqua>" + town.getName() + "<reset>");
    }

    private void balance(Player player, String name) {
        if (!plugin.hasPermission(player, "vanillatowns.balance")) {
            Text.send(messages.get("no-permission"), player);
            return;
        }

        Town town;
        boolean self = false;

        if (name == null) {
            town = townRepository.findByMember(player).orElse(null);

            if (town == null) {
                Text.send(messages.get("not-in-town"), player);
                return;
            }

            self = true;
        } else {
            town = townRepository.findByName(name).orElse(null);

            if (town == null) {
                Text.send(messages.get("town-not-found"), player);
                return;
            }

            if (town.getMember(player) != null) self = true;
        }

        if (!self && !plugin.hasPermission(player, "vanillatowns.balance.others")) {
            Text.send(messages.get("no-permission"), player);
            return;
        }

        Text.send(messages.getAndReplace("town-balance", "town", town, "balance", Utils.formatNumber(town.getBalance())), player);
    }

    private void deposit(Player player, String amountStr) {
        if (!plugin.hasPermission(player, "vanillatowns.deposit")) {
            Text.send(messages.get("no-permission"), player);
            return;
        }

        Optional<Town> townOpt = cache.getTown(player);
        if (townOpt.isEmpty()) {
            Text.send(messages.get("not-in-town"), player);
            return;
        }

        Town town = townOpt.get();

        if (!town.getMember(player).canDeposit()) {
            Text.send(messages.get("no-deposit-permission"), player);
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            Text.send(messages.get("invalid-amount"), player);
            return;
        }

        if (amount <= 0) {
            Text.send(messages.get("invalid-amount"), player);
            return;
        }

        if (!plugin.hasEnoughMoney(player, amount)) {
            Text.send(messages.get("not-enough-money"), player);
            return;
        }

        if (!plugin.withdrawMoney(player, amount)) {
            Text.send(messages.get("deposit-failed"), player);
            return;
        }

        town.deposit(amount);
        town = townRepository.save(town);
        cache.updateTown(town);

        Text.send(messages.getAndReplace("deposit-success", "amount", Utils.formatNumber(amount)), player);

        Text.info("Player <aqua>" + player.getName() + "<reset> deposited <yellow>" + amount + "$<reset> to town <aqua>" + town.getName() + "<reset>");
    }

    private void withdraw(Player player, String amountStr) {
        if (!plugin.hasPermission(player, "vanillatowns.withdraw")) {
            Text.send(messages.get("no-permission"), player);
            return;
        }

        Optional<Town> townOpt = cache.getTown(player);
        if (townOpt.isEmpty()) {
            Text.send(messages.get("not-in-town"), player);
            return;
        }

        Town town = townOpt.get();

        if (!town.getMember(player).canWithdraw()) {
            Text.send(messages.get("no-withdraw-permission"), player);
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            Text.send(messages.get("invalid-amount"), player);
            return;
        }

        if (amount <= 0) {
            Text.send(messages.get("invalid-amount"), player);
            return;
        }

        if (town.getBalance() < amount) {
            Text.send(messages.get("not-enough-money-town"), player);
            return;
        }

        town.withdraw(amount);
        town = townRepository.save(town);
        cache.updateTown(town);

        if (!plugin.giveMoney(player, amount)) {
            Text.send(messages.get("withdraw-failed"), player);
            Text.warning("Failed to give <yellow>" + amount + "$<reset> to player <aqua>" + player.getName() + "<reset> after withdrawing from town <aqua>" + town.getName());
            return;
        }

        Text.send(messages.getAndReplace("withdraw-success", "amount", Utils.formatNumber(amount)), player);

        Text.info("Player <aqua>" + player.getName() + "<reset> withdrew <yellow>" + amount + "$<reset> from town <aqua>" + town.getName() + "<reset>");
    }

    private void baltop(Player player) {
        if (!plugin.hasPermission(player, "vanillatowns.baltop")) {
            Text.send(messages.get("no-permission"), player);
            return;
        }

        List<Town> towns = townRepository.findAll();
        towns.sort((t1, t2) -> Double.compare(t2.getBalance(), t1.getBalance()));
        Optional<Town> townOpt = cache.getTown(player);

        Text.send(messages.get("baltop-header"), player);

        boolean found = false;
        for (int i = 0; i < Math.min(10, towns.size()); i++) {
            Town town = towns.get(i);
            if (townOpt.isPresent() && town.getId() == townOpt.get().getId()) found = true;

            Text.send(messages.getAndReplace("baltop-entry", "position", i + 1, "town", town, "balance", Utils.formatNumber(town.getBalance())), player);
        }

        if (!found && townOpt.isPresent()) {
            Town town = towns.stream().filter(t -> t.getId() == townOpt.get().getId()).findFirst().orElse(null);
            if (town != null) {
                int index = towns.indexOf(town);
                Text.send(messages.getAndReplace("baltop-entry", "position", index + 1, "town", town, "balance", Utils.formatNumber(town.getBalance())), player);
            }
        }

        Text.send(messages.get("baltop-footer"), player);
    }

    private void setHome(Player player) {
        if (!plugin.hasPermission(player, "vanillatowns.home.edit")) {
            Text.send(messages.get("no-permission"), player);
            return;
        }

        Optional<Town> townOpt = cache.getTown(player);
        if (townOpt.isEmpty()) {
            Text.send(messages.get("not-in-town"), player);
            return;
        }

        Town town = townOpt.get();

        if (!town.canEditHome(player)) {
            Text.send(messages.get("no-sethome-permission"), player);
            return;
        }

        String world = player.getWorld().getName();
        if (config.getStringList("town-home-dimension-blacklist", List.of()).stream().anyMatch(world::equalsIgnoreCase)) {
            Text.send(messages.get("home-dimension-blacklisted"), player);
            return;
        }

        double setHomeCost = config.getDouble("town-sethome-cost", 0.0);
        if (setHomeCost > 0) {
            if (town.getBalance() >= setHomeCost) {
                town.withdraw(setHomeCost);
            } else {
                double dueAmount = setHomeCost - town.getBalance();

                if (!plugin.hasEnoughMoney(player, dueAmount)) {
                    Text.send(messages.get("not-enough-money"), player);
                    return;
                }

                if (!plugin.withdrawMoney(player, dueAmount)) {
                    Text.send(messages.get("sethome-failed-withdraw"), player);
                    return;
                }

                town.setBalance(0);
            }
        }

        town.setHome(player.getLocation());
        town = townRepository.save(town);
        cache.updateTown(town);

        Text.send(messages.get("home-set"), player);

        Text.info("Player <aqua>" + player.getName() + "<reset> set home for town <aqua>" + town.getName() + "<reset>");
    }

    private void delHome(Player player) {
        if (!plugin.hasPermission(player, "vanillatowns.home.edit")) {
            Text.send(messages.get("no-permission"), player);
            return;
        }

        Optional<Town> townOpt = cache.getTown(player);
        if (townOpt.isEmpty()) {
            Text.send(messages.get("not-in-town"), player);
            return;
        }

        Town town = townOpt.get();

        if (!town.canEditHome(player)) {
            Text.send(messages.get("no-delhome-permission"), player);
            return;
        }

        town.setHome(null);
        town = townRepository.save(town);
        cache.updateTown(town);

        Text.send(messages.get("home-deleted"), player);

        Text.info("Player <aqua>" + player.getName() + "<reset> deleted home for town <aqua>" + town.getName() + "<reset>");
    }

    private void home(Player player) {
        if (!plugin.hasPermission(player, "vanillatowns.home")) {
            Text.send(messages.get("no-permission"), player);
            return;
        }

        Optional<Town> townOpt = cache.getTown(player);
        if (townOpt.isEmpty()) {
            Text.send(messages.get("not-in-town"), player);
            return;
        }

        Town town = townOpt.get();

        if (town.getHome() == null || town.getHome().getLocation().isEmpty()) {
            Text.send(messages.get("home-not-set"), player);
            return;
        }

        double homeTeleportCost = config.getDouble("town-home-teleport-cost", 0.0);
        if (homeTeleportCost > 0) {
            if (!plugin.hasEnoughMoney(player, homeTeleportCost)) {
                Text.send(messages.get("not-enough-money"), player);
                return;
            }

            if (!plugin.withdrawMoney(player, homeTeleportCost)) {
                Text.send(messages.get("home-teleport-failed-withdraw"), player);
                return;
            }
        }

        int teleportCooldown = config.getInt("town-home-teleport-cooldown", 0);
        if (teleportCooldown > 0) {
            Date lastTeleport = VanillaTowns.TELEPORT_COOLDOWN.get(player);

            if (lastTeleport != null) {
                long diff = new Date().getTime() - lastTeleport.getTime();
                if (diff < (long) teleportCooldown * 1000) {
                    Text.send(messages.getAndReplace("home-teleport-cooldown", "time", (int) (teleportCooldown - diff / 1000)), player);
                    return;
                }
            }
        }

        int countdownTime = config.getInt("town-home-teleport-countdown", 0);
        if (countdownTime > 0) {
            HomeTeleportRequest request = new HomeTeleportRequest(plugin, player, town.getHome().getLocation().get(), countdownTime);
            VanillaTowns.TELEPORTS.put(player, request);
            request.teleport();

            Text.send(messages.getAndReplace("home-teleport-countdown", "time", countdownTime), player);
        } else {
            player.teleport(town.getHome().getLocation().get());
            Text.send(messages.get("home-teleporting"), player);

            if (teleportCooldown > 0) {
                VanillaTowns.TELEPORT_COOLDOWN.put(player, new Date());
            }
        }
    }

    private void user(Player player, String[] args) {
        switch (args[1].toLowerCase()) {
            case "setofficer" -> {
                if (args.length < 3) help(player);
                else setOfficer(player, args[2]);
            }
            case "removeofficer" -> {
                if (args.length < 3) help(player);
                else removeOfficer(player, args[2]);
            }
            case "deposit" -> {
                if (args.length < 4) help(player);
                else deposit(player, args[2], args[3]);
            }
            case "withdraw" -> {
                if (args.length < 4) help(player);
                else withdraw(player, args[2], args[3]);
            }
            default -> help(player);
        }
    }

    private void setOfficer(Player player, String name) {
        Optional<Town> townOpt = cache.getTown(player);
        if (townOpt.isEmpty()) {
            Text.send(messages.get("not-in-town"), player);
            return;
        }

        Town town = townOpt.get();

        if (!town.getMayor().getUniqueId().equals(player.getUniqueId())) {
            Text.send(messages.get("no-setofficer-permission"), player);
            return;
        }

        TownMember targetMember = town.getMember(name);
        if (targetMember == null) {
            Text.send(messages.get("not-in-your-town"), player);
            return;
        }

        if (targetMember.getUniqueId().equals(player.getUniqueId())) {
            Text.send(messages.get("cant-setofficer-yourself"), player);
            return;
        }

        if (targetMember.getRole().equals(TownRole.OFFICER)) {
            Text.send(messages.get("already-officer"), player);
            return;
        }

        targetMember.setRole(TownRole.OFFICER);
        town = townRepository.save(town);
        cache.updateTown(town);

        Text.send(messages.getAndReplace("officer-set", "player", targetMember), player);

        Player target = Bukkit.getPlayer(targetMember.getUniqueId());
        if (target != null) {
            Text.send(messages.get("officer-set-target"), target);
        }

        Text.info("Player <aqua>" + player.getName() + "<reset> set officer <aqua>" + targetMember.getName() + "<reset> in town <aqua>" + town.getName() + "<reset>");
    }

    private void removeOfficer(Player player, String name) {
        Optional<Town> townOpt = cache.getTown(player);
        if (townOpt.isEmpty()) {
            Text.send(messages.get("not-in-town"), player);
            return;
        }

        Town town = townOpt.get();

        if (!town.getMayor().getUniqueId().equals(player.getUniqueId())) {
            Text.send(messages.get("no-removeofficer-permission"), player);
            return;
        }

        TownMember targetMember = town.getMember(name);
        if (targetMember == null) {
            Text.send(messages.get("not-in-your-town"), player);
            return;
        }

        if (targetMember.getRole().equals(TownRole.CITIZEN)) {
            Text.send(messages.get("not-officer"), player);
            return;
        }

        targetMember.setRole(TownRole.CITIZEN);
        town = townRepository.save(town);
        cache.updateTown(town);

        Text.send(messages.getAndReplace("officer-removed", "player", targetMember), player);

        Player target = Bukkit.getPlayer(targetMember.getUniqueId());
        if (target != null) {
            Text.send(messages.get("officer-removed-target"), target);
        }

        Text.info("Player <aqua>" + player.getName() + "<reset> removed officer <aqua>" + targetMember.getName() + "<reset> in town <aqua>" + town.getName() + "<reset>");
    }

    private void deposit(Player player, String name, String bool) {
        Optional<Town> townOpt = cache.getTown(player);
        if (townOpt.isEmpty()) {
            Text.send(messages.get("not-in-town"), player);
            return;
        }

        Town town = townOpt.get();

        if (!town.getMayor().getUniqueId().equals(player.getUniqueId())) {
            Text.send(messages.get("no-deposit-change-permission"), player);
            return;
        }

        TownMember targetMember = town.getMember(name);
        if (targetMember == null) {
            Text.send(messages.get("not-in-your-town"), player);
            return;
        }

        if (targetMember.getRole().equals(TownRole.CITIZEN)) {
            Text.send(messages.get("not-citizen"), player);
            return;
        }

        boolean deposit;
        if (bool.equalsIgnoreCase("true") || bool.equalsIgnoreCase("yes") || bool.equalsIgnoreCase("allow")) deposit = true;
        else if (bool.equalsIgnoreCase("false") || bool.equalsIgnoreCase("no") || bool.equalsIgnoreCase("deny")) deposit = false;
        else {
            Text.send(messages.get("invalid-boolean"), player);
            return;
        }

        targetMember.setDeposit(deposit);
        town = townRepository.save(town);
        cache.updateTown(town);

        if (deposit) {
            Text.send(messages.getAndReplace("deposit-allowed", "player", targetMember), player);
        } else {
            Text.send(messages.getAndReplace("deposit-denied", "player", targetMember), player);
        }

        Player target = Bukkit.getPlayer(targetMember.getUniqueId());
        if (target != null) {
            if (deposit) {
                Text.send(messages.get("deposit-allowed-target"), target);
            } else {
                Text.send(messages.get("deposit-denied-target"), target);
            }
        }

        Text.info("Player <aqua>" + player.getName() + "<reset> " + (deposit ? "allowed" : "denied") + " deposit for <aqua>" + targetMember.getName() + "<reset> in town <aqua>" + town.getName() + "<reset>");
    }

    private void withdraw(Player player, String name, String bool) {
        Optional<Town> townOpt = cache.getTown(player);
        if (townOpt.isEmpty()) {
            Text.send(messages.get("not-in-town"), player);
            return;
        }

        Town town = townOpt.get();

        if (!town.getMayor().getUniqueId().equals(player.getUniqueId())) {
            Text.send(messages.get("no-withdraw-change-permission"), player);
            return;
        }

        TownMember targetMember = town.getMember(name);
        if (targetMember == null) {
            Text.send(messages.get("not-in-your-town"), player);
            return;
        }

        if (targetMember.getRole().equals(TownRole.CITIZEN)) {
            Text.send(messages.get("not-citizen"), player);
            return;
        }

        boolean withdraw;
        if (bool.equalsIgnoreCase("true") || bool.equalsIgnoreCase("yes") || bool.equalsIgnoreCase("allow")) withdraw = true;
        else if (bool.equalsIgnoreCase("false") || bool.equalsIgnoreCase("no") || bool.equalsIgnoreCase("deny")) withdraw = false;
        else {
            Text.send(messages.get("invalid-boolean"), player);
            return;
        }

        targetMember.setWithdraw(withdraw);
        town = townRepository.save(town);
        cache.updateTown(town);

        if (withdraw) {
            Text.send(messages.getAndReplace("withdraw-allowed", "player", targetMember), player);
        } else {
            Text.send(messages.getAndReplace("withdraw-denied", "player", targetMember), player);
        }

        Player target = Bukkit.getPlayer(targetMember.getUniqueId());
        if (target != null) {
            if (withdraw) {
                Text.send(messages.get("withdraw-allowed-target"), target);
            } else {
                Text.send(messages.get("withdraw-denied-target"), target);
            }
        }

        Text.info("Player <aqua>" + player.getName() + "<reset> " + (withdraw ? "allowed" : "denied") + " withdraw for <aqua>" + targetMember.getName() + "<reset> in town <aqua>" + town.getName() + "<reset>");
    }
}
