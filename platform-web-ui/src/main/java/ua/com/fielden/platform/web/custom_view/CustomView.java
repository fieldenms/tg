package ua.com.fielden.platform.web.custom_view;

import ua.com.fielden.platform.web.interfaces.IRenderable;

/**
 * Base class for all custom views. All custom view should have unique name.
 *
 * @author TG Team
 *
 */
public abstract class CustomView {

    /**
     * Unique name for custom view.
     */
    private final String viewName;

    /**
     * Initialises custom view with it's unique name
     *
     * @param viewName
     */
    public CustomView(final String viewName) {
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
     * Returns the renderable representation for this custom view.
     *
     * @return
     */
    public abstract IRenderable build();
}
