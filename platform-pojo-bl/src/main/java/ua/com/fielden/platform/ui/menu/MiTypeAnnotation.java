package ua.com.fielden.platform.ui.menu;

import java.util.Optional;

/**
 * A factory for convenient instantiation of {@link MiType} annotations.
 *
 * @author TG Team
 *
 */
public class MiTypeAnnotation {
    
    public MiType newInstance(final Class<? extends MiWithConfigurationSupport<?>> miType, final Optional<String> saveAsName) {
        return new MiType() {
            
            @Override
            public Class<MiType> annotationType() {
                return MiType.class;
            }
            
            @Override
            public Class<? extends MiWithConfigurationSupport<?>> value() {
                return miType;
            }
            
            @Override
            public String saveAsName() {
                return saveAsName.orElse("");
            }
            
        };
    }
    
}
