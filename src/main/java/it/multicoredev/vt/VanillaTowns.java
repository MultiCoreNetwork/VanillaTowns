package it.multicoredev.vt;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import it.multicoredev.mbcore.spigot.Chat;
import it.multicoredev.vt.commands.TownChatCommand;
import it.multicoredev.vt.commands.TownCommand;
import it.multicoredev.vt.commands.VanillaTownsCommand;
import it.multicoredev.vt.listeners.OnChatListener;
import it.multicoredev.vt.placeholders.VTPlaceholders;
import it.multicoredev.vt.storage.Config;
import it.multicoredev.vt.storage.towns.Towns;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.charset.StandardCharsets;

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
public class VanillaTowns extends JavaPlugin {
    private static final Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    public static Economy eco;
    public static Config config;
    public static Towns towns;
    private File configFile;
    private File townsFile;

    @Override
    public void onEnable() {
        if (!initEconomy()) {
            Chat.severe("&cCannot initialize Vault Economy! Please install Vault and restart the server.");
            onDisable();
            return;
        }

        configFile = new File(getDataFolder(), "config.json");
        townsFile = new File(getDataFolder(), "towns.json");

        if (!initStorage()) {
            onDisable();
            return;
        }

        TownCommand tCommand = new TownCommand(this);
        getCommand("town").setExecutor(tCommand);
        getCommand("town").setTabCompleter(tCommand);
        VanillaTownsCommand vtCommand = new VanillaTownsCommand(this);
        getCommand("vanillatowns").setExecutor(vtCommand);
        getCommand("vanillatowns").setTabCompleter(vtCommand);
        getCommand("townchat").setExecutor(new TownChatCommand());

        getServer().getPluginManager().registerEvents(new OnChatListener(), this);

        new VTPlaceholders(this);

        Chat.info("&2VanillaTowns loaded and enabled!");
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
        Chat.info("&cVanillaTowns disabled!");
    }

    private boolean initEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) return false;
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        eco = rsp.getProvider();
        return eco != null;
    }

    private boolean initStorage() {
        if (!getDataFolder().exists() || !getDataFolder().isDirectory()) {
            if (!getDataFolder().mkdir()) {
                new IOException("Cannot create VanillaTowns directory").printStackTrace();
                return false;
            }
        }

        if (!configFile.exists() || !configFile.isFile()) {
            config = new Config();
            try {
                saveConfig(configFile, config);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else {
            try {
                config = loadConfig(configFile, Config.class);
                if (config == null) config = new Config();
                if (config.completeMissing()) saveConfig(configFile, config);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        if (!townsFile.exists() || !townsFile.isFile()) {
            towns = new Towns();
            try {
                saveConfig(townsFile, towns);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else {
            try {
                towns = loadConfig(townsFile, Towns.class);
                if (towns == null) towns = new Towns();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        return true;
    }

    private <T> T loadConfig(File src, Class<T> classOfT) throws Exception {
        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(src), StandardCharsets.UTF_8)) {
            return gson.fromJson(reader, classOfT);
        }
    }

    private synchronized void saveConfig(File dst, Object obj) throws Exception {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dst), StandardCharsets.UTF_8))) {
            writer.write(gson.toJson(obj));
            writer.flush();
        }
    }

    public synchronized boolean saveTowns() {
        try {
            saveConfig(townsFile, towns);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
