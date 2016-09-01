package ua.com.fielden.platform.web.layout.api.impl;

import ua.com.fielden.platform.dom.DomElement;

/**
 * The builder for flex layout configuration.
 *
 * @author TG Team
 *
 */
public class LayoutBuilder {

    public static ContainerRepeatConfig cell(final ContainerConfig container, final FlexLayoutConfig layout) {
        return new ContainerCellConfig().cell(container, layout);
    }

    public static ContainerRepeatConfig cell(final ContainerConfig container) {
        return new ContainerCellConfig().cell(container);
    }

    public static ContainerRepeatConfig cell(final FlexLayoutConfig layout) {
        return new ContainerCellConfig().cell(layout);
    }

    public static ContainerRepeatConfig cell() {
        return new ContainerCellConfig().cell();
    }

    public static ContainerRepeatConfig skip(final FlexLayoutConfig layout) {
        return new ContainerCellConfig().skip(layout);
    }

    public static ContainerRepeatConfig skip() {
        return new ContainerCellConfig().skip();
    }

    public static ContainerRepeatConfig select(final String attribute, final String value, final FlexLayoutConfig layout) {
        return new ContainerCellConfig().select(attribute, value, layout);
    }

    public static ContainerRepeatConfig select(final String attribute, final String value) {
        return new ContainerCellConfig().select(attribute, value);
    }

    public static ContainerRepeatConfig html(final DomElement dom, final FlexLayoutConfig layout) {
        return new ContainerCellConfig().html(dom, layout);
    }

    public static ContainerRepeatConfig html(final DomElement dom) {
        return new ContainerCellConfig().html(dom);
    }

    public static ContainerRepeatConfig html(final String html, final FlexLayoutConfig layout) {
        return new ContainerCellConfig().html(html, layout);
    }

    public static ContainerRepeatConfig html(final String html) {
        return new ContainerCellConfig().html(html);
    }

    public static ContainerRepeatConfig subheader(final String title, final FlexLayoutConfig layout) {
        return new ContainerCellConfig().subheader(title, layout);
    }

    public static ContainerRepeatConfig subheader(final String title) {
        return new ContainerCellConfig().subheader(title);
    }

    public static ContainerRepeatConfig subheaderOpen(final String title, final FlexLayoutConfig layout) {
        return new ContainerCellConfig().subheaderOpen(title, layout);
    }

    public static ContainerRepeatConfig subheaderOpen(final String title) {
        return new ContainerCellConfig().subheaderOpen(title);
    }

    public static ContainerRepeatConfig subheaderClosed(final String title, final FlexLayoutConfig layout) {
        return new ContainerCellConfig().subheaderClosed(title, layout);
    }

    public static ContainerRepeatConfig subheaderClosed(final String title) {
        return new ContainerCellConfig().subheaderClosed(title);
    }
}
