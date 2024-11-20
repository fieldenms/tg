package ua.com.fielden.platform.eql.stage1.operands.functions;

import static java.util.Collections.emptySet;

import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.stage1.TransformationContextFromStage1To2;
import ua.com.fielden.platform.eql.stage2.operands.functions.CountAll2;
import ua.com.fielden.platform.utils.ToString;

public class CountAll1 implements IFunction1<CountAll2> {

    public static final CountAll1 INSTANCE = new CountAll1();
    
    private CountAll1() {}
    
    @Override
    public CountAll2 transform(final TransformationContextFromStage1To2 context) {
        return CountAll2.INSTANCE;
    }
    
    @Override
    public Set<Class<? extends AbstractEntity<?>>> collectEntityTypes() {
        return emptySet();
    }

    @Override
    public String toString() {
        return ToString.standard.toString(this).$();
    }

}
