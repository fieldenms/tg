package ua.com.fielden.platform.web.view.master.api.widgets.impl;

import static java.lang.String.format;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.helpers.IPropertySelector;
import ua.com.fielden.platform.web.view.master.api.widgets.ISinglelineTextConfig;
import ua.com.fielden.platform.web.view.master.api.widgets.singlelinetext.ISinglelineTextConfig0;
import ua.com.fielden.platform.web.view.master.api.widgets.singlelinetext.ISinglelineTextConfig1;
import ua.com.fielden.platform.web.view.master.api.widgets.singlelinetext.impl.SinglelineTextWidget;

public class SinglelineTextConfig<T extends AbstractEntity<?>>
        extends AbstractEditorWidgetConfig<T, SinglelineTextWidget, ISinglelineTextConfig1<T>>
        implements ISinglelineTextConfig<T>, ISinglelineTextConfig0<T>, ISinglelineTextConfig1<T> {

    private static final String AUTO_COMMIT_MILLIS_ARE = "Auto-commit milliseconds [%s] are %s";
    private static final String LESS_THAN_ZERO = "less than zero.";
    private static final String GREATER_THAN_10 = "greater than 10 seconds. This is not practical.";
    private static final String TOO_SMALL = "too small. At least 10 milliseconds are required.";

    public SinglelineTextConfig(final SinglelineTextWidget widget, final IPropertySelector<T> propSelector) {
        super(widget, propSelector);
    }

    @Override
    public ISinglelineTextConfig1<T> skipValidation() {
        skipVal();
        return this;
    }

    @Override
    public ISinglelineTextConfig0<T> autoCommit(final int millis) {
        if (millis < 10 || millis > 10000) {
            throw new MasterWidgetException(format(
                AUTO_COMMIT_MILLIS_ARE,
                millis,
                millis < 0 ? LESS_THAN_ZERO
                    : millis > 10000 ? GREATER_THAN_10
                    : TOO_SMALL
            ));
        }
        widget().autoCommit(millis);
        return this;
    }

}