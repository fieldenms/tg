package ua.com.fielden.platform.test.domain.entities;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

/**
 * Represents a wagon business entity.
 * 
 * @author 01es
 * 
 */
@KeyType(String.class)
@KeyTitle(value = "Wagon No", desc = "Wagon number")
@DescTitle(value = "Description", desc = "Wagon description")
public class Wagon extends Equipment<String> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @Title(value = "Serial No", desc = "Wagon serial number")
    private String serialNo;
    @IsProperty
    @Title(value = "Class", desc = "Wagon class")
    private WagonClass wagonClass;
    @IsProperty(WagonSlot.class)
    @Title(value = "Wagon slots", desc = "A list of slots for the wagon")
    private List<WagonSlot> slots = new ArrayList<WagonSlot>();

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

    @Observable
    public void setSerialNo(final String serialNo) {
        this.serialNo = serialNo;
    }

    public WagonClass getWagonClass() {
        return wagonClass;
    }

    @Observable
    public void setWagonClass(final WagonClass wagonClass) {
        this.wagonClass = wagonClass;
    }

    public List<WagonSlot> getSlots() {
        return slots;
    }

    @Observable
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
    public WagonSlot getSlot(final Integer slotPosition) {
        System.out.println("slotPosition = " + slotPosition + " slots.size() = " + slots.size());
        if (slotPosition > 0 && slotPosition <= slots.size()) {
            return slots.get(slotPosition - 1);
        } else {
            throw new RuntimeException("Invalid slot position.");
        }
    }

    /**
     * Tests compatibility of the given bogie with this wagon.
     * 
     * @param rotable
     * @return
     */
    public boolean isClassCompatible(final Bogie rotable) {
        return getWagonClass().isBogieClassCompatible(rotable.getRotableClass());
    };

    public Bogie getBogieInSlotPosition(final Integer slotPosition) {
        return getSlot(slotPosition).getBogie();
    }

    public void defitBogie(final Bogie bogie) {
        for (final WagonSlot wagonSlot : slots) {
            if (wagonSlot.getBogie() != null && wagonSlot.getBogie().equals(bogie)) {
                wagonSlot.setBogie(null);
                break;
            }
        }
    }

    //    @Override
    //    public String toString() {
    //        // TODO Auto-generated method stub
    //        return super.toString() + " --:-- " + slots.get(0).getBogie();
    //    }
}
