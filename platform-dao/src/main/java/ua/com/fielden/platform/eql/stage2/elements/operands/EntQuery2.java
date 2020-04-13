package ua.com.fielden.platform.eql.stage2.elements.operands;

import static java.util.Collections.emptySet;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.eql.meta.QueryCategory.RESULT_QUERY;
import static ua.com.fielden.platform.eql.meta.QueryCategory.SOURCE_QUERY;
import static ua.com.fielden.platform.eql.meta.QueryCategory.SUB_QUERY;
import static ua.com.fielden.platform.utils.CollectionUtil.listOf;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.hibernate.type.LongType;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.meta.AbstractPropInfo;
import ua.com.fielden.platform.eql.meta.QueryCategory;
import ua.com.fielden.platform.eql.stage2.elements.EntQueryBlocks2;
import ua.com.fielden.platform.eql.stage2.elements.GroupBys2;
import ua.com.fielden.platform.eql.stage2.elements.OrderBys2;
import ua.com.fielden.platform.eql.stage2.elements.TransformationContext;
import ua.com.fielden.platform.eql.stage2.elements.TransformationResult;
import ua.com.fielden.platform.eql.stage2.elements.Yield2;
import ua.com.fielden.platform.eql.stage2.elements.Yields2;
import ua.com.fielden.platform.eql.stage2.elements.conditions.Conditions2;
import ua.com.fielden.platform.eql.stage2.elements.sources.Sources2;
import ua.com.fielden.platform.eql.stage3.elements.EntQueryBlocks3;
import ua.com.fielden.platform.eql.stage3.elements.GroupBys3;
import ua.com.fielden.platform.eql.stage3.elements.OrderBys3;
import ua.com.fielden.platform.eql.stage3.elements.Yields3;
import ua.com.fielden.platform.eql.stage3.elements.conditions.Conditions3;
import ua.com.fielden.platform.eql.stage3.elements.operands.EntQuery3;
import ua.com.fielden.platform.eql.stage3.elements.sources.IQrySources3;

public class EntQuery2 implements ISingleOperand2<EntQuery3> {

    public final Sources2 sources;
    public final Conditions2 conditions;
    public final Yields2 yields;
    public final GroupBys2 groups;
    public final OrderBys2 orderings;
    public final Class<?> resultType;
    public final QueryCategory category;
    public final Object hibType;

    public EntQuery2(final EntQueryBlocks2 queryBlocks, final Class<? extends AbstractEntity<?>> resultType, final QueryCategory category) {
        this.category = category;
        this.sources = queryBlocks.sources;
        this.conditions = queryBlocks.conditions;
        this.yields = enhanceYields(queryBlocks.yields, queryBlocks.sources, category);
        this.groups = queryBlocks.groups;
        this.orderings = queryBlocks.orderings;
        this.resultType = enhance(resultType);
        this.hibType = resultType == null ? yields.getYields().iterator().next().operand.hibType() : LongType.INSTANCE;
    }

    private Class<?> enhance(final Class<? extends AbstractEntity<?>> resultType) {
        // TODO EQL (if resultType == null, then take it should be PrimitiveResultQuery -- just take resultType of its single yield
        return resultType == null ? yields.getYields().iterator().next().javaType() : resultType;
    }

    private Yields2 enhanceYields(final Yields2 yields, final Sources2 sources2, final QueryCategory category) {
        if (yields.getYields().isEmpty()) {
            if (category == SUB_QUERY) {
                if (sources2.main.entityInfo().getProps().containsKey(ID)) {
                    return new Yields2(listOf(new Yield2(new EntProp2(sources2.main, listOf(sources2.main.entityInfo().getProps().get(ID))), "", false)));
                } else {
                    return new Yields2(listOf(new Yield2(new EntValue2(0), "", false)));
                }
            } else {
                final List<Yield2> enhancedYields = new ArrayList<>();
                for (final Entry<String, AbstractPropInfo<?>> el : sources2.main.entityInfo().getProps().entrySet()) {
                    if (!el.getValue().hasExpression() && category == SOURCE_QUERY || category == RESULT_QUERY) {
                        enhancedYields.add(new Yield2(new EntProp2(sources2.main, listOf(el.getValue())), el.getKey(), false));
                    }
                }
                return new Yields2(enhancedYields);
            }
        }
        return yields;
    }
    
    @Override
    public TransformationResult<EntQuery3> transform(final TransformationContext context) {
        final TransformationResult<IQrySources3> sourcesTr = sources != null ? sources.transform(context) : null;
        final TransformationResult<Conditions3> conditionsTr = conditions.transform(sourcesTr != null ? sourcesTr.updatedContext : context);
        final TransformationResult<Yields3> yieldsTr = yields.transform(conditionsTr.updatedContext);
        final TransformationResult<GroupBys3> groupsTr = groups.transform(yieldsTr.updatedContext);
        final TransformationResult<OrderBys3> orderingsTr = orderings.transform(groupsTr.updatedContext);

        final EntQueryBlocks3 entQueryBlocks = new EntQueryBlocks3(sourcesTr != null ? sourcesTr.item : null, conditionsTr.item, yieldsTr.item, groupsTr.item, orderingsTr.item);

        return new TransformationResult<EntQuery3>(new EntQuery3(entQueryBlocks, category, resultType), orderingsTr.updatedContext);
    }

    @Override
    public Set<EntProp2> collectProps() {
        final Set<EntProp2> result = new HashSet<>();
        result.addAll(sources != null ? sources.collectProps() : emptySet());
        result.addAll(conditions.collectProps());
        result.addAll(yields.collectProps());
        result.addAll(groups.collectProps());
        result.addAll(orderings.collectProps());
        
        return result;
    }

    @Override
    public Class<?> type() {
        return resultType;
    }
    
    @Override
    public Object hibType() {
        return hibType;
    }

    @Override
    public boolean ignore() {
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + conditions.hashCode();
        result = prime * result + groups.hashCode();
        result = prime * result + category.hashCode();
        result = prime * result + ((resultType == null) ? 0 : resultType.hashCode());
        result = prime * result + ((sources == null) ? 0 : sources.hashCode());
        result = prime * result + yields.hashCode();
        result = prime * result + orderings.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof EntQuery2)) {
            return false;
        }

        final EntQuery2 other = (EntQuery2) obj;

        return Objects.equals(category, other.category) &&
                Objects.equals(resultType, other.resultType) &&
                Objects.equals(sources, other.sources) &&
                Objects.equals(yields, other.yields) &&
                Objects.equals(conditions, other.conditions) &&
                Objects.equals(groups, other.groups) &&
                Objects.equals(orderings, other.orderings);
    }
}