package ua.com.fielden.platform.eql.retrieval;


import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import org.junit.Ignore;
import org.junit.Test;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFromAlias;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.sample.domain.TgFuelUsage;
import ua.com.fielden.platform.sample.domain.TgOrgUnit4;
import ua.com.fielden.platform.sample.domain.TgOrgUnit5;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.sample.domain.TgVehicleMake;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;

public class QuerySourceEntityPropsNullabilityTest extends AbstractEqlShortcutTest {
    private static String NULLABLE_YIELD = "nullable_prop";
    private static String NONNULLABLE_YIELD = "nonnullable_prop";

    /* Test of proper yield nullability is performed indirectly -- by ensuring that proper kind of join is made while accessing subprops of the yield under test:
     * In case that yield is nullable -- LEFT JOIN should be used in order to access its subprops.
     * In case that yield is nonnullable -- INNER JOIN should be used in order to access its subprops. 
     */ 

    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // :::::::::::::::::::::::::::::::  1.  TESTING (NON)NULLABILITY OF YIELD OPERANDS :::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // :::::::::::::::::::::::::::::::  1.1 TESTING (NON)NULLABILITY OF YIELDED PROP OPERAND :::::::::::::::::::::::::::::::::::::::::::::::::::
    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    @Test
    public void yield_of_nullable_prop_is_nullable() {
        // property "station" within entity TgVehicle is nullable 
        final var sourceQry = select(TgVehicle.class).yield().prop("station").as(NULLABLE_YIELD).modelAsAggregate();
        assertYieldIsNullable(NULLABLE_YIELD, TgOrgUnit5.class, sourceQry);
    }

    @Test
    public void yield_of_nonnullable_prop_is_nonnullable() {
        // property "model" within entity TgVehicle is nonnullable
        final var sourceQry = select(TgVehicle.class).yield().prop("model").as(NONNULLABLE_YIELD).modelAsAggregate();
        assertYieldIsNonnullable(NONNULLABLE_YIELD, TgVehicleModel.class, sourceQry);
    }
    
    @Test
    public void yield_of_nonnullable_subprop_of_nullable_prop_is_nullable() {
        // property "station" of TgOrgunit5  within entity TgVehicle is nullable, and property "parent" within TgOrgunit5 is nonnullable
        final var sourceQry = select(TgVehicle.class).yield().prop("station.parent").as(NULLABLE_YIELD).modelAsAggregate();
        assertYieldIsNullable(NULLABLE_YIELD, TgOrgUnit4.class, sourceQry);
    }

    @Test
    public void yield_of_nonnullable_subprop_of_nonnullable_prop_is_nonnullable() {
        // property "model" of TgVehicleModel  within entity TgVehicle is nonnullable, and property "make" within TgVehicleModel is nonnullable
        final var sourceQry = select(TgVehicle.class).yield().prop("model.make").as(NONNULLABLE_YIELD).modelAsAggregate();
        assertYieldIsNonnullable(NONNULLABLE_YIELD, TgVehicleMake.class, sourceQry);
    }

    @Test
    public void yield_of_nullable_subprop_of_nullable_prop_is_nullable() {
        // property "replacedBy" of TgVehicle within entity TgVehicle is nullable, and property "station" within TgVehicle is nullable
        final var sourceQry = select(TgVehicle.class).yield().prop("replacedBy.station").as(NULLABLE_YIELD).modelAsAggregate();
        assertYieldIsNullable(NULLABLE_YIELD, TgOrgUnit5.class, sourceQry);
    }

    @Test
    public void yield_of_nullable_subprop_of_nonnullable_prop_is_nullable() {
        // property "vehicle" of TgVehicle  within entity TgFuelUsage is nonnullable, and property "station" within TgVehicle is nullable
        final var sourceQry = select(TgFuelUsage.class).yield().prop("vehicle.station").as(NULLABLE_YIELD).modelAsAggregate();
        assertYieldIsNullable(NULLABLE_YIELD, TgOrgUnit5.class, sourceQry);
    }

