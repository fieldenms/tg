package ua.com.fielden.platform.eql.stage1.operands;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.stage1.TransformationContextFromStage1To2;
import ua.com.fielden.platform.eql.stage1.queries.SubQuery1;
import ua.com.fielden.platform.eql.stage2.operands.QueryBasedSet2;
import ua.com.fielden.platform.utils.ToString;

import java.util.Set;

public record QueryBasedSet1 (SubQuery1 model) implements ISetOperand1<QueryBasedSet2>, ToString.IFormattable {

    @Override
    public QueryBasedSet2 transform(final TransformationContextFromStage1To2 context) {
        return new QueryBasedSet2(model.transform(context));
    }

    @Override
    public Set<Class<? extends AbstractEntity<?>>> collectEntityTypes() {
        return model.collectEntityTypes();
    }

    @Override
    public String toString() {
        return toString(ToString.standard);
    }

    @Override
    public String toString(final ToString.IFormat format) {
        return format.toString(this)
                .add("model", model)
                .$();
    }

}
