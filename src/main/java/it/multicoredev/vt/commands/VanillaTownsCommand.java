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
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

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
public class VanillaTownsCommand implements CommandExecutor, TabCompleter {
    private final VanillaTowns plugin;
    private final Config config;
    private final Towns towns;

    public VanillaTownsCommand(VanillaTowns plugin, Config config, Towns towns) {
        this.plugin = plugin;
        this.config = config;
        this.towns = towns;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("vanillatowns.staff")) {
            Chat.send(config.strings.insufficientPerms, sender);
            return true;
        }

        if (args.length < 1) {
            help(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                reload(sender);
                break;
            case "invite":
                if (args.length < 3) help(sender);
                else invite(sender, args[1], args[2]);
                break;
            case "join":
                if (args.length < 3) help(sender);
                else join(sender, args[1], args[2]);
                break;
            case "kick":
                if (args.length < 3) help(sender);
                else kick(sender, args[1], args[2]);
                break;
            case "rename":
                if (args.length < 3) help(sender);
                else rename(sender, args[1], args[2]);
                break;
            case "delete":
                if (args.length < 2) help(sender);
                else delete(sender, args[1]);
                break;
            case "setleader":
                if (args.length < 3) help(sender);
                else setLeader(sender, args[1], args[2]);
                break;
            case "setadmin":
                if (args.length < 3) help(sender);
                else setAdmin(sender, args[1], args[2]);
                break;
            case "setmember":
                if (args.length < 3) help(sender);
                else setMember(sender, args[1], args[2]);
                break;
            case "sethome":
                if (args.length < 2) help(sender);
                else setHome(sender, args[1]);
                break;
            case "home":
                if (args.length < 2) help(sender);
                else home(sender, args[1]);
                break;
            case "delhome":
                if (args.length < 2) help(sender);
                else delHome(sender, args[1]);
                break;
            default:
                help(sender);
                break;
        }

        return true;
    }

    private void help(CommandSender sender) {
        if (!hasStaffPermission(sender, "vanillatowns.help")) {
            Chat.send(config.strings.insufficientPerms, sender);
            return;
        }

        for (String str : config.strings.staffHelpMessage) {
            Chat.send(str, sender);
        }
    }

    private void reload(CommandSender sender) {
        if (!hasStaffPermission(sender, "vanillatowns.reload")) {
            Chat.send(config.strings.insufficientPerms, sender);
            return;
        }

        long millis = System.currentTimeMillis();
        plugin.onDisable();
        plugin.onEnable();

        Chat.send(config.strings.pluginReloaded.replace("{time}", String.valueOf(System.currentTimeMillis() - millis)), sender);
    }

    private void invite(CommandSender sender, String name, String townName) {
        if (!hasStaffPermission(sender, "vanillatowns.staff.invite")) {
            Chat.send(config.strings.insufficientPerms, sender);
            return;
        }

        Town town = towns.getTown(townName, null);
        if (town == null) {
            Chat.send(config.strings.townNotFound, sender);
            return;
        }

        Player target = Bukkit.getPlayer(name);
        if (target == null) {
            Chat.send(config.strings.playerNotFound, sender);
            return;
        }

        TownCommand.invites.put(target.getUniqueId(), town.getId());

        String senderName = sender instanceof Player ? ((Player) sender).getDisplayName() : sender.getName();
        Chat.send(config.strings.playerInviteSent.replace("{player}", target.getDisplayName()), target);
        Chat.send(config.strings.playerInviteReceived.replace("{town}", town.getName()).replace("{player}", senderName), target);

        if (config.logTowns)
            Chat.info("&e" + senderName + "&b invited &e" + target.getDisplayName() + "&b in the town &e" + town.getName());
    }

