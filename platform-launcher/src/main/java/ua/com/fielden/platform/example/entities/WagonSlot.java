package ua.com.fielden.platform.example.entities;

import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;

/**
 * Represents slot in the wagon for fitting bogie rotable there.
 * 
 * @author nc
 * 
 */
@KeyType(DynamicEntityKey.class)
public class WagonSlot extends RotableLocation<DynamicEntityKey> {

    private static final long serialVersionUID = 1L;

    @CompositeKeyMember(1)
    @IsProperty
    private Wagon wagon;

    @CompositeKeyMember(2)
    @IsProperty
    private Integer position;

    protected WagonSlot() {
        setKey(new DynamicEntityKey(this));
    }

    public WagonSlot(final Wagon wagon, final Integer position) {
        this();
        setWagon(wagon);
        setPosition(position);
    }

    public Wagon getWagon() {
        return wagon;
    }

    protected void setWagon(final Wagon wagon) {
        this.wagon = wagon;
    }

    public Integer getPosition() {
        return position;
    }

    protected void setPosition(final Integer position) {
        this.position = position;
    }

    /**
     * Retrieves full wagon slot number, which is composed of the wagon serial number, char 'B' and the slot index.
     * 
     * @return
     */
    public String getSlotNumber() {
        return wagon.getSerialNo() + "B" + getSlotIndex();
    }

    /**
     * Retrieves wagon slot index.
     * 
     * @return
     */
    public String getSlotIndex() {
        return String.format("%02d", getPosition());
    }
}
