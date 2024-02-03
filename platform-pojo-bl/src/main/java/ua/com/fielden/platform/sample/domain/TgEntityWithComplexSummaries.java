package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;

@KeyType(String.class)
@CompanionObject(ITgEntityWithComplexSummaries.class)
@MapEntityTo
public class TgEntityWithComplexSummaries extends AbstractEntity<String> {

    @IsProperty
    @MapTo
    private Integer kms;
    
    @IsProperty
    @MapTo
    private Integer cost;
    
    @Observable
    public TgEntityWithComplexSummaries setCost(final Integer cost) {
        this.cost = cost;
        return this;
    }

    public Integer getCost() {
        return cost;
    }

    @Observable
    public TgEntityWithComplexSummaries setKms(final Integer kms) {
        this.kms = kms;
        return this;
    }

    public Integer getKms() {
        return kms;
    }
}