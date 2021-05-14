package it.multicoredev.vt.placeholders;

import it.multicoredev.vt.Utils;
import it.multicoredev.vt.storage.Config;
import it.multicoredev.vt.storage.towns.Town;
import it.multicoredev.vt.storage.towns.Towns;

import java.util.UUID;

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
public class PlaceholdersUtils {
    private final Config config;
    private final Towns towns;

    public PlaceholdersUtils(Config config, Towns towns) {
        this.config = config;
        this.towns = towns;
    }

    public String getTownName(UUID uuid) {
        Town town = towns.getTown(uuid, null);
        if (town == null) return config.strings.noTown;

        return town.getName();
    }

    public String getTownBalance(UUID uuid) {
        Town town = towns.getTown(uuid, null);
        if (town == null) return Utils.formatNumber(0);

        return Utils.formatNumber(town.getBalance());
    }

    public String getTownRole(UUID uuid) {
        Town town = towns.getTown(uuid, null);
        if (town == null) return config.strings.noRole;

        if (town.isLeader(uuid)) return config.strings.leader;
        else if (town.isAdmin(uuid)) return config.strings.admin;
        else return config.strings.member;
    }

    public String getTownRoleColor(UUID uuid) {
        Town town = towns.getTown(uuid, null);
        if (town == null) return config.strings.noRole;

        if (town.isLeader(uuid)) return config.colors.leader;
        else if (town.isAdmin(uuid)) return config.colors.admin;
        else return config.colors.member;
    }

    public String getTownHomeWorld(UUID uuid) {
        Town town = towns.getTown(uuid, null);
        if (town == null) return "";
        if (town.getHome() == null) return "";

        return town.getHome().getWorld();
    }

    public String getTownHomeX(UUID uuid) {
        Town town = towns.getTown(uuid, null);
        if (town == null) return "";
        if (town.getHome() == null) return "";

        return String.valueOf(town.getHome().getX());
    }

    public String getTownHomeY(UUID uuid) {
        Town town = towns.getTown(uuid, null);
        if (town == null) return "";
        if (town.getHome() == null) return "";

        return String.valueOf(town.getHome().getY());
    }

    public String getTownHomeZ(UUID uuid) {
        Town town = towns.getTown(uuid, null);
        if (town == null) return "";
        if (town.getHome() == null) return "";

        return String.valueOf(town.getHome().getZ());
    }

    public String getTownRank(UUID uuid) {
        Town town = towns.getTown(uuid, null);
        if (town == null) return config.strings.noTown;

        return String.valueOf(towns.getTowns().indexOf(town) + 1);
    }
}
