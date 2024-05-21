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
    private final Map<String, PropertyMetadata> properties;

    EntityMetadataImpl(final EntityMetadataBuilder<N, D> builder) {
        this.javaType = builder.getJavaType();
        this.nature = builder.getNature();
        this.data = builder.getData();
        this.properties = builder.getProperties().stream()
                .collect(toImmutableMap(PropertyMetadata::name, identity()));
    }

    public Class<? extends AbstractEntity<?>> javaType() {
        return javaType;
    }

    public Collection<PropertyMetadata> properties() {
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
        Persistent(final EntityMetadataBuilder<EntityNature.Persistent, EntityNature.Persistent.Data> builder) {
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
        Synthetic(final EntityMetadataBuilder<EntityNature.Synthetic, EntityNature.Synthetic.Data> builder) {
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
        Union(final EntityMetadataBuilder<EntityNature.Union, EntityNature.Union.Data> builder) {
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
        Other(final EntityMetadataBuilder<EntityNature.Other, EntityNature.Other.Data> builder) {
            super(builder);
        }

        @Override
        public <R> R match(final EntityMetadataVisitor<R> visitor) {
            return visitor.otherwise(this);
        }
    }

}
