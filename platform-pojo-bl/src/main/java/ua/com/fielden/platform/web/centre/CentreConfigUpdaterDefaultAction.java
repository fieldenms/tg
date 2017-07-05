package ua.com.fielden.platform.web.centre;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

/**
 * An entity for loading default centre configuration into {@link CentreConfigUpdater} master.
 * 
 * @author TG Team
 *
 */
@KeyType(String.class)
@CompanionObject(ICentreConfigUpdaterDefaultAction.class)
public class CentreConfigUpdaterDefaultAction extends AbstractFunctionalEntityWithCentreContext<String> {
    
    @IsProperty(String.class) 
    @Title(value = "Default Visible Properties", desc = "Ordered set of visible properties from default centre configuration")
    private Set<String> defaultVisibleProperties = new LinkedHashSet<>();
    
    @Observable
    public CentreConfigUpdaterDefaultAction setDefaultVisibleProperties(final Set<String> defaultVisibleProperties) {
        this.defaultVisibleProperties.clear();
        this.defaultVisibleProperties.addAll(defaultVisibleProperties);
        return this;
    }
    
    public Set<String> getDefaultVisibleProperties() {
        return Collections.unmodifiableSet(defaultVisibleProperties);
    }
    
// TODO    
//    /**
//     * Override to ignore link property requiredness for collectional properties 
//     * <code>chosenIds</code>, <code>addedIds</code>, <code>removedIds</code>.
//     *  
//     * @param entityType
//     * @param propertyName
//     * @return
//     */
//    @Override
//    protected boolean isLinkPropertyRequiredButMissing(final String propertyName) {
//        if (!isCollectionOfIds(propertyName)) {
//            return super.isLinkPropertyRequiredButMissing(propertyName);
//        } else {
//            return false;
//        }
//    }
}
