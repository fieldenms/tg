package ua.com.fielden.platform.eql.stage2.elements.functions;

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