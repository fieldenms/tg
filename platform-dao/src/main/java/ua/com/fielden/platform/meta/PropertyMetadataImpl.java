package ua.com.fielden.platform.meta;

import com.google.common.collect.ImmutableMap;
import jakarta.annotation.Nullable;
import ua.com.fielden.platform.meta.PropertyMetadata.AnyKey;
import ua.com.fielden.platform.types.tuples.T2;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

abstract class PropertyMetadataImpl<N extends PropertyNature, D extends PropertyNature.Data<N>> {

    private final String name;
    private final @Nullable Object hibType;
    private final N nature;
    private final D data;
    private final PropertyTypeMetadata type;
    private final Map<PropertyMetadata.IKey, Object> keyMap;

    public String name() {
        return name;
    }

    public @Nullable Object hibType() {
        return hibType;
    }

    public N nature() {
        return nature;
    }

    public D data() {
        return data;
    }

    public PropertyTypeMetadata type() {
        return type;
    }

//    @Override
//    public <V> Optional<V> get(final Key<V, N> key) {
//        return Optional.ofNullable((V) keyMap.get(key));
//    }

    public <V> Optional<V> get(final AnyKey<V> key) {
        return Optional.ofNullable((V) keyMap.get(key));
    }

    public <K extends AnyKey<V>, V> Optional<PropertyMetadataWithKey<K, V>> withKey(final K key) {
        // Let's cast to avoid overriding this method in each subclass.
        // The cast is safe since all subclasses of this class are subtypes of the type being cast to.
        return get(key).map(v -> new PropertyMetadataWithKeyImpl<>((PropertyMetadata) this, v));
    }

    public boolean has(final AnyKey<?> key) {
        return keyMap.containsKey(key);
    }

    private PropertyMetadataImpl(final Builder<N, D> builder) {
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
               || obj instanceof PropertyMetadataImpl that
                  && Objects.equals(name, that.name)
                  && Objects.equals(hibType, that.hibType)
                  && Objects.equals(nature, that.nature)
                  && Objects.equals(data, that.data)
                  && Objects.equals(type, that.type)
                  && Objects.equals(keyMap, that.keyMap);
    }

    @Override
    public String toString() {
        return new StringJoiner(" ")
                .add(nature().toString())
                .add("[%s]".formatted(name()))
                .add("(type %s)".formatted(type()))
                .add("(hibType %s)".formatted(hibType()))
                .add("(data %s)".formatted(data()))
                .add(keyMap.entrySet().stream()
                             .map(e -> "%s: %s".formatted(e.getKey(), e.getValue()))
                             .collect(joining(",", "{", "}")))
                .toString();
    }

    static final class Persistent
            extends PropertyMetadataImpl<PropertyNature.Persistent, PropertyNature.Persistent.Data>
            implements PropertyMetadata.Persistent
    {
        private Persistent(final Builder<PropertyNature.Persistent, PropertyNature.Persistent.Data> builder) {
            super(builder);
        }

        @Override
        public <R> R match(final PropertyMetadataVisitor<R> visitor) {
            return visitor.persistent(this);
        }
    }

    static final class Calculated
            extends PropertyMetadataImpl<PropertyNature.Calculated, PropertyNature.Calculated.Data>
            implements PropertyMetadata.Calculated
    {
        private Calculated(final Builder<PropertyNature.Calculated, PropertyNature.Calculated.Data> builder) {
            super(builder);
        }

        @Override
        public <R> R match(final PropertyMetadataVisitor<R> visitor) {
            return visitor.calculated(this);
        }
    }

    static final class CritOnly
            extends PropertyMetadataImpl<PropertyNature.CritOnly, PropertyNature.CritOnly.Data>
            implements PropertyMetadata.CritOnly
    {
        private CritOnly(final Builder<PropertyNature.CritOnly, PropertyNature.CritOnly.Data> builder) {
            super(builder);
        }

        @Override
        public <R> R match(final PropertyMetadataVisitor<R> visitor) {
            return visitor.critOnly(this);
        }
    }

    static final class Plain
            extends PropertyMetadataImpl<PropertyNature.Plain, PropertyNature.Plain.Data>
            implements PropertyMetadata.Plain
    {
        private Plain(final Builder<PropertyNature.Plain, PropertyNature.Plain.Data> builder) {
            super(builder);
        }

        @Override
        public <R> R match(final PropertyMetadataVisitor<R> visitor) {
            return visitor.plain(this);
        }
    }

