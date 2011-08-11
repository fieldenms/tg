package ua.com.fielden.platform.example.entities;

import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;

/**
 * Represents wagon class entity.
 *
 * @author 01es
 *
 */
@KeyType(String.class)
@KeyTitle(value="Wagon Class No", desc="Wagon Class Number")
@DescTitle(value="Wagon Class Desc", desc="Wagon Class Description")
public class WagonClass extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    @IsProperty(WagonClassCompatibility.class)
    private Set<WagonClassCompatibility> compatibles;

    private Integer numberOfBogies;
    private Integer numberOfWheelsets;

    private Integer tonnage; // most likely should be immutable

    /**
     * Constructor for Hibernate.
     */
    protected WagonClass() {
    }

    public WagonClass(final String code, final String desc) {
	super(null, code, desc);
    }

    public Integer getTonnage() {
	return tonnage;
    }

    protected void setTonnage(final Integer tonnage) {
	this.tonnage = tonnage;
    }

    public Integer getNumberOfBogies() {
	return numberOfBogies;
    }

    protected void setNumberOfBogies(final Integer numberOfBogies) {
	this.numberOfBogies = numberOfBogies;
    }

    public Integer getNumberOfWheelsets() {
	return numberOfWheelsets;
    }

    protected void setNumberOfWheelsets(final Integer numberOfWheelsets) {
	this.numberOfWheelsets = numberOfWheelsets;
    }

    public Set<WagonClassCompatibility> getCompatibles() {
	return this.compatibles;
    }

    protected void setCompatibles(final Set<WagonClassCompatibility> compatibles) {
	this.compatibles = compatibles;
    }

    /**
     * Determines whether given bogie class is compatible with given wagon class
     *
     * @param bogieClass
     * @return
     */
    public boolean isBogieClassCompatible(final BogieClass bogieClass) {
	for (final WagonClassCompatibility wagonClassCompatibility : compatibles) {
	    if (wagonClassCompatibility.getBogieClass().equals(bogieClass)) {
		return true;
	    }
	}
	return false;
    }
}
