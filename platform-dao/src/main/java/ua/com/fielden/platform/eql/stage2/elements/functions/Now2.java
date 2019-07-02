package ua.com.fielden.platform.eql.stage2.elements.functions;

import org.joda.time.DateTime;

import ua.com.fielden.platform.eql.stage2.elements.TransformationContext;
import ua.com.fielden.platform.eql.stage2.elements.TransformationResult;
import ua.com.fielden.platform.eql.stage3.elements.functions.Now3;


public class Now2 extends AbstractFunction2<Now3> {

    @Override
    public Class<DateTime> type() {
        return DateTime.class;
    }

    @Override
    public TransformationResult<Now3> transform(final TransformationContext context) {
        return new TransformationResult<Now3>(new Now3(), context);
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        final int result = 1;
        return prime * result + Now2.class.getName().hashCode();
    }
    
    @Override
    public boolean equals(final Object obj) {
        return this == obj || obj instanceof Now2;
    } 
}