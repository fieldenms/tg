package ua.com.fielden.platform.web.layout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.component.AbstractWebComponent;
import ua.com.fielden.platform.web.interfaces.ILayout;

/**
 * The layout that is sensitive to device size. And provides ability to specify layout for each device and device orientation.
 *
 * @author TG Team
 *
 */
public class FlexLayout implements ILayout {

    /**
     * Helper interface, it just hides the device sensitive layout API. Introduced in order to provide chaining API.
     *
     * @author TG Team
     *
     */
    public static interface ILayoutSetter {
        FlexLayout set(final String layout);
    }

    /**
     * Components to layout.
     */
    private final List<AbstractWebComponent> components = new ArrayList<>();

    /**
     * Map of available layouts.
     */
    private final Map<Pair<Device, Orientation>, LayoutWrapper> layouts = new HashMap<>();

    /**
     * Specifies the device and orientation for which the specific layout must be set.
     *
     * @param device
     * @param orientation
     * @return
     */
    public ILayoutSetter whenMedia(final Device device, final Orientation orientation) {
        return getLayout(device, orientation);
    }

    /**
     * Specifies the device for which the specific layout must be set.
     *
     * @param device
     * @return
     */
    public ILayoutSetter whenMedia(final Device device) {
        return getLayout(device, null);
    }

    /**
     * Returns the {@link LayoutWrapper} instance for the specified {@link Device} and {@link Orientation}.
     *
     * @param device
     * @param orientation
     * @return
     */
    private LayoutWrapper getLayout(final Device device, final Orientation orientation) {
        if (device == null) {
            throw new NullPointerException("The layout device can not be null");
        }
        LayoutWrapper layoutDefiner = layouts.get(new Pair<>(device, orientation));
        if (layoutDefiner == null) {
            layoutDefiner = new LayoutWrapper();
            layouts.put(new Pair<>(device, orientation), layoutDefiner);
        }
        return layoutDefiner;
    }

    /**
     * The internal {@link ILayoutSetter} implementation.
     *
     * @author TG Team
     *
     */
    private class LayoutWrapper implements ILayoutSetter {
        public String layout;

        @Override
        public FlexLayout set(final String layout) {
            this.layout = layout;
            return FlexLayout.this;
        }
    }

    @Override
    public DomElement render() {
        final DomElement flexElement = new DomElement("tg-flex-layout");
        for (final Pair<Device, Orientation> layout : layouts.keySet()) {
            if (layout.getValue() == null) {
                flexElement.attr("when" + layout.getKey().toString(), layouts.get(layout).layout);
            }
        }
        for (final AbstractWebComponent component : components) {
            flexElement.add(component.render());
        }
        return flexElement;
    }

    @Override
    public ILayout add(final AbstractWebComponent component) {
        this.components.add(component);
        return this;
    }
}
