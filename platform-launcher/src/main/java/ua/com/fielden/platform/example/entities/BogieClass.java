package ua.com.fielden.platform.example.entities;

import java.util.Set;

import ua.com.fielden.platform.entity.annotation.IsProperty;

/**
 * Represents bogie class entity.
 * 
 * @author nc
 * 
 */

public class BogieClass extends RotableClass {

    private static final long serialVersionUID = 1L;

    @IsProperty(BogieClassCompatibility.class)
    private Set<BogieClassCompatibility> compatibles;

    protected BogieClass() {

    }

    public BogieClass(final String code, final String desc, final Integer tonnage) {
	super(code, desc);
	setTonnage(tonnage);
    }

    public Set<BogieClassCompatibility> getCompatibles() {
	return this.compatibles;
    }

    protected void setCompatibles(final Set<BogieClassCompatibility> compatibles) {
	this.compatibles = compatibles;
    }

    public int getNumberOfWheelsets() {
	return 2;
    }

    /**
     * Determines whether given wheelset class is compatible with given bogie
     * class
     * 
     * @param wheelsetClass
     * @return
     */
    public boolean isWheelsetClassCompatible(final WheelsetClass wheelsetClass) {
	for (final BogieClassCompatibility bogieClassCompatibility : compatibles) {
	    if (bogieClassCompatibility.getWheelsetClass().equals(wheelsetClass)) {
		return true;
	    }
	}
	return false;
    }
}
