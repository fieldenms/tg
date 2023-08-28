package ua.com.fielden.platform.eql.stage3;


import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.entity.AbstractEntity.DESC;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.eql.meta.PropType.BIGDECIMAL_PROP_TYPE;
import static ua.com.fielden.platform.eql.meta.PropType.INTEGER_PROP_TYPE;
import static ua.com.fielden.platform.eql.meta.PropType.LONG_PROP_TYPE;
import static ua.com.fielden.platform.eql.meta.PropType.STRING_PROP_TYPE;

import org.junit.Ignore;
import org.junit.Test;

import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.PrimitiveResultQueryModel;
import ua.com.fielden.platform.eql.meta.EqlStage3TestCase;
import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.eql.stage3.conditions.Conditions3;
import ua.com.fielden.platform.eql.stage3.etc.Yield3;
import ua.com.fielden.platform.eql.stage3.etc.Yields3;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.eql.stage3.operands.functions.MaxOf3;
import ua.com.fielden.platform.eql.stage3.operands.queries.ResultQuery3;
import ua.com.fielden.platform.eql.stage3.operands.queries.SourceQuery3;
import ua.com.fielden.platform.eql.stage3.operands.queries.SubQuery3;
import ua.com.fielden.platform.eql.stage3.sources.IJoinNode3;
import ua.com.fielden.platform.eql.stage3.sources.Source3BasedOnQueries;
import ua.com.fielden.platform.eql.stage3.sources.Source3BasedOnTable;
import ua.com.fielden.platform.sample.domain.TeVehicle;
import ua.com.fielden.platform.sample.domain.TeVehicleModel;
import ua.com.fielden.platform.sample.domain.TeWorkOrder;
import ua.com.fielden.platform.sample.domain.TgSynBogie;

public class QmToStage3TransformationTest extends EqlStage3TestCase {
    
    @Test
    public void common_subproperty_of_union_property_is_resolved() {
        qryCountAll(select(TgSynBogie.class).where().prop("location.id").isNotNull());
    }
    
    @Test
    public void invoking_id_property_on_persistent_property_of_entity_type_does_not_generate_extra_join() {
        final ResultQuery3 actQry = qryCountAll(select(MODEL).where().prop("make.id").isNotNull());
        
        final Source3BasedOnTable model = source(MODEL, 1);
        final Conditions3 conditions = or(isNotNull(prop("make", model, LONG_PROP_TYPE)));
        final ResultQuery3 expQry = qryCountAll(sources(model), conditions);
        
        assertEquals(expQry, actQry);
    }
    
    @Test
    public void invoking_id_property_on_calculated_property_of_entity_type_does_not_generate_extra_join() {
        final ResultQuery3 actQry = qryCountAll(select(VEHICLE).where().prop("modelMake.id").isNotNull());
        
        final Source3BasedOnTable veh = source(VEHICLE, 1);
        final Source3BasedOnTable model = source(MODEL, 2);
        
        final IJoinNode3 sources = 
                ij(
                        veh,
                        model,
                        eq(entityProp("model", veh, MODEL), idProp(model))
                  );
        final Conditions3 conditions = or(isNotNull(entityProp("make", model, MAKE)));
        final ResultQuery3 expQry = qryCountAll(sources, conditions);
        
        assertEquals(expQry, actQry);
    }
    
    @Test
    public void calc_props_of_component_type_are_resolved_correctly() {
        final ResultQuery3 actQry = qryCountAll(select(ORG5).where().anyOfProps("maxVehPrice", "maxVehPurchasePrice").isNotNull());
        
        final Source3BasedOnTable ou5 = source(ORG5, 1);

        final Source3BasedOnTable veh1 = source(VEHICLE, 2);
        final IJoinNode3 subQrySources1 = sources(veh1); 
        final Conditions3 subQryConditions1 = cond(eq(entityProp("station", veh1, ORG5), idProp(ou5)));
        final SubQuery3 expSubQry1 = subqry(subQrySources1, subQryConditions1, yields(new Yield3(new MaxOf3(prop("price.amount", veh1, BIGDECIMAL_PROP_TYPE), BIGDECIMAL_PROP_TYPE), "", nextSqlId(), BIGDECIMAL_PROP_TYPE)), BIGDECIMAL_PROP_TYPE);

        final Source3BasedOnTable veh2 = source(VEHICLE, 3);
        final IJoinNode3 subQrySources2 = sources(veh2); 
        final Conditions3 subQryConditions2 = cond(eq(entityProp("station", veh2, ORG5), idProp(ou5)));
        final SubQuery3 expSubQry2 = subqry(subQrySources2, subQryConditions2, yields(new Yield3(new MaxOf3(prop("purchasePrice.amount", veh2, BIGDECIMAL_PROP_TYPE), BIGDECIMAL_PROP_TYPE), "", nextSqlId(), BIGDECIMAL_PROP_TYPE)), BIGDECIMAL_PROP_TYPE);

        final IJoinNode3 sources = sources(ou5);
        final Conditions3 conditions = or(and(or(isNotNull(expSubQry1), isNotNull(expSubQry2))));
        final ResultQuery3 expQry = qryCountAll(sources, conditions);
        assertEquals(expQry, actQry);
    }
    
