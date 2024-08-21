/**
 *
 */
package ua.com.fielden.platform.entity.validation.test_entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.Dependent;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.validation.annotation.GeProperty;
import ua.com.fielden.platform.entity.validation.annotation.LeProperty;

/**
 * Entity for testing of multi-range property validators.
 * 
 * @author TG Team
 */
@KeyType(String.class)
public class EntityWithMultyEndedRangeProperties extends AbstractEntity<String> {

    @IsProperty
    @Dependent({ "middleInt", "toInt" })
    @LeProperty({ "middleInt", "toInt" })
    private Integer fromInt;
    @IsProperty
    @Dependent({ "fromInt", "toInt" })
    @LeProperty("toInt")
    @GeProperty("fromInt")
    private Integer middleInt;
    @IsProperty
    @Dependent({ "fromInt", "middleInt" })
    @GeProperty({ "fromInt", "middleInt" })
    private Integer toInt;

    public Integer getFromInt() {
        return fromInt;
    }

    @Observable
    public void setFromInt(final Integer fromInt) {
        this.fromInt = fromInt;
    }

    public Integer getMiddleInt() {
        return middleInt;
    }

    @Observable
    public void setMiddleInt(final Integer middleInt) {
        this.middleInt = middleInt;
    }

    public Integer getToInt() {
        return toInt;
    }

    @Observable
    public void setToInt(final Integer toInt) {
        this.toInt = toInt;
    }
}
