package ua.com.fielden.platform.test_entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.DescRequired;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.annotation.mutator.BeforeChange;
import ua.com.fielden.platform.entity.annotation.mutator.Handler;
import ua.com.fielden.platform.entity.validators.BceForEntityWithWarningsIntProp;
import ua.com.fielden.platform.entity.validators.BceForEntityWithWarningsSelfRefProp;

/**
 * Entity class with properties that have BCE handlers yielding warnings.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
@KeyTitle(value = "Entity No", desc = "Key Property")
@DescTitle(value = "Description", desc = "Description Property")
@DescRequired
public class EntityWithWarnings extends AbstractEntity<String> {

    @IsProperty
    @MapTo
    @Title(value = "Integer", desc = "Desc")
    @BeforeChange(@Handler(BceForEntityWithWarningsIntProp.class))
    private Integer intProp;
    
    @IsProperty
    @MapTo
    @Title(value = "Self", desc = "Desc")
    @BeforeChange(@Handler(BceForEntityWithWarningsSelfRefProp.class))
    private EntityWithWarnings selfRefProp;
    
    @Observable
    public EntityWithWarnings setSelfRefProp(final EntityWithWarnings selfRefProp) {
        this.selfRefProp = selfRefProp;
        return this;
    }

    public EntityWithWarnings getSelfRefProp() {
        return selfRefProp;
    }

    @Observable
    public EntityWithWarnings setIntProp(final Integer intProp) {
        this.intProp = intProp;
        return this;
    }

    public Integer getIntProp() {
        return intProp;
    }

    

}