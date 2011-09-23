package ua.com.fielden.platform.criteria.generator.impl;

import java.util.Date;

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
public class ThirdLevelEntity extends AbstractEntity<String> {

    private static final long serialVersionUID = 5896211024838356909L;

    @IsProperty
    private Date dateProp;

    @IsProperty
    private ThirdLevelEntity simpleEntityProp;

    public Date getDateProp() {
	return dateProp;
    }

    @Observable
    public void setDateProp(final Date dateProp) {
	this.dateProp = dateProp;
    }

    public ThirdLevelEntity getSimpleEntityProp() {
	return simpleEntityProp;
    }

    @Observable
    public void setSimpleEntityProp(final ThirdLevelEntity simpleEntityProp) {
	this.simpleEntityProp = simpleEntityProp;
    }


}
