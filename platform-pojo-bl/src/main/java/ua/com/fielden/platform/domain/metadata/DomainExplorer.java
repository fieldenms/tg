package ua.com.fielden.platform.domain.metadata;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.NoKey;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;

/**
 * Entity that is used as base entity for domain explorer centre.
 *
 * @author TG Team
 *
 */
@KeyType(NoKey.class)
@CompanionObject(DomainExplorerCo.class)
@EntityTitle(value = "Domain Explorer", desc = "Entity used as the basis for the Domain Explorer centre.")
public class DomainExplorer extends AbstractEntity<NoKey> {

    protected static final EntityResultQueryModel<DomainExplorer> model_ = select(DomainType.class).where().val(true).eq().val(false).yield().prop("dbTable").as("dbTable").modelAsEntity(DomainExplorer.class);

    public DomainExplorer () {
        setKey(NoKey.NO_KEY);
    }
    
    @IsProperty
    @Title(value = "DB Table", desc = "A name of a database table.")
    private String dbTable;

    @Observable
    public DomainExplorer setDbTable(final String dbTable) {
        this.dbTable = dbTable;
        return this;
    }

    public String getDbTable() {
        return dbTable;
    }

}