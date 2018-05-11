package ua.com.fielden.platform.web.centre;

import static java.util.Collections.unmodifiableSet;

import java.util.LinkedHashSet;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityForCollectionModification;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

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