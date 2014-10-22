package ua.com.fielden.platform.web.component;

import ua.com.fielden.platform.dom.DomElement;

/**
 * An abstraction for the panel that is translated into html5.
 *
 * @author TG Team
 *
 */
public class WebPanel extends AbstractWebComponent {

    @Override
    public DomElement render(final DomElement parent) {
	final DomElement panel = new DomElement("div");
	return parent == null ? panel : parent.add(panel);
    }

}
