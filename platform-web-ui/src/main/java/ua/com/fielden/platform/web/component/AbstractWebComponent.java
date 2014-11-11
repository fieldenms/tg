package ua.com.fielden.platform.web.component;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.web.interfaces.IRenderable;

/**
 * Represents the html5 component.
 *
 * @author TG Team
 *
 */
public abstract class AbstractWebComponent implements IRenderable {

    /**
     * Returns the list of html components on which this component depends on.
     *
     * @return
     */
    public List<String> imports() {
	return new ArrayList<>();
    }
}
