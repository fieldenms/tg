package ua.com.fielden.platform.web.centre.api.insertion_points;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A contract to make insertion point as preferred or not resizable.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IInsertionPointConfig0<T extends AbstractEntity<?>> extends IInsertionPointWithToolbar<T> {

    /**
     * Makes this insertion point as preferred.
     *
     * @return
     */
    IInsertionPointWithToolbar<T> makePreferred();

    /**
     * Disallows user to change insertion point height
     *
     * @return
     */
    IInsertionPointWithToolbar<T> noResizing();
}