    @Test
    @Ignore //TODO implement support for better inference of calc props nonnullability
    public void yield_of_nonnullable_calc_prop_is_nonnullable() {
        // property "calcModel" within entity TgVehicle is nonnullable (nullable according to declarations, but nonnullable according to its actual expression) 
        final var sourceQry = select(TgVehicle.class).yield().prop("calcModel").as(NONNULLABLE_YIELD).modelAsAggregate();
        assertYieldIsNonnullable(NONNULLABLE_YIELD, TgVehicleModel.class, sourceQry);
    }
    
    @Test
    @Ignore //TODO implement support for better inference of calc props nonnullability
    public void yield_of_nonnullable_calc_subprop_of_nonnullable_prop_is_nonnullable() {
        // property "vehicle" of TgVehicle  within entity TgFuelUsage is nonnullable, and property "calcModel" within TgVehicle is nonnullable (nullable according to declarations, but nonnullable according to its actual expression) 
        final var sourceQry = select(TgFuelUsage.class).yield().prop("vehicle.calcModel").as(NONNULLABLE_YIELD).modelAsAggregate();
        assertYieldIsNonnullable(NONNULLABLE_YIELD, TgVehicleModel.class, sourceQry);
    }

    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // :::::::::::::::::::::::::::::::  1.2 TESTING (NON)NULLABILITY OF YIELDED EXPRESSION OPERAND :::::::::::::::::::::::::::::::::::::::::::::
    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    
    @Test
    public void yield_of_nullable_prop_wrapped_into_expression_is_nullable() {
        // property "station" within entity TgVehicle is nullable 
        final var sourceQry = select(TgVehicle.class).yield().beginExpr().prop("station").endExpr().as(NULLABLE_YIELD).modelAsAggregate();
        assertYieldIsNullable(NULLABLE_YIELD, TgOrgUnit5.class, sourceQry);
    }
    
    @Test
    public void yield_of_nonnullable_prop_wrapped_into_expression_is_nonnullable() {
        // property "model" within entity TgVehicle is nonnullable
        final var sourceQry = select(TgVehicle.class).yield().beginExpr().prop("model").endExpr().as(NONNULLABLE_YIELD).modelAsAggregate();
        assertYieldIsNonnullable(NONNULLABLE_YIELD, TgVehicleModel.class, sourceQry);
    }

    @Test
    public void yield_of_non_nullable_subprop_of_nullable_prop_wrapped_into_expression_is_nullable() {
        // property "station" of TgOrgunit5  within entity TgVehicle is nullable, and property "parent" within TgOrgunit5 is nonnullable
        final var sourceQry = select(TgVehicle.class).yield().beginExpr().prop("station.parent").endExpr().as(NULLABLE_YIELD).modelAsAggregate();
        assertYieldIsNullable(NULLABLE_YIELD, TgOrgUnit4.class, sourceQry);
    }
    
    @Test
    public void yield_of_nonnullable_subprop_of_nonnullable_prop_wrapped_into_expression_is_nonnullable() {
        // property "model" of TgVehicleModel  within entity TgVehicle is nonnullable, and property "make" within TgVehicleModel is nonnullable
        final var sourceQry = select(TgVehicle.class).yield().beginExpr().prop("model.make").endExpr().as(NONNULLABLE_YIELD).modelAsAggregate();
        assertYieldIsNonnullable(NONNULLABLE_YIELD, TgVehicleMake.class, sourceQry);
    }

    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // :::::::::::::::::::::::::::::::  1.3 TESTING (NON)NULLABILITY OF YIELDED SUBQUERY OPERAND :::::::::::::::::::::::::::::::::::::::::::::::
    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    
    @Test
    public void yield_of_some_arbitrary_subquery_is_nullable() {
        final var sourceQry = select(TgVehicle.class).yield().model(select(TgVehicle.class).where().prop(ID).eq().val(1l).model()).as(NULLABLE_YIELD).modelAsAggregate();
        assertYieldIsNullable(NULLABLE_YIELD, TgVehicle.class, sourceQry);
    }
    
