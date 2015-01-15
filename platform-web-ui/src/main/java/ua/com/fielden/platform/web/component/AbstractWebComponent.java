package ua.com.fielden.platform.web.component;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.web.interfaces.IRenderable;

/**
 * Represents a HTML5 component.
 *
 * @author TG Team
 *
 */
public abstract class AbstractWebComponent implements IRenderable {

    /**
     * Returns the list of HTML components and CSS that this component depends on.
     *
     * @return
     */
    public List<String> imports() {
        return new ArrayList<>();
    }
}
