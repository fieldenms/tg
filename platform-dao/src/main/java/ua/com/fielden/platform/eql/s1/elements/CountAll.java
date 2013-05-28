package ua.com.fielden.platform.eql.s1.elements;

import ua.com.fielden.platform.eql.meta.TransformatorToS2;



public class CountAll extends ZeroOperandFunction<ua.com.fielden.platform.eql.s2.elements.CountAll> {

    public CountAll() {
	super("COUNT(*)");
    }

    @Override
    public ua.com.fielden.platform.eql.s2.elements.CountAll transform(TransformatorToS2 resolver) {
	return new ua.com.fielden.platform.eql.s2.elements.CountAll();
    }
}