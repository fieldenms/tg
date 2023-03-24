package ua.com.fielden.platform.eql.stage2.operands.functions;

import static java.util.Collections.emptySet;

import java.util.Set;

import ua.com.fielden.platform.eql.stage2.TransformationContext2;
import ua.com.fielden.platform.eql.stage2.TransformationResult2;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage3.operands.functions.CountAll3;

public class CountAll2 extends AbstractFunction2<CountAll3> {

    public static CountAll2 INSTANCE = new CountAll2();
    
    private CountAll2() {
        super(Integer.class);
    }

    @Override
    public TransformationResult2<CountAll3> transform(final TransformationContext2 context) {
        return new TransformationResult2<>(CountAll3.INSTANCE, context);
    }

    @Override
    public Set<Prop2> collectProps() {
        return emptySet();
    } 
}