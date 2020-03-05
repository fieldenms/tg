package ua.com.fielden.platform.eql.stage3.elements;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;

import org.junit.Ignore;
import org.junit.Test;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.meta.EqlStage3TestCase;
import ua.com.fielden.platform.eql.stage1.elements.conditions.Conditions1;
import ua.com.fielden.platform.eql.stage1.elements.operands.EntQuery1;
import ua.com.fielden.platform.eql.stage1.elements.sources.QrySource1BasedOnPersistentType;
import ua.com.fielden.platform.eql.stage1.elements.sources.Sources1;
import ua.com.fielden.platform.eql.stage3.elements.conditions.Conditions3;
import ua.com.fielden.platform.eql.stage3.elements.operands.EntQuery3;
import ua.com.fielden.platform.eql.stage3.elements.sources.IQrySources3;
import ua.com.fielden.platform.eql.stage3.elements.sources.QrySource3BasedOnTable;

public class SqlGenerationTest extends EqlStage3TestCase {
    private final static String[] modelYields = new String[]{"id", "key", "desc", "version", "make"};
    private final static String[] vehicleYields = new String[]{"id", "key", "desc", "version", "initDate", "model", "make", "replacedBy", "station", "active", "leased", "lastMeterReading", "price", "purchasePrice"};
    private final static String[] woYields = new String[]{"id", "key", "desc", "version", "vehicle", "actCost", "estCost", "yearlyCost"};

    @Test
    public void calc_prop_is_correctly_transformed_10() {
        // select(WORK_ORDER).
        // where().anyOfProps("vehicle.modelMakeKey", "vehicle.model.make.key").isNotNull().model();
        
        final QrySource1BasedOnPersistentType wo1 = source(WORK_ORDER);
        final Conditions1 conditions1 = conditions(isNotNull(prop("vehicle.modelMakeKey")), or(isNotNull(prop("vehicle.model.make.key"))));

        final EntQuery3 actQry = queryCountAll(sources(wo1), conditions1);
        
        final QrySource3BasedOnTable wo = source(WORK_ORDER, wo1);
        final QrySource3BasedOnTable veh = source(VEHICLE, wo1, "vehicle");
        final QrySource3BasedOnTable model = source(MODEL, wo1, "vehicle_model");
        final QrySource3BasedOnTable make = source(MAKE, wo1, "vehicle_model_make");
        
        final IQrySources3 sources = 
                lj(
                        wo,
                        ij(
                                veh,
                                ij(
                                        model,
                                        make,
                                        eq(entityProp("make", model, MAKE), entityProp(ID, make, MAKE))
                                  ),
                                eq(entityProp("model", veh, MODEL), entityProp(ID, model, MODEL))
                          ),
                        eq(entityProp("vehicle", wo, VEHICLE), entityProp(ID, veh, VEHICLE))
                  );
        final Conditions3 conditions = or(isNotNull(expr(expr(stringProp(KEY, make)))), isNotNull(expr(stringProp(KEY, make))));
        final EntQuery3 expQry = qryCountAll(sources, conditions);
        
        assertEquals(expQry, actQry);
    }

    @Test
    public void calc_prop_is_correctly_transformed_13() {
        // select(WORK_ORDER).
        // where().anyOfProps("vehicle.modelKey", "vehicle.model.key").isNotNull().model();
        
        final QrySource1BasedOnPersistentType wo1 = source(WORK_ORDER);
        final Conditions1 conditions1 = conditions(isNotNull(prop("vehicle.modelKey")), or(isNotNull(prop("vehicle.model.key"))));

        final EntQuery3 actQry = queryCountAll(sources(wo1), conditions1);
        
        final QrySource3BasedOnTable wo = source(WORK_ORDER, wo1);
        final QrySource3BasedOnTable veh = source(VEHICLE, wo1, "vehicle");
        final QrySource3BasedOnTable model = source(MODEL, wo1, "vehicle_model");
        
        final IQrySources3 sources = 
                lj(
                        wo,
                        ij(
                                veh,
                                model,
                                eq(entityProp("model", veh, MODEL), entityProp(ID, model, MODEL))
                          ),
                        eq(entityProp("vehicle", wo, VEHICLE), entityProp(ID, veh, VEHICLE))
                  );
        final Conditions3 conditions = or(isNotNull(expr(expr(stringProp(KEY, model)))), isNotNull(expr(stringProp(KEY, model))));
        final EntQuery3 expQry = qryCountAll(sources, conditions);
        
        assertEquals(expQry, actQry);    
    }
    
    @Test
    @Ignore
    public void calc_prop_is_correctly_transformed_12() {
        // select(WORK_ORDER).
        // where().anyOfProps("vehicle.modelMakeKey6").isNotNull().model();
        
        //protected static final ExpressionModel TgVehicle.modelMakeKey6_ = expr().model(select(TgVehicleModel.class).where().prop("id").eq().extProp("model").yield().prop("model.makeKey2").modelAsPrimitive()).model();
        //protected static final ExpressionModel makeKey2_ = expr().model(select(TgVehicleMake.class).where().prop("id").eq().extProp("make").yield().prop(KEY).modelAsPrimitive()).model();

        final EntQuery1 calcPropSubqry1 = (EntQuery1)metadata.get(VEHICLE).getProps().get("modelMakeKey6").expression.first;
        
        final QrySource1BasedOnPersistentType wo1 = source(WORK_ORDER);
        final QrySource1BasedOnPersistentType vehModel1 = (QrySource1BasedOnPersistentType) calcPropSubqry1.sources.main;//source(MODEL);
        final Conditions1 conditions1 = conditions(isNotNull(prop("vehicle.modelMakeKey6")));
        


        final EntQuery3 actQry = queryCountAll(sources(wo1), conditions1);
        
        final QrySource3BasedOnTable wo = source(WORK_ORDER, wo1);
        final QrySource3BasedOnTable veh = source(VEHICLE, wo1, "vehicle");
        final QrySource3BasedOnTable model = source(MODEL, wo1, "vehicleModel");
        
        final IQrySources3 sources = 
                lj(
                        lj(
                                wo, 
                                veh, 
                                eq(prop("vehicle", wo), prop(ID, veh))
                          ),
                        model,
                        eq(expr(expr(prop("model", veh))), prop(ID, model))                
                  );
        final Conditions3 conditions = or(isNotNull(expr(prop(KEY, model))));
        final EntQuery3 expQry = qryCountAll(sources, conditions);
        assertEquals(expQry, actQry);
    }