    /**
     * <b>NOTE</b>: This builder is <b>mutable</b>.
     * @param <N>
     */
    static class Builder<N extends PropertyNature, D extends PropertyNature.Data<N>> {
        private String name;
        private PropertyTypeMetadata type;
        private @Nullable Object hibType;
        private final N nature;
        private D data;
        private final ImmutableMap.Builder<PropertyMetadata.IKey, Object> keyMap;

        Builder(final String name, final PropertyTypeMetadata type, final @Nullable Object hibType, final N nature, final D data) {
            this.name = name;
            this.type = type;
            this.hibType = hibType;
            this.nature = nature;
            this.data = data;
            this.keyMap = ImmutableMap.builder();
        }

        /**
         * Key-value pairs are not used.
         */
        public static Builder<?, ?> toBuilder(final PropertyMetadata propertyMetadata) {
            return switch (propertyMetadata) {
                case PropertyMetadata.Calculated it -> calculatedProp(it.name(), it.type(), it.hibType(), it.data());
                case PropertyMetadata.CritOnly   it -> critOnlyProp(it.name(), it.type(), it.hibType());
                case PropertyMetadata.Persistent it -> persistentProp(it.name(), it.type(), it.hibType(), it.data());
                case PropertyMetadata.Plain      it -> plainProp(it.name(), it.type(), it.hibType());
            };
        }

        public static Builder<PropertyNature.CritOnly, PropertyNature.CritOnly.Data> critOnlyProp
                (final String name, final PropertyTypeMetadata type, final @Nullable Object hibType)
        {
            return new Builder<>(name, type, hibType, PropertyNature.CRIT_ONLY, PropertyNature.CritOnly.NO_DATA);
        }

        public static Builder<PropertyNature.Plain, PropertyNature.Plain.Data> plainProp
                (final String name, final PropertyTypeMetadata type, final @Nullable Object hibType)
        {
            return new Builder<>(name, type, hibType, PropertyNature.PLAIN, PropertyNature.Plain.NO_DATA);
        }

        public static Builder<PropertyNature.Persistent, PropertyNature.Persistent.Data> persistentProp
                (final String name, final PropertyTypeMetadata type, final @Nullable Object hibType, final PropertyNature.Persistent.Data data)
        {
            return new Builder<>(name, type, hibType, PropertyNature.PERSISTENT, data);
        }

        public static Builder<PropertyNature.Calculated, PropertyNature.Calculated.Data> calculatedProp
                (final String name, final PropertyTypeMetadata type, final @Nullable Object hibType, final PropertyNature.Calculated.Data data)
        {
            return new Builder<>(name, type, hibType, PropertyNature.CALCULATED, data);
        }

        public PropertyMetadata build() {
            return switch (nature) {
                case PropertyNature.Persistent $ -> new Persistent((Builder<PropertyNature.Persistent, PropertyNature.Persistent.Data>) this);
                case PropertyNature.Calculated $ -> new Calculated((Builder<PropertyNature.Calculated, PropertyNature.Calculated.Data>) this);
                case PropertyNature.CritOnly $ -> new CritOnly((Builder<PropertyNature.CritOnly, PropertyNature.CritOnly.Data>) this);
                case PropertyNature.Plain $ -> new Plain((Builder<PropertyNature.Plain, PropertyNature.Plain.Data>) this);
            };
        }

        public Builder<N, D> name(final String name) {
            this.name = name;
            return this;
        }

        public Builder<N, D> type(final PropertyTypeMetadata type) {
            this.type = type;
            return this;
        }

        public Builder<N, D> hibType(final Object hibType) {
            this.hibType = hibType;
            return this;
        }

        public Builder<N, D> data(final D data) {
            this.data = data;
            return this;
        }

        public <V> Builder<N, D> with(final AnyKey<V> key, final V value) {
            this.keyMap.put(key, value);
            return this;
        }

        public <V> Builder<N, D> withAnyKeys(final Iterable<T2<AnyKey<V>, V>> keysValues) {
            for (final var kv : keysValues) {
                this.keyMap.put(kv._1, kv._2);
            }
            return this;
        }

//        public <V> Builder<N, D> with(final Key<V, N> key, final V value) {
//            this.keyMap.put(key, value);
//            return this;
//        }
//
//        public <V> Builder<N, D> withKeys(final Iterable<T2<Key<V, N>, V>> keysValues) {
//            for (final var kv : keysValues) {
//                this.keyMap.put(kv._1, kv._2);
//            }
//            return this;
//        }

        public Builder<N, D> required(final boolean isRequired) {
            return with(PropertyMetadataKeys.REQUIRED, isRequired);
        }
    }

}
