package ua.com.fielden.platform.types;

import java.util.List;

/**
 * Interface for value category determination and additional categorization info.
 * 
 * IMPORTANT : default constructor should be provided for all implementors.
 * 
 * @author Jhou
 * 
 */
public interface ICategorizer {

    /**
     * Returns the value's category.
     * 
     * @param value
     * @return
     */
    ICategory getCategory(final Object value);

    /**
     * Returns all pre-defined categories (without category that marks uncategorized values).
     * 
     * @return
     */
    List<? extends ICategory> getAllCategories();

    /**
     * Returns pre-defined categories which form summary duration for avalibility calculation.
     * 
     * @return
     */
    List<? extends ICategory> getMainCategories();

    /**
     * Returns pre-defined properties by which availability information could be distributed.
     * 
     * @return
     */
    List<String> getDistributionProperties();

}
