package ua.com.fielden.platform.eql.retrieval;


import static org.junit.Assert.fail;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import org.junit.Test;

import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.eql.exceptions.EqlStage1ProcessingException;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.sample.domain.TgVehicleFuelUsage;

public class QuerySourceEntityPropsTypeTest extends AbstractEqlShortcutTest {
    
    @Test
    public void yielding_prop_of_one_entity_type_into_declared_property_of_another_entity_type_throws_exception() {
        try {
            transformToModelResult(select(select(TgVehicle.class).yield().prop("station").as("model").modelAsEntity(TgVehicle.class)).model());
            fail("Actual yield type should be in conflict with the declared one.");
        } catch (final EqlStage1ProcessingException e) {
        }
    }
    
    @Test
    public void yielding_prop_of_non_entity_type_into_declared_property_of_entity_type_throws_exception() {
        try {
            transformToModelResult(select(select(TgVehicle.class).yield().val("someModel").as("model").modelAsEntity(TgVehicle.class)).model());
            fail("Actual yield type should be in conflict with the declared one.");
        } catch (final EqlStage1ProcessingException e) {
        }
    }

    @Test
    public void yielding_null_value_under_declared_property_of_entity_type_works() {
        transformToModelResult(select(select().yield().val(null).as("vehicle").modelAsEntity(TgVehicleFuelUsage.class)).yield().countAll().as("result").modelAsAggregate());
    }
    
    @Test
    public void yielding_long_value_under_declared_property_of_entity_type_works() {
        transformToModelResult(select(select().yield().val(1l).as("vehicle").modelAsEntity(TgVehicleFuelUsage.class)).yield().countAll().as("result").modelAsAggregate());
    }

    @Test
    public void yielding_id_prop_under_declared_property_of_entity_type_works() {
        transformToModelResult(select(select(TgVehicle.class).yield().prop("id").as("vehicle").modelAsEntity(TgVehicleFuelUsage.class)).yield().countAll().as("result").modelAsAggregate());
    }
}