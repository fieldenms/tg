package ua.com.fielden.platform.web.layout;

import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.interfaces.IImportable;

/**
 * The layout that is sensitive to device size. And provides ability to specify layout for each device and device orientation.
 *
 * @author TG Team
 *
 */
public class FlexLayout extends AbstractLayout<AbstractLayoutSetter<FlexLayout>> implements IImportable {
    private final String flexLayoutPath = "layout/tg-flex-layout";

    @Override
    public DomElement render() {
        final DomElement flexElement = new DomElement("tg-flex-layout");
        for (final Pair<Device, Orientation> layout : layouts.keySet()) {
            if (layout.getValue() == null) {
                flexElement.attr("when" + layout.getKey().toString(), "{{" + layouts.get(layout).get() + "}}");
            }
        }
        return flexElement;
    }

    @Override
    public String importPath() {
        return flexLayoutPath;
    }

    @Override
    protected AbstractLayoutSetter<FlexLayout> createLayoutSetter() {
        return new AbstractLayoutSetter<FlexLayout>(this);
    }
}
