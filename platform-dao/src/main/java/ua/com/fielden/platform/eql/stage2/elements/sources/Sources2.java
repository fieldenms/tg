package ua.com.fielden.platform.eql.stage2.elements.sources;

import static java.util.Arrays.asList;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.query.fluent.enums.ComparisonOperator.EQ;
import static ua.com.fielden.platform.entity.query.fluent.enums.JoinType.IJ;
import static ua.com.fielden.platform.entity.query.fluent.enums.JoinType.LJ;
import static ua.com.fielden.platform.types.tuples.T2.t2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import ua.com.fielden.platform.eql.stage2.elements.TransformationContext;
import ua.com.fielden.platform.eql.stage2.elements.TransformationResult;
import ua.com.fielden.platform.eql.stage3.elements.conditions.ComparisonTest3;
import ua.com.fielden.platform.eql.stage3.elements.conditions.Conditions3;
import ua.com.fielden.platform.eql.stage3.elements.operands.EntProp3;
import ua.com.fielden.platform.eql.stage3.elements.sources.IQrySource3;
import ua.com.fielden.platform.eql.stage3.elements.sources.IQrySources3;
import ua.com.fielden.platform.eql.stage3.elements.sources.JoinedQrySource3;
import ua.com.fielden.platform.eql.stage3.elements.sources.QrySource3BasedOnTable;
import ua.com.fielden.platform.eql.stage3.elements.sources.SingleQrySource3;
import ua.com.fielden.platform.types.tuples.T2;


public class Sources2 {
    public final IQrySource2<? extends IQrySource3> main;
    private final List<CompoundSource2> compounds;

    public Sources2(final IQrySource2<? extends IQrySource3> main, final List<CompoundSource2> compounds) {
        this.main = main;
        this.compounds = compounds;
    }

    public TransformationResult<IQrySources3> transform(final TransformationContext context) {
        final TransformationResult<? extends IQrySource3> mainTr = main.transform(context);    
        TransformationContext currentContext = mainTr.updatedContext;
        final T2<IQrySources3, Map<String, T2<IQrySource3, String>>> mainEnhancement = enhance(main, mainTr.item, context);
        IQrySources3 currentSourceTree = mainEnhancement._1;
        currentContext = currentContext.cloneWithResolutions(main, mainEnhancement._2);
        
        for (final CompoundSource2 compoundSource : compounds) {
            final TransformationResult<? extends IQrySource3> compoundSourceTr = compoundSource.source.transform(currentContext);
            final T2<IQrySources3,Map<String, T2<IQrySource3,String>>> compoundEnhancement = enhance(compoundSource.source, compoundSourceTr.item, context);
            final IQrySources3 coumpoundSourceTree = compoundEnhancement._1;
            currentContext = compoundSourceTr.updatedContext.cloneWithResolutions(compoundSource.source, compoundEnhancement._2);
            final TransformationResult<Conditions3> compConditionsTr = compoundSource.joinConditions.transform(currentContext);
            currentSourceTree = new JoinedQrySource3(currentSourceTree, coumpoundSourceTree, compoundSource.joinType, compConditionsTr.item);
            currentContext = compConditionsTr.updatedContext;
        }
        
        return new TransformationResult<>(currentSourceTree, currentContext);
    }
    
    private T2<IQrySources3, Map<String, T2<IQrySource3, String>>> enhance(final IQrySource2<?> source2, final IQrySource3 source, final TransformationContext context) {
        return attachChildren(source, context.getSourceChildren(source2), context, new HashMap<>());
    }
    
    private T2<IQrySources3, Map<String, T2<IQrySource3, String>>> attachChildren(final IQrySource3 source, final Set<Child> children, final TransformationContext context, final Map<String, T2<IQrySource3, String>> cumulativeResolutions) {
        IQrySources3 currMainSources = new SingleQrySource3(source);
        final Map<String, T2<IQrySource3, String>> resolutions = new HashMap<>();
        resolutions.putAll(cumulativeResolutions);

        for (final Child fc : children) {
            if (fc.fullPath != null) {
                resolutions.put(fc.fullPath, t2(source, fc.main.name));
            }

            if (!fc.items.isEmpty()) {
                final T2<IQrySources3, Map<String, T2<IQrySource3, String>>> res = attachChild(currMainSources, source, fc, context, resolutions);
                currMainSources = res._1;
                resolutions.putAll(res._2);
            }
        }

        return t2(currMainSources, resolutions);
    }
    
    private T2<IQrySources3, Map<String, T2<IQrySource3, String>>> attachChild(final IQrySources3 mainSources, final IQrySource3 rootSource, final Child child, final TransformationContext context, final Map<String, T2<IQrySource3, String>> cumulativeResolutions) {
        //final Table tbl = context.getTable(child.main.javaType().getName());
        final QrySource3BasedOnTable addedSource = child.source.transform(context).item;//new QrySource3BasedOnTable(tbl, rootSource.contextId(), child.context);
        final EntProp3 lo = new EntProp3(child.main.name, rootSource);
        final EntProp3 ro = new EntProp3(ID, addedSource);
        final ComparisonTest3 ct = new ComparisonTest3(lo, EQ, ro);
        final Conditions3 jc = new Conditions3(false, asList(asList(ct)));
        final T2<IQrySources3, Map<String, T2<IQrySource3, String>>> res = attachChildren(addedSource, child.items, context, cumulativeResolutions);
        return t2(new JoinedQrySource3(mainSources, res._1, (child.required ? IJ : LJ), jc), res._2);
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