    @Test
    public void yielding_entity_id_under_different_alias_preserves_entity_type_info() {
        final AggregatedResultQueryModel qry = select(TeVehicleModel.class).
                yield().prop("id").as("model").
                yield().prop("make").as("make").
                modelAsAggregate();
        
        final ResultQuery3 actQry = qry(qry);
        
        final Source3BasedOnTable source = source(MODEL, 1);
        
        final Yield3 modelYield = yieldId(source, "model");
        final Yield3 makeYield = yieldProp("make", source, "make", new PropType(MAKE, H_LONG));
        final Yields3 yields = yields(modelYield, makeYield);
        
        final ResultQuery3 expQry = qry(sources(source), yields);
        assertEquals(expQry, actQry);
    }
    
    @Test
    public void correlated_source_query_works() {
        final AggregatedResultQueryModel sourceQry = select(TeVehicle.class).where().prop("model").eq().extProp("id").yield().countAll().as("qty").modelAsAggregate();
        final PrimitiveResultQueryModel qtySubQry = select(sourceQry).yield().prop("qty").modelAsPrimitive();
        final AggregatedResultQueryModel qry = select(TeVehicleModel.class).yield().model(qtySubQry).as("qty").modelAsAggregate();

        final ResultQuery3 actQry = qry(qry);
        
        final Source3BasedOnTable modelSource = source(MODEL, 3);
        
        final Source3BasedOnTable vehSource = source(VEHICLE, 1);
        final IJoinNode3 vehSources = sources(vehSource);
        final ISingleOperand3 vehModelProp = entityProp("model", vehSource, MODEL);
        final ISingleOperand3 modelIdProp = idProp(modelSource);
        final Conditions3 vehConditions = or(eq(vehModelProp, modelIdProp));
        final Yields3 vehYields = yields(yieldCountAll("qty"));

        final SourceQuery3 vehSourceQry = srcqry(vehSources, vehConditions, vehYields);
        
        final Source3BasedOnQueries qtyQrySource = source(2, vehSourceQry);
        final IJoinNode3 qtyQrySources = sources(qtyQrySource);
        final Yields3 qtyQryYields = yields(yieldProp("qty", qtyQrySource, "", INTEGER_PROP_TYPE));
        
        final Yields3 modelQryYields = yields(yieldModel(subqry(qtyQrySources, qtyQryYields, INTEGER_PROP_TYPE), "qty", INTEGER_PROP_TYPE));
        
        final ResultQuery3 expQry = qry(sources(modelSource), modelQryYields);
        assertEquals(expQry, actQry);
    }
    
    @Test
    public void calc_prop_is_correctly_transformed_10() {
        final ResultQuery3 actQry = qryCountAll(select(WORK_ORDER).where().anyOfProps("vehicle.modelMakeKey", "vehicle.model.make.key").isNotNull());

        final Source3BasedOnTable wo = source(WORK_ORDER, 1);
        final Source3BasedOnTable veh = source(VEHICLE, 2);
        final Source3BasedOnTable model = source(MODEL, 3);
        final Source3BasedOnTable make = source(MAKE, 4);
        final IJoinNode3 sources = 
                lj(
                        wo,
                        ij(
                                veh,
                                ij(
                                        model,
                                        make,
                                        eq(entityProp("make", model, MAKE), idProp(make))
                                  ),
                                eq(entityProp("model", veh, MODEL), idProp(model))
                          ),
                        eq(entityProp("vehicle", wo, VEHICLE), idProp(veh))
                  );
        final Conditions3 conditions = or(and(or(isNotNull(stringProp(KEY, make)), isNotNull(stringProp(KEY, make)))));
        final ResultQuery3 expQry = qryCountAll(sources, conditions);
        
        assertEquals(expQry, actQry);
    }

    @Test
    public void calc_prop_is_correctly_transformed_11() {
        final ResultQuery3 actQry = qryCountAll(select(WORK_ORDER).where().anyOfProps("vehicle.modelMakeKey", "vehicle.modelMakeKeyDuplicate").isNotNull());

        final Source3BasedOnTable wo = source(WORK_ORDER, 1);
        final Source3BasedOnTable veh = source(VEHICLE, 2);
        final Source3BasedOnTable model = source(MODEL, 3);
        final Source3BasedOnTable make = source(MAKE, 4);
        final IJoinNode3 sources = 
                lj(
                        wo,
                        ij(
                                veh,
                                ij(
                                        model,
                                        make,
                                        eq(entityProp("make", model, MAKE), idProp(make))
                                  ),
                                eq(entityProp("model", veh, MODEL), idProp(model))
                          ),
                        eq(entityProp("vehicle", wo, VEHICLE), idProp(veh))
                  );
        final Conditions3 conditions = or(and(or(isNotNull(stringProp(KEY, make)), isNotNull(stringProp(KEY, make)))));
        final ResultQuery3 expQry = qryCountAll(sources, conditions);
        
        assertEquals(expQry, actQry);
    }

    
    @Test
    public void calc_prop_is_correctly_transformed_13() {
        final ResultQuery3 actQry = qryCountAll(select(WORK_ORDER).where().anyOfProps("vehicle.modelKey", "vehicle.model.key").isNotNull());
        
        final Source3BasedOnTable wo = source(WORK_ORDER, 1);
        final Source3BasedOnTable veh = source(VEHICLE, 2);
        final Source3BasedOnTable model = source(MODEL, 3);
        
        final IJoinNode3 sources = 
                lj(
                        wo,
                        ij(
                                veh,
                                model,
                                eq(entityProp("model", veh, MODEL), idProp(model))
                          ),
                        eq(entityProp("vehicle", wo, VEHICLE), idProp(veh))
                  );
        final Conditions3 conditions = or(and(or(isNotNull(stringProp(KEY, model)), isNotNull(stringProp(KEY, model)))));
        final ResultQuery3 expQry = qryCountAll(sources, conditions);
        
        assertEquals(expQry, actQry);    
    }
    
