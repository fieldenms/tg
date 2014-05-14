package ua.com.fielden.platform.example.swing.booking;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.validation.annotation.NotNull;

@KeyTitle(value = "Veh No", desc = "vehicle number")
@KeyType(String.class)
@DescTitle(value = "Description", desc = "Vehicle description")
@EntityTitle(value = "Vehicle", desc = "Domain entity representing a vehicle.")
public class VehicleEntity extends AbstractEntity<String> {

    private static final long serialVersionUID = -4000034917653651392L;

    @Override
    @NotNull
    @Observable
    public VehicleEntity setKey(final String key) {
        return (VehicleEntity) super.setKey(key);
    }

    @Override
    @Observable
    public VehicleEntity setDesc(final String desc) {
        return (VehicleEntity) super.setDesc(desc);
    }
}