    @Test
    @Ignore
    public void calc_prop_is_correctly_transformed_09() {
        // select(WORK_ORDER).
        // where().anyOfProps("makeKey2").isNotNull().model();
        
        final QrySource1BasedOnPersistentType wo1 = source(WORK_ORDER);
        final Conditions1 conditions1 = conditions(isNotNull(prop("makeKey2")));
        
        //protected static final ExpressionModel TgWorkOrder.makeKey2_ = expr().model(select(TgVehicle.class).where().prop("id").eq().extProp("vehicle").yield().prop("modelMakeKey4").modelAsPrimitive()).model();
        //protected static final ExpressionModel TgVehicle.modelMakeKey4_ = expr().model(select(TgVehicleModel.class).where().prop("id").eq().extProp("model").yield().prop("model.makeKey2").modelAsPrimitive()).model();
        //protected static final ExpressionModel makeKey2_ = expr().model(select(TgVehicleMake.class).where().prop("id").eq().extProp("make").yield().prop(KEY).modelAsPrimitive()).model();


        final EntQuery3 actQry = queryCountAll(sources(wo1), conditions1);
        
        final QrySource3BasedOnTable wo = source(WORK_ORDER, wo1);
        final QrySource3BasedOnTable veh = source(VEHICLE, wo1, "vehicle");
        final QrySource3BasedOnTable model = source(MODEL, wo1, "vehicleModel");
        
        final IQrySources3 sources = 
                lj(
                        lj(
                                wo, 
                                veh, 
                                eq(prop("vehicle", wo), prop(ID, veh))
                          ),
                        model,
                        eq(expr(expr(prop("model", veh))), prop(ID, model))                
                  );
        final Conditions3 conditions = or(isNotNull(expr(prop(KEY, model))));
        final EntQuery3 expQry = qryCountAll(sources, conditions);
        assertEquals(expQry, actQry);
    }
    
    @Test
    public void calc_prop_is_correctly_transformed_08() {
        // select(WORK_ORDER).
        // where().anyOfProps("make.key").isNotNull().model();
        
        final EntQuery1 calcPropSubqry1 = (EntQuery1)metadata.get(WORK_ORDER).getProps().get("make").expression.first;
        
        final QrySource1BasedOnPersistentType wo1 = source(WORK_ORDER);
        final QrySource1BasedOnPersistentType veh1 = (QrySource1BasedOnPersistentType) calcPropSubqry1.sources.main;//source(VEHICLE);
        final Conditions1 conditions1 = conditions(isNotNull(prop("make.key")));

        final EntQuery3 actQry = queryCountAll(sources(wo1), conditions1);

        final QrySource3BasedOnTable wo = source(WORK_ORDER, wo1);

        final QrySource3BasedOnTable veh = source(VEHICLE, wo, Integer.toString(veh1.contextId));
        final QrySource3BasedOnTable model = source(MODEL, veh, "model");
        final QrySource3BasedOnTable make = source(MAKE, wo1, "make");
        
        final IQrySources3 subQrySources = 
                ij(
                        veh,  
                        model, 
                        eq(entityProp("model", veh, MODEL), entityProp(ID, model, MODEL))
                  );
        
        final Conditions3 subQryConditions = cond(eq(expr(entityProp(ID, veh, VEHICLE)), expr(entityProp("vehicle", wo, VEHICLE))));
        
        final EntQuery3 expSubQry = subqry(subQrySources, subQryConditions, yields3(yieldSingleExpr("make", model, MAKE)), MAKE);

        final IQrySources3 sources = 
                lj(
                        wo,
                        make,
                        eq(expr(expSubQry), entityProp(ID, make, MAKE))                
                  );
        final Conditions3 conditions = or(isNotNull(expr(stringProp(KEY, make))));
        final EntQuery3 expQry = qryCountAll(sources, conditions);
        
        assertEquals(expQry, actQry);
    }
    
    @Test
    public void calc_prop_is_correctly_transformed_07() {
        // select(WORK_ORDER).
        // where().anyOfProps("make").isNotNull().model();
        // make_ = expr().model(select(TgVehicle.class).where().prop("id").eq().extProp("vehicle").yield().prop("model.make").modelAsEntity(TgVehicleMake.class)).model();
        
        final EntQuery1 calcPropSubqry1 = (EntQuery1)metadata.get(WORK_ORDER).getProps().get("make").expression.first;
        
        final QrySource1BasedOnPersistentType wo1 = source(WORK_ORDER);
        final QrySource1BasedOnPersistentType veh1 = (QrySource1BasedOnPersistentType) calcPropSubqry1.sources.main;//source(VEHICLE);
        final Conditions1 conditions1 = conditions(isNotNull(prop("make")));

        final EntQuery3 actQry = queryCountAll(sources(wo1), conditions1);

        final QrySource3BasedOnTable wo = source(WORK_ORDER, wo1);

        final QrySource3BasedOnTable veh = source(VEHICLE, wo, Integer.toString(veh1.contextId));
        final QrySource3BasedOnTable model = source(MODEL, veh, "model");
        
        final IQrySources3 subQrySources = 
                ij(
                        veh,  
                        model, 
                        eq(entityProp("model", veh, MODEL), entityProp(ID, model, MODEL))
                  );
        
        final Conditions3 subQryConditions = cond(eq(expr(entityProp(ID, veh, VEHICLE)), expr(entityProp("vehicle", wo, VEHICLE))));
        
        final EntQuery3 expSubQry = subqry(subQrySources, subQryConditions, yields3(yieldSingleExpr("make", model, MAKE)), MAKE);

        final IQrySources3 sources = sources(wo);
        final Conditions3 conditions = or(isNotNull(expr(expSubQry)));
        final EntQuery3 expQry = qryCountAll(sources, conditions);
        
        assertEquals(expQry, actQry);
    }

