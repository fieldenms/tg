package ua.com.fielden.platform.example.entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;

/**
 * Represents the compatibility between certain bogie and wheelset classes.
 * 
 * @author nc
 * 
 */
@KeyType(DynamicEntityKey.class)
public class BogieClassCompatibility extends AbstractEntity<DynamicEntityKey> {
    private static final long serialVersionUID = 1L;

    @CompositeKeyMember(1)
    @IsProperty
    private BogieClass bogieClass;

    @CompositeKeyMember(2)
    @IsProperty
    private WheelsetClass wheelsetClass;

    private String status;

    public BogieClassCompatibility() {
        setKey(new DynamicEntityKey(this));
    }

    public BogieClass getBogieClass() {
        return bogieClass;
    }

    public void setBogieClass(final BogieClass bogieClass) {
        this.bogieClass = bogieClass;
    }

    public WheelsetClass getWheelsetClass() {
        return wheelsetClass;
    }

    public void setWheelsetClass(final WheelsetClass wheelsetClass) {
        this.wheelsetClass = wheelsetClass;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }
}