    @Test
    public void yield_of_subquery_that_refetches_id_is_nonnullable() {
        final var sourceQry = select(TgVehicle.class).yield().model(select(TgVehicle.class).where().prop(ID).eq().extProp(ID).model()).as(NONNULLABLE_YIELD).modelAsAggregate();
        assertYieldIsNonnullable(NONNULLABLE_YIELD, TgVehicle.class, sourceQry);
    }

    @Test
    public void yield_of_subquery_that_refetches_nullable_prop_is_nullable() {
        final var sourceQry = select(TgVehicle.class).yield().model(select(TgOrgUnit5.class).where().prop(ID).eq().extProp("station").model()).as(NULLABLE_YIELD).modelAsAggregate();
        assertYieldIsNullable(NULLABLE_YIELD, TgOrgUnit5.class, sourceQry);
    }

    @Test
    @Ignore //TODO implement better nullability inference for sourceQryueries refetching some nonnullable property
    public void yield_of_subquery_that_refetches_nonnullable_prop_is_nonnullable() {
        final var sourceQry = select(TgVehicle.class).yield().model(select(TgVehicleModel.class).where().prop(ID).eq().extProp("model").model()).as(NONNULLABLE_YIELD).modelAsAggregate();
        assertYieldIsNonnullable(NONNULLABLE_YIELD, TgVehicleModel.class, sourceQry);
    }
    
    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // :::::::::::::::::::::::::::::::  1.4 TESTING (NON)NULLABILITY OF YIELDED VALUE OPERAND ::::::::::::::::::::::::::::::::::::::::::::::::::
    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    @Test
    @Ignore 
    /* TODO currently val(..) operand is "forgetting" entity type when entity instance is passed into it (only entity ID prop is preserved). 
     * Need to remember original type in order to treat it as Entity in untyped (EntityAggregates) query source result. 
     */
    public void yield_of_nonnull_entity_value_is_nullable() {
        final var sourceQry = select(TgVehicle.class).yield().val(new TgVehicle()).as(NULLABLE_YIELD).modelAsAggregate();
        assertYieldIsNullable(NULLABLE_YIELD, TgVehicle.class, sourceQry);
    }    

    @Test
    public void yield_of_nonnull_entity_value_into_defined_entity_property_is_nullable() {
        final EntityResultQueryModel<TgVehicleModel> sourceQry = select().yield().val(1L).as("make").modelAsEntity(TgVehicleModel.class);
        assertYieldIsNullable("make", TgVehicleMake.class, sourceQry);
    }    
    
    @Test
    public void yield_of_null_entity_value_into_defined_entity_property_is_nullable() {
        final EntityResultQueryModel<TgVehicleModel> sourceQry = select().yield().val(null).as("make").modelAsEntity(TgVehicleModel.class);
        assertYieldIsNullable("make", TgVehicleMake.class, sourceQry);
    }
    
    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // :::::::::::::::::::::::::::::::  1.5 TESTING (NON)NULLABILITY OF YIELDED FUNCTION OPERAND :::::::::::::::::::::::::::::::::::::::::::::::
    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    @Test
    public void yield_of_nullable_function_is_nullable() {
        // property "station" within entity TgVehicle is nullable, val(..) operand is also nullable 
        final var sourceQry = select(TgVehicle.class).yield().ifNull().prop("station").then().val(1l).as(NULLABLE_YIELD).modelAsAggregate();
        assertYieldIsNullable(NULLABLE_YIELD, TgOrgUnit5.class, sourceQry);
    }

