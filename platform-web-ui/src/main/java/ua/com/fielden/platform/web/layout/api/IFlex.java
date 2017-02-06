package ua.com.fielden.platform.web.layout.api;

/**
 * A contract for specifying the ability of container element to grow with respect to other elements in the same container.
 *
 * @author TG Team
 *
 */
public interface IFlex extends ILayoutCellCompleted {

    /**
     * Enables the element to grow. It is the same as flex(1);
     *
     * @return
     */
    ILayoutCellCompleted flex();

    /**
     * Enables element to grow according to specified ratio.
     *
     * @param ratio
     *            - the amount of space an element can take in the flex container with respect to other elements.
     * @return
     */
    ILayoutCellCompleted flex(int ratio);

    /**
     * Disables element ability to grow.
     *
     * @return
     */
    ILayoutCellCompleted flexNone();

}
