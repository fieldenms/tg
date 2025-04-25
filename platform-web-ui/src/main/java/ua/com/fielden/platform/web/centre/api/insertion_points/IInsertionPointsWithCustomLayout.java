package ua.com.fielden.platform.web.centre.api.insertion_points;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.alternative_view.IAlternativeView;

/**
 * A contract to enable a custom layout for insertion points in the entity centre.
 *
 * @param <T>
 */
public interface IInsertionPointsWithCustomLayout<T extends AbstractEntity<?>> extends IAlternativeView<T> {

    /**
     * Enables a custom layout for insertion points. This option allows users to drag and drop insertion points, thereby changing their arrangement.
     *
     * @return
     */
    IAlternativeView<T> withCustomisableLayout();
}
