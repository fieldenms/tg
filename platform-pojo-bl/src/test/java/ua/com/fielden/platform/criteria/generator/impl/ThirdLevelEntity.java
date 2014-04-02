package ua.com.fielden.platform.criteria.generator.impl;

import java.util.Date;

import ua.com.fielden.platform.entity.AbstractEntity;
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
public class ThirdLevelEntity extends AbstractEntity<String> {

    private static final long serialVersionUID = 5896211024838356909L;

    @IsProperty
    @Title(value = "date property", desc = "date property description")
    private Date dateProp;

    @IsProperty
    @Title(value = "entity property", desc = "entity property description")
    private LastLevelEntity simpleEntityProp;

    public Date getDateProp() {
        return dateProp;
    }

    @Observable
    public void setDateProp(final Date dateProp) {
        this.dateProp = dateProp;
    }

    public LastLevelEntity getSimpleEntityProp() {
        return simpleEntityProp;
    }

    @Observable
    public void setSimpleEntityProp(final LastLevelEntity simpleEntityProp) {
        this.simpleEntityProp = simpleEntityProp;
    }

}
