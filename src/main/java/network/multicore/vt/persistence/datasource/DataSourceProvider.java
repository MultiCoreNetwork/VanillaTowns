package network.multicore.vt.persistence.datasource;

import com.google.common.base.Preconditions;
import com.mysql.cj.jdbc.MysqlDataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.h2.jdbcx.JdbcDataSource;
import org.jetbrains.annotations.NotNull;
import org.mariadb.jdbc.MariaDbDataSource;
import org.postgresql.ds.PGSimpleDataSource;
import org.sqlite.SQLiteDataSource;

import javax.sql.DataSource;
import java.io.File;
import java.sql.SQLException;
import java.util.Map;

public class DataSourceProvider<D extends DataSource> {
    public static final int DEF_MYSQL_PORT = 3306;
    public static final int DEF_POSTGRESQL_PORT = 5432;

    private final D dataSource;
    private final String driver;
    private final String dialect;

    private DataSourceProvider(D dataSource, String driver, String dialect) {
        this.dataSource = dataSource;
        this.driver = driver;
        this.dialect = dialect;
    }

    public D getDataSource() {
        return dataSource;
    }

    public String getDriver() {
        return driver;
    }

    public String getDialect() {
        return dialect;
    }

    public static DataSourceProvider<MysqlDataSource> newMysqlDataSourceProvider(@NotNull String host, int port, @NotNull String database, @NotNull String user, String password, Map<String, String> properties) {
        Preconditions.checkNotNull(host, "host");
        Preconditions.checkArgument(!host.isBlank(), "Host cannot be empty");
        Preconditions.checkArgument(port > 0, "Port must be greater than 0");
        Preconditions.checkNotNull(database, "database");
        Preconditions.checkArgument(!database.isBlank(), "Database cannot be empty");
        Preconditions.checkNotNull(user, "user");

        if (password.isBlank()) password = "";

        DataSourceType type = DataSourceType.MYSQL;

        MysqlDataSource dataSource = new MysqlDataSource();

        String url = String.format(type.getUrl(), host, port, database);
        if (properties != null && !properties.isEmpty()) {
            StringBuilder sb = new StringBuilder(url);
            sb.append("?");
            properties.forEach((k, v) -> sb.append(k).append("=").append(v).append("&"));
            url = sb.toString();
        }

        dataSource.setURL(url);
        dataSource.setUser(user);
        dataSource.setPassword(password);

        return new DataSourceProvider<>(dataSource, type.getDriver(), type.getDialect());
    }

    public static DataSourceProvider<MysqlDataSource> newMysqlDataSourceProvider(@NotNull String host, @NotNull String database, @NotNull String user, String password, Map<String, String> properties) {
        return newMysqlDataSourceProvider(host, DEF_MYSQL_PORT, database, user, password, properties);
    }

    public static DataSourceProvider<MysqlDataSource> newMysqlDataSourceProvider(@NotNull String host, int port, @NotNull String database, @NotNull String user, String password) {
        return newMysqlDataSourceProvider(host, port, database, user, password, null);
    }

    public static DataSourceProvider<MysqlDataSource> newMysqlDataSourceProvider(@NotNull String host, @NotNull String database, @NotNull String user, String password) {
        return newMysqlDataSourceProvider(host, DEF_MYSQL_PORT, database, user, password, null);
    }


    public static DataSourceProvider<HikariDataSource> newMysqlHikariDataSourceProvider(@NotNull String host, int port, @NotNull String database, @NotNull String user, String password, Map<String, String> properties, HikariConfig config) {
        Preconditions.checkNotNull(config, "config");

        DataSourceProvider<MysqlDataSource> dataSourceProvider = newMysqlDataSourceProvider(host, port, database, user, password, properties);

        config.setDataSource(dataSourceProvider.getDataSource());
        return new DataSourceProvider<>(new HikariDataSource(config), dataSourceProvider.getDriver(), dataSourceProvider.getDialect());
    }

    public static DataSourceProvider<HikariDataSource> newMysqlHikariDataSourceProvider(@NotNull String host, @NotNull String database, @NotNull String user, String password, Map<String, String> properties, HikariConfig config) {
        return newMysqlHikariDataSourceProvider(host, DEF_MYSQL_PORT, database, user, password, properties, config);
    }

