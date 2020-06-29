package ua.com.fielden.platform.web.centre.api.resultset;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.resultset.toolbar.IToolbarConfig;

/**
 *
 * Provides a convenient abstraction to specify toolbar visibility.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IResultSetBuilder1cToolbar<T extends AbstractEntity<?>> extends IResultSetBuilder1dScroll<T> {

    /**
     * Provides custom toolbar configuration instead of standard one.
     *
     * @param toolbar
     * @return
     */
    IResultSetBuilder1dScroll<T> setToolbar(IToolbarConfig toolbar);

	/**
	 * TODO Should potentially be expanded to support hiding of the individual standard parts of EGI toolbar
	 * @return
	 */
    IResultSetBuilder1dScroll<T> hideToolbar();

}
