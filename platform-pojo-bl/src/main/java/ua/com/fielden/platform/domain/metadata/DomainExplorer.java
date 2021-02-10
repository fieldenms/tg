package ua.com.fielden.platform.domain.metadata;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.NoKey;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.KeyType;

/**
 * Entity that is used as base entity for domain explorer centre.
 *
 * @author TG Team
 *
 */
@KeyType(NoKey.class)
@CompanionObject(DomainExplorerCo.class)
public class DomainExplorer extends AbstractEntity<NoKey> {

    public DomainExplorer () {
        setKey(NoKey.NO_KEY);
    }
}
