package ua.com.fielden.platform.dashboard;

import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.annotation.mutator.BeforeChange;
import ua.com.fielden.platform.entity.annotation.mutator.Handler;
import ua.com.fielden.platform.entity.validation.MaxLengthValidator;
import ua.com.fielden.platform.utils.Pair;

/**
 * Entity representing standard duration unit defined by a fixed number of milliseconds.
 * <p>
 * Minutes, hours and days are supported.<br>
 * Day is defined as standard 24 hours (86400 seconds) and does not change when transitioning through a daylight saving boundary.
 *
 * @author TG Team
 * @see ua.com.fielden.platform.dashboard.Duration
 */
@EntityTitle("Duration Unit")
@KeyType(DynamicEntityKey.class)
@KeyTitle("Duration Unit")
@CompanionObject(DurationUnitCo.class)
@MapEntityTo
public class DurationUnit extends AbstractEntity<DynamicEntityKey> {
    private static final Pair<String, String> entityTitleAndDesc = getEntityTitleAndDesc(DurationUnit.class);
    public static final String ENTITY_TITLE = entityTitleAndDesc.getKey();
    public static final String ENTITY_DESC = entityTitleAndDesc.getValue();
    
    @IsProperty(length = 16)
    @MapTo
    @CompositeKeyMember(0)
    @BeforeChange(@Handler(MaxLengthValidator.class))
    private String unit;

    @IsProperty
    @MapTo
    private Integer millis; // no more than 2,147,483,647 which is sufficient even for 'days' durationUnit (= 86400000 milliseconds)
    
    @Observable
    protected DurationUnit setMillis(final Integer millis) {
        this.millis = millis;
        return this;
    }
    
    public Integer getMillis() {
        return millis;
    }

    @Observable
    public DurationUnit setUnit(final String unit) {
        this.unit = unit;
        return this;
    }

    public String getUnit() {
        return unit;
    }

}