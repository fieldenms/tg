package ua.com.fielden.platform.web.layout.api;


/**
 * A contract for specifying gap between elements in the same container.
 *
 * @author TG Team
 *
 */
public interface IGap extends IFlexContainerLayout {

    /**
     * Set the gap between elements in the same container. Please note that gap between elements is set using the 'margin-right' or 'margin-bottom' style attributes. If a container
     * element has layout configuration with specified 'margin-right' or 'margin-bottom' style attributes then they will be ignored unless gap is not specified or is 0.
     *
     * @param pixels
     * @return
     */
    IFlexContainerLayout withGapBetweenCells(int pixels);
}
