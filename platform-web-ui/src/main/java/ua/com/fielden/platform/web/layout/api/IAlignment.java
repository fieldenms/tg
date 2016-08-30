package ua.com.fielden.platform.web.layout.api;

/**
 * A contract for specifying component alignment along cross-axis in the flex container.
 *
 * @author TG Team
 *
 */
public interface IAlignment extends IFlex {

    /**
     * Aligns container items on the start of cross-axis.
     *
     * @return
     */
    IFlex startAligned();

    /**
     * Aligns container items on the centre of cross-axis
     *
     * @return
     */
    IFlex centerAligned();

    /**
     * Aligns container items on the end of cross-axis
     *
     * @return
     */
    IFlex endAligned();
}
