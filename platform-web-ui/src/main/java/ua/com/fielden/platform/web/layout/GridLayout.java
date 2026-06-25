package ua.com.fielden.platform.web.layout;

import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.minijs.JsCode;

/// The device-sensitive layout that maps a fluent Java API onto CSS Grid, rendered by the `tg-grid-layout` client component.
///
/// It is the grid-based counterpart of [FlexLayout] and reuses the same per-breakpoint machinery ([#whenMedia]).
/// A per-breakpoint layout is supplied as the wire string produced by the fluent grid API (see `GridLayoutBuilder.grid()`).
///
public class GridLayout extends AbstractLayout<AbstractLayoutSetter<GridLayout>> {
    private final String gridLayoutPath = "layout/tg-grid-layout";
    private final String name;

    /// Constructs a [GridLayout] with a `name` that provides uniqueness within the source file it is generated into.
    ///
    public GridLayout(final String name) {
        this.name = name;
    }

    @Override
    public DomElement render() {
        final DomElement gridElement = new DomElement("tg-grid-layout").clazz("tg-layout");
        for (final Pair<Device, Orientation> layout : layouts.keySet()) {
            if (layout.getValue() == null) {
                gridElement.attr("when-" + layout.getKey().toString(), "[[_" + layout.getKey().toString() + "Layout_" + name + "]]");
            }
        }
        return gridElement;
    }

    @Override
    public String importPath() {
        return gridLayoutPath;
    }

    @Override
    protected AbstractLayoutSetter<GridLayout> createLayoutSetter() {
        return new AbstractLayoutSetter<GridLayout>(this);
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