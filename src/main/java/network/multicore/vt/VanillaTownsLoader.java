package network.multicore.vt;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("UnstableApiUsage")
public class VanillaTownsLoader implements PluginLoader {
    private static final Gson GSON = new Gson();

    @Override
    public void classloader(@NotNull PluginClasspathBuilder classpathBuilder) {
        try (InputStreamReader reader = new InputStreamReader(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("dependencies.json")), StandardCharsets.UTF_8)) {
            List<String> dependencies = GSON.fromJson(reader, new TypeToken<List<String>>() {}.getType());

            MavenLibraryResolver resolver = new MavenLibraryResolver();
            resolver.addRepository(new RemoteRepository.Builder("maven central", "default", "https://repo.maven.apache.org/maven2/").build());
            dependencies.forEach(dependency -> resolver.addDependency(new Dependency(new DefaultArtifact(dependency), null)));

            classpathBuilder.addLibrary(resolver);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
