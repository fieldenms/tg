package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.*;

@KeyType(DynamicEntityKey.class)
@DescTitle("Description")
@DisplayDescription
@DescReadonly
@CompanionObject(TgInventoryCo.class)
@MapEntityTo
@DeactivatableDependencies(TgInventoryBin.class)
public class TgInventory extends ActivatableAbstractEntity<DynamicEntityKey> {

    @IsProperty
    @MapTo
    @CompositeKeyMember(1)
    private TgInventoryPart inventoryPart;

    @Override
    @Observable
    public TgInventory setActive(boolean active) {
        super.setActive(active);
        return this;
    }

    @Observable
    public TgInventory setInventoryPart(final TgInventoryPart inventoryPart) {
        this.inventoryPart = inventoryPart;
        return this;
    }

    public TgInventoryPart getInventoryPart() {
        return inventoryPart;
    }

}
