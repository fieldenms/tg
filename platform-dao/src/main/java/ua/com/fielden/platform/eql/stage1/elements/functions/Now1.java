package ua.com.fielden.platform.eql.stage1.elements.functions;

import ua.com.fielden.platform.eql.stage1.elements.PropsResolutionContext;
import ua.com.fielden.platform.eql.stage1.elements.TransformationResult;
import ua.com.fielden.platform.eql.stage2.elements.functions.Now2;

public class Now1 extends AbstractFunction1<Now2> {

    @Override
    public TransformationResult<Now2> transform(final PropsResolutionContext context) {
        return new TransformationResult<Now2>(new Now2(), context.cloneNew());
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        final int result = super.hashCode();
        return prime * result + Now1.class.getName().hashCode();
    }
    
    @Override
    public boolean equals(final Object obj) {
        return this == obj || obj instanceof Now1;
    } 
}