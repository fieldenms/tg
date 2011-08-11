package ua.com.fielden.platform.equery;

/**
 * Contract for anything that provides query string for aggregation function.
 * 
 * @author oleh
 * 
 */
public interface IPropertyAggregationFunction {

    /**
     * Returns query string for this aggregation function and specified property.
     * 
     * @param key
     * @return
     */
    String createQueryString(final String key);

    /**
     * Returns type result returned by aggregation function applied to values of <code>propertyClass</code>.
     * 
     * @param propertyClass
     * @return
     */
    Class<?> getReturnedType(Class<?> propertyClass);
}
