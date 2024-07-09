package ua.com.fielden.platform.entity.query.model;

import ua.com.fielden.platform.eql.antlr.tokens.util.ListTokenSource;

/**
 * Models a query that returns a primitive result (as opposed to an entity result).
 * <p>
 * The result of such a query cannot be obtained directly due to the nature of the relational model.
 * Instead it has to be embedded, as a subquery, into a query the results of which can be returned.
 * <p>
 * Usage example with embedding the result into an {@linkplain AggregatedResultQueryModel aggregated result}:
 * {@snippet lang="java" :
 * PrimitiveResultQueryModel primitiveModel = select(Vehicle.class).yield()
 *    .beginExpr().maxOf().prop("price").div().maxOf().prop("purchasePrice").endExpr()
 *    .modelAsPrimitive();
 * AggregatedResultQueryModel aggModel = select().yield().model(primitiveModel).as("result").modelAsAggregate();
 * BigDecimal result = aggregateDao.getEntity(from(aggregate).model()).get("result");
 * }
 */
public class PrimitiveResultQueryModel extends SingleResultQueryModel {

    public PrimitiveResultQueryModel(final ListTokenSource tokens) {
        super(tokens, null, false);
    }

    @Override
    public PrimitiveResultQueryModel setFilterable(boolean filterable) {
        super.setFilterable(filterable);
        return this;
    }

}
