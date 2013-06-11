package ua.com.fielden.platform.eql.s2.elements;

import org.joda.time.DateTime;

public class Now2 extends ZeroOperandFunction2 {
    public Now2() {
	super("now");
    }

    @Override
    public Class type() {
	return DateTime.class;
    }
}