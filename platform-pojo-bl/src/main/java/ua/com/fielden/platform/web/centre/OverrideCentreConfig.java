package ua.com.fielden.platform.web.centre;

import static ua.com.fielden.platform.entity.NoKey.NO_KEY;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.IContinuationData;
import ua.com.fielden.platform.entity.NoKey;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;

/** 
 * An entity representing prompt before overriding existing centre configuration in {@link CentreConfigEditAction} or {@link CentreConfigSaveAction} during configuration editing / savingAs.
 * 
 * @author TG Team
 *
 */
@KeyType(NoKey.class)
@CompanionObject(IOverrideCentreConfig.class)
@EntityTitle(value = "Override Configuration", desc = "Override existing centre cnfiguration")
public class OverrideCentreConfig extends AbstractFunctionalEntityWithCentreContext<NoKey> implements IContinuationData {
    
    public OverrideCentreConfig() {
        setKey(NO_KEY);
    }
    
}