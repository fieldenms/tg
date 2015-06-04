package ua.com.fielden.platform.web.layout;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.interfaces.ILayout;
import ua.com.fielden.platform.web.interfaces.IRenderable;

/**
 * The web layout manager contract.
 *
 * @author TG Team
 *
 */
public abstract class AbstractLayout<T extends AbstractLayoutSetter<?>> implements ILayout<T>, IRenderable {

    /**
     * Map of available layouts.
     */
    protected final Map<Pair<Device, Orientation>, T> layouts = new HashMap<>();

    @Override
    public T whenMedia(final Device device, final Orientation orientation) {
        return getLayout(device, orientation);
    }

    @Override
    public T whenMedia(final Device device) {
        return getLayout(device, null);
    }

    /**
     * Creates layout setter
     *
     * @return
     */
    protected abstract T createLayoutSetter();

    /**
     * Returns the {@link LayoutWrapper} instance for the specified {@link Device} and {@link Orientation}.
     *
     * @param device
     * @param orientation
     * @return
     */
    public T getLayout(final Device device, final Orientation orientation) {
        if (device == null) {
            throw new NullPointerException("The layout device can not be null");
        }
        T layoutDefiner = layouts.get(new Pair<>(device, orientation));
        if (layoutDefiner == null) {
            layoutDefiner = createLayoutSetter();
            layouts.put(new Pair<>(device, orientation), layoutDefiner);
        }
        return layoutDefiner;
    }

    /**
     * Identifies whether there is already a layout associated with a device and the specified orientation.
     *
     * @param device
     * @param orientation
     * @return
     */
    public boolean hasLayoutFor(final Device device, final Orientation orientation) {
        final T wrapper = layouts.get(new Pair<>(device, orientation));
        if (wrapper != null) {
            return !StringUtils.isEmpty(wrapper.get());
        }
        return false;
    }
}
