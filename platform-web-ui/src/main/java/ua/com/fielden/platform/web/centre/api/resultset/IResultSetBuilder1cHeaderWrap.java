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
public interface IResultSetBuilder1cHeaderWrap<T extends AbstractEntity<?>> extends IResultSetBuilder1cVisibleRowsCount<T> {

    /**
     * Set the number of text lines in EGI header.
     *
     * @param headerLineNumber
     * @return
     */
    IResultSetBuilder1cVisibleRowsCount<T> wrapHeader(int headerLineNumber);
}
