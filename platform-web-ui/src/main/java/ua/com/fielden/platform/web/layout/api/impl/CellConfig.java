package ua.com.fielden.platform.web.layout.api.impl;

import java.util.Optional;

import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.web.layout.api.IFlexContainerLayout;
import ua.com.fielden.platform.web.layout.api.IFlexLayout;

public class CellConfig {

    private final Optional<IFlexContainerLayout> optionalContainer;
    private final Optional<String> layoutWidget;

    private Optional<IFlexLayout> optionalLayout = Optional.empty();

    public CellConfig() {
        this(null, null, (String) null);
    }

    public CellConfig(final IFlexContainerLayout container) {
        this(container, null, (String) null);
    }

    public CellConfig(final IFlexLayout layout) {
        this(null, layout, (String) null);
    }

    public CellConfig(final IFlexContainerLayout container, final IFlexLayout layout) {
        this(container, layout, (String) null);
    }

    public CellConfig(final DomElement dom) {
        this(null, null, "html:" + dom.toString());
    }

    public CellConfig(final DomElement dom, final IFlexLayout layout) {
        this(null, layout, "html:" + dom.toString());
    }

    public CellConfig(final String layoutWidget) {
        this(null, null, layoutWidget);
    }

    public CellConfig(final IFlexLayout layout, final String layoutWidget) {
        this(null, layout, layoutWidget);
    }

    public void setLayoutIfNotPresent(final IFlexLayout layout) {
        if (!optionalLayout.isPresent()) {
            this.optionalLayout = Optional.ofNullable(layout);
        }
    }

    private CellConfig(final IFlexContainerLayout container, final IFlexLayout flexLayout, final String layoutWidget) {
        this.optionalContainer = Optional.ofNullable(container);
        this.optionalLayout = Optional.ofNullable(flexLayout);
        this.layoutWidget = Optional.ofNullable(layoutWidget);
    }
}
