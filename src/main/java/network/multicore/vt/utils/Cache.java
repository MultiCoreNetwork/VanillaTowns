package network.multicore.vt.utils;

import network.multicore.vt.VanillaTowns;
import network.multicore.vt.data.Town;
import network.multicore.vt.data.TownMember;
import network.multicore.vt.data.TownRepository;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

public class Cache {
    private static Cache instance;
    private final TownRepository townRepository;
    private final List<Town> towns = new CopyOnWriteArrayList<>();

    private Cache(VanillaTowns plugin) {
        this.townRepository = plugin.townRepository();
    }

    public static void init(VanillaTowns plugin) {
        if (instance == null) instance = new Cache(plugin);
    }

    public static Cache get() {
        if (instance == null) throw new IllegalStateException("Cache has not been initialized");
        return instance;
    }

    public void loadTown(Player player) {
        townRepository.findByMember(player.getUniqueId()).ifPresent(town -> {
            TownMember member = town.getMember(player);
            if (!member.getName().equalsIgnoreCase(player.getName())) {
                member.setName(player.getName());
                town = townRepository.save(town);
            }

            towns.add(town);
        });
    }

    public void unloadTown(Player player) {
        Town town = towns.stream()
                .filter(t -> t.getMember(player) != null)
                .findFirst()
                .orElse(null);

        if (town == null) return;
        if (town.getMembers().stream().allMatch(member -> Bukkit.getPlayer(member.getUniqueId()) == null)) towns.remove(town);
    }

    public void addTown(Town town) {
        towns.add(town);
    }

    public void removeTown(Town town) {
        towns.removeIf(t -> t.getId() == town.getId());
    }

    public void updateTown(Town town) {
        Town current = towns.stream()
                .filter(t -> t.getId() == town.getId())
                .findFirst()
                .orElse(null);

        if (current == null) {
            towns.add(town);
        } else {
            towns.set(towns.indexOf(current), town);
        }
    }

    public boolean isInTown(Player player) {
        return towns.stream().anyMatch(t -> t.getMember(player) != null);
    }

    public Optional<Town> getTown(Player player) {
        return towns.stream()
                .filter(t -> t.getMember(player) != null)
                .findFirst();
    }

    public Optional<Town> getTown(OfflinePlayer player) {
        return towns.stream()
                .filter(t -> t.getMember(player.getUniqueId()) != null)
                .findFirst();
    }
}
