package ua.com.fielden.platform.web.layout.api;

/**
 * A contract with two methods for specifying style and css class for cell.
 *
 * @author TG Team
 *
 */
public interface ICell extends IDirection {

    /**
     * Specify style for cell.
     *
     * @param style
     *            - the style name (e.g. margin, padding, overflow etc.)
     * @param value
     *            - the style value (e.g. 20px, auto, etc.)
     * @return
     */
    ICell withStyle(String style, String value);

    /**
     * Specify the css class for cell.
     *
     * @param clazz
     * @return
     */
    ICell withClass(String clazz);
}