    @Test
    public void calc_prop_is_correctly_transformed_06() {
        // select(WORK_ORDER).
        // where().anyOfProps("makeKey").isNotNull().model();
        // makeKey_ = expr().model(select(TgVehicle.class).where().prop("id").eq().extProp("vehicle").yield().prop("model.make.key").modelAsPrimitive()).model();
        
        final EntQuery1 calcPropSubqry1 = (EntQuery1)metadata.get(WORK_ORDER).getProps().get("makeKey").expression.first;
        
        final QrySource1BasedOnPersistentType wo1 = source(WORK_ORDER);
        final QrySource1BasedOnPersistentType veh1 = (QrySource1BasedOnPersistentType) calcPropSubqry1.sources.main;//source(VEHICLE);
        final Conditions1 conditions1 = conditions(isNotNull(prop("makeKey")));

        final EntQuery3 actQry = queryCountAll(sources(wo1), conditions1);

        final QrySource3BasedOnTable wo = source(WORK_ORDER, wo1);

        final QrySource3BasedOnTable veh = source(VEHICLE, wo1, Integer.toString(veh1.contextId));
        final QrySource3BasedOnTable model = source(MODEL, veh, "model");
        final QrySource3BasedOnTable make = source(MAKE, veh, "model_make");
        
        final IQrySources3 subQrySources = 
                ij(
                        veh,  
                        ij(
                                model, 
                                make, 
                                eq(entityProp("make", model, MAKE), entityProp(ID, make, MAKE))
                          ), 
                        eq(entityProp("model", veh, MODEL), entityProp(ID, model, MODEL))
                  );
        
        final Conditions3 subQryConditions = cond(eq(expr(entityProp(ID, veh, VEHICLE)), expr(entityProp("vehicle", wo, VEHICLE))));
        
        final EntQuery3 expSubQry = subqry(subQrySources, subQryConditions, yields3(yieldSingleStringExpr(KEY, make)), String.class);

        final IQrySources3 sources = sources(wo);
        final Conditions3 conditions = or(isNotNull(expr(expSubQry)));
        final EntQuery3 expQry = qryCountAll(sources, conditions);
        
        assertEquals(expQry, actQry);
        System.out.println(actQry.sql(DbVersion.H2));
    }
    
    @Test
    public void calc_prop_is_correctly_transformed_05() {
        // select(WORK_ORDER).
        // where().anyOfProps("vehicleModel.key").isNotNull().model();
        
        final QrySource1BasedOnPersistentType wo1 = source(WORK_ORDER);
        final Conditions1 conditions1 = conditions(isNotNull(prop("vehicleModel.key")));

        final EntQuery3 actQry = queryCountAll(sources(wo1), conditions1);
        
        final QrySource3BasedOnTable wo = source(WORK_ORDER, wo1);
        final QrySource3BasedOnTable veh = source(VEHICLE, wo1, "vehicle");
        final QrySource3BasedOnTable model = source(MODEL, wo1, "vehicleModel");
        
        final IQrySources3 sources = 
                lj(
                        lj(
                                wo, 
                                veh, 
                                eq(entityProp("vehicle", wo, VEHICLE), entityProp(ID, veh, VEHICLE))
                          ),
                        model,
                        eq(expr(expr(entityProp("model", veh, MODEL))), entityProp(ID, model, MODEL))                
                  );
        final Conditions3 conditions = or(isNotNull(expr(stringProp(KEY, model))));
        final EntQuery3 expQry = qryCountAll(sources, conditions);
        
        assertEquals(expQry, actQry);
        System.out.println(actQry.sql(DbVersion.H2));
    }

    @Test
    public void calc_prop_is_correctly_transformed_04() {
        // select(WORK_ORDER).
        // where().anyOfProps("vehicleModel.makeKey").isNotNull().model();
        
        final QrySource1BasedOnPersistentType wo1 = source(WORK_ORDER);
        final Conditions1 conditions1 = conditions(isNotNull(prop("vehicleModel.makeKey")));

        final EntQuery3 actQry = queryCountAll(sources(wo1), conditions1);
        
        final QrySource3BasedOnTable wo = source(WORK_ORDER, wo1);
        final QrySource3BasedOnTable veh = source(VEHICLE, wo1, "vehicle");
        final QrySource3BasedOnTable model = source(MODEL, wo1, "vehicleModel");
        final QrySource3BasedOnTable make = source(MAKE, wo1, "vehicleModel_make");
        
        final IQrySources3 sources = 
                lj(
                        lj(
                                wo,
                                veh,
                                eq(entityProp("vehicle", wo, VEHICLE), entityProp(ID, veh, VEHICLE))
                          ),  
                        ij(
                                model,
                                make,
                                eq(entityProp("make", model, MAKE), entityProp(ID, make, MAKE))
                          ), 
                        eq(expr(expr(entityProp("model", veh, MODEL))), entityProp(ID, model, MODEL))                
                  );
        final Conditions3 conditions = or(isNotNull(expr(expr(stringProp(KEY, make)))));
        final EntQuery3 expQry = qryCountAll(sources, conditions);
        
        assertEquals(expQry, actQry);
        System.out.println(actQry.sql(DbVersion.H2));
        
    }

    @Test
    public void calc_prop_is_correctly_transformed_03() {
        // select(WORK_ORDER).
        // where().anyOfProps("vehicle.modelMakeKey2").isNotNull().model();
        
        final QrySource1BasedOnPersistentType wo1 = source(WORK_ORDER);
        final Conditions1 conditions1 = conditions(isNotNull(prop("vehicle.modelMakeKey2")));

        final EntQuery3 actQry = queryCountAll(sources(wo1), conditions1);
        
        final QrySource3BasedOnTable wo = source(WORK_ORDER, wo1);
        final QrySource3BasedOnTable veh = source(VEHICLE, wo1, "vehicle");
        final QrySource3BasedOnTable model = source(MODEL, wo1, "vehicle_model");
        final QrySource3BasedOnTable make = source(MAKE, wo1, "vehicle_model_make");
        
        final IQrySources3 sources = 
                lj(
                        wo,
                        ij(
                                veh,  
                                ij(
                                        model,
                                        make,
                                        eq(entityProp("make", model, MAKE), entityProp(ID, make, MAKE))
                                  ), 
                                eq(entityProp("model", veh, MODEL), entityProp(ID, model, MODEL))
                          ),
                        eq(entityProp("vehicle", wo, VEHICLE), entityProp(ID, veh, VEHICLE))
                  );
        final Conditions3 conditions = or(isNotNull(expr(expr(expr(stringProp(KEY, make))))));
        final EntQuery3 expQry = qryCountAll(sources, conditions);
        
        assertEquals(expQry, actQry);
    }

