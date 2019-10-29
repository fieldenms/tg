package ua.com.fielden.platform.web.centre.api.resultset;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A contract that allows one to specify whether egi content get fit to it's height or not
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IResultSetBuilder1dFitBehaviour<T extends AbstractEntity<?>> extends IResultSetBuilder1eRowHeight<T> {

    /**
     * Makes egi content fit to it's height
     *
     * @return
     */
    IResultSetBuilder1eRowHeight<T> fitToHeight();
}
