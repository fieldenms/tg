package ua.com.fielden.platform.web.view.master.api.widgets.multilinetext;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A contract that allows one to specify the maximum visible row number.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IMultilineTextConfigWithRowNumber<T extends AbstractEntity<?>> extends IMultilineTextConfig1<T> {

    /**
     * Specifies the maximum visible rows for multiline text editor. If the maxRows parameter is 0 then multiline text area will grow to it's content size.
     *
     * @param maxRows
     * @return
     */
    IMultilineTextConfig1<T> withMaxVisibleRows(int maxRows);
}
