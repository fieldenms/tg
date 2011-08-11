package ua.com.fielden.platform.example.entities;

import java.util.List;

import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Title;

/**
 * Represents a wagon business entity.
 *
 * @author 01es
 *
 */
@KeyType(String.class)
@KeyTitle(value="Wagon No", desc="Wagon Number")
@DescTitle(value="Wagon Description", desc="Wagon description")
public class Wagon extends Equipment<String> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @Title(value="Wagon Serial No", desc="Wagon Serial Number")
    private String serialNo;
    @IsProperty
    private WagonClass wagonClass;
    @IsProperty(WagonSlot.class)
    private List<WagonSlot> slots;

    /**
     * Constructor for Hibernate
     */
    protected Wagon() {
    }

    /**
     * The main constructor.
     *
     * @param number
     * @param desc
     */
    public Wagon(final String number, final String desc) {
	super(null, number, desc);
    }

    public String getSerialNo() {
	return serialNo;
    }

    public void setSerialNo(final String serialNo) {
	this.serialNo = serialNo;
    }

    public WagonClass getWagonClass() {
	return wagonClass;
    }

    public void setWagonClass(final WagonClass wagonClass) {
	this.wagonClass = wagonClass;
    }

    public List<WagonSlot> getSlots() {
	return slots;
    }

    protected void setSlots(final List<WagonSlot> slots) {
	this.slots = slots;
    }

    /**
     * Gets slot by slot position.
     *
     * @param slotPosition
     * @return
     * @throws Exception
     */
    public WagonSlot getSlot(final Integer slotPosition) throws Exception {
	if (slotPosition > 0 && slotPosition <= slots.size()) {
	    return slots.get(slotPosition - 1);
	} else {
	    throw new Exception("Invalid slot position.");
	}
    }

    /**
     * Tests compatibility of the given bogie with this wagon.
     *
     * @param rotable
     * @return
     */
    public boolean isClassCompatible(final Bogie rotable) {
	return wagonClass.isBogieClassCompatible(rotable.getRotableClass());
    };

}
