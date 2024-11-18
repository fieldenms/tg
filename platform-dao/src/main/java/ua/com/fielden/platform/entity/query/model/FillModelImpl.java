package ua.com.fielden.platform.entity.query.model;

import com.google.common.collect.ImmutableMap;
import ua.com.fielden.platform.entity.AbstractEntity;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

/**
 * Default immutable implementation of {@link IFillModel}.
 *
 * @param <T> an entity type for which the model provides the filling information.
 */
final class FillModelImpl<T extends AbstractEntity<?>> implements IFillModel<T> {

    private final ImmutableMap<String, Object> values;

    FillModelImpl(final Map<String, Object> values) {
        this.values = ImmutableMap.copyOf(values);
    }

    @Override
    public boolean contains(final CharSequence propName) {
        return values.containsKey(propName.toString());
    }

    @Override
    public Optional<Object> getValue(final CharSequence propName) {
        return Optional.ofNullable(values.get(propName.toString()));
    }

    @Override
    public <X> Stream<X> values(final BiFunction<? super String, Object, X> fn) {
        return values.entrySet().stream().map(entry -> fn.apply(entry.getKey(), entry.getValue()));
    }

    @Override
    public Set<String> properties() {
        return values.keySet();
    }

    @Override
    public boolean isEmpty() {
        return values.isEmpty();
    }

    @Override
    public T fill(final T entity) {
        values.forEach(entity::set);
        return entity;
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj || obj instanceof FillModelImpl that && values.equals(that.values());
    }

    @Override
    public int hashCode() {
        return values.hashCode();
    }

    @Override
    public String toString() {
        return "FillModel {%s}"
                .formatted(values.entrySet().stream()
                                   .map(entry -> "%s: %s".formatted(entry.getKey(), entry.getValue()))
                                   .collect(joining(",")));
    }

}
