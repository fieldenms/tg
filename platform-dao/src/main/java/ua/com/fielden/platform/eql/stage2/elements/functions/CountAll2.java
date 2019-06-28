package ua.com.fielden.platform.eql.stage2.elements.functions;

import ua.com.fielden.platform.eql.stage2.elements.TransformationContext;
import ua.com.fielden.platform.eql.stage2.elements.TransformationResult;
import ua.com.fielden.platform.eql.stage3.elements.functions.CountAll3;

public class CountAll2 extends ZeroOperandFunction2<CountAll3> {

    public CountAll2() {
        super("COUNT(*)");
    }

    @Override
    public Class<Long> type() {
        return Long.class;
    }

    @Override
    public TransformationResult<CountAll3> transform(final TransformationContext context) {
        return new TransformationResult<CountAll3>(new CountAll3(), context);
    }
}