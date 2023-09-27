package ua.com.fielden.platform.eql.stage1.operands.queries;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toList;
import static ua.com.fielden.platform.eql.stage1.operands.Prop1.enhancePath;
import static ua.com.fielden.platform.eql.stage2.KeyPropertyExtractor.extract;
import static ua.com.fielden.platform.eql.stage2.KeyPropertyExtractor.needsExtraction;
import static ua.com.fielden.platform.eql.stage2.conditions.Conditions2.emptyConditions;
import static ua.com.fielden.platform.eql.stage2.etc.GroupBys2.emptyGroupBys;
import static ua.com.fielden.platform.eql.stage2.etc.OrderBys2.emptyOrderBys;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.exceptions.EqlStage1ProcessingException;
import ua.com.fielden.platform.eql.meta.QuerySourceInfoProvider;
import ua.com.fielden.platform.eql.meta.query.AbstractPropInfo;
import ua.com.fielden.platform.eql.stage1.PropResolution;
import ua.com.fielden.platform.eql.stage1.QueryComponents1;
import ua.com.fielden.platform.eql.stage1.TransformationContext1;
import ua.com.fielden.platform.eql.stage1.conditions.Conditions1;
import ua.com.fielden.platform.eql.stage1.etc.GroupBys1;
import ua.com.fielden.platform.eql.stage1.etc.OrderBys1;
import ua.com.fielden.platform.eql.stage1.etc.Yields1;
import ua.com.fielden.platform.eql.stage1.operands.Prop1;
import ua.com.fielden.platform.eql.stage1.sources.IJoinNode1;
import ua.com.fielden.platform.eql.stage2.QueryComponents2;
import ua.com.fielden.platform.eql.stage2.conditions.Conditions2;
import ua.com.fielden.platform.eql.stage2.etc.GroupBy2;
import ua.com.fielden.platform.eql.stage2.etc.GroupBys2;
import ua.com.fielden.platform.eql.stage2.etc.OrderBy2;
import ua.com.fielden.platform.eql.stage2.etc.OrderBys2;
import ua.com.fielden.platform.eql.stage2.etc.Yield2;
import ua.com.fielden.platform.eql.stage2.etc.Yields2;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage2.sources.IJoinNode2;
import ua.com.fielden.platform.eql.stage2.sources.ISource2;
import ua.com.fielden.platform.eql.stage3.sources.ISource3;

public abstract class AbstractQuery1 {

    public final IJoinNode1<? extends IJoinNode2<?>> joinRoot;
    public final Conditions1 conditions;
    public final Conditions1 udfConditions;
    public final Yields1 yields;
    public final GroupBys1 groups;
    public final OrderBys1 orderings;
    public final Class<? extends AbstractEntity<?>> resultType;
    public final boolean yieldAll;

    public AbstractQuery1(final QueryComponents1 queryComponents, final Class<? extends AbstractEntity<?>> resultType) {
        this.joinRoot = queryComponents.joinRoot;
        this.conditions = queryComponents.conditions;
        this.udfConditions = queryComponents.udfConditions;
        this.yields = queryComponents.yields;
        this.groups = queryComponents.groups;
        this.orderings = queryComponents.orderings;
        this.resultType = resultType;
        this.yieldAll = queryComponents.yieldAll;
    }

    public Set<Class<? extends AbstractEntity<?>>> collectEntityTypes() {
        final Set<Class<? extends AbstractEntity<?>>> result = new HashSet<>();
        result.addAll(joinRoot != null ? joinRoot.collectEntityTypes() : emptySet());
        result.addAll(conditions.collectEntityTypes());
        result.addAll(yields.collectSyntheticEntities());
        result.addAll(groups.collectSyntheticEntities());
        result.addAll(orderings.collectSyntheticEntities());
        
        return result;
    }
    
    public QueryComponents2 transformSourceless(final TransformationContext1 context) {
        return new QueryComponents2(null, conditions.transform(context), yields.transform(context), groups.transform(context), orderings.transform(context));
    }
    
    protected Conditions2 enhanceWithUserDataFilterConditions(final ISource2<? extends ISource3> mainSource, final QuerySourceInfoProvider querySourceInfoProvider, final Conditions2 originalConditions) { 
        if (udfConditions.isEmpty()) {
            return originalConditions;
        }
        
        final TransformationContext1 localContext = (new TransformationContext1(querySourceInfoProvider, false)).cloneWithAdded(mainSource); 
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
        result = prime * result + ((joinRoot == null) ? 0 : joinRoot.hashCode());
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
                Objects.equals(joinRoot, other.joinRoot) &&
                Objects.equals(yields, other.yields) &&
                Objects.equals(conditions, other.conditions) &&
                Objects.equals(udfConditions, other.udfConditions) &&
                Objects.equals(groups, other.groups) &&
                Objects.equals(orderings, other.orderings) &&
                Objects.equals(yieldAll, other.yieldAll);
    }
}