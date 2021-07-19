package ua.com.fielden.platform.sample.domain.crit_gen;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

/**
 * Entity for testing purposes.
 * 
 * @author TG Team
 * 
 */
@KeyType(String.class)
@KeyTitle("key")
@DescTitle("desc")
@CompanionObject(ISecondLevelEntity.class)
public class SecondLevelEntity extends AbstractEntity<String> {

    private static final long serialVersionUID = -9055554371537762147L;

    @IsProperty
    @Title(value = "entity property", desc = "entity property description")
    private ThirdLevelEntity entityProp;

    public ThirdLevelEntity getEntityProp() {
        return entityProp;
    }

    @Observable
    public void setEntityProp(final ThirdLevelEntity entityProp) {
        this.entityProp = entityProp;
    }
}
