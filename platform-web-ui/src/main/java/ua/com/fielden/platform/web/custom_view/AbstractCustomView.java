package ua.com.fielden.platform.web.custom_view;

import ua.com.fielden.platform.web.interfaces.IRenderable;

/**
 * Base class for all custom views. All custom view should have unique names.
 *
 * @author TG Team
 *
 */
public abstract class AbstractCustomView {

    /**
     * Unique name for custom view.
     */
    private final String viewName;

    /**
     * Initialises custom view with it's unique name
     *
     * @param viewName
     */
    public AbstractCustomView(final String viewName) {
        this.viewName = viewName;
    }

    /**
     * Returns the unique view name.
     *
     * @return
     */
    public String getViewName() {
        return viewName;
    }

    /**
     * Should be implemented by descendants to return a renderable representation for a specific view.
     *
     * @return
     */
    public abstract IRenderable build();
}
