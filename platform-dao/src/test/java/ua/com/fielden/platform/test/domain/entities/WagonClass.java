package ua.com.fielden.platform.test.domain.entities;

import java.util.HashSet;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;

/**
 * Represents wagon class entity.
 * 
 * @author 01es
 * 
 */
@KeyType(String.class)
public class WagonClass extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    @IsProperty(WagonClassCompatibility.class)
    private Set<WagonClassCompatibility> compatibles = new HashSet<WagonClassCompatibility>();
    @IsProperty
    private Integer numberOfBogies;
    @IsProperty
    private Integer numberOfWheelsets;
    @IsProperty
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

    @Observable
    protected void setTonnage(final Integer tonnage) {
        this.tonnage = tonnage;
    }

    public Integer getNumberOfBogies() {
        return numberOfBogies;
    }

    @Observable
    protected void setNumberOfBogies(final Integer numberOfBogies) {
        this.numberOfBogies = numberOfBogies;
    }

    public Integer getNumberOfWheelsets() {
        return numberOfWheelsets;
    }

    @Observable
    protected void setNumberOfWheelsets(final Integer numberOfWheelsets) {
        this.numberOfWheelsets = numberOfWheelsets;
    }

    public Set<WagonClassCompatibility> getCompatibles() {
        return this.compatibles;
    }

    @Observable
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
