package ua.com.fielden.platform.web.centre.api.resultset;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
*
* Provides a convenient abstraction for specifying the number of text lines in EGI header.
*
* @author TG Team
*
* @param <T>
*/
public interface IResultSetBuilder1hHeaderWrap<T extends AbstractEntity<?>> extends IResultSetBuilder1iVisibleRowsCount<T> {

    /**
     * Set the number of text lines in EGI header.
     *
     * @param headerLineNumber
     * @return
     */
    IResultSetBuilder1iVisibleRowsCount<T> wrapHeader(int headerLineNumber);
}
