package network.multicore.vt.data;

import com.google.common.base.Preconditions;
import jakarta.persistence.*;
import network.multicore.vt.utils.Text;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
public class Town {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String name;
    private double balance;
    @OneToOne(mappedBy = "town", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private TownHome home;
    @OneToMany(mappedBy = "town", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<TownMember> members;

    public Town(@NotNull String name, @NotNull Player leader) {
        Preconditions.checkNotNull(name, "name");
        Preconditions.checkNotNull(leader, "leader");

        this.name = Text.stripFormatting(name);
        this.members = new ArrayList<>();
        this.members.add(new TownMember(this, leader, TownRole.MAYOR));
    }

    protected Town() {
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Town setName(@NotNull String name) {
        Preconditions.checkNotNull(name, "name");

        this.name = Text.stripFormatting(name);
        return this;
    }

    public double getBalance() {
        return balance;
    }

    public double deposit(double amount) {
        balance += amount;
        return balance;
    }

    public double withdraw(double amount) {
        if (balance < amount) throw new IllegalArgumentException("Insufficient funds");
        balance -= amount;
        return balance;
    }

    public Town setBalance(double balance) {
        this.balance = balance;
        return this;
    }

    public TownHome getHome() {
        return home;
    }

    public Town setHome(Location location) {
        if (location == null) this.home = null;
        else this.home = new TownHome(this, location);
        return this;
    }

    public List<TownMember> getMembers() {
        return members;
    }

    public TownMember getMember(@NotNull Player player) {
        Preconditions.checkNotNull(player, "player");

        return members.stream()
                .filter(member -> member.getUniqueId().equals(player.getUniqueId()))
                .findFirst()
                .orElse(null);
    }

    public TownMember getMember(@NotNull UUID uuid) {
        Preconditions.checkNotNull(uuid, "uuid");

        return members.stream()
                .filter(member -> member.getUniqueId().equals(uuid))
                .findFirst()
                .orElse(null);
    }

    public TownMember getMember(@NotNull String name) {
        Preconditions.checkNotNull(name, "name");

        return members.stream()
                .filter(member -> member.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public Town addMember(@NotNull Player player) {
        Preconditions.checkNotNull(player, "player");

        members.add(new TownMember(this, player, TownRole.CITIZEN));
        return this;
    }

    public Town removeMember(@NotNull Player player) {
        Preconditions.checkNotNull(player, "player");

        members.removeIf(member -> member.getUniqueId().equals(player.getUniqueId()));
        return this;
    }

    public Town removeMember(@NotNull UUID uuid) {
        Preconditions.checkNotNull(uuid, "uuid");

        members.removeIf(member -> member.getUniqueId().equals(uuid));
        return this;
    }

    public TownMember getMayor() {
        return members.stream()
                .filter(m -> m.getRole().equals(TownRole.MAYOR))
                .findFirst()
                .orElse(null);
    }

    public List<TownMember> getOfficers() {
        return members.stream()
                .filter(m -> m.getRole().equals(TownRole.OFFICER))
                .toList();
    }

    public List<TownMember> getCitizens() {
        return members.stream()
                .filter(m -> m.getRole().equals(TownRole.CITIZEN))
                .toList();
    }

    public boolean canInvite(Player player) {
        return player.getUniqueId().equals(getMayor().getUniqueId()) ||
                getOfficers().stream().anyMatch(m -> m.getUniqueId().equals(player.getUniqueId()));
    }

    public boolean canKick(Player player, TownMember target) {
        return player.getUniqueId().equals(getMayor().getUniqueId()) ||
                (getOfficers().stream().anyMatch(m -> m.getUniqueId().equals(player.getUniqueId())) && target.getRole().equals(TownRole.CITIZEN));
    }

    public boolean canEditHome(Player player) {
        return player.getUniqueId().equals(getMayor().getUniqueId()) ||
                getOfficers().stream().anyMatch(m -> m.getUniqueId().equals(player.getUniqueId()));
    }

    public void setMayor(TownMember newMayor) {
        TownMember oldMayor = getMayor();
        oldMayor.setRole(TownRole.CITIZEN);
        newMayor.setRole(TownRole.MAYOR);
    }
}
