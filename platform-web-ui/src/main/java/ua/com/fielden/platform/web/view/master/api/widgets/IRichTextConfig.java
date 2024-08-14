package ua.com.fielden.platform.web.view.master.api.widgets;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.helpers.ISkipValidation;
import ua.com.fielden.platform.web.view.master.api.widgets.richtext.IRichTextConfig0;

public interface IRichTextConfig<T extends AbstractEntity<?>> extends IRichTextConfig0<T>, ISkipValidation<IRichTextConfig0<T>> {
}
