package ua.com.fielden.platform.test.domain.entities;

/**
 * Represents wheelset class entity.
 * 
 * @author nc
 * 
 */
public class WheelsetClass extends RotableClass {

    private static final long serialVersionUID = 970082519917064704L;

    protected WheelsetClass() {

    }

    public WheelsetClass(final String code, final String desc, final Integer tonnage) {
        super(code, desc);
        setTonnage(tonnage);
    }
}
