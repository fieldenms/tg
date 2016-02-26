package ua.com.fielden.platform.swing.menu;


/**
 * A factory for convenient instantiation of {@link MiType} annotations.
 *
 * @author TG Team
 *
 */
public class MiTypeAnnotation {

    public MiTypeAnnotation() {
    }

    public MiType newInstance(final Class<? extends MiWithConfigurationSupport<?>> miType) {
        return new MiType() {

            @Override
            public Class<MiType> annotationType() {
                return MiType.class;
            }

            @Override
            public Class<? extends MiWithConfigurationSupport<?>> value() {
                return miType;
            }
        };
    }
}
