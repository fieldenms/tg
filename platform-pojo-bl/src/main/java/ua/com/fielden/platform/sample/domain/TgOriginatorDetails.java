package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.*;

@KeyType(DynamicEntityKey.class)
@KeyTitle("Originator Details")
@CompanionObject(TgOriginatorDetailsCo.class)
@MapEntityTo
public class TgOriginatorDetails extends ActivatableAbstractEntity<DynamicEntityKey> {

    @IsProperty
    @MapTo
    @Title(value = "Originator")
    @CompositeKeyMember(1)
    private TgOriginator originator;

    @IsProperty(length = 255)
    @MapTo
    @Title(value = "Comment")
    @CompositeKeyMember(2)
    @Optional
    private String comment;

    @Observable
    public TgOriginatorDetails setComment(final String comment) {
        this.comment = comment;
        return this;
    }

    public String getComment() {
        return comment;
    }

    @Observable
    public TgOriginatorDetails setOriginator(final TgOriginator originator) {
        this.originator = originator;
        return this;
    }

    public TgOriginator getOriginator() {
        return originator;
    }

    @Observable
    public TgOriginatorDetails setActive(final boolean active) {
        super.setActive(active);
        return this;
    }

}
