package ua.com.fielden.platform.domain.metadata;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.NoKey;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

/**
 * Entity that represents the insertion point in Domain Explorer Centre.
 *
 * @author TG Team
 *
 */

@KeyType(NoKey.class)
@CompanionObject(DomainExplorerInsertionPointCo.class)
public class DomainExplorerInsertionPoint extends AbstractFunctionalEntityWithCentreContext<NoKey> {

    @IsProperty
    @Title(value = "Domain Filter", desc = "Desc")
    private String domainFilter;

    @Observable
    public DomainExplorerInsertionPoint setDomainFilter(final String domainFilter) {
        this.domainFilter = domainFilter;
        return this;
    }

    public String getDomainFilter() {
        return domainFilter;
    }

    public DomainExplorerInsertionPoint () {
        setKey(NoKey.NO_KEY);
    }
}
