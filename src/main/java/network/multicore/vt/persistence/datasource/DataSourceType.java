package network.multicore.vt.persistence.datasource;

public enum DataSourceType {
    MYSQL("com.mysql.ci.jdbc.Driver", "jdbc:mysql://%s:%d/%s", "org.hibernate.dialect.MySQLDialect"),
    MARIADB("org.mariadb.jdbc.Driver", "jdbc:mariadb://%s:%d/%s", "org.hibernate.dialect.MariaDBDialect"),
    SQLITE("org.sqlite.JDBC", "jdbc:sqlite:%s", "org.hibernate.dialect.SQLiteDialect"),
    POSTGRESQL("org.postgresql.Driver", "jdbc:postgresql://%s:%d/%s", "org.hibernate.dialect.PostgreSQLDialect"),
    H2("org.h2.Driver", "jdbc:h2:%s", "org.hibernate.dialect.H2Dialect"),
    H2_MEMORY("org.h2.Driver", "jdbc:h2:mem:%s", "org.hibernate.dialect.H2Dialect");

    private final String driver;
    private final String url;
    private final String dialect;

    DataSourceType(String driver, String url, String dialect) {
        this.driver = driver;
        this.url = url;
        this.dialect = dialect;
    }

    public String getDriver() {
        return driver;
    }

    public String getUrl() {
        return url;
    }

    public String getDialect() {
        return dialect;
    }
}
