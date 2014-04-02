package ua.com.fielden.platform.migration;

/**
 * A contract for implementing a specific logic for retrieving of a value for a specific property in a specific domain entity.
 * 
 * @author TG Team
 * 
 */
public interface IPropertyValueRetriever {
    /**
     * Should provide logic for obtaining an appropriate property value by its legacy value.
     * 
     * @param legacyValue
     * @return
     */
    Object find(Object legacyValue);
}