    @Test
    @Ignore
    public void calc_prop_is_correctly_transformed_12() {
//        //protected static final ExpressionModel TgVehicle.modelMakeKey6_ = expr().model(select(TgVehicleModel.class).where().prop("id").eq().extProp("model").yield().prop("model.makeKey2").modelAsPrimitive()).model();
//        //protected static final ExpressionModel makeKey2_ = expr().model(select(TgVehicleMake.class).where().prop("id").eq().extProp("make").yield().prop(KEY).modelAsPrimitive()).model();

        final ResultQuery3 actQry = qryCountAll(select(WORK_ORDER).where().anyOfProps("vehicle.modelMakeKey6").isNotNull());
    }

    @Test
    @Ignore
    public void calc_prop_is_correctly_transformed_09() {
//        //protected static final ExpressionModel TgWorkOrder.makeKey2_ = expr().model(select(TgVehicle.class).where().prop("id").eq().extProp("vehicle").yield().prop("modelMakeKey4").modelAsPrimitive()).model();
//        //protected static final ExpressionModel TgVehicle.modelMakeKey4_ = expr().model(select(TgVehicleModel.class).where().prop("id").eq().extProp("model").yield().prop("model.makeKey2").modelAsPrimitive()).model();
//        //protected static final ExpressionModel makeKey2_ = expr().model(select(TgVehicleMake.class).where().prop("id").eq().extProp("make").yield().prop(KEY).modelAsPrimitive()).model();
        
        final ResultQuery3 actQry = qryCountAll(select(WORK_ORDER).where().anyOfProps("makeKey2").isNotNull());
    }
    
    @Test
    public void calc_prop_is_correctly_transformed_14() {
        final ResultQuery3 actQry = qryCountAll(select(TeWorkOrder.class).where().prop("vehicleModel.key").isNotNull());

        final Source3BasedOnTable wo = source(WORK_ORDER, 1);

    }
    
    @Test
    public void calc_prop_is_correctly_transformed_08() {
        final ResultQuery3 actQry = qryCountAll(select(WORK_ORDER).where().prop("make.key").isNotNull());

        final Source3BasedOnTable wo = source(WORK_ORDER, 1);
        final Source3BasedOnTable veh = source(VEHICLE, 2);
        final Source3BasedOnTable model = source(MODEL, 3);
        final Source3BasedOnTable make = source(MAKE, 4);
        
        final IJoinNode3 subQrySources = 
                ij(
                        veh,  
                        model, 
                        eq(entityProp("model", veh, MODEL), idProp(model))
                  );
        
        final Conditions3 subQryConditions = cond(eq(idProp(veh), entityProp("vehicle", wo, VEHICLE)));
        
        final SubQuery3 expSubQry = subqry(subQrySources, subQryConditions, yields(yieldSingleEntity("make", model, MAKE)), new PropType(MAKE, H_LONG));

        final IJoinNode3 sources = 
                lj(
                        wo,
                        make,
                        eq(expSubQry, idProp(make))                
                  );
        final Conditions3 conditions = or(isNotNull(stringProp(KEY, make)));
        final ResultQuery3 expQry = qryCountAll(sources, conditions);
        
        assertEquals(expQry, actQry);
    }
    
    @Test
    public void calc_prop_is_correctly_transformed_07() {
        final ResultQuery3 actQry = qryCountAll(select(WORK_ORDER).where().prop("make").isNotNull());

        final Source3BasedOnTable wo = source(WORK_ORDER, 1);
        final Source3BasedOnTable veh = source(VEHICLE, 2);
        final Source3BasedOnTable model = source(MODEL, 3);
        
        final IJoinNode3 subQrySources = 
                ij(
                        veh,  
                        model, 
                        eq(entityProp("model", veh, MODEL), idProp(model))
                  );
        
        final Conditions3 subQryConditions = cond(eq(idProp(veh), entityProp("vehicle", wo, VEHICLE)));
        
        final SubQuery3 expSubQry = subqry(subQrySources, subQryConditions, yields(yieldSingleEntity("make", model, MAKE)), new PropType(MAKE, H_LONG));

        final IJoinNode3 sources = sources(wo);
        final Conditions3 conditions = or(isNotNull(expSubQry));
        final ResultQuery3 expQry = qryCountAll(sources, conditions);
        
        assertEquals(expQry, actQry);
    }
    
