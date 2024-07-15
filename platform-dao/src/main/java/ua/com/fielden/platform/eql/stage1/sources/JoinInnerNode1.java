package ua.com.fielden.platform.eql.stage1.sources;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.enums.JoinType;
import ua.com.fielden.platform.eql.stage1.TransformationContextFromStage1To2;
import ua.com.fielden.platform.eql.stage1.TransformationResultFromStage1To2;
import ua.com.fielden.platform.eql.stage1.conditions.Conditions1;
import ua.com.fielden.platform.eql.stage2.conditions.Conditions2;
import ua.com.fielden.platform.eql.stage2.sources.IJoinNode2;
import ua.com.fielden.platform.eql.stage2.sources.ISource2;
import ua.com.fielden.platform.eql.stage2.sources.JoinInnerNode2;

public class JoinInnerNode1 implements IJoinNode1<JoinInnerNode2> {
    public final IJoinNode1<? extends IJoinNode2<?>> leftNode;
    public final IJoinNode1<? extends IJoinNode2<?>> rightNode;
    public final JoinType joinType;
    public final Conditions1 joinConditions;

    public JoinInnerNode1(final IJoinNode1<?> leftNode, final IJoinNode1<?> rightNode, final JoinType joinType, final Conditions1 joinConditions) {
        this.leftNode = leftNode;
        this.rightNode = rightNode;
        this.joinType = joinType;
        this.joinConditions = joinConditions;
    }

    @Override
    public TransformationResultFromStage1To2<JoinInnerNode2> transform(final TransformationContextFromStage1To2 context) {
        final TransformationResultFromStage1To2<? extends IJoinNode2<?>> lsTransformed = leftNode.transform(context);
        final TransformationResultFromStage1To2<? extends IJoinNode2<?>> rsTransformed = rightNode.transform(context);
        final TransformationContextFromStage1To2 updatedContext = context.cloneWithAdded(lsTransformed.updatedContext.getCurrentLevelSources(), rsTransformed.updatedContext.getCurrentLevelSources());
        final Conditions2 jcTransformed = joinConditions.transform(updatedContext);
        return new TransformationResultFromStage1To2<>(new JoinInnerNode2(lsTransformed.item, rsTransformed.item, joinType, jcTransformed), updatedContext);
    }

    @Override
    public ISource1<? extends ISource2<?>> mainSource() {
        return leftNode.mainSource();
    }

    @Override
    public Set<Class<? extends AbstractEntity<?>>> collectEntityTypes() {
        final Set<Class<? extends AbstractEntity<?>>> result = new HashSet<>(); 
        result.addAll(leftNode.collectEntityTypes());
        result.addAll(rightNode.collectEntityTypes());
        result.addAll(joinConditions.collectEntityTypes());
        return result;
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

        if (!(obj instanceof JoinInnerNode1)) {
            return false;
        }
        
        final JoinInnerNode1 other = (JoinInnerNode1) obj;
        
        return Objects.equals(leftNode, other.leftNode) &&
                Objects.equals(rightNode, other.rightNode) &&
                Objects.equals(joinType, other.joinType) &&
                Objects.equals(joinConditions, other.joinConditions);
    }
}