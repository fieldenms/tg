package ua.com.fielden.platform.entity.query.metadata.test_entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.*;

import java.util.Date;

/// Single optional, non-string key member.
///
@KeyType(DynamicEntityKey.class)
@MapEntityTo
@CompanionIsGenerated
public class CompositeKeyEqlExpression_Entity2 extends AbstractEntity<DynamicEntityKey> {

    @IsProperty
    @MapTo
    @Optional
    @CompositeKeyMember(1)
    private Date date;

    @Observable
    public CompositeKeyEqlExpression_Entity2 setDate(final Date date) {
        this.date = date;
        return this;
    }

    public Date getDate() {
        return date;
    }

}