    @Test
    public void calc_prop_is_correctly_transformed_02() {
        // select(WORK_ORDER).
        // where().anyOfProps("vehicle.modelMakeKey").isNotNull().model();
        
        final QrySource1BasedOnPersistentType wo1 = source(WORK_ORDER);
        final Conditions1 conditions1 = conditions(isNotNull(prop("vehicle.modelMakeKey")));

        final EntQuery3 actQry = queryCountAll(sources(wo1), conditions1);
        
        final QrySource3BasedOnTable wo = source(WORK_ORDER, wo1);
        final QrySource3BasedOnTable veh = source(VEHICLE, wo1, "vehicle");
        final QrySource3BasedOnTable model = source(MODEL, wo1, "vehicle_model");
        final QrySource3BasedOnTable make = source(MAKE, wo1, "vehicle_model_make");
        
        final IQrySources3 sources = 
                lj(
                        wo,
                        ij(
                                veh,
                                ij(
                                        model,
                                        make,
                                        eq(entityProp("make", model, MAKE), entityProp(ID, make, MAKE))
                                  ),
                                eq(entityProp("model", veh, MODEL), entityProp(ID, model, MODEL))
                          ),
                        eq(entityProp("vehicle", wo, VEHICLE), entityProp(ID, veh, VEHICLE))
                  );
        final Conditions3 conditions = or(isNotNull(expr(expr(stringProp(KEY, make)))));
        final EntQuery3 expQry = qryCountAll(sources, conditions);
        
        assertEquals(expQry, actQry);
    }
    
    // select(VEHICLE).
    // where().anyOfProps(("model.make.key", "modelMakeKey", "make.key").isNotNull().model();

    // select(VEHICLE).
    // where().anyOfProps("modelMakeKey", "make.key").isNotNull().model();
    
    @Test
    public void veh_calc_prop_is_correctly_transformed_08() {

        // select(VEHICLE).
        // where().anyOfProps("modelMakeKey2", "make.key", "model.make.key").isNotNull().model();
        
        final QrySource1BasedOnPersistentType veh1 = source(VEHICLE);
        final Conditions1 conditions1 = conditions(isNotNull(prop("modelMakeKey2")), or(isNotNull(prop("make.key"))), or(isNotNull(prop("model.make.key"))));
        final EntQuery3 actQry = queryCountAll(sources(veh1), conditions1);
        
        final QrySource3BasedOnTable veh = source(VEHICLE, veh1);
        final QrySource3BasedOnTable model = source(MODEL, veh1, "model");
        final QrySource3BasedOnTable makeA = source(MAKE, veh1, "make");
        final QrySource3BasedOnTable make = source(MAKE, veh1, "model_make");
        
        final IQrySources3 sources =
                ij(
                        lj(
                                veh,
                                makeA,
                                eq(entityProp("make", veh, MAKE), entityProp(ID, makeA, MAKE))
                          ),
                        ij(
                                model,
                                make,
                                eq(entityProp("make", model, MAKE), entityProp(ID, make, MAKE))
                          ),
                        eq(entityProp("model", veh, MODEL), entityProp(ID, model, MODEL))
                  );
        final Conditions3 conditions = or(isNotNull(expr(expr(expr(stringProp(KEY, make))))), isNotNull(expr(stringProp(KEY, makeA))), isNotNull(expr(stringProp(KEY, make))));
        final EntQuery3 expQry = qryCountAll(sources, conditions);
        
        assertEquals(expQry, actQry);   
    }


    @Test
    public void veh_calc_prop_is_correctly_transformed_07() {

        // select(VEHICLE).
        // where().anyOfProps("modelMakeKey2", "make.key").isNotNull().model();
        
        final QrySource1BasedOnPersistentType veh1 = source(VEHICLE);
        final Conditions1 conditions1 = conditions(isNotNull(prop("modelMakeKey2")), or(isNotNull(prop("make.key"))));
        final EntQuery3 actQry = queryCountAll(sources(veh1), conditions1);
        
        final QrySource3BasedOnTable veh = source(VEHICLE, veh1);
        final QrySource3BasedOnTable model = source(MODEL, veh1, "model");
        //final QrySource3BasedOnTable make = source(MAKE, veh1, "model_make");
        final QrySource3BasedOnTable makeA = source(MAKE, veh1, "make");
        final QrySource3BasedOnTable make = source(MAKE, veh1, "make");
        
        final IQrySources3 sources =
                ij(
                        lj(
                                veh,
                                makeA,
                                eq(prop("make", veh), prop(ID, makeA))
                          ),
                        ij(
                                model,
                                make,
                                eq(prop("make", model), prop(ID, make))
                          ),
                        eq(prop("model", veh), prop(ID, model))
                  );
        final Conditions3 conditions = or(isNotNull(expr(expr(expr(prop(KEY, make))))), isNotNull(expr(prop(KEY, make))));
        final EntQuery3 expQry = qryCountAll(sources, conditions);
        
        assertNotEquals(expQry, actQry);
    }
    
    @Test
    public void veh_calc_prop_is_correctly_transformed_06() {

        // select(VEHICLE).
        // where().anyOfProps("modelMakeKey2", "make.key").isNotNull().model();
        
        final QrySource1BasedOnPersistentType veh1 = source(VEHICLE);
        final Conditions1 conditions1 = conditions(isNotNull(prop("modelMakeKey2")), or(isNotNull(prop("make.key"))));
        final EntQuery3 actQry = queryCountAll(sources(veh1), conditions1);
        
        final QrySource3BasedOnTable veh = source(VEHICLE, veh1);
        final QrySource3BasedOnTable model = source(MODEL, veh1, "model");
        final QrySource3BasedOnTable makeA = source(MAKE, veh1, "make");
        final QrySource3BasedOnTable make = source(MAKE, veh1, "model_make");
        
        final IQrySources3 sources =
                ij(
                        lj(
                                veh,
                                makeA,
                                eq(entityProp("make", veh, MAKE), entityProp(ID, makeA, MAKE))
                          ),
                        ij(
                                model,
                                make,
                                eq(entityProp("make", model, MAKE), entityProp(ID, make, MAKE))
                          ),
                        eq(entityProp("model", veh, MODEL), entityProp(ID, model, MODEL))
                  );
        final Conditions3 conditions = or(isNotNull(expr(expr(expr(stringProp(KEY, make))))), isNotNull(expr(stringProp(KEY, makeA))));
        final EntQuery3 expQry = qryCountAll(sources, conditions);
        
        assertEquals(expQry, actQry);
    }
    
