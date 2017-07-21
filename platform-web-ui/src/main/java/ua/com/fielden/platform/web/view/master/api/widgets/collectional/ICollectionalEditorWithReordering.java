package ua.com.fielden.platform.web.view.master.api.widgets.collectional;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A contract that allows one to mark this collectional editor with a reordering capabilities.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface ICollectionalEditorWithReordering<T extends AbstractEntity<?>> extends ICollectionalEditorConfig0<T> {

    /**
     * Marks this colletional editor with reordering capabilities.
     *
     * @return
     */
    ICollectionalEditorConfig0<T> reorderable();
}