    public static DataSourceProvider<HikariDataSource> newMysqlHikariDataSourceProvider(@NotNull String host, int port, @NotNull String database, @NotNull String user, String password, HikariConfig config) {
        return newMysqlHikariDataSourceProvider(host, port, database, user, password, null, config);
    }

    public static DataSourceProvider<HikariDataSource> newMysqlHikariDataSourceProvider(@NotNull String host, @NotNull String database, @NotNull String user, String password, HikariConfig config) {
        return newMysqlHikariDataSourceProvider(host, DEF_MYSQL_PORT, database, user, password, null, config);
    }


    public static DataSourceProvider<MariaDbDataSource> newMariaDbDataSourceProvider(@NotNull String host, int port, @NotNull String database, @NotNull String user, String password, Map<String, String> properties) {
        Preconditions.checkNotNull(host, "host");
        Preconditions.checkArgument(!host.isBlank(), "Host cannot be empty");
        Preconditions.checkArgument(port > 0, "Port must be greater than 0");
        Preconditions.checkNotNull(database, "database");
        Preconditions.checkArgument(!database.isBlank(), "Database cannot be empty");
        Preconditions.checkNotNull(user, "user");

        if (password.isBlank()) password = "";

        DataSourceType type = DataSourceType.MARIADB;

        MariaDbDataSource dataSource = new MariaDbDataSource();

        String url = String.format(type.getUrl(), host, port, database);
        if (properties != null && !properties.isEmpty()) {
            StringBuilder sb = new StringBuilder(url);
            sb.append("?");
            properties.forEach((k, v) -> sb.append(k).append("=").append(v).append("&"));
            url = sb.toString();
        }

        try {
            dataSource.setUrl(url);
            dataSource.setUser(user);
            dataSource.setPassword(password);
        } catch (SQLException e) {
            throw new IllegalArgumentException(e);
        }

        return new DataSourceProvider<>(dataSource, type.getDriver(), type.getDialect());
    }

    public static DataSourceProvider<MariaDbDataSource> newMariaDbDataSourceProvider(@NotNull String host, @NotNull String database, @NotNull String user, String password, Map<String, String> properties) {
        return newMariaDbDataSourceProvider(host, DEF_MYSQL_PORT, database, user, password, properties);
    }

    public static DataSourceProvider<MariaDbDataSource> newMariaDbDataSourceProvider(@NotNull String host, int port, @NotNull String database, @NotNull String user, String password) {
        return newMariaDbDataSourceProvider(host, port, database, user, password, null);
    }

    public static DataSourceProvider<MariaDbDataSource> newMariaDbDataSourceProvider(@NotNull String host, @NotNull String database, @NotNull String user, String password) {
        return newMariaDbDataSourceProvider(host, DEF_MYSQL_PORT, database, user, password, null);
    }


    public static DataSourceProvider<HikariDataSource> newMariaDbHikariDataSourceProvider(@NotNull String host, int port, @NotNull String database, @NotNull String user, String password, Map<String, String> properties, @NotNull HikariConfig config) {
        Preconditions.checkNotNull(config, "config");

        DataSourceProvider<MariaDbDataSource> dataSourceProvider = newMariaDbDataSourceProvider(host, port, database, user, password, properties);

        config.setDataSource(dataSourceProvider.getDataSource());
        return new DataSourceProvider<>(new HikariDataSource(config), dataSourceProvider.getDriver(), dataSourceProvider.getDialect());
    }

    public static DataSourceProvider<HikariDataSource> newMariaDbHikariDataSourceProvider(@NotNull String host, @NotNull String database, @NotNull String user, String password, Map<String, String> properties, HikariConfig config) {
        return newMariaDbHikariDataSourceProvider(host, DEF_MYSQL_PORT, database, user, password, properties, config);
    }

    public static DataSourceProvider<HikariDataSource> newMariaDbHikariDataSourceProvider(@NotNull String host, int port, @NotNull String database, @NotNull String user, String password, HikariConfig config) {
        return newMariaDbHikariDataSourceProvider(host, port, database, user, password, null, config);
    }

