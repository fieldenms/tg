package ua.com.fielden.platform.entity.query.model;

import com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

final class FillModelImpl implements IFillModel {

    private final ImmutableMap<String, Object> values;

    FillModelImpl(final Map<String, Object> values) {
        this.values = ImmutableMap.copyOf(values);
    }

    @Override
    public boolean contains(final CharSequence property) {
        return values.containsKey(property.toString());
    }

    @Override
    public Optional<Object> getValue(final CharSequence property) {
        return Optional.ofNullable(values.get(property.toString()));
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
    public void forEach(final BiConsumer<? super String, Object> fn) {
        values.forEach(fn);
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
