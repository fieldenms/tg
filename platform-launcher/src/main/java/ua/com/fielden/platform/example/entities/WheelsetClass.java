package ua.com.fielden.platform.example.entities;

/**
 * Represents wheelset class entity.
 * @author nc
 *
 */
public class WheelsetClass extends RotableClass {

    private static final long serialVersionUID = 1L;

    protected WheelsetClass() {

    }

    public WheelsetClass (final String code, final String desc, final Integer tonnage) {
	super(code, desc);
	setTonnage(tonnage);
    }
}
