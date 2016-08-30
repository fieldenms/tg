package ua.com.fielden.platform.web.layout.api.impl;

import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.web.layout.api.IFlexContainerLayout;
import ua.com.fielden.platform.web.layout.api.IFlexLayout;
import ua.com.fielden.platform.web.layout.api.ILayoutCell;

/**
 * The builder for flex layout configuration.
 *
 * @author TG Team
 *
 */
public class LayoutBuilder {

    private final IFlexContainerLayout layoutContainer;

    private LayoutBuilder(final IFlexContainerLayout containerConfig, final IFlexLayout flexLayout) {
        layoutContainer = cell(containerConfig, flexLayout);
    }

    /**
     * Creates empty layout builder.
     *
     * @return
     */
    public static LayoutBuilder layoutContainer() {
        return new LayoutBuilder(null, null);
    }

    /**
     * Creates layout builder with container configuration.
     *
     * @param containerConfig
     * @return
     */
    public static LayoutBuilder layoutContainer(final IFlexContainerLayout containerConfig) {
        return new LayoutBuilder(containerConfig, null);
    }

    /**
     * Creates layout builder with container configuration and layout configuration.
     *
     * @param containerConfig
     * @param flexLayout
     * @return
     */
    public static LayoutBuilder layoutContainer(final IFlexContainerLayout containerConfig, final IFlexLayout flexLayout) {
        return new LayoutBuilder(containerConfig, flexLayout);
    }

    /**
     * Creates layout builder with container configuration with flex layout.
     *
     * @param flexLayout
     * @return
     */
    public static LayoutBuilder layoutContainer(final IFlexLayout flexLayout) {
        return new LayoutBuilder(null, flexLayout);
    }

    public static ILayoutCell cell(final IFlexContainerLayout container, final IFlexLayout layout) {
        return new ContainerConfig().cell(container, layout);
    }

    public static ILayoutCell cell(final IFlexContainerLayout container) {
        return new ContainerConfig().cell(container);
    }

    public static ILayoutCell cell(final IFlexLayout layout) {
        return new ContainerConfig().cell(layout);
    }

    public static ILayoutCell cell() {
        return new ContainerConfig().cell();
    }

    public static ILayoutCell skip(final IFlexLayout layout) {
        return new ContainerConfig().skip(layout);
    }

    public static ILayoutCell skip() {
        return new ContainerConfig().skip();
    }

    public static ILayoutCell select(final String attribute, final String value, final IFlexLayout layout) {
        return new ContainerConfig().select(attribute, value, layout);
    }

    public static ILayoutCell select(final String attribute, final String value) {
        return new ContainerConfig().select(attribute, value);
    }

    public static ILayoutCell html(final DomElement dom, final IFlexLayout layout) {
        return new ContainerConfig().html(dom, layout);
    }

    public static ILayoutCell html(final DomElement dom) {
        return new ContainerConfig().html(dom);
    }

    public static ILayoutCell subheader(final String title, final IFlexLayout layout) {
        return new ContainerConfig().subheader(title, layout);
    }

    public static ILayoutCell subheader(final String title) {
        return new ContainerConfig().subheader(title);
    }

    public static ILayoutCell subheaderOpen(final String title, final IFlexLayout layout) {
        return new ContainerConfig().subheaderOpen(title, layout);
    }

    public static ILayoutCell subheaderOpen(final String title) {
        return new ContainerConfig().subheaderOpen(title);
    }

    public static ILayoutCell subheaderClosed(final String title, final IFlexLayout layout) {
        return new ContainerConfig().subheaderClosed(title, layout);
    }

    public static ILayoutCell subheaderClosed(final String title) {
        return new ContainerConfig().subheaderClosed(title);
    }

    @Override
    public String toString() {
        return layoutContainer.render(false, false);
    }
}
