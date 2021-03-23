package ua.com.fielden.platform.eql.stage1.operands;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static ua.com.fielden.platform.eql.stage1.operands.EntProp1.enhancePath;
import static ua.com.fielden.platform.eql.stage2.KeyPropertyExtractor.extract;
import static ua.com.fielden.platform.eql.stage2.KeyPropertyExtractor.needsExtraction;
import static ua.com.fielden.platform.utils.EntityUtils.isEntityType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.exceptions.EqlStage1ProcessingException;
import ua.com.fielden.platform.eql.meta.AbstractPropInfo;
import ua.com.fielden.platform.eql.meta.ComponentTypePropInfo;
import ua.com.fielden.platform.eql.meta.UnionTypePropInfo;
import ua.com.fielden.platform.eql.stage1.EntQueryBlocks1;
import ua.com.fielden.platform.eql.stage1.PropResolution;
import ua.com.fielden.platform.eql.stage1.conditions.Conditions1;
import ua.com.fielden.platform.eql.stage1.core.GroupBys1;
import ua.com.fielden.platform.eql.stage1.core.OrderBys1;
import ua.com.fielden.platform.eql.stage1.core.Yields1;
import ua.com.fielden.platform.eql.stage1.sources.QrySources1;
import ua.com.fielden.platform.eql.stage2.core.GroupBy2;
import ua.com.fielden.platform.eql.stage2.core.GroupBys2;
import ua.com.fielden.platform.eql.stage2.core.OrderBy2;
import ua.com.fielden.platform.eql.stage2.core.OrderBys2;
import ua.com.fielden.platform.eql.stage2.core.Yield2;
import ua.com.fielden.platform.eql.stage2.core.Yields2;
import ua.com.fielden.platform.eql.stage2.operands.EntProp2;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage2.sources.IQrySource2;
import ua.com.fielden.platform.eql.stage3.sources.IQrySource3;

public abstract class AbstractQuery1 {

    public final QrySources1 sources;
    public final Conditions1 conditions;
    public final Yields1 yields;
    public final GroupBys1 groups;
    public final OrderBys1 orderings;
    public final Class<? extends AbstractEntity<?>> resultType;
    public final boolean yieldAll;

    public AbstractQuery1(final EntQueryBlocks1 queryBlocks, final Class<? extends AbstractEntity<?>> resultType) {
        this.sources = queryBlocks.sources;
        this.conditions = queryBlocks.conditions;
        this.yields = queryBlocks.yields;
        this.groups = queryBlocks.groups;
        this.orderings = queryBlocks.orderings;
        this.resultType = resultType;
        this.yieldAll = queryBlocks.yieldAll;
    }

    protected static GroupBys2 enhance(final GroupBys2 groupBys) {
        final List<GroupBy2> enhanced = new ArrayList<>();
        
        for (final GroupBy2 original : groupBys.getGroups()) {
            enhanced.addAll(enhance(original));
        }

        return new GroupBys2(enhanced);
    }
    
    protected static OrderBys2 enhance(final OrderBys2 orderBys, final Yields2 yields, final IQrySource2<? extends IQrySource3> mainSource) {
        final List<OrderBy2> enhanced = new ArrayList<>();
        
        for (final OrderBy2 original : orderBys.getModels()) {
            enhanced.addAll(original.operand != null ? transformForOperand(original.operand, original.isDesc) :
                transformForYield(original, yields, mainSource));
        }
        
        return new OrderBys2(enhanced);
    }
    
    protected static Yields2 expand(final Yields2 original) {
        return new Yields2(expand(original.getYields()), original.allGenerated);
    }

    private static List<Yield2> expand(final Collection<Yield2> original) {
        final List<Yield2> expanded = new ArrayList<>();
        
        for (final Yield2 originalYield : original) {
            expanded.addAll(expand(originalYield));
        }

        return expanded;
    }