    @Test
    public void veh_calc_prop_is_correctly_transformed_05() {
        // select(VEHICLE).
        // where().anyOfProps("model.make.key", "make.key").isNotNull().model();
        
        final QrySource1BasedOnPersistentType veh1 = source(VEHICLE);
        final Conditions1 conditions1 = conditions(isNotNull(prop("model.make.key")), or(isNotNull(prop("make.key"))));

        final EntQuery3 actQry = queryCountAll(sources(veh1), conditions1);
        
        final QrySource3BasedOnTable veh = source(VEHICLE, veh1);
        final QrySource3BasedOnTable model = source(MODEL, veh1, "model");
        final QrySource3BasedOnTable make = source(MAKE, veh1, "model_make");
        final QrySource3BasedOnTable makeA = source(MAKE, veh1, "make");
        
        final IQrySources3 sources = 
                ij(
                        lj(
                                veh,
                                makeA,
                                eq(entityProp("make", veh, MAKE), entityProp(ID, makeA, MAKE))
                          ),
                        ij(
                                model,
                                make,
                                eq(entityProp("make", model, MAKE), entityProp(ID, make, MAKE))
                          ),
                        eq(entityProp("model", veh, MODEL), entityProp(ID, model, MODEL))
                  );
        final Conditions3 conditions = or(isNotNull(expr(stringProp(KEY, make))), isNotNull(expr(stringProp(KEY, makeA))));
        final EntQuery3 expQry = qryCountAll(sources, conditions);
        
        assertEquals(expQry, actQry);    
    }
    
    @Test
    public void veh_calc_prop_is_correctly_transformed_04() {
        // select(VEHICLE).
        // where().anyOfProps("modelMakeKey2", "model.make.key").isNotNull().model();
        
        final QrySource1BasedOnPersistentType veh1 = source(VEHICLE);
        final Conditions1 conditions1 = conditions(isNotNull(prop("modelMakeKey2")), or(isNotNull(prop("model.make.key"))));
        final EntQuery3 actQry = queryCountAll(sources(veh1), conditions1);
        
        final QrySource3BasedOnTable veh = source(VEHICLE, veh1);
        final QrySource3BasedOnTable model = source(MODEL, veh1, "model");
        final QrySource3BasedOnTable make = source(MAKE, veh1, "model_make");
        //final QrySource3BasedOnTable make = source(MAKE, veh1, "make");
        
        final IQrySources3 sources =
                ij(
                        veh,
                        ij(
                                model,
                                make,
                                eq(entityProp("make", model, MAKE), entityProp(ID, make, MAKE))
                          ),
                        eq(entityProp("model", veh, MODEL), entityProp(ID, model, MODEL))
                  );
        final Conditions3 conditions = or(isNotNull(expr(expr(expr(stringProp(KEY, make))))), isNotNull(expr(stringProp(KEY, make))));
        final EntQuery3 expQry = qryCountAll(sources, conditions);
        
        assertEquals(expQry, actQry);
    }
    
    @Test
    public void veh_calc_prop_is_correctly_transformed_03() {
        // select(VEHICLE).
        // where().anyOfProps("modelMakeKey", "modelMakeDesc").isNotNull().model();
        // modelMakeKey_ = expr().prop("model.make.key").model(); 
        // modelMakeDesc_ = expr().prop("model.make.desc").model(); 
        
        final QrySource1BasedOnPersistentType veh1 = source(VEHICLE);
        final Sources1 sources1 = sources(veh1);
        final Conditions1 conditions1 = conditions(isNotNull(prop("modelMakeKey")), or(isNotNull(prop("modelMakeDesc"))));
 
        final EntQuery3 actQry = queryCountAll(sources1, conditions1);
        
        final QrySource3BasedOnTable veh = source(VEHICLE, veh1);
        final QrySource3BasedOnTable model = source(MODEL, veh1, "model");
        final QrySource3BasedOnTable make = source(MAKE, veh1, "model_make");
        
        final IQrySources3 sources = 
                ij(
                        veh,
                        ij(
                                model,
                                make,
                                eq(entityProp("make", model, MAKE), entityProp(ID, make, MAKE))
                          ),
                        eq(entityProp("model", veh, MODEL), entityProp(ID, model, MODEL))
                  );
        final Conditions3 conditions = or(isNotNull(expr(expr(stringProp(KEY, make)))), isNotNull(expr(expr(stringProp("desc", make)))));
        final EntQuery3 expQry = qryCountAll(sources, conditions);
        
        assertEquals(expQry, actQry);
    }
    
    @Test
    public void veh_calc_prop_is_correctly_transformed_02() {
        // select(VEHICLE).
        // where().anyOfProps("modelKey", "modelDesc").isNotNull().model();
        // modelKey_ = expr().prop("model.key").model(); 
        // modelDesc_ = expr().prop("model.desc").model(); 
        
        final QrySource1BasedOnPersistentType veh1 = source(VEHICLE);
        final Conditions1 conditions1 = conditions(isNotNull(prop("modelKey")), or(isNotNull(prop("modelDesc"))));
        final EntQuery3 actQry = queryCountAll(sources(veh1), conditions1);
        
        final QrySource3BasedOnTable veh = source(VEHICLE, veh1);
        final QrySource3BasedOnTable model = source(MODEL, veh1, "model");
        
        final IQrySources3 sources = 
                ij(
                        veh,
                        model,
                        eq(entityProp("model", veh, MODEL), entityProp(ID, model, MODEL))
                  );
        final Conditions3 conditions = or(isNotNull(expr(expr(stringProp(KEY, model)))), isNotNull(expr(expr(stringProp("desc", model)))));
        final EntQuery3 expQry = qryCountAll(sources, conditions);
        
        assertEquals(expQry, actQry);
    }

    @Test
    public void veh_calc_prop_is_correctly_transformed_01() {
        // select(VEHICLE).
        // where().anyOfProps("modelMakeKey").isNotNull().model();
        
        final QrySource1BasedOnPersistentType veh1 = source(VEHICLE);
        final Conditions1 conditions1 = conditions(isNotNull(prop("modelMakeKey")));
        final EntQuery3 actQry = queryCountAll(sources(veh1), conditions1);
        
        final QrySource3BasedOnTable veh = source(VEHICLE, veh1);
        final QrySource3BasedOnTable model = source(MODEL, veh1, "model");
        final QrySource3BasedOnTable make = source(MAKE, veh1, "model_make");
        
        final IQrySources3 sources = 
                ij(
                        veh,
                        ij(
                                model,
                                make,
                                eq(entityProp("make", model, MAKE), entityProp(ID, make, MAKE))
                          ),
                        eq(entityProp("model", veh, MODEL), entityProp(ID, model, MODEL))
                  );
        final Conditions3 conditions = or(isNotNull(expr(expr(stringProp(KEY, make)))));
        final EntQuery3 expQry = qryCountAll(sources, conditions);
        
        assertEquals(expQry, actQry);
    }


