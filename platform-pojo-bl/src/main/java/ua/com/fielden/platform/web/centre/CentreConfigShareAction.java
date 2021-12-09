package ua.com.fielden.platform.web.centre;

import static ua.com.fielden.platform.entity.NoKey.NO_KEY;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.NoKey;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;

/** 
 * Functional entity for sharing centre configuration.
 * <p>
 * It covers basic needs for validating whether configuration can be shared.<br>
 * Shows non-intrusive informational message if not.<br>
 * Copies URI into the clipboard otherwise.
 * <p>
 * Other functional actions for sharing can be implemented similarly using {@link CentreConfigShareAction} producer static methods.
 * 
 * @author TG Team
 *
 */
@KeyType(NoKey.class)
@CompanionObject(CentreConfigShareActionCo.class)
public class CentreConfigShareAction extends AbstractFunctionalEntityWithCentreContext<NoKey> {
    
    public CentreConfigShareAction() {
        setKey(NO_KEY);
    }
    
    /**
     * Error message if centre configuraton could not be shared (or {@code null} otherwise).
     */
    @IsProperty
    private String errorMessage;
    
    @Observable
    public CentreConfigShareAction setErrorMessage(final String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
}