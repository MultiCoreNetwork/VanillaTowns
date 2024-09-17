package network.multicore.vt.persistence.entity.entities;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StaticEntities implements Entities {
    private final List<Class<?>> entities = new ArrayList<>();

    public StaticEntities(@NotNull Class<?>... entities) {
        Collections.addAll(this.entities, entities);
    }

    public StaticEntities(@NotNull Iterable<Class<?>> entities) {
        entities.forEach(this.entities::add);
    }

    @Override
    public List<String> getEntityClassNames() {
        return entities.stream()
                .map(Class::getName)
                .toList();
    }
}
