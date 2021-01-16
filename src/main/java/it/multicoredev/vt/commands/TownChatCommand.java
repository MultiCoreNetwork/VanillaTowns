package it.multicoredev.vt.commands;

import it.multicoredev.mbcore.spigot.Chat;
import it.multicoredev.mclib.yaml.Configuration;
import it.multicoredev.vt.storage.Town;
import it.multicoredev.vt.storage.TownMember;
import it.multicoredev.vt.storage.Towns;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
public class TownChatCommand implements CommandExecutor {
    private final Configuration config;
    private final Towns towns;

    public TownChatCommand(Configuration config, Towns towns) {
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

        if (!player.hasPermission("vanillatowns.chat")) {
            Chat.send(getString("insufficient-perm"), player);
            return true;
        }

        if (args.length < 1) {
            Chat.send(getString("incorrect-usage"), player);
            return true;
        }

        if (!towns.hasTown(player)) {
            Chat.send(getString("not-in-town"), player);
            return true;
        }

        Town town = towns.getTown(player);
        if (town == null) {
            Chat.send(getString("not-in-town"), player);
            return true;
        }

        String msg = config.getString("town-chat").replace("{player}", player.getDisplayName()).replace("{message}", Chat.builder(args));

        for (TownMember member : town.getMembers()) {
            Player receiver = Bukkit.getPlayer(member.getUuid());
            if (receiver == null) continue;
            Chat.send(msg, receiver);
        }

        return true;
    }

    private String getString(String path) {
        return config.getString("messages." + path);
    }
}
