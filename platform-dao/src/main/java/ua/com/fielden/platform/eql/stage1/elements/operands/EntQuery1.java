package ua.com.fielden.platform.eql.stage1.elements.operands;

import static ua.com.fielden.platform.eql.meta.QueryCategory.SOURCE_QUERY;
import static ua.com.fielden.platform.eql.meta.QueryCategory.SUB_QUERY;

import java.util.Objects;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.meta.PropsResolutionContext;
import ua.com.fielden.platform.eql.meta.QueryCategory;
import ua.com.fielden.platform.eql.meta.TransformationResult;
import ua.com.fielden.platform.eql.stage1.elements.AbstractElement1;
import ua.com.fielden.platform.eql.stage1.elements.EntQueryBlocks1;
import ua.com.fielden.platform.eql.stage1.elements.GroupBys1;
import ua.com.fielden.platform.eql.stage1.elements.OrderBys1;
import ua.com.fielden.platform.eql.stage1.elements.Yields1;
import ua.com.fielden.platform.eql.stage1.elements.conditions.Conditions1;
import ua.com.fielden.platform.eql.stage1.elements.sources.Sources1;
import ua.com.fielden.platform.eql.stage2.elements.EntQueryBlocks2;
import ua.com.fielden.platform.eql.stage2.elements.GroupBys2;
import ua.com.fielden.platform.eql.stage2.elements.OrderBys2;
import ua.com.fielden.platform.eql.stage2.elements.Yields2;
import ua.com.fielden.platform.eql.stage2.elements.conditions.Conditions2;
import ua.com.fielden.platform.eql.stage2.elements.operands.EntQuery2;
import ua.com.fielden.platform.eql.stage2.elements.sources.Sources2;

public class EntQuery1 extends AbstractElement1 implements ISingleOperand1<EntQuery2> {

    public final Sources1 sources;
    public final Conditions1 conditions;
    public final Yields1 yields;
    public final GroupBys1 groups;
    public final OrderBys1 orderings;

    public final Class<? extends AbstractEntity<?>> resultType;
    public final QueryCategory category;

    public EntQuery1(final EntQueryBlocks1 queryBlocks, final Class<? extends AbstractEntity<?>> resultType, final QueryCategory category, final int contextId) {
       super(contextId);
       this.category = category;
       this.sources = queryBlocks.sources;
       this.conditions = queryBlocks.conditions;
       this.yields = queryBlocks.yields;
       this.groups = queryBlocks.groups;
       this.orderings = queryBlocks.orderings;
       this.resultType = resultType;
       
       if (this.resultType == null && category != SUB_QUERY) { // only primitive result queries have result type not assigned
           throw new IllegalStateException("This query is not subquery, thus its result type shouldn't be null!");
       }
   }
    
    public boolean isSubQuery() {
        return category == SUB_QUERY;
    }
    
    public boolean isSourceQuery() {
        return category == SOURCE_QUERY;
    }

    public Class<? extends AbstractEntity<?>> type() {
        return resultType;
    }

    @Override
    public TransformationResult<EntQuery2> transform(final PropsResolutionContext resolutionContext) {
        final PropsResolutionContext localResolutionContext = isSubQuery() ? resolutionContext.produceForCorrelatedSubquery() : resolutionContext.produceForUncorrelatedSubquery();
        // .produceForUncorrelatedSubquery() should be used only for cases of synthetic entities (where source query can only be uncorrelated) -- simple queries as source queries are accessible for correlation
        final TransformationResult<Sources2> sourcesTransformationResult =  sources.transform(localResolutionContext);
        final TransformationResult<Conditions2> conditionsTransformationResult =  conditions.transform(sourcesTransformationResult.getUpdatedContext());
        final TransformationResult<Yields2> yieldsTransformationResult =  yields.transform(conditionsTransformationResult.getUpdatedContext());
        final TransformationResult<GroupBys2> groupsTransformationResult =  groups.transform(yieldsTransformationResult.getUpdatedContext());
        final TransformationResult<OrderBys2> orderingsTransformationResult =  orderings.transform(groupsTransformationResult.getUpdatedContext());

        final EntQueryBlocks2 entQueryBlocks = new EntQueryBlocks2(
                sourcesTransformationResult.getItem(), 
                conditionsTransformationResult.getItem(), 
                yieldsTransformationResult.getItem(), 
                groupsTransformationResult.getItem(), 
                orderingsTransformationResult.getItem());

        final PropsResolutionContext resultResolutionContext = (isSubQuery() || isSourceQuery()) ? 
                new PropsResolutionContext(resolutionContext.getDomainInfo(), resolutionContext.getSources(), orderingsTransformationResult.getUpdatedContext().getResolvedProps()) :
                    orderingsTransformationResult.getUpdatedContext();
               
        return new TransformationResult<EntQuery2>(new EntQuery2(entQueryBlocks, type(), category), resultResolutionContext);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((category == null) ? 0 : category.hashCode());
        result = prime * result + ((conditions == null) ? 0 : conditions.hashCode());
        result = prime * result + ((groups == null) ? 0 : groups.hashCode());
        result = prime * result + ((orderings == null) ? 0 : orderings.hashCode());
        result = prime * result + ((resultType == null) ? 0 : resultType.hashCode());
        result = prime * result + ((sources == null) ? 0 : sources.hashCode());
        result = prime * result + ((yields == null) ? 0 : yields.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof EntQuery1)) {
            return false;
        }
        
        final EntQuery1 other = (EntQuery1) obj;
        
        return Objects.equals(category, other.category) &&
                Objects.equals(resultType, other.resultType) &&
                Objects.equals(sources, other.sources) &&
                Objects.equals(yields, other.yields) &&
                Objects.equals(conditions, other.conditions) &&
                Objects.equals(groups, other.groups) &&
                Objects.equals(orderings, other.orderings);
   }
}