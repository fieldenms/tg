package ua.com.fielden.platform.meta;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static java.util.Collections.unmodifiableCollection;

/**
 * <b>NOTE</b>: This builder is <b>mutable</b>.
 */
abstract class EntityMetadataBuilder<N extends EntityNature, D extends EntityNature.Data<N>> {
    private final Class<? extends AbstractEntity<?>> javaType;
    private final N nature;
    private final D data;
    private final Collection<PropertyMetadata> properties = new ArrayList<>();

    EntityMetadataBuilder(final Class<? extends AbstractEntity<?>> javaType, final N nature, final D data) {
        this.javaType = javaType;
        this.nature = nature;
        this.data = data;
    }

    static EntityMetadataBuilder<?, ?> toBuilder(final EntityMetadata entityMetadata) {
        final var builder = switch (entityMetadata) {
            case EntityMetadata.Persistent it ->
                    new Persistent(it.javaType(), it.nature(), it.data());
            case EntityMetadata.Synthetic it ->
                    new Synthetic(it.javaType(), it.nature());
            case EntityMetadata.Union it ->
                    new Union(it.javaType(), it.nature(), it.data());
        };

        return builder.properties(entityMetadata.properties());
    }

    public abstract EntityMetadata build();

    public Class<? extends AbstractEntity<?>> getJavaType() {
        return javaType;
    }

    public D getData() {
        return data;
    }

    public N getNature() {
        return nature;
    }

    public Collection<PropertyMetadata> getProperties() {
        return unmodifiableCollection(properties);
    }

    public EntityMetadataBuilder<N, D> properties(final Iterable<? extends PropertyMetadata> properties) {
        properties.forEach(this.properties::add);
        return this;
    }

    public EntityMetadataBuilder<N, D> properties(final PropertyMetadata... properties) {
        Collections.addAll(this.properties, properties);
        return this;
    }

    static EntityMetadataBuilder<EntityNature.Persistent, EntityNature.Persistent.Data> persistentEntity
            (final Class<? extends AbstractEntity<?>> type, final EntityNature.Persistent.Data data) {
        return new Persistent(type, EntityNature.PERSISTENT, data);
    }

    static EntityMetadataBuilder<EntityNature.Synthetic, EntityNature.Synthetic.Data> syntheticEntity
            (final Class<? extends AbstractEntity<?>> type) {
        return new Synthetic(type, EntityNature.SYNTHETIC);
    }

    static EntityMetadataBuilder<EntityNature.Union, EntityNature.Union.Data> unionEntity
            (final Class<? extends AbstractUnionEntity> type, final EntityNature.Union.Data data) {
        return new Union(type, EntityNature.UNION, data);
    }

    static final class Persistent
            extends EntityMetadataBuilder<EntityNature.Persistent, EntityNature.Persistent.Data>
    {
        Persistent(final Class<? extends AbstractEntity<?>> javaType, final EntityNature.Persistent nature, final EntityNature.Persistent.Data data) {
            super(javaType, nature, data);
        }

        @Override
        public EntityMetadata build() {
            return new EntityMetadataImpl.Persistent(this);
        }
    }

    static final class Synthetic
            extends EntityMetadataBuilder<EntityNature.Synthetic, EntityNature.Synthetic.Data>
    {
        Synthetic(final Class<? extends AbstractEntity<?>> javaType, final EntityNature.Synthetic nature) {
            super(javaType, nature, EntityNature.Synthetic.NO_DATA);
        }

        @Override
        public EntityMetadata build() {
            return new EntityMetadataImpl.Synthetic(this);
        }
    }

    static final class Union
            extends EntityMetadataBuilder<EntityNature.Union, EntityNature.Union.Data>
    {
        Union(final Class<? extends AbstractUnionEntity> javaType, final EntityNature.Union nature, final EntityNature.Union.Data data) {
            super(javaType, nature, data);
        }

        @Override
        public Class<? extends AbstractUnionEntity> getJavaType() {
            return (Class<? extends AbstractUnionEntity>) super.getJavaType();
        }

        @Override
        public EntityMetadata build() {
            return new EntityMetadataImpl.Union(this);
        }
    }

}
