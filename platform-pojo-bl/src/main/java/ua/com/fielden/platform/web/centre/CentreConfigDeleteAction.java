package ua.com.fielden.platform.web.centre;

import static ua.com.fielden.platform.entity.NoKey.NO_KEY;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.NoKey;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

/** 
 * Functional entity for deleting centre configuration.
 * 
 * @author TG Team
 *
 */
@CompanionObject(ICentreConfigDeleteAction.class)
@KeyType(NoKey.class)
public class CentreConfigDeleteAction extends AbstractFunctionalEntityWithCentreContext<NoKey> {
    
    public CentreConfigDeleteAction() {
        setKey(NO_KEY);
    }
    
    @IsProperty
    @Title(value = "Preferred Config", desc = "Preferred configuration after delete action success.")
    private String preferredConfig;
    
    @Observable
    public CentreConfigDeleteAction setPreferredConfig(final String preferredConfig) {
        this.preferredConfig = preferredConfig;
        return this;
    }
    
    public String getPreferredConfig() {
        return preferredConfig;
    }
    
}