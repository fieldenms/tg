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
import ua.com.fielden.platform.eql.stage3.sources.ISources3;
import ua.com.fielden.platform.eql.stage3.sources.MultipleNodesSources3;
import ua.com.fielden.platform.eql.stage3.sources.SingleNodeSources3;
import ua.com.fielden.platform.eql.stage3.sources.Source3BasedOnTable;

public interface ISources2<S3 extends ISources3> extends ITransformableToS3<S3> {
    
    /**
     * Gets the leftmost query source. Needed for auto-yielding and UDF (user data filtering).
     * 
     * @return
     */
    ISource2<? extends ISource3> mainSource();
    
    
    static TransformationResult2<ISources3> transformNone(final TransformationContext2 context) {
        return new TransformationResult2<ISources3>(null, context);
    }
    
    static TransformationResult2<ISources3> transform(final ISource2<?> explicitSource, final TransformationContext2 context) {
        final TransformationResult2<? extends ISource3> explicitSourceTr = explicitSource.transform(context);
        return attachChildren(explicitSourceTr.item, context.getSourceLeaves(explicitSource.id()), context.getSourceBranches(explicitSource.id()), explicitSourceTr.updatedContext);
    }
    
    private static TransformationResult2<ISources3> attachChildren(final ISource3 source, final List<LeafNode> leaves, final List<BranchNode> branches, final TransformationContext2 context) {
        ISources3 currMainSources = new SingleNodeSources3(source);
        TransformationContext2 currentContext = leaves.isEmpty() ? context : context.cloneWithResolutions(source, leaves);

        for (final BranchNode fc : branches) {
            final TransformationResult2<ISources3> res = attachChild(currMainSources, source, fc, currentContext);
            currMainSources = res.item;
            currentContext = res.updatedContext;
        }

        return new TransformationResult2<>(currMainSources, currentContext);
    }
    
    private static TransformationResult2<ISources3> attachChild(final ISources3 mainSources, final ISource3 rootSource, final BranchNode child, final TransformationContext2 context) {
        final TransformationResult2<Source3BasedOnTable> tr = child.source.transform(context);
        final Source3BasedOnTable addedSource = tr.item;
        TransformationContext2 currentContext = tr.updatedContext; 

        final ISingleOperand3 lo;
        
        if (child.expr == null) {
            lo = new Prop3(child.name, rootSource, child.source.sourceType(), LongType.INSTANCE);
        } else {
            final TransformationResult2<Expression3> expTr = child.expr.transform(currentContext);
            lo = expTr.item.isSingle() ? expTr.item.first : expTr.item;
            currentContext = expTr.updatedContext;
        }
        
        final Prop3 ro = new Prop3(ID, addedSource, /*child.source.sourceType()*/Long.class, LongType.INSTANCE);
        final ComparisonTest3 ct = new ComparisonTest3(lo, EQ, ro);
        final Conditions3 jc = new Conditions3(false, asList(asList(ct)));
        final TransformationResult2<ISources3> res = attachChildren(addedSource, child.leaves(), child.branches(), currentContext);
        return new TransformationResult2<>(new MultipleNodesSources3(mainSources, res.item, (child.required ? IJ : LJ), jc), res.updatedContext);
    }
}