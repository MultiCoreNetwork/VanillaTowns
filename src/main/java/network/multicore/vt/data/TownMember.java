package network.multicore.vt.data;

import com.google.common.base.Preconditions;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Entity
public class TownMember {
    @Id
    private UUID uuid;
    @ManyToOne
    @JoinColumn(name = "town")
    private Town town;
    private String name;
    private TownRole role;
    private boolean deposit;
    private boolean withdraw;

    public TownMember(@NotNull Town town, @NotNull Player player, @NotNull TownRole role) {
        Preconditions.checkNotNull(town, "town");
        Preconditions.checkNotNull(player, "player");
        Preconditions.checkNotNull(role, "role");

        this.town = town;
        this.uuid = player.getUniqueId();
        this.name = player.getName();
        this.role = role;
        this.deposit = false;
        this.withdraw = false;
    }

    public TownMember(@NotNull Town town, @NotNull Player player) {
        this(town, player, TownRole.CITIZEN);
    }

    protected TownMember() {
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public Town getTown() {
        return town;
    }

    public String getName() {
        return name;
    }

    public TownMember setName(@NotNull String name) {
        Preconditions.checkNotNull(name, "name");

        this.name = name;
        return this;
    }

    public TownRole getRole() {
        return role;
    }

    public TownMember setRole(@NotNull TownRole role) {
        Preconditions.checkNotNull(role, "role");

        this.role = role;
        return this;
    }

    public boolean canDeposit() {
        return deposit || role.equals(TownRole.MAYOR) || role.equals(TownRole.OFFICER);
    }

    public TownMember setDeposit(boolean deposit) {
        this.deposit = deposit;
        return this;
    }

    public boolean canWithdraw() {
        return withdraw || role.equals(TownRole.MAYOR) || role.equals(TownRole.OFFICER);
    }

    public TownMember setWithdraw(boolean withdraw) {
        this.withdraw = withdraw;
        return this;
    }
}
