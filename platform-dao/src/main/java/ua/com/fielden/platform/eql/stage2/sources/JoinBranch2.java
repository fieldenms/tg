package ua.com.fielden.platform.eql.stage2.sources;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import ua.com.fielden.platform.entity.query.fluent.enums.JoinType;
import ua.com.fielden.platform.eql.stage2.TransformationContext2;
import ua.com.fielden.platform.eql.stage2.TransformationResult2;
import ua.com.fielden.platform.eql.stage2.conditions.Conditions2;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage3.conditions.Conditions3;
import ua.com.fielden.platform.eql.stage3.sources.IJoinNode3;
import ua.com.fielden.platform.eql.stage3.sources.ISource3;
import ua.com.fielden.platform.eql.stage3.sources.JoinBranch3;

public class JoinBranch2 implements IJoinNode2<JoinBranch3> {
    public final IJoinNode2<? extends IJoinNode3> leftNode;
    public final IJoinNode2<? extends IJoinNode3> rightNode;
    public final JoinType joinType;
    public final Conditions2 joinConditions;

    public JoinBranch2(final IJoinNode2<?> leftNode, final IJoinNode2<?> rightNode, final JoinType joinType, final Conditions2 joinConditions) {
        this.leftNode = leftNode;
        this.rightNode = rightNode;
        this.joinType = joinType;
        this.joinConditions = joinConditions;
    }

    @Override
    public TransformationResult2<JoinBranch3> transform(TransformationContext2 context) {
        final TransformationResult2<? extends IJoinNode3> lsTransformed = leftNode.transform(context);
        final TransformationResult2<? extends IJoinNode3> rsTransformed = rightNode.transform(lsTransformed.updatedContext);
        final TransformationResult2<Conditions3> jcTransformed = joinConditions.transform(rsTransformed.updatedContext);
        return new TransformationResult2<>(new JoinBranch3(lsTransformed.item, rsTransformed.item, joinType, jcTransformed.item), jcTransformed.updatedContext);
    }

    @Override
    public Set<Prop2> collectProps() {
        final Set<Prop2> result = new HashSet<>(); 
        result.addAll(leftNode.collectProps());
        result.addAll(rightNode.collectProps());
        result.addAll(joinConditions.collectProps());
        return result;
    }

    @Override
    public ISource2<? extends ISource3> mainSource() {
        return leftNode.mainSource();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + leftNode.hashCode();
        result = prime * result + rightNode.hashCode();
        result = prime * result + joinConditions.hashCode();
        result = prime * result + joinType.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof JoinBranch2)) {
            return false;
        }
        
        final JoinBranch2 other = (JoinBranch2) obj;
        
        return Objects.equals(leftNode, other.leftNode) &&
                Objects.equals(rightNode, other.rightNode) &&
                Objects.equals(joinType, other.joinType) &&
                Objects.equals(joinConditions, other.joinConditions);
    }
}