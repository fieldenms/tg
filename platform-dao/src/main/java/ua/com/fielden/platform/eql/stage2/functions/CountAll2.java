package ua.com.fielden.platform.eql.stage2.functions;

import static java.util.Collections.emptySet;

import java.util.Set;

import org.hibernate.type.IntegerType;

import ua.com.fielden.platform.eql.stage2.TransformationContext;
import ua.com.fielden.platform.eql.stage2.TransformationResult;
import ua.com.fielden.platform.eql.stage2.operands.EntProp2;
import ua.com.fielden.platform.eql.stage3.functions.CountAll3;

public class CountAll2 extends AbstractFunction2<CountAll3> {

    @Override
    public Class<Integer> type() {
        return Integer.class;
    }
    
    @Override
    public Object hibType() {
        return IntegerType.INSTANCE;
    }

    @Override
    public TransformationResult<CountAll3> transform(final TransformationContext context) {
        return new TransformationResult<CountAll3>(new CountAll3(), context);
    }

    @Override
    public Set<EntProp2> collectProps() {
        return emptySet();
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