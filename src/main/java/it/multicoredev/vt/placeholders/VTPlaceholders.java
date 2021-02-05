package it.multicoredev.vt.placeholders;

import be.maximvdw.placeholderapi.PlaceholderAPI;
import it.multicoredev.vt.Utils;
import it.multicoredev.vt.storage.towns.Town;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import static it.multicoredev.vt.VanillaTowns.config;
import static it.multicoredev.vt.VanillaTowns.towns;

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
public class VTPlaceholders extends PlaceholderExpansion {
    private final Plugin plugin;

    public VTPlaceholders(Plugin plugin) {
        this.plugin = plugin;

        if (plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) register();

        if (plugin.getServer().getPluginManager().isPluginEnabled("MVdWPlaceholderAPI")) registerMVdWPlaceholders();
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public @NotNull String getAuthor() {
        return "LoreSchaeffer";
    }

    @Override
    public @NotNull String getIdentifier() {
        return "vanillatowns";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String identifier) {
        switch (identifier) {
            case "town_name":
                return getTownName(player.getUniqueId());
            case "town_balance":
                return getTownBalance(player.getUniqueId());
            case "town_role":
                return getTownRole(player.getUniqueId());
            case "role_color":
                return getTownRoleColor(player.getUniqueId());
            case "town_home_w":
                return getTownHomeWorld(player.getUniqueId());
            case "town_home_x":
                return getTownHomeX(player.getUniqueId());
            case "town_home_y":
                return getTownHomeY(player.getUniqueId());
            case "town_home_z":
                return getTownHomeZ(player.getUniqueId());
            case "town_rank":
                return getTownRank(player.getUniqueId());
        }

        return null;
    }

    private void registerMVdWPlaceholders() {
        PlaceholderAPI.registerPlaceholder(plugin, "vt_town_name", e -> getTownName(e.getPlayer().getUniqueId()));

        PlaceholderAPI.registerPlaceholder(plugin, "vt_town_balance", e -> getTownBalance(e.getPlayer().getUniqueId()));

        PlaceholderAPI.registerPlaceholder(plugin, "vt_town_role", e -> getTownRole(e.getPlayer().getUniqueId()));

        PlaceholderAPI.registerPlaceholder(plugin, "vt_role_color", e -> getTownRoleColor(e.getPlayer().getUniqueId()));

        PlaceholderAPI.registerPlaceholder(plugin, "vt_town_home_w", e -> getTownHomeWorld(e.getPlayer().getUniqueId()));

        PlaceholderAPI.registerPlaceholder(plugin, "vt_town_home_x", e -> getTownHomeX(e.getPlayer().getUniqueId()));

        PlaceholderAPI.registerPlaceholder(plugin, "vt_town_home_y", e -> getTownHomeY(e.getPlayer().getUniqueId()));

        PlaceholderAPI.registerPlaceholder(plugin, "vt_town_home_z", e -> getTownHomeZ(e.getPlayer().getUniqueId()));

        PlaceholderAPI.registerPlaceholder(plugin, "vt_town_rank", e -> getTownRank(e.getPlayer().getUniqueId()));
    }

    private String getTownName(UUID uuid) {
        Town town = towns.getTown(uuid, null);
        if (town == null) return config.strings.noTown;

        return town.getName();
    }

    private String getTownBalance(UUID uuid) {
        Town town = towns.getTown(uuid, null);
        if (town == null) return Utils.formatNumber(0);

        return Utils.formatNumber(town.getBalance());
    }

    private String getTownRole(UUID uuid) {
        Town town = towns.getTown(uuid, null);
        if (town == null) return config.strings.noRole;

        if (town.isLeader(uuid)) return config.strings.leader;
        else if (town.isAdmin(uuid)) return config.strings.admin;
        else return config.strings.member;
    }

    private String getTownRoleColor(UUID uuid) {
        Town town = towns.getTown(uuid, null);
        if (town == null) return config.strings.noRole;

        if (town.isLeader(uuid)) return config.colors.leader;
        else if (town.isAdmin(uuid)) return config.colors.admin;
        else return config.colors.member;
    }

    private String getTownHomeWorld(UUID uuid) {
        Town town = towns.getTown(uuid, null);
        if (town == null) return "";
        if (town.getHome() == null) return "";

        return town.getHome().getWorld();
    }

    private String getTownHomeX(UUID uuid) {
        Town town = towns.getTown(uuid, null);
        if (town == null) return "";
        if (town.getHome() == null) return "";

        return String.valueOf(town.getHome().getX());
    }

    private String getTownHomeY(UUID uuid) {
        Town town = towns.getTown(uuid, null);
        if (town == null) return "";
        if (town.getHome() == null) return "";

        return String.valueOf(town.getHome().getY());
    }

    private String getTownHomeZ(UUID uuid) {
        Town town = towns.getTown(uuid, null);
        if (town == null) return "";
        if (town.getHome() == null) return "";

        return String.valueOf(town.getHome().getZ());
    }

    private String getTownRank(UUID uuid) {
        Town town = towns.getTown(uuid, null);
        if (town == null) return config.strings.noTown;

        return String.valueOf(towns.getTowns().indexOf(town) + 1);
    }
}
