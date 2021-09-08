package ua.com.fielden.platform.web.view.master.api.widgets.collectional;

import ua.com.fielden.platform.entity.AbstractEntity;

public interface ICollectionalEditorWithStaticOrder<T extends AbstractEntity<?>> extends ICollectionalEditorWithReordering<T> {

    ICollectionalEditorWithReordering<T> withStaticOrder();
}
