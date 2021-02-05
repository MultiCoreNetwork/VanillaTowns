package it.multicoredev.vt.storage.towns;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
public class Towns {
    private final List<Town> towns;
    private int lastId;

    public Towns() {
        towns = new ArrayList<>();
        lastId = -1;
    }

    public int getFirstId() {
        return lastId++;
    }

    public Town getTown(Player player, Town def) {
        for (Town town : towns) {
            for (TownMember member : town.getMembers()) {
                if (member.getUuid().equals(player.getUniqueId())) return town;
            }
        }

        return def;
    }

    public Town getTown(UUID uuid, Town def) {
        for (Town town : towns) {
            for (TownMember member : town.getMembers()) {
                if (member.getUuid().equals(uuid)) return town;
            }
        }

        return def;
    }

    public Town getTown(String name, Town def) {
        for (Town town : towns) {
            if (town.getName().equalsIgnoreCase(name)) return town;
        }

        return def;
    }

    public Town getTown(int id, Town def) {
        for (Town town : towns) {
            if (town.getId() == id) return town;
        }

        return def;
    }

    public void addTown(Town town) {
        towns.add(town);
    }

    public void removeTown(Town town) {
        towns.remove(town);
    }

    public boolean townExists(String name) {
        return getTown(name, null) != null;
    }

    public boolean isInTown(Player player) {
        return getTown(player, null) != null;
    }

    public List<Town> getTowns() {
        List<Town> t = new ArrayList<>(towns);
        Collections.sort(t);
        return t;
    }
}
