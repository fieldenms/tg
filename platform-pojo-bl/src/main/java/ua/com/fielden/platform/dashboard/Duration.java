package ua.com.fielden.platform.dashboard;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Readonly;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.annotation.mutator.BeforeChange;
import ua.com.fielden.platform.entity.annotation.mutator.Handler;
import ua.com.fielden.platform.entity.annotation.mutator.StrParam;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.entity.validation.GreaterValidator;
import ua.com.fielden.platform.utils.Pair;

/**
 * Entity representing simple duration defined by integer number of {@link DurationUnit}s.
 *
 * @author TG Team
 */
@EntityTitle("Duration")
@KeyType(DynamicEntityKey.class)
@KeyTitle("Duration")
@CompanionObject(DurationCo.class)
@MapEntityTo
public class Duration extends AbstractEntity<DynamicEntityKey> {
    private static final Pair<String, String> entityTitleAndDesc = getEntityTitleAndDesc(Duration.class);
    public static final String ENTITY_TITLE = entityTitleAndDesc.getKey();
    public static final String ENTITY_DESC = entityTitleAndDesc.getValue();
    
    @IsProperty
    @MapTo
    @Title("Count")
    @CompositeKeyMember(1)
    @BeforeChange(@Handler(value = GreaterValidator.class, str = { @StrParam(name = "limit", value = "0") }))
    private Integer count; // no more than 2,147,483,647 which is sufficient even for 'milliseconds' durationUnit (~24.86 days)
    
    @IsProperty
    @MapTo
    @Title("Duration Unit")
    @CompositeKeyMember(2)
    private DurationUnit durationUnit;
    
    @IsProperty
    @Readonly
    @Calculated
    @Title("Millis")
    private Long millis;
    protected static final ExpressionModel millis_ = expr().prop("count").mult().prop("durationUnit.millis").model();
    
    @Observable
    protected Duration setMillis(final Long millis) {
        this.millis = millis;
        return this;
    }
    
    public Long getMillis() {
        return millis;
    }
    
    @Observable
    public Duration setDurationUnit(final DurationUnit durationUnit) {
        this.durationUnit = durationUnit;
        return this;
    }
    
    public DurationUnit getDurationUnit() {
        return durationUnit;
    }
    
    @Observable
    public Duration setCount(final Integer count) {
        this.count = count;
        return this;
    }
    
    public Integer getCount() {
        return count;
    }
    
}