package ua.com.fielden.platform.eql.stage2.operands.functions;

import static java.util.Collections.emptySet;
import static ua.com.fielden.platform.eql.meta.PropType.INTEGER_PROP_TYPE;

import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.stage2.TransformationContextFromStage2To3;
import ua.com.fielden.platform.eql.stage2.TransformationResultFromStage2To3;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage3.operands.functions.CountAll3;

public class CountAll2 extends AbstractFunction2<CountAll3> {

    public static final CountAll2 INSTANCE = new CountAll2();
    
    private CountAll2() {
        super(INTEGER_PROP_TYPE);
    }

    @Override
    public TransformationResultFromStage2To3<CountAll3> transform(final TransformationContextFromStage2To3 context) {
        return new TransformationResultFromStage2To3<>(CountAll3.INSTANCE, context);
    }

    @Override
    public Set<Prop2> collectProps() {
        return emptySet();
    } 
    
    @Override
    public Set<Class<? extends AbstractEntity<?>>> collectEntityTypes() {
        return emptySet();
    } 
}
