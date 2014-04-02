package ua.com.fielden.platform.example.entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;

/**
 * Represents the compatibility between certain wagon and bogie classes.
 * 
 * @author nc
 * 
 */
@KeyType(DynamicEntityKey.class)
public class WagonClassCompatibility extends AbstractEntity<DynamicEntityKey> {
    private static final long serialVersionUID = 1L;

    @CompositeKeyMember(1)
    @IsProperty
    private WagonClass wagonClass;

    @CompositeKeyMember(2)
    @IsProperty
    private BogieClass bogieClass;

    private String status;

    public WagonClassCompatibility() {
        setKey(new DynamicEntityKey(this));
    }

    public WagonClass getWagonClass() {
        return wagonClass;
    }

    protected void setWagonClass(final WagonClass wagonClass) {
        this.wagonClass = wagonClass;
    }

    public BogieClass getBogieClass() {
        return bogieClass;
    }

    protected void setBogieClass(final BogieClass bogieClass) {
        this.bogieClass = bogieClass;
    }

    public String getStatus() {
        return status;
    }

    protected void setStatus(final String status) {
        this.status = status;
    }
}
