package ua.com.fielden.platform.test.domain.entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;

import java.util.HashSet;
import java.util.Set;

import static java.util.Collections.unmodifiableSet;

/**
 * Represents wagon class entity.
 *
 * @author 01es
 */
@KeyType(String.class)
public class WagonClass extends AbstractEntity<String> {

    private static final long serialVersionUID = 1L;

    @IsProperty(WagonClassCompatibility.class)
    private final Set<WagonClassCompatibility> compatibles = new HashSet<>();
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
        return unmodifiableSet(compatibles);
    }

    @Observable
    protected WagonClass setCompatibles(final Set<WagonClassCompatibility> compatibles) {
        this.compatibles.clear();
        this.compatibles.addAll(compatibles);
        return this;
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
