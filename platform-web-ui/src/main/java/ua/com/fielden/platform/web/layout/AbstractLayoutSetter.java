package ua.com.fielden.platform.web.layout;

import ua.com.fielden.platform.web.interfaces.ILayoutSetter;

public class AbstractLayoutSetter<T extends AbstractLayout<?>> implements ILayoutSetter<T> {

    private final T layoutManager;
    private String layout;

    public AbstractLayoutSetter(final T layoutManager) {
        this.layoutManager = layoutManager;
    }

    @Override
    public T set(final String layout) {
        this.layout = layout;
        return layoutManager;
    }

    /**
     * Returns the layout spec.
     *
     * @return
     */
    public String get() {
        return layout;
    }
}
