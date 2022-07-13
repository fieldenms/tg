package ua.com.fielden.platform.web.view.master.api.widgets.collectional;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.processors.metamodel.IConvertableToPath;

public interface ICollectionalEditorConfig1<T extends AbstractEntity<?>> extends ICollectionalEditorConfig2<T> {
    
    ICollectionalEditorConfig2<T> withHeader(final String headerPropertyName);
    
    default ICollectionalEditorConfig2<T> withHeader(final IConvertableToPath headerPropertyName) {
        return withHeader(headerPropertyName.toPath());
    }
    
}