    @Test
    public void yield_of_nonnullable_function_is_nonnullable() {
        // property "replacedBy" within entity TgVehicle is nullable, query refetching vehicle by its "id" is nonnullable 
        final var sourceQry = select(TgVehicle.class).yield().ifNull().prop("replacedBy").then().model(select(TgVehicle.class).where().prop(ID).eq().extProp(ID).model()).as(NONNULLABLE_YIELD).modelAsAggregate();
        assertYieldIsNonnullable(NONNULLABLE_YIELD, TgVehicle.class, sourceQry);
    }
        
    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // :::::::::::::::::::::::::::::::  2.  TESTING NONNULLABILITY OF NULLABLE YIELD OPERANDS WHICH HAVE NOT NULL CHECK IN WHERE STMT  ::::::::: 
    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    @Test
    public void yield_of_nullable_prop_with_nulls_filtered_out_is_nonnullable() {
        // property "station" within entity TgVehicle is nullable 
        final var sourceQry = select(TgVehicle.class).where().prop("station").isNotNull().yield().prop("station").as(NONNULLABLE_YIELD).modelAsAggregate();
        assertYieldIsNonnullable(NONNULLABLE_YIELD, TgOrgUnit5.class, sourceQry);
    }

    @Test
    public void yield_of_nonnulable_subprop_of_nullable_prop_with_nulls_filtered_out_is_nonnullable() {
        // property "station" of TgOrgunit5  within entity TgVehicle is nullable, and property "parent" within TgOrgunit5 is nonnullable
        final var sourceQry = select(TgVehicle.class).where().prop("station.parent").isNotNull().yield().prop("station.parent").as(NONNULLABLE_YIELD).modelAsAggregate();
        assertYieldIsNonnullable(NONNULLABLE_YIELD, TgOrgUnit4.class, sourceQry);
    }
   
    @Test
    public void yield_of_nullable_prop_wrapped_into_expression_with_nulls_filtered_out_is_nonnullable() {
        // property "station" within entity TgVehicle is nullable 
        final var sourceQry = select(TgVehicle.class).where().beginExpr().prop("station").endExpr().isNotNull().yield().beginExpr().prop("station").endExpr().as(NONNULLABLE_YIELD).modelAsAggregate();
        assertYieldIsNonnullable(NONNULLABLE_YIELD, TgOrgUnit5.class, sourceQry);
    }
    
    @Test
    @Ignore // TODO need to be able to determine sourceQryueries equality as if at stage1 (at stage2 every sourceQryuery gets unique query sources and this doesn't allow to determine the required semantic equality).
    public void yield_of_subquery_that_refetches_nullable_prop_with_nulls_filtered_out_is_nonnullable() {
        // property "station" within entity TgVehicle is nullable 
        final var sourceQry = select(TgVehicle.class).where().model(select(TgOrgUnit5.class).where().prop(ID).eq().extProp("station").model()).isNotNull().
                yield().model(select(TgOrgUnit5.class).where().prop("id").eq().extProp("station").model()).as(NONNULLABLE_YIELD).modelAsAggregate();
        assertYieldIsNonnullable(NONNULLABLE_YIELD, TgOrgUnit5.class, sourceQry);
    }
    
    @Test
    public void yield_of_nullable_function_with_nulls_filtered_out_is_nonnullable() {
        // property "station" within entity TgVehicle is nullable, val(..) operand is also nullable 
        final var sourceQry = select(TgVehicle.class).where().ifNull().prop("station").then().val(1l).isNotNull().
                yield().ifNull().prop("station").then().val(1l).as(NONNULLABLE_YIELD).modelAsAggregate();
        assertYieldIsNonnullable(NONNULLABLE_YIELD, TgOrgUnit5.class, sourceQry);
    }
    
    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // :::::::::::::::::::::::::::::::  3.  TESTING NONNULLABILITY OF NULLABLE YIELD OPERANDS WHICH HAVE BEEN MARKED AS REQUIRED  :::::::::::::: 
    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    @Test
    public void yield_of_nullable_prop_marked_as_required_is_nonnullable() {
        // property "station" within entity TgVehicle is nullable 
        final var sourceQry = select(TgVehicle.class).where().prop("station.key").isNotNull().yield().prop("station").asRequired(NONNULLABLE_YIELD).modelAsAggregate();
        assertYieldIsNonnullable(NONNULLABLE_YIELD, TgOrgUnit5.class, sourceQry);
    }