    @Test
    public void veh_model_calc_prop_is_correctly_transformed_05() {
        // select(MODEL).
        // where().anyOfProps("makeKey", "makeKey2", "make.key").isNotNull().model();

        final QrySource1BasedOnPersistentType model1 = source(MODEL);
        final EntQuery1 calcPropSubqry1 = (EntQuery1)metadata.get(MODEL).getProps().get("makeKey2").expression.first;
        final QrySource1BasedOnPersistentType make1 = (QrySource1BasedOnPersistentType) calcPropSubqry1.sources.main;//source(MAKE);
        final Conditions1 conditions1 = conditions(isNotNull(prop("makeKey")), or(isNotNull(prop("makeKey2"))), or(isNotNull(prop("make.key"))));

        final EntQuery3 actQry = queryCountAll(sources(model1), conditions1);
        
        final QrySource3BasedOnTable model = source(MODEL, model1);
        final QrySource3BasedOnTable make = source(MAKE, model1, "make");
        
        final QrySource3BasedOnTable subQryMake = source(MAKE, model1, Integer.toString(make1.contextId));
        
        final IQrySources3 subQrySources = sources(subQryMake);
        
        final Conditions3 subQryConditions = cond(eq(expr(entityProp(ID, subQryMake, MAKE)), expr(entityProp("make", model, MAKE))));
        
        final EntQuery3 expSubQry = subqry(subQrySources, subQryConditions, yields3(yieldSingleStringExpr(KEY, subQryMake)), String.class);

        
        final IQrySources3 sources = 
                ij(
                        model,
                        make,
                        eq(entityProp("make", model, MAKE), entityProp(ID, make, MAKE))
                  );
        final Conditions3 conditions = or(isNotNull(expr(expr(stringProp(KEY, make)))), isNotNull(expr(expSubQry)), isNotNull(expr(stringProp(KEY, make))));
        final EntQuery3 expQry = qryCountAll(sources, conditions);
        
        assertEquals(expQry, actQry);
    }

    @Test
    public void veh_model_calc_prop_is_correctly_transformed_04() {
        // select(MODEL).
        // where().anyOfProps("makeKey", "makeKey2").isNotNull().model();

        final QrySource1BasedOnPersistentType model1 = source(MODEL);
        final EntQuery1 calcPropSubqry1 = (EntQuery1)metadata.get(MODEL).getProps().get("makeKey2").expression.first;
        final QrySource1BasedOnPersistentType make1 = (QrySource1BasedOnPersistentType) calcPropSubqry1.sources.main;//source(MAKE);
        final Conditions1 conditions1 = conditions(isNotNull(prop("makeKey")), or(isNotNull(prop("makeKey2"))));

        final EntQuery3 actQry = queryCountAll(sources(model1), conditions1);
        
        final QrySource3BasedOnTable model = source(MODEL, model1);
        final QrySource3BasedOnTable make = source(MAKE, model1, "make");
        
        final QrySource3BasedOnTable subQryMake = source(MAKE, model1, Integer.toString(make1.contextId));
        
        final IQrySources3 subQrySources = sources(subQryMake);
        
        final Conditions3 subQryConditions = cond(eq(expr(entityProp(ID, subQryMake, MAKE)), expr(entityProp("make", model, MAKE))));
        
        final EntQuery3 expSubQry = subqry(subQrySources, subQryConditions, yields3(yieldSingleStringExpr(KEY, subQryMake)), String.class);

        
        final IQrySources3 sources = 
                ij(
                        model,
                        make,
                        eq(entityProp("make", model, MAKE), entityProp(ID, make, MAKE))
                  );
        final Conditions3 conditions = or(isNotNull(expr(expr(stringProp(KEY, make)))), isNotNull(expr(expSubQry)));
        final EntQuery3 expQry = qryCountAll(sources, conditions);
        
        assertEquals(expQry, actQry);
    }

    @Test
    public void veh_model_calc_prop_is_correctly_transformed_03() {
        // select(MODEL).
        // where().anyOfProps("makeKey, make.key").isNotNull().model();
        
        final QrySource1BasedOnPersistentType model1 = source(MODEL);
        final Conditions1 conditions1 = conditions(isNotNull(prop("makeKey")), or(isNotNull(prop("make.key"))));

        final EntQuery3 actQry = queryCountAll(sources(model1), conditions1);
        
        final QrySource3BasedOnTable model = source(MODEL, model1);
        final QrySource3BasedOnTable make = source(MAKE, model1, "make");
        
        final IQrySources3 sources = 
                ij(
                        model,
                        make,
                        eq(entityProp("make", model, MAKE), entityProp(ID, make, MAKE))
                  );
        final Conditions3 conditions = or(isNotNull(expr(expr(stringProp(KEY, make)))), isNotNull(expr(stringProp(KEY, make))));
        final EntQuery3 expQry = qryCountAll(sources, conditions);
        
        assertEquals(expQry, actQry);
    }
    
    @Test
    public void veh_model_calc_prop_is_correctly_transformed_02() {
        // select(MODEL).
        // where().anyOfProps("makeKey2").isNotNull().model();

        // makeKey2_ = expr().model(select(TeVehicleMake.class).where().prop("id").eq().extProp("make").yield().prop(KEY).modelAsPrimitive()).model();
        
        final EntQuery1 calcPropSubqry1 = (EntQuery1)metadata.get(MODEL).getProps().get("makeKey2").expression.first;
        
        final QrySource1BasedOnPersistentType model1 = source(MODEL);
        final QrySource1BasedOnPersistentType make1 = (QrySource1BasedOnPersistentType) calcPropSubqry1.sources.main;//source(MAKE);
        final Conditions1 conditions1 = conditions(isNotNull(prop("makeKey2")));

        final EntQuery3 actQry = queryCountAll(sources(model1), conditions1);

        final QrySource3BasedOnTable model = source(MODEL, model1);

        final QrySource3BasedOnTable subQryMake = source(MAKE, model1, Integer.toString(make1.contextId));
        
        final IQrySources3 subQrySources = sources(subQryMake);
        
        final Conditions3 subQryConditions = cond(eq(expr(entityProp(ID, subQryMake, MAKE)), expr(entityProp("make", model, MAKE))));
        
        final EntQuery3 expSubQry = subqry(subQrySources, subQryConditions, yields3(yieldSingleStringExpr(KEY, subQryMake)), String.class);

        final IQrySources3 sources = sources(model);
        final Conditions3 conditions = or(isNotNull(expr(expSubQry)));
        final EntQuery3 expQry = qryCountAll(sources, conditions);
        
        assertEquals(expQry, actQry);
    }

