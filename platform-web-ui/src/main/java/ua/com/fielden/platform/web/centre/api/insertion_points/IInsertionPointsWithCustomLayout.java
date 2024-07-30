package ua.com.fielden.platform.web.centre.api.insertion_points;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.IEcbCompletion;
import ua.com.fielden.platform.web.centre.api.alternative_view.IAlternativeView;

/**
 * A contract to enable rearrangement of insertion points on entity centre via drag'n'drop process.
 *
 * @param <T>
 */
public interface IInsertionPointsWithCustomLayout<T extends AbstractEntity<?>> extends IAlternativeView<T> {

    /**
     * Enables the insertion point rearrangement via drag'n'drop process.
     *
     * @return
     */
    IAlternativeView<T> withCustomisableLayout();
}
