package ua.com.fielden.platform.types;

import java.awt.Color;

/**
 * Interface for "category" concept.
 * 
 * @author Jhou
 * 
 */
public interface ICategory {

    /**
     * Returns category "normality".
     * 
     * @return
     */
    boolean isNormal();

    /**
     * Returns if this category is "non-category".
     * 
     * @return
     */
    boolean isUncategorized();

    /**
     * Returns category name.
     * 
     * @return
     */
    String getName();

    /**
     * Returns category description.
     * 
     * @return
     */
    String getDesc();

    /**
     * Returns category color.
     * 
     * @return
     */
    Color getColor();
}
