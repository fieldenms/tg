package ua.com.fielden.platform.criteria.generator.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;

/**
 * Entity for testing purposes.
 * 
 * @author TG Team
 *
 */
@KeyType(String.class)
@KeyTitle("key")
@DescTitle("desc")
public class SecondLevelEntity extends AbstractEntity<String> {

    private static final long serialVersionUID = -9055554371537762147L;

    @IsProperty
    private ThirdLevelEntity entityProp;

    public ThirdLevelEntity getEntityProp() {
	return entityProp;
    }

    @Observable
    public void setEntityProp(final ThirdLevelEntity entityProp) {
	this.entityProp = entityProp;
    }
}
