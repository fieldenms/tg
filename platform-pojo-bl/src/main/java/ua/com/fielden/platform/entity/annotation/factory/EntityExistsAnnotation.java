package ua.com.fielden.platform.entity.annotation.factory;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.validation.annotation.EntityExists;

/**
 * A factory for convenient instantiation of {@link EntityExists} annotations, which mainly should be used for dynamic property or validator creation.
 *
 * @author TG Team
 *
 */
public class EntityExistsAnnotation {

    private final Class<? extends AbstractEntity<?>> value;
    private final String errorMsg;

    public EntityExistsAnnotation(final Class<? extends AbstractEntity<?>> value) {
        this(value, "");
    }

    public EntityExistsAnnotation(final Class<? extends AbstractEntity<?>> value, final String errorMsg) {
        this.value = value;
        this.errorMsg = errorMsg;
    }

    public EntityExists newInstance() {
        return new EntityExists() {

            @Override
            public Class<EntityExists> annotationType() {
                return EntityExists.class;
            }

            @Override
            public Class<? extends AbstractEntity<?>> value() {
                return value;
            }

            @Override
            public String errorMsg() {
                return errorMsg;
            }

        };
    }

}
