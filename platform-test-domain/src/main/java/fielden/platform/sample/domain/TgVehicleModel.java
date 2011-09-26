package fielden.platform.sample.domain;

import org.junit.Ignore;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

@KeyType(String.class)
@MapEntityTo
@DescTitle("Description")
@Ignore
public class TgVehicleModel extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @MapTo
    @Title(value = "Test vehicle model", desc = "Test vehicle model")
    private TgVehicleMake make;

    @Observable
    public TgVehicleModel setMake(final TgVehicleMake make) {
	this.make = make;
	return this;
    }

    public TgVehicleMake getMake() {
	return make;
    }

    /**
     * Constructor for (@link EntityFactory}.
     */
    protected TgVehicleModel() {
    }
}
