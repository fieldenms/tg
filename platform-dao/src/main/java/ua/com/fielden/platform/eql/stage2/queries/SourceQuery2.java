package ua.com.fielden.platform.eql.stage2.queries;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.stage2.ITransformableToStage3;
import ua.com.fielden.platform.eql.stage2.QueryComponents2;
import ua.com.fielden.platform.eql.stage2.TransformationContext2;
import ua.com.fielden.platform.eql.stage2.TransformationResult2;
import ua.com.fielden.platform.eql.stage3.QueryComponents3;
import ua.com.fielden.platform.eql.stage3.queries.SourceQuery3;

public class SourceQuery2 extends AbstractQuery2 implements ITransformableToStage3<SourceQuery3> {

    public SourceQuery2(final QueryComponents2 queryComponents, final Class<? extends AbstractEntity<?>> resultType) {
        super(queryComponents, resultType);
    }

    @Override
    public TransformationResult2<SourceQuery3> transform(final TransformationContext2 context) {
        final TransformationResult2<QueryComponents3> queryComponentsTr = transformQueryComponents(context);
        return new TransformationResult2<>(new SourceQuery3(queryComponentsTr.item, resultType), queryComponentsTr.updatedContext);
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