package ua.com.fielden.platform.eql.stage1.operands;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.stage1.TransformationContextFromStage1To2;
import ua.com.fielden.platform.eql.stage1.queries.SubQuery1;
import ua.com.fielden.platform.eql.stage2.operands.QueryBasedSet2;

import java.util.Set;

public record QueryBasedSet1 (SubQuery1 model) implements ISetOperand1<QueryBasedSet2> {

    @Override
    public QueryBasedSet2 transform(final TransformationContextFromStage1To2 context) {
        return new QueryBasedSet2(model.transform(context));
    }

    @Override
    public Set<Class<? extends AbstractEntity<?>>> collectEntityTypes() {
        return model.collectEntityTypes();
    }
    
}
