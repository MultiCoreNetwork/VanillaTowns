package it.multicoredev.vt.storage;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
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
public class Town {
    private int id;
    private String name;
    private double balance;
    private String world;
    private double x;
    private double y;
    private double z;
    private final List<TownMember> members = new ArrayList<>();

    public Town(int id, String name, Player leader) {
        this.id = id;
        this.name = name;
        this.members.add(new TownMember(leader, true));
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public void addBalance(double amount) {
        balance += amount;
    }

    public Location getHomeLocation() {
        if (world.equals("")) return null;
        return new Location(Bukkit.getWorld(world), x, y, z);
    }

    public void setHomeLocation(Location location) {
        if (location == null) {
            world = "";
            x = 0;
            y = 0;
            z = 0;
        } else {
            world = location.getWorld().getName();
            x = location.getX();
            y = location.getY();
            z = location.getZ();
        }
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
            if (member.equals(player)) return member;
        }

        return null;
    }

    public TownMember getMember(String name) {
        for (TownMember member : members) {
            if (member.equals(name)) return member;
        }

        return null;
    }

    public void addMember(TownMember member) {
        members.add(member);
    }

    public void removeMember(String name) {
        members.removeIf(member -> member.equals(name));
    }

    public boolean hasMember(String name) {
        for (TownMember member : members) {
            if (member.equals(name)) return true;
        }

        return false;
    }

    public boolean isLeader(Player player) {
        TownMember member = getMember(player);
        if (member == null) return false;
        return member.isLeader();
    }

    public boolean isAdmin(Player player) {
        TownMember member = getMember(player);
        if (member == null) return false;
        return member.isAdmin();
    }
}
