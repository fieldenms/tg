package ua.com.fielden.platform.web.centre;

import static ua.com.fielden.platform.entity.NoKey.NO_KEY;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.IContinuationData;
import ua.com.fielden.platform.entity.NoKey;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

/** 
 * An entity representing prompt before overriding existing centre configuration in {@link CentreConfigEditAction} during configuration editing / copying.
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