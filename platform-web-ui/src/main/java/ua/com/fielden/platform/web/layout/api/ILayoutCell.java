package ua.com.fielden.platform.web.layout.api;

/**
 * Represents the cell to layout.
 *
 * @author TG Team
 *
 */
public interface ILayoutCell {

    //cell(ICell)

    IQuantifier skip();

    IQuantifier html(String html);

    IQuantifier subheader(String title);

    IQuantifier subheaderOpen(String title);

    IQuantifier subheaderClosed(String title);
}