    @Test
    public void veh_model_calc_prop_is_correctly_transformed_01() {
        // select(MODEL).
        // where().anyOfProps("makeKey").isNotNull().model();
        
        final QrySource1BasedOnPersistentType model1 = source(MODEL);
        final Conditions1 conditions1 = conditions(isNotNull(prop("makeKey")));

        final EntQuery3 actQry = queryCountAll(sources(model1), conditions1);
        
        final QrySource3BasedOnTable model = source(MODEL, model1);
        final QrySource3BasedOnTable make = source(MAKE, model1, "make");
        
        final IQrySources3 sources = 
                ij(
                        model,
                        make,
                        eq(entityProp("make", model, MAKE), entityProp(ID, make, MAKE))
                  );
        final Conditions3 conditions = or(isNotNull(expr(expr(stringProp(KEY, make)))));
        final EntQuery3 expQry = qryCountAll(sources, conditions);
        
        assertEquals(expQry, actQry);
    }

    @Test
    public void dot_notated_props_are_correctly_transformed_01() {
        // select(VEHICLE).
        // where().anyOfProps(KEY, "replacedBy.key").isNotNull().model();
        
        final QrySource1BasedOnPersistentType veh1 = source(VEHICLE);
        final Conditions1 conditions1 = conditions(isNotNull(prop(KEY)), //
                or(isNotNull(prop("replacedBy.key"))) //
        );

        final EntQuery3 actQry = queryCountAll(sources(veh1), conditions1);
        
        final QrySource3BasedOnTable veh = source(VEHICLE, veh1);
        final QrySource3BasedOnTable repVeh = source(VEHICLE, veh1, "replacedBy");
        
        final IQrySources3 sources = 
                lj(
                        veh,
                        repVeh,
                        eq(entityProp("replacedBy", veh, VEHICLE), entityProp(ID, repVeh, VEHICLE))
                  );
        final Conditions3 conditions = or(isNotNull(expr(stringProp(KEY, veh))), isNotNull(expr(stringProp(KEY, repVeh))));
        final EntQuery3 expQry = qryCountAll(sources, conditions);
        
        assertEquals(expQry, actQry);
    }

    @Test
    public void dot_notated_props_are_correctly_transformed_02() {
        // select(VEHICLE).
        // where().anyOfProps("initDate", "station.name", "station.parent.name", "replacedBy.initDate").isNotNull().model();
        
        final QrySource1BasedOnPersistentType veh1 = source(VEHICLE);
        final Conditions1 conditions1 = conditions(isNotNull(prop("initDate")), //
                or(isNotNull(prop("station.name"))), //
                or(isNotNull(prop("station.parent.name"))), //
                or(isNotNull(prop("replacedBy.initDate"))) //
        );

        final EntQuery3 actQry = queryCountAll(sources(veh1), conditions1);
        
        final QrySource3BasedOnTable veh = source(VEHICLE, veh1);
        final QrySource3BasedOnTable repVeh = source(VEHICLE, veh1, "replacedBy");
        final QrySource3BasedOnTable org5 = source(ORG5, veh1, "station");
        final QrySource3BasedOnTable org4 = source(ORG4, veh1, "station_parent");
        
        final IQrySources3 sources = 
                lj(
                        lj(
                                veh,
                                repVeh,
                                eq(entityProp("replacedBy", veh, VEHICLE), entityProp(ID, repVeh, VEHICLE))
                          ),
                        ij(
                                org5,
                                org4,
                                eq(entityProp("parent", org5, ORG4), entityProp(ID, org4, ORG4))
                          ),
                        eq(entityProp("station", veh, ORG5), entityProp(ID, org5, ORG5))
                  );
        final Conditions3 conditions = or(isNotNull(expr(dateProp("initDate", veh))), isNotNull(expr(stringProp("name", org5))), isNotNull(expr(stringProp("name", org4))), isNotNull(expr(dateProp("initDate", repVeh))));
        final EntQuery3 expQry = qryCountAll(sources, conditions);
        
        assertEquals(expQry, actQry);
    }
    
    @Test
    public void dot_notated_props_are_correctly_transformed_03() {
        // select(VEHICLE).as("veh").join(ORG5).as("ou5e").on().prop("veh.station").eq().prop("ou5e.id").
        // where().anyOfProps("veh.key", "veh.replacedBy.key").isNotNull().model();
        
        final QrySource1BasedOnPersistentType veh1 = source(VEHICLE, "veh");
        final QrySource1BasedOnPersistentType ou5e1 = source(ORG5, "ou5e");
        final Sources1 sources1 = sources(veh1, ij(ou5e1, conditions(eq(prop("veh.station"), prop("ou5e.id")))));
        
        final Conditions1 conditions1 = conditions(isNotNull(prop("veh.key")), //
                or(isNotNull(prop("veh.replacedBy.key"))) //
        );

        final EntQuery3 actQry = queryCountAll(sources1, conditions1);

        final QrySource3BasedOnTable veh = source(VEHICLE, veh1);
        final QrySource3BasedOnTable repVeh = source(VEHICLE, veh1, "replacedBy");
        final QrySource3BasedOnTable ou5e = source(ORG5, ou5e1);

        final IQrySources3 sources = 
                ij(
                        lj(
                                veh,
                                repVeh,
                                eq(entityProp("replacedBy", veh, VEHICLE), entityProp(ID, repVeh, VEHICLE))
                          ),
                        ou5e,
                        eq(expr(entityProp("station", veh, ORG5)), expr(entityProp(ID, ou5e, ORG5)))
                  );
        final Conditions3 conditions = or(isNotNull(expr(stringProp(KEY, veh))), isNotNull(expr(stringProp(KEY, repVeh))));
        final EntQuery3 expQry = qryCountAll(sources, conditions);

        assertEquals(expQry, actQry);
    }
    
