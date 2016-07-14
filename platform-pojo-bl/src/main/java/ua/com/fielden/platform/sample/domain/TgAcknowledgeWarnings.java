package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

/** 
 * Master entity object.
 * 
 * @author Developers
 *
 */
@KeyType(String.class)
@KeyTitle(value = "Key", desc = "Some key description")
@CompanionObject(ITgAcknowledgeWarnings.class)
public class TgAcknowledgeWarnings extends AbstractFunctionalEntityWithCentreContext<String> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @MapTo
    @Title(value = "Acknowledged?", desc = "Acknowledged?")
    private boolean acknowledged;

    @Observable
    public TgAcknowledgeWarnings setAcknowledged(final boolean acknowledged) {
        this.acknowledged = acknowledged;
        return this;
    }

    public boolean getAcknowledged() {
        return acknowledged;
    }
}