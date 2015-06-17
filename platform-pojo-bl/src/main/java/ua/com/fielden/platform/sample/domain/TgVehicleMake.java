package ua.com.fielden.platform.sample.domain;

import org.junit.Ignore;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

@KeyType(String.class)
@MapEntityTo
@DescTitle("Description")
@Ignore
@CompanionObject(ITgVehicleMake.class)
public class TgVehicleMake extends AbstractEntity<String> {
    @IsProperty
    @Title(value = "Non-persisted prop", desc = "Desc")
    private String npProp;

    @Observable
    public TgVehicleMake setNpProp(final String npProp) {
        this.npProp = npProp;
        return this;
    }

    public String getNpProp() {
        return npProp;
    }

    @IsProperty
    @Title(value = "Competitor", desc = "Competitor")
    private TgVehicleMake competitor;

    @Observable
    public TgVehicleMake setCompetitor(final TgVehicleMake competitor) {
        this.competitor = competitor;
        return this;
    }

    public TgVehicleMake getCompetitor() {
        return competitor;
    }
}