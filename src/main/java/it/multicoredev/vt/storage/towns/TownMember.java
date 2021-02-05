package it.multicoredev.vt.storage.towns;

import org.bukkit.entity.Player;

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
public class TownMember {
    private final String uuid;
    private String name;
    private boolean leader;
    private boolean admin;
    private boolean deposit;
    private boolean withdraw;

    public TownMember(Player player, boolean leader) {
        this.uuid = player.getUniqueId().toString();
        this.name = player.getName();
        this.leader = leader;
        this.admin = false;
        this.deposit = true;
        this.withdraw = false;
    }

    public UUID getUuid() {
        return UUID.fromString(uuid);
    }

    public String getName() {
        return name;
    }

    public TownMember setName(String name) {
        this.name = name;
        return this;
    }

    public boolean isLeader() {
        return leader;
    }

    public TownMember setLeader(boolean leader) {
        this.leader = leader;
        return this;
    }

    public boolean isAdmin() {
        return admin;
    }

    public TownMember setAdmin(boolean admin) {
        this.admin = admin;
        return this;
    }

    public boolean canDeposit() {
        return deposit;
    }

    public TownMember setDeposit(boolean deposit) {
        this.deposit = deposit;
        return this;
    }

    public boolean canWithdraw() {
        return withdraw;
    }

    public TownMember setWithdraw(boolean withdraw) {
        this.withdraw = withdraw;
        return this;
    }
}
