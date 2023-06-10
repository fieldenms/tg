package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;

@KeyType(String.class)
@MapEntityTo(autoConflictResolution = false)
@DescTitle("Description")
@CompanionObject(ITgBogieClass.class)
public class TgBogieClass extends ActivatableAbstractEntity<String> {

    //    @IsProperty(BogieClassCompatibility.class)
    //    private Set<BogieClassCompatibility> compatibles = new HashSet<BogieClassCompatibility>();
    @IsProperty
    @MapTo
    private Integer tonnage;

    protected TgBogieClass() {

    }

    public Integer getTonnage() {
        return tonnage;
    }

    @Observable
    public TgBogieClass setTonnage(final Integer tonnage) {
        this.tonnage = tonnage;
        return this;
    }

    //    public Set<BogieClassCompatibility> getCompatibles() {
    //	return this.compatibles;
    //    }
    //
    //    @Observable
    //    protected void setCompatibles(final Set<BogieClassCompatibility> compatibles) {
    //	this.compatibles = compatibles;
    //    }
    //
    public int getNumberOfWheelsets() {
        return 2;
    }

    @Observable
    @Override
    protected TgBogieClass setActive(boolean active) {
        super.setActive(active);
        return this;
    }

}