    public static DataSourceProvider<HikariDataSource> newMariaDbHikariDataSourceProvider(@NotNull String host, @NotNull String database, @NotNull String user, String password, HikariConfig config) {
        return newMariaDbHikariDataSourceProvider(host, DEF_MYSQL_PORT, database, user, password, null, config);
    }


    public static DataSourceProvider<SQLiteDataSource> newSQLiteDataSourceProvider(@NotNull File database, Map<String, String> properties) {
        Preconditions.checkNotNull(database, "file");
        Preconditions.checkArgument(database.exists(), "File must exist");
        Preconditions.checkArgument(database.isFile(), "File must be a file");

        DataSourceType type = DataSourceType.SQLITE;

        SQLiteDataSource dataSource = new SQLiteDataSource();

        String url = String.format(type.getUrl(), database.getAbsolutePath());
        if (properties != null && !properties.isEmpty()) {
            StringBuilder sb = new StringBuilder(url);
            sb.append("?");
            properties.forEach((k, v) -> sb.append(k).append("=").append(v).append("&"));
            url = sb.toString();
        }

        dataSource.setUrl(url);

        return new DataSourceProvider<>(dataSource, type.getDriver(), type.getDialect());
    }

    public static DataSourceProvider<SQLiteDataSource> newSQLiteDataSourceProvider(@NotNull File database) {
        return newSQLiteDataSourceProvider(database, null);
    }


    public static DataSourceProvider<HikariDataSource> newSQLiteHikariDataSourceProvider(@NotNull File database, Map<String, String> properties, HikariConfig config) {
        Preconditions.checkNotNull(config, "config");

        DataSourceProvider<SQLiteDataSource> dataSourceProvider = newSQLiteDataSourceProvider(database, properties);

        config.setDataSource(dataSourceProvider.getDataSource());
        return new DataSourceProvider<>(new HikariDataSource(config), dataSourceProvider.getDriver(), dataSourceProvider.getDialect());
    }

    public static DataSourceProvider<HikariDataSource> newSQLiteHikariDataSourceProvider(@NotNull File database, HikariConfig config) {
        return newSQLiteHikariDataSourceProvider(database, null, config);
    }


    public static DataSourceProvider<PGSimpleDataSource> newPostgreSqlDataSourceProvider(@NotNull String host, int port, @NotNull String database, @NotNull String user, String password, Map<String, String> properties) {
        Preconditions.checkNotNull(host, "host");
        Preconditions.checkArgument(!host.isBlank(), "Host cannot be empty");
        Preconditions.checkArgument(port > 0, "Port must be greater than 0");
        Preconditions.checkNotNull(database, "database");
        Preconditions.checkArgument(!database.isBlank(), "Database cannot be empty");
        Preconditions.checkNotNull(user, "user");

        if (password.isBlank()) password = "";

        DataSourceType type = DataSourceType.POSTGRESQL;

        PGSimpleDataSource dataSource = new PGSimpleDataSource();

        String url = String.format(type.getUrl(), host, port, database);
        if (properties != null && !properties.isEmpty()) {
            StringBuilder sb = new StringBuilder(url);
            sb.append("?");
            properties.forEach((k, v) -> sb.append(k).append("=").append(v).append("&"));
            url = sb.toString();
        }

        dataSource.setURL(url);
        dataSource.setUser(user);
        dataSource.setPassword(password);

        return new DataSourceProvider<>(dataSource, type.getDriver(), type.getDialect());
    }

    public static DataSourceProvider<PGSimpleDataSource> newPostgreSqlDataSourceProvider(@NotNull String host, @NotNull String database, @NotNull String user, String password, Map<String, String> properties) {
        return newPostgreSqlDataSourceProvider(host, DEF_POSTGRESQL_PORT, database, user, password, properties);
    }

    public static DataSourceProvider<PGSimpleDataSource> newPostgreSqlDataSourceProvider(@NotNull String host, int port, @NotNull String database, @NotNull String user, String password) {
        return newPostgreSqlDataSourceProvider(host, port, database, user, password, null);
    }

