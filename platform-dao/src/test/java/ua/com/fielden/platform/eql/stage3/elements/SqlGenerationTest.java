package ua.com.fielden.platform.eql.stage3.elements;


import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.meta.EqlStage3TestCase;
import ua.com.fielden.platform.eql.stage1.elements.conditions.Conditions1;
import ua.com.fielden.platform.eql.stage1.elements.sources.QrySource1BasedOnPersistentType;
import ua.com.fielden.platform.eql.stage1.elements.sources.Sources1;
import ua.com.fielden.platform.eql.stage3.elements.conditions.ComparisonTest3;
import ua.com.fielden.platform.eql.stage3.elements.conditions.Conditions3;
import ua.com.fielden.platform.eql.stage3.elements.operands.EntProp3;
import ua.com.fielden.platform.eql.stage3.elements.operands.EntQuery3;
import ua.com.fielden.platform.eql.stage3.elements.sources.IQrySources3;
import ua.com.fielden.platform.eql.stage3.elements.sources.QrySource3BasedOnTable;

public class SqlGenerationTest extends EqlStage3TestCase {

    @Test
    public void dot_notated_props_are_correctly_transformed() {
        final QrySource1BasedOnPersistentType veh1 = source(VEHICLE);
        final Sources1 sources1 = sources(veh1);
        final Conditions1 conditions1 = conditions(isNotNull(prop("initDate")), //
                or(isNotNull(prop("station.name"))), //
                or(isNotNull(prop("station.parent.name"))), //
                or(isNotNull(prop("replacedBy.initDate"))) //
        );

        final EntQuery3 actQry = query(sources1, conditions1, VEHICLE);
        
        final QrySource3BasedOnTable veh = source(VEHICLE, veh1);
        final QrySource3BasedOnTable repVeh = source3(VEHICLE, veh1, "replacedBy");
        final QrySource3BasedOnTable org5 = source3(ORG5, veh1, "station");
        final QrySource3BasedOnTable org4 = source3(ORG4, veh1, "station_parent");
        
        final IQrySources3 sources = lj(
                lj(
                        veh, repVeh, cond(eq(prop("replacedBy", veh), prop(ID, repVeh)))), 
                ij(
                        org5, org4, cond(eq(prop("parent", org5), prop(ID, org4)))), 
                cond(eq(prop("station", veh), prop(ID, org5))));
        final Conditions3 conditions = or(isNotNull(prop("initDate", veh)), isNotNull(prop("name", org5)), isNotNull(prop("name", org4)), isNotNull(prop("initDate", repVeh)));
        final EntQuery3 expQry = qry(sources, conditions);
        
        assertEquals(expQry, actQry);
    }
    
    @Test
    public void query_with_one_join_generates_sql() {
         final Map<String, Column> eqdetColumns = new HashMap<>();
         eqdetColumns.put("id", new Column("id"));
         eqdetColumns.put("key", new Column("key"));
         eqdetColumns.put("desc", new Column("desc"));
         eqdetColumns.put("replacing", new Column("replacing"));
        
         final Table EQDET = new Table("EQDET", eqdetColumns);
         
         final QrySource3BasedOnTable veh = new QrySource3BasedOnTable(EQDET, 1);
         final QrySource3BasedOnTable replacingVeh = new QrySource3BasedOnTable(EQDET, 2);
         
         final EntProp3 pc1 = new EntProp3("replacing", veh);
         final EntProp3 pc2 = new EntProp3("id", replacingVeh);
         final EntProp3 yp1 = new EntProp3("key", veh);
         final EntProp3 yp2 = new EntProp3("key", replacingVeh);
         final Yield3 y1 = new Yield3(yp1, "veh-key");
         final Yield3 y2 = new Yield3(yp2, "replacingVeh-key");
         
         final ComparisonTest3 cond1 = eq(pc1, pc2);
         final ComparisonTest3 cond2 = ne(pc1, pc2);
         
         final IQrySources3 sources = lj(veh, replacingVeh, or(cond1, cond2));

         final EntQuery3 qry = qry(sources, yields(y1, y2));
         
         System.out.println(qry.sql(DbVersion.H2));
    }

    @Test
    public void query_with_two_joins_generates_sql2() {
         final Map<String, Column> eqdetColumns = new HashMap<>();
         eqdetColumns.put("id", new Column("id"));
         eqdetColumns.put("key", new Column("key"));
         eqdetColumns.put("desc", new Column("desc"));
         eqdetColumns.put("replacing", new Column("replacing"));
         eqdetColumns.put("replacedBy", new Column("replacedBy"));
        
         final Table EQDET = new Table("EQDET", eqdetColumns);
         
         final QrySource3BasedOnTable veh = new QrySource3BasedOnTable(EQDET, 1);
         final QrySource3BasedOnTable replacingVeh = new QrySource3BasedOnTable(EQDET, 2);
         final QrySource3BasedOnTable replacedByVeh = new QrySource3BasedOnTable(EQDET, 3);
         
         final EntProp3 pc1 = new EntProp3("replacing", veh);
         final EntProp3 pc2 = new EntProp3("id", replacingVeh);
         final EntProp3 pc3 = new EntProp3("replacedBy", veh);
         final EntProp3 pc4 = new EntProp3("id", replacedByVeh);
         final EntProp3 yp1 = new EntProp3("key", veh);
         final EntProp3 yp2 = new EntProp3("key", replacingVeh);
         final EntProp3 yp3 = new EntProp3("key", replacedByVeh);
         final Yield3 y1 = new Yield3(yp1, "veh-key");
         final Yield3 y2 = new Yield3(yp2, "replacingVeh-key");
         final Yield3 y3 = new Yield3(yp3, "replacedByVeh-key");
         
         final ComparisonTest3 cond1 = eq(pc1, pc2);
         final ComparisonTest3 cond2 = ne(pc1, pc2);
         final ComparisonTest3 cond3 = eq(pc3, pc4);
         final ComparisonTest3 cond4 = ne(pc3, pc4);
         
         final IQrySources3 sources = lj(lj(veh, replacingVeh, or(cond1, cond2)), replacedByVeh, or(cond3, cond4));

         final EntQuery3 qry = qry(sources, yields(y1, y2, y3));
         
         System.out.println(qry.sql(DbVersion.H2));
    }
 }