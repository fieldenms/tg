package ua.com.fielden.platform.domain.metadata;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.NoKey;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;

/**
 * Entity that is used as base entity for domain explorer centre.
 *
 * @author TG Team
 *
 */
@KeyType(NoKey.class)
@CompanionObject(DomainExplorerCo.class)
public class DomainExplorer extends AbstractEntity<NoKey> {

    protected static final EntityResultQueryModel<DomainExplorer> model_ = select(DomainType.class).where().val(true).eq().val(false).modelAsEntity(DomainExplorer.class);

    public DomainExplorer () {
        setKey(NoKey.NO_KEY);
    }
}
