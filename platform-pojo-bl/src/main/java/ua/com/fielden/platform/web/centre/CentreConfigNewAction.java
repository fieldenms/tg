package ua.com.fielden.platform.web.centre;

import static java.util.Collections.unmodifiableMap;
import static ua.com.fielden.platform.entity.NoKey.NO_KEY;

import java.util.HashMap;
import java.util.Map;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.NoKey;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

/** 
 * Functional entity for loading new centre configuration.
 * 
 * @author TG Team
 *
 */
@CompanionObject(ICentreConfigNewAction.class)
@KeyType(NoKey.class)
public class CentreConfigNewAction extends AbstractFunctionalEntityWithCentreContext<NoKey> {
    
    public CentreConfigNewAction() {
        setKey(NO_KEY);
    }
    
    @IsProperty(Object.class)
    @Title("Custom object")
    private final Map<String, Object> customObject = new HashMap<>();
    
    @Observable
    protected CentreConfigNewAction setCustomObject(final Map<String, Object> customObject) {
        this.customObject.clear();
        this.customObject.putAll(customObject);
        return this;
    }
    
    public Map<String, Object> getCustomObject() {
        return unmodifiableMap(customObject);
    }
    
}