package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.gis.gps.AbstractAvlMessage;
/** 
 * Master entity object.
 * 
 * @author Developers
 *
 */
@CompanionObject(ITgMessage.class)
@MapEntityTo("MESSAGES")
public class TgMessage extends AbstractAvlMessage {
    public static final String MACHINE_PROP_ALIAS = "machine";

    @IsProperty
    @MapTo
    @Title(value = "Машина", desc = "Машина, з якої було отримано повідомлення")
    @CompositeKeyMember(1)
    private TgMachine machine;

    @Observable
    public TgMessage setMachine(final TgMachine machine) {
        this.machine = machine;
        return this;
    }

    public TgMachine getMachine() {
        return machine;
    }
}