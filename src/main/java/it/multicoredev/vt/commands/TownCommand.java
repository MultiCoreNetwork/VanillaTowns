package it.multicoredev.vt.commands;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import it.multicoredev.mbcore.spigot.Chat;
import it.multicoredev.mbcore.spigot.util.TabCompleterUtil;
import it.multicoredev.vt.Utils;
import it.multicoredev.vt.VanillaTowns;
import it.multicoredev.vt.storage.Config;
import it.multicoredev.vt.storage.towns.Town;
import it.multicoredev.vt.storage.towns.TownHome;
import it.multicoredev.vt.storage.towns.TownMember;
import it.multicoredev.vt.storage.towns.Towns;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Copyright © 2020 - 2021 by Lorenzo Magni
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
    private final Config config;
    private final Towns towns;
    public static final ConcurrentMap<UUID, Integer> invites = new ConcurrentHashMap<>();

    public TownCommand(VanillaTowns plugin, Config config, Towns towns) {
        this.plugin = plugin;
        this.config = config;
        this.towns = towns;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            Chat.send(config.strings.notPlayer, sender);
            return true;
        }

        Player player = (Player) sender;

        if (!hasPermission(player, "vanillatowns.town")) {
            Chat.send(config.strings.insufficientPerms, player);
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
            case "create":
                if (args.length < 2) help(player);
                else create(player, args[1]);
                break;
            case "invite":
                if (args.length < 2) help(player);
                else invite(player, args[1]);
                break;
            case "join":
                join(player);
                break;
            case "leave":
                leave(player);
                break;
            case "kick":
                if (args.length < 2) help(player);
                else kick(player, args[1]);
                break;
            case "rename":
                if (args.length < 2) help(player);
                else rename(player, args[1]);
                break;
            case "give":
                if (args.length < 2) help(player);
                else give(player, args[1]);
                break;
            case "delete":
                delete(player);
                break;
            case "balance":
                balance(player);
                break;
            case "deposit":
                if (args.length < 2) help(player);
                else deposit(player, args[1]);
                break;
            case "withdraw":
                if (args.length < 2) help(player);
                else withdraw(player, args[1]);
                break;
            case "baltop":
                baltop(player);
                break;
            case "sethome":
                setHome(player);
                break;
            case "home":
                home(player);
                break;
            case "delhome":
                delHome(player);
                break;
            case "user":
                user(player, args);
                break;
            default:
                town(player, args[0]);
                break;
        }

        return true;
    }

    private void town(Player player, String name) {
        if (!hasPermission(player, "vanillatowns.info")) {
            help(player);
            return;
        }

        Town town;
        boolean self;
        if (name == null) {
            town = towns.getTown(player, null);
            self = true;

            if (town == null) {
                help(player);
                return;
            }
        } else {
            town = towns.getTown(name, null);
            if (town == null) {
                Chat.send(config.strings.townNotFound, player);
                return;
            }

            self = town.isMember(player);
        }

        if (!self && !hasPermission(player, "vanillatowns.info.other")) {
            Chat.send(config.strings.insufficientPerms, player);
            return;
        }

        TownHome home = town.getHome();
        String homeStr = home == null ? config.strings.notSet : config.strings.homeFormat
                .replace("{world}", home.getWorld())
                .replace("{x}", String.valueOf(home.getX()))
                .replace("{y}", String.valueOf(home.getY()))
                .replace("{z}", String.valueOf(home.getZ()));

        if (self) {
            for (String str : config.strings.townInfoSelf) {
                Chat.send(str.replace("{town}", town.getName())
                                .replace("{balance}", Utils.formatNumber(town.getBalance()))
                                .replace("{leader}", town.getLeader().getName())
                                .replace("{admins}", town.getAdminNames())
                                .replace("{members}", town.getSimpleMembersNames())
                                .replace("{home}", homeStr)
                        , player);
            }
        } else {
            if (hasStaffPermission(player, "vanillatowns.staff.info")) {
                for (String str : config.strings.townInfoStaff) {
                    Chat.send(str.replace("{town}", town.getName())
                                    .replace("{balance}", Utils.formatNumber(town.getBalance()))
                                    .replace("{leader}", town.getLeader().getName())
                                    .replace("{admins}", town.getAdminNames())
                                    .replace("{members}", town.getSimpleMembersNames())
                                    .replace("{home}", homeStr)
                            , player);
                }
            } else {
                for (String str : config.strings.townInfoOther) {
                    Chat.send(str.replace("{town}", town.getName())
                                    .replace("{balance}", Utils.formatNumber(town.getBalance()))
                                    .replace("{leader}", town.getLeader().getName())
                                    .replace("{admins}", town.getAdminNames())
                                    .replace("{members}", town.getSimpleMembersNames())
                                    .replace("{home}", homeStr)
                            , player);
                }
            }
        }
    }

    private void help(Player player) {
        if (!hasPermission(player, "vanillatowns.help")) {
            Chat.send(config.strings.insufficientPerms, player);
            return;
        }

        for (String str : config.strings.helpMessage) {
            Chat.send(str, player);
        }
    }

    private void create(Player player, String name) {
        if (!hasPermission(player, "vanillatowns.create")) {
            Chat.send(config.strings.insufficientPerms, player);
            return;
        }

        if (towns.isInTown(player)) {
            Chat.send(config.strings.alreadyInTown, player);
            return;
        }

        name = Chat.getDiscolored(name);
        if (towns.townExists(name)) {
            Chat.send(config.strings.nameNotAvailable, player);
            return;
        }

        Town town = new Town(towns.getFirstId(), name, player);
        towns.addTown(town);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, plugin::saveTowns);

        Chat.send(config.strings.townCreated.replace("{town}", name), player);

        if (config.broadcastTownCreation) {
            String bc = config.strings.townCreatedBC.replace("{player}", player.getDisplayName()).replace("{town}", name);
            broadcast(bc, "vanillatowns.broadcast", player);
        }

        if (config.logTowns) Chat.info("&e" + player.getDisplayName() + " &bcreated the town &e" + name);
    }

    private void invite(Player player, String name) {
        if (!hasPermission(player, "vanillatowns.invite")) {
            Chat.send(config.strings.insufficientPerms, player);
            return;
        }

        if (!towns.isInTown(player)) {
            Chat.send(config.strings.notInTown, player);
            return;
        }

        Town town = towns.getTown(player, null);
        if (town == null) {
            Chat.send(config.strings.notInTown, player);
            return;
        }

        if (!town.isAdmin(player) && !town.isLeader(player)) {
            Chat.send(config.strings.notAdmin, player);
            return;
        }

        Player target = Bukkit.getPlayer(name);
        if (target == null) {
            Chat.send(config.strings.playerNotFound, player);
            return;
        }

        if (!hasPermission(target, "vanillatowns.join")) {
            Chat.send(config.strings.cannotInvite, player);
            return;
        }

        if (town.isMember(target)) {
            Chat.send(config.strings.alreadyInYourTown, player);
            return;
        }

        invites.put(target.getUniqueId(), town.getId());

        Chat.send(config.strings.playerInviteSent.replace("{player}", target.getDisplayName()), player);
        Chat.send(config.strings.playerInviteReceived.replace("{town}", town.getName()).replace("{player}", player.getDisplayName()), target);

        if (config.logTowns)
            Chat.info("&e" + player.getDisplayName() + "&b invited &e" + target.getDisplayName() + "&b in the town &e" + town.getName());
    }

    private void join(Player player) {
        if (!hasPermission(player, "vanillatowns.join")) {
            Chat.send(config.strings.insufficientPerms, player);
            return;
        }

        if (!invites.containsKey(player.getUniqueId())) {
            Chat.send(config.strings.noInvites, player);
            return;
        }

        if (towns.isInTown(player)) {
            Chat.send(config.strings.alreadyInTown, player);
            return;
        }

        Town town = towns.getTown(invites.get(player.getUniqueId()), null);
        if (town == null) {
            Chat.send(config.strings.noInvites, player);
            return;
        }

        town.addMember(new TownMember(player, false));
        plugin.saveTowns();
        invites.remove(player.getUniqueId());

        Chat.send(config.strings.playerJoin.replace("{town}", town.getName()), player);

        String msg = config.strings.playerJoinMembers.replace("{player}", player.getDisplayName());
        for (Player member : town.getOnlineMembers()) Chat.send(msg, member);

        if (config.logTowns) Chat.info("&e" + player.getDisplayName() + "&b joined the town &e" + town.getName());
    }

    private void leave(Player player) {
        if (!hasPermission(player, "vanillatowns.join")) {
            Chat.send(config.strings.insufficientPerms, player);
            return;
        }

        if (!towns.isInTown(player)) {
            Chat.send(config.strings.notInTown, player);
            return;
        }

        Town town = towns.getTown(player, null);
        if (town == null) {
            Chat.send(config.strings.notInTown, player);
            return;
        }

        if (town.isLeader(player)) {
            Chat.send(config.strings.leaveDenied, player);
            return;
        }

        town.removeMember(player.getUniqueId());
        plugin.saveTowns();

        Chat.send(config.strings.playerLeft.replace("{town}", town.getName()), player);

        String msg = config.strings.playerLeftMembers.replace("{player}", player.getDisplayName());
        for (Player member : town.getOnlineMembers()) Chat.send(msg, member);

        Chat.info("&e" + player.getDisplayName() + "&b left the town &e" + town.getName());
    }

    private void kick(Player player, String name) {
        if (!hasPermission(player, "vanillatowns.kick")) {
            Chat.send(config.strings.insufficientPerms, player);
            return;
        }

        if (!towns.isInTown(player)) {
            Chat.send(config.strings.notInTown, player);
            return;
        }

        Town town = towns.getTown(player, null);
        if (town == null) {
            Chat.send(config.strings.notInTown, player);
            return;
        }

        if (!town.isAdmin(player) && !town.isLeader(player)) {
            Chat.send(config.strings.notAdmin, player);
            return;
        }

        if (!town.isMember(name)) {
            Chat.send(config.strings.notInYourTown, player);
            return;
        }

        if (town.getMember(name).isLeader()) {
            Chat.send(config.strings.notToTheLeader, player);
            return;
        }

        if (town.getMember(name).isAdmin() && !town.isLeader(player)) {
            Chat.send(config.strings.notLeader, player);
            return;
        }

        town.removeMember(name);
        plugin.saveTowns();

        Player target = Bukkit.getPlayer(name);
        if (target != null) Chat.send(config.strings.playerKicked.replace("{town}", town.getName()), target);

        String msg = config.strings.playerKickedMembers.replace("{player}", target != null ? target.getDisplayName() : name);
        for (Player member : town.getOnlineMembers()) Chat.send(msg, member);

        Chat.info("&e" + player.getDisplayName() + "&b kicked &e" + (target != null ? target.getDisplayName() : name) + "&b from town &e" + town.getName());
    }

    private void rename(Player player, String name) {
        if (!hasPermission(player, "vanillatowns.rename")) {
            Chat.send(config.strings.insufficientPerms, player);
            return;
        }

        if (!towns.isInTown(player)) {
            Chat.send(config.strings.notInTown, player);
            return;
        }

        Town town = towns.getTown(player, null);
        if (town == null) {
            Chat.send(config.strings.notInTown, player);
            return;
        }

        if (!town.isLeader(player)) {
            Chat.send(config.strings.notLeader, player);
            return;
        }

        name = Chat.getDiscolored(name);
        if (towns.townExists(name)) {
            Chat.send(config.strings.nameNotAvailable, player);
            return;
        }

        String old = town.getName();
        town.setName(name);
        plugin.saveTowns();

        if (config.broadcastTownRename) {
            String bc = config.strings.townRenamedBC
                    .replace("{player}", player.getDisplayName())
                    .replace("{town_old}", old)
                    .replace("{town_new}", name);
            broadcast(bc, "vanillatowns.broadcast", player);
        }

        Chat.send(config.strings.townRenamed.replace("{town}", name), player);

        if (config.logTowns)
            Chat.info("&e" + player.getDisplayName() + " &brenamed the town &e" + old + "&b to &e" + name);
    }

    private void give(Player player, String name) {
        if (!hasPermission(player, "vanillatowns.give")) {
            Chat.send(config.strings.insufficientPerms, player);
            return;
        }

        if (!towns.isInTown(player)) {
            Chat.send(config.strings.notInTown, player);
            return;
        }

        Town town = towns.getTown(player, null);
        if (town == null) {
            Chat.send(config.strings.notInTown, player);
            return;
        }

        if (!town.isLeader(player)) {
            Chat.send(config.strings.notLeader, player);
            return;
        }

        if (!town.isMember(name)) {
            Chat.send(config.strings.notInYourTown, player);
            return;
        }

        town.getMember(player.getUniqueId()).setLeader(false);
        town.getMember(name).setLeader(true);
        plugin.saveTowns();

        String msg = config.strings.leaderTransfer.replace("{player}", name);
        for (Player member : town.getOnlineMembers()) Chat.send(msg, member);

        if (config.logTowns)
            Chat.info("&e" + player.getDisplayName() + "&b gave &e" + town.getName() + "&b leader role to &e" + name);
    }

    private void delete(Player player) {
        if (!hasPermission(player, "vanillatowns.create")) {
            Chat.send(config.strings.insufficientPerms, player);
            return;
        }

        if (!towns.isInTown(player)) {
            Chat.send(config.strings.notInTown, player);
            return;
        }

        Town town = towns.getTown(player, null);
        if (town == null) {
            Chat.send(config.strings.notInTown, player);
            return;
        }

        if (!town.isLeader(player)) {
            Chat.send(config.strings.notLeader, player);
            return;
        }

        towns.removeTown(town);
        plugin.saveTowns();

        if (town.getBalance() > 0) {
            Utils.giveMoney(player, town.getBalance());
            Chat.send(config.strings.balanceChargeback.replace("{money}", Utils.formatNumber(town.getBalance())), player);
        }

        if (config.broadcastTownDeletion) {
            String bc = config.strings.townDeletedBC.replace("{player}", player.getDisplayName()).replace("{town}", town.getName());
            broadcast(bc, "vanillatowns.broadcast", player);
        }

        Chat.send(config.strings.townDeleted, player);

        if (config.logTowns) Chat.info("&e" + player.getDisplayName() + " &bdeleted the town &e" + town.getName());
    }

    private void balance(Player player) {
        if (!hasPermission(player, "vanillatowns.balance")) {
            Chat.send(config.strings.insufficientPerms, player);
            return;
        }

        if (!towns.isInTown(player)) {
            Chat.send(config.strings.notInTown, player);
            return;
        }

        Town town = towns.getTown(player, null);
        if (town == null) {
            Chat.send(config.strings.notInTown, player);
            return;
        }

        Chat.send(config.strings.townBalance.replace("{money}", Utils.formatNumber(town.getBalance())), player);
    }

    private void deposit(Player player, String a) {
        if (!hasPermission(player, "vanillatowns.deposit")) {
            Chat.send(config.strings.insufficientPerms, player);
            return;
        }

        if (!towns.isInTown(player)) {
            Chat.send(config.strings.notInTown, player);
            return;
        }

        Town town = towns.getTown(player, null);
        if (town == null) {
            Chat.send(config.strings.notInTown, player);
            return;
        }

        if (!town.canDeposit(player)) {
            Chat.send(config.strings.cannotDeposit, player);
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(a);
        } catch (NumberFormatException ignored) {
            help(player);
            return;
        }

        if (amount < 0) amount = Math.abs(amount);

        if (!Utils.hasEnoughMoney(player, amount)) {
            Chat.send(config.strings.insufficientMoney, player);
            return;
        }

        if (!Utils.withdrawMoney(player, amount)) {
            Chat.send(config.strings.transactionError, player);
            return;
        }

        town.addBalance(amount);
        plugin.saveTowns();

        Chat.send(config.strings.depositSuccess.replace("{money}", Utils.formatNumber(amount)), player);
        Chat.send(config.strings.townBalance.replace("{money}", Utils.formatNumber(town.getBalance())), player);

        if (config.logTowns)
            Chat.info("&e" + player.getDisplayName() + "&b deposited &e" + Utils.formatNumber(amount) + "&b to &e" + town.getName() + "&b bank. Balance: &e" + Utils.formatNumber(town.getBalance()));
    }

    private void withdraw(Player player, String a) {
        if (!hasPermission(player, "vanillatowns.withdraw")) {
            Chat.send(config.strings.insufficientPerms, player);
            return;
        }

        if (!towns.isInTown(player)) {
            Chat.send(config.strings.notInTown, player);
            return;
        }

        Town town = towns.getTown(player, null);
        if (town == null) {
            Chat.send(config.strings.notInTown, player);
            return;
        }

        if (!town.canWithdraw(player)) {
            Chat.send(config.strings.cannotWithdraw, player);
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(a);
        } catch (NumberFormatException ignored) {
            help(player);
            return;
        }

        if (amount < 0) amount = Math.abs(amount);

        if (town.getBalance() < amount) {
            Chat.send(config.strings.insufficientTownMoney, player);
            return;
        }

        if (!Utils.giveMoney(player, amount)) {
            Chat.send(config.strings.transactionError, player);
            return;
        }

        town.addBalance(-amount);
        plugin.saveTowns();

        Chat.send(config.strings.withdrawSuccess.replace("{money}", Utils.formatNumber(amount)), player);
        Chat.send(config.strings.townBalance.replace("{money}", Utils.formatNumber(town.getBalance())), player);

        if (config.logTowns)
            Chat.info("&e" + player.getDisplayName() + "&b withdrew &e" + Utils.formatNumber(amount) + "&b to &e" + town.getName() + "&b bank. Balance: &e" + Utils.formatNumber(town.getBalance()));
    }

    private void baltop(Player player) {
        if (!hasPermission(player, "vanillatowns.baltop")) {
            Chat.send(config.strings.insufficientPerms, player);
            return;
        }

        List<Town> list = towns.getTowns();
        Town playerTown = towns.getTown(player, null);
        boolean isTownInTop = false;

        Chat.send(config.strings.baltopHead, player);

        for (int i = 0; i < 10; i++) {
            Town town = list.get(i);
            if (town.equals(playerTown)) isTownInTop = true;

            Chat.send(config.strings.baltop
                    .replace("{position}", String.valueOf(i + 1))
                    .replace("{town}", town.getName())
                    .replace("{balance}", Utils.formatNumber(town.getBalance())), player);
        }

        if (playerTown != null && !isTownInTop) {
            Chat.send(config.strings.baltop
                    .replace("{position}", String.valueOf(list.indexOf(playerTown) + 1))
                    .replace("{town}", playerTown.getName())
                    .replace("{balance}", Utils.formatNumber(playerTown.getBalance())), player);
        }

        Chat.send(config.strings.baltopTail, player);
    }

    private void setHome(Player player) {
        if (!hasPermission(player, "vanillatowns.home.edit")) {
            Chat.send(config.strings.insufficientPerms, player);
            return;
        }

        if (!towns.isInTown(player)) {
            Chat.send(config.strings.notInTown, player);
            return;
        }

        Town town = towns.getTown(player, null);
        if (town == null) {
            Chat.send(config.strings.notInTown, player);
            return;
        }

        if (!town.isAdmin(player) && !town.isLeader(player)) {
            Chat.send(config.strings.notAdmin, player);
            return;
        }

        if (isBlacklistedDimension(player.getWorld())) {
            Chat.send(config.strings.blacklistedDim, player);
            return;
        }

        town.setHome(new TownHome(player.getLocation()));
        plugin.saveTowns();

        Chat.send(config.strings.homeSet, player);

        if (config.logTowns)
            Chat.info("&e" + player.getDisplayName() + "&b set the home of the town &e" + town.getName() + "&b to &e" + player.getLocation().toString());
    }

    private void home(Player player) {
        if (!hasPermission(player, "vanillatowns.home")) {
            Chat.send(config.strings.insufficientPerms, player);
            return;
        }

        if (!towns.isInTown(player)) {
            Chat.send(config.strings.notInTown, player);
            return;
        }

        Town town = towns.getTown(player, null);
        if (town == null) {
            Chat.send(config.strings.notInTown, player);
            return;
        }

        boolean isJailed = false;
        int time = config.teleportTimer;

        if (Bukkit.getPluginManager().isPluginEnabled("EssentialsX")) {
            Essentials essentials = (Essentials) Bukkit.getPluginManager().getPlugin("EssentialsX");

            if (essentials != null) {
                User user = essentials.getUser(player.getUniqueId());
                if (user != null) isJailed = user.isJailed();
            }
        }

        if (isJailed) {
            Chat.send(config.strings.jailed, player);
            return;
        }

        if (town.getHome() == null) {
            Chat.send(config.strings.noHome, player);
            return;
        }

        Location home = town.getHome().getLocation();

        if (time <= 0 || hasStaffPermission(player, "vanillatowns.instanttp")) {
            player.teleport(home);
            return;
        }

        Chat.send(config.strings.teleportCountdown.replace("{time}", String.valueOf(time)), player);

        Bukkit.getScheduler().runTaskLater(plugin, () -> player.teleport(home), time * 20L);
    }

    private void delHome(Player player) {
        if (!hasPermission(player, "vanillatowns.home.edit")) {
            Chat.send(config.strings.insufficientPerms, player);
            return;
        }

        if (!towns.isInTown(player)) {
            Chat.send(config.strings.notInTown, player);
            return;
        }

        Town town = towns.getTown(player, null);
        if (town == null) {
            Chat.send(config.strings.notInTown, player);
            return;
        }

        if (!town.isAdmin(player) && !town.isLeader(player)) {
            Chat.send(config.strings.notAdmin, player);
            return;
        }

        town.setHome(null);
        plugin.saveTowns();

        Chat.send(config.strings.homeRemoved, player);

        if (config.logTowns)
            Chat.info("&e" + player.getDisplayName() + "&b removed the home of the town &e" + town.getName());
    }

    private void user(Player player, String[] args) {
        switch (args[1].toLowerCase()) {
            case "setadmin":
                if (args.length < 3) help(player);
                else setAdmin(player, args[2]);
                break;
            case "deladmin":
                if (args.length < 3) help(player);
                else delAdmin(player, args[2]);
                break;
            case "deposit":
                canDeposit(player, args[2], args[3]);
                break;
            case "withdraw":
                canWithdraw(player, args[2], args[3]);
                break;
            default:
                help(player);
                break;
        }
    }

    private void setAdmin(Player player, String name) {
        if (!towns.isInTown(player)) {
            Chat.send(config.strings.notInTown, player);
            return;
        }

        Town town = towns.getTown(player, null);
        if (town == null) {
            Chat.send(config.strings.notInTown, player);
            return;
        }

        if (!town.isLeader(player)) {
            Chat.send(config.strings.notLeader, player);
            return;
        }

        if (!town.isMember(name)) {
            Chat.send(config.strings.notInYourTown, player);
            return;
        }

        town.getMember(name).setAdmin(true);
        plugin.saveTowns();

        Chat.send(config.strings.adminPromoted.replace("{player}", name), player);

        Player target = Bukkit.getPlayer(name);
        if (target != null) Chat.send(config.strings.adminPromotedTarget, target);

        if (config.logTowns) Chat.info("&e" + player.getDisplayName() + "&b promoted &e" + name + "&b to town admin");
    }

    private void delAdmin(Player player, String name) {
        if (!towns.isInTown(player)) {
            Chat.send(config.strings.notInTown, player);
            return;
        }

        Town town = towns.getTown(player, null);
        if (town == null) {
            Chat.send(config.strings.notInTown, player);
            return;
        }

        if (!town.isLeader(player)) {
            Chat.send(config.strings.notLeader, player);
            return;
        }

        if (!town.isMember(name)) {
            Chat.send(config.strings.notInYourTown, player);
            return;
        }

        town.getMember(name).setAdmin(false);
        plugin.saveTowns();

        Chat.send(config.strings.adminDemoted, player);

        Player target = Bukkit.getPlayer(name);
        if (target != null) Chat.send(config.strings.adminDemotedTarget, target);

        if (config.logTowns) Chat.info("&e" + player.getDisplayName() + "&b demoted &e" + name + "&b to town admin");
    }

    private void canDeposit(Player player, String name, String bool) {
        if (!towns.isInTown(player)) {
            Chat.send(config.strings.notInTown, player);
            return;
        }

        Town town = towns.getTown(player, null);
        if (town == null) {
            Chat.send(config.strings.notInTown, player);
            return;
        }

        if (!town.isLeader(player)) {
            Chat.send(config.strings.notLeader, player);
            return;
        }

        if (!town.isMember(name)) {
            Chat.send(config.strings.notInYourTown, player);
            return;
        }

        if (bool.equalsIgnoreCase("allow") || bool.equalsIgnoreCase("true")) {
            town.getMember(name).setDeposit(true);
            plugin.saveTowns();

            Chat.send(config.strings.allowPlayerDeposit.replace("{player}", name), player);

            Player target = Bukkit.getPlayer(name);
            if (target != null) Chat.send(config.strings.allowPlayerDepositTarget, target);
        } else if (bool.equalsIgnoreCase("deny") || bool.equalsIgnoreCase("false")) {
            town.getMember(name).setDeposit(false);
            plugin.saveTowns();

            Chat.send(config.strings.allowPlayerDeposit.replace("{player}", name), player);

            Player target = Bukkit.getPlayer(name);
            if (target != null) Chat.send(config.strings.allowPlayerDepositTarget, target);
        } else {
            help(player);
        }
    }

    private void canWithdraw(Player player, String name, String bool) {
        if (!towns.isInTown(player)) {
            Chat.send(config.strings.notInTown, player);
            return;
        }

        Town town = towns.getTown(player, null);
        if (town == null) {
            Chat.send(config.strings.notInTown, player);
            return;
        }

        if (!town.isLeader(player)) {
            Chat.send(config.strings.notLeader, player);
            return;
        }

        if (!town.isMember(name)) {
            Chat.send(config.strings.notInYourTown, player);
            return;
        }

        if (bool.equalsIgnoreCase("allow") || bool.equalsIgnoreCase("true")) {
            town.getMember(name).setWithdraw(true);
            plugin.saveTowns();

            Chat.send(config.strings.allowPlayerWithdraw.replace("{player}", name), player);

            Player target = Bukkit.getPlayer(name);
            if (target != null) Chat.send(config.strings.allowPlayerWithdrawTarget, target);
        } else if (bool.equalsIgnoreCase("deny") || bool.equalsIgnoreCase("false")) {
            town.getMember(name).setWithdraw(false);
            plugin.saveTowns();

            Chat.send(config.strings.allowPlayerWithdraw.replace("{player}", name), player);

            Player target = Bukkit.getPlayer(name);
            if (target != null) Chat.send(config.strings.allowPlayerWithdrawTarget, target);
        } else {
            help(player);
        }
    }

    private boolean hasPermission(Player player, String perm) {
        return player.hasPermission(perm) || player.hasPermission("vanillatowns.player") || player.hasPermission("vanillatowns.staff");
    }

    private boolean hasStaffPermission(Player player, String perm) {
        return player.hasPermission(perm) || player.hasPermission("vanillatowns.staff");
    }

    private void broadcast(String msg, String perm, Player exception) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player == exception) continue;
            if (!player.hasPermission(perm)) continue;
            Chat.send(msg, player);
        }
    }

    private boolean isBlacklistedDimension(World world) {
        for (String dim : config.dimBlacklist) {
            if (dim.equalsIgnoreCase(world.getName())) return true;
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) return null;
        Player player = (Player) sender;

        if (!hasPermission(player, "vanillatowns.player")) return null;

        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> tmp = new ArrayList<>();

            if (hasPermission(player, "vanillatowns.baltop")) tmp.add("baltop");
            if (hasPermission(player, "vanillatowns.create")) {
                tmp.add("create");
                tmp.add("delete");
            }
            if (hasPermission(player, "vanillatowns.invite")) tmp.add("invite");
            if (hasPermission(player, "vanillatowns.join")) {
                tmp.add("join");
                tmp.add("leave");
            }
            if (hasPermission(player, "vanillatowns.kick")) tmp.add("kick");
            if (hasPermission(player, "vanillatowns.give")) tmp.add("give");
            if (hasPermission(player, "vanillatowns.home")) tmp.add("home");
            if (hasPermission(player, "vanillatowns.home.edit")) {
                tmp.add("setHome");
                tmp.add("delHome");
            }
            if (hasPermission(player, "vanillatowns.balance")) tmp.add("balance");
            if (hasPermission(player, "vanillatowns.deposit")) tmp.add("deposit");
            if (hasPermission(player, "vanillatowns.withdraw")) tmp.add("withdraw");
            if (hasPermission(player, "vanillatowns.rename")) tmp.add("rename");

            tmp.add("user");
            completions = TabCompleterUtil.getCompletions(args[0], tmp);
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("invite")) {
                completions = new ArrayList<>(TabCompleterUtil.getPlayers(args[1], player.hasPermission("vanillatowns.vanish")));
            } else if (args[0].equalsIgnoreCase("kick")) {
                Town town = towns.getTown(player, null);
                if (town != null) {
                    if (town.isLeader(player) || town.isAdmin(player)) {
                        List<String> tmp = new ArrayList<>();
                        town.getAdmins().forEach(member -> tmp.add(member.getName()));
                        town.getMembers().forEach(member -> tmp.add(member.getName()));
                        completions = new ArrayList<>(TabCompleterUtil.getCompletions(args[1], tmp));
                    }
                }
            } else if (args[0].equalsIgnoreCase("give")) {
                Town town = towns.getTown(player, null);
                if (town != null) {
                    if (town.isLeader(player) || town.isAdmin(player)) {
                        List<String> tmp = new ArrayList<>();
                        town.getAdmins().forEach(member -> tmp.add(member.getName()));
                        town.getMembers().forEach(member -> tmp.add(member.getName()));
                        completions = new ArrayList<>(TabCompleterUtil.getCompletions(args[1], tmp));
                    }
                }
            } else if (args[0].equalsIgnoreCase("user")) {
                completions = new ArrayList<>(TabCompleterUtil.getCompletions(args[1], Arrays.asList("setAdmin", "delAdmin", "deposit", "withdraw")));
            }
        } else if (args.length == 3) {
            if (args[1].equalsIgnoreCase("setadmin")) {
                Town town = towns.getTown(player, null);
                if (town != null) {
                    if (town.isLeader(player) || town.isAdmin(player)) {
                        List<String> tmp = new ArrayList<>();
                        town.getMembers().forEach(member -> tmp.add(member.getName()));
                        completions = new ArrayList<>(TabCompleterUtil.getCompletions(args[1], tmp));
                    }
                }
            } else if (args[1].equalsIgnoreCase("deladmin")) {
                Town town = towns.getTown(player, null);
                if (town != null) {
                    if (town.isLeader(player) || town.isAdmin(player)) {
                        List<String> tmp = new ArrayList<>();
                        town.getAdmins().forEach(member -> tmp.add(member.getName()));
                        completions = new ArrayList<>(TabCompleterUtil.getCompletions(args[1], tmp));
                    }
                }
            } else if (args[1].equals("deposit")) {
                Town town = towns.getTown(player, null);
                if (town != null) {
                    if (town.isLeader(player)) {
                        List<String> tmp = new ArrayList<>();
                        town.getMembers().forEach(member -> tmp.add(member.getName()));
                        completions = new ArrayList<>(TabCompleterUtil.getCompletions(args[2], tmp));
                    }
                }
            } else if (args[1].equals("withdraw")) {
                Town town = towns.getTown(player, null);
                if (town != null) {
                    if (town.isLeader(player)) {
                        List<String> tmp = new ArrayList<>();
                        town.getMembers().forEach(member -> tmp.add(member.getName()));
                        completions = new ArrayList<>(TabCompleterUtil.getCompletions(args[2], tmp));
                    }
                }
            }
        } else if (args.length == 4) {
            if (args[0].equals("user") && args[1].equals("deposit")) {
                completions = new ArrayList<>(TabCompleterUtil.getCompletions(args[2], Arrays.asList("allow", "deny")));
            } else if (args[0].equals("user") && args[1].equals("withdraw")) {
                completions = new ArrayList<>(TabCompleterUtil.getCompletions(args[2], Arrays.asList("allow", "deny")));
            }
        }

        return completions;
    }
}
