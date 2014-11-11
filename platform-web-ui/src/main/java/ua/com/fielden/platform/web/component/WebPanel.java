package ua.com.fielden.platform.web.component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ua.com.fielden.platform.dom.DomContainer;
import ua.com.fielden.platform.dom.DomElement;

/**
 * An abstraction for the panel that is translated into html5.
 *
 * @author TG Team
 *
 */
public class WebPanel extends AbstractWebComponent {

    protected final List<AbstractWebComponent> components = new ArrayList<>();

    @Override
    public DomElement render() {
	return new DomContainer().add(renderChildren());
    }

    public WebPanel add(final AbstractWebComponent... components) {
	this.components.addAll(Arrays.asList(components));
	return this;
    }

    private DomElement[] renderChildren() {
	final List<DomElement> children = new ArrayList<>();
	for(final AbstractWebComponent component : components) {
	    children.add(component.render());
	}
	return children.toArray(new DomElement[0]);
    }

}
