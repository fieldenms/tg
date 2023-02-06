package ua.com.fielden.platform.eql.stage2.sources;

import static java.util.Arrays.asList;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.query.fluent.enums.ComparisonOperator.EQ;
import static ua.com.fielden.platform.entity.query.fluent.enums.JoinType.IJ;
import static ua.com.fielden.platform.entity.query.fluent.enums.JoinType.LJ;

import java.util.List;

import org.hibernate.type.LongType;

import ua.com.fielden.platform.eql.stage2.ITransformableToS3;
import ua.com.fielden.platform.eql.stage2.TransformationContext2;
import ua.com.fielden.platform.eql.stage2.TransformationResult2;
import ua.com.fielden.platform.eql.stage3.conditions.ComparisonTest3;
import ua.com.fielden.platform.eql.stage3.conditions.Conditions3;
import ua.com.fielden.platform.eql.stage3.operands.Expression3;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.eql.stage3.operands.Prop3;
import ua.com.fielden.platform.eql.stage3.sources.ISource3;
import ua.com.fielden.platform.eql.stage3.sources.IJoinNode3;
import ua.com.fielden.platform.eql.stage3.sources.JoinBranch3;
import ua.com.fielden.platform.eql.stage3.sources.JoinLeaf3;
import ua.com.fielden.platform.eql.stage3.sources.Source3BasedOnTable;

public interface IJoinNode2<S3 extends IJoinNode3> extends ITransformableToS3<S3> {
    
    /**
     * Gets the leftmost query source. Needed for auto-yielding and UDF (user data filtering).
     * 
     * @return
     */
    ISource2<? extends ISource3> mainSource();
    
    
    static TransformationResult2<IJoinNode3> transformNone(final TransformationContext2 context) {
        return new TransformationResult2<IJoinNode3>(null, context);
    }
    
    static TransformationResult2<IJoinNode3> transform(final ISource2<?> explicitSource, final TransformationContext2 context) {
        final TransformationResult2<? extends ISource3> explicitSourceTr = explicitSource.transform(context);
        return attachChildren(explicitSourceTr.item, context.getSourceImplicitNodes(explicitSource.id()), explicitSourceTr.updatedContext);
    }
    
    private static TransformationResult2<IJoinNode3> attachChildren(final ISource3 source, final List<ImplicitNode> implicitNodes, final TransformationContext2 context) {
        TransformationContext2 currentContext = context.cloneWithSource(source);
        IJoinNode3 currentJoinNode = new JoinLeaf3(source);

        for (final ImplicitNode implicitNode : implicitNodes) {
            final TransformationResult2<IJoinNode3> res = attachChild(currentJoinNode, source, implicitNode, currentContext);
            currentJoinNode = res.item;
            currentContext = res.updatedContext;
        }

        return new TransformationResult2<>(currentJoinNode, currentContext);
    }
    
    private static TransformationResult2<IJoinNode3> attachChild(final IJoinNode3 currentJoinNode, final ISource3 rootSource, final ImplicitNode implicitNode, final TransformationContext2 context) {
        final TransformationResult2<Source3BasedOnTable> tr = implicitNode.source.transform(context);
        final Source3BasedOnTable addedSource = tr.item;
        TransformationContext2 currentContext = tr.updatedContext; 

        final ISingleOperand3 lo;
        
        if (implicitNode.expr == null) {
            lo = new Prop3(implicitNode.name, rootSource, implicitNode.source.sourceType(), LongType.INSTANCE);
        } else {
            final TransformationResult2<Expression3> expTr = implicitNode.expr.transform(currentContext);
            lo = expTr.item.isSingle() ? expTr.item.first : expTr.item;
            currentContext = expTr.updatedContext;
        }
        
        final Prop3 ro = new Prop3(ID, addedSource, Long.class, LongType.INSTANCE);
        final ComparisonTest3 ct = new ComparisonTest3(lo, EQ, ro);
        final Conditions3 jc = new Conditions3(false, asList(asList(ct)));
        final TransformationResult2<IJoinNode3> res = attachChildren(addedSource, implicitNode.subnodes(), currentContext);
        return new TransformationResult2<>(new JoinBranch3(currentJoinNode, res.item, (implicitNode.required ? IJ : LJ), jc), res.updatedContext);
    }
}