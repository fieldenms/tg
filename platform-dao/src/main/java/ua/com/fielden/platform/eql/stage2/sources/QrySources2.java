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
import ua.com.fielden.platform.eql.stage2.operands.EntProp2;
import ua.com.fielden.platform.eql.stage3.conditions.ComparisonTest3;
import ua.com.fielden.platform.eql.stage3.conditions.Conditions3;
import ua.com.fielden.platform.eql.stage3.operands.EntProp3;
import ua.com.fielden.platform.eql.stage3.operands.Expression3;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.eql.stage3.sources.IQrySource3;
import ua.com.fielden.platform.eql.stage3.sources.IQrySources3;
import ua.com.fielden.platform.eql.stage3.sources.MultipleNodesQrySources3;
import ua.com.fielden.platform.eql.stage3.sources.QrySource3BasedOnTable;
import ua.com.fielden.platform.eql.stage3.sources.SingleNodeQrySources3;
import ua.com.fielden.platform.types.tuples.T2;

public class QrySources2  {
    public final IQrySource2<? extends IQrySource3> main;
    private final List<CompoundSource2> compounds;

    public QrySources2(final IQrySource2<? extends IQrySource3> main, final List<CompoundSource2> compounds) {
        this.main = main;
        this.compounds = compounds;
    }

    public TransformationResult<IQrySources3> transform(final TransformationContext context) {
        final TransformationResult<? extends IQrySource3> mainTr = main.transform(context);    
        TransformationContext currentContext = mainTr.updatedContext;
        final T2<IQrySources3, TransformationContext> mainEnhancement = enhance(main, mainTr.item, currentContext);
        IQrySources3 currentSourceTree = mainEnhancement._1;
        currentContext = mainEnhancement._2;
        
        for (final CompoundSource2 compoundSource : compounds) {
            final TransformationResult<? extends IQrySource3> compoundSourceTr = compoundSource.source.transform(currentContext);
            currentContext = compoundSourceTr.updatedContext;
            final T2<IQrySources3, TransformationContext> compoundEnhancement = enhance(compoundSource.source, compoundSourceTr.item, currentContext);
            final IQrySources3 coumpoundSourceTree = compoundEnhancement._1;
            currentContext = compoundEnhancement._2;
            final TransformationResult<Conditions3> compConditionsTr = compoundSource.joinConditions.transform(currentContext);
            currentSourceTree = new MultipleNodesQrySources3(currentSourceTree, coumpoundSourceTree, compoundSource.joinType, compConditionsTr.item);
            currentContext = compConditionsTr.updatedContext;
        }
        
        return new TransformationResult<>(currentSourceTree, currentContext);
    }
    
    public Set<EntProp2> collectProps() {
        final Set<EntProp2> result = new HashSet<>(); 
        result.addAll(main.collectProps());
        for (final CompoundSource2 item : compounds) {
            result.addAll(item.source.collectProps());
            result.addAll(item.joinConditions.collectProps());
        }
        return result;
    }
    
    private static T2<IQrySources3, TransformationContext> enhance(final IQrySource2<?> source2, final IQrySource3 source, final TransformationContext context) {
        return attachChildren(source, context.getSourceChildren(source2), context);
    }
    
    private static T2<IQrySources3, TransformationContext> attachChildren(final IQrySource3 source, final List<ChildGroup> children, final TransformationContext context) {
        IQrySources3 currMainSources = new SingleNodeQrySources3(source);
        TransformationContext currentContext = context;
        
        for (final ChildGroup fc : children) {
            for (final Entry<String, String> el : fc.paths.entrySet()) {
                currentContext = currentContext.cloneWithResolutions(t2(el.getKey(), el.getValue()), t2(source, fc.expr == null ? fc.name : fc.expr));
            }

            if (!fc.items.isEmpty()) {
                final T2<IQrySources3, TransformationContext> res = attachChild(currMainSources, source, fc, currentContext);
                currMainSources = res._1;
                currentContext = res._2;
            }
        }

        return t2(currMainSources, currentContext);
    }
    
    private static T2<IQrySources3, TransformationContext> attachChild(final IQrySources3 mainSources, final IQrySource3 rootSource, final ChildGroup child, final TransformationContext context) {
        TransformationContext currentContext = context;
        final TransformationResult<QrySource3BasedOnTable> tr = child.source.transform(currentContext);
        final QrySource3BasedOnTable addedSource = tr.item;
        currentContext = tr.updatedContext; 

        final ISingleOperand3 lo;
        
        if (child.expr == null) {
            lo = new EntProp3(child.name, rootSource, child.source.sourceType(), LongType.INSTANCE);
        } else {
            final TransformationResult<Expression3> expTr = child.expr.transform(currentContext);
            lo = expTr.item;
            currentContext = expTr.updatedContext;
        }
        
        final EntProp3 ro = new EntProp3(ID, addedSource, /*child.source.sourceType()*/Long.class, LongType.INSTANCE);
        final ComparisonTest3 ct = new ComparisonTest3(lo, EQ, ro);
        final Conditions3 jc = new Conditions3(false, asList(asList(ct)));
        final T2<IQrySources3, TransformationContext> res = attachChildren(addedSource, child.items, currentContext);
        return t2(new MultipleNodesQrySources3(mainSources, res._1, (child.required ? IJ : LJ), jc), res._2);
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

        if (!(obj instanceof QrySources2)) {
            return false;
        }

        final QrySources2 other = (QrySources2) obj;

        return Objects.equals(main, other.main) && Objects.equals(compounds, other.compounds);
    }
}