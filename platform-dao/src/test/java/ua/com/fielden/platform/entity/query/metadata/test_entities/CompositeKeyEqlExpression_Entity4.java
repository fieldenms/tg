package ua.com.fielden.platform.entity.query.metadata.test_entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.*;

/// Single optional, string key member.
///
@KeyType(DynamicEntityKey.class)
@MapEntityTo
@CompanionIsGenerated
public class CompositeKeyEqlExpression_Entity4 extends AbstractEntity<DynamicEntityKey> {

    @IsProperty
    @MapTo
    @Optional
    @CompositeKeyMember(1)
    private String name;

    @Observable
    public CompositeKeyEqlExpression_Entity4 setName(final String name) {
        this.name = name;
        return this;
    }

    public String getName() {
        return name;
    }

}
