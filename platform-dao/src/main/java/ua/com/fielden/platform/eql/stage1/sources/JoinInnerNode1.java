package ua.com.fielden.platform.eql.stage1.sources;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.enums.JoinType;
import ua.com.fielden.platform.eql.stage1.TransformationContextFromStage1To2;
import ua.com.fielden.platform.eql.stage1.TransformationResultFromStage1To2;
import ua.com.fielden.platform.eql.stage1.conditions.Conditions1;
import ua.com.fielden.platform.eql.stage2.conditions.Conditions2;
import ua.com.fielden.platform.eql.stage2.sources.IJoinNode2;
import ua.com.fielden.platform.eql.stage2.sources.ISource2;
import ua.com.fielden.platform.eql.stage2.sources.JoinInnerNode2;
import ua.com.fielden.platform.utils.ToString;

import java.util.HashSet;
import java.util.Set;

public record JoinInnerNode1 (IJoinNode1<? extends IJoinNode2<?>> leftNode,
                              IJoinNode1<? extends IJoinNode2<?>> rightNode,
                              JoinType joinType,
                              Conditions1 joinConditions)
        implements IJoinNode1<JoinInnerNode2>, ToString.IFormattable
{

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
    public String toString() {
        return toString(ToString.separateLines);
    }

    @Override
    public String toString(final ToString.IFormat format) {
        return format.toString(this)
                .add("type", joinType)
                .add("left", leftNode)
                .add("right", rightNode)
                .addIfNot("conditions", joinConditions, Conditions1::isEmpty)
                .$();
    }

}
