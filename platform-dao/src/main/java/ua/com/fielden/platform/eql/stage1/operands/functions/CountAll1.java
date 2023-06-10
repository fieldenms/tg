package ua.com.fielden.platform.eql.stage1.operands.functions;

import ua.com.fielden.platform.eql.stage1.TransformationContext1;
import ua.com.fielden.platform.eql.stage2.operands.functions.CountAll2;

public class CountAll1 extends AbstractFunction1<CountAll2> {

    public static CountAll1 INSTANCE = new CountAll1();
    
    private CountAll1() {}
    
    @Override
    public CountAll2 transform(final TransformationContext1 context) {
        return CountAll2.INSTANCE;
    }
}