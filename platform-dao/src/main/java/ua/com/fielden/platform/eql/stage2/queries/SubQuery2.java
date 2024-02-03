package ua.com.fielden.platform.eql.stage2.queries;

import java.util.Objects;

import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.eql.stage2.QueryComponents2;
import ua.com.fielden.platform.eql.stage2.TransformationContextFromStage2To3;
import ua.com.fielden.platform.eql.stage2.TransformationResultFromStage2To3;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage3.QueryComponents3;
import ua.com.fielden.platform.eql.stage3.queries.SubQuery3;

public class SubQuery2 extends AbstractQuery2 implements ISingleOperand2<SubQuery3> {
    public final boolean isRefetchOnly;
    public final PropType type;

    public SubQuery2(final QueryComponents2 queryComponents, final PropType type, final boolean isRefetchOnly) {
        super(queryComponents, type.javaType());
        this.isRefetchOnly = isRefetchOnly;
        this.type = type;
    }

    @Override
    public TransformationResultFromStage2To3<SubQuery3> transform(final TransformationContextFromStage2To3 context) {
        final TransformationResultFromStage2To3<QueryComponents3> queryComponentsTr = transformQueryComponents(context);
        return new TransformationResultFromStage2To3<>(new SubQuery3(queryComponentsTr.item, type), queryComponentsTr.updatedContext);
    }

    @Override
    public PropType type() {
        return type;
    }

    @Override
    public boolean ignore() {
        return false;
    }

    @Override
    public boolean isNonnullableEntity() {
        return isRefetchOnly;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + type.hashCode();
        result = prime * result + (isRefetchOnly ? 1231 : 1237);
        return prime * result + SubQuery2.class.getName().hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!super.equals(obj)) {
            return false;
        }

        if (!(obj instanceof SubQuery2)) {
            return false;
        }

        final SubQuery2 other = (SubQuery2) obj;

        return Objects.equals(type, other.type) && (isRefetchOnly == other.isRefetchOnly);
    }
}