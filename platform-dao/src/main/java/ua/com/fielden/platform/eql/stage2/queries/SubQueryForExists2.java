package ua.com.fielden.platform.eql.stage2.queries;

import ua.com.fielden.platform.eql.stage2.ITransformableToStage3;
import ua.com.fielden.platform.eql.stage2.QueryComponents2;
import ua.com.fielden.platform.eql.stage2.TransformationContext2;
import ua.com.fielden.platform.eql.stage2.TransformationResult2;
import ua.com.fielden.platform.eql.stage3.QueryComponents3;
import ua.com.fielden.platform.eql.stage3.queries.SubQueryForExists3;

public class SubQueryForExists2 extends AbstractQuery2 implements ITransformableToStage3<SubQueryForExists3> {

    public SubQueryForExists2(final QueryComponents2 queryComponents) {
        super(queryComponents, null);
    }

    @Override
    public TransformationResult2<SubQueryForExists3> transform(final TransformationContext2 context) {
        final TransformationResult2<QueryComponents3> queryComponentsTr = transformQueryComponents(context);
        return new TransformationResult2<>(new SubQueryForExists3(queryComponentsTr.item), queryComponentsTr.updatedContext);
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