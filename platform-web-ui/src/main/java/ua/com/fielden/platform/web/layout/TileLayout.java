package ua.com.fielden.platform.web.layout;

import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.minijs.JsCode;

public class TileLayout extends AbstractLayout<AbstractLayoutSetter<TileLayout>> {
    private final String tileLayoutPath = "layout/tg-tile-layout";

    private int minCellWidth = 100, minCellHeight = 100;

    @Override
    public DomElement render() {
        final DomElement tileElement = new DomElement("tg-tile-layout");
        tileElement.attr("minCellHeight", Integer.toString(minCellHeight) + "px");
        tileElement.attr("minCellWidth", Integer.toString(minCellWidth) + "px");
        for (final Pair<Device, Orientation> layout : layouts.keySet()) {
            if (layout.getValue() == null) {
                tileElement.attr("when" + layout.getKey().toString(), "{{" + layouts.get(layout).get() + "}}");
            }
        }
        return tileElement;
    }

    public TileLayout minCellWidth(final int minCellWidth) {
        this.minCellWidth = minCellWidth;
        return this;
    }

    public TileLayout minCellHeight(final int minCellHeight) {
        this.minCellHeight = minCellHeight;
        return this;
    }

    @Override
    public String importPath() {
        return tileLayoutPath;
    }

    /// The tile layout inlines its per-breakpoint values directly into the rendered element via data-binding, so it emits no separate JavaScript assignment.
    ///
    @Override
    public JsCode code() {
        return new JsCode("");
    }

    @Override
    protected AbstractLayoutSetter<TileLayout> createLayoutSetter() {
        return new AbstractLayoutSetter<TileLayout>(this);
    }

    public int getMinCellWidth() {
        return minCellWidth;
    }

    public int getMinCellHeight() {
        return minCellHeight;
    }
}
