package ua.com.fielden.platform.eql.stage2.operands.functions;

import static java.util.Collections.emptySet;

import java.util.Set;

import org.hibernate.type.IntegerType;

import ua.com.fielden.platform.eql.stage2.TransformationContext;
import ua.com.fielden.platform.eql.stage2.TransformationResult;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage3.operands.functions.CountAll3;

public class CountAll2 extends AbstractFunction2<CountAll3> {

    public static CountAll2 INSTANCE = new CountAll2();
    
    private CountAll2() {
        super(Integer.class, IntegerType.INSTANCE);
    }

    @Override
    public TransformationResult<CountAll3> transform(final TransformationContext context) {
        return new TransformationResult<>(CountAll3.INSTANCE, context);
    }

    @Override
    public Set<Prop2> collectProps() {
        return emptySet();
    } 
}