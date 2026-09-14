package ua.com.fielden.platform.entity.query.metadata.test_entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.*;

import java.util.Date;

/// One required and one optional key member.
///
@KeyType(DynamicEntityKey.class)
@MapEntityTo
@CompanionIsGenerated
public class CompositeKeyEqlExpression_Entity9 extends AbstractEntity<DynamicEntityKey> {

    @IsProperty
    @MapTo
    @CompositeKeyMember(1)
    private String name;

    @IsProperty
    @MapTo
    @Optional
    @CompositeKeyMember(2)
    private Date date;

    @Observable
    public CompositeKeyEqlExpression_Entity9 setName(final String name) {
        this.name = name;
        return this;
    }

    public String getName() {
        return name;
    }

    @Observable
    public CompositeKeyEqlExpression_Entity9 setDate(final Date date) {
        this.date = date;
        return this;
    }

    public Date getDate() {
        return date;
    }

}
