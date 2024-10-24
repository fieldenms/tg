package ua.com.fielden.platform.web.view.master.api.widgets.richtext;

import ua.com.fielden.platform.entity.AbstractEntity;

public interface IRichTextConfigWithHeight<T extends AbstractEntity<?>> extends IRichTextConfigWithMinHeight<T> {

    IRichTextConfigWithMinHeight<T> withHeight(int height);
}
