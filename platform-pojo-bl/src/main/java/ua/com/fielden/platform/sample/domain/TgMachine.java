package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Readonly;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.gis.gps.AbstractAvlMachine;
/** 
 * Master entity object.
 * 
 * @author Developers
 *
 */
@CompanionObject(ITgMachine.class)
@MapEntityTo
public class TgMachine extends AbstractAvlMachine<TgMessage> {
    private static final long serialVersionUID = 5862862554383165685L;
    
//    @IsProperty(linkProperty = "machine")
//    @Readonly
//    @Calculated
//    @Title(value = "Останнє GPS повідомлення", desc = "Містить інформацію про останнє GPS повідомлення, отримане від GPS модуля.")
//    private TgMessage lastMessage;
//
    @Override
    @Observable
    public TgMachine setLastMessage(final TgMessage lastMessage) {
        // this.lastMessage = lastMessage;
        return this;
    }

    @Override
    public TgMessage getLastMessage() {
        return /* lastMessage */ null;
    }
}