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
 * Functional entity for sharing centre configuration.
 * 
 * @author TG Team
 *
 */
@KeyType(NoKey.class)
@CompanionObject(ICentreConfigShareAction.class)
public class CentreConfigShareAction extends AbstractFunctionalEntityWithCentreContext<NoKey> {
    
    public CentreConfigShareAction() {
        setKey(NO_KEY);
    }
    
    @IsProperty
    @Title("Error Message")
    private String errorMsg;
    
    @Observable
    public CentreConfigShareAction setErrorMsg(final String errorMsg) {
        this.errorMsg = errorMsg;
        return this;
    }
    
    public String getErrorMsg() {
        return errorMsg;
    }
    
}