    @Test
    public void calc_prop_is_correctly_transformed_06() {
        final ResultQuery3 actQry = qryCountAll(select(WORK_ORDER).where().prop("makeKey").isNotNull());
        
        final Source3BasedOnTable wo = source(WORK_ORDER, 1);
        final Source3BasedOnTable veh = source(VEHICLE, 2);
        final Source3BasedOnTable model = source(MODEL, 3);
        final Source3BasedOnTable make = source(MAKE, 4);
        
        final IJoinNode3 subQrySources = 
                ij(
                        veh,  
                        ij(
                                model, 
                                make, 
                                eq(entityProp("make", model, MAKE), idProp(make))
                          ), 
                        eq(entityProp("model", veh, MODEL), idProp(model))
                  );
        
        final Conditions3 subQryConditions = cond(eq(idProp(veh), entityProp("vehicle", wo, VEHICLE)));
        
        final SubQuery3 expSubQry = subqry(subQrySources, subQryConditions, yields(yieldSingleString(KEY, make)), STRING_PROP_TYPE);

        final IJoinNode3 sources = sources(wo);
        final Conditions3 conditions = or(isNotNull(expSubQry));
        final ResultQuery3 expQry = qryCountAll(sources, conditions);
        
        assertEquals(expQry, actQry);
    }
    
    @Test
    public void calc_prop_is_correctly_transformed_05() {
        final ResultQuery3 actQry = qryCountAll(select(WORK_ORDER).where().prop("vehicleModel.key").isNotNull());
        
        final Source3BasedOnTable wo = source(WORK_ORDER, 1);
        final Source3BasedOnTable veh = source(VEHICLE, 2);
        final Source3BasedOnTable model = source(MODEL, 3);
        
        final IJoinNode3 sources = 
                lj(
                        lj(
                                wo, 
                                veh, 
                                eq(entityProp("vehicle", wo, VEHICLE), idProp(veh))
                          ),
                        model,
                        eq(entityProp("model", veh, MODEL), idProp(model))                
                  );
        final Conditions3 conditions = or(isNotNull(stringProp(KEY, model)));
        final ResultQuery3 expQry = qryCountAll(sources, conditions);

        assertEquals(expQry, actQry);
    }

    @Test
    public void calc_prop_is_correctly_transformed_04() {
        final ResultQuery3 actQry = qryCountAll(select(WORK_ORDER).where().prop("vehicleModel.makeKey").isNotNull());
        
        final Source3BasedOnTable wo = source(WORK_ORDER, 1);
        final Source3BasedOnTable veh = source(VEHICLE, 2);
        final Source3BasedOnTable model = source(MODEL, 3);
        final Source3BasedOnTable make = source(MAKE, 4);
        
        final IJoinNode3 sources = 
                lj(
                        lj(
                                wo,
                                veh,
                                eq(entityProp("vehicle", wo, VEHICLE), idProp(veh))
                          ),  
                        ij(
                                model,
                                make,
                                eq(entityProp("make", model, MAKE), idProp(make))
                          ), 
                        eq(entityProp("model", veh, MODEL), idProp(model))                
                  );
        final Conditions3 conditions = or(isNotNull(stringProp(KEY, make)));
        final ResultQuery3 expQry = qryCountAll(sources, conditions);
        
        assertEquals(expQry, actQry);
    }

    @Test
    public void calc_prop_is_correctly_transformed_03() {
        final ResultQuery3 actQry = qryCountAll(select(WORK_ORDER).where().prop("vehicle.modelMakeKey2").isNotNull());
        
        final Source3BasedOnTable wo = source(WORK_ORDER, 1);
        final Source3BasedOnTable veh = source(VEHICLE, 2);
        final Source3BasedOnTable model = source(MODEL, 3);
        final Source3BasedOnTable make = source(MAKE, 4);
        
        final IJoinNode3 sources = 
                lj(
                        wo,
                        ij(
                                veh,  
                                ij(
                                        model,
                                        make,
                                        eq(entityProp("make", model, MAKE), idProp(make))
                                  ), 
                                eq(entityProp("model", veh, MODEL), idProp(model))
                          ),
                        eq(entityProp("vehicle", wo, VEHICLE), idProp(veh))
                  );
        final Conditions3 conditions = or(isNotNull(stringProp(KEY, make)));
        final ResultQuery3 expQry = qryCountAll(sources, conditions);
        
        assertEquals(expQry, actQry);
    }

