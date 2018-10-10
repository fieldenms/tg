package ua.com.fielden.platform.web.view.master.api.widgets;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.helpers.ISkipValidation;
import ua.com.fielden.platform.web.view.master.api.widgets.multilinetext.IMultilineTextConfig1;
import ua.com.fielden.platform.web.view.master.api.widgets.multilinetext.IMultilineTextConfigWithRowNumber;

public interface IMultilineTextConfig<T extends AbstractEntity<?>> extends IMultilineTextConfigWithRowNumber<T>, ISkipValidation<IMultilineTextConfig1<T>> {
}
