package network.multicore.vt.persistence.entity;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;

import java.io.Serializable;

public class SpecificationComposer {

    private SpecificationComposer() {
        throw new IllegalStateException("Utility class");
    }

    static <T> Specification<T> composed(Specification<T> thisSpec, Specification<T> otherSpec, Combiner combiner) {
        return (root, query, builder) -> {
            Predicate thisPredicate = thisSpec.toPredicate(root, query, builder);
            Predicate otherPredicate = otherSpec.toPredicate(root, query, builder);

            if (thisPredicate == null) {
                return otherPredicate;
            }

            return otherPredicate == null ? thisPredicate : combiner.combine(builder, thisPredicate, otherPredicate);
        };
    }

    interface Combiner extends Serializable {
        Predicate combine(CriteriaBuilder builder, Predicate left, Predicate right);
    }
}