    @Test
    public void calc_prop_is_correctly_transformed_02() {
        final ResultQuery3 actQry = qryCountAll(select(WORK_ORDER).where().prop("vehicle.modelMakeKey").isNotNull());
        
        final Source3BasedOnTable wo = source(WORK_ORDER, 1);
        final Source3BasedOnTable veh = source(VEHICLE, 2);
        final Source3BasedOnTable model = source(MODEL, 3);
        final Source3BasedOnTable make = source(MAKE, 4);
        
        final IJoinNode3 sources = 
                lj(
                        wo,
                        ij(
                                veh,
                                ij(
                                        model,
                                        make,
                                        eq(entityProp("make", model, MAKE), idProp(make))
                                  ),
                                eq(entityProp("model", veh, MODEL), idProp(model))
                          ),
                        eq(entityProp("vehicle", wo, VEHICLE), idProp(veh))
                  );
        final Conditions3 conditions = or(isNotNull(stringProp(KEY, make)));
        final ResultQuery3 expQry = qryCountAll(sources, conditions);
        
        assertEquals(expQry, actQry);
    }
    
    @Test
    public void veh_calc_prop_is_correctly_transformed_08() {
        final ResultQuery3 actQry = qryCountAll(select(VEHICLE).where().anyOfProps("modelMakeKey2", "make.key", "model.make.key").isNotNull());
        
        final Source3BasedOnTable veh = source(VEHICLE, 1);
        final Source3BasedOnTable model = source(MODEL, 3);
        final Source3BasedOnTable makeA = source(MAKE, 2);
        final Source3BasedOnTable make = source(MAKE, 4);
        
        final IJoinNode3 sources =
                ij(
                        lj(
                                veh,
                                makeA,
                                eq(entityProp("make", veh, MAKE), idProp(makeA))),
                        ij(
                                model,
                                make,
                                eq(entityProp("make", model, MAKE), idProp(make))),
                        eq(entityProp("model", veh, MODEL), idProp(model))
                  );
        final Conditions3 conditions = or(and(or(isNotNull(stringProp(KEY, make)), isNotNull(stringProp(KEY, makeA)), isNotNull(stringProp(KEY, make)))));
        final ResultQuery3 expQry = qryCountAll(sources, conditions);
        
        assertEquals(expQry, actQry);   
    }

    @Test
    public void veh_calc_prop_is_correctly_transformed_06() {
        final ResultQuery3 actQry = qryCountAll(select(VEHICLE).where().anyOfProps("modelMakeKey2", "make.key").isNotNull());
        
        final Source3BasedOnTable veh = source(VEHICLE, 1);
        final Source3BasedOnTable model = source(MODEL, 3);
        final Source3BasedOnTable makeA = source(MAKE, 2);
        final Source3BasedOnTable make = source(MAKE, 4);
        
        final IJoinNode3 sources =
                ij(
                        lj(
                                veh,
                                makeA,
                                eq(entityProp("make", veh, MAKE), idProp(makeA))),
                        ij(
                                model,
                                make,
                                eq(entityProp("make", model, MAKE), idProp(make))),
                        eq(entityProp("model", veh, MODEL), idProp(model))
                  );
        final Conditions3 conditions = or(and(or(isNotNull(stringProp(KEY, make)), isNotNull(stringProp(KEY, makeA)))));
        final ResultQuery3 expQry = qryCountAll(sources, conditions);
        
        assertEquals(expQry, actQry);
    }
    
    @Test
    public void veh_calc_prop_is_correctly_transformed_05() {
        final ResultQuery3 actQry = qryCountAll(select(VEHICLE).where().anyOfProps("model.make.key", "make.key").isNotNull());
        
        final Source3BasedOnTable veh = source(VEHICLE, 1);
        final Source3BasedOnTable model = source(MODEL, 3);
        final Source3BasedOnTable make = source(MAKE, 4);
        final Source3BasedOnTable makeA = source(MAKE, 2);
        
        final IJoinNode3 sources =
                ij(
                        lj(
                                veh,
                                makeA,
                                eq(entityProp("make", veh, MAKE), idProp(makeA))),
                        ij(
                                model,
                                make,
                                eq(entityProp("make", model, MAKE), idProp(make))),
                        eq(entityProp("model", veh, MODEL), idProp(model))
                  );
        final Conditions3 conditions = or(and(or(isNotNull(stringProp(KEY, make)), isNotNull(stringProp(KEY, makeA)))));
        final ResultQuery3 expQry = qryCountAll(sources, conditions);
        
        assertEquals(expQry, actQry);    
    }
    
    @Test
    public void veh_calc_prop_is_correctly_transformed_04() {
        final ResultQuery3 actQry = qryCountAll(select(VEHICLE).where().anyOfProps("modelMakeKey2", "model.make.key").isNotNull());
        final Source3BasedOnTable veh = source(VEHICLE, 1);
        final Source3BasedOnTable model = source(MODEL, 2);
        final Source3BasedOnTable make = source(MAKE, 3);
        
        final IJoinNode3 sources =
                ij(
                        veh,
                        ij(
                                model,
                                make,
                                eq(entityProp("make", model, MAKE), idProp(make))
                          ),
                        eq(entityProp("model", veh, MODEL), idProp(model))
                  );
        final Conditions3 conditions = or(and(or(isNotNull(stringProp(KEY, make)), isNotNull(stringProp(KEY, make)))));
        final ResultQuery3 expQry = qryCountAll(sources, conditions);
        
        assertEquals(expQry, actQry);
    }
    
