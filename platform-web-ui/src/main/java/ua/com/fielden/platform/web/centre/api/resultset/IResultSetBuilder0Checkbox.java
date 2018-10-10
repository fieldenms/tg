package ua.com.fielden.platform.web.centre.api.resultset;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 *
 * Provides a convenient abstraction for controlling visiblity of the checkboxes to the left of the result set,
 * as well as for the header.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IResultSetBuilder0Checkbox<T extends AbstractEntity<?>> extends IResultSetBuilder1Toolbar<T> {

	IResultSetBuilder1Toolbar<T> hideCheckboxes();

}
