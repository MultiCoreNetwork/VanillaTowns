package it.multicoredev.vt.commands;

import it.multicoredev.mbcore.spigot.Chat;
import it.multicoredev.mbcore.spigot.util.TabCompleterUtil;
import it.multicoredev.mclib.yaml.Configuration;
import it.multicoredev.vt.Utils;
import it.multicoredev.vt.VanillaTowns;
import it.multicoredev.vt.storage.Town;
import it.multicoredev.vt.storage.TownMember;
import it.multicoredev.vt.storage.Towns;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Copyright © 2020 by Lorenzo Magni
 * This file is part of VanillaTowns.
 * VanillaTowns is under "The 3-Clause BSD License", you can find a copy <a href="https://opensource.org/licenses/BSD-3-Clause">here</a>.
 * <p>
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
 * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
public class TownCommand implements CommandExecutor, TabExecutor {
    private final VanillaTowns plugin;
    private final Configuration config;
    private final Towns towns;

    public TownCommand(VanillaTowns plugin, Configuration config, Towns towns) {
        this.plugin = plugin;
        this.config = config;
        this.towns = towns;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            Chat.send(getString("not-player"), sender);
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("vanillatowns.town")) {
            Chat.send(getString("insufficient-perm"), player);
            return true;
        }

        if (args.length < 1) {
            town(player, null);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "help":
                help(player);
                break;
            case "baltop":
                baltop(player);
                break;
            case "create":
                create(player, args);
                break;
            case "delete":
                delete(player);
                break;
            case "invite":
                invite(player, args);
                break;
            case "join":
                join(player, args);
                break;
            case "kick":
                kick(player, args);
                break;
            case "leave":
                leave(player);
                break;
            case "give":
                give(player, args);
                break;
            case "home":
                home(player);
                break;
            case "sethome":
                setHome(player);
                break;
            case "delhome":
                delHome(player);
                break;
            case "balance":
                balance(player);
                break;
            case "deposit":
                deposit(player, args);
                break;
            case "withdraw":
                withdraw(player, args);
                break;
            case "user":
                user(player, args);
                break;
            case "rename":
                rename(player, args);
                break;
            default:
                town(player, args[0]);
                break;
        }