    private static List<Yield2> expand(final Yield2 original) {
        final List<Yield2> expanded = new ArrayList<>();
        expanded.add(original);
        
        if (original.operand.isHeader() && original.operand instanceof EntProp2){
            final EntProp2 originalYieldProp = (EntProp2) original.operand;

            if (originalYieldProp.lastPart() instanceof UnionTypePropInfo) {
                for (final Entry<String, AbstractPropInfo<?>> sub : ((UnionTypePropInfo<?>) originalYieldProp.lastPart()).propEntityInfo.getProps().entrySet()) {
                    if (isEntityType(sub.getValue().javaType()) && !sub.getValue().hasExpression()) {
                        expanded.addAll(expand(originalYieldProp, sub.getValue(), original.alias));             
                    }
                }
            } else if (originalYieldProp.lastPart() instanceof ComponentTypePropInfo) {
                for (final Entry<String, AbstractPropInfo<?>> sub : ((ComponentTypePropInfo<?>) originalYieldProp.lastPart()).getProps().entrySet()) {
                    expanded.addAll(expand(originalYieldProp, sub.getValue(), original.alias));             
                }
            }
        }

        return expanded;
    }
    
    private static List<Yield2> expand(final EntProp2 originalYieldProp, final AbstractPropInfo<?> subProp, final String yieldAlias) {
        final List<AbstractPropInfo<?>> expandedPath = new ArrayList<>(originalYieldProp.getPath());
        expandedPath.add(subProp);
        return expand(new Yield2(new EntProp2(originalYieldProp.source, expandedPath), yieldAlias + "." + subProp.name, false));             
    }
    
    
    
    private static List<OrderBy2> transformForYield(final OrderBy2 original, final Yields2 yields, final IQrySource2<? extends IQrySource3> mainSource) {
        if (yields.getYieldsMap().containsKey(original.yieldName)) {
            final Yield2 yield = yields.getYieldsMap().get(original.yieldName);
            if (yield.operand instanceof EntProp2 && needsExtraction(((EntProp2) yield.operand).lastPart())) {
                return transformForOperand(yield.operand, original.isDesc);
            } else {
                return asList(original);
            }
        } 
        
        if (yields.getYieldsMap().isEmpty()) {
            final PropResolution propResolution = EntProp1.resolvePropAgainstSource(mainSource, new EntProp1(original.yieldName, false));
            if (propResolution != null) {
                final List<AbstractPropInfo<?>> path = enhancePath(propResolution.getPath());
                return transformForOperand(new EntProp2(mainSource, path), original.isDesc);
            }
        }
        
        throw new EqlStage1ProcessingException("Can't find yield [" + original.yieldName + "]!");
    }

    private static List<OrderBy2> transformForOperand(final ISingleOperand2<?> operand, final boolean isDesc) {
        return operand instanceof EntProp2 ?
                extract((EntProp2) operand).stream().map(keySubprop -> new OrderBy2(keySubprop, isDesc)).collect(toList()) :
                asList(new OrderBy2(operand, isDesc));
    }
    
    private static List<GroupBy2> enhance(final GroupBy2 original) {
        return original.operand instanceof EntProp2 ?
                extract((EntProp2) original.operand).stream().map(keySubprop -> new GroupBy2(keySubprop)).collect(toList()) :
                asList(original);
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + conditions.hashCode();
        result = prime * result + groups.hashCode();
        result = prime * result + orderings.hashCode();
        result = prime * result + ((resultType == null) ? 0 : resultType.hashCode());
        result = prime * result + sources.hashCode();
        result = prime * result + yields.hashCode();
        result = prime * result + (yieldAll ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof AbstractQuery1)) {
            return false;
        }

        final AbstractQuery1 other = (AbstractQuery1) obj;

        return Objects.equals(resultType, other.resultType) &&
                Objects.equals(sources, other.sources) &&
                Objects.equals(yields, other.yields) &&
                Objects.equals(conditions, other.conditions) &&
                Objects.equals(groups, other.groups) &&
                Objects.equals(orderings, other.orderings) &&
                Objects.equals(yieldAll, other.yieldAll);
    }
}