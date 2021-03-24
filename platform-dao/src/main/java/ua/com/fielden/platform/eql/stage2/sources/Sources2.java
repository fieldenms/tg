package ua.com.fielden.platform.eql.stage2.sources;

import static java.util.Arrays.asList;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.query.fluent.enums.ComparisonOperator.EQ;
import static ua.com.fielden.platform.entity.query.fluent.enums.JoinType.IJ;
import static ua.com.fielden.platform.entity.query.fluent.enums.JoinType.LJ;
import static ua.com.fielden.platform.types.tuples.T2.t2;

import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.hibernate.type.LongType;

import ua.com.fielden.platform.eql.stage2.TransformationContext;
import ua.com.fielden.platform.eql.stage2.TransformationResult;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage3.conditions.ComparisonTest3;
import ua.com.fielden.platform.eql.stage3.conditions.Conditions3;
import ua.com.fielden.platform.eql.stage3.operands.Prop3;
import ua.com.fielden.platform.eql.stage3.operands.Expression3;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.eql.stage3.sources.ISource3;
import ua.com.fielden.platform.eql.stage3.sources.ISources3;
import ua.com.fielden.platform.eql.stage3.sources.MultipleNodesSources3;
import ua.com.fielden.platform.eql.stage3.sources.Source3BasedOnTable;
import ua.com.fielden.platform.eql.stage3.sources.SingleNodeSources3;
import ua.com.fielden.platform.types.tuples.T2;

public class Sources2  {
    public final ISource2<? extends ISource3> main;
    private final List<CompoundSource2> compounds;

    public Sources2(final ISource2<? extends ISource3> main, final List<CompoundSource2> compounds) {
        this.main = main;
        this.compounds = compounds;
    }

    public TransformationResult<ISources3> transform(final TransformationContext context) {
        final TransformationResult<? extends ISource3> mainTr = main.transform(context);    
        TransformationContext currentContext = mainTr.updatedContext;
        final T2<ISources3, TransformationContext> mainEnhancement = enhance(main, mainTr.item, currentContext);
        ISources3 currentSourceTree = mainEnhancement._1;
        currentContext = mainEnhancement._2;
        
        for (final CompoundSource2 compoundSource : compounds) {
            final TransformationResult<? extends ISource3> compoundSourceTr = compoundSource.source.transform(currentContext);
            currentContext = compoundSourceTr.updatedContext;
            final T2<ISources3, TransformationContext> compoundEnhancement = enhance(compoundSource.source, compoundSourceTr.item, currentContext);
            final ISources3 coumpoundSourceTree = compoundEnhancement._1;
            currentContext = compoundEnhancement._2;
            final TransformationResult<Conditions3> compConditionsTr = compoundSource.joinConditions.transform(currentContext);
            currentSourceTree = new MultipleNodesSources3(currentSourceTree, coumpoundSourceTree, compoundSource.joinType, compConditionsTr.item);
            currentContext = compConditionsTr.updatedContext;
        }
        
        return new TransformationResult<>(currentSourceTree, currentContext);
    }
    
    public Set<Prop2> collectProps() {
        final Set<Prop2> result = new HashSet<>(); 
        result.addAll(main.collectProps());
        for (final CompoundSource2 item : compounds) {
            result.addAll(item.source.collectProps());
            result.addAll(item.joinConditions.collectProps());
        }
        return result;
    }
    
    private static T2<ISources3, TransformationContext> enhance(final ISource2<?> source2, final ISource3 source, final TransformationContext context) {
        return attachChildren(source, context.getSourceChildren(source2), context);
    }
    
    private static T2<ISources3, TransformationContext> attachChildren(final ISource3 source, final List<ChildGroup> children, final TransformationContext context) {
        ISources3 currMainSources = new SingleNodeSources3(source);
        TransformationContext currentContext = context;
        
        for (final ChildGroup fc : children) {
            for (final Entry<String, String> el : fc.paths.entrySet()) {
                currentContext = currentContext.cloneWithResolutions(t2(el.getKey(), el.getValue()), t2(source, fc.expr == null ? fc.name : fc.expr));
            }

            if (!fc.items.isEmpty()) {
                final T2<ISources3, TransformationContext> res = attachChild(currMainSources, source, fc, currentContext);
                currMainSources = res._1;
                currentContext = res._2;
            }
        }

        return t2(currMainSources, currentContext);
    }
    
    private static T2<ISources3, TransformationContext> attachChild(final ISources3 mainSources, final ISource3 rootSource, final ChildGroup child, final TransformationContext context) {
        TransformationContext currentContext = context;
        final TransformationResult<Source3BasedOnTable> tr = child.source.transform(currentContext);
        final Source3BasedOnTable addedSource = tr.item;
        currentContext = tr.updatedContext; 

        final ISingleOperand3 lo;
        
        if (child.expr == null) {
            lo = new Prop3(child.name, rootSource, child.source.sourceType(), LongType.INSTANCE);
        } else {
            final TransformationResult<Expression3> expTr = child.expr.transform(currentContext);
            lo = expTr.item;
            currentContext = expTr.updatedContext;
        }
        
        final Prop3 ro = new Prop3(ID, addedSource, /*child.source.sourceType()*/Long.class, LongType.INSTANCE);
        final ComparisonTest3 ct = new ComparisonTest3(lo, EQ, ro);
        final Conditions3 jc = new Conditions3(false, asList(asList(ct)));
        final T2<ISources3, TransformationContext> res = attachChildren(addedSource, child.items, currentContext);
        return t2(new MultipleNodesSources3(mainSources, res._1, (child.required ? IJ : LJ), jc), res._2);
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + compounds.hashCode();
        result = prime * result + main.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Sources2)) {
            return false;
        }

        final Sources2 other = (Sources2) obj;

        return Objects.equals(main, other.main) && Objects.equals(compounds, other.compounds);
    }
}