package ua.com.fielden.platform.eql.stage2.elements.functions;

import org.joda.time.DateTime;

import ua.com.fielden.platform.eql.stage2.elements.TransformationContext;
import ua.com.fielden.platform.eql.stage2.elements.TransformationResult;
import ua.com.fielden.platform.eql.stage3.elements.functions.Now3;


public class Now2 extends ZeroOperandFunction2<Now3> {
    public Now2() {
        super(Now2.class.getName());
    }

    @Override
    public Class<DateTime> type() {
        return DateTime.class;
    }

    @Override
    public TransformationResult<Now3> transform(final TransformationContext context) {
        return new TransformationResult<Now3>(new Now3(), context);
    }
}