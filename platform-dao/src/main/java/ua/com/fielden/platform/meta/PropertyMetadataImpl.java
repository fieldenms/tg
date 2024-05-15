package ua.com.fielden.platform.meta;

import com.google.common.collect.ImmutableMap;
import ua.com.fielden.platform.meta.PropertyMetadata.AnyKey;
import ua.com.fielden.platform.types.tuples.T2;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

abstract class PropertyMetadataImpl<N extends PropertyNature, D extends PropertyNature.Data<N>> {

    private final String name;
    private final Object hibType;
    private final N nature;
    private final D data;
    private final PropertyTypeMetadata type;
    private final Map<PropertyMetadata.IKey, Object> keyMap;

    public String name() {
        return name;
    }

    public Object hibType() {
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

    static final class Transient
            extends PropertyMetadataImpl<PropertyNature.Transient, PropertyNature.Transient.Data>
            implements PropertyMetadata.Transient
    {
        private Transient(final Builder<PropertyNature.Transient, PropertyNature.Transient.Data> builder) {
            super(builder);
        }

        @Override
        public <R> R match(final PropertyMetadataVisitor<R> visitor) {
            return visitor.transient_(this);
        }
    }

    /**
     * <b>NOTE</b>: This builder is <b>mutable</b>.
     * @param <N>
     */
    static class Builder<N extends PropertyNature, D extends PropertyNature.Data<N>> {
        private final String name;
        private final PropertyTypeMetadata type;
        private final Object hibType;
        private final N nature;
        private final D data;
        private final ImmutableMap.Builder<PropertyMetadata.IKey, Object> keyMap;

        Builder(final String name, final PropertyTypeMetadata type, final Object hibType, final N nature, final D data) {
            this.name = name;
            this.type = type;
            this.hibType = hibType;
            this.nature = nature;
            this.data = data;
            this.keyMap = ImmutableMap.builder();
        }

        public static Builder<PropertyNature.CritOnly, PropertyNature.CritOnly.Data> critOnlyProp
                (final String name, final PropertyTypeMetadata type, final Object hibType)
        {
            return new Builder<>(name, type, hibType, PropertyNature.CRIT_ONLY, PropertyNature.CritOnly.NO_DATA);
        }

        public static Builder<PropertyNature.Transient, PropertyNature.Transient.Data> transientProp
                (final String name, final PropertyTypeMetadata type, final Object hibType)
        {
            return new Builder<>(name, type, hibType, PropertyNature.TRANSIENT, PropertyNature.Transient.NO_DATA);
        }

        public static Builder<PropertyNature.Persistent, PropertyNature.Persistent.Data> persistentProp
                (final String name, final PropertyTypeMetadata type, final Object hibType, final PropertyNature.Persistent.Data data)
        {
            return new Builder<>(name, type, hibType, PropertyNature.PERSISTENT, data);
        }

        public static Builder<PropertyNature.Calculated, PropertyNature.Calculated.Data> calculatedProp
                (final String name, final PropertyTypeMetadata type, final Object hibType, final PropertyNature.Calculated.Data data)
        {
            return new Builder<>(name, type, hibType, PropertyNature.CALCULATED, data);
        }

        public PropertyMetadata build() {
            return switch (nature) {
                case PropertyNature.Persistent $ -> new Persistent((Builder<PropertyNature.Persistent, PropertyNature.Persistent.Data>) this);
                case PropertyNature.Calculated $ -> new Calculated((Builder<PropertyNature.Calculated, PropertyNature.Calculated.Data>) this);
                case PropertyNature.CritOnly $ -> new CritOnly((Builder<PropertyNature.CritOnly, PropertyNature.CritOnly.Data>) this);
                case PropertyNature.Transient $ -> new Transient((Builder<PropertyNature.Transient, PropertyNature.Transient.Data>) this);
            };
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
