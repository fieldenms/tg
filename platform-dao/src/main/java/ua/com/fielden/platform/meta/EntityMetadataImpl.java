package ua.com.fielden.platform.meta;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;

import java.util.*;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.function.Function.identity;

abstract class EntityMetadataImpl<N extends EntityNature, D extends EntityNature.Data<N>> {

    private final Class<? extends AbstractEntity<?>> javaType;
    private final N nature;
    private final D data;
    private final Map<String, ? extends PropertyMetadata> properties;

    EntityMetadataImpl(final Builder<N, D> builder) {
        this.javaType = builder.javaType;
        this.nature = builder.nature;
        this.data = builder.data;
        this.properties = builder.properties.stream()
                .collect(toImmutableMap(PropertyMetadata::name, identity()));
    }

    public Class<? extends AbstractEntity<?>> javaType() {
        return javaType;
    }

    public Collection<? extends PropertyMetadata> properties() {
        return properties.values();
    }

    public Optional<PropertyMetadata> property(final String name) {
        return Optional.ofNullable(properties.get(name));
    }

    public N nature() {
        return nature;
    }

    public D data() {
        return data;
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this
               || obj instanceof EntityMetadataImpl that
                  && Objects.equals(this.javaType, that.javaType)
                  && Objects.equals(this.nature, that.nature)
                  && Objects.equals(this.data, that.data)
                  && Objects.equals(this.properties, that.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(javaType, nature, data, properties);
    }

    @Override
    public String toString() {
        return "EntityMetadata[" +
               "javaType=" + javaType + ", " +
               "nature=" + nature + ", " +
               "data=" + data + ", " +
               "properties=" + properties + ']';
    }

    static final class Persistent
            extends EntityMetadataImpl<EntityNature.Persistent, EntityNature.Persistent.Data>
            implements EntityMetadata.Persistent
    {
        Persistent(final Builder<EntityNature.Persistent, EntityNature.Persistent.Data> builder) {
            super(builder);
        }

        @Override
        public <R> R match(final EntityMetadataVisitor<R> visitor) {
            return visitor.persistent(this);
        }
    }

    static final class Synthetic
            extends EntityMetadataImpl<EntityNature.Synthetic, EntityNature.Synthetic.Data>
            implements EntityMetadata.Synthetic
    {
        Synthetic(final Builder<EntityNature.Synthetic, EntityNature.Synthetic.Data> builder) {
            super(builder);
        }

        @Override
        public <R> R match(final EntityMetadataVisitor<R> visitor) {
            return visitor.synthetic(this);
        }
    }

    static final class Union
            extends EntityMetadataImpl<EntityNature.Union, EntityNature.Union.Data>
            implements EntityMetadata.Union
    {
        Union(final Builder<EntityNature.Union, EntityNature.Union.Data> builder) {
            super(builder);
        }

        @Override
        public Class<? extends AbstractUnionEntity> javaType() {
            return (Class<? extends AbstractUnionEntity>) super.javaType();
        }

        @Override
        public <R> R match(final EntityMetadataVisitor<R> visitor) {
            return visitor.union(this);
        }
    }

    static final class Other
            extends EntityMetadataImpl<EntityNature.Other, EntityNature.Other.Data>
            implements EntityMetadata.Other
    {
        Other(final Builder<EntityNature.Other, EntityNature.Other.Data> builder) {
            super(builder);
        }

        @Override
        public <R> R match(final EntityMetadataVisitor<R> visitor) {
            return visitor.otherwise(this);
        }
    }

    /**
     * <b>NOTE</b>: This builder is <b>mutable</b>.
     */
    static final class Builder<N extends EntityNature, D extends EntityNature.Data<N>> {
        private final Class<? extends AbstractEntity<?>> javaType;
        private final N nature;
        private final D data;
        private final Collection<PropertyMetadata> properties = new ArrayList<>();

        Builder(final Class<? extends AbstractEntity<?>> javaType, final N nature, final D data) {
            this.javaType = javaType;
            this.nature = nature;
            this.data = data;
        }

        public EntityMetadata build() {
            return switch (nature) {
                case EntityNature.Persistent $ -> new Persistent((Builder<EntityNature.Persistent, EntityNature.Persistent.Data>) this);
                case EntityNature.Synthetic $ -> new Synthetic((Builder<EntityNature.Synthetic, EntityNature.Synthetic.Data>) this);
                case EntityNature.Union $ -> new Union((Builder<EntityNature.Union, EntityNature.Union.Data>) this);
                case EntityNature.Other $ -> new Other((Builder<EntityNature.Other, EntityNature.Other.Data>) this);
            };
        }

        public Builder<N, D> properties(final Iterable<? extends PropertyMetadata> properties) {
            properties.forEach(this.properties::add);
            return this;
        }

        public Builder<N, D> properties(final PropertyMetadata... properties) {
            Collections.addAll(this.properties, properties);
            return this;
        }

        static Builder<EntityNature.Persistent, EntityNature.Persistent.Data> persistentEntity
                (final Class<? extends AbstractEntity<?>> type, final EntityNature.Persistent.Data data) {
            return new Builder<>(type, EntityNature.PERSISTENT, data);
        }

        static Builder<EntityNature.Synthetic, EntityNature.Synthetic.Data> syntheticEntity
                (final Class<? extends AbstractEntity<?>> type, final EntityNature.Synthetic.Data data) {
            return new Builder<>(type, EntityNature.SYNTHETIC, data);
        }

        static Builder<EntityNature.Union, EntityNature.Union.Data> unionEntity
                (final Class<? extends AbstractUnionEntity> type, final EntityNature.Union.Data data) {
            return new Builder<>(type, EntityNature.UNION, data);
        }

        static Builder<EntityNature.Other, EntityNature.Other.Data> otherEntity
                (final Class<? extends AbstractEntity<?>> type) {
            return new Builder<>(type, EntityNature.OTHER, EntityNature.Other.NO_DATA);
        }
    }

}
