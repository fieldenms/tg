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

    public static EntityExists newInstance(final Class<? extends AbstractEntity<?>> value) {
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
                return "";
            }

        };
    }

}
