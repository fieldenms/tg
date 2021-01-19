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
public interface IResultSetBuilder1bCheckbox<T extends AbstractEntity<?>> extends IResultSetBuilder1cToolbar<T> {

	IResultSetBuilder1cToolbar<T> hideCheckboxes();

}
