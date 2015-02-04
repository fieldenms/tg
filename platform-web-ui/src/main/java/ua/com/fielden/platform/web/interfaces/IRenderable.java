package ua.com.fielden.platform.web.interfaces;

import ua.com.fielden.platform.dom.DomElement;

/**
 * A contract for any kind of HTML enabled component such as web component (could be a Polymer component), web application, layout etc. that is responsible for generating
 * (rendering) a corresponding valid HTML code for rendering in a web browser.
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
    DomElement render();
}
