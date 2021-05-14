package it.multicoredev.vt.storage.towns;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
public class Town implements Comparable<Town> {
    private final int id;
    private String name;
    private double balance;
    private TownHome home;
    private final List<TownMember> members;

    public Town(int id, String name, Player leader) {
        this.id = id;
        this.name = name;
        this.members = new ArrayList<>();
        this.members.add(new TownMember(leader, true));
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Town setName(String name) {
        this.name = name;
        return this;
    }

    public double getBalance() {
        return balance;
    }

    public Town addBalance(double amount) {
        balance += amount;
        return this;
    }

    public TownHome getHome() {
        return home;
    }

    public Town setHome(TownHome home) {
        this.home = home;
        return this;
    }

    public List<TownMember> getMembers() {
        return members;
    }

    public TownMember getLeader() {
        for (TownMember member : members) {
            if (member.isLeader()) return member;
        }

        return null;
    }

    public List<TownMember> getAdmins() {
        List<TownMember> admins = new ArrayList<>();
        for (TownMember member : members) {
            if (member.isAdmin()) admins.add(member);
        }

        return admins;
    }

    public List<TownMember> getSimpleMembers() {
        List<TownMember> simpleMembers = new ArrayList<>();
        for (TownMember member : members) {
            if (!member.isLeader() && !member.isAdmin()) simpleMembers.add(member);
        }

        return simpleMembers;
    }

    public TownMember getMember(Player player) {
        for (TownMember member : members) {
            if (member.getUuid().equals(player.getUniqueId())) return member;
        }

        return null;
    }

    public TownMember getMember(UUID uuid) {
        for (TownMember member : members) {
            if (member.getUuid().equals(uuid)) return member;
        }

        return null;
    }

    public TownMember getMember(String name) {
        for (TownMember member : members) {
            if (member.getName().equalsIgnoreCase(name)) return member;
        }

        return null;
    }

    public void addMember(TownMember member) {
        members.add(member);
    }

    public void removeMember(String name) {
        members.removeIf(member -> member.getName().equalsIgnoreCase(name));
    }

    public void removeMember(UUID uuid) {
        members.removeIf(members -> members.getUuid().equals(uuid));
    }

    public boolean isMember(Player player) {
        return getMember(player) != null;
    }

    public boolean isMember(String name) {
        return getMember(name) != null;
    }

    public boolean isLeader(Player player) {
        TownMember member = getMember(player);
        return member != null && member.isLeader();
    }

    public boolean isLeader(UUID uuid) {
        TownMember member = getMember(uuid);
        return member != null && member.isLeader();
    }

    public boolean isLeader(String name) {
        TownMember member = getMember(name);
        return member != null && member.isLeader();
    }

    public boolean isAdmin(Player player) {
        TownMember member = getMember(player);
        return member != null && member.isAdmin();
    }

    public boolean isAdmin(UUID uuid) {
        TownMember member = getMember(uuid);
        return member != null && member.isAdmin();
    }

    public String getAdminNames() {
        List<TownMember> admins = getAdmins();
        if (admins.size() == 0) return "[]";

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < admins.size(); i++) {
            builder.append(admins.get(i).getName());
            if (i < admins.size() - 1) builder.append(", ");
        }

        return "[" + builder.toString() + "]";
    }

    public String getSimpleMembersNames() {
        List<TownMember> members = getSimpleMembers();
        if (members.size() == 0) return "[]";

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < members.size(); i++) {
            builder.append(members.get(i).getName());
            if (i < members.size() - 1) builder.append(", ");
        }

        return "[" + builder.toString() + "]";
    }

    public List<Player> getOnlineMembers() {
        List<Player> players = new ArrayList<>();

        for (TownMember member : members) {
            Player player = Bukkit.getPlayer(member.getUuid());
            if (player != null) players.add(player);
        }

        return players;
    }

    public boolean canDeposit(Player player) {
        TownMember member = getMember(player);
        if (member == null) return false;

        return member.isLeader() || member.isAdmin() || member.canDeposit();
    }

    public boolean canWithdraw(Player player) {
        TownMember member = getMember(player);
        if (member == null) return false;

        return member.isLeader() || member.isAdmin() || member.canWithdraw();
    }

    @Override
    public int compareTo(@NotNull Town town) {
        return Double.compare(town.getBalance(), getBalance());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Town town = (Town) o;
        return id == town.id && Objects.equals(name, town.name);
    }
}
