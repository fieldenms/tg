package ua.com.fielden.platform.web.centre;

import static java.util.Collections.unmodifiableMap;
import static ua.com.fielden.platform.entity.NoKey.NO_KEY;

import java.util.HashMap;
import java.util.Map;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.NoKey;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

/** 
 * Abstract functional entity for centre configuration actions.
 * 
 * @author TG Team
 *
 */
@KeyType(NoKey.class)
public abstract class AbstractCentreConfigAction extends AbstractFunctionalEntityWithCentreContext<NoKey> {
    
    public AbstractCentreConfigAction() {
        setKey(NO_KEY);
    }
    
    @IsProperty(Object.class)
    @Title("Custom object")
    private final Map<String, Object> customObject = new HashMap<>();
    
    @Observable
    protected AbstractCentreConfigAction setCustomObject(final Map<String, Object> customObject) {
        this.customObject.clear();
        this.customObject.putAll(customObject);
        return this;
    }
    
    public Map<String, Object> getCustomObject() {
        return unmodifiableMap(customObject);
    }
    
}