package ua.com.fielden.platform.gis.gps;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.validation.annotation.CompanionObject;

/**
 * The entity which represents a state of machine actor at some moment of its lifecycle.
 *
 * @author Developers
 *
 */
@KeyType(DynamicEntityKey.class)
@KeyTitle(value = "Machine Server State", desc = "Machine Server State")
@CompanionObject(IMachineServerState.class)
@DescTitle(value = "Machine Server State Description", desc = "Machine Server State Description")
public class MachineServerState extends AbstractEntity<DynamicEntityKey> {

    @IsProperty
    @MapTo
    @Title(value = "Blackout Size", desc = "The size of the 'Blackout' buffer for the current machine processing actor")
    @CompositeKeyMember(1)
    private Integer blackoutSize;

    @IsProperty
    @MapTo
    @Title(value = "Dummy", desc = "Desc")
    @CompositeKeyMember(2)
    private String dummy;

    @Observable
    public MachineServerState setDummy(final String dummy) {
        this.dummy = dummy;
        return this;
    }

    public String getDummy() {
        return dummy;
    }

    @Observable
    public MachineServerState setBlackoutSize(final Integer blackoutSize) {
        this.blackoutSize = blackoutSize;
        return this;
    }

    public Integer getBlackoutSize() {
        return blackoutSize;
    }

}