        return true;
    }

    private String getString(String path) {
        return config.getString("messages." + path);
    }

    private String incorrectUsage(String usage) {
        return getString("incorrect-usage").replace("{usage}", usage);
    }

    private void town(Player player, String name) {
        Town town;

        if (name != null) {
            town = towns.getTown(name);
            if (town == null) {
                Chat.send(getString("town-not-found"), player);
                return;
            }
        } else {
            if (!towns.hasTown(player)) {
                Chat.send(getString("not-in-town"), player);
                return;
            }

            town = towns.getTown(player);
        }

        TownMember leader = town.getLeader();
        List<TownMember> admins = town.getAdmins();
        List<TownMember> members = town.getSimpleMembers();
        StringBuilder adminsName = new StringBuilder();
        StringBuilder membersName = new StringBuilder();

        if (admins.size() > 0) {
            for (int i = 0; i < admins.size(); i++) {
                adminsName.append(admins.get(i).getName());
                if (i < admins.size() - 1) adminsName.append(", ");
            }
        } else {
            adminsName.append("[]");
        }

        if (members.size() > 0) {
            for (int i = 0; i < members.size(); i++) {
                membersName.append(members.get(i).getName());
                if (i < members.size() - 1) membersName.append(", ");
            }
        } else {
            membersName.append("[]");
        }

        for (String str : config.getStringList("town")) {
            Chat.send(str.replace("{town}", town.getName())
                    .replace("{balance}", Utils.formatNumber(town.getBalance()))
                    .replace("{leader}", leader != null ? leader.getName() : "")
                    .replace("{admins}", adminsName.toString())
                    .replace("{members}", membersName.toString()), player);
        }
    }

    private void help(Player player) {
        for (String line : config.getStringList("help")) {
            Chat.send(line, player);
        }
    }

    private void baltop(Player player) {
        List<Town> list = this.towns.getTowns();
        Town playerTown = this.towns.getTown(player);
        boolean isInTop = false;

        Chat.send(getString("baltop-head"), player);
        for (int i = 0; i < 10; i++) {
            Town town = list.get(i);
            if (playerTown != null && playerTown.equals(town)) isInTop = true;

            Chat.send(getString("baltop")
                    .replace("{position}", String.valueOf(i + 1))
                    .replace("{town}", town.getName())
                    .replace("{balance}", String.valueOf(Utils.formatNumber(town.getBalance()))), player);
        }
        if (playerTown != null && !isInTop) Chat.send(getString("baltop")
                .replace("{position}", String.valueOf(list.indexOf(playerTown) + 1))
                .replace("{town}", playerTown.getName())
                .replace("{balance}", String.valueOf(Utils.formatNumber(playerTown.getBalance()))), player);
        Chat.send(getString("baltop-footer"), player);
    }

    private void create(Player player, String[] args) {
        if (args.length < 2) {
            Chat.send(incorrectUsage("/town create <name>"), player);
            return;
        }

        if (towns.hasTown(player)) {
            Chat.send(getString("already-in-town"), player);
            return;
        }

        String name = Chat.getDiscolored(args[1]);
        if (towns.townExists(name)) {
            Chat.send(getString("name-not-available"), player);
            return;
        }

        Town town = new Town(towns.getFirstId(), name, player);
        towns.addTown(town);
        plugin.saveTowns();

        Chat.send(getString("town-created").replace("{town}", name), player);
        Utils.broadcast(getString("town-created-broadcast")
                .replace("{player}", player.getDisplayName())
                .replace("{town}", name), player);
        Chat.info("&b" + player.getDisplayName() + " &3created town &b" + name);
    }

    private void delete(Player player) {
        if (!towns.hasTown(player)) {
            Chat.send(getString("not-in-town"), player);
            return;
        }

        Town town = towns.getTown(player);

        if (!town.isLeader(player)) {
            Chat.send(getString("not-leader"), player);
            return;
        }

        towns.removeTown(town);
        plugin.saveTowns();

        if (town.getBalance() > 0) {
            Utils.giveMoney(player, town.getBalance());
            Chat.send(getString("town-balance-chargeback")
                    .replace("{money}", String.valueOf(town.getBalance())), player);
        }

        Utils.broadcast(getString("town-deleted-broadcast")
                .replace("{player}", player.getDisplayName())
                .replace("{town}", town.getName()), player);
        Chat.info("&b" + player.getDisplayName() + " &3deleted town &b" + town.getName());
    }

    private void invite(Player player, String[] args) {
        if (args.length < 2) {
            Chat.send(incorrectUsage("/town invite <player>"), player);
            return;
        }

        if (!towns.hasTown(player)) {
            Chat.send(getString("not-in-town"), player);
            return;
        }

        Town town = towns.getTown(player);
        if (!town.isAdmin(player) && !town.isLeader(player)) {
            Chat.send(getString("not-admin"), player);
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            Chat.send(getString("player-not-found"), player);
            return;
        }

        plugin.addInvite(target, town);
        Chat.send(getString("player-invited").replace("{player}", target.getDisplayName()), player);
        Chat.send(getString("invite-received")
                .replace("{town}", town.getName())
                .replace("{player}", player.getDisplayName()), target);
        Chat.info("&b" + player.getDisplayName() + " &3invited &b" + player.getDisplayName() + " &3to town &b" + town.getName());
    }

    private void join(Player player, String[] args) {
        if (args.length < 2) {
            Chat.send(incorrectUsage("/town join <town>"), player);
            return;
        }

        if (towns.hasTown(player)) {
            Chat.send(getString("already-in-team"), player);
            return;
        }

        if (!plugin.hasInvite(player)) {
            Chat.send(getString("no-invites"), player);
            return;
        }

        Town town = plugin.getInvite(player, args[1]);
        if (town == null) {
            Chat.send(getString("invite-not-found"), player);
            return;
        }

        Chat.send(getString("town-joined").replace("{town}", town.getName()), player);
        for (TownMember member : town.getMembers()) {
            Player target = member.getPlayer();
            if (target != null)
                Chat.send(getString("player-joined-town").replace("{player}", player.getDisplayName()), target);
        }
        Chat.info("&b" + player.getDisplayName() + " &3joined the town &b" + town.getName());

        town.addMember(new TownMember(player, false));
        plugin.saveTowns();
        plugin.removeInvite(town);
    }

    private void kick(Player player, String[] args) {
        if (args.length < 2) {
            Chat.send(incorrectUsage("/town kick <player>"), player);
            return;
        }

        if (!towns.hasTown(player)) {
            Chat.send(getString("not-in-town"), player);
            return;
        }

        Town town = towns.getTown(player);
        if (!town.isAdmin(player) && !town.isLeader(player)) {
            Chat.send(getString("not-admin"), player);
            return;
        }

        String name = args[1];
        if (!town.hasMember(name)) {
            Chat.send(getString("not-in-your-town"), player);
            return;
        }

        town.removeMember(name);
        plugin.saveTowns();

        Chat.send(getString("player-kicked").replace("{player}", name), player);
        Player target = Bukkit.getPlayer(name);
        if (target != null) Chat.send(getString("you-have-been-kicked").replace("{town}", town.getName()), target);
        Chat.info("&b" + player.getDisplayName() + " &3kicked &b" + name + " &3from town &b" + town.getName());
    }

    private void leave(Player player) {
        if (!towns.hasTown(player)) {
            Chat.send(getString("not-in-town"), player);
            return;
        }

        Town town = towns.getTown(player);
        if (town.isLeader(player)) {
            Chat.send(getString("give-first"), player);
            return;
        }

        town.removeMember(player.getName());
        plugin.saveTowns();

        Chat.send(getString("you-left").replace("{town}", town.getName()), player);
        for (TownMember member : town.getMembers()) {
            Player target = member.getPlayer();
            if (target != null)
                Chat.send(getString("player-left").replace("{player}", player.getDisplayName()), target);
        }
        Chat.info("&b" + player.getDisplayName() + " &3left the town &b" + town.getName());
    }

    private void give(Player player, String[] args) {
        if (args.length < 2) {
            Chat.send(incorrectUsage("/town give <player>"), player);
            return;
        }

        if (!towns.hasTown(player)) {
            Chat.send(getString("not-in-town"), player);
            return;
        }

        Town town = towns.getTown(player);
        if (!town.isAdmin(player)) {
            Chat.send(getString("not-admin"), player);
            return;
        }

        String name = args[1];
        if (!town.hasMember(name)) {
            Chat.send(getString("not-in-your-town"), player);
            return;
        }

        TownMember ex = town.getMember(player);
        TownMember nw = town.getMember(name);

        if (ex == null || nw == null) {
            Chat.send(getString("town-give-failed"), player);
            return;
        }

        ex.setLeader(false);
        nw.setLeader(true);
        plugin.saveTowns();

        Chat.send(getString("leader-transferred").replace("{player}", player.getDisplayName()), player);
        Player target = Bukkit.getPlayer(name);
        if (target != null) Chat.send(getString("you-are-leader"), target);

        Chat.info("&b" + player.getDisplayName() + " &3gave the leader &b" + name);
    }

    private void home(Player player) {
        if (!towns.hasTown(player)) {
            Chat.send(getString("not-in-town"), player);
            return;
        }

        Town town = towns.getTown(player);
        Location home = town.getHomeLocation();
        if (home == null) {
            Chat.send(getString("town-without-home"), player);
            return;
        }

        Chat.send(getString("teleport").replace("{time}", String.valueOf(config.getInt("teleport-timer"))), player);
        Bukkit.getScheduler().runTaskLater(plugin, () -> player.teleport(home), config.getInt("teleport-timer") * 20L);
    }

    private void setHome(Player player) {
        if (!towns.hasTown(player)) {
            Chat.send(getString("not-in-town"), player);
            return;
        }

        Town town = towns.getTown(player);
        if (!town.isLeader(player) && !town.isAdmin(player)) {
            Chat.send(getString("not-admin"), player);
            return;
        }

        town.setHomeLocation(player.getLocation());
        plugin.saveTowns();

        Chat.send(getString("home-set")
                .replace("{location}",
                        (int) player.getLocation().getX() + " " +
                                (int) player.getLocation().getY() + " " +
                                (int) player.getLocation().getZ()), player);
    }

    private void delHome(Player player) {
        if (!towns.hasTown(player)) {
            Chat.send(getString("not-in-town"), player);
            return;
        }

        Town town = towns.getTown(player);
        if (!town.isLeader(player) && !town.isAdmin(player)) {
            Chat.send(getString("not-admin"), player);
            return;
        }

        town.setHomeLocation(null);
        plugin.saveTowns();

        Chat.send(getString("home-removed"), player);
    }

    private void balance(Player player) {
        if (!towns.hasTown(player)) {
            Chat.send(getString("not-in-town"), player);
            return;
        }

        Town town = towns.getTown(player);

        Chat.send(getString("town-balance")
                .replace("{balance}", Utils.formatNumber(town.getBalance())), player);
    }

    private void deposit(Player player, String[] args) {
        if (args.length < 2) {
            Chat.send(incorrectUsage("/town deposit <amount>"), player);
            return;
        }

        if (!towns.hasTown(player)) {
            Chat.send(getString("not-in-town"), player);
            return;
        }

        Town town = towns.getTown(player);
        TownMember member = town.getMember(player);
        if (!member.canDeposit() && !member.isAdmin() && !member.isLeader()) {
            Chat.send(getString("cannot-deposit"), player);
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[1]);
            amount = Math.abs(amount);
        } catch (NumberFormatException ignored) {
            Chat.send(incorrectUsage("/town deposit <amount>"), player);
            return;
        }

        if (amount <= 0) return;

        if (!Utils.hasEnoughMoney(player, amount)) {
            Chat.send(getString("insufficient-money"), player);
            return;
        }

        if (!Utils.withdrawMoney(player, amount)) {
            Chat.send(getString("transaction-error"), player);
            return;
        }

        town.addBalance(amount);
        plugin.saveTowns();

        Chat.send(getString("deposit-success")
                .replace("{money}", Utils.formatNumber(amount))
                .replace("{balance}", Utils.formatNumber(town.getBalance())), player);
        Chat.info("&b" + player.getDisplayName() + " &3deposited &b" + Utils.formatNumber(amount) + " &3to &b" + town.getName() + " &3bank. Balance: &b" + Utils.formatNumber(town.getBalance()));
    }

    private void withdraw(Player player, String[] args) {
        if (args.length < 2) {
            Chat.send(incorrectUsage("/town withdraw <amount>"), player);
            return;
        }

        if (!towns.hasTown(player)) {
            Chat.send(getString("not-in-town"), player);
            return;
        }

        Town town = towns.getTown(player);
        TownMember member = town.getMember(player);
        if (!member.canWithdraw() && !member.isAdmin() && !member.isLeader()) {
            Chat.send(getString("cannot-withdraw"), player);
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[1]);
            amount = -Math.abs(amount);
        } catch (NumberFormatException ignored) {
            Chat.send(incorrectUsage("/town withdraw <amount>"), player);
            return;
        }

        if (amount >= 0) return;

        if (town.getBalance() < Math.abs(amount)) {
            Chat.send(getString("insufficient-town-money"), player);
            return;
        }

        if (!Utils.giveMoney(player, Math.abs(amount))) {
            Chat.send(getString("transaction-error"), player);
            return;
        }

        town.addBalance(amount);
        plugin.saveTowns();

        Chat.send(getString("withdraw-success")
                .replace("{money}", Utils.formatNumber(amount))
                .replace("{balance}", Utils.formatNumber(town.getBalance())), player);
        Chat.info("&b" + player.getDisplayName() + " &3withdrew &b" + Utils.formatNumber(amount) + " &3from &b" + town.getName() + " &3bank. Balance: &b" + Utils.formatNumber(town.getBalance()));
    }

    private void user(Player player, String[] args) {
        switch (args[1].toLowerCase()) {
            case "setadmin":
                setAdmin(player, args);
                break;
            case "deladmin":
                delAdmin(player, args);
                break;
            case "deposit":
                canDeposit(player, args);
                break;
            case "withdraw":
                canWithdraw(player, args);
                break;
            default:
                Chat.send(incorrectUsage("/town user <setadmin|deladmin|deposit|withdraw> <player>"), player);
                break;
        }
    }

    private void setAdmin(Player player, String[] args) {
        if (!towns.hasTown(player)) {
            Chat.send(getString("not-in-town"), player);
            return;
        }

        Town town = towns.getTown(player);
        if (!town.isLeader(player)) {
            Chat.send(getString("not-leader"), player);
            return;
        }

        String name = args[2];
        TownMember member = town.getMember(name);
        if (member == null) {
            Chat.send(getString("not-in-your-town"), player);
            return;
        }

        member.setAdmin(true);
        plugin.saveTowns();

        Chat.send(getString("admin-promote"), player);
        Player target = Bukkit.getPlayer(name);
        if (target != null) Chat.send(getString("admin-promote-target"), target);
    }

    private void delAdmin(Player player, String[] args) {
        if (!towns.hasTown(player)) {
            Chat.send(getString("not-in-town"), player);
            return;
        }

        Town town = towns.getTown(player);
        if (!town.isLeader(player)) {
            Chat.send(getString("not-leader"), player);
            return;
        }

        String name = args[2];
        TownMember member = town.getMember(name);
        if (member == null) {
            Chat.send(getString("not-in-your-town"), player);
            return;
        }

        member.setAdmin(false);
        plugin.saveTowns();

        Chat.send(getString("admin-demote"), player);
        Player target = Bukkit.getPlayer(name);
        if (target != null) Chat.send(getString("admin-demote-target"), target);
    }

    private void canDeposit(Player player, String[] args) {
        if (!towns.hasTown(player)) {
            Chat.send(getString("not-in-town"), player);
            return;
        }

        Town town = towns.getTown(player);
        if (!town.isLeader(player)) {
            Chat.send(getString("not-leader"), player);
            return;
        }

        String name = args[2];
        TownMember member = town.getMember(name);
        if (member == null) {
            Chat.send(getString("not-in-your-town"), player);
            return;
        }

        String bool = args[3];
        if (bool.equals("allow")) {
            member.setDeposit(true);
            plugin.saveTowns();

            Chat.send(getString("player-can-deposit").replace("{player}", name), player);
        } else if (bool.equals("deny")) {
            member.setDeposit(false);
            plugin.saveTowns();

            Chat.send(getString("player-cannot-deposit").replace("{player}", name), player);
        } else {
            Chat.send(incorrectUsage("/town user deposit <player> <allaw|deny>"), player);
        }
    }

    private void canWithdraw(Player player, String[] args) {
        if (!towns.hasTown(player)) {
            Chat.send(getString("not-in-town"), player);
            return;
        }

        Town town = towns.getTown(player);
        if (!town.isLeader(player)) {
            Chat.send(getString("not-leader"), player);
            return;
        }

        String name = args[2];
        TownMember member = town.getMember(name);
        if (member == null) {
            Chat.send(getString("not-in-your-town"), player);
            return;
        }

        String bool = args[3];
        if (bool.equals("allow")) {
            member.setWithdraw(true);
            plugin.saveTowns();

            Chat.send(getString("player-can-withdraw").replace("{player}", name), player);
        } else if (bool.equals("deny")) {
            member.setWithdraw(false);
            plugin.saveTowns();

            Chat.send(getString("player-cannot-withdraw").replace("{player}", name), player);
        } else {
            Chat.send(incorrectUsage("/town user withdraw <player> <allaw|deny>"), player);
        }
    }

    private void rename(Player player, String[] args) {
        if (args.length < 2) {
            Chat.send(incorrectUsage("/town rename <name>"), player);
            return;
        }

        if (!towns.hasTown(player)) {
            Chat.send(getString("not-in-town"), player);
            return;
        }

        Town town = towns.getTown(player);
        TownMember member = town.getMember(player);
        if (!member.isLeader()) {
            Chat.send(getString("not-leader"), player);
            return;
        }

        String name = args[1];
        if (towns.townExists(name)) {
            Chat.send(getString("name-not-available"), player);
            return;
        }

        town.setName(name);
        plugin.saveTowns();
        Chat.send(getString("town-rename").replace("{town}", town.getName()), player);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) return null;
        Player player = (Player) sender;

        if (!player.hasPermission("vanillatowns.town")) return null;

        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(TabCompleterUtil.getCompletions(args[0], Arrays.asList(
                    "baltop",
                    "create",
                    "delete",
                    "invite",
                    "join",
                    "kick",
                    "leave",
                    "give",
                    "home",
                    "setHome",
                    "delHome",
                    "balance",
                    "deposit",
                    "withdraw",
                    "user",
                    "rename"
            )));
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("invite")) {
                completions.addAll(TabCompleterUtil.getPlayers(args[1], player.hasPermission("vanillatowns.vanish")));
            } else if (args[0].equalsIgnoreCase("join")) {
                if (plugin.hasInvite(player)) {
                    List<String> tmp = new ArrayList<>();
                    plugin.getInvites(player).forEach(town -> tmp.add(town.getName()));
                    completions.addAll(TabCompleterUtil.getCompletions(args[1], tmp));
                }
            } else if (args[0].equalsIgnoreCase("kick")) {
                Town town = towns.getTown(player);
                if (town != null) {
                    if (town.isLeader(player) || town.isAdmin(player)) {
                        List<String> tmp = new ArrayList<>();
                        town.getAdmins().forEach(member -> tmp.add(member.getName()));
                        town.getMembers().forEach(member -> tmp.add(member.getName()));
                        completions.addAll(TabCompleterUtil.getCompletions(args[1], tmp));
                    }
                }
            } else if (args[0].equalsIgnoreCase("give")) {
                Town town = towns.getTown(player);
                if (town != null) {
                    if (town.isLeader(player) || town.isAdmin(player)) {
                        List<String> tmp = new ArrayList<>();
                        town.getAdmins().forEach(member -> tmp.add(member.getName()));
                        town.getMembers().forEach(member -> tmp.add(member.getName()));
                        completions.addAll(TabCompleterUtil.getCompletions(args[1], tmp));
                    }
                }
            } else if (args[0].equalsIgnoreCase("user")) {
                completions.addAll(TabCompleterUtil.getCompletions(args[1], Arrays.asList("setAdmin", "delAdmin", "deposit", "withdraw")));
            }
        } else if (args.length == 3) {
            if (args[1].equalsIgnoreCase("setadmin")) {
                Town town = towns.getTown(player);
                if (town != null) {
                    if (town.isLeader(player) || town.isAdmin(player)) {
                        List<String> tmp = new ArrayList<>();
                        town.getMembers().forEach(member -> tmp.add(member.getName()));
                        completions.addAll(TabCompleterUtil.getCompletions(args[1], tmp));
                    }
                }
            } else if (args[1].equalsIgnoreCase("deladmin")) {
                Town town = towns.getTown(player);
                if (town != null) {
                    if (town.isLeader(player) || town.isAdmin(player)) {
                        List<String> tmp = new ArrayList<>();
                        town.getAdmins().forEach(member -> tmp.add(member.getName()));
                        completions.addAll(TabCompleterUtil.getCompletions(args[1], tmp));
                    }
                }
            } else if (args[1].equals("deposit")) {
                Town town = towns.getTown(player);
                if (town != null) {
                    if (town.isLeader(player)) {
                        List<String> tmp = new ArrayList<>();
                        town.getMembers().forEach(member -> tmp.add(member.getName()));
                        completions.addAll(TabCompleterUtil.getCompletions(args[2], tmp));
                    }
                }
            } else if (args[1].equals("withdraw")) {
                Town town = towns.getTown(player);
                if (town != null) {
                    if (town.isLeader(player)) {
                        List<String> tmp = new ArrayList<>();
                        town.getMembers().forEach(member -> completions.add(member.getName()));
                        completions.addAll(TabCompleterUtil.getCompletions(args[2], tmp));
                    }
                }
            }
        } else if (args.length == 4) {
            if (args[0].equals("user") && args[1].equals("deposit")) {
                completions.addAll(TabCompleterUtil.getCompletions(args[2], Arrays.asList("allow", "deny")));
            } else if (args[0].equals("user") && args[1].equals("withdraw")) {
                completions.addAll(TabCompleterUtil.getCompletions(args[2], Arrays.asList("allow", "deny")));
            }
        }

        return completions;
    }
}
