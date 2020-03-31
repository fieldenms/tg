package ua.com.fielden.platform.web.centre.api.resultset;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
*
* Provides a convenient abstraction for specifying the maximal number of text lines in EGI header.
*
* @author TG Team
*
* @param <T>
*/
public interface IResultSetBuilder1cMaxHeaderLines<T extends AbstractEntity<?>> extends IResultSetBuilder1cHeaderLines<T> {

    /**
     * Set the maximal number of text lines in EGI header.
     *
     * @param headerLineNumber
     * @return
     */
    IResultSetBuilder1cHeaderLines<T> setMaxHeaderLineNumber(int headerLineNumber);
}
