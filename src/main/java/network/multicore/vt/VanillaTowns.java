package network.multicore.vt;

import com.google.common.base.Preconditions;
import com.zaxxer.hikari.HikariConfig;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.milkbowl.vault.economy.Economy;
import network.multicore.vt.commands.TownChatCommand;
import network.multicore.vt.commands.TownCommand;
import network.multicore.vt.commands.VanillaTownsCommand;
import network.multicore.vt.data.Town;
import network.multicore.vt.data.TownRepository;
import network.multicore.vt.listeners.CacheListener;
import network.multicore.vt.listeners.OnPlayerMoveListener;
import network.multicore.vt.persistence.Database;
import network.multicore.vt.persistence.HibernateHbm2DdlAutoMode;
import network.multicore.vt.persistence.PrefixNamingStrategy;
import network.multicore.vt.persistence.datasource.DataSourceProvider;
import network.multicore.vt.persistence.entity.entities.PackageEntities;
import network.multicore.vt.utils.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


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
public class VanillaTowns extends JavaPlugin {
    public static final ConcurrentMap<UUID, Long> INVITES = new ConcurrentHashMap<>();
    public static final ConcurrentMap<Player, HomeTeleportRequest> TELEPORTS = new ConcurrentHashMap<>();
    public static final ConcurrentMap<Player, Date> TELEPORT_COOLDOWN = new ConcurrentHashMap<>();
    private Economy eco;
    private YamlDocument config;
    private Database db;
    private TownRepository townRepository;
    private Integer cooldownTask = null;
    private boolean firstRun = true;

    public VanillaTowns() {
        Text.setLogger(getLogger());
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onEnable() {
        if (!initEconomy()) {
            Text.severe("<red>Cannot initialize Vault Economy! Please install Vault and restart the server.");
            onDisable();
            return;
        }

        try {
            initConfig();
        } catch (IOException e) {
            Text.severe("<red>Cannot load config.yml file: " + e.getMessage());
            onDisable();
            return;
        }

        try {
            initStorage();
        } catch (IOException | IllegalArgumentException e) {
            Text.severe("<red>Cannot initialize storage: " + e.getMessage());
            onDisable();
            return;
        }

        Cache.init(this);

        Messages.init(this);

        getServer().getPluginManager().registerEvents(new CacheListener(), this);
        getServer().getPluginManager().registerEvents(new OnPlayerMoveListener(), this);

        registerCommands();

        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) new PAPIPlaceholders(this).register();

        Text.info("<dark_green>VanillaTowns loaded and enabled!");

        int teleportCooldown = config.getInt("teleport-cooldown", 0);
        if (teleportCooldown > 0) {
            cooldownTask = getServer().getScheduler().scheduleAsyncRepeatingTask(this, () -> {
                Date now = new Date();

                List<Player> toRemove = new ArrayList<>();
                for (Map.Entry<Player, Date> entry : TELEPORT_COOLDOWN.entrySet()) {
                    if (now.getTime() - entry.getValue().getTime() > (long) teleportCooldown * 1000) {
                        toRemove.add(entry.getKey());
                    }
                }

                toRemove.forEach(TELEPORT_COOLDOWN::remove);
            }, 0L, 20L);
        }

        firstRun = false;
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);

        if (cooldownTask != null) getServer().getScheduler().cancelTask(cooldownTask);

        try {
            if (db != null) db.close();
        } catch (Throwable ignored) {
        }

