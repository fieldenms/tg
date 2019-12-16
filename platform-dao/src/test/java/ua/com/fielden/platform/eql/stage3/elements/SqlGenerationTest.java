package ua.com.fielden.platform.eql.stage3.elements;


import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.query.DbVersion.H2;

import org.junit.Ignore;
import org.junit.Test;

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

    @Test
    @Ignore
    public void calc_prop_is_correctly_transformed_09() {
        // select(WORK_ORDER).
        // where().anyOfProps("vehicleModel.key").isNotNull().model();
        
        final QrySource1BasedOnPersistentType wo1 = source(WORK_ORDER);
        final Sources1 sources1 = sources(wo1);
        final Conditions1 conditions1 = conditions(isNotNull(prop("makeKey2")));

        final EntQuery3 actQry = query(sources1, conditions1, WORK_ORDER);
        
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
        final Conditions3 conditions = or(isNotNull(expr(prop("key", model))));
        final EntQuery3 expQry = qry(sources, conditions);
        System.out.println(actQry.sql(H2));
        assertEquals(expQry, actQry);
    }
    
    @Test
    public void calc_prop_is_correctly_transformed_08() {
        // select(WORK_ORDER).
        // where().anyOfProps("make.key").isNotNull().model();
        
        final EntQuery1 calcPropSubqry1 = (EntQuery1)metadata.get(WORK_ORDER).getProps().get("make").expression.first;
        
        final QrySource1BasedOnPersistentType wo1 = source(WORK_ORDER);
        final QrySource1BasedOnPersistentType veh1 = (QrySource1BasedOnPersistentType) calcPropSubqry1.sources.main;//source(VEHICLE);
        final Sources1 sources1 = sources(wo1);
        final Conditions1 conditions1 = conditions(isNotNull(prop("make.key")));

        final EntQuery3 actQry = query(sources1, conditions1, WORK_ORDER);

        final QrySource3BasedOnTable wo = source(WORK_ORDER, wo1);

        final QrySource3BasedOnTable veh = source(VEHICLE, veh1);
        final QrySource3BasedOnTable model = source(MODEL, veh1, "model");
        final QrySource3BasedOnTable make = source(MAKE, wo1, "make");
        
        final IQrySources3 subQrySources = 
                ij(
                        veh,  
                        model, 
                        eq(prop("model", veh), prop(ID, model))
                  );
        
        final Conditions3 subQryConditions = cond(eq(expr(prop(ID, veh)), expr(prop("vehicle", wo))));
        
        final EntQuery3 expSubQry = subqry(subQrySources, subQryConditions, yields(yieldSingleExpr("make", model)));

        final IQrySources3 sources = 
                lj(
                        wo,
                        make,
                        eq(expr(expSubQry), prop(ID, make))                
                  );
        final Conditions3 conditions = or(isNotNull(expr(prop("key", make))));
        final EntQuery3 expQry = qry(sources, conditions);
        
        assertEquals(expQry, actQry);
    }
    
    @Test
    public void calc_prop_is_correctly_transformed_07() {
        // select(WORK_ORDER).
        // where().anyOfProps("make").isNotNull().model();
        
        final EntQuery1 calcPropSubqry1 = (EntQuery1)metadata.get(WORK_ORDER).getProps().get("make").expression.first;
        
        final QrySource1BasedOnPersistentType wo1 = source(WORK_ORDER);
        final QrySource1BasedOnPersistentType veh1 = (QrySource1BasedOnPersistentType) calcPropSubqry1.sources.main;//source(VEHICLE);
        final Sources1 sources1 = sources(wo1);
        final Conditions1 conditions1 = conditions(isNotNull(prop("make")));

        final EntQuery3 actQry = query(sources1, conditions1, WORK_ORDER);

        final QrySource3BasedOnTable wo = source(WORK_ORDER, wo1);

        final QrySource3BasedOnTable veh = source(VEHICLE, veh1);
        final QrySource3BasedOnTable model = source(MODEL, veh1, "model");
        
        final IQrySources3 subQrySources = 
                ij(
                        veh,  
                        model, 
                        eq(prop("model", veh), prop(ID, model))
                  );
        
        final Conditions3 subQryConditions = cond(eq(expr(prop(ID, veh)), expr(prop("vehicle", wo))));
        
        final EntQuery3 expSubQry = subqry(subQrySources, subQryConditions, yields(yieldSingleExpr("make", model)));

        final IQrySources3 sources = sources(wo);
        final Conditions3 conditions = or(isNotNull(expr(expSubQry)));
        final EntQuery3 expQry = qry(sources, conditions);
        
        assertEquals(expQry, actQry);
    }

    @Test
    public void calc_prop_is_correctly_transformed_06() {
        // select(WORK_ORDER).
        // where().anyOfProps("makeKey").isNotNull().model();
        
        final EntQuery1 calcPropSubqry1 = (EntQuery1)metadata.get(WORK_ORDER).getProps().get("makeKey").expression.first;
        
        final QrySource1BasedOnPersistentType wo1 = source(WORK_ORDER);
        final QrySource1BasedOnPersistentType veh1 = (QrySource1BasedOnPersistentType) calcPropSubqry1.sources.main;//source(VEHICLE);
        final Sources1 sources1 = sources(wo1);
        final Conditions1 conditions1 = conditions(isNotNull(prop("makeKey")));

        final EntQuery3 actQry = query(sources1, conditions1, WORK_ORDER);

        final QrySource3BasedOnTable wo = source(WORK_ORDER, wo1);

        final QrySource3BasedOnTable veh = source(VEHICLE, veh1);
        final QrySource3BasedOnTable model = source(MODEL, veh1, "model");
        final QrySource3BasedOnTable make = source(MAKE, veh1, "model_make");
        
        final IQrySources3 subQrySources = 
                ij(
                        veh,  
                        ij(
                                model, 
                                make, 
                                eq(prop("make", model), prop(ID, make))
                          ), 
                        eq(prop("model", veh), prop(ID, model))
                  );
        
        final Conditions3 subQryConditions = cond(eq(expr(prop(ID, veh)), expr(prop("vehicle", wo))));
        
        final EntQuery3 expSubQry = subqry(subQrySources, subQryConditions, yields(yieldSingleExpr("key", make)));

        final IQrySources3 sources = sources(wo);
        final Conditions3 conditions = or(isNotNull(expr(expSubQry)));
        final EntQuery3 expQry = qry(sources, conditions);
        
        assertEquals(expQry, actQry);
    }
    
    @Test
    public void calc_prop_is_correctly_transformed_05() {
        // select(WORK_ORDER).
        // where().anyOfProps("vehicleModel.key").isNotNull().model();
        
        final QrySource1BasedOnPersistentType wo1 = source(WORK_ORDER);
        final Sources1 sources1 = sources(wo1);
        final Conditions1 conditions1 = conditions(isNotNull(prop("vehicleModel.key")));

        final EntQuery3 actQry = query(sources1, conditions1, WORK_ORDER);
        
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
        final Conditions3 conditions = or(isNotNull(expr(prop("key", model))));
        final EntQuery3 expQry = qry(sources, conditions);
        
        assertEquals(expQry, actQry);
    }

    @Test
    public void calc_prop_is_correctly_transformed_04() {
        // select(WORK_ORDER).
        // where().anyOfProps("vehicleModel.makeKey").isNotNull().model();
        
        final QrySource1BasedOnPersistentType wo1 = source(WORK_ORDER);
        final Sources1 sources1 = sources(wo1);
        final Conditions1 conditions1 = conditions(isNotNull(prop("vehicleModel.makeKey")));

        final EntQuery3 actQry = query(sources1, conditions1, WORK_ORDER);
        
        final QrySource3BasedOnTable wo = source(WORK_ORDER, wo1);
        final QrySource3BasedOnTable veh = source(VEHICLE, wo1, "vehicle");
        final QrySource3BasedOnTable model = source(MODEL, wo1, "vehicleModel");
        final QrySource3BasedOnTable make = source(MAKE, wo1, "make");
        
        final IQrySources3 sources = 
                lj(
                        lj(
                                wo,
                                veh,
                                eq(prop("vehicle", wo), prop(ID, veh))
                          ),  
                        ij(
                                model,
                                make,
                                eq(prop("make", model), prop(ID, make))
                          ), 
                        eq(expr(expr(prop("model", veh))), prop(ID, model))                
                  );
        final Conditions3 conditions = or(isNotNull(expr(expr(prop("key", make)))));
        final EntQuery3 expQry = qry(sources, conditions);
        
        assertEquals(expQry, actQry);
    }

    @Test
    public void calc_prop_is_correctly_transformed_03() {
        // select(WORK_ORDER).
        // where().anyOfProps("vehicle.makeKey2").isNotNull().model();
        
        final QrySource1BasedOnPersistentType wo1 = source(WORK_ORDER);
        final Sources1 sources1 = sources(wo1);
        final Conditions1 conditions1 = conditions(isNotNull(prop("vehicle.makeKey2")));

        final EntQuery3 actQry = query(sources1, conditions1, WORK_ORDER);
        
        final QrySource3BasedOnTable wo = source(WORK_ORDER, wo1);
        final QrySource3BasedOnTable veh = source(VEHICLE, wo1, "vehicle");
        final QrySource3BasedOnTable model = source(MODEL, wo1, "model");
        final QrySource3BasedOnTable make = source(MAKE, wo1, "make");
        
        final IQrySources3 sources = 
                lj(
                        wo,
                        ij(
                                veh,  
                                ij(
                                        model,
                                        make,
                                        eq(prop("make", model), prop(ID, make))
                                  ), 
                                eq(prop("model", veh), prop(ID, model))
                          ),
                        eq(prop("vehicle", wo), prop(ID, veh))
                  );
        final Conditions3 conditions = or(isNotNull(expr(expr(expr(prop("key", make))))));
        final EntQuery3 expQry = qry(sources, conditions);
        
        assertEquals(expQry, actQry);
    }

    @Test
    public void calc_prop_is_correctly_transformed_02() {
        // select(WORK_ORDER).
        // where().anyOfProps("vehicle.makeKey").isNotNull().model();
        
        final QrySource1BasedOnPersistentType wo1 = source(WORK_ORDER);
        final Sources1 sources1 = sources(wo1);
        final Conditions1 conditions1 = conditions(isNotNull(prop("vehicle.makeKey")));

        final EntQuery3 actQry = query(sources1, conditions1, WORK_ORDER);
        
        final QrySource3BasedOnTable wo = source(WORK_ORDER, wo1);
        final QrySource3BasedOnTable veh = source(VEHICLE, wo1, "vehicle");
        final QrySource3BasedOnTable model = source(MODEL, wo1, "model");
        final QrySource3BasedOnTable make = source(MAKE, wo1, "model_make");
        
        final IQrySources3 sources = 
                lj(
                        wo,
                        ij(
                                veh,
                                ij(
                                        model,
                                        make,
                                        eq(prop("make", model), prop(ID, make))
                                  ),
                                eq(prop("model", veh), prop(ID, model))
                          ),
                        eq(prop("vehicle", wo), prop(ID, veh))
                  );
        final Conditions3 conditions = or(isNotNull(expr(expr(prop("key", make)))));
        final EntQuery3 expQry = qry(sources, conditions);
        
        assertEquals(expQry, actQry);
    }
    
    @Test
    public void calc_prop_is_correctly_transformed_01() {
        // select(VEHICLE).
        // where().anyOfProps("makeKey").isNotNull().model();
        
        final QrySource1BasedOnPersistentType veh1 = source(VEHICLE);
        final Sources1 sources1 = sources(veh1);
        final Conditions1 conditions1 = conditions(isNotNull(prop("makeKey")));

        final EntQuery3 actQry = query(sources1, conditions1, VEHICLE);
        
        final QrySource3BasedOnTable veh = source(VEHICLE, veh1);
        final QrySource3BasedOnTable model = source(MODEL, veh1, "model");
        final QrySource3BasedOnTable make = source(MAKE, veh1, "model_make");
        
        final IQrySources3 sources = 
                ij(
                        veh,
                        ij(
                                model,
                                make,
                                eq(prop("make", model), prop(ID, make))
                          ),
                        eq(prop("model", veh), prop(ID, model))
                  );
        final Conditions3 conditions = or(isNotNull(expr(expr(prop("key", make)))));
        final EntQuery3 expQry = qry(sources, conditions);
        
        assertEquals(expQry, actQry);
    }

    @Test
    public void dot_notated_props_are_correctly_transformed_01() {
        // select(VEHICLE).
        // where().anyOfProps("key", "replacedBy.key").isNotNull().model();
        
        final QrySource1BasedOnPersistentType veh1 = source(VEHICLE);
        final Sources1 sources1 = sources(veh1);
        final Conditions1 conditions1 = conditions(isNotNull(prop("key")), //
                or(isNotNull(prop("replacedBy.key"))) //
        );

        final EntQuery3 actQry = query(sources1, conditions1, VEHICLE);
        
        final QrySource3BasedOnTable veh = source(VEHICLE, veh1);
        final QrySource3BasedOnTable repVeh = source(VEHICLE, veh1, "replacedBy");
        
        final IQrySources3 sources = 
                lj(
                        veh,
                        repVeh,
                        eq(prop("replacedBy", veh), prop(ID, repVeh))
                  );
        final Conditions3 conditions = or(isNotNull(expr(prop("key", veh))), isNotNull(expr(prop("key", repVeh))));
        final EntQuery3 expQry = qry(sources, conditions);
        
        assertEquals(expQry, actQry);
    }

    @Test
    public void dot_notated_props_are_correctly_transformed_02() {
        // select(VEHICLE).
        // where().anyOfProps("initDate", "station.name", "station.parent.name", "replacedBy.initDate").isNotNull().model();
        
        final QrySource1BasedOnPersistentType veh1 = source(VEHICLE);
        final Sources1 sources1 = sources(veh1);
        final Conditions1 conditions1 = conditions(isNotNull(prop("initDate")), //
                or(isNotNull(prop("station.name"))), //
                or(isNotNull(prop("station.parent.name"))), //
                or(isNotNull(prop("replacedBy.initDate"))) //
        );

        final EntQuery3 actQry = query(sources1, conditions1, VEHICLE);
        
        final QrySource3BasedOnTable veh = source(VEHICLE, veh1);
        final QrySource3BasedOnTable repVeh = source(VEHICLE, veh1, "replacedBy");
        final QrySource3BasedOnTable org5 = source(ORG5, veh1, "station");
        final QrySource3BasedOnTable org4 = source(ORG4, veh1, "station_parent");
        
        final IQrySources3 sources = 
                lj(
                        lj(
                                veh,
                                repVeh,
                                eq(prop("replacedBy", veh), prop(ID, repVeh))
                          ),
                        ij(
                                org5,
                                org4,
                                eq(prop("parent", org5), prop(ID, org4))
                          ),
                        eq(prop("station", veh), prop(ID, org5))
                  );
        final Conditions3 conditions = or(isNotNull(expr(prop("initDate", veh))), isNotNull(expr(prop("name", org5))), isNotNull(expr(prop("name", org4))), isNotNull(expr(prop("initDate", repVeh))));
        final EntQuery3 expQry = qry(sources, conditions);
        
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

        final EntQuery3 actQry = query(sources1, conditions1, VEHICLE);

        final QrySource3BasedOnTable veh = source(VEHICLE, veh1);
        final QrySource3BasedOnTable repVeh = source(VEHICLE, veh1, "replacedBy");
        final QrySource3BasedOnTable ou5e = source(ORG5, ou5e1);

        final IQrySources3 sources = 
                ij(
                        lj(
                                veh,
                                repVeh,
                                eq(prop("replacedBy", veh), prop(ID, repVeh))
                          ),
                        ou5e,
                        eq(expr(prop("station", veh)), expr(prop(ID, ou5e)))
                  );
        final Conditions3 conditions = or(isNotNull(expr(prop("key", veh))), isNotNull(expr(prop("key", repVeh))));
        final EntQuery3 expQry = qry(sources, conditions);

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

        final EntQuery3 actQry = query(sources1, conditions1, VEHICLE);

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
                                        eq(prop("replacedBy", veh), prop(ID, repVeh))
                                  ),
                                ij(
                                        ou5,
                                        ou4,
                                        eq(prop("parent", ou5), prop(ID, ou4))
                                  ),
                                eq(prop("station", veh), prop(ID, ou5))
                          ),
                        ij(
                                ou5e,
                                ou5eou4,
                                eq(prop("parent", ou5e), prop(ID, ou5eou4))
                          ),
                        eq(expr(prop("station", veh)), expr(prop(ID, ou5e)))
                  );
        final Conditions3 conditions = or(
                isNotNull(expr(prop("key", veh))),
                isNotNull(expr(prop("key", repVeh))), 
                isNotNull(expr(prop("initDate", veh))), 
                isNotNull(expr(prop("name", ou5))), 
                isNotNull(expr(prop("name", ou4))),
                isNotNull(expr(prop("name", ou5eou4))));
        final EntQuery3 expQry = qry(sources, conditions);

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
        
        final EntQuery3 actQry = query(sources1, conditions1, VEHICLE);
        
        
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
                                        eq(prop("replacedBy", veh), prop(ID, repVeh))
                                  ),
                                ij(
                                        ou5,
                                        ij(
                                                ou4,
                                                ou3,
                                                eq(prop("parent", ou4), prop(ID, ou3))
                                          ),
                                        eq(prop("parent", ou5), prop(ID, ou4))
                                  ),
                                eq(prop("station", veh), prop(ID, ou5))
                          ),
                        ij(
                                ou2e,
                                ou2eou1,
                                eq(prop("parent", ou2e), prop(ID, ou2eou1))
                          ),
                        eq(expr(prop("parent", ou3)), expr(prop(ID, ou2e)))
                  );
        final Conditions3 conditions = or(
                isNotNull(expr(prop("initDate", veh))),
                isNotNull(expr(prop("initDate", repVeh))), 
                isNotNull(expr(prop("name", ou5))), 
                isNotNull(expr(prop("name", ou4))),
                isNotNull(expr(prop("key", ou2eou1))));
        final EntQuery3 expQry = qry(sources, conditions);

        assertEquals(expQry, actQry);
    }
 }