package ua.com.fielden.platform.web.centre.api.insertion_points;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A contract to make insertion point flexible (resizable).
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IInsertionPointsFlexible <T extends AbstractEntity<?>> extends IInsertionPoints<T>{

    /**
     * Makes insertion point flexible (resizable).
     * This will fit insertion point to all visible area.
     * <p>
     * Otherwise, preferred dimensions should be added to insertion point action to define its size.
     *
     * @return
     */
    IInsertionPoints<T> flex();
}
