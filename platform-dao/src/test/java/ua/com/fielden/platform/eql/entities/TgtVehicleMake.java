package ua.com.fielden.platform.eql.entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

@KeyType(String.class)
@MapEntityTo
public class TgtVehicleMake extends AbstractEntity<String> {
    @IsProperty
    @Title(value = "Competitor", desc = "Competitor")
    private TgtVehicleMake competitor;

    @Observable
    public TgtVehicleMake setCompetitor(final TgtVehicleMake competitor) {
        this.competitor = competitor;
        return this;
    }

    public TgtVehicleMake getCompetitor() {
        return competitor;
    }
}