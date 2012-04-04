package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.annotation.UpperCase;
import ua.com.fielden.platform.entity.validation.annotation.DefaultController2;
import ua.com.fielden.platform.sample.domain.controller.ITgVehicleFinDetails;

/**
 * Entity representing {@link Vehicle} financial details.
 *
 * @author TG Team
 *
 */
@EntityTitle(value="Vehicle Financial Details", desc="Vehicle Financial Details entity")
@KeyType(TgVehicle.class)
@KeyTitle(value="Vehicle", desc="Vehicle")
@MapEntityTo
@DefaultController2(ITgVehicleFinDetails.class)
public class TgVehicleFinDetails extends AbstractEntity<TgVehicle> {
    private static final long serialVersionUID = 1L;

    ////////////////// Group1 -- Ordering //////////////////////////////
    @IsProperty @UpperCase
    @Title(value = "Cap. Works No", desc = "Capital Works No")
    @MapTo("CAPITAL_WORKS_NO")
    private String capitalWorksNo;

    /**
     * Needed for TG entity factory.
     */
    protected TgVehicleFinDetails() {

    }

    public String getCapitalWorksNo() {
	return capitalWorksNo;
    }


    @Observable
    public TgVehicleFinDetails setCapitalWorksNo(final String capitalWorksNo) {
	this.capitalWorksNo = capitalWorksNo;
	return this;
    }
}
