package ua.com.fielden.platform.entity;

import static ua.com.fielden.platform.entity.NoKey.NO_KEY;

import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

@KeyType(NoKey.class)
@CompanionObject(IBarCodeLocator.class)
public class BarCodeLocator extends AbstractFunctionalEntityWithCentreContext<NoKey> {

    @IsProperty
    @Title("Entity Key")
    private String entityKey;

    @IsProperty
    @Title("Entity")
    private AbstractEntity<?> entity;

    @IsProperty
    @Title("Entity Type")
    private String entityType;

    protected BarCodeLocator() {
        setKey(NO_KEY);
    }

    @Observable
    public BarCodeLocator setEntity(final AbstractEntity<?> entity) {
        this.entity = entity;
        return this;
    }

    public AbstractEntity<?> getEntity() {
        return entity;
    }

    @Observable
    public BarCodeLocator setEntityKey(final String entityKey) {
        this.entityKey = entityKey;
        return this;
    }

    public String getEntityKey() {
        return entityKey;
    }

    public String getEntityType() {
        return entityType;
    }

    @Observable
    public BarCodeLocator setEntityType(final String entityType) {
        this.entityType = entityType;
        return this;
    }
}