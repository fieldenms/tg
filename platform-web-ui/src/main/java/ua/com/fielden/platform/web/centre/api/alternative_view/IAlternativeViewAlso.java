package ua.com.fielden.platform.web.centre.api.alternative_view;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A concatenator for functional actions on alternative view
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IAlternativeViewAlso<T extends AbstractEntity<?>> extends IAlternativeViewWithToolbar<T> {

    /**
     * Expresses the willingness to add another top functional action to the alternative view.
     *
     * @return
     */
    IAlternativeViewWithActions<T> also();
}
