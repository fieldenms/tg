package ua.com.fielden.platform.eql.stage3.elements;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class TestSqlGeneration {

    @Test
    public void test1() {
         final Map<String, Column> eqdetColumns = new HashMap<>();
         eqdetColumns.put("id", new Column("id"));
         eqdetColumns.put("key", new Column("key"));
         eqdetColumns.put("desc", new Column("desc"));
         eqdetColumns.put("replacing", new Column("replacing"));
         
        
         final Table EQDET = new Table("EQDET", eqdetColumns);
         
         final TableAsQuerySource veh = new TableAsQuerySource(EQDET, 1l);
         final TableAsQuerySource replacingVeh = new TableAsQuerySource(EQDET, 2l);
         
         final PropColumn pc1 = new PropColumn(veh, "replacing");
         final PropColumn pc2 = new PropColumn(replacingVeh, "id");
         
         
         final SqlCondition cond = new SqlCondition(pc1, pc2);
         
         final SqlQuery q = new SqlQuery(3l, veh, replacingVeh, cond, Collections.emptyMap());
         
         System.out.println(q.sql());
    }
 }