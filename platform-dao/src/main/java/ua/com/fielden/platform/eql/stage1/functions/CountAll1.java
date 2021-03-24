package ua.com.fielden.platform.eql.stage1.functions;

import ua.com.fielden.platform.eql.stage1.TransformationContext;
import ua.com.fielden.platform.eql.stage2.functions.CountAll2;

public class CountAll1 extends AbstractFunction1<CountAll2> {

    @Override
    public CountAll2 transform(final TransformationContext context) {
        return new CountAll2();
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        final int result = 1;
        return prime * result + CountAll1.class.getName().hashCode();
    }
    
    @Override
    public boolean equals(final Object obj) {
        return this == obj || obj instanceof CountAll1;
    } 
}