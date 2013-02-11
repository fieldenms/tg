package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.validation.annotation.CompanionObject;

/**
 * Represents slot in the wagon for fitting bogie rotable there.
 *
 * @author TG Team
 *
 */
@KeyType(DynamicEntityKey.class)
@KeyTitle("Wagon slots")
@MapEntityTo
@CompanionObject(ITgWagonSlot.class)
public class TgWagonSlot extends AbstractEntity<DynamicEntityKey> {
    private static final long serialVersionUID = 1L;

    @IsProperty @MapTo @Title("Wagon")
    @CompositeKeyMember(1)
    private TgWagon wagon;

    @IsProperty @MapTo @Title("Position")
    @CompositeKeyMember(2)
    private Integer position;

    @IsProperty @MapTo @Title("Bogie")
    private TgBogie bogie;

    public TgBogie getBogie() {
        return bogie;
    }

    @Observable
    public TgWagonSlot setBogie(final TgBogie bogie) {
        this.bogie = bogie;
        return this;
    }

    public TgWagon getWagon() {
	return wagon;
    }

    @Observable
    protected TgWagonSlot setWagon(final TgWagon wagon) {
	this.wagon = wagon;
	return this;
    }

    public Integer getPosition() {
	return position;
    }

    @Observable
    protected TgWagonSlot setPosition(final Integer position) {
	this.position = position;
	return this;
    }

    /**
     * Retrieves full wagon slot number, which is composed of the wagon serial number, char 'B'  and the slot index.
     * @return
     */
    public String getSlotNumber() {
	return wagon.getSerialNo() + "B" + getSlotIndex();
    }

    /**
     * Retrieves wagon slot index.
     * @return
     */
    public String getSlotIndex() {
	return String.format("%02d", getPosition());
    }
}