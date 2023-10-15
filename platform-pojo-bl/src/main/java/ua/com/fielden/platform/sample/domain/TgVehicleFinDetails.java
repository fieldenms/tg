package ua.com.fielden.platform.sample.domain;

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
import ua.com.fielden.platform.entity.annotation.UpperCase;

/**
 * Entity representing {@link Vehicle} financial details.
 * 
 * @author TG Team
 * 
 */
@EntityTitle(value = "Vehicle Financial Details", desc = "Vehicle Financial Details entity")
@KeyType(TgVehicle.class)
@KeyTitle(value = "Vehicle", desc = "Vehicle")
@MapEntityTo
@CompanionObject(ITgVehicleFinDetails.class)
public class TgVehicleFinDetails extends AbstractEntity<TgVehicle> {

    @IsProperty
    @MapTo
    @UpperCase
    @Title(value = "Cap. Works No", desc = "Capital Works No")
    private String capitalWorksNo;

    public String getCapitalWorksNo() {
        return capitalWorksNo;
    }

    @Observable
    public TgVehicleFinDetails setCapitalWorksNo(final String capitalWorksNo) {
        this.capitalWorksNo = capitalWorksNo;
        return this;
    }
}