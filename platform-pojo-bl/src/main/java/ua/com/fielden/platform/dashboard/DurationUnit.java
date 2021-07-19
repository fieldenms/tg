package ua.com.fielden.platform.dashboard;

import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;

import java.time.Duration;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.utils.Pair;

/**
 * Entity representing standard duration unit defined by a fixed number of milliseconds.
 * <p>
 * Milliseconds, seconds, minutes, hours and days are supported.<br>
 * Day is defined by exact standard 24 hours (86400 seconds) and does not change when transitioning through daylight saving boundary.
 *
 * @author TG Team
 * @see Duration
 */
@EntityTitle("Duration Unit")
@KeyType(String.class)
@KeyTitle("Duration Unit")
@CompanionObject(DurationUnitCo.class)
@MapEntityTo
public class DurationUnit extends AbstractEntity<String> {
    private static final Pair<String, String> entityTitleAndDesc = getEntityTitleAndDesc(DurationUnit.class);
    public static final String ENTITY_TITLE = entityTitleAndDesc.getKey();
    public static final String ENTITY_DESC = entityTitleAndDesc.getValue();
    
    @IsProperty
    @Title("Millis")
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
    
}