package ua.com.fielden.platform.web.centre.api.insertion_points;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A contract to make insertion point as preferred.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IInsertionPointPreferred<T extends AbstractEntity<?>> extends IInsertionPointWithToolbar<T> {

    /**
     * Makes this insertion point as preferred.
     *
     * @return
     */
    IInsertionPointWithToolbar<T> makePreferred();
}
