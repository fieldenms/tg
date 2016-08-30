package ua.com.fielden.platform.web.layout.api;

import ua.com.fielden.platform.dom.DomElement;

/**
 * A contract for container layout.
 *
 * @author TG Team
 *
 */
public interface ILayoutCell extends IQuantifier {

    /**
     * Adds the container element to this container with specified layout configuration.
     *
     * @param container
     * @param layout
     * @return
     */
    ILayoutCell cell(final IFlexContainerLayout container, final IFlexLayout layout);

    /**
     * Adds the container element to this container.
     *
     * @param container
     * @return
     */
    ILayoutCell cell(final IFlexContainerLayout container);

    /**
     * Adds the element to this container with specified layout.
     *
     * @param layout
     * @return
     */
    ILayoutCell cell(final IFlexLayout layout);

    /**
     * Adds the element to this container.
     *
     * @return
     */
    ILayoutCell cell();

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
    ILayoutCell select(final String attribute, final String value, final IFlexLayout layout);

    /**
     * Adds the selected element to this container with specified layout.
     *
     * @param attribute
     * @param value
     * @return
     */
    ILayoutCell select(final String attribute, final String value);

    /**
     * Adds skip cell to the container configuration with specified layout.
     *
     * @param layout
     * @return
     */
    ILayoutCell skip(final IFlexLayout layout);

    /**
     * Adds skip cell to the container configuration.
     *
     * @return
     */
    ILayoutCell skip();

    /**
     * Adds the snippet of html to the container configuration with specified layout.
     *
     * @param dom
     * @param layout
     * @return
     */
    ILayoutCell html(final DomElement dom, final IFlexLayout layout);

    /**
     * Adds the snippet of html to the container configuration.
     *
     * @param dom
     * @return
     */
    ILayoutCell html(final DomElement dom);

    /**
     * Adds the default subheader without ability to close or open it.
     *
     * @param title
     *            - subheader title.
     * @param layout
     *            - the layout configuration for subheader.
     * @return
     */
    ILayoutCell subheader(final String title, final IFlexLayout layout);

    /**
     * Adds the default subheader without ability to close or open it.
     *
     * @param title
     *            - subheader title.
     * @return
     */
    ILayoutCell subheader(final String title);

    /**
     * Adds the subheader opened by default.
     *
     * @param title
     *            - subheader title.
     * @param layout
     *            - the layout configuration for subheader.
     * @return
     */
    ILayoutCell subheaderOpen(final String title, final IFlexLayout layout);

    /**
     * Adds the subheader opened by default.
     *
     * @param title
     *            - subheader title.
     * @return
     */
    ILayoutCell subheaderOpen(final String title);

    /**
     * Adds the subheader closed by default.
     *
     * @param title
     *            - subheader title.
     * @param layout
     *            - the layout configuration for subheader.
     * @return
     */
    ILayoutCell subheaderClosed(final String title, final IFlexLayout layout);

    /**
     * Adds the subheader closed by default.
     *
     * @param title
     *            - subheader title.
     * @return
     */
    ILayoutCell subheaderClosed(final String title);
}
