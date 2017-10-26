package ua.com.fielden.platform.web.centre;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
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
    private LinkedHashSet<String> defaultVisibleProperties = new LinkedHashSet<>();
    
    @IsProperty(String.class) 
    @Title(value = "Default Sorting Values", desc = "Ordered set of pairs between property name and its 'asc' or 'desc' sorting value separated by colon")
    private List<String> defaultSortingVals = new ArrayList<>(); // this list should not contain duplicates, please ensure that when setSortingVals invocation is performing
    
    @Observable
    public CentreConfigUpdaterDefaultAction setDefaultSortingVals(final List<String> defaultSortingVals) {
        this.defaultSortingVals.clear();
        this.defaultSortingVals.addAll(defaultSortingVals);
        return this;
    }
    
    public List<String> getDefaultSortingVals() {
        return Collections.unmodifiableList(defaultSortingVals);
    }
    
    @Observable
    public CentreConfigUpdaterDefaultAction setDefaultVisibleProperties(final LinkedHashSet<String> defaultVisibleProperties) {
        this.defaultVisibleProperties.clear();
        this.defaultVisibleProperties.addAll(defaultVisibleProperties);
        return this;
    }
    
    public Set<String> getDefaultVisibleProperties() {
        return Collections.unmodifiableSet(defaultVisibleProperties);
    }
    
}
