package ua.com.fielden.platform.entity.query.metadata.test_entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.types.markers.IMoneyType;

/// Composite entity with a required money-with-currency key member.
///
@KeyType(DynamicEntityKey.class)
@MapEntityTo
@CompanionIsGenerated
public class CompositeKeyEqlExpression_Entity12 extends AbstractEntity<DynamicEntityKey> {

    @IsProperty
    @MapTo
    @CompositeKeyMember(1)
    private String name;

    @IsProperty
    @MapTo
    @CompositeKeyMember(2)
    @PersistentType(userType = IMoneyType.class)
    private Money price;

    @Observable
    public CompositeKeyEqlExpression_Entity12 setName(final String name) {
        this.name = name;
        return this;
    }

    public String getName() {
        return name;
    }

    @Observable
    public CompositeKeyEqlExpression_Entity12 setPrice(final Money price) {
        this.price = price;
        return this;
    }

    public Money getPrice() {
        return price;
    }
}
