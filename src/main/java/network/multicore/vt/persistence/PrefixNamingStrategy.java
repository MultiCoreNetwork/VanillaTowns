package network.multicore.vt.persistence;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

public class PrefixNamingStrategy extends NamingStrategy {
    private static String tablePrefix;

    public static void setTablePrefix(String prefix) {
        tablePrefix = prefix;
    }

    @Override
    public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment context) {
        Identifier customName = super.toPhysicalTableName(name, context);
        return context.getIdentifierHelper().toIdentifier(tablePrefix + customName.getText());
    }
}
