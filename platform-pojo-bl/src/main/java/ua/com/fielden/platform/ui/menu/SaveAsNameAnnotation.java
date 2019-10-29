package ua.com.fielden.platform.ui.menu;

/**
 * A factory for convenient instantiation of {@link SaveAsName} annotations.
 *
 * @author TG Team
 *
 */
public class SaveAsNameAnnotation {
    
    public SaveAsName newInstance(final String saveAsName) {
        return new SaveAsName() {
            
            @Override
            public Class<SaveAsName> annotationType() {
                return SaveAsName.class;
            }
            
            @Override
            public String value() {
                return saveAsName;
            }
            
        };
    }
    
}
