package ua.com.fielden.platform.web.view.master.api.widgets.collectional;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.processors.metamodel.IConvertableToPath;

public interface ICollectionalEditorConfig2<T extends AbstractEntity<?>> extends ICollectionalEditorConfig3<T> {
    
    ICollectionalEditorConfig3<T> withDescription(final String descriptionPropertyName);
    
    default ICollectionalEditorConfig3<T> withDescription(final IConvertableToPath descriptionPropertyName) {
        return withDescription(descriptionPropertyName.toPath());
    }
    
}