    @Test
    public void yield_of_nullable_prop_wrapped_into_expression_marked_as_required_is_nonnullable() {
        // property "station" within entity TgVehicle is nullable 
        final var sourceQry = select(TgVehicle.class).where().prop("station.key").isNotNull().yield().beginExpr().prop("station").endExpr().asRequired(NONNULLABLE_YIELD).modelAsAggregate();
        assertYieldIsNonnullable(NONNULLABLE_YIELD, TgOrgUnit5.class, sourceQry);
    }
    
    @Test
    public void yield_of_subquery_that_refetches_nullable_prop_marked_as_required_is_nonnullable() {
        // property "station" within entity TgVehicle is nullable 
        final var sourceQry = select(TgVehicle.class).where().prop("station.key").isNotNull().yield().model(select(TgOrgUnit5.class).where().prop(ID).eq().extProp("station").model()).asRequired(NONNULLABLE_YIELD).modelAsAggregate();
        assertYieldIsNonnullable(NONNULLABLE_YIELD, TgOrgUnit5.class, sourceQry);
    }
    
    @Test
    public void yield_of_nullable_function_marked_as_required_is_nonnullable() {
        // property "station" within entity TgVehicle is nullable, val(..) operand is also nullable 
        final var sourceQry = select(TgVehicle.class).yield().ifNull().prop("station").then().val(1l).asRequired(NONNULLABLE_YIELD).modelAsAggregate();
        assertYieldIsNonnullable(NONNULLABLE_YIELD, TgOrgUnit5.class, sourceQry);
    }

    @Test
    public void yield_of_nonnull_entity_value_into_defined_entity_property_marked_as_required_is_nonullable() {
        // val(1L) is nullable (as there is no way to be sure that entity of TgVehicleMake type with such id really exists).
        final EntityResultQueryModel<TgVehicleModel> sourceQry = select().yield().val(1L).asRequired("make").modelAsEntity(TgVehicleModel.class);
        assertYieldIsNonnullable("make", TgVehicleMake.class, sourceQry);
    }    
    
    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // :::::::::::::::::::::::::::::::  4.  TESTING THAT ACTUAL YIELD (NON)NULLABILITY OVERRIDES THE DECLARED ONE  :::::::::::::::::::::::::::::
    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    @Test
    public void yield_of_nullable_property_into_defined_entity_nonnullable_property_is_nullable() {
        final EntityResultQueryModel<TgVehicle> sourceQry = select(TgVehicle.class).yield().val(null).as("model").modelAsEntity(TgVehicle.class);
        assertYieldIsNullable("model", TgVehicleModel.class, sourceQry);
    }    

    @Test
    public void yield_of_nonnullable_property_into_defined_entity_nullable_property_is_nonnullable() {
        // property "replacedBy" within entity TgVehicle is nullable 
        final EntityResultQueryModel<TgVehicle> sourceQry = select(TgVehicle.class).yield().prop(ID).as("replacedBy").modelAsEntity(TgVehicle.class);
        assertYieldIsNonnullable("replacedBy", TgVehicle.class, sourceQry);
    }    
    
    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // :::::::::::::::::::::::::::::::  5.  TESTING (NON)NULLABILITY OF YIELDS DERIVERD FROM MULTIPLES SOURCE QUERIES  :::::::::::::::::::::::::
    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    @Test
    public void yield_from_source_queries_containing_both_nullable_and_nonnullable_members_is_nullable() {
        final var sourceQryWithNullableYield = select(TgVehicle.class).yield().prop("station.parent").as("orgunit4").modelAsAggregate();
        final var sourceQryWithNonnullableYield = select(TgOrgUnit5.class).yield().prop("parent").as("orgunit4").modelAsAggregate();
        assertYieldIsNullable("orgunit4", TgOrgUnit4.class, sourceQryWithNullableYield, sourceQryWithNonnullableYield);
    }

