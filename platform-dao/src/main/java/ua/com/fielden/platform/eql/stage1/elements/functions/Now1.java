package ua.com.fielden.platform.eql.stage1.elements.functions;

import ua.com.fielden.platform.eql.stage1.elements.PropsResolutionContext;
import ua.com.fielden.platform.eql.stage1.elements.TransformationResult;
import ua.com.fielden.platform.eql.stage2.elements.functions.Now2;

public class Now1 extends ZeroOperandFunction1<Now2> {
    public Now1() {
        super(Now1.class.getName());
    }

    @Override
    public TransformationResult<Now2> transform(final PropsResolutionContext context) {
        return new TransformationResult<Now2>(new Now2(), context.cloneNew());
    }
}