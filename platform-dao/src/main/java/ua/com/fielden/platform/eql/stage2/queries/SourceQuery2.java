package ua.com.fielden.platform.eql.stage2.queries;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.stage2.ITransformableFromStage2To3;
import ua.com.fielden.platform.eql.stage2.QueryComponents2;
import ua.com.fielden.platform.eql.stage2.TransformationContextFromStage2To3;
import ua.com.fielden.platform.eql.stage2.TransformationResultFromStage2To3;
import ua.com.fielden.platform.eql.stage3.QueryComponents3;
import ua.com.fielden.platform.eql.stage3.queries.SourceQuery3;

public class SourceQuery2 extends AbstractQuery2 implements ITransformableFromStage2To3<SourceQuery3> {

    public SourceQuery2(final QueryComponents2 queryComponents, final Class<? extends AbstractEntity<?>> resultType) {
        super(queryComponents, resultType);
    }

    @Override
    public TransformationResultFromStage2To3<SourceQuery3> transform(final TransformationContextFromStage2To3 context) {
        final TransformationResultFromStage2To3<QueryComponents3> queryComponentsTr = transformQueryComponents(context);
        return new TransformationResultFromStage2To3<>(new SourceQuery3(queryComponentsTr.item, resultType), queryComponentsTr.updatedContext);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        final int result = super.hashCode();
        return prime * result + SourceQuery2.class.getName().hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj || super.equals(obj) && obj instanceof SourceQuery2;
    }
}