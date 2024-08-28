package ua.com.fielden.platform.eql.stage2.sources;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.enums.JoinType;
import ua.com.fielden.platform.eql.stage2.TransformationContextFromStage2To3;
import ua.com.fielden.platform.eql.stage2.TransformationResultFromStage2To3;
import ua.com.fielden.platform.eql.stage2.conditions.Conditions2;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage3.conditions.Conditions3;
import ua.com.fielden.platform.eql.stage3.sources.IJoinNode3;
import ua.com.fielden.platform.eql.stage3.sources.ISource3;
import ua.com.fielden.platform.eql.stage3.sources.JoinInnerNode3;
import ua.com.fielden.platform.utils.ToString;

import java.util.HashSet;
import java.util.Set;

public record JoinInnerNode2 (IJoinNode2<? extends IJoinNode3> leftNode,
                              IJoinNode2<? extends IJoinNode3> rightNode,
                              JoinType joinType,
                              Conditions2 joinConditions)
        implements IJoinNode2<JoinInnerNode3>, ToString.IFormattable
{


    @Override
    public TransformationResultFromStage2To3<JoinInnerNode3> transform(TransformationContextFromStage2To3 context) {
        final TransformationResultFromStage2To3<? extends IJoinNode3> lsTransformed = leftNode.transform(context);
        final TransformationResultFromStage2To3<? extends IJoinNode3> rsTransformed = rightNode.transform(lsTransformed.updatedContext);
        final TransformationResultFromStage2To3<Conditions3> jcTransformed = joinConditions.transform(rsTransformed.updatedContext);
        return new TransformationResultFromStage2To3<>(new JoinInnerNode3(lsTransformed.item, rsTransformed.item, joinType, jcTransformed.item), jcTransformed.updatedContext);
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
    public Set<Class<? extends AbstractEntity<?>>> collectEntityTypes() {
        final Set<Class<? extends AbstractEntity<?>>> result = new HashSet<>(); 
        result.addAll(leftNode.collectEntityTypes());
        result.addAll(rightNode.collectEntityTypes());
        result.addAll(joinConditions.collectEntityTypes());
        return result;
    }

    @Override
    public ISource2<? extends ISource3> mainSource() {
        return leftNode.mainSource();
    }

    @Override
    public String toString() {
        return toString(ToString.separateLines);
    }

    @Override
    public String toString(final ToString.IFormat format) {
        return format.toString(this)
                .add("left", leftNode)
                .add("right", rightNode)
                .add("type", joinType)
                .addIfNot("conditions", joinConditions, Conditions2::isEmpty)
                .$();
    }

}
