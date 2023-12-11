package ua.com.fielden.platform.eql.stage2.queries;

import ua.com.fielden.platform.eql.stage2.ITransformableFromStage2To3;
import ua.com.fielden.platform.eql.stage2.QueryComponents2;
import ua.com.fielden.platform.eql.stage2.TransformationContextFromStage2To3;
import ua.com.fielden.platform.eql.stage2.TransformationResultFromStage2To3;
import ua.com.fielden.platform.eql.stage3.QueryComponents3;
import ua.com.fielden.platform.eql.stage3.queries.SubQueryForExists3;

public class SubQueryForExists2 extends AbstractQuery2 implements ITransformableFromStage2To3<SubQueryForExists3> {

    public SubQueryForExists2(final QueryComponents2 queryComponents) {
        super(queryComponents, null);
    }

    @Override
    public TransformationResultFromStage2To3<SubQueryForExists3> transform(final TransformationContextFromStage2To3 context) {
        final TransformationResultFromStage2To3<QueryComponents3> queryComponentsTr = transformQueryComponents(context);
        return new TransformationResultFromStage2To3<>(new SubQueryForExists3(queryComponentsTr.item), queryComponentsTr.updatedContext);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        return prime * result + SubQueryForExists2.class.getName().hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return (this == obj) || super.equals(obj) && obj instanceof SubQueryForExists2;
    }
}