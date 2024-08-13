package ua.com.fielden.platform.entity.query.model;

import ua.com.fielden.platform.companion.TrivialPersistentEntity;
import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.annotation.*;

@KeyType(String.class)
@CompanionObject(EntityToFillDao.class)
@MapEntityTo
public class EntityToFill extends AbstractPersistentEntity<String> {

    @IsProperty
    private String plainStr;

    @IsProperty
    private TrivialPersistentEntity plainEntity;

    public TrivialPersistentEntity getPlainEntity() {
        return plainEntity;
    }

    @Observable
    public EntityToFill setPlainEntity(final TrivialPersistentEntity plainEntity) {
        this.plainEntity = plainEntity;
        return this;
    }

    public String getPlainStr() {
        return plainStr;
    }

    @Observable
    public EntityToFill setPlainStr(final String plainStr) {
        this.plainStr = plainStr;
        return this;
    }

}
