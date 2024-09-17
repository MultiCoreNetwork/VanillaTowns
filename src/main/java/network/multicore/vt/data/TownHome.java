package network.multicore.vt.data;

import com.google.common.base.Preconditions;
import jakarta.persistence.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@Entity
public class TownHome {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @OneToOne
    @JoinColumn(name = "town")
    private Town town;
    private String world;
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;

    public TownHome(@NotNull Town town, @NotNull Location location) {
        Preconditions.checkNotNull(town, "town");
        Preconditions.checkNotNull(location, "location");
        Preconditions.checkNotNull(location.getWorld(), "world");

        this.town = town;
        this.world = location.getWorld().getName();
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
        this.yaw = location.getYaw();
        this.pitch = location.getPitch();
    }

    protected TownHome() {
    }

    public long getId() {
        return id;
    }

    public Town getTown() {
        return town;
    }

    public Optional<Location> getLocation() {
        World w = Bukkit.getWorld(world);
        if (w == null) return Optional.empty();
        return Optional.of(new Location(w, x, y, z, yaw, pitch));
    }
}
