package ua.com.fielden.platform.web.centre.api.insertion_points;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.IEcbCompletion;

/**
 * A contract to enable rearrangement of insertion points on entity centre.
 *
 * @param <T>
 */
public interface IEnableInsertionPointRearrangement<T extends AbstractEntity<?>> extends IEcbCompletion<T> {

    /**
     * Enables the insertion point rearrangement.
     *
     * @return
     */
    IEcbCompletion<T> withCustomisableLayout();
}
