package ua.com.fielden.platform.web.layout.api;

import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.web.layout.api.impl.ContainerCellConfig;
import ua.com.fielden.platform.web.layout.api.impl.ContainerConfig;
import ua.com.fielden.platform.web.layout.api.impl.FlexLayoutConfig;

/**
 * A contract for container layout.
 *
 * @author TG Team
 *
 */
public interface ILayoutCell extends IRepeater {

    /**
     * Adds the container element to this container with specified layout configuration.
     *
     * @param container
     * @param layout
     * @return
     */
    ContainerCellConfig cell(final ContainerConfig container, final FlexLayoutConfig layout);

    /**
     * Adds the container element to this container.
     *
     * @param container
     * @return
     */
    ContainerCellConfig cell(final ContainerConfig container);

    /**
     * Adds the element to this container with specified layout.
     *
     * @param layout
     * @return
     */
    ContainerCellConfig cell(final FlexLayoutConfig layout);

    /**
     * Adds the element to this container.
     *
     * @return
     */
    ContainerCellConfig cell();

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
    ContainerCellConfig select(final String attribute, final String value, final FlexLayoutConfig layout);

    /**
     * Adds the selected element to this container with specified layout.
     *
     * @param attribute
     * @param value
     * @return
     */
    ContainerCellConfig select(final String attribute, final String value);

    /**
     * Adds skip cell to the container configuration with specified layout.
     *
     * @param layout
     * @return
     */
    ContainerCellConfig skip(final FlexLayoutConfig layout);

    /**
     * Adds skip cell to the container configuration.
     *
     * @return
     */
    ContainerCellConfig skip();

    /**
     * Adds the snippet of html to the container configuration with specified layout.
     *
     * @param dom
     * @param layout
     * @return
     */
    ContainerCellConfig html(final DomElement dom, final FlexLayoutConfig layout);

    /**
     * Adds the snippet of html to the container configuration.
     *
     * @param dom
     * @return
     */
    ContainerCellConfig html(final DomElement dom);

    /**
     * Adds the snippet of html to the container configuration with specified layout.
     *
     * @param dom
     * @param layout
     * @return
     */
    ContainerCellConfig html(final String html, final FlexLayoutConfig layout);

    /**
     * Adds the snippet of html to the container configuration.
     *
     * @param dom
     * @return
     */
    ContainerCellConfig html(final String html);

    /**
     * Adds the default subheader without ability to close or open it.
     *
     * @param title
     *            - subheader title.
     * @param layout
     *            - the layout configuration for subheader.
     * @return
     */
    ContainerCellConfig subheader(final String title, final FlexLayoutConfig layout);

    /**
     * Adds the default subheader without ability to close or open it.
     *
     * @param title
     *            - subheader title.
     * @return
     */
    ContainerCellConfig subheader(final String title);

    /**
     * Adds the subheader opened by default.
     *
     * @param title
     *            - subheader title.
     * @param layout
     *            - the layout configuration for subheader.
     * @return
     */
    ContainerCellConfig subheaderOpen(final String title, final FlexLayoutConfig layout);

    /**
     * Adds the subheader opened by default.
     *
     * @param title
     *            - subheader title.
     * @return
     */
    ContainerCellConfig subheaderOpen(final String title);

    /**
     * Adds the subheader closed by default.
     *
     * @param title
     *            - subheader title.
     * @param layout
     *            - the layout configuration for subheader.
     * @return
     */
    ContainerCellConfig subheaderClosed(final String title, final FlexLayoutConfig layout);

    /**
     * Adds the subheader closed by default.
     *
     * @param title
     *            - subheader title.
     * @return
     */
    ContainerCellConfig subheaderClosed(final String title);
}