        Text.info("<dark_red>VanillaTowns disabled!");
    }

    public YamlDocument config() {
        return config;
    }

    public TownRepository townRepository() {
        return townRepository;
    }

    public boolean hasPermission(@NotNull Player player, @NotNull String permission) {
        Preconditions.checkNotNull(player, "player");
        Preconditions.checkNotNull(permission, "permission");

        return player.hasPermission(permission) ||
                player.hasPermission("vanillatowns.player") ||
                player.hasPermission("vanillatowns.staff");
    }

    public boolean hasStaffPermission(@NotNull CommandSender sender, @NotNull String permission) {
        Preconditions.checkNotNull(sender, "sender");
        Preconditions.checkNotNull(permission, "permission");

        return sender.hasPermission(permission) || sender.hasPermission("vanillatowns.staff");
    }

    public boolean hasEnoughMoney(Player player, double amount) {
        return eco.has(player, amount);
    }

    public boolean withdrawMoney(Player player, double amount) {
        return eco.withdrawPlayer(player, amount).transactionSuccess();
    }

    public boolean giveMoney(Player player, double amount) {
        return eco.depositPlayer(player, amount).transactionSuccess();
    }

    private boolean initEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) return false;
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        eco = rsp.getProvider();
        return eco != null;
    }

    private void initConfig() throws IOException {
        if (!getDataFolder().exists() || !getDataFolder().isDirectory()) {
            if (!getDataFolder().mkdir()) throw new IOException("Failed to create plugin data folder");
        }

        try (InputStream is = getClass().getClassLoader().getResourceAsStream("config.yml")) {
            config = YamlDocument.create(
                    new File(getDataFolder(), "config.yml"),
                    Objects.requireNonNull(is),
                    GeneralSettings.DEFAULT,
                    LoaderSettings.builder()
                            .setAutoUpdate(true)
                            .setCreateFileIfAbsent(true)
                            .build(),
                    DumperSettings.DEFAULT,
                    UpdaterSettings.builder()
                            .setVersioning(new BasicVersioning("file-version"))
                            .setOptionSorting(UpdaterSettings.OptionSorting.SORT_BY_DEFAULTS)
                            .build()
            );

            config.update();
            config.save();
        }
    }

    private void initStorage() throws IOException {
        DataSourceProvider<?> provider;

        String storageType = config.getString("storage-type");

        switch (storageType) {
            case "MySQL", "MariaDB", "PostgreSQL" -> {
                String address = config.getString("data.address");
                String database = config.getString("data.database");
                String username = config.getString("data.username");
                String password = config.getString("data.password");
                boolean usePool = config.getBoolean("data.pool.enabled");

                Preconditions.checkNotNull(address, "address");
                Preconditions.checkNotNull(database, "database");
                Preconditions.checkNotNull(username, "username");
                Preconditions.checkNotNull(password, "password");
                Preconditions.checkArgument(!address.isBlank(), "address must not be blank");
                Preconditions.checkArgument(!database.isBlank(), "database must not be blank");
                Preconditions.checkArgument(!username.isBlank(), "username must not be blank");

                String host;
                int port;

                if (address.contains(":")) {
                    String[] parts = address.split(":");

                    try {
                        host = parts[0];
                        port = Integer.parseInt(parts[1]);
                    } catch (Exception e) {
                        throw new IllegalArgumentException("Invalid address format");
                    }
                } else {
                    host = address;
                    port = storageType.equals("MySQL") || storageType.equals("MariaDB") ? DataSourceProvider.DEF_MYSQL_PORT : DataSourceProvider.DEF_POSTGRESQL_PORT;
                }

                if (usePool) {
                    HikariConfig poolConfig = new HikariConfig();
                    poolConfig.setMaximumPoolSize(config.getInt("data.pool.maximum-pool-size"));
                    poolConfig.setMinimumIdle(config.getInt("data.pool.minimum-idle"));
                    poolConfig.setMaxLifetime(config.getLong("data.pool.maximum-lifetime"));
                    poolConfig.setKeepaliveTime(config.getLong("data.pool.keepalive-time"));
                    poolConfig.setConnectionTimeout(config.getLong("data.pool.connection-timeout"));

                    switch (storageType) {
                        case "MySQL" -> provider = DataSourceProvider.newMysqlHikariDataSourceProvider(host, port, database, username, password, poolConfig);
                        case "MariaDB" -> provider = DataSourceProvider.newMariaDbHikariDataSourceProvider(host, port, database, username, password, poolConfig);
                        case "PostgreSQL" -> provider = DataSourceProvider.newPostgreSqlHikariDataSourceProvider(host, port, database, username, password, poolConfig);
                        default -> throw new IllegalArgumentException("Invalid storage type");
                    }
                } else {
                    switch (storageType) {
                        case "MySQL" -> provider = DataSourceProvider.newMysqlDataSourceProvider(host, port, database, username, password);
                        case "MariaDB" -> provider = DataSourceProvider.newMariaDbDataSourceProvider(host, port, database, username, password);
                        case "PostgreSQL" -> provider = DataSourceProvider.newPostgreSqlDataSourceProvider(host, port, database, username, password);
                        default -> throw new IllegalArgumentException("Invalid storage type");
                    }
                }
            }
            case "H2" -> {
                File dbFile = new File(getDataFolder(), "velocitycompact-h2.db");
                if (!dbFile.exists() || !dbFile.isFile()) {
                    if (!dbFile.createNewFile()) {
                        throw new IOException("Failed to create H2 database file");
                    }
                }

                provider = DataSourceProvider.newH2DataSourceProvider(dbFile);
            }
            case "SQLite" -> {
                File dbFile = new File(getDataFolder(), "velocitycompact-sqlite.db");
                if (!dbFile.exists() || !dbFile.isFile()) {
                    if (!dbFile.createNewFile()) {
                        throw new IOException("Failed to create SQLite database file");
                    }
                }

                provider = DataSourceProvider.newSQLiteDataSourceProvider(dbFile);
            }
            default -> {
                Text.severe("Invalid storage type");
                throw new IllegalArgumentException("Invalid storage type");
            }
        }

        Database.Builder builder = new Database.Builder();
        builder.persistenceUnitName(getName())
                .hbm2ddlAuto(HibernateHbm2DdlAutoMode.UPDATE)
                .dataSourceProvider(provider)
                .entities(new PackageEntities(Town.class.getPackageName()));

        db = builder.build();

        townRepository = db.createRepository(TownRepository.class, Town.class);
    }

    @SuppressWarnings("UnstableApiUsage")
    private void registerCommands() {
        if (!firstRun) return;

        LifecycleEventManager<Plugin> manager = this.getLifecycleManager();
        manager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            commands.register("town", "Town", List.of("towns"), new TownCommand(this));
            commands.register("townchat", "Town Chat", List.of("tchat", "tc"), new TownChatCommand(this));
            commands.register("vanillatowns", "VanillaTowns", List.of("vtowns"), new VanillaTownsCommand(this));
        });
    }
}
