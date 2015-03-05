package ua.com.fielden.platform.entity.annotation.factory;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.MasterEntityType;

/**
 * A factory for convenient instantiation of {@link MasterEntityType} annotations.
 *
 * @author TG Team
 *
 */
public class MasterEntityTypeAnnotation {

    public MasterEntityTypeAnnotation() {
    }

    public MasterEntityType newInstance(final Class<? extends AbstractEntity<?>> masterEntityType) {
        return new MasterEntityType() {

            @Override
            public Class<MasterEntityType> annotationType() {
                return MasterEntityType.class;
            }

            @Override
            public Class<? extends AbstractEntity<?>> value() {
                return masterEntityType;
            }
        };
    }
}