    public static DataSourceProvider<PGSimpleDataSource> newPostgreSqlDataSourceProvider(@NotNull String host, @NotNull String database, @NotNull String user, String password) {
        return newPostgreSqlDataSourceProvider(host, DEF_POSTGRESQL_PORT, database, user, password, null);
    }


    public static DataSourceProvider<HikariDataSource> newPostgreSqlHikariDataSourceProvider(@NotNull String host, int port, @NotNull String database, @NotNull String user, String password, Map<String, String> properties, HikariConfig config) {
        Preconditions.checkNotNull(config, "config");

        DataSourceProvider<PGSimpleDataSource> dataSourceProvider = newPostgreSqlDataSourceProvider(host, port, database, user, password, properties);

        config.setDataSource(dataSourceProvider.getDataSource());
        return new DataSourceProvider<>(new HikariDataSource(config), dataSourceProvider.getDriver(), dataSourceProvider.getDialect());
    }

    public static DataSourceProvider<HikariDataSource> newPostgreSqlHikariDataSourceProvider(@NotNull String host, @NotNull String database, @NotNull String user, String password, Map<String, String> properties, HikariConfig config) {
        return newPostgreSqlHikariDataSourceProvider(host, DEF_POSTGRESQL_PORT, database, user, password, properties, config);
    }

    public static DataSourceProvider<HikariDataSource> newPostgreSqlHikariDataSourceProvider(@NotNull String host, int port, @NotNull String database, @NotNull String user, String password, HikariConfig config) {
        return newPostgreSqlHikariDataSourceProvider(host, port, database, user, password, null, config);
    }

    public static DataSourceProvider<HikariDataSource> newPostgreSqlHikariDataSourceProvider(@NotNull String host, @NotNull String database, @NotNull String user, String password, HikariConfig config) {
        return newPostgreSqlHikariDataSourceProvider(host, DEF_POSTGRESQL_PORT, database, user, password, null, config);
    }


    public static DataSourceProvider<JdbcDataSource> newH2DataSourceProvider(@NotNull File file, Map<String, String> properties) {
        Preconditions.checkNotNull(file, "file");
        Preconditions.checkArgument(file.exists(), "File must exist");
        Preconditions.checkArgument(file.isFile(), "File must be a file");

        DataSourceType type = DataSourceType.H2;

        JdbcDataSource dataSource = new JdbcDataSource();

        String url = String.format(type.getUrl(), file.getAbsolutePath());
        if (properties != null && !properties.isEmpty()) {
            StringBuilder sb = new StringBuilder(url);
            sb.append("?");
            properties.forEach((k, v) -> sb.append(k).append("=").append(v).append("&"));
            url = sb.toString();
        }

        dataSource.setURL(url);

        return new DataSourceProvider<>(dataSource, type.getDriver(), type.getDialect());
    }

    public static DataSourceProvider<JdbcDataSource> newH2DataSourceProvider(@NotNull File file) {
        return newH2DataSourceProvider(file, null);
    }


    public static DataSourceProvider<HikariDataSource> newH2HikariDataSourceProvider(@NotNull File database, Map<String, String> properties, HikariConfig config) {
        Preconditions.checkNotNull(config, "config");

        DataSourceProvider<JdbcDataSource> dataSourceProvider = newH2DataSourceProvider(database, properties);

        config.setDataSource(dataSourceProvider.getDataSource());
        return new DataSourceProvider<>(new HikariDataSource(config), dataSourceProvider.getDriver(), dataSourceProvider.getDialect());
    }

    public static DataSourceProvider<HikariDataSource> newH2HikariDataSourceProvider(@NotNull File database, HikariConfig config) {
        return newH2HikariDataSourceProvider(database, null, config);
    }

    public static HikariConfig createHikariConfig(@NotNull String poolName, @NotNull HikariConfig config) {
        Preconditions.checkNotNull(poolName, "poolName");
        Preconditions.checkArgument(!poolName.isBlank(), "Pool name cannot be empty");
        Preconditions.checkNotNull(config, "config");

        config.setPoolName(poolName);
        config.setInitializationFailTimeout(1);
        config.setConnectionTestQuery("SELECT 1");

        return config;
    }
}
