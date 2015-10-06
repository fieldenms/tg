package ua.com.fielden.platform.web.centre.api.insertion_points;

/**
 * Defines selectors for insertion points. These enum values are used when specifying where a functional action should insert its view.
 *
 * @author TG Team
 *
 */
public enum InsertionPoints {

    LEFT(".left-insertion-point"),  BOTTOM(".bottom-insertion-point"), RIGHT(".right-insertion-point");

    public final String selector;

    private InsertionPoints(final String selector) {
        this.selector = selector;
    }

}
