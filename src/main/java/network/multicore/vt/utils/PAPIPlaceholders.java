package network.multicore.vt.utils;

import dev.dejvokep.boostedyaml.YamlDocument;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import network.multicore.vt.VanillaTowns;
import network.multicore.vt.data.Town;
import network.multicore.vt.data.TownMember;
import network.multicore.vt.data.TownRepository;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Copyright © 2020 - 2024 by Lorenzo Magni
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
public class PAPIPlaceholders extends PlaceholderExpansion {
    private final Plugin plugin;
    private final Cache cache = Cache.get();
    private final Messages messages = Messages.get();
    private final YamlDocument config;
    private final TownRepository townRepository;

    public PAPIPlaceholders(VanillaTowns plugin) {
        this.plugin = plugin;
        this.config = plugin.config();
        this.townRepository = plugin.townRepository();
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
    @SuppressWarnings("deprecation")
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String identifier) {
        return switch (identifier) {
            case "town_name" -> {
                Optional<Town> town = cache.getTown(player);
                if (town.isEmpty()) town = townRepository.findByMember(player.getUniqueId());
                if (town.isPresent()) yield town.get().getName();
                yield messages.get("no-town");
            }
            case "town_balance" -> {
                Optional<Town> town = cache.getTown(player);
                if (town.isEmpty()) town = townRepository.findByMember(player.getUniqueId());
                if (town.isPresent()) yield Utils.formatNumber(town.get().getBalance());
                yield Utils.formatNumber(0);
            }
            case "town_role" -> {
                Optional<Town> town = cache.getTown(player);
                if (town.isEmpty()) town = townRepository.findByMember(player.getUniqueId());
                if (town.isEmpty()) yield "";

                TownMember member = town.get().getMember(player.getUniqueId());
                if (member == null) yield "";

                yield config.getString("roles." + member.getRole().getName());
            }
            case "role_color" -> {
                Optional<Town> town = cache.getTown(player);
                if (town.isEmpty()) town = townRepository.findByMember(player.getUniqueId());
                if (town.isEmpty()) yield "";

                TownMember member = town.get().getMember(player.getUniqueId());
                if (member == null) yield "";

                yield config.getString("colors." + member.getRole().getName());
            }
            case "town_home_world" -> {
                Optional<Town> town = cache.getTown(player);
                if (town.isEmpty()) town = townRepository.findByMember(player.getUniqueId());
                if (town.isEmpty()) yield "";
                if (town.get().getHome() == null) yield "";

                Optional<Location> home = town.get().getHome().getLocation();
                if (home.isEmpty()) yield "";
                yield home.get().getWorld().getName();
            }
            case "town_home_x" -> {
                Optional<Town> town = cache.getTown(player);
                if (town.isEmpty()) town = townRepository.findByMember(player.getUniqueId());
                if (town.isEmpty()) yield "";
                if (town.get().getHome() == null) yield "";

                Optional<Location> home = town.get().getHome().getLocation();
                if (home.isEmpty()) yield "";
                yield String.format("%.1f", home.get().getX());
            }
            case "town_home_y" -> {
                Optional<Town> town = cache.getTown(player);
                if (town.isEmpty()) town = townRepository.findByMember(player.getUniqueId());
                if (town.isEmpty()) yield "";
                if (town.get().getHome() == null) yield "";

                Optional<Location> home = town.get().getHome().getLocation();
                if (home.isEmpty()) yield "";
                yield String.format("%.1f", home.get().getY());
            }
            case "town_home_z" -> {
                Optional<Town> town = cache.getTown(player);
                if (town.isEmpty()) town = townRepository.findByMember(player.getUniqueId());
                if (town.isEmpty()) yield "";
                if (town.get().getHome() == null) yield "";

                Optional<Location> home = town.get().getHome().getLocation();
                if (home.isEmpty()) yield "";
                yield String.format("%.1f", home.get().getZ());
            }
            case "town_home_yaw" -> {
                Optional<Town> town = cache.getTown(player);
                if (town.isEmpty()) town = townRepository.findByMember(player.getUniqueId());
                if (town.isEmpty()) yield "";
                if (town.get().getHome() == null) yield "";

                Optional<Location> home = town.get().getHome().getLocation();
                if (home.isEmpty()) yield "";
                yield String.format("%.1f", home.get().getYaw());
            }
            case "town_home_pitch" -> {
                Optional<Town> town = cache.getTown(player);
                if (town.isEmpty()) town = townRepository.findByMember(player.getUniqueId());
                if (town.isEmpty()) yield "";
                if (town.get().getHome() == null) yield "";

                Optional<Location> home = town.get().getHome().getLocation();
                if (home.isEmpty()) yield "";
                yield String.format("%.1f", home.get().getPitch());
            }
            case "town_name_fancy" -> {
                Optional<Town> town = cache.getTown(player);
                if (town.isEmpty()) town = townRepository.findByMember(player.getUniqueId());
                if (town.isPresent()) yield config.getString("colors." + town.get().getName()) + " ";
                yield messages.get("no-town");
            }
            default -> null;
        };

    }
}
