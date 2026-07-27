package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.entity.annotation.mutator.BeforeChange;
import ua.com.fielden.platform.entity.annotation.mutator.Handler;
import ua.com.fielden.platform.entity.validation.MaxLengthValidator;

@KeyType(DynamicEntityKey.class)
@EntityTitle(value = "Inventory Part", desc = "Inventory Part - a spare part or component kept in stock by the organisation.")
@KeyTitle(value = "ACNZ No", desc = "ACNZ Number")
@CompanionObject(TgInventoryPartCo.class)
@MapEntityTo
@DescTitle("Long Description")
@DisplayDescription
@DescRequired
@DeactivatableDependencies(TgInventory.class)
public class TgInventoryPart extends ActivatableAbstractEntity<DynamicEntityKey> {

    @IsProperty(length = 32)
    @MapTo
    @CompositeKeyMember(1)
    @BeforeChange(@Handler(MaxLengthValidator.class))
    private String number;

    @Override
    @Observable
    public TgInventoryPart setActive(boolean active) {
        super.setActive(active);
        return this;
    }

    @Observable
    public TgInventoryPart setNumber(final String number) {
        this.number = number;
        return this;
    }

    public String getNumber() {
        return number;
    }

    @Override
    @Observable
    public TgInventoryPart setDesc(String desc) {
        return super.setDesc(desc);
    }

}
