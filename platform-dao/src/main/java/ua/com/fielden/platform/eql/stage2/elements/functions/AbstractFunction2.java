package ua.com.fielden.platform.eql.stage2.elements.functions;

import ua.com.fielden.platform.eql.stage2.elements.TransformationContext;
import ua.com.fielden.platform.eql.stage2.elements.TransformationResult;
import ua.com.fielden.platform.eql.stage2.elements.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage3.elements.operands.ISingleOperand3;

public abstract class AbstractFunction2<S3 extends ISingleOperand3> implements ISingleOperand2<S3> {

    @Override
    public boolean ignore() {
        return false;
    }
    
    @Override
    public TransformationResult<S3> transform(final TransformationContext transformationContext) {
        // TODO Auto-generated method stub
        return null;
    }
}