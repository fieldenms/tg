package ua.com.fielden.platform.web.interfaces;

import ua.com.fielden.platform.dom.DomElement;

/**
 * The contract for any kind of html5 enabled component like web component, web application, layout etc.
 *
 * @author TG Team
 *
 */
public interface IRenderable {

    /**
     * Returns the html5 string or template that represents this web entity (i.e. web component, web application, layout etc.)
     *
     * @return
     */
    DomElement render(DomElement parent);
}
