package ua.com.fielden.platform.entity.query.metadata.test_entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.*;

import java.util.Date;

/// Two required key members.
///
@KeyType(value = DynamicEntityKey.class, keyMemberSeparator = "|")
@MapEntityTo
@CompanionIsGenerated
public class CompositeKeyEqlExpression_Entity8 extends AbstractEntity<DynamicEntityKey> {

    @IsProperty
    @MapTo
    @CompositeKeyMember(1)
    private Date date;

    @IsProperty
    @MapTo
    @CompositeKeyMember(2)
    private String name;

    @Observable
    public CompositeKeyEqlExpression_Entity8 setDate(final Date date) {
        this.date = date;
        return this;
    }

    public Date getDate() {
        return date;
    }

    @Observable
    public CompositeKeyEqlExpression_Entity8 setName(final String name) {
        this.name = name;
        return this;
    }

    public String getName() {
        return name;
    }

}
