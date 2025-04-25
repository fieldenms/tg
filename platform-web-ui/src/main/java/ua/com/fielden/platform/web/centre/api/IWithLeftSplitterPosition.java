package ua.com.fielden.platform.web.centre.api;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A contract for defining left splitter position within entity centre view.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IWithLeftSplitterPosition<T extends AbstractEntity<?>> extends IWithRightSplitterPosition<T> {

    /**
     * Specifies the position of the left splitter of entity centre. The value is a percentage of total entity centre width from the left border of centre.
     * The value should be between 0 and 100.
     * If the value is less than 0 or greater than 100 the exception will be thrown.
     *
     * @param percentage
     * @return
     */
    IWithRightSplitterPosition<T> withLeftSplitterPosition(int percentage);
}
