package network.multicore.vt.persistence;

import com.google.common.base.Preconditions;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.spi.PersistenceProvider;
import network.multicore.vt.persistence.datasource.DataSourceProvider;
import network.multicore.vt.persistence.entity.EntityRepository;
import network.multicore.vt.persistence.entity.entities.Entities;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.hibernate.jpa.boot.spi.IntegratorProvider;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Database implements Closeable {
    private static final PersistenceProvider PERSISTENCE_PROVIDER = new HibernatePersistenceProvider();
    private final EntityManagerFactory emf;
    private final EntityManager em;

    private Database(String persistenceUnitName, DataSourceProvider<?> dataSourceProvider, Entities entities, Properties properties, Map<String, Object> configuration) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(Database.class.getClassLoader());
        this.emf = PERSISTENCE_PROVIDER.createContainerEntityManagerFactory(
                new PersistenceUnitInfoImpl(persistenceUnitName, entities.getEntityClassNames(), properties).setNonJtaDataSource(dataSourceProvider.getDataSource()),
                configuration
        );
        this.em = emf.createEntityManager();
        Thread.currentThread().setContextClassLoader(classLoader);
    }

    public <T, R extends EntityRepository<T, ?>> R createRepository(@NotNull Class<R> repositoryClass, @NotNull Class<T> entityClass) {
        Preconditions.checkNotNull(repositoryClass, "repositoryClass");
        Preconditions.checkNotNull(entityClass, "entityClass");
        Preconditions.checkArgument(AnnotationsUtils.isEntity(entityClass), "Entity class must be annotated with @Entity");

        try {
            return repositoryClass.getConstructor(EntityManager.class, Class.class).newInstance(em, entityClass);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public void close() throws IllegalStateException {
        if (em != null) em.close();
        if (emf != null) emf.close();
    }

    public static class Builder {
        private String persistenceUnitName;
        private DataSourceProvider<?> dataSourceProvider;
        private Entities entities;
        private String tablesPrefix;
        private Properties properties = new Properties();
        private Integrator integrator;
        private Map<String, Object> configuration = new HashMap<>();

        public Builder persistenceUnitName(@NotNull String persistenceUnitName) {
            Preconditions.checkNotNull(persistenceUnitName, "persistenceUnitName");
            Preconditions.checkArgument(!persistenceUnitName.isBlank(), "Persistence unit name cannot be empty");

            this.persistenceUnitName = persistenceUnitName;
            return this;
        }

        public Builder dataSourceProvider(@NotNull DataSourceProvider<?> dataSourceProvider) {
            Preconditions.checkNotNull(dataSourceProvider, "dataSourceProvider");
            Preconditions.checkNotNull(dataSourceProvider.getDataSource(), "Data source must not be null");
            Preconditions.checkNotNull(dataSourceProvider.getDriver(), "Data source driver must not be null");
            Preconditions.checkArgument(!dataSourceProvider.getDriver().isBlank(), "Data source driver cannot be empty");
            Preconditions.checkNotNull(dataSourceProvider.getDialect(), "Data source dialect must not be null");
            Preconditions.checkArgument(!dataSourceProvider.getDialect().isBlank(), "Data source dialect cannot be empty");

            this.dataSourceProvider = dataSourceProvider;
            return this;
        }

        public Builder entities(@NotNull Entities entities) {
            Preconditions.checkNotNull(entities, "entities");

            this.entities = entities;
            return this;
        }

        public Builder tablesPrefix(@NotNull String tablesPrefix) {
            Preconditions.checkNotNull(tablesPrefix, "tablePrefix");
            Preconditions.checkArgument(!tablesPrefix.isBlank(), "Tables prefix cannot be empty");

            this.tablesPrefix = tablesPrefix.endsWith("_") ? tablesPrefix : tablesPrefix.concat("_");
            return this;
        }

        public Builder properties(@NotNull Properties properties) {
            Preconditions.checkNotNull(properties, "properties");

            this.properties = properties;
            return this;
        }

        public Builder withProperty(@NotNull String key, @NotNull Object value) {
            Preconditions.checkNotNull(key, "key");
            Preconditions.checkNotNull(value, "value");

            this.properties.put(key, value);
            return this;
        }

        public Builder removeProperty(@NotNull String key) {
            properties.remove(key);
            return this;
        }

        public Builder hbm2ddlAuto(@NotNull HibernateHbm2DdlAutoMode mode) {
            Preconditions.checkNotNull(mode, "mode");

            return withProperty("hibernate.hbm2ddl.auto", mode.getValue());
        }

        public Builder showSql(boolean showSql) {
            return withProperty("hibernate.show_sql", showSql);
        }

        public Builder integrator(Integrator integrator) {
            this.integrator = integrator;
            return this;
        }

        public Builder configuration(@NotNull Map<String, Object> configuration) {
            Preconditions.checkNotNull(configuration, "configuration");

            this.configuration = configuration;
            return this;
        }

        public Builder withConfiguration(@NotNull String key, @NotNull Object value) {
            Preconditions.checkNotNull(key, "key");
            Preconditions.checkNotNull(value, "value");

            this.configuration.put(key, value);
            return this;
        }

        public Builder removeConfiguration(@NotNull String key) {
            configuration.remove(key);
            return this;
        }

        public Database build() {
            // Not needed
            // properties.put("hibernate.dialect", dataSourceProvider.getDialect());
            properties.put("hibernate.connection.datasource", dataSourceProvider.getDataSource());
            if (!properties.containsKey("hibernate.hbm2ddl.auto")) {
                properties.put("hibernate.hbm2ddl.auto", HibernateHbm2DdlAutoMode.VALIDATE.getValue());
            }
            if (tablesPrefix != null) {
                properties.put("hibernate.globally_quoted_identifiers", Boolean.TRUE.toString());
                properties.put("hibernate.physical_naming_strategy", PrefixNamingStrategy.class.getName());
                properties.put("hibernate.physical_naming_strategy.table_prefix", tablesPrefix);
            } else {
                properties.put("hibernate.physical_naming_strategy", NamingStrategy.class.getName());
            }

            if (integrator != null && !configuration.containsKey("hibernate.integrator_provider")) {
                properties.put("hibernate.integrator_provider", (IntegratorProvider) () -> Collections.singletonList(integrator));
            }

            // properties.put("hibernate.show_sql", true);
            // properties.put("hibernate.format_sql", true);
            // properties.put("hibernate.use_sql_comments", true);

            return new Database(
                    persistenceUnitName.contains(" ") ? persistenceUnitName.replace(" ", "_") : persistenceUnitName,
                    dataSourceProvider,
                    entities,
                    properties,
                    configuration
            );
        }
    }
}
