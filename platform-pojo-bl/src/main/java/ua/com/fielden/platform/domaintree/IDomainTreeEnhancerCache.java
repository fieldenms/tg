package ua.com.fielden.platform.domaintree;

import java.util.List;
import java.util.Map;
import java.util.Set;

import ua.com.fielden.platform.domaintree.impl.CalculatedPropertyInfo;
import ua.com.fielden.platform.domaintree.impl.CustomProperty;
import ua.com.fielden.platform.domaintree.impl.DomainTreeEnhancer;

/**
 * Cache for {@link DomainTreeEnhancer} instances and generated types.
 * <p>
 * The main purpose of this accidental (by means of complexity) cache is to reduce heavy calculated properties re-computation and to 
 * reduce the count of resultant generated entity types.
 * 
 * @author TG Team
 *
 */
public interface IDomainTreeEnhancerCache {
    /**
     * Retrieves {@link DomainTreeEnhancer} by its <code>rootTypes</code>, <code>calculatedProperties</code> and <code>customProperties</code>.
     * 
     * @param rootTypes
     * @param calculatedProperties
     * @param customProperties
     * @return
     */
    DomainTreeEnhancer getDomainTreeEnhancerFor(final Set<Class<?>> rootTypes, final Map<Class<?>, Set<CalculatedPropertyInfo>> calculatedProperties, final Map<Class<?>, List<CustomProperty>> customProperties);
    
    /**
     * Puts {@link DomainTreeEnhancer} by its <code>rootTypes</code>, <code>calculatedProperties</code> and <code>customProperties</code>.
     * 
     * @param rootTypes
     * @param calculatedPropertiesInfo
     * @param customProperties
     * @param domainTreeEnhancer
     * @return
     */
    DomainTreeEnhancer putDomainTreeEnhancerFor(final Set<Class<?>> rootTypes, final Map<Class<?>, Set<CalculatedPropertyInfo>> calculatedPropertiesInfo, final Map<Class<?>, List<CustomProperty>> customProperties, final DomainTreeEnhancer domainTreeEnhancer);
    
    /**
     * Retrieves generated type for named (saveAs) centre configuration by its <code>miType</code>, <code>saveAsName</code> and <code>userId</code>.
     * 
     * @param miType
     * @param saveAsName
     * @param userId
     * @return
     */
    Class<?> getGeneratedTypeFor(final Class<?> miType, final String saveAsName, final Long userId);
    
    /**
     * Puts generated type for named (saveAs) centre configuration by its <code>miType</code>, <code>saveAsName</code> and <code>userId</code>.
     * 
     * @param miType
     * @param saveAsName
     * @param userId
     * @param generatedType
     * @return
     */
    Class<?> putGeneratedTypeFor(final Class<?> miType, final String saveAsName, final Long userId, final Class<?> generatedType);
    
}