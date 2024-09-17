package network.multicore.vt.persistence;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

public class NamingStrategy extends PhysicalNamingStrategyStandardImpl {

    @Override
    public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment context) {
        return context.getIdentifierHelper().toIdentifier(toPlural(toSnakeCase(name.getText())));
    }

    private static String toSnakeCase(String className) {
        StringBuilder builder = new StringBuilder();

        char[] chars = className.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];

            if (Character.isUpperCase(c)) {
                if (i != 0) builder.append("_");
                builder.append(Character.toLowerCase(c));
            } else {
                builder.append(c);
            }
        }

        return builder.toString();
    }

    private static String toPlural(String snakeCaseName) {
        if (snakeCaseName == null || snakeCaseName.isEmpty()) {
            return snakeCaseName;
        }

        if (snakeCaseName.endsWith("y") && !isVowel(snakeCaseName.charAt(snakeCaseName.length() - 2))) {
            return snakeCaseName.substring(0, snakeCaseName.length() - 1) + "ies";
        } else if (snakeCaseName.endsWith("s") || snakeCaseName.endsWith("sh") ||
                snakeCaseName.endsWith("ch") || snakeCaseName.endsWith("x") ||
                snakeCaseName.endsWith("z")) {
            return snakeCaseName + "es";
        } else {
            return snakeCaseName + "s";
        }
    }

    private static boolean isVowel(char c) {
        return "aeiou".indexOf(Character.toLowerCase(c)) != -1;
    }
}
