package ua.com.fielden.platform.eql.s1.elements;

import ua.com.fielden.platform.eql.meta.TransformatorToS2;

public class Now extends ZeroOperandFunction<ua.com.fielden.platform.eql.s2.elements.Now> {
    public Now() {
	super("now");
    }

    @Override
    public ua.com.fielden.platform.eql.s2.elements.Now transform(TransformatorToS2 resolver) {
	return new ua.com.fielden.platform.eql.s2.elements.Now();
    }
}