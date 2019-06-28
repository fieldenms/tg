package ua.com.fielden.platform.eql.stage2.elements.functions;

import ua.com.fielden.platform.eql.stage3.elements.functions.CountAll3;

public class CountAll2 extends ZeroOperandFunction2<CountAll3> {

    public CountAll2() {
        super("COUNT(*)");
    }

    @Override
    public Class type() {
        return Long.class;
    }
}