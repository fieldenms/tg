package ua.com.fielden.platform.entity.query.metadata.test_entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.*;

import java.util.Date;

/// Two optional key members.
///
@KeyType(DynamicEntityKey.class)
@MapEntityTo
@CompanionIsGenerated
public class CompositeKeyEqlExpression_Entity7 extends AbstractEntity<DynamicEntityKey> {

    @IsProperty
    @MapTo
    @Optional
    @CompositeKeyMember(1)
    private Date date1;

    @IsProperty
    @MapTo
    @Optional
    @CompositeKeyMember(2)
    private Date date2;

    @Observable
    public CompositeKeyEqlExpression_Entity7 setDate1(final Date date1) {
        this.date1 = date1;
        return this;
    }

    public Date getDate1() {
        return date1;
    }

    @Observable
    public CompositeKeyEqlExpression_Entity7 setDate2(final Date date2) {
        this.date2 = date2;
        return this;
    }

    public Date getDate2() {
        return date2;
    }

}
