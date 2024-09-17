package network.multicore.vt.data;

import jakarta.persistence.EntityManager;
import network.multicore.vt.persistence.entity.EntityRepository;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

public class TownRepository extends EntityRepository<Town, Long> {

    public TownRepository(EntityManager entityManager, Class<Town> entityClass) {
        super(entityManager, entityClass);
    }

    public Optional<Town> findByMember(UUID uuid) {
        return entityManager.createQuery("SELECT t FROM Town t JOIN t.members m WHERE m.uuid = :uuid", Town.class)
                .setParameter("uuid", uuid)
                .getResultList()
                .stream()
                .findFirst();
    }

    public Optional<Town> findByMember(Player player) {
        return findByMember(player.getUniqueId());
    }

    public Optional<Town> findByName(String name) {
        return entityManager.createQuery("SELECT t FROM Town t WHERE t.name = :name", Town.class)
                .setParameter("name", name)
                .getResultList()
                .stream()
                .findFirst();
    }
}
