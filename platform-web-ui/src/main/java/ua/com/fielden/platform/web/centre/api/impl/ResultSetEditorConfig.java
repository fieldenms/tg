package ua.com.fielden.platform.web.centre.api.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.resultset.IResultSetBuilder3Ordering;
import ua.com.fielden.platform.web.centre.api.resultset.IResultSetEditorConfig;
import ua.com.fielden.platform.web.centre.api.resultset.IResultSetPropertyActionConfig;
import ua.com.fielden.platform.web.view.master.api.widgets.impl.AbstractWidget;

public class ResultSetEditorConfig<T extends AbstractEntity<?>> extends ResultSetBuilderWrapperForEditors<T> implements IResultSetEditorConfig<T> {

    private final AbstractWidget widget;

    public ResultSetEditorConfig(final ResultSetBuilder<T> builder, final AbstractWidget widget) {
        super(builder);
        this.widget = widget;
    }

    @Override
    public IResultSetPropertyActionConfig<T> skipValidation() {
        widget.skipValidation();
        return this;
    }

    @Override
    public IResultSetBuilder3Ordering<T> withEditorAction(final EntityActionConfig actionConfig) {
        widget.withAction(actionConfig);
        return builder;
    }

}
