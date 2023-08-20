package ua.com.fielden.platform.eql.retrieval;


import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import org.junit.Test;

import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.sample.domain.TgVehicleMake;

public class YieldAllTest extends AbstractEqlShortcutTest {

    @Test
    public void yield_all_overrides_the_yield_with_the_same_name() {
        final EntityResultQueryModel<TgVehicleMake> act = select(TgVehicleMake.class).
                yieldAll().
                yield().val(100).as("version").modelAsEntity(TgVehicleMake.class);
        
        final EntityResultQueryModel<TgVehicleMake> exp = select(TgVehicleMake.class).
                yield().prop("key").as("key").
                yield().prop("desc").as("desc").
                yield().prop("id").as("id").
                yield().prop("version").as("version").
                modelAsEntity(TgVehicleMake.class);
        
        assertModelResultsEquals(exp, act);
    }
    
    @Test
    public void yield_all_yields_props_of_union_entity_correctly() {
        final var act = select(BOGIE).yieldAll().modelAsEntity(BOGIE);
        
        final var exp = select(BOGIE).
                yield().prop("key").as("key").
                yield().prop("desc").as("desc").
                yield().prop("id").as("id").
                yield().prop("version").as("version").
                yield().prop("location.wagonSlot").as("location.wagonSlot").
                yield().prop("location.workshop").as("location.workshop").
                yield().prop("bogieClass").as("bogieClass").
                yield().prop("active").as("active").
                yield().prop("createdBy").as("createdBy").
                yield().prop("createdDate").as("createdDate").
                yield().prop("createdTransactionGuid").as("createdTransactionGuid").
                yield().prop("lastUpdatedBy").as("lastUpdatedBy").
                yield().prop("lastUpdatedDate").as("lastUpdatedDate").
                yield().prop("lastUpdatedTransactionGuid").as("lastUpdatedTransactionGuid").
                yield().prop("refCount").as("refCount").
                modelAsEntity(BOGIE);
        
        assertModelResultsEquals(exp, act);
    }
}