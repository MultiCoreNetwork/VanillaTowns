package network.multicore.vt.persistence.entity.entities;

import com.google.common.base.Preconditions;
import jakarta.persistence.Entity;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.util.ArrayList;
import java.util.List;

public class PackageEntities implements Entities {
    private final List<Class<?>> entities = new ArrayList<>();

    public PackageEntities(@NotNull ClassLoader classLoader, @NotNull String... packages) {
        Preconditions.checkNotNull(classLoader, "classLoader");
        Preconditions.checkNotNull(packages, "packages");

        ConfigurationBuilder builder = new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forClassLoader(classLoader))
                .addScanners(Scanners.TypesAnnotated)
                .forPackages(packages);

        Reflections reflections = new Reflections(builder);
        entities.addAll(reflections.getTypesAnnotatedWith(Entity.class));
    }

    public PackageEntities(@NotNull String... packages) {
        this(Thread.currentThread().getContextClassLoader(), packages);
    }

    @Override
    public List<String> getEntityClassNames() {
        return entities.stream()
                .map(Class::getName)
                .toList();
    }
}
