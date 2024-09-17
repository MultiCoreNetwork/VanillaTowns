package network.multicore.vt.persistence;

import com.google.common.base.Preconditions;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Locale;

public class AnnotationsUtils {

    private AnnotationsUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static boolean isEntity(Class<?> classToEvaluate) {
        if (classToEvaluate == null) {
            return false;
        }

        return classToEvaluate.isAnnotationPresent(Entity.class);
    }

    public static String getEntityName(@NotNull Class<?> classToEvaluate) {
        Preconditions.checkNotNull(classToEvaluate, "classToEvaluate");

        if (!isEntity(classToEvaluate)) {
            throw new IllegalArgumentException("Class must be annotated with @Entity");
        }

        Entity entity = classToEvaluate.getAnnotation(Entity.class);
        if (entity.name() != null && !entity.name().isBlank()) {
            return entity.name();
        } else {
            return classToEvaluate.getSimpleName().toLowerCase(Locale.US);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T, ID> ID getEntityId(@NotNull T entity) {
        Preconditions.checkNotNull(entity, "entity");

        Field[] fields = entity.getClass().getDeclaredFields();

        for (Field field : fields) {
            if (field.isAnnotationPresent(Id.class)) {
                try {
                    if (field.canAccess(entity)) {
                        return (ID) field.get(entity);
                    } else {
                        field.setAccessible(true);
                        ID id = (ID) field.get(entity);
                        field.setAccessible(false);
                        return id;
                    }
                } catch (IllegalAccessException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        }

        throw new IllegalArgumentException("Entity must have an @Id field");
    }
}
