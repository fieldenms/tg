package ua.com.fielden.platform.eql.stage2.elements.operands;

import java.util.Objects;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.meta.QueryCategory;
import ua.com.fielden.platform.eql.stage2.elements.EntQueryBlocks2;
import ua.com.fielden.platform.eql.stage2.elements.GroupBys2;
import ua.com.fielden.platform.eql.stage2.elements.OrderBys2;
import ua.com.fielden.platform.eql.stage2.elements.TransformationContext;
import ua.com.fielden.platform.eql.stage2.elements.TransformationResult;
import ua.com.fielden.platform.eql.stage2.elements.Yields2;
import ua.com.fielden.platform.eql.stage2.elements.conditions.Conditions2;
import ua.com.fielden.platform.eql.stage2.elements.sources.Sources2;
import ua.com.fielden.platform.eql.stage3.elements.EntQueryBlocks3;
import ua.com.fielden.platform.eql.stage3.elements.GroupBys3;
import ua.com.fielden.platform.eql.stage3.elements.OrderBys3;
import ua.com.fielden.platform.eql.stage3.elements.Yields3;
import ua.com.fielden.platform.eql.stage3.elements.conditions.Conditions3;
import ua.com.fielden.platform.eql.stage3.elements.operands.EntQuery3;
import ua.com.fielden.platform.eql.stage3.elements.sources.Sources3;

public class EntQuery2 implements ISingleOperand2<EntQuery3> {

    public final Sources2 sources;
    public final Conditions2 conditions;
    public final Yields2 yields;
    public final GroupBys2 groups;
    public final OrderBys2 orderings;
    public final Class<? extends AbstractEntity<?>> resultType;
    public final QueryCategory category;

    public EntQuery2(final EntQueryBlocks2 queryBlocks, final Class<? extends AbstractEntity<?>> resultType, final QueryCategory category) {
        this.category = category;
        this.sources = queryBlocks.sources;
        this.conditions = queryBlocks.conditions;
        this.yields = queryBlocks.yields;
        this.groups = queryBlocks.groups;
        this.orderings = queryBlocks.orderings;
        this.resultType = resultType;
    }

    @Override
    public TransformationResult<EntQuery3> transform(final TransformationContext transformationContext) {
        final TransformationResult<Sources3> sourcesTransformationResult =  sources.transform(transformationContext);
        final TransformationResult<Conditions3> conditionsTransformationResult =  conditions.transform(sourcesTransformationResult.updatedContext);
        final TransformationResult<Yields3> yieldsTransformationResult =  yields.transform(conditionsTransformationResult.updatedContext);
        final TransformationResult<GroupBys3> groupsTransformationResult =  groups.transform(yieldsTransformationResult.updatedContext);
        final TransformationResult<OrderBys3> orderingsTransformationResult =  orderings.transform(groupsTransformationResult.updatedContext);

        final EntQueryBlocks3 entQueryBlocks = new EntQueryBlocks3(
                sourcesTransformationResult.item, 
                conditionsTransformationResult.item, 
                yieldsTransformationResult.item, 
                groupsTransformationResult.item, 
                orderingsTransformationResult.item);

        return new TransformationResult<EntQuery3>(new EntQuery3(entQueryBlocks), orderingsTransformationResult.updatedContext);
    }    

    @Override
    public Class<? extends AbstractEntity<?>> type() {
        return resultType;
    }

    @Override
    public boolean ignore() {
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((conditions == null) ? 0 : conditions.hashCode());
        result = prime * result + ((groups == null) ? 0 : groups.hashCode());
        result = prime * result + ((category == null) ? 0 : category.hashCode());
        result = prime * result + ((resultType == null) ? 0 : resultType.hashCode());
        result = prime * result + ((sources == null) ? 0 : sources.hashCode());
        result = prime * result + ((yields == null) ? 0 : yields.hashCode());
        result = prime * result + ((orderings == null) ? 0 : orderings.hashCode());
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