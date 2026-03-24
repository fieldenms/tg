package ua.com.fielden.platform.entity.query.metadata.test_entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.sample.domain.TgPerson;

/// Single required, entity key member.
///
@KeyType(DynamicEntityKey.class)
@MapEntityTo
@CompanionIsGenerated
public class CompositeKeyEqlExpression_Entity5 extends AbstractEntity<DynamicEntityKey> {

    @IsProperty
    @MapTo
    @CompositeKeyMember(1)
    private TgPerson person;

    @Observable
    public CompositeKeyEqlExpression_Entity5 setPerson(final TgPerson person) {
        this.person = person;
        return this;
    }

    public TgPerson getPerson() {
        return person;
    }

}
