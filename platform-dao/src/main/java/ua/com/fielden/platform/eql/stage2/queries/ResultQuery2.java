package ua.com.fielden.platform.eql.stage2.queries;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.stage2.ITransformableFromStage2To3;
import ua.com.fielden.platform.eql.stage2.QueryComponents2;
import ua.com.fielden.platform.eql.stage2.TransformationContextFromStage2To3;
import ua.com.fielden.platform.eql.stage2.TransformationResultFromStage2To3;
import ua.com.fielden.platform.eql.stage3.QueryComponents3;
import ua.com.fielden.platform.eql.stage3.queries.ResultQuery3;

public class ResultQuery2 extends AbstractQuery2 implements ITransformableFromStage2To3<ResultQuery3> {

    public ResultQuery2(final QueryComponents2 queryComponents, final Class<? extends AbstractEntity<?>> resultType) {
        super(queryComponents, resultType);
    }

    @Override
    public TransformationResultFromStage2To3<ResultQuery3> transform(final TransformationContextFromStage2To3 context) {
        final TransformationResultFromStage2To3<QueryComponents3> queryComponentsTr = transformQueryComponents(context);
        return new TransformationResultFromStage2To3<>(new ResultQuery3(queryComponentsTr.item, resultType), queryComponentsTr.updatedContext);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        final int result = super.hashCode();
        return prime * result + ResultQuery2.class.getName().hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj || super.equals(obj) && obj instanceof ResultQuery2;
    }
}