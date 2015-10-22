package ua.com.fielden.platform.web.view.master.api.widgets.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.helpers.IPropertySelector;
import ua.com.fielden.platform.web.view.master.api.widgets.IColourConfig;
import ua.com.fielden.platform.web.view.master.api.widgets.colour.IColourConfig0;
import ua.com.fielden.platform.web.view.master.api.widgets.colour.impl.ColourWidget;

public class ColourConfig<T extends AbstractEntity<?>>
        extends AbstractEditorWidgetConfig<T, ColourWidget, IColourConfig0<T>>
        implements IColourConfig<T>, IColourConfig0<T> {

    public ColourConfig(final ColourWidget widget, final IPropertySelector<T> propSelector) {
        super(widget, propSelector);
    }

    @Override
    public IColourConfig0<T> skipValidation() {
        skipVal();
        return this;
    }

}
