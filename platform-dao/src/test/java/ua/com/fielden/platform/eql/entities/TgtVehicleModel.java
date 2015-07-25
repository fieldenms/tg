package ua.com.fielden.platform.eql.entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;

@KeyType(DynamicEntityKey.class)
@MapEntityTo
public class TgtVehicleModel extends AbstractEntity<DynamicEntityKey> {  

    @IsProperty
    @MapTo
    @CompositeKeyMember(1)
    private TgtVehicleMake make;

    @IsProperty
    @MapTo
    @CompositeKeyMember(2)
    private String name;

    @Observable
    public TgtVehicleModel setName(final String name) {
        this.name = name;
        return this;
    }

    public String getName() {
        return name;
    }

    
    @Observable
    public TgtVehicleModel setMake(final TgtVehicleMake make) {
        this.make = make;
        return this;
    }

    public TgtVehicleMake getMake() {
        return make;
    }
}