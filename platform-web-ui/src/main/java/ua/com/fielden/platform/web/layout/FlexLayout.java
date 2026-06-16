package ua.com.fielden.platform.web.layout;

import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.minijs.JsCode;

/// The layout that is sensitive to device size, providing the ability to specify the layout for each device and orientation.
///
public class FlexLayout extends AbstractLayout<AbstractLayoutSetter<FlexLayout>> {
    private final String flexLayoutPath = "layout/tg-flex-layout";
    private final String name;

    /// Constructs a [FlexLayout] with a `name` that provides uniqueness within the source file it is generated into.
    ///
    public FlexLayout(final String name) {
        this.name = name;
    }

    @Override
    public DomElement render() {
        final DomElement flexElement = new DomElement("tg-flex-layout");
        for (final Pair<Device, Orientation> layout : layouts.keySet()) {
            if (layout.getValue() == null) {
                flexElement.attr("when-" + layout.getKey().toString(), "[[_" + layout.getKey().toString() + "Layout_" + name + "]]");
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

    @Override
    public JsCode code() {
        final StringBuilder code = new StringBuilder();
        for (final Pair<Device, Orientation> layout : layouts.keySet()) {
            if (layout.getValue() == null) {
                code.append("this._" + layout.getKey().toString() + "Layout_" + name + " = " + layouts.get(layout).get() + ";\n");
            }
        }
        return new JsCode(code.toString());
    }
}