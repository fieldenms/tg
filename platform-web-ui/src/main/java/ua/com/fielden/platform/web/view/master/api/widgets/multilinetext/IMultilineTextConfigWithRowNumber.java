package ua.com.fielden.platform.web.view.master.api.widgets.multilinetext;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A contract that allows one to make multiline text editor flexible of to specify the maximum visible row number.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IMultilineTextConfigWithRowNumber<T extends AbstractEntity<?>> extends IMultilineTextConfig1<T> {

    /**
     * Makes multiline text editor flexible.
     *
     * @return
     */
    IMultilineTextConfig1<T> flexible();

    /**
     * Specifies the maximum visible rows for multiline text editor
     *
     * @param maxRows
     * @return
     */
    IMultilineTextConfig1<T> withMaxVisibleRows(int maxRows);
}
