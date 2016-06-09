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
public interface IResultSetBuilder1Toolbar<T extends AbstractEntity<?>> extends IResultSetBuilder1aScroll<T> {

    /**
     * Provides custom toolbar configuration instead of standard one.
     *
     * @param toolbar
     * @return
     */
    IResultSetBuilder1aScroll<T> setToolbar(IToolbarConfig toolbar);

	/**
	 * TODO Should potentially be expanded to support hiding of the individual standard parts of EGI toolbar
	 * @return
	 */
    IResultSetBuilder1aScroll<T> hideToolbar();

}