    @Test
    public void veh_calc_prop_is_correctly_transformed_03() {
        final ResultQuery3 actQry = qryCountAll(select(VEHICLE).where().anyOfProps("modelMakeKey", "modelMakeDesc").isNotNull());
        
        final Source3BasedOnTable veh = source(VEHICLE, 1);
        final Source3BasedOnTable model = source(MODEL, 2);
        final Source3BasedOnTable make = source(MAKE, 3);
        
        final IJoinNode3 sources = 
                ij(
                        veh,
                        ij(
                                model,
                                make,
                                eq(entityProp("make", model, MAKE), idProp(make))
                          ),
                        eq(entityProp("model", veh, MODEL), idProp(model))
                  );
        final Conditions3 conditions = or(and(or(isNotNull(stringProp(KEY, make)), isNotNull(stringProp(DESC, make)))));
        final ResultQuery3 expQry = qryCountAll(sources, conditions);
        
        assertEquals(expQry, actQry);
    }
    
    @Test
    public void veh_calc_prop_is_correctly_transformed_02() {
        final ResultQuery3 actQry = qryCountAll(select(VEHICLE).where().anyOfProps("modelKey", "modelDesc").isNotNull());
        final Source3BasedOnTable veh = source(VEHICLE, 1);
        final Source3BasedOnTable model = source(MODEL, 2);
        
        final IJoinNode3 sources = 
                ij(
                        veh,
                        model,
                        eq(entityProp("model", veh, MODEL), idProp(model))
                  );
        final Conditions3 conditions = or(and(or(isNotNull(stringProp(KEY, model)), isNotNull(stringProp(DESC, model)))));
        final ResultQuery3 expQry = qryCountAll(sources, conditions);
        
        assertEquals(expQry, actQry);
    }

    @Test
    public void veh_calc_prop_is_correctly_transformed_01() {
        final ResultQuery3 actQry = qryCountAll(select(VEHICLE).where().prop("modelMakeKey").isNotNull());
        final Source3BasedOnTable veh = source(VEHICLE, 1);
        final Source3BasedOnTable model = source(MODEL, 2);
        final Source3BasedOnTable make = source(MAKE, 3);
        
        final IJoinNode3 sources = 
                ij(
                        veh,
                        ij(
                                model,
                                make,
                                eq(entityProp("make", model, MAKE), idProp(make))
                          ),
                        eq(entityProp("model", veh, MODEL), idProp(model))
                  );
        final Conditions3 conditions = or(isNotNull(stringProp(KEY, make)));
        final ResultQuery3 expQry = qryCountAll(sources, conditions);
        
        assertEquals(expQry, actQry);
    }


    @Test
    public void veh_model_calc_prop_is_correctly_transformed_05() {
        final ResultQuery3 actQry = qryCountAll(select(MODEL).where().anyOfProps("makeKey", "makeKey2", "make.key").isNotNull());
        
        final Source3BasedOnTable model = source(MODEL, 1);
        final Source3BasedOnTable make = source(MAKE, 3);
        
        final Source3BasedOnTable subQryMake = source(MAKE, 2);
        
        final IJoinNode3 subQrySources = sources(subQryMake);
        
        final Conditions3 subQryConditions = cond(eq(idProp(subQryMake), entityProp("make", model, MAKE)));
        
        final SubQuery3 expSubQry = subqry(subQrySources, subQryConditions, yields(yieldSingleString(KEY, subQryMake)), STRING_PROP_TYPE);

        
        final IJoinNode3 sources = 
                ij(
                        model,
                        make,
                        eq(entityProp("make", model, MAKE), idProp(make))
                  );
        final Conditions3 conditions = or(and(or(isNotNull(stringProp(KEY, make)), isNotNull(expSubQry), isNotNull(stringProp(KEY, make)))));
        final ResultQuery3 expQry = qryCountAll(sources, conditions);
        
        assertEquals(expQry, actQry);
    }

    @Test
    public void veh_model_calc_prop_is_correctly_transformed_04() {
        final ResultQuery3 actQry = qryCountAll(select(MODEL).where().anyOfProps("makeKey", "makeKey2").isNotNull());
        final Source3BasedOnTable model = source(MODEL, 1);
        final Source3BasedOnTable make = source(MAKE, 3);
        final Source3BasedOnTable subQryMake = source(MAKE, 2);
        
        final IJoinNode3 subQrySources = sources(subQryMake);
        
        final Conditions3 subQryConditions = cond(eq(idProp(subQryMake), entityProp("make", model, MAKE)));
        
        final SubQuery3 expSubQry = subqry(subQrySources, subQryConditions, yields(yieldSingleString(KEY, subQryMake)), STRING_PROP_TYPE);

        
        final IJoinNode3 sources = 
                ij(
                        model,
                        make,
                        eq(entityProp("make", model, MAKE), idProp(make))
                  );
        final Conditions3 conditions = or(and(or(isNotNull(stringProp(KEY, make)), isNotNull(expSubQry))));
        final ResultQuery3 expQry = qryCountAll(sources, conditions);
        
        assertEquals(expQry, actQry);
    }

