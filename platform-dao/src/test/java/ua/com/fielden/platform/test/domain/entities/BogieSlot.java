package ua.com.fielden.platform.test.domain.entities;

import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;

/**
 * Represents slot in the bogie for fitting wheelset rotable there.
 *
 * @author nc
 *
 */
@KeyType(DynamicEntityKey.class)
public class BogieSlot extends RotableLocation<DynamicEntityKey> {

    private static final long serialVersionUID = 1L;

    @IsProperty
    @CompositeKeyMember(1)
    private Bogie bogie;

    @IsProperty
    @CompositeKeyMember(2)
    private Integer position;

    private Wheelset wheelset;

    public Wheelset getWheelset() {
        return wheelset;
    }

    public void setWheelset(final Wheelset wheelset) {
        this.wheelset = wheelset;
    }

    public BogieSlot() {
	setKey(new DynamicEntityKey(this));
    }

    public Bogie getBogie() {
	return bogie;
    }

    protected void setBogie(final Bogie bogie) {
	this.bogie = bogie;
    }

    public Integer getPosition() {
	return position;
    }

    protected void setPosition(final Integer position) {
	this.position = position;
    }

    /**
     * Retrieves full bogie slot number, which composition depends on whether bogie is currently on the wagon or not.
     *
     * @return
     */
    public String getSlotNumber() {
	if (bogie.getLocation() instanceof WagonSlot) {
	    return ((WagonSlot) bogie.getLocation()).getWagon().getSerialNo() + "WS" + getSlotIndex();
	} else {
	    return "WS" + getSlotIndex();
	}
    }

    /**
     * Retrieves bogie slot index, which calculation depends on whether bogie is currently on the wagon or not.
     *
     * @return
     */
    public String getSlotIndex() {
	if (bogie.getLocation() instanceof WagonSlot) {
	    return String.format("%02d", (((WagonSlot) bogie.getLocation()).getPosition() - 1) * 2 + getPosition());
	} else {
	    return String.format("%02d", getPosition());
	}
    }
}
