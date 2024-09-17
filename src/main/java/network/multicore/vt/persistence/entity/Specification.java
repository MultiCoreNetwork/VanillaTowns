package network.multicore.vt.persistence.entity;

import com.google.common.base.Preconditions;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.StreamSupport;

public interface Specification<T> {
    Predicate toPredicate(@NotNull Root<T> root, @NotNull CriteriaQuery<?> query, @NotNull CriteriaBuilder criteriaBuilder);

    default Specification<T> and(@NotNull Specification<T> other) {
        Preconditions.checkNotNull(other, "other");

        return SpecificationComposer.composed(this, other, CriteriaBuilder::and);
    }

    default Specification<T> or(@NotNull Specification<T> other) {
        Preconditions.checkNotNull(other, "other");

        return SpecificationComposer.composed(this, other, CriteriaBuilder::or);
    }

    static <T> Specification<T> not(Specification<T> spec) {
        return spec == null ?
                (root, query, builder) -> null :
                (root, query, builder) -> builder.not(spec.toPredicate(root, query, builder));
    }

    static <T> Specification<T> where(Specification<T> spec) {
        return spec == null ? (root, query, builder) -> null : spec;
    }

    static <T> Specification<T> allOf(@NotNull Iterable<Specification<T>> specifications) {
        Preconditions.checkNotNull(specifications, "specifications");
        Preconditions.checkArgument(specifications.iterator().hasNext(), "Specifications must not be empty");

        return StreamSupport.stream(specifications.spliterator(), false)
                .reduce(Specification.where(null), Specification::and);
    }

    @SafeVarargs
    static <T> Specification<T> allOf(@NotNull Specification<T>... specifications) {
        return allOf(List.of(specifications));
    }

    static <T> Specification<T> anyOf(@NotNull Iterable<Specification<T>> specifications) {
        Preconditions.checkNotNull(specifications, "specifications");
        Preconditions.checkArgument(specifications.iterator().hasNext(), "Specifications must not be empty");

        return StreamSupport.stream(specifications.spliterator(), false)
                .reduce(Specification.where(null), Specification::or);
    }

    @SafeVarargs
    static <T> Specification<T> anyOf(@NotNull Specification<T>... specifications) {
        return anyOf(List.of(specifications));
    }
}
