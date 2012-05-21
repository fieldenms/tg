package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

@EntityTitle(value = "Bogie location", desc = "Bogie location")
@KeyTitle(value = "Bogie location key")
@DescTitle(value = "Bogie location description", desc = "Description of Bogie location")
public class TgBogieLocation extends AbstractUnionEntity {

    private static final long serialVersionUID = 7362243737334921917L;

    @IsProperty
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