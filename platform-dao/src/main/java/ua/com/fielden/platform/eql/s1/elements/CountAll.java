package ua.com.fielden.platform.eql.s1.elements;



public class CountAll extends ZeroOperandFunction<ua.com.fielden.platform.eql.s2.elements.CountAll> {

    public CountAll() {
	super("COUNT(*)");
    }

    @Override
    public ua.com.fielden.platform.eql.s2.elements.CountAll transform() {
	return new ua.com.fielden.platform.eql.s2.elements.CountAll();
    }
}