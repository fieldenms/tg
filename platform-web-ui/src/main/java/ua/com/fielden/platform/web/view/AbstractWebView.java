package ua.com.fielden.platform.web.view;

import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.web.WebUtils;
import ua.com.fielden.platform.web.component.WebPanel;
import ua.com.fielden.platform.web.model.WebModel;

/**
 * Represents the html5 with angular controller.
 *
 * @author TG Team
 *
 */
public abstract class AbstractWebView<WM extends WebModel> extends WebPanel {

    private final WM webModel;
    private final String name;

    public AbstractWebView(final WM webModel, final String name) {
	this.webModel = webModel;
	this.name = name;
    }

    public WM getWebModel() {
	return webModel;
    }

    public String getName() {
	return name;
    }

    @Override
    public DomElement render(final DomElement parent) {
        return super.render(parent).attr("ng-controller", WebUtils.controllerName(getWebModel()));
    }
}
