package ua.com.fielden.platform.web.layout.api;

/**
 * A contract for specifying the direction of flex container.
 *
 * @author TG Team
 *
 */
public interface IDirection extends IJustification {

    /**
     * Sets the horizontal direction of flex container.
     *
     * @return
     */
    IJustification horizontal();

    /**
     * Sets the vertical direction of flex container.
     *
     * @return
     */
    IJustification vertical();
}
