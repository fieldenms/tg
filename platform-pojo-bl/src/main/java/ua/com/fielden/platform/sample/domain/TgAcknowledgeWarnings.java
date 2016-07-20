package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.ContinuationData;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Readonly;
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
@EntityTitle(value = "Acknowledge warnings", desc = "Acknowledge warnings of the current initiating entity")
public class TgAcknowledgeWarnings extends ContinuationData<String> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @Title(value = "Acknowledged?", desc = "Acknowledged?")
    private boolean acknowledged;

    @IsProperty
    @Title(value = "All warnings", desc = "Desc")
    @Readonly
    private String allWarnings;

    @Observable
    public TgAcknowledgeWarnings setAllWarnings(final String allWarnings) {
        this.allWarnings = allWarnings;
        return this;
    }

    public String getAllWarnings() {
        return allWarnings;
    }

    @Observable
    public TgAcknowledgeWarnings setAcknowledged(final boolean acknowledged) {
        this.acknowledged = acknowledged;
        return this;
    }

    public boolean getAcknowledged() {
        return acknowledged;
    }
}