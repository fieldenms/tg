package ua.com.fielden.platform.meta;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;

import java.util.*;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.function.Function.identity;

final class EntityMetadataImpl<N extends EntityNature> implements TypeMetadata.Entity<N> {

    private final Class<? extends AbstractEntity<?>> javaType;
    private final N nature;
    private final EntityNature.Data<N> data;
    private final Map<String, ? extends PropertyMetadata> properties;

    EntityMetadataImpl(final Builder<N> builder) {
        this.javaType = builder.javaType;
        this.nature = builder.nature;
        this.data = builder.data;
        this.properties = builder.properties.stream()
                .collect(toImmutableMap(PropertyMetadata::name, identity()));
    }

    @Override
    public Class<? extends AbstractEntity<?>> javaType() {
        return javaType;
    }

    @Override
    public Collection<? extends PropertyMetadata> properties() {
        return properties.values();
    }

    @Override
    public Optional<PropertyMetadata> property(final String name) {
        return Optional.ofNullable(properties.get(name));
    }

    @Override
    public N nature() {
        return nature;
    }

    @Override
    public EntityNature.Data<N> data() {
        return data;
    }

    @Override
    public <R> R match(final EntityMetadataVisitor<R> visitor) {
        return switch (nature) {
            case EntityNature.Persistent $ -> visitor.persistent((TypeMetadata.Entity<EntityNature.Persistent>) this,
                                                                 (EntityNature.Persistent.Data) data);
            case EntityNature.Synthetic $ -> visitor.synthetic((TypeMetadata.Entity<EntityNature.Synthetic>) this,
                                                               (EntityNature.Synthetic.Data) data);
            case EntityNature.Union $ -> visitor.union((TypeMetadata.Entity<EntityNature.Union>) this,
                                                       (EntityNature.Union.Data) data,
                                                       (Class<? extends AbstractUnionEntity>) javaType);
            case EntityNature.Other $ -> visitor.otherwise(this);
        };
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this
               || obj instanceof EntityMetadataImpl<?> that
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

    /**
     * <b>NOTE</b>: This builder is <b>mutable</b>.
     */
    static final class Builder<N extends EntityNature> {
        private final Class<? extends AbstractEntity<?>> javaType;
        private final N nature;
        private final EntityNature.Data<N> data;
        private final Collection<PropertyMetadata> properties = new ArrayList<>();

        Builder(final Class<? extends AbstractEntity<?>> javaType, final N nature, final EntityNature.Data<N> data) {
            this.javaType = javaType;
            this.nature = nature;
            this.data = data;
        }

        public TypeMetadata.Entity<N> build() {
            return new EntityMetadataImpl<>(this);
        }

        public Builder<N> properties(final Iterable<? extends PropertyMetadata> properties) {
            properties.forEach(this.properties::add);
            return this;
        }

        public Builder<N> properties(final PropertyMetadata... properties) {
            Collections.addAll(this.properties, properties);
            return this;
        }

        static Builder<EntityNature.Persistent> persistentEntity(final Class<? extends AbstractEntity<?>> type,
                                                                 final EntityNature.Persistent.Data data) {
            return new Builder<>(type, EntityNature.PERSISTENT, data);
        }

        static Builder<EntityNature.Synthetic> syntheticEntity(final Class<? extends AbstractEntity<?>> type,
                                                               final EntityNature.Synthetic.Data data) {
            return new Builder<>(type, EntityNature.SYNTHETIC, data);
        }

        static Builder<EntityNature.Union> unionEntity(final Class<? extends AbstractUnionEntity> type,
                                                       final EntityNature.Union.Data data) {
            return new Builder<>(type, EntityNature.UNION, data);
        }

        static Builder<EntityNature.Other> otherEntity(final Class<? extends AbstractEntity<?>> type) {
            return new Builder<>(type, EntityNature.OTHER, EntityNature.Other.NO_DATA);
        }
    }

}
