package ua.com.fielden.platform.domain.metadata;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.NoKey;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.KeyType;

/**
 * Entity that represents the insertion point in Domain Explorer Centre.
 *
 * @author TG Team
 *
 */

@KeyType(NoKey.class)
@CompanionObject(DomainExplorerInsertionPointCo.class)
public class DomainExplorerInsertionPoint extends AbstractFunctionalEntityWithCentreContext<NoKey> {

    public DomainExplorerInsertionPoint () {
        setKey(NoKey.NO_KEY);
    }
}
