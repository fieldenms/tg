package ua.com.fielden.platform.eql.stage2.operands;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.stage2.TransformationContextFromStage2To3;
import ua.com.fielden.platform.eql.stage2.TransformationResultFromStage2To3;
import ua.com.fielden.platform.eql.stage2.queries.SubQuery2;
import ua.com.fielden.platform.eql.stage3.operands.QueryBasedSet3;
import ua.com.fielden.platform.eql.stage3.queries.SubQuery3;
import ua.com.fielden.platform.utils.ToString;

import java.util.Set;

public record QueryBasedSet2 (SubQuery2 model) implements ISetOperand2<QueryBasedSet3>, ToString.IFormattable {

    @Override
    public TransformationResultFromStage2To3<QueryBasedSet3> transform(final TransformationContextFromStage2To3 context) {
        final TransformationResultFromStage2To3<SubQuery3> modelTr = model.transform(context);
        return new TransformationResultFromStage2To3<>(new QueryBasedSet3(modelTr.item), modelTr.updatedContext);
    }

    @Override
    public Set<Prop2> collectProps() {
        return model.collectProps();
    }
    
    @Override
    public Set<Class<? extends AbstractEntity<?>>> collectEntityTypes() {
        return model.collectEntityTypes();
    }

    @Override
    public String toString() {
        return toString(ToString.separateLines);
    }

    @Override
    public String toString(final ToString.IFormat format) {
        return format.toString(this)
                .add("model", model)
                .$();
    }

}
