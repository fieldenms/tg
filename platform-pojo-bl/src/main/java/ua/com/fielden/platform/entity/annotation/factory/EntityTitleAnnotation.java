package ua.com.fielden.platform.entity.annotation.factory;

import ua.com.fielden.platform.entity.annotation.EntityTitle;

/// A factory for convenient instantiation of [EntityTitle] annotations, which mainly should be used for dynamic class generation.
///
public class EntityTitleAnnotation {
    private final String value;
    private final String desc;

    public EntityTitleAnnotation(final String value, final String desc) {
        this.value = value;
        this.desc = desc;
    }

    public EntityTitle newInstance() {
        return new EntityTitle() {

            @Override
            public Class<EntityTitle> annotationType() {
                return EntityTitle.class;
            }

            @Override
            public String value() {
                return value;
            }

            @Override
            public String desc() {
                return desc;
            }
        };
    }

}
