package ua.com.fielden.platform.test.domain.entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;

/**
 * Represents the compatibility between certain wagon and bogie classes.
 * 
 * @author nc
 * 
 */
@KeyType(DynamicEntityKey.class)
public class WagonClassCompatibility extends AbstractEntity<DynamicEntityKey> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @CompositeKeyMember(1)
    private WagonClass wagonClass;

    @IsProperty
    @CompositeKeyMember(2)
    private BogieClass bogieClass;

    @IsProperty
    private String status;

    public WagonClassCompatibility() {
        setKey(new DynamicEntityKey(this));
    }

    public WagonClass getWagonClass() {
        return wagonClass;
    }

    @Observable
    protected void setWagonClass(final WagonClass wagonClass) {
        this.wagonClass = wagonClass;
    }

    public BogieClass getBogieClass() {
        return bogieClass;
    }

    @Observable
    protected void setBogieClass(final BogieClass bogieClass) {
        this.bogieClass = bogieClass;
    }

    public String getStatus() {
        return status;
    }

    @Observable
    protected void setStatus(final String status) {
        this.status = status;
    }
}
