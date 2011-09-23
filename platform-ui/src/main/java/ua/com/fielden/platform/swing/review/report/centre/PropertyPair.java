package ua.com.fielden.platform.swing.review.report.centre;

/**
 * Marks criteria properties those have a pair.
 * 
 * @author TG Team
 *
 */
public @interface PropertyPair {

    /**
     * Returns the order of the property in the pair.
     * 
     * @return
     */
    int order();

    /**
     * Returns the pair property name generated for marked one.
     * 
     * @return
     */
    String propertyPair();
}
