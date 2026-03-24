package ua.com.fielden.platform.entity.query.metadata.test_entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.*;

import java.util.Date;

/// Required, optional, optional, and required key members.
///
@KeyType(DynamicEntityKey.class)
@MapEntityTo
@CompanionIsGenerated
public class CompositeKeyEqlExpression_Entity11 extends AbstractEntity<DynamicEntityKey> {

    @IsProperty
    @MapTo
    @CompositeKeyMember(1)
    private String name;

    @IsProperty
    @MapTo
    @Optional
    @CompositeKeyMember(2)
    private Date date;

    @IsProperty
    @MapTo
    @Optional
    @CompositeKeyMember(3)
    private Integer count;

    @IsProperty
    @MapTo
    @CompositeKeyMember(4)
    private String description;

    @Observable
    public CompositeKeyEqlExpression_Entity11 setName(final String name) {
        this.name = name;
        return this;
    }

    public String getName() {
        return name;
    }

    @Observable
    public CompositeKeyEqlExpression_Entity11 setDate(final Date date) {
        this.date = date;
        return this;
    }

    public Date getDate() {
        return date;
    }

    @Observable
    public CompositeKeyEqlExpression_Entity11 setCount(final Integer count) {
        this.count = count;
        return this;
    }

    public Integer getCount() {
        return count;
    }

    @Observable
    public CompositeKeyEqlExpression_Entity11 setDescription(final String description) {
        this.description = description;
        return this;
    }

    public String getDescription() {
        return description;
    }

}
