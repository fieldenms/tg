package ua.com.fielden.platform.web.layout.api;

import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.web.layout.api.impl.ContainerConfig;
import ua.com.fielden.platform.web.layout.api.impl.ContainerRepeatConfig;
import ua.com.fielden.platform.web.layout.api.impl.FlexLayoutConfig;

/**
 * A contract for container layout.
 *
 * @author TG Team
 *
 */
public interface ILayoutCell {

    /**
     * Adds the container element to this container with specified layout configuration.
     *
     * @param container
     * @param layout
     * @return
     */
    ContainerRepeatConfig cell(final ContainerConfig container, final FlexLayoutConfig layout);

    /**
     * Adds the container element to this container.
     *
     * @param container
     * @return
     */
    ContainerRepeatConfig cell(final ContainerConfig container);

    /**
     * Adds the element to this container with specified layout.
     *
     * @param layout
     * @return
     */
    ContainerRepeatConfig cell(final FlexLayoutConfig layout);

    /**
     * Adds the element to this container.
     *
     * @return
     */
    ContainerRepeatConfig cell();

    /**
     * Adds the selected element to this container with specified layout.
     *
     * @param attribute
     *            - the attribute name for selecting an element.
     * @param value
     *            - the attribute value for selecting the element.
     * @param layout
     * @return
     */
    ContainerRepeatConfig select(final String attribute, final String value, final FlexLayoutConfig layout);

    /**
     * Adds the selected element to this container with specified layout.
     *
     * @param attribute
     * @param value
     * @return
     */
    ContainerRepeatConfig select(final String attribute, final String value);

    /**
     * Adds skip cell to the container configuration with specified layout.
     *
     * @param layout
     * @return
     */
    ContainerRepeatConfig skip(final FlexLayoutConfig layout);

    /**
     * Adds skip cell to the container configuration.
     *
     * @return
     */
    ContainerRepeatConfig skip();

    /**
     * Adds the snippet of html to the container configuration with specified layout.
     *
     * @param dom
     * @param layout
     * @return
     */
    ContainerRepeatConfig html(final DomElement dom, final FlexLayoutConfig layout);

    /**
     * Adds the snippet of html to the container configuration.
     *
     * @param dom
     * @return
     */
    ContainerRepeatConfig html(final DomElement dom);

    /**
     * Adds the snippet of html to the container configuration with specified layout.
     *
     * @param dom
     * @param layout
     * @return
     */
    ContainerRepeatConfig html(final String html, final FlexLayoutConfig layout);

    /**
     * Adds the snippet of html to the container configuration.
     *
     * @param dom
     * @return
     */
    ContainerRepeatConfig html(final String html);

    /**
     * Adds the default subheader without ability to close or open it.
     *
     * @param title
     *            - subheader title.
     * @param layout
     *            - the layout configuration for subheader.
     * @return
     */
    ContainerRepeatConfig subheader(final String title, final FlexLayoutConfig layout);

    /**
     * Adds the default subheader without ability to close or open it.
     *
     * @param title
     *            - subheader title.
     * @return
     */
    ContainerRepeatConfig subheader(final String title);

    /**
     * Adds the subheader opened by default.
     *
     * @param title
     *            - subheader title.
     * @param layout
     *            - the layout configuration for subheader.
     * @return
     */
    ContainerRepeatConfig subheaderOpen(final String title, final FlexLayoutConfig layout);

    /**
     * Adds the subheader opened by default.
     *
     * @param title
     *            - subheader title.
     * @return
     */
    ContainerRepeatConfig subheaderOpen(final String title);

    /**
     * Adds the subheader closed by default.
     *
     * @param title
     *            - subheader title.
     * @param layout
     *            - the layout configuration for subheader.
     * @return
     */
    ContainerRepeatConfig subheaderClosed(final String title, final FlexLayoutConfig layout);

    /**
     * Adds the subheader closed by default.
     *
     * @param title
     *            - subheader title.
     * @return
     */
    ContainerRepeatConfig subheaderClosed(final String title);
}
