package ua.com.fielden.platform.web.centre.api.resultset.toolbar.impl;

import ua.com.fielden.platform.dom.DomContainer;
import ua.com.fielden.platform.dom.DomElement;

public class InsertionPointToolbar extends CentreToolbar {

    @Override
    protected DomElement createToolbarElement() {
        return new DomContainer().add(pagination("standart-action"), refreshButton());
    }
}
