package ua.com.fielden.platform.web.view.master.api.widgets.richtext;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.helpers.ISkipValidation;

public interface IRichTextConfigWithMinHeight<T extends AbstractEntity<?>> extends IRichTextConfig1<T> {

    IRichTextConfig1<T> withMinHeight(int minHeight);
}
