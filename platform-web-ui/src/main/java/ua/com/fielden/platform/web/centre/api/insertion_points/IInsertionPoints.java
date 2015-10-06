package ua.com.fielden.platform.web.centre.api.insertion_points;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.IEcbCompletion;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;

/**
 * A contract for defining entity centre insertion points, which as are driven by functional entities.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IInsertionPoints<T extends AbstractEntity<?>> extends IEcbCompletion<T> {
    /**
     * Associates the specified action with an insertion point.
     * Several actions can be associated with the same insertion point, where the order of associations determines the order of insertions.
     *
     * @param actionConfig
     * @param whereToInsertView
     * @return
     */
    IInsertionPoints<T> addInsertionPoint(final EntityActionConfig actionConfig, final InsertionPoints whereToInsertView);
}
