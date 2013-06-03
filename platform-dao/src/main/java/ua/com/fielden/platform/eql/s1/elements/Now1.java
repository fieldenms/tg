package ua.com.fielden.platform.eql.s1.elements;

import ua.com.fielden.platform.eql.meta.TransformatorToS2;
import ua.com.fielden.platform.eql.s2.elements.Now2;

public class Now1 extends ZeroOperandFunction1<Now2> {
    public Now1() {
	super("now");
    }

    @Override
    public Now2 transform(final TransformatorToS2 resolver) {
	return new Now2();
    }
}