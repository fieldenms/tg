package ua.com.fielden.platform.eql.stage1.functions;

import ua.com.fielden.platform.eql.stage1.TransformationContext;
import ua.com.fielden.platform.eql.stage2.functions.CountAll2;

public class CountAll1 extends AbstractFunction1<CountAll2> {

    public static CountAll1 INSTANCE = new CountAll1();
    
    private CountAll1() {}
    
    @Override
    public CountAll2 transform(final TransformationContext context) {
        return CountAll2.INSTANCE;
    }
}