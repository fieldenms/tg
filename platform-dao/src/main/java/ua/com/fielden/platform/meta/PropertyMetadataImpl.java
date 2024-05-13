package ua.com.fielden.platform.meta;

import com.google.common.collect.ImmutableMap;
import ua.com.fielden.platform.types.tuples.T2;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

final class PropertyMetadataImpl<N extends PropertyNature> implements PropertyMetadata<N> {

    private final String name;
    private final Object hibType;
    private final N nature;
    private final PropertyNature.Data<N> data;
    private final PropertyTypeMetadata type;
    private final Map<IKey, Object> keyMap;

    @Override
    public String name() {
        return name;
    }

    @Override
    public Object hibType() {
        return hibType;
    }

    public N nature() {
        return nature;
    }

    @Override
    public PropertyNature.Data<N> data() {
        return data;
    }

    public PropertyTypeMetadata type() {
        return type;
    }

    public <R> R match(PropertyMetadataVisitor<R> visitor) {
        return switch (nature) {
            case PropertyNature.Persistent $ -> visitor.persistent((PropertyMetadata<PropertyNature.Persistent>) this,
                                                                   (PropertyNature.Persistent.Data) data());
            case PropertyNature.Calculated $ -> visitor.calculated((PropertyMetadata<PropertyNature.Calculated>) this,
                                                                   (PropertyNature.Calculated.Data) data());
            case PropertyNature.CritOnly $ -> visitor.critOnly((PropertyMetadata<PropertyNature.CritOnly>) this);
            case PropertyNature.Transient $ -> visitor.transient_((PropertyMetadata<PropertyNature.Transient>) this);
        };
    }

    @Override
    public <V> Optional<V> get(final Key<V, N> key) {
        return Optional.ofNullable((V) keyMap.get(key));
    }

    @Override
    public <V> Optional<V> get(final AnyKey<V> key) {
        return Optional.ofNullable((V) keyMap.get(key));
    }

    private PropertyMetadataImpl(final Builder<N> builder) {
        name = requireNonNull(builder.name);
        hibType = builder.hibType;
        nature = requireNonNull(builder.nature);
        data = requireNonNull(builder.data);
        type = requireNonNull(builder.type);
        keyMap = requireNonNull(builder.keyMap.build());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, hibType, nature, data, type, keyMap);
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj
               || obj instanceof PropertyMetadataImpl<?> that
                  && Objects.equals(name, that.name)
                  && Objects.equals(hibType, that.hibType)
                  && Objects.equals(nature, that.nature)
                  && Objects.equals(data, that.data)
                  && Objects.equals(type, that.type)
                  && Objects.equals(keyMap, that.keyMap);
    }

    /**
     * <b>NOTE</b>: This builder is <b>mutable</b>.
     * @param <N>
     */
    static class Builder<N extends PropertyNature> {
        private final String name;
        private final PropertyTypeMetadata type;
        private final Object hibType;
        private final N nature;
        private final PropertyNature.Data<N> data;
        private final ImmutableMap.Builder<IKey, Object> keyMap;

        Builder(final String name, final PropertyTypeMetadata type, final Object hibType, final N nature, final PropertyNature.Data<N> data) {
            this.name = name;
            this.type = type;
            this.hibType = hibType;
            this.nature = nature;
            this.data = data;
            this.keyMap = ImmutableMap.builder();
        }

        public static Builder<PropertyNature.CritOnly> critOnlyProp
                (final String name, final PropertyTypeMetadata type, final Object hibType)
        {
            return new Builder<>(name, type, hibType, PropertyNature.CRIT_ONLY, PropertyNature.CritOnly.NO_DATA);
        }

        public static Builder<PropertyNature.Transient> transientProp
                (final String name, final PropertyTypeMetadata type, final Object hibType)
        {
            return new Builder<>(name, type, hibType, PropertyNature.TRANSIENT, PropertyNature.Transient.NO_DATA);
        }

        public static Builder<PropertyNature.Persistent> persistentProp
                (final String name, final PropertyTypeMetadata type, final Object hibType, final PropertyNature.Persistent.Data data)
        {
            return new Builder<>(name, type, hibType, PropertyNature.PERSISTENT, data);
        }

        public static Builder<PropertyNature.Calculated> calculatedProp
                (final String name, final PropertyTypeMetadata type, final Object hibType, final PropertyNature.Calculated.Data data)
        {
            return new Builder<>(name, type, hibType, PropertyNature.CALCULATED, data);
        }

        public PropertyMetadata<N> build() {
            return new PropertyMetadataImpl<>(this);
        }

        public <V> Builder<N> with(final AnyKey<V> key, final V value) {
            this.keyMap.put(key, value);
            return this;
        }

        public <V> Builder<N> withAnyKeys(final Iterable<T2<AnyKey<V>, V>> keysValues) {
            for (final var kv : keysValues) {
                this.keyMap.put(kv._1, kv._2);
            }
            return this;
        }

        public <V> Builder<N> with(final Key<V, N> key, final V value) {
            this.keyMap.put(key, value);
            return this;
        }

        public <V> Builder<N> withKeys(final Iterable<T2<Key<V, N>, V>> keysValues) {
            for (final var kv : keysValues) {
                this.keyMap.put(kv._1, kv._2);
            }
            return this;
        }

        public Builder<N> required(final boolean isRequired) {
            return with(PropertyMetadataKeys.REQUIRED, isRequired);
        }
    }

}