    @Test
    public void veh_model_calc_prop_is_correctly_transformed_03() {
        final ResultQuery3 actQry = qryCountAll(select(MODEL).where().anyOfProps("makeKey", "make.key").isNotNull());
        final Source3BasedOnTable model = source(MODEL, 1);
        final Source3BasedOnTable make = source(MAKE, 2);
        
        final IJoinNode3 sources = 
                ij(
                        model,
                        make,
                        eq(entityProp("make", model, MAKE), idProp(make))
                  );
        final Conditions3 conditions = or(and(or(isNotNull(stringProp(KEY, make)), isNotNull(stringProp(KEY, make)))));
        final ResultQuery3 expQry = qryCountAll(sources, conditions);
        
        assertEquals(expQry, actQry);
    }
    
    @Test
    public void veh_model_calc_prop_is_correctly_transformed_02() {
        final ResultQuery3 actQry = qryCountAll(select(MODEL).where().prop("makeKey2").isNotNull());
        final Source3BasedOnTable model = source(MODEL, 1);

        final Source3BasedOnTable subQryMake = source(MAKE, 2);
        
        final IJoinNode3 subQrySources = sources(subQryMake);
        
        final Conditions3 subQryConditions = cond(eq(idProp(subQryMake), entityProp("make", model, MAKE)));
        
        final SubQuery3 expSubQry = subqry(subQrySources, subQryConditions, yields(yieldSingleString(KEY, subQryMake)), STRING_PROP_TYPE);

        final IJoinNode3 sources = sources(model);
        final Conditions3 conditions = or(isNotNull(expSubQry));
        final ResultQuery3 expQry = qryCountAll(sources, conditions);
        
        assertEquals(expQry, actQry);
    }

    @Test
    public void veh_model_calc_prop_is_correctly_transformed_01() {
        final ResultQuery3 actQry = qryCountAll(select(MODEL).where().prop("makeKey").isNotNull());
        final Source3BasedOnTable model = source(MODEL, 1);
        final Source3BasedOnTable make = source(MAKE, 2);
        
        final IJoinNode3 sources = 
                ij(
                        model,
                        make,
                        eq(entityProp("make", model, MAKE), idProp(make))
                  );
        final Conditions3 conditions = or(isNotNull(stringProp(KEY, make)));
        final ResultQuery3 expQry = qryCountAll(sources, conditions);
        
        assertEquals(expQry, actQry);
    }

    @Test
    public void dot_notated_props_are_correctly_transformed_01() {
        final ResultQuery3 actQry = qryCountAll(select(VEHICLE).where().anyOfProps(KEY, "replacedBy.key").isNotNull());
        final Source3BasedOnTable veh = source(VEHICLE, 1);
        final Source3BasedOnTable repVeh = source(VEHICLE, 2);
        
        final IJoinNode3 sources = 
                lj(
                        veh,
                        repVeh,
                        eq(entityProp("replacedBy", veh, VEHICLE), idProp(repVeh))
                  );
        final Conditions3 conditions = or(and(or(isNotNull(stringProp(KEY, veh)), isNotNull(stringProp(KEY, repVeh)))));
        final ResultQuery3 expQry = qryCountAll(sources, conditions);
        
        assertEquals(expQry, actQry);
    }

    @Test
    public void dot_notated_props_are_correctly_transformed_02() {
        final ResultQuery3 actQry = qryCountAll(select(VEHICLE).where().anyOfProps("initDate", "station.name", "station.parent.name", "replacedBy.initDate").isNotNull());
        final Source3BasedOnTable veh = source(VEHICLE, 1);
        final Source3BasedOnTable repVeh = source(VEHICLE, 2);
        final Source3BasedOnTable org5 = source(ORG5, 3);
        final Source3BasedOnTable org4 = source(ORG4, 4);
        
        final IJoinNode3 sources = 
                lj(
                        lj(
                                veh,
                                repVeh,
                                eq(entityProp("replacedBy", veh, VEHICLE), idProp(repVeh))
                          ),
                        ij(
                                org5,
                                org4,
                                eq(entityProp("parent", org5, ORG4), idProp(org4))
                          ),
                        eq(entityProp("station", veh, ORG5), idProp(org5))
                  );
        final Conditions3 conditions = or(and(or(isNotNull(dateProp("initDate", veh)), isNotNull(stringProp("name", org5)), isNotNull(stringProp("name", org4)), isNotNull(dateProp("initDate", repVeh)))));
        final ResultQuery3 expQry = qryCountAll(sources, conditions);
        
        assertEquals(expQry, actQry);
    }
    
