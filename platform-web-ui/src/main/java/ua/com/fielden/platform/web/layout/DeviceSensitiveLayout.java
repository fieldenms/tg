package ua.com.fielden.platform.web.layout;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.web.interfaces.ILayout;
import ua.com.fielden.platform.web.interfaces.IRenderable;

/**
 * The layout that is sensitive to device size. And provides ability to specify layout for each device and device orientation.
 *
 * @author TG Team
 *
 */
public class DeviceSensitiveLayout implements IRenderable{

    /**
     * Represents the list of supported devices.
     *
     * @author TG Team
     *
     */
    public enum Device {
	DESKTOP,
	TABLET,
	PHONE,
	PRINT;
	@Override
	public String toString() {
	    return name().toLowerCase();
	};
    }

    /**
     * Represents the device orientation.
     *
     * @author TG Team
     *
     */
    public enum Orientation {
	LANDSCAPE,
	PORTRAIT;
	@Override
	public String toString() {
	    return name().toLowerCase();
	};
    }

    /**
     * Helper interface, it just hides the device sensitive layout API. Introduced in order to provide chaining API.
     *
     * @author TG Team
     *
     */
    public static interface ILayoutSetter {
	DeviceSensitiveLayout set(ILayout layout);
    }

    /**
     * Map of available layouts.
     */
    private final Map<Map.Entry<Device, Orientation>, LayoutWrapper> layouts = new HashMap<>();

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
	    throw new NullPointerException("The device of devecie sensitive layout can not be null");
	}
	LayoutWrapper layoutDefiner = layouts.get(new AbstractMap.SimpleEntry<>(device, orientation));
	if (layoutDefiner == null) {
	    layoutDefiner = new LayoutWrapper();
	    layouts.put(new AbstractMap.SimpleEntry<>(device, orientation), layoutDefiner);
	}
	return layoutDefiner;
    }

    /**
     * The internal {@link ILayoutSetter} implementation.
     *
     * @author TG Team
     *
     */
    private class LayoutWrapper implements ILayoutSetter{
	public ILayout layout;

	@Override
	public DeviceSensitiveLayout set(final ILayout layout) {
	    this.layout = layout;
	    return DeviceSensitiveLayout.this;
	}
    }

    @Override
    public DomElement render() {
	//TODO implement this.
	return null;
    }
}