    private void join(CommandSender sender, String name, String townName) {
        if (!hasStaffPermission(sender, "vanillatowns.staff.join")) {
            Chat.send(config.strings.insufficientPerms, sender);
            return;
        }

        Player target = Bukkit.getPlayer(name);
        if (target == null) {
            Chat.send(config.strings.playerNotFound, sender);
            return;
        }

        if (towns.isInTown(target)) {
            Chat.send(config.strings.alreadyInTownStaff, sender);
            return;
        }

        Town town = towns.getTown(townName, null);
        if (town == null) {
            Chat.send(config.strings.townNotFound, sender);
            return;
        }

        town.addMember(new TownMember(target, false));
        plugin.saveTowns();

        Chat.send(config.strings.playerAddedToTown.replace("{player}", target.getDisplayName()).replace("{town}", town.getName()), sender);
        Chat.send(config.strings.playerJoin.replace("{town}", town.getName()), target);

        String msg = config.strings.playerJoinMembers.replace("{player}", target.getDisplayName());
        for (Player member : town.getOnlineMembers()) Chat.send(msg, member);

        if (config.logTowns)
            Chat.info("&e" + (sender instanceof Player ? ((Player) sender).getDisplayName() : sender.getName()) + "&b added &e" + target.getDisplayName() + "&b to the town &e" + town.getName());
    }

    private void kick(CommandSender sender, String name, String townName) {
        if (!hasStaffPermission(sender, "vanillatowns.staff.kick")) {
            Chat.send(config.strings.insufficientPerms, sender);
            return;
        }

        Town town = towns.getTown(townName, null);
        if (town == null) {
            Chat.send(config.strings.townNotFound, sender);
            return;
        }

        if (!town.isMember(name)) {
            Chat.send(config.strings.notInThatTown, sender);
            return;
        }

        if (town.isLeader(name)) {
            Chat.send(config.strings.notToTheLeader, sender);
            return;
        }

        town.removeMember(name);
        plugin.saveTowns();

        Player target = Bukkit.getPlayer(name);
        if (target != null) Chat.send(config.strings.playerKicked.replace("{town}", town.getName()), target);
        Chat.send(config.strings.playerKickedStaff
                .replace("{player}", target != null ? target.getDisplayName() : name)
                .replace("{town}", town.getName()), sender);

        String msg = config.strings.playerKickedMembers.replace("{player}", name);
        for (Player member : town.getOnlineMembers()) Chat.send(msg, member);

        Chat.info("&e" + (sender instanceof Player ? ((Player) sender).getDisplayName() : sender.getName()) + "&b kicked &e" + (target != null ? target.getDisplayName() : name) + "&b from town &e" + town.getName());
    }

    private void rename(CommandSender sender, String name, String townName) {
        if (!hasStaffPermission(sender, "vanillatowns.staff.rename")) {
            Chat.send(config.strings.insufficientPerms, sender);
            return;
        }

        Town town = towns.getTown(townName, null);
        if (town == null) {
            Chat.send(config.strings.townNotFound, sender);
            return;
        }

        name = Chat.getDiscolored(name);
        if (towns.townExists(name)) {
            Chat.send(config.strings.nameNotAvailable, sender);
            return;
        }

        String old = town.getName();
        town.setName(name);
        plugin.saveTowns();

        String senderName = sender instanceof Player ? ((Player) sender).getDisplayName() : sender.getName();
        if (config.broadcastTownRename) {
            String bc = config.strings.townRenamedBC
                    .replace("{player}", senderName)
                    .replace("{town_old}", old)
                    .replace("{town_new}", name);
            broadcast(bc, "vanillatowns.broadcast");
        }

        if (config.logTowns)
            Chat.info("&e" + senderName + " &brenamed the town &e" + old + "&b to &e" + name);
    }