    @Test
    public void dot_notated_props_are_correctly_transformed_03() {
        final ResultQuery3 actQry = qryCountAll(select(VEHICLE).as("veh").join(ORG5).as("ou5e").on().prop("veh.station").eq().prop("ou5e.id").where().anyOfProps("veh.key", "veh.replacedBy.key").isNotNull());
        final Source3BasedOnTable veh = source(VEHICLE, 1);
        final Source3BasedOnTable repVeh = source(VEHICLE, 3);
        final Source3BasedOnTable ou5e = source(ORG5, 2);

        final IJoinNode3 sources = 
                ij(
                        lj(
                                veh,
                                repVeh,
                                eq(entityProp("replacedBy", veh, VEHICLE), idProp(repVeh))
                          ),
                        ou5e,
                        eq(entityProp("station", veh, ORG5), idProp(ou5e))
                  );
        final Conditions3 conditions = or(and(or(isNotNull(stringProp(KEY, veh)), isNotNull(stringProp(KEY, repVeh)))));
        final ResultQuery3 expQry = qryCountAll(sources, conditions);

        assertEquals(expQry, actQry);
    }
    
    @Test
    public void dot_notated_props_are_correctly_transformed_04() {
        final ResultQuery3 actQry = qryCountAll(select(VEHICLE).as("veh").join(ORG5).as("ou5e").on().prop("station").eq().prop("ou5e.id").
                where().anyOfProps("veh.key", "replacedBy.key", "initDate", "station.name", "station.parent.name", "ou5e.parent.name").isNotNull());

        final Source3BasedOnTable veh = source(VEHICLE, 1);
        final Source3BasedOnTable repVeh = source(VEHICLE, 3);
        final Source3BasedOnTable ou5e = source(ORG5, 2);
        final Source3BasedOnTable ou5eou4 = source(ORG4, 6);
        final Source3BasedOnTable ou5 = source(ORG5, 4);
        final Source3BasedOnTable ou4 = source(ORG4, 5);

        final IJoinNode3 sources = 
                ij(
                        lj(
                                lj(
                                        veh,
                                        repVeh,
                                        eq(entityProp("replacedBy", veh, VEHICLE), idProp(repVeh))
                                  ),
                                ij(
                                        ou5,
                                        ou4,
                                        eq(entityProp("parent", ou5, ORG4), idProp(ou4))
                                  ),
                                eq(entityProp("station", veh, ORG5), idProp(ou5))
                          ),
                        ij(
                                ou5e,
                                ou5eou4,
                                eq(entityProp("parent", ou5e, ORG4), idProp(ou5eou4))
                          ),
                        eq(entityProp("station", veh, ORG5), idProp(ou5e))
                  );
        final Conditions3 conditions = or(and(or(
                isNotNull(stringProp(KEY, veh)),
                isNotNull(stringProp(KEY, repVeh)), 
                isNotNull(dateProp("initDate", veh)), 
                isNotNull(stringProp("name", ou5)), 
                isNotNull(stringProp("name", ou4)),
                isNotNull(stringProp("name", ou5eou4)))));
        final ResultQuery3 expQry = qryCountAll(sources, conditions);

        assertEquals(expQry, actQry);
    }

    @Test
    public void dot_notated_props_are_correctly_transformed_05() {
        final ResultQuery3 actQry = qryCountAll(select(VEHICLE).
                join(ORG2).as("ou2e").on().prop("station.parent.parent.parent").eq().prop("ou2e.id").
                where().anyOfProps("initDate", "replacedBy.initDate", "station.name", "station.parent.name", "ou2e.parent.key").isNotNull());
        final Source3BasedOnTable veh = source(VEHICLE, 1);
        final Source3BasedOnTable repVeh = source(VEHICLE, 3);
        final Source3BasedOnTable ou5 = source(ORG5, 4);
        final Source3BasedOnTable ou4 = source(ORG4, 5);
        final Source3BasedOnTable ou3 = source(ORG3, 6);
        final Source3BasedOnTable ou2e = source(ORG2, 2);
        final Source3BasedOnTable ou2eou1 = source(ORG1, 7);

        final IJoinNode3 sources = 
                ij(
                        lj(
                                lj(
                                        veh,
                                        repVeh,
                                        eq(entityProp("replacedBy", veh, VEHICLE), idProp(repVeh))
                                  ),
                                ij(
                                        ou5,
                                        ij(
                                                ou4,
                                                ou3,
                                                eq(entityProp("parent", ou4, ORG3), idProp(ou3))
                                          ),
                                        eq(entityProp("parent", ou5, ORG4), idProp(ou4))
                                  ),
                                eq(entityProp("station", veh, ORG5), idProp(ou5))
                          ),
                        ij(
                                ou2e,
                                ou2eou1,
                                eq(entityProp("parent", ou2e, ORG1), idProp(ou2eou1))
                          ),
                        eq(entityProp("parent", ou3, ORG2), idProp(ou2e))
                  );
        final Conditions3 conditions = or(and(or(
                isNotNull(dateProp("initDate", veh)),
                isNotNull(dateProp("initDate", repVeh)), 
                isNotNull(stringProp("name", ou5)), 
                isNotNull(stringProp("name", ou4)),
                isNotNull(stringProp(KEY, ou2eou1)))));
        final ResultQuery3 expQry = qryCountAll(sources, conditions);

        assertEquals(expQry, actQry);
    }
 }