package ua.com.fielden.platform.web.view.master.api.widgets.richtext;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A contract to specify height of rich text editor.
 *
 * @param <T>
 *
 * @author  TG Team
 */
public interface IRichTextConfigWithHeight<T extends AbstractEntity<?>> extends IRichTextConfigWithMinHeight<T> {

    /**
     * Sets the height of rich text editor.
     *
     * @param height
     * @return
     */
    IRichTextConfigWithMinHeight<T> withHeight(int height);
}
