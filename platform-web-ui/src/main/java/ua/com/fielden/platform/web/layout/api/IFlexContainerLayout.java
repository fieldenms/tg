package ua.com.fielden.platform.web.layout.api;

/**
 * A contract for anything that can render (generate) the container layout.
 *
 * @author TG Team
 *
 */
public interface IFlexContainerLayout {

    /**
     * Renders the container layout.
     *
     * @param vertical
     *            - indicates whether container direction is vertical or not. This parameter is set according to layout configuration or it's default direction.
     * @param isVerticalDefault
     *            - indicates whether container direction is vertical by default or not.
     * @return
     */
    String render(boolean vertical, boolean isVerticalDefault);
}
