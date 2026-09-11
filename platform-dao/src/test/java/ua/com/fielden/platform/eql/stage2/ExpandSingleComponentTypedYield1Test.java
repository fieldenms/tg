package ua.com.fielden.platform.eql.stage2;

import org.junit.Test;
import ua.com.fielden.platform.eql.meta.EqlStage2TestCase;
import ua.com.fielden.platform.sample.domain.TgWorkOrder;

import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

public class ExpandSingleComponentTypedYield1Test extends EqlStage2TestCase {

    @Test
    public void yield_into_Money_typed_property_with_amount_only_is_transformed_into_a_yield_into_amount__top_level_query() {
        final var query1 = select(TgWorkOrder.class)
                .yield().prop("yearlyCost").as("yearlyCost")
                .modelAsEntity(TgWorkOrder.class);

        final var query2 = select(TgWorkOrder.class)
                .yield().prop("yearlyCost.amount").as("yearlyCost.amount")
                .modelAsEntity(TgWorkOrder.class);

        assertEquals(qry(query2), qry(query1));
    }

    @Test
    public void yield_into_Money_typed_property_with_amount_only_is_transformed_into_a_yield_into_amount__source_query() {
        final var query1 = select(select(TgWorkOrder.class)
                                          .yield().prop("yearlyCost").as("yearlyCost")
                                          .modelAsEntity(TgWorkOrder.class))
                .model();

        final var query2 = select(select(TgWorkOrder.class)
                                          .yield().prop("yearlyCost.amount").as("yearlyCost.amount")
                                          .modelAsEntity(TgWorkOrder.class))
                .model();

        assertEquals(qry(query2), qry(query1));
    }

}
