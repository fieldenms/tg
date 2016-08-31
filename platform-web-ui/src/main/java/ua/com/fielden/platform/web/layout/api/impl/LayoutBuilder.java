package ua.com.fielden.platform.web.layout.api.impl;

import ua.com.fielden.platform.dom.DomElement;

/**
 * The builder for flex layout configuration.
 *
 * @author TG Team
 *
 */
public class LayoutBuilder {

    public static ContainerCellConfig cell(final ContainerConfig container, final FlexLayoutConfig layout) {
        return new ContainerCellConfig().cell(container, layout);
    }

    public static ContainerCellConfig cell(final ContainerConfig container) {
        return new ContainerCellConfig().cell(container);
    }

    public static ContainerCellConfig cell(final FlexLayoutConfig layout) {
        return new ContainerCellConfig().cell(layout);
    }

    public static ContainerCellConfig cell() {
        return new ContainerCellConfig().cell();
    }

    public static ContainerCellConfig skip(final FlexLayoutConfig layout) {
        return new ContainerCellConfig().skip(layout);
    }

    public static ContainerCellConfig skip() {
        return new ContainerCellConfig().skip();
    }

    public static ContainerCellConfig select(final String attribute, final String value, final FlexLayoutConfig layout) {
        return new ContainerCellConfig().select(attribute, value, layout);
    }

    public static ContainerCellConfig select(final String attribute, final String value) {
        return new ContainerCellConfig().select(attribute, value);
    }

    public static ContainerCellConfig html(final DomElement dom, final FlexLayoutConfig layout) {
        return new ContainerCellConfig().html(dom, layout);
    }

    public static ContainerCellConfig html(final DomElement dom) {
        return new ContainerCellConfig().html(dom);
    }

    public static ContainerCellConfig html(final String html, final FlexLayoutConfig layout) {
        return new ContainerCellConfig().html(html, layout);
    }

    public static ContainerCellConfig html(final String html) {
        return new ContainerCellConfig().html(html);
    }

    public static ContainerCellConfig subheader(final String title, final FlexLayoutConfig layout) {
        return new ContainerCellConfig().subheader(title, layout);
    }

    public static ContainerCellConfig subheader(final String title) {
        return new ContainerCellConfig().subheader(title);
    }

    public static ContainerCellConfig subheaderOpen(final String title, final FlexLayoutConfig layout) {
        return new ContainerCellConfig().subheaderOpen(title, layout);
    }

    public static ContainerCellConfig subheaderOpen(final String title) {
        return new ContainerCellConfig().subheaderOpen(title);
    }

    public static ContainerCellConfig subheaderClosed(final String title, final FlexLayoutConfig layout) {
        return new ContainerCellConfig().subheaderClosed(title, layout);
    }

    public static ContainerCellConfig subheaderClosed(final String title) {
        return new ContainerCellConfig().subheaderClosed(title);
    }
}
