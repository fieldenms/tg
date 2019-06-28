package ua.com.fielden.platform.eql.stage2.elements.functions;

import org.joda.time.DateTime;

import ua.com.fielden.platform.eql.stage3.elements.functions.Now3;


public class Now2 extends ZeroOperandFunction2<Now3> {
    public Now2() {
        super("now");
    }

    @Override
    public Class type() {
        return DateTime.class;
    }
}