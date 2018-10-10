package ua.com.fielden.platform.web.view.master.api.widgets.collectional;

import ua.com.fielden.platform.entity.AbstractEntity;

public interface ICollectionalEditorConfig2<T extends AbstractEntity<?>> extends ICollectionalEditorConfig3<T> {
    
    ICollectionalEditorConfig3<T> withDescription(final String descriptionPropertyName);
    
}
