package network.multicore.vt.persistence.entity;

import com.google.common.base.Preconditions;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import network.multicore.vt.persistence.AnnotationsUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public abstract class EntityRepository<T, ID> {
    private static final String DELETE_ALL_QUERY = "delete from %s x";
    private static final String COUNT_QUERY = "select count(x) from %s x";

    protected final EntityManager entityManager;
    protected final Class<T> entityClass;
    protected final String entityName;

    public EntityRepository(EntityManager entityManager, Class<T> entityClass) {
        this.entityManager = entityManager;
        this.entityClass = entityClass;
        this.entityName = AnnotationsUtils.getEntityName(entityClass);
    }

    public void deleteById(@NotNull ID id) {
        Preconditions.checkNotNull(id, "idl");

        entityManager.getTransaction().begin();

        try {
            findById(id).ifPresent(this::delete);
            entityManager.getTransaction().commit();
        } catch (Exception ignored) {
            entityManager.getTransaction().rollback();
        }
    }

    public void delete(@NotNull T entity) {
        Preconditions.checkNotNull(entity, "entity");

        ID id = AnnotationsUtils.getEntityId(entity);
        T existing = entityManager.find(entityClass, id);

        if (existing == null) return;

        entityManager.getTransaction().begin();

        try {
            entityManager.remove(entityManager.contains(entity) ? entity : entityManager.merge(entity));
            entityManager.getTransaction().commit();
        } catch (Exception ignored) {
            entityManager.getTransaction().rollback();
        }
    }

    public void deleteAll(@NotNull Iterable<T> entities) {
        Preconditions.checkNotNull(entities, "entities");

        for (T entity : entities) {
            delete(entity);
        }
    }

    public void deleteAllInBatch(@NotNull Iterable<T> entities) {
        Preconditions.checkNotNull(entities, "entities");

        if (!entities.iterator().hasNext()) {
            return;
        }

        // TODO This is not in batch
        while (entities.iterator().hasNext()) {
            entityManager.remove(entities.iterator().next());
        }
    }

    public void deleteAll() {
        List<T> entities = findAll();
        deleteAll(entities);
    }

    public void deleteAllById(@NotNull Iterable<ID> ids) {
        Preconditions.checkNotNull(ids, "ids");

        for (ID id : ids) {
            deleteById(id);
        }
    }

    public void deleteAllByIdInBatch(@NotNull Iterable<ID> ids) {
        Preconditions.checkNotNull(ids, "ids");

        if (!ids.iterator().hasNext()) {
            return;
        }

        // TODO This is not in batch
        while (ids.iterator().hasNext()) {
            deleteById(ids.iterator().next());
        }
    }

    public void deleteAllInBatch() {
        Query query = entityManager.createQuery(String.format(DELETE_ALL_QUERY, entityName));

        entityManager.getTransaction().begin();

        try {
            query.executeUpdate();
            entityManager.getTransaction().commit();
        } catch (Exception ignored) {
            entityManager.getTransaction().rollback();
        }
    }

    public Optional<T> findById(@NotNull ID id) {
        Preconditions.checkNotNull(id, "ids");

        return Optional.ofNullable(entityManager.find(entityClass, id));
    }

    public boolean existsById(@NotNull ID id) {
        Preconditions.checkNotNull(id, "ids");

        return findById(id).isPresent();
    }

    public List<T> findAll() {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = builder.createQuery(entityClass);
        Root<T> root = query.from(entityClass);

        return entityManager.createQuery(query.select(root)).getResultList();
    }

    public List<T> findAllById(@NotNull Iterable<ID> ids) {
        Preconditions.checkNotNull(ids, "ids");

        List<T> entities = new ArrayList<>();
        for (ID id : ids) {
            findById(id).ifPresent(entities::add);
        }

        return entities;
    }

    public List<T> findAll(Sort sort) {
        return getQuery(null, sort).getResultList();
    }

    public Optional<T> findOne(Specification<T> spec) {
        try {
            return Optional.of(getQuery(spec, Sort.unsorted()).setMaxResults(2).getSingleResult());
        } catch (NoResultException ignored) {
            return Optional.empty();
        }
    }

    public List<T> findAll(Specification<T> spec) {
        return getQuery(spec, Sort.unsorted()).getResultList();
    }

    public List<T> findAll(Specification<T> spec, Sort sort) {
        return getQuery(spec, sort).getResultList();
    }

    public boolean exists(Specification<T> spec) {
        CriteriaQuery<Integer> criteriaQuery = entityManager.getCriteriaBuilder()
                .createQuery(Integer.class)
                .select(entityManager.getCriteriaBuilder().literal(1));

        applySpecificationToCriteria(spec, entityClass, criteriaQuery);

        TypedQuery<Integer> typedQuery = entityManager.createQuery(criteriaQuery);
        return typedQuery.setMaxResults(1).getResultList().size() == 1;
    }

    public long delete(Specification<T> spec) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaDelete<T> delete = builder.createCriteriaDelete(entityClass);

        if (spec != null) {
            Predicate predicate = spec.toPredicate(delete.from(entityClass), null, builder);

            if (predicate != null) {
                delete.where(predicate);
            }
        }

        return entityManager.createQuery(delete).executeUpdate();
    }

    public long count() {
        TypedQuery<Long> query = entityManager.createQuery(String.format(COUNT_QUERY, entityName), Long.class);
        return query.getSingleResult();
    }

    public long count(Specification<T> spec) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = builder.createQuery(Long.class);

        Root<T> root = applySpecificationToCriteria(spec, entityClass, criteriaQuery);

        if (criteriaQuery.isDistinct()) {
            criteriaQuery.select(builder.countDistinct(root));
        } else {
            criteriaQuery.select(builder.count(root));
        }

        criteriaQuery.orderBy(Collections.emptyList());

        TypedQuery<Long> typedQuery = entityManager.createQuery(criteriaQuery);

        List<Long> totals = typedQuery.getResultList();
        long total = 0;

        for (Long elem : totals) {
            total += elem == null ? 0 : elem;
        }

        return total;
    }

    public <S extends T> S save(@NotNull S entity) {
        Preconditions.checkNotNull(entity, "entity");

        ID id = AnnotationsUtils.getEntityId(entity);

        entityManager.getTransaction().begin();

        try {
            if (id != null && findById(id).isPresent()) {
                S result = entityManager.merge(entity);
                entityManager.getTransaction().commit();
                return result;
            } else {
                entityManager.persist(entity);
                entityManager.getTransaction().commit();
                return entity;
            }
        } catch (Exception ignored) {
            entityManager.getTransaction().rollback();
            return null;
        }
    }

    public <S extends T> List<S> saveAll(@NotNull Iterable<S> entities) {
        Preconditions.checkNotNull(entities, "entities");

        List<S> result = new ArrayList<>();
        for (S entity : entities) {
            result.add(save(entity));
        }

        return result;
    }

    protected <S extends T> TypedQuery<S> getQuery(Specification<S> spec, Class<S> entityClass, Sort sort) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<S> query = builder.createQuery(entityClass);

        Root<S> root = applySpecificationToCriteria(spec, entityClass, query);
        query.select(root);

        if (sort.isSorted()) {
            query.orderBy(toOrders(sort, root, builder));
        }

        return entityManager.createQuery(query);
    }

    protected TypedQuery<T> getQuery(Specification<T> spec, Sort sort) {
        return getQuery(spec, entityClass, sort);
    }

    private <S, U extends T> Root<U> applySpecificationToCriteria(Specification<U> spec, @NotNull Class<U> entityClass, @NotNull CriteriaQuery<S> query) {
        Preconditions.checkNotNull(entityClass, "entityClass");
        Preconditions.checkNotNull(query, "query");

        Root<U> root = query.from(entityClass);

        if (spec == null) {
            return root;
        }

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        Predicate predicate = spec.toPredicate(root, query, builder);

        if (predicate != null) {
            query.where(predicate);
        }

        return root;
    }

    private List<Order> toOrders(Sort sort, @NotNull From<?, ?> from, @NotNull CriteriaBuilder builder) {
        Preconditions.checkNotNull(from, "from");
        Preconditions.checkNotNull(builder, "builder");

        if (sort == null || sort.isUnsorted()) {
            return Collections.emptyList();
        }

        List<Order> orders = new ArrayList<>();
        for (Sort.Order order : sort) {
            orders.add(toJpaOrder(order, from, builder));
        }

        return orders;
    }

    @SuppressWarnings("unchecked")
    private Order toJpaOrder(Sort.Order order, From<?, ?> from, CriteriaBuilder builder) {
        Path<?> propertyPath = getPath(order.getProperty(), from);

        if (order.isIgnoreCase() && propertyPath.getJavaType() == String.class) {
            Expression<String> lower = builder.lower((Expression<String>) propertyPath);
            return order.isAscending() ? builder.asc(lower) : builder.desc(lower);
        } else {
            return order.isAscending() ? builder.asc(propertyPath) : builder.desc(propertyPath);
        }
    }

    private Path<?> getPath(String property, From<?, ?> from) {
        String[] propertyParts = property.split("\\.");
        Path<?> path = from;

        for (String part : propertyParts) {
            path = path.get(part);
        }

        return path;
    }
}
