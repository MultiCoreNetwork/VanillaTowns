package it.multicoredev.vt;

import be.maximvdw.placeholderapi.PlaceholderAPI;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import it.multicoredev.mbcore.spigot.Chat;
import it.multicoredev.mclib.yaml.Configuration;
import it.multicoredev.vt.commands.TownChatCommand;
import it.multicoredev.vt.commands.TownCommand;
import it.multicoredev.vt.listeners.OnChatListener;
import it.multicoredev.vt.storage.Town;
import it.multicoredev.vt.storage.TownMember;
import it.multicoredev.vt.storage.Towns;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
public class VanillaTowns extends JavaPlugin {
    public static Economy eco;
    private Configuration config;
    private File storage;
    private Towns towns;
    private Map<Town, Player> invites = new HashMap<>();

    @Override
    public void onEnable() {
        if (!initEconomy()) {
            Chat.severe("&cCannot initialize Vault Economy!");
            onDisable();
            return;
        }

        try {
            initConfig();
        } catch (IOException e) {
            e.printStackTrace();
            onDisable();
            return;
        }

        try {
            storage = new File(getDataFolder(), "towns.json");
            loadStorage();
        } catch (IOException e) {
            e.printStackTrace();
            onDisable();
            return;
        }

        TownCommand tc = new TownCommand(this, config, towns);
        getCommand("town").setExecutor(tc);
        getCommand("town").setTabCompleter(tc);
        getCommand("townchat").setExecutor(new TownChatCommand(config, towns));

        getServer().getPluginManager().registerEvents(new OnChatListener(config, towns), this);

        initMVdWPlaceholderAPI();

        Chat.info("&2VanillaTowns loaded and enabled!");
    }

    private boolean initEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) return false;
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        eco = rsp.getProvider();
        return eco != null;
    }

    private void initConfig() throws IOException {
        File configFile = new File(getDataFolder(), "config.yml");
        if (!getDataFolder().exists()) {
            if (!getDataFolder().mkdir()) throw new IOException("Cannot create plugin directory");
        }

        config = new Configuration(configFile, getResource("config.yml"));
        config.autoload();
    }

    private void initMVdWPlaceholderAPI() {
        if (!getServer().getPluginManager().isPluginEnabled("MVdWPlaceholderAPI")) return;

        PlaceholderAPI.registerPlaceholder(this, "vtown_name", event -> {
            Player player = event.getPlayer();
            if (player == null) return config.getString("placeholders.none");

            Town town = towns.getTown(player);
            if (town == null) return config.getString("placeholders.none");

            return town.getName();
        });

        PlaceholderAPI.registerPlaceholder(this, "vtown_balance", event -> {
            Player player = event.getPlayer();
            if (player == null) return Utils.formatNumber(0);

            Town town = towns.getTown(player);
            if (town == null) return Utils.formatNumber(0);

            return Utils.formatNumber(town.getBalance());
        });

        PlaceholderAPI.registerPlaceholder(this, "vtown_role", event -> {
            Player player = event.getPlayer();
            if (player == null) return config.getString("placeholders.none");

            Town town = towns.getTown(player);
            if (town == null) return config.getString("placeholders.none");

            TownMember member = town.getMember(player);
            if (member.isLeader()) return config.getString("placeholders.leader");
            if (member.isAdmin()) return config.getString("placeholders.admin");
            else return config.getString("placeholders.member");
        });

        PlaceholderAPI.registerPlaceholder(this, "vtown_role_color", event -> {
            Player player = event.getPlayer();
            if (player == null) return "";

            Town town = towns.getTown(player);
            if (town == null) return "";

            TownMember member = town.getMember(player);
            if (member.isLeader()) return config.getString(Chat.getTranslated("colors.leader"));
            if (member.isAdmin()) return config.getString(Chat.getTranslated("colors.admin"));
            else return config.getString(Chat.getTranslated("colors.member"));
        });

        PlaceholderAPI.registerPlaceholder(this, "vtown_home_w", event -> {
            Player player = event.getPlayer();
            if (player == null) return config.getString("placeholders.none");

            Town town = towns.getTown(player);
            if (town == null) return config.getString("placeholders.none");

            Location home = town.getHomeLocation();
            if (home == null) return config.getString("placeholders.none");

            return home.getWorld().getName();
        });

        PlaceholderAPI.registerPlaceholder(this, "vtown_home_x", event -> {
            Player player = event.getPlayer();
            if (player == null) return config.getString("placeholders.none");

            Town town = towns.getTown(player);
            if (town == null) return config.getString("placeholders.none");

            Location home = town.getHomeLocation();
            if (home == null) return config.getString("placeholders.none");

            return String.valueOf(home.getBlockX());
        });

        PlaceholderAPI.registerPlaceholder(this, "vtown_home_y", event -> {
            Player player = event.getPlayer();
            if (player == null) return config.getString("placeholders.none");

            Town town = towns.getTown(player);
            if (town == null) return config.getString("placeholders.none");

            Location home = town.getHomeLocation();
            if (home == null) return config.getString("placeholders.none");

            return String.valueOf(home.getBlockY());
        });

        PlaceholderAPI.registerPlaceholder(this, "vtown_home_z", event -> {
            Player player = event.getPlayer();
            if (player == null) return config.getString("placeholders.none");

            Town town = towns.getTown(player);
            if (town == null) return config.getString("placeholders.none");

            Location home = town.getHomeLocation();
            if (home == null) return config.getString("placeholders.none");

            return String.valueOf(home.getBlockZ());
        });

        PlaceholderAPI.registerPlaceholder(this, "vtown_position", event -> {
            Player player = event.getPlayer();
            if (player == null) return config.getString("placeholders.none");

            Town town = towns.getTown(player);
            if (town == null) return config.getString("placeholders.none");

            int pos = towns.getTowns().indexOf(town);

            return String.valueOf(pos + 1);
        });
    }

    private void loadStorage() throws IOException {
        if (!storage.exists() || !storage.isFile()) {
            if (!storage.createNewFile()) throw new IOException("Cannot create storage file");

            towns = new Towns();
            saveStorage();
        }

        try (FileReader reader = new FileReader(storage)) {
            Gson gson = new Gson();
            towns = gson.fromJson(reader, Towns.class);
        }
    }

    private synchronized void saveStorage() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(storage))) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            writer.write(gson.toJson(towns));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveTowns() {
        getServer().getScheduler().runTaskAsynchronously(this, this::saveStorage);
    }

    public void addInvite(Player player, Town town) {
        invites.put(town, player);
    }

    public void removeInvite(Town town) {
        invites.remove(town);
    }

    public boolean hasInvite(Player player) {
        for (Player p : invites.values()) {
            if (p.equals(player)) return true;
        }

        return false;
    }

    public Town getInvite(Player player, String name) {
        for (Map.Entry<Town, Player> entry : invites.entrySet()) {
            if (entry.getValue().equals(player) && entry.getKey().getName().toLowerCase().equals(name.toLowerCase()))
                return entry.getKey();
        }

        return null;
    }

    public List<Town> getInvites(Player player) {
        List<Town> list = new ArrayList<>();
        for (Map.Entry<Town, Player> entry : invites.entrySet()) {
            if (entry.getValue().equals(player)) list.add(entry.getKey());
        }

        return list;
    }
}
