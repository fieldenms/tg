package ua.com.fielden.platform.web.centre;

import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityForCollectionModification;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.functional.centre.CentreContextHolder;

/** 
 * Functional entity for loading centre configuration.
 * <p>
 * Key of this functional entity represents the name of currently loaded 'saveAs' configuration or empty string for the case of unnamed configuration.
 * 
 * @author TG Team
 *
 */
@CompanionObject(ICentreConfigLoadAction.class)
//!@MapEntityTo -- here the entity is not persistent intentionally
public class CentreConfigLoadAction extends AbstractFunctionalEntityForCollectionModification<String> {
    
    @IsProperty(LoadableCentreConfig.class)
    @Title("Configurations")
    private Set<LoadableCentreConfig> centreConfigurations = new LinkedHashSet<>();
    
    @IsProperty
    @Title("Context Holder")
    private CentreContextHolder centreContextHolder;
    
    @IsProperty(Object.class)
    @Title("Custom object")
    private final Map<String, Object> customObject = new HashMap<>();
    
    @Observable
    protected CentreConfigLoadAction setCustomObject(final Map<String, Object> customObject) {
        this.customObject.clear();
        this.customObject.putAll(customObject);
        return this;
    }
    
    public Map<String, Object> getCustomObject() {
        return unmodifiableMap(customObject);
    }
    
    @Observable
    public CentreConfigLoadAction setCentreContextHolder(final CentreContextHolder centreContextHolder) {
        this.centreContextHolder = centreContextHolder;
        return this;
    }
    
    public CentreContextHolder getCentreContextHolder() {
        return centreContextHolder;
    }
    
    @Observable
    protected CentreConfigLoadAction setCentreConfigurations(final Set<LoadableCentreConfig> centreConfigurations) {
        this.centreConfigurations.clear();
        this.centreConfigurations.addAll(centreConfigurations);
        return this;
    }
    
    public Set<LoadableCentreConfig> getCentreConfigurations() {
        return unmodifiableSet(centreConfigurations);
    }
    
}