    private void delete(CommandSender sender, String townName) {
        if (!hasStaffPermission(sender, "vanillatowns.staff.delete")) {
            Chat.send(config.strings.insufficientPerms, sender);
            return;
        }

        Town town = towns.getTown(townName, null);
        if (town == null) {
            Chat.send(config.strings.townNotFound, sender);
            return;
        }

        towns.removeTown(town);
        plugin.saveTowns();

        TownMember leader = town.getLeader();

        if (town.getBalance() > 0) {
            Utils.giveMoney(leader.getUuid(), town.getBalance());
            Player target = Bukkit.getPlayer(leader.getUuid());
            if (target != null)
                Chat.send(config.strings.balanceChargeback.replace("{money}", Utils.formatNumber(town.getBalance())), target);
        }

        String senderName = sender instanceof Player ? ((Player) sender).getDisplayName() : sender.getName();
        if (config.broadcastTownDeletion) {
            String bc = config.strings.townDeletedBC.replace("{player}", senderName).replace("{town}", town.getName());
            broadcast(bc, "vanillatowns.broadcast");
        }

        if (config.logTowns) Chat.info("&e" + senderName + " &bdeleted the town &e" + town.getName());
    }

    private void setLeader(CommandSender sender, String name, String townName) {
        if (!hasStaffPermission(sender, "vanillatowns.staff.roles")) {
            Chat.send(config.strings.insufficientPerms, sender);
            return;
        }

        Town town = towns.getTown(townName, null);
        if (town == null) {
            Chat.send(config.strings.townNotFound, sender);
            return;
        }

        if (!town.isMember(name)) {
            Chat.send(config.strings.notInThatTown, sender);
            return;
        }

        String old = town.getLeader().getName();
        town.getLeader().setLeader(false);
        town.getMember(name).setLeader(true);
        plugin.saveTowns();

        String msg = config.strings.leaderTransfer.replace("{player}", name);
        for (Player member : town.getOnlineMembers()) Chat.send(msg, member);

        Chat.send(config.strings.leaderTransferStaff.replace("{player}", name).replace("{town}", town.getName()), sender);

        if (config.logTowns)
            Chat.info("&e" + (sender instanceof Player ? ((Player) sender).getDisplayName() : sender.getName()) + "&b gave &e" + town.getName() + "&b leader role from &e" + old + "&b to &e" + name);
    }

    private void setAdmin(CommandSender sender, String name, String townName) {
        if (!hasStaffPermission(sender, "vanillatowns.staff.roles")) {
            Chat.send(config.strings.insufficientPerms, sender);
            return;
        }

        Town town = towns.getTown(townName, null);
        if (town == null) {
            Chat.send(config.strings.townNotFound, sender);
            return;
        }

        if (!town.isMember(name)) {
            Chat.send(config.strings.notInThatTown, sender);
            return;
        }

        town.getMember(name).setAdmin(true);
        plugin.saveTowns();

        Chat.send(config.strings.adminPromotedStaff.replace("{player}", name).replace("{town}", town.getName()), sender);

        Player target = Bukkit.getPlayer(name);
        if (target != null) Chat.send(config.strings.adminPromotedTarget, target);

        if (config.logTowns)
            Chat.info("&e" + (sender instanceof Player ? ((Player) sender).getDisplayName() : sender.getName()) + "&b promoted &e" + name + "&b to town admin");
    }

    private void setMember(CommandSender sender, String name, String townName) {
        if (!hasStaffPermission(sender, "vanillatowns.staff.roles")) {
            Chat.send(config.strings.insufficientPerms, sender);
            return;
        }

        Town town = towns.getTown(townName, null);
        if (town == null) {
            Chat.send(config.strings.townNotFound, sender);
            return;
        }

        if (!town.isMember(name)) {
            Chat.send(config.strings.notInThatTown, sender);
            return;
        }

        if (town.isLeader(name)) {
            Chat.send(config.strings.notToTheLeader, sender);
            return;
        }

        town.getMember(name).setAdmin(false);
        plugin.saveTowns();

        Chat.send(config.strings.adminDemotedStaff.replace("{player}", name).replace("{town}", town.getName()), sender);

        Player target = Bukkit.getPlayer(name);
        if (target != null) Chat.send(config.strings.adminDemotedTarget, target);

        if (config.logTowns)
            Chat.info("&e" + (sender instanceof Player ? ((Player) sender).getDisplayName() : sender.getName()) + "&b demoted &e" + name + "&b to town admin");
    }

