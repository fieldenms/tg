package ua.com.fielden.platform.serialisation.entity;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;

@KeyType(String.class)
@KeyTitle(value = "Key", desc = "Key Property")
public class EntityWithQueryProperty extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    private EntityResultQueryModel<BaseEntity> query;

    public EntityResultQueryModel<BaseEntity> getQuery() {
        return query;
    }

    @Observable
    public void setQuery(final EntityResultQueryModel<BaseEntity> query) {
        this.query = query;
    }

}
