package ua.com.fielden.platform.web.view.master.api.widgets.collectional;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A contract that allows one to make this collectional editor not change the order of arrived entities.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface ICollectionalEditorWithStaticOrder<T extends AbstractEntity<?>> extends ICollectionalEditorWithReordering<T> {

    /**
     * Make this collectional editor with static order.
     *
     * @return
     */
    ICollectionalEditorWithReordering<T> withStaticOrder();
}
