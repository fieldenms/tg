package ua.com.fielden.platform.web.centre.api.insertion_points;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.IEcbCompletion;
import ua.com.fielden.platform.web.centre.api.alternative_view.IAlternativeView;

/**
 * A contract to enable custom layout for insertion points on entity centre.
 *
 * @param <T>
 */
public interface IInsertionPointsWithCustomLayout<T extends AbstractEntity<?>> extends IAlternativeView<T> {

    /**
     * Enables the insertion points' custom layout. This options enables user to drag'n'drop insertion points therefore changing their layout.
     *
     * @return
     */
    IAlternativeView<T> withCustomisableLayout();
}
