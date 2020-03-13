package ua.com.fielden.platform.web.centre.api.insertion_points;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A contract to make insertion point resizable.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IInsertionPointsFlexible <T extends AbstractEntity<?>> extends IInsertionPoints<T>{

    /**
     * Makes insertion point flexible (resizable)
     *
     * @return
     */
    IInsertionPoints<T> flex();
}
