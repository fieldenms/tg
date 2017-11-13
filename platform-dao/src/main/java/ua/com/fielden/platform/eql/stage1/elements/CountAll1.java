package ua.com.fielden.platform.eql.stage1.elements;

import ua.com.fielden.platform.eql.meta.TransformatorToS2;
import ua.com.fielden.platform.eql.stage2.elements.CountAll2;

public class CountAll1 extends ZeroOperandFunction1<CountAll2> {

    public CountAll1() {
        super("COUNT(*)");
    }

    @Override
    public CountAll2 transform(final TransformatorToS2 resolver) {
        return new CountAll2();
    }
}