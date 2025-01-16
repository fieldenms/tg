package ua.com.fielden.platform.web.view.master.api.widgets.richtext;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.helpers.ISkipValidation;

/**
 * A contract to specify minimal height of rich text editor.
 *
 * @param <T>
 *
 * @author  TG Team
 */
public interface IRichTextConfigWithMinHeight<T extends AbstractEntity<?>> extends IRichTextConfig1<T> {

    /**
     * Sets the minimal height of rich text editor.
     *
     * @param minHeight
     * @return
     */
    IRichTextConfig1<T> withMinHeight(int minHeight);
}
