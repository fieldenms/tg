package ua.com.fielden.platform.web.centre.api;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.alternative_view.IAlternativeView;

/**
 * The contract to specify the position of right splitter.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IWithRightSplitterPosition<T extends AbstractEntity<?>> extends IAlternativeView<T> {

    /**
     * Specifies the position of the right splitter of entity centre. The value is a percentage of total entity centre width from the right border of centre.
     * The value should be between 0 and 100.
     * If the value is less than 0 or greater than 100 the exception will be thrown.
     * Also this method throws error if right splitter overlaps left one (i.e. left splitter position + right splitter position > 100).
     *
     * @param percentage
     * @return
     */
    IAlternativeView<T> withRightSplitterPosition(int percentage);
}
