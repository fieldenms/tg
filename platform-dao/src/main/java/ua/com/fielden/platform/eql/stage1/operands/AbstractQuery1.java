package ua.com.fielden.platform.eql.stage1.operands;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static ua.com.fielden.platform.eql.stage1.operands.Prop1.enhancePath;
import static ua.com.fielden.platform.eql.stage2.KeyPropertyExtractor.extract;
import static ua.com.fielden.platform.eql.stage2.KeyPropertyExtractor.needsExtraction;
import static ua.com.fielden.platform.eql.stage2.conditions.Conditions2.emptyConditions;
import static ua.com.fielden.platform.eql.stage2.etc.GroupBys2.emptyGroupBys;
import static ua.com.fielden.platform.eql.stage2.etc.OrderBys2.emptyOrderBys;
import static ua.com.fielden.platform.utils.EntityUtils.isEntityType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.exceptions.EqlStage1ProcessingException;
import ua.com.fielden.platform.eql.meta.AbstractPropInfo;
import ua.com.fielden.platform.eql.meta.ComponentTypePropInfo;
import ua.com.fielden.platform.eql.meta.UnionTypePropInfo;
import ua.com.fielden.platform.eql.stage1.PropResolution;
import ua.com.fielden.platform.eql.stage1.QueryBlocks1;
import ua.com.fielden.platform.eql.stage1.TransformationContext;
import ua.com.fielden.platform.eql.stage1.conditions.Conditions1;
import ua.com.fielden.platform.eql.stage1.etc.GroupBys1;
import ua.com.fielden.platform.eql.stage1.etc.OrderBys1;
import ua.com.fielden.platform.eql.stage1.etc.Yields1;
import ua.com.fielden.platform.eql.stage1.sources.ISources1;
import ua.com.fielden.platform.eql.stage2.QueryBlocks2;
import ua.com.fielden.platform.eql.stage2.conditions.Conditions2;
import ua.com.fielden.platform.eql.stage2.etc.GroupBy2;
import ua.com.fielden.platform.eql.stage2.etc.GroupBys2;
import ua.com.fielden.platform.eql.stage2.etc.OrderBy2;
import ua.com.fielden.platform.eql.stage2.etc.OrderBys2;
import ua.com.fielden.platform.eql.stage2.etc.Yield2;
import ua.com.fielden.platform.eql.stage2.etc.Yields2;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage2.sources.ISource2;
import ua.com.fielden.platform.eql.stage2.sources.ISources2;
import ua.com.fielden.platform.eql.stage3.sources.ISource3;

public abstract class AbstractQuery1 {

    public final ISources1<? extends ISources2<?>> sources;
    public final Conditions1 conditions;
    public final Conditions1 udfConditions;
    public final Yields1 yields;
    public final GroupBys1 groups;
    public final OrderBys1 orderings;
    public final Class<? extends AbstractEntity<?>> resultType;
    public final boolean yieldAll;

    public AbstractQuery1(final QueryBlocks1 queryBlocks, final Class<? extends AbstractEntity<?>> resultType) {
        this.sources = queryBlocks.sources;
        this.conditions = queryBlocks.conditions;
        this.udfConditions = queryBlocks.udfConditions;
        this.yields = queryBlocks.yields;
        this.groups = queryBlocks.groups;
        this.orderings = queryBlocks.orderings;
        this.resultType = resultType;
        this.yieldAll = queryBlocks.yieldAll;
    }

    public QueryBlocks2 transformSourceless(final TransformationContext context) {
        return new QueryBlocks2(null, conditions.transform(context), yields.transform(context), groups.transform(context), orderings.transform(context));
    }
    
