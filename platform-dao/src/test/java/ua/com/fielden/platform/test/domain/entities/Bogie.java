package ua.com.fielden.platform.test.domain.entities;

import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.Observable;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.unmodifiableList;

/**
 * Bogie rotable business entity
 *
 * @author nc
 */
public class Bogie extends Rotable {

    private static final long serialVersionUID = 1L;

    @IsProperty(BogieSlot.class)
    private final List<BogieSlot> slots = new ArrayList<>();

    protected Bogie() {
    }

    public Bogie(final String name, final String desc) {
        super(name, desc);
    }

    public List<BogieSlot> getSlots() {
        return unmodifiableList(slots);
    }

    @Observable
    public Bogie setSlots(final List<BogieSlot> slots) {
        this.slots.clear();
        this.slots.addAll(slots);
        return this;
    }


    /**
     * Gets slot by slot position.
     *
     * @param slotPosition
     * @return
     * @throws Exception
     */
    public BogieSlot getSlot(final Integer slotPosition) {
        if (slotPosition > 0 && slotPosition <= slots.size()) {
            return slots.get(slotPosition - 1);
        } else {
            throw new RuntimeException("Invalid slot position.");
        }
    }

    /**
     * Tests compatibility of the given wheelset with this bogie.
     *
     * @param rotable
     * @return
     */
    public boolean isClassCompatible(final Wheelset rotable) {
        return getRotableClass().isWheelsetClassCompatible(rotable.getRotableClass());
    };

    @Override
    public BogieClass getRotableClass() {
        return (BogieClass) super.getRotableClass();
    }

    @Override
    @Observable
    public Bogie setRotableClass(final RotableClass klass) {
        super.setRotableClass(klass);
        return this;
    }

    @Override
    public String toString() {
        final StringBuffer result = new StringBuffer();
        result.append("Name: " + getKey() + "\n");
        result.append("Desc: " + getDesc() + "\n");
        //	result.append(getRotableClass().toString() + "\n");
        //result.append("\nLocation: " + getLocation());
        return result.toString();
    }

    public Wheelset getWheelsetInSlotPosition(final Integer slotPosition) {
        return getSlot(slotPosition).getWheelset();
    }

    public void defitWheelset(final Wheelset wheelset) {
        for (final BogieSlot bogieSlot : slots) {
            if (bogieSlot.getWheelset() != null && bogieSlot.getWheelset().equals(wheelset)) {
                bogieSlot.setWheelset(null);
            }
        }
    }

}
