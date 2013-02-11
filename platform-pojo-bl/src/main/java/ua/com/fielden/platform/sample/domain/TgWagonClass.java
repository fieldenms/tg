package ua.com.fielden.platform.sample.domain;

import java.util.HashSet;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.validation.annotation.CompanionObject;

@KeyType(String.class)
@MapEntityTo
@DescTitle("Description")
@CompanionObject(ITgWagonClass.class)
public class TgWagonClass extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    @IsProperty(value = TgWagonClassCompatibility.class, linkProperty = "wagonClass")
    private Set<TgWagonClassCompatibility> compatibles = new HashSet<TgWagonClassCompatibility>();
    @IsProperty @MapTo
    private Integer numberOfBogies;
    @IsProperty @MapTo
    private Integer numberOfWheelsets;
    @IsProperty @MapTo
    private Integer tonnage;

    protected TgWagonClass() {}

    public TgWagonClass(final String code, final String desc) {
	super(null, code, desc);
    }

    public Integer getTonnage() {
        return tonnage;
    }

    @Observable
    public TgWagonClass setTonnage(final Integer tonnage) {
        this.tonnage = tonnage;
        return this;
    }

    public Integer getNumberOfBogies() {
        return numberOfBogies;
    }

    @Observable
    public TgWagonClass setNumberOfBogies(final Integer numberOfBogies) {
        this.numberOfBogies = numberOfBogies;
        return this;
    }

    public Integer getNumberOfWheelsets() {
        return numberOfWheelsets;
    }

    @Observable
    public TgWagonClass setNumberOfWheelsets(final Integer numberOfWheelsets) {
        this.numberOfWheelsets = numberOfWheelsets;
        return this;
    }

    public Set<TgWagonClassCompatibility> getCompatibles() {
	return this.compatibles;
    }

    @Observable
    public TgWagonClass setCompatibles(final Set<TgWagonClassCompatibility> compatibles) {
        this.compatibles = compatibles;
        return this;
    }
}