    protected Conditions2 enhanceWithUserDataFilterConditions(final ISource2<? extends ISource3> mainSource, final TransformationContext context, final Conditions2 originalConditions) { 
        if (udfConditions.isEmpty()) {
            return originalConditions;
        }
        
        final TransformationContext localContext = new TransformationContext(context.domainInfo, asList(asList(mainSource)), context.sourceIdPrefix); 
        final Conditions2 udfConditions2 = udfConditions.transform(localContext);
        
        if (originalConditions.ignore()) {
            return udfConditions2.ignore() ? emptyConditions : udfConditions2;  
        } else {
            return udfConditions2.ignore() ? originalConditions : new Conditions2(false, asList(asList(udfConditions2, originalConditions)));
        }
    }
    
    protected static GroupBys2 enhance(final GroupBys2 groupBys) {
        if (groupBys.equals(emptyGroupBys)) {
            return emptyGroupBys;
        }
        
        final List<GroupBy2> enhanced = groupBys.getGroups().stream().map(group -> enhance(group)).flatMap(List::stream).collect(Collectors.toList());
        return new GroupBys2(enhanced);
    }
    
    protected static OrderBys2 enhance(final OrderBys2 orderBys, final Yields2 yields, final ISource2<? extends ISource3> mainSource) {
        if (orderBys.equals(emptyOrderBys)) {
            return emptyOrderBys;
        }
        
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
        
        if (original.operand.isHeader() && original.operand instanceof Prop2){
            final Prop2 originalYieldProp = (Prop2) original.operand;

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
    
    private static List<Yield2> expand(final Prop2 originalYieldProp, final AbstractPropInfo<?> subProp, final String yieldAlias) {
        final List<AbstractPropInfo<?>> expandedPath = new ArrayList<>(originalYieldProp.getPath());
        expandedPath.add(subProp);
        return expand(new Yield2(new Prop2(originalYieldProp.source, expandedPath), yieldAlias + "." + subProp.name, false));             
    }
    
    private static List<OrderBy2> transformForYield(final OrderBy2 original, final Yields2 yields, final ISource2<? extends ISource3> mainSource) {
        if (yields.getYieldsMap().containsKey(original.yieldName)) {
            final Yield2 yield = yields.getYieldsMap().get(original.yieldName);
            if (yield.operand instanceof Prop2 && needsExtraction(((Prop2) yield.operand).lastPart())) {
                return transformForOperand(yield.operand, original.isDesc);
            } else {
                return asList(original);
            }
        } 
        
        if (yields.getYieldsMap().isEmpty()) {
            final PropResolution propResolution = Prop1.resolvePropAgainstSource(mainSource, new Prop1(original.yieldName, false));
            if (propResolution != null) {
                final List<AbstractPropInfo<?>> path = enhancePath(propResolution.getPath());
                return transformForOperand(new Prop2(mainSource, path), original.isDesc);
            }
        }
        
        throw new EqlStage1ProcessingException("Can't find yield [" + original.yieldName + "]!");
    }

    private static List<OrderBy2> transformForOperand(final ISingleOperand2<?> operand, final boolean isDesc) {
        return operand instanceof Prop2 ? //
                extract((Prop2) operand).stream().map(keySubprop -> new OrderBy2(keySubprop, isDesc)).collect(toList()) : //
                asList(new OrderBy2(operand, isDesc));
    }
    
    private static List<GroupBy2> enhance(final GroupBy2 original) {
        return original.operand instanceof Prop2 ? //
                extract((Prop2) original.operand).stream().map(keySubprop -> new GroupBy2(keySubprop)).collect(toList()) : //
                asList(original);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + conditions.hashCode();
        result = prime * result + udfConditions.hashCode();
        result = prime * result + groups.hashCode();
        result = prime * result + orderings.hashCode();
        result = prime * result + ((resultType == null) ? 0 : resultType.hashCode());
        result = prime * result + ((sources == null) ? 0 : sources.hashCode());
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
                Objects.equals(udfConditions, other.udfConditions) &&
                Objects.equals(groups, other.groups) &&
                Objects.equals(orderings, other.orderings) &&
                Objects.equals(yieldAll, other.yieldAll);
    }
}