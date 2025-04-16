package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.entity.annotation.mutator.BeforeChange;
import ua.com.fielden.platform.entity.annotation.mutator.Handler;
import ua.com.fielden.platform.entity.validation.MaxLengthValidator;

@KeyType(value = DynamicEntityKey.class, keyMemberSeparator = " / ")
@EntityTitle("Inventory Bin")
@KeyTitle("Inventory Bin")
@CompanionObject(TgInventoryBinCo.class)
@MapEntityTo
@DescTitle("Description")
@DisplayDescription
public class TgInventoryBin extends ActivatableAbstractEntity<DynamicEntityKey> {

    @IsProperty
    @MapTo
    @CompositeKeyMember(1)
    private TgInventory inventory;

    @IsProperty(length = 10)
    @MapTo
    @BeforeChange(@Handler(MaxLengthValidator.class))
    private String bin;

    @Override
    @Observable
    public TgInventoryBin setActive(boolean active) {
        super.setActive(active);
        return this;
    }

    @Observable
    public TgInventoryBin setBin(final String bin) {
        this.bin = bin;
        return this;
    }

    public String getBin() {
        return bin;
    }

    @Observable
    public TgInventoryBin setInventory(final TgInventory inventory) {
        this.inventory = inventory;
        return this;
    }

    public TgInventory getInventory() {
        return inventory;
    }

}
