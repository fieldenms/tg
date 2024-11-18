package ua.com.fielden.platform.entity.query.model;

import ua.com.fielden.platform.entity.annotation.mutator.AfterChange;
import ua.com.fielden.platform.entity.meta.IAfterChangeEventHandler;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.sample.domain.TrivialPersistentEntity;
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

    @IsProperty
    @MapTo
    @AfterChange(UpdatePlainPropDefiner.class)
    @Title(value = "Persistent", desc = "A persistent property with a definer assigning a value to plainStr.")
    private Integer intProp;

    public Integer getIntProp() {
        return intProp;
    }

    @Observable
    public EntityToFill setIntProp(final Integer intProp) {
        this.intProp = intProp;
        return this;
    }

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


    public static class UpdatePlainPropDefiner implements IAfterChangeEventHandler<Integer> {

        public static final String VALUE_FROM_DEFINER = "value from definer";

        @Override
        public void handle(final MetaProperty<Integer> property, final Integer entityPropertyValue) {
            final EntityToFill entity = property.getEntity();
            entity.setPlainStr(VALUE_FROM_DEFINER);
            if (entity.isInitialising()) {
                entity.getProperty("plainStr").resetState();
            }
        }
    }
}
