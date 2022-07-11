package ua.com.fielden.platform.web.centre;

import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.functional.centre.CentreContextHolder;

/** 
 * Functional entity for configuring centre configuration.
 * 
 * @author TG Team
 *
 */
@CompanionObject(CentreConfigConfigureActionCo.class)
public class CentreConfigConfigureAction extends AbstractCentreConfigAction {
    
    @IsProperty
    @Title("Context Holder")
    private CentreContextHolder centreContextHolder;
    
    @IsProperty
    @Title(value = "Run automatically?", desc = "Defines whether this configuration should be auto run upon loading.")
    private boolean runAutomatically = false;
    
    @Observable
    public CentreConfigConfigureAction setRunAutomatically(final boolean runAutomatically) {
        this.runAutomatically = runAutomatically;
        return this;
    }
    
    public boolean isRunAutomatically() {
        return runAutomatically;
    }
    
    @Observable
    public CentreConfigConfigureAction setCentreContextHolder(final CentreContextHolder centreContextHolder) {
        this.centreContextHolder = centreContextHolder;
        return this;
    }
    
    public CentreContextHolder getCentreContextHolder() {
        return centreContextHolder;
    }
    
}