    @Test
    public void dot_notated_props_are_correctly_transformed_04() {
        // select(VEHICLE).as("veh").
        // join(ORG5).as("ou5e").on().prop("station").eq().prop("ou5e.id").
        // where().anyOfProps("veh.key", "replacedBy.key", "initDate", "station.name", "station.parent.name", "ou5e.parent.name").isNotNull().model();
        
        final QrySource1BasedOnPersistentType veh1 = source(VEHICLE, "veh");
        final QrySource1BasedOnPersistentType ou5e1 = source(ORG5, "ou5e");
        final Sources1 sources1 = sources(veh1, ij(ou5e1, conditions(eq(prop("station"), prop("ou5e.id")))));
        
        final Conditions1 conditions1 = conditions(isNotNull(prop("veh.key")), //
                or(isNotNull(prop("replacedBy.key"))), //
                or(isNotNull(prop("initDate"))), //
                or(isNotNull(prop("station.name"))), //
                or(isNotNull(prop("station.parent.name"))), //
                or(isNotNull(prop("ou5e.parent.name"))) //
        );

        final EntQuery3 actQry = queryCountAll(sources1, conditions1);

        final QrySource3BasedOnTable veh = source(VEHICLE, veh1);
        final QrySource3BasedOnTable repVeh = source(VEHICLE, veh1, "replacedBy");
        final QrySource3BasedOnTable ou5e = source(ORG5, ou5e1);
        final QrySource3BasedOnTable ou5eou4 = source(ORG4, ou5e1, "parent");
        final QrySource3BasedOnTable ou5 = source(ORG5, veh1, "station");
        final QrySource3BasedOnTable ou4 = source(ORG4, veh1, "station_parent");

        final IQrySources3 sources = 
                ij(
                        lj(
                                lj(
                                        veh,
                                        repVeh,
                                        eq(entityProp("replacedBy", veh, VEHICLE), entityProp(ID, repVeh, VEHICLE))
                                  ),
                                ij(
                                        ou5,
                                        ou4,
                                        eq(entityProp("parent", ou5, ORG4), entityProp(ID, ou4, ORG4))
                                  ),
                                eq(entityProp("station", veh, ORG5), entityProp(ID, ou5, ORG5))
                          ),
                        ij(
                                ou5e,
                                ou5eou4,
                                eq(entityProp("parent", ou5e, ORG4), entityProp(ID, ou5eou4, ORG4))
                          ),
                        eq(expr(entityProp("station", veh, ORG5)), expr(entityProp(ID, ou5e, ORG5)))
                  );
        final Conditions3 conditions = or(
                isNotNull(expr(stringProp(KEY, veh))),
                isNotNull(expr(stringProp(KEY, repVeh))), 
                isNotNull(expr(dateProp("initDate", veh))), 
                isNotNull(expr(stringProp("name", ou5))), 
                isNotNull(expr(stringProp("name", ou4))),
                isNotNull(expr(stringProp("name", ou5eou4))));
        final EntQuery3 expQry = qryCountAll(sources, conditions);

        assertEquals(expQry, actQry);
    }

    @Test
    public void dot_notated_props_are_correctly_transformed_05() {
    
        // select(VEHICLE).
        // join(ORG2).as("ou2e").on().prop("station.parent.parent.parent").eq().prop("ou2e.id").
        // where().anyOfProps("initDate", "replacedBy.initDate", "station.name", "station.parent.name", "ou2e.parent.key").isNotNull().model();
        
        final QrySource1BasedOnPersistentType veh1 = source(VEHICLE);
        final QrySource1BasedOnPersistentType ou2e1 = source(ORG2, "ou2e");
        final Sources1 sources1 = sources(veh1, ij(ou2e1, conditions(eq(prop("station.parent.parent.parent"), prop("ou2e.id")))));
        
        final Conditions1 conditions1 = conditions(isNotNull(prop("initDate")), //
                or(isNotNull(prop("replacedBy.initDate"))), //
                or(isNotNull(prop("station.name"))), //
                or(isNotNull(prop("station.parent.name"))), //
                or(isNotNull(prop("ou2e.parent.key"))) //
        );
        
        final EntQuery3 actQry = queryCountAll(sources1, conditions1);
        
        
        final QrySource3BasedOnTable veh = source(VEHICLE, veh1);
        final QrySource3BasedOnTable repVeh = source(VEHICLE, veh1, "replacedBy");
        final QrySource3BasedOnTable ou5 = source(ORG5, veh1, "station");
        final QrySource3BasedOnTable ou4 = source(ORG4, veh1, "station_parent");
        final QrySource3BasedOnTable ou3 = source(ORG3, veh1, "station_parent_parent");
        final QrySource3BasedOnTable ou2e = source(ORG2, ou2e1);
        final QrySource3BasedOnTable ou2eou1 = source(ORG1, ou2e1, "parent");

        final IQrySources3 sources = 
                ij(
                        lj(
                                lj(
                                        veh,
                                        repVeh,
                                        eq(entityProp("replacedBy", veh, VEHICLE), entityProp(ID, repVeh, VEHICLE))
                                  ),
                                ij(
                                        ou5,
                                        ij(
                                                ou4,
                                                ou3,
                                                eq(entityProp("parent", ou4, ORG3), entityProp(ID, ou3, ORG3))
                                          ),
                                        eq(entityProp("parent", ou5, ORG4), entityProp(ID, ou4, ORG4))
                                  ),
                                eq(entityProp("station", veh, ORG5), entityProp(ID, ou5, ORG5))
                          ),
                        ij(
                                ou2e,
                                ou2eou1,
                                eq(entityProp("parent", ou2e, ORG1), entityProp(ID, ou2eou1, ORG1))
                          ),
                        eq(expr(entityProp("parent", ou3, ORG2)), expr(entityProp(ID, ou2e, ORG2)))
                  );
        final Conditions3 conditions = or(
                isNotNull(expr(dateProp("initDate", veh))),
                isNotNull(expr(dateProp("initDate", repVeh))), 
                isNotNull(expr(stringProp("name", ou5))), 
                isNotNull(expr(stringProp("name", ou4))),
                isNotNull(expr(stringProp(KEY, ou2eou1))));
        final EntQuery3 expQry = qryCountAll(sources, conditions);

        assertEquals(expQry, actQry);
    }
 }