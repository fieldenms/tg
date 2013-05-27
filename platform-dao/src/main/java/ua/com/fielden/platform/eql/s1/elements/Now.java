package ua.com.fielden.platform.eql.s1.elements;

public class Now extends ZeroOperandFunction<ua.com.fielden.platform.eql.s2.elements.Now> {
    public Now() {
	super("now");
    }

    @Override
    public ua.com.fielden.platform.eql.s2.elements.Now transform() {
	return new ua.com.fielden.platform.eql.s2.elements.Now();
    }
}