    @Test
    public void yield_from_source_queries_containing_only_nonnullable_members_is_nonnullable() {
        final var sourceQry1 = select(TgVehicle.class).where().prop("station.parent").isNotNull().yield().prop("station.parent").as("orgunit4").modelAsAggregate();
        final var sourceQry2 = select(TgOrgUnit5.class).yield().prop("parent").as("orgunit4").modelAsAggregate();
        assertYieldIsNonnullable("orgunit4", TgOrgUnit4.class, sourceQry1, sourceQry2);
    }
    
    @Test
    public void yield_from_source_queries_containing_only_nullable_members_is_nullable() {
        final var sourceQry1 = select(TgVehicle.class).yield().prop("station.parent").as("orgunit4").modelAsAggregate();
        final var sourceQry2 = select(TgVehicle.class).yield().prop("replacedBy.station.parent").as("orgunit4").modelAsAggregate();
        assertYieldIsNullable("orgunit4", TgOrgUnit4.class, sourceQry1, sourceQry2);
    }

    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // :::::::::::::::::::::::::::::::  TEST HELPING METHODS  ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    
    private static void assertYieldIsNonnullable(final String yieldName, final Class<? extends AbstractEntity<?>> yieldEntityType, final AggregatedResultQueryModel ... sourceModels) {
        assertYieldNullability(false, yieldName, yieldEntityType, startModel(sourceModels));
    }

    private static void assertYieldIsNullable(final String yieldName, final Class<? extends AbstractEntity<?>> yieldEntityType, final AggregatedResultQueryModel ... sourceModels) {
        assertYieldNullability(true, yieldName, yieldEntityType, startModel(sourceModels));
    }

    @SafeVarargs
    private static <T extends AbstractEntity<?>> void assertYieldIsNonnullable(final String yieldName, final Class<? extends AbstractEntity<?>> yieldEntityType, final EntityResultQueryModel<T> ... sourceModels) {
        assertYieldNullability(false, yieldName, yieldEntityType, startModel(sourceModels));
    }

    @SafeVarargs
    private static <T extends AbstractEntity<?>>  void assertYieldIsNullable(final String yieldName, final Class<? extends AbstractEntity<?>> yieldEntityType, final EntityResultQueryModel<T> ... sourceModels) {
        assertYieldNullability(true, yieldName, yieldEntityType, startModel(sourceModels));
    }

    private static IFromAlias<EntityAggregates> startModel(final AggregatedResultQueryModel ... sourceModels) {
        return select(sourceModels);
    }
    
    @SafeVarargs
    private static <T extends AbstractEntity<?>> IFromAlias<T> startModel(final EntityResultQueryModel<T> ... sourceModels) {
        return select(sourceModels);
    }
    
    private static <T extends AbstractEntity<?>> void assertYieldNullability(final boolean isNullable, final String yieldName, final Class<? extends AbstractEntity<?>> yieldEntityType, final IFromAlias<T> startModel) {
        final var act = startModel.yield().prop(yieldName + ".key").as("result").modelAsAggregate();
        final var expStart = isNullable ? startModel.as("parent").leftJoin(yieldEntityType) : startModel.as("parent").join(yieldEntityType);
        final var exp = expStart.as("child").on().prop("parent." + yieldName).eq().prop("child").yield().prop("child.key").as("result").modelAsAggregate();
        assertModelResultsAreEqual(exp, act);
    }
}