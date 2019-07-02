package ua.com.fielden.platform.eql.stage2.elements.functions;

import ua.com.fielden.platform.eql.stage2.elements.TransformationContext;
import ua.com.fielden.platform.eql.stage2.elements.TransformationResult;
import ua.com.fielden.platform.eql.stage3.elements.functions.CountAll3;

public class CountAll2 extends AbstractFunction2<CountAll3> {

    @Override
    public Class<Long> type() {
        return Long.class;
    }

    @Override
    public TransformationResult<CountAll3> transform(final TransformationContext context) {
        return new TransformationResult<CountAll3>(new CountAll3(), context);
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        final int result = 1;
        return prime * result + CountAll2.class.getName().hashCode();
    }
    
    @Override
    public boolean equals(final Object obj) {
        return this == obj || obj instanceof CountAll2;
    } 
}