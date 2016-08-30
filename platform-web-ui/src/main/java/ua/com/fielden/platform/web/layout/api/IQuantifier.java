package ua.com.fielden.platform.web.layout.api;

/**
 * A contract for copying the layout configuration.
 *
 * @author TG Team
 *
 */
public interface IQuantifier extends IForEachLayoutSetter {

    /**
     * Repeats the previous container layout configuration the specified number of teims.
     *
     * @param times
     * @return
     */
    ILayoutCell repeat(int times);

}
