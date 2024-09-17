package network.multicore.vt.persistence.entity;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Sort implements Iterable<Sort.Order>, Supplier<Stream<Sort.Order>>, Serializable {
    private static final Direction DEF_DIRECTION = Direction.ASC;
    private static final Sort UNSORTED = Sort.by(new Order[0]);
    private final List<Order> orders;

    private Sort(Direction direction, @NotNull List<String> properties) {
        Preconditions.checkNotNull(properties, "properties");
        Preconditions.checkArgument(!properties.isEmpty(), "Properties must not be empty");

        this.orders = properties.stream()
                .map(prop -> new Order(direction, prop))
                .toList();
    }

    private Sort(@NotNull List<Order> orders) {
        Preconditions.checkNotNull(orders, "orders");

        this.orders = orders;
    }

    public static Sort by(Direction direction, String... properties) {
        return new Sort(direction, List.of(properties));
    }

    public static Sort by(String... properties) {
        return by(DEF_DIRECTION, properties);
    }

    public static Sort by(Direction direction, Iterable<String> properties) {
        return new Sort(direction, StreamSupport.stream(properties.spliterator(), false).toList());
    }

    public static Sort by(@NotNull Order... orders) {
        Preconditions.checkNotNull(orders, "orders");

        return new Sort(List.of(orders));
    }

    public static Sort by(@NotNull Iterable<Order> orders) {
        return new Sort(StreamSupport.stream(orders.spliterator(), false).toList());
    }

    public static Sort unsorted() {
        return UNSORTED;
    }

    public Sort ascending() {
        return withDirection(Direction.ASC);
    }

    public Sort descending() {
        return withDirection(Direction.DESC);
    }

    public boolean isSorted() {
        return !isEmpty();
    }

    public boolean isUnsorted() {
        return !isSorted();
    }

    public Sort and(@NotNull Sort other) {
        Preconditions.checkNotNull(other, "other");

        List<Order> these = new ArrayList<>(orders);
        these.addAll(other.orders);

        return Sort.by(these);
    }

    public Sort reverse() {
        List<Order> reversed = new ArrayList<>(orders.size());
        for (Order order : this) {
            reversed.add(order.reverse());
        }

        return Sort.by(reversed);
    }

    @Nullable
    public Order getOrderFor(@NotNull String property) {
        if (property == null) {
            return null;
        }

        for (Order order : this) {
            if (property.equals(order.getProperty())) {
                return order;
            }
        }

        return null;
    }

    private Sort withDirection(Direction direction) {
        List<Order> result = new ArrayList<>(orders.size());

        for (Order order : this) {
            result.add(order.with(direction));
        }

        return Sort.by(result);
    }

    public boolean isEmpty() {
        return orders.isEmpty();
    }

    @NotNull
    @Override
    public Iterator<Order> iterator() {
        return orders.iterator();
    }

    @Override
    public Stream<Order> get() {
        return orders.stream();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Sort that)) {
            return false;
        }

        return orders.equals(that.orders);
    }

    @Override
    public int hashCode() {
        int result = 13;
        result = 17 * result + orders.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return isEmpty() ? "UNSORTED" : String.join(", ", orders.stream().map(Order::toString).toList());
    }

    public static class Order implements Serializable {
        private static final boolean DEF_IGNORE_CASE = false;
        private static final NullHandling DEF_NULL_HANDLING = NullHandling.NATIVE;

        private final Direction direction;
        private final String property;
        private final boolean ignoreCase;
        private final NullHandling nullHandling;

        public Order(Direction direction, @NotNull String property, boolean ignoreCase, NullHandling nullHandling) {
            Preconditions.checkNotNull(property, "property");

            this.direction = direction == null ? DEF_DIRECTION : direction;
            this.property = property;
            this.ignoreCase = ignoreCase;
            this.nullHandling = nullHandling;
        }

        public Order(Direction direction, @NotNull String property, NullHandling nullHandling) {
            this(direction, property, DEF_IGNORE_CASE, nullHandling);
        }

        public Order(Direction direction, @NotNull String property) {
            this(direction, property, DEF_IGNORE_CASE, DEF_NULL_HANDLING);
        }

        public static Order by(@NotNull String property) {
            return new Order(DEF_DIRECTION, property);
        }

        public static Order asc(@NotNull String property) {
            return new Order(Direction.ASC, property);
        }

        public static Order desc(@NotNull String property) {
            return new Order(Direction.DESC, property);
        }

        public Direction getDirection() {
            return direction;
        }

        public String getProperty() {
            return property;
        }

        public boolean isAscending() {
            return direction.isAscending();
        }

        public boolean isDescending() {
            return direction.isDescending();
        }

        public boolean isIgnoreCase() {
            return ignoreCase;
        }

        public Order with(Direction direction) {
            return new Order(direction, property, ignoreCase, nullHandling);
        }

        public Order reverse() {
            return with(this.direction == Direction.ASC ? Direction.DESC : Direction.ASC);
        }

        public Order withProperty(@NotNull String property) {
            return new Order(direction, property, ignoreCase, nullHandling);
        }

        public Sort withProperties(String... properties) {
            return Sort.by(direction, properties);
        }

        public Order ignoreCase() {
            return new Order(direction, property, true, nullHandling);
        }

        public Order with(NullHandling nullHandling) {
            return new Order(direction, property, ignoreCase, nullHandling);
        }

        public Order nullsFirst() {
            return with(NullHandling.NULLS_FIRST);
        }

        public Order nullsLast() {
            return with(NullHandling.NULLS_LAST);
        }

        public Order nullsNative() {
            return with(NullHandling.NATIVE);
        }

        public NullHandling getNullHandling() {
            return nullHandling;
        }

        @Override
        public int hashCode() {
            int result = 13;

            result = 17 * result + direction.hashCode();
            result = 17 * result + property.hashCode();
            result = 17 * result + (ignoreCase ? 1 : 0);
            result = 17 * result + nullHandling.hashCode();

            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (!(obj instanceof Order that)) {
                return false;
            }

            return direction.equals(that.direction) &&
                    property.equals(that.property) &&
                    ignoreCase == that.ignoreCase &&
                    nullHandling.equals(that.nullHandling);
        }

        @Override
        public String toString() {
            String str = String.format("%s: %s", property, direction);

            if (!NullHandling.NATIVE.equals(nullHandling)) {
                str += ", " + nullHandling;
            }

            if (ignoreCase) {
                str += ", ignoring case";
            }

            return str;
        }
    }

    public enum Direction {
        ASC,
        DESC;

        public boolean isAscending() {
            return this.equals(ASC);
        }

        public boolean isDescending() {
            return this.equals(DESC);
        }

        public static Direction fromString(@NotNull String value) {
            Preconditions.checkNotNull(value, "value");

            try {
                return Direction.valueOf(value.toUpperCase(Locale.US));
            } catch (Exception ignored) {
                throw new IllegalArgumentException(String.format("Invalid value '%s'.", value));
            }
        }
    }

    public enum NullHandling {
        NATIVE,
        NULLS_FIRST,
        NULLS_LAST;

        public static NullHandling fromString(@NotNull String value) {
            Preconditions.checkNotNull(value, "value");

            try {
                return NullHandling.valueOf(value.toUpperCase(Locale.US));
            } catch (Exception ignored) {
                throw new IllegalArgumentException(String.format("Invalid value '%s'.", value));
            }
        }
    }
}
