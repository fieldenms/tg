package ua.com.fielden.platform.web.layout;

import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.interfaces.IExecutable;
import ua.com.fielden.platform.web.interfaces.IImportable;
import ua.com.fielden.platform.web.minijs.JsCode;

/**
 * The layout that is sensitive to device size. And provides ability to specify layout for each device and device orientation.
 *
 * @author TG Team
 *
 */
public class FlexLayout extends AbstractLayout<AbstractLayoutSetter<FlexLayout>> implements IImportable, IExecutable {
    private final String flexLayoutPath = "layout/tg-flex-layout";
    private final String name;
    
    /**
     * Constructs {@link FlexLayout} instance with <code>name</code> providing uniqueness inside the same source file where it is generated into.
     * 
     * @param name
     */
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
