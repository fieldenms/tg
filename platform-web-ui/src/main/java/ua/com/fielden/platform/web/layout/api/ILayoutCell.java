package ua.com.fielden.platform.web.layout.api;

import ua.com.fielden.platform.dom.DomElement;

/**
 * Represents the cell to layout.
 *
 * @author TG Team
 *
 */
public interface ILayoutCell extends IQuantifier {

    ILayoutCell cell(final IFlexContainerLayout container, final IFlexLayout layout);

    ILayoutCell cell(final IFlexContainerLayout container);

    ILayoutCell cell(final IFlexLayout layout);

    ILayoutCell cell();

    ILayoutCell skip(final IFlexLayout layout);

    ILayoutCell skip();

    ILayoutCell html(final DomElement dom, final IFlexLayout layout);

    ILayoutCell html(final DomElement dom);

    ILayoutCell subheader(final String title, final IFlexLayout layout);

    ILayoutCell subheader(final String title);

    ILayoutCell subheaderOpen(final String title, final IFlexLayout layout);

    ILayoutCell subheaderOpen(final String title);

    ILayoutCell subheaderClosed(final String title, final IFlexLayout layout);

    ILayoutCell subheaderClosed(final String title);
}
