package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.validation.annotation.CompanionObject;

@EntityTitle(value = "Bogie location", desc = "Bogie location")
@CompanionObject(ITgBogieLocation.class)
public class TgBogieLocation extends AbstractUnionEntity {

    private static final long serialVersionUID = 7362243737334921917L;

    @IsProperty(linkProperty = "bogie")
    @MapTo
    @Title(value = "Wagon slot", desc = "Wagon slot")
    private TgWagonSlot wagonSlot;

    @IsProperty
    @MapTo
    @Title(value = "Workshop", desc = "Workshop")
    private TgWorkshop workshop;

    @Observable
    public TgBogieLocation setWorkshop(final TgWorkshop workshop) {
	this.workshop = workshop;
	return this;
    }

    public TgWorkshop getWorkshop() {
	return workshop;
    }

    @Observable
    public TgBogieLocation setWagonSlot(final TgWagonSlot wagonSlot) {
	this.wagonSlot = wagonSlot;
	return this;
    }

    public TgWagonSlot getWagonSlot() {
	return wagonSlot;
    }
}