    private void setHome(CommandSender sender, String townName) {
        if (!hasStaffPermission(sender, "vanillatowns.staff.home.edit")) {
            Chat.send(config.strings.insufficientPerms, sender);
            return;
        }

        if (!(sender instanceof Player)) {
            Chat.send(config.strings.notPlayer, sender);
            return;
        }

        Player player = (Player) sender;

        Town town = towns.getTown(townName, null);
        if (town == null) {
            Chat.send(config.strings.townNotFound, sender);
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

    private void home(CommandSender sender, String townName) {
        if (!hasStaffPermission(sender, "vanillatowns.staff.home")) {
            Chat.send(config.strings.insufficientPerms, sender);
            return;
        }

        if (!(sender instanceof Player)) {
            Chat.send(config.strings.notPlayer, sender);
            return;
        }

        Player player = (Player) sender;

        Town town = towns.getTown(townName, null);
        if (town == null) {
            Chat.send(config.strings.townNotFound, sender);
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
            Chat.send(config.strings.noHomeStaff, player);
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

    private void delHome(CommandSender sender, String townName) {
        if (!hasStaffPermission(sender, "vanillatowns.staff.home.edit")) {
            Chat.send(config.strings.insufficientPerms, sender);
            return;
        }

        Town town = towns.getTown(townName, null);
        if (town == null) {
            Chat.send(config.strings.townNotFound, sender);
            return;
        }

        town.setHome(null);
        plugin.saveTowns();

        Chat.send(config.strings.homeRemoved, sender);

        if (config.logTowns)
            Chat.info("&e" + (sender instanceof Player ? ((Player) sender).getDisplayName() : sender.getName()) + "&b removed the home of the town &e" + town.getName());
    }

    private boolean hasStaffPermission(CommandSender sender, String perm) {
        return sender.hasPermission(perm) || sender.hasPermission("vanillatowns.staff");
    }

    private void broadcast(String msg, String perm) {
        for (Player player : Bukkit.getOnlinePlayers()) {
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
        if (!hasStaffPermission(sender, "vanillatowns.staff")) return null;

        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            List<String> tmp = new ArrayList<>();

            if (hasStaffPermission(sender, "vanillatowns.reload")) tmp.add("reload");
            if (hasStaffPermission(sender, "vanillatowns.invite")) tmp.add("invite");
            if (hasStaffPermission(sender, "vanillatowns.join")) tmp.add("join");
            if (hasStaffPermission(sender, "vanillatowns.kick")) tmp.add("kick");
            if (hasStaffPermission(sender, "vanillatowns.rename")) tmp.add("rename");
            if (hasStaffPermission(sender, "vanillatowns.delete")) tmp.add("delete");
            if (hasStaffPermission(sender, "vanillatowns.roles")) {
                tmp.add("setLeader");
                tmp.add("setAdmin");
                tmp.add("setMember");
            }
            if (hasStaffPermission(sender, "vanillatowns.home")) tmp.add("home");
            if (hasStaffPermission(sender, "vanillatowns.home.edit")) {
                tmp.add("setHome");
                tmp.add("delHome");
            }

            completions = TabCompleterUtil.getCompletions(args[0], tmp);
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("invite") ||
                    args[0].equalsIgnoreCase("join") ||
                    args[0].equalsIgnoreCase("kick") ||
                    args[0].equalsIgnoreCase("setLeader") ||
                    args[0].equalsIgnoreCase("setAdmin") ||
                    args[0].equalsIgnoreCase("setMember")) {
                completions = new ArrayList<>(TabCompleterUtil.getPlayers(args[1], sender.hasPermission("vanillatowns.vanish")));
            } else if (args[0].equalsIgnoreCase("rename") ||
                    args[0].equalsIgnoreCase("delete") ||
                    args[0].equalsIgnoreCase("setHome") ||
                    args[0].equalsIgnoreCase("home") ||
                    args[0].equalsIgnoreCase("delHome")) {

                List<String> tmp = new ArrayList<>();
                towns.getTowns().forEach(t -> tmp.add(t.getName()));

                completions = new ArrayList<>(TabCompleterUtil.getCompletions(args[1], tmp));
            }
        }

        return completions;
    }
}
