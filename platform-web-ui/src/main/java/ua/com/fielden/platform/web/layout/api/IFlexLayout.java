package ua.com.fielden.platform.web.layout.api;

import java.util.Optional;

/**
 * A contract for layout configuration rendering (generation)
 *
 * @author TG Team
 *
 */
public interface IFlexLayout {

    /**
     * Renders the layout configuration according to container direction and gap between elements in that container.
     *
     * @param vertical
     * @param gap
     * @return
     */
    String render(boolean vertical, int gap);

    /**
     * Returns value that indicates whether element with this layout configuration is vertical or not. If the layout wasn't configured with direction class then it returns empty
     * value.
     *
     * @return
     */
    Optional<Boolean> isVerticalLayout();
}
