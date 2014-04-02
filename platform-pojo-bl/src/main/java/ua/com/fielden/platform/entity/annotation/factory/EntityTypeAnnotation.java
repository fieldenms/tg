package ua.com.fielden.platform.entity.annotation.factory;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

/**
 * A factory for convenient instantiation of {@link EntityType} annotations, which mainly should be used for dynamic property creation.
 * 
 * @author TG Team
 * 
 */
public class EntityTypeAnnotation {

    private final Class<? extends AbstractEntity> value;
    private Class<?>[] parameters = new Class<?>[] {};

    public EntityTypeAnnotation(final Class<? extends AbstractEntity> value) {
        this.value = value;
    }

    public EntityTypeAnnotation parameters(final Class<?>[] parameters) {
        this.parameters = parameters;
        return this;
    }

    public EntityType newInstance() {
        return new EntityType() {

            @Override
            public Class<EntityType> annotationType() {
                return EntityType.class;
            }

            @Override
            public Class<? extends AbstractEntity> value() {
                return value;
            }

            @Override
            public Class<?>[] parameters() {
                return parameters;
            }

        };
    }

    public EntityType copyFrom(final EntityType original) {
        return new EntityType() {

            @Override
            public Class<EntityType> annotationType() {
                return EntityType.class;
            }

            @Override
            public Class<? extends AbstractEntity> value() {
                return original.value();
            }

            @Override
            public Class<?>[] parameters() {
                return original.parameters();
            }
        };
    }
}
