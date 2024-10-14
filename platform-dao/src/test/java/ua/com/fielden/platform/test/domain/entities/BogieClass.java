package ua.com.fielden.platform.test.domain.entities;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.Observable;

import static java.util.Collections.unmodifiableSet;

/**
 * Represents bogie class entity.
 *
 * @author TG Team
 */
public class BogieClass extends RotableClass {

    private static final long serialVersionUID = 1L;

    @IsProperty(BogieClassCompatibility.class)
    private final Set<BogieClassCompatibility> compatibles = new HashSet<>();

    protected BogieClass() {

    }

    public BogieClass(final String code, final String desc, final Integer tonnage) {
        super(code, desc);
        setTonnage(tonnage);
    }

    public Set<BogieClassCompatibility> getCompatibles() {
        return unmodifiableSet(compatibles);
    }

    @Observable
    public BogieClass setCompatibles(Set<BogieClassCompatibility> compatibles) {
        this.compatibles.clear();
        this.compatibles.addAll(compatibles);
        return this;
    }

    public int getNumberOfWheelsets() {
        return 2;
    }

    /**
     * Determines whether given wheelset class is compatible with given bogie class
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
