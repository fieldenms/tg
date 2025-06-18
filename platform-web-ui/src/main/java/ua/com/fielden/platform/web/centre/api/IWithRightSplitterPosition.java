package ua.com.fielden.platform.web.centre.api;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.insertion_points.IInsertionPointsWithCustomLayout;

/**
 * A contract for defining right splitter position within entity centre view.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IWithRightSplitterPosition<T extends AbstractEntity<?>> extends IInsertionPointsWithCustomLayout<T> {

    /**
     * Specifies the position of the right splitter of entity centre. The value is a percentage of total entity centre width from the right border of centre.
     * The value should be between 0 and 100.
     * If the value is less than 0 or greater than 100 the exception will be thrown.
     * Also this method throws error if right splitter overlaps left one (i.e. left splitter position + right splitter position > 100).
     *
     * @param percentage
     * @return
     */
    IInsertionPointsWithCustomLayout<T> withRightSplitterPosition(int percentage);
}
