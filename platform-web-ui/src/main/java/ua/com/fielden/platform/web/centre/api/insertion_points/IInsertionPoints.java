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
    IInsertionPointsFlexible<T> addInsertionPoint(final EntityActionConfig actionConfig, final InsertionPoints whereToInsertView);

    /**
     * The same as {@link #addInsertionPoint(EntityActionConfig, InsertionPoints)} but also adds pagination buttons to the insertion point which
     * become visible only when insertion point is in expanded mode.
     *
     * @param actionConfig
     * @param whereToInsertView
     * @return
     */
    IInsertionPointsFlexible<T> addInsertionPointWithPagination(final EntityActionConfig actionConfig, final InsertionPoints whereToInsertView);
}
