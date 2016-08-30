package ua.com.fielden.platform.web.layout.api;

/**
 * A contract for specifying the element alignment along main axis.
 *
 * @author TG Team
 *
 */
public interface IJustification extends IAlignment {

    /**
     * Elements are packed toward to start of main axis.
     *
     * @return
     */
    IAlignment startJustified();

    /**
     * Elements are centred along main axis.
     *
     * @return
     */
    IAlignment centerJustified();

    /**
     * Elements are packed toward to end of main axis.
     *
     * @return
     */
    IAlignment endJustified();

    /**
     * The elements are distributed evenly along main axis. The first element is on the start of main axis and the last element is on the end of main axis.
     *
     * @return
     */
    IAlignment justified();

    /**
     * The elements are distributed evenly along main axis with equal space around them.
     *
     * @return
     */
    IAlignment aroundJustified();

}
