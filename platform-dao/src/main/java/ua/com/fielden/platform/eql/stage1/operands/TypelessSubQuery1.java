package ua.com.fielden.platform.eql.stage1.operands;

import static ua.com.fielden.platform.eql.stage2.etc.Yields2.nullYields;

import ua.com.fielden.platform.eql.stage1.ITransformableToS2;
import ua.com.fielden.platform.eql.stage1.QueryBlocks1;
import ua.com.fielden.platform.eql.stage1.TransformationContext;
import ua.com.fielden.platform.eql.stage1.TransformationResult;
import ua.com.fielden.platform.eql.stage2.QueryBlocks2;
import ua.com.fielden.platform.eql.stage2.conditions.Conditions2;
import ua.com.fielden.platform.eql.stage2.etc.GroupBys2;
import ua.com.fielden.platform.eql.stage2.etc.OrderBys2;
import ua.com.fielden.platform.eql.stage2.etc.Yields2;
import ua.com.fielden.platform.eql.stage2.operands.TypelessSubQuery2;
import ua.com.fielden.platform.eql.stage2.sources.Sources2;

public class TypelessSubQuery1 extends AbstractQuery1 implements ITransformableToS2<TypelessSubQuery2> {

    public TypelessSubQuery1(final QueryBlocks1 queryBlocks) {
        super(queryBlocks, null);
    }

    @Override
    public TypelessSubQuery2 transform(final TransformationContext context) {
        final TransformationContext localContext = context.produceForCorrelatedSubquery();
        final TransformationResult<Sources2> sourcesTr = sources.transform(localContext);
        final TransformationContext enhancedContext = sourcesTr.updatedContext;
        final Sources2 sources2 = sourcesTr.item;
        final Conditions2 conditions2 = conditions.transform(enhancedContext);
        final Yields2 yields2 = yields.getYields().isEmpty() ? nullYields : yields.transform(enhancedContext);
        final GroupBys2 groups2 = enhance(groups.transform(enhancedContext));
        final OrderBys2 orderings2 = enhance(orderings.transform(enhancedContext), yields2, sources2.main);
        final QueryBlocks2 entQueryBlocks = new QueryBlocks2(sources2, conditions2, yields2, groups2, orderings2);
        return new TypelessSubQuery2(entQueryBlocks);
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        final int result = super.hashCode();
        return prime * result + TypelessSubQuery1.class.getName().hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj || super.equals(obj) && obj instanceof TypelessSubQuery1;
    }
}