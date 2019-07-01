package ua.com.fielden.platform.eql.stage3.elements;


import static java.util.Arrays.asList;
import static ua.com.fielden.platform.entity.query.fluent.enums.ComparisonOperator.EQ;
import static ua.com.fielden.platform.entity.query.fluent.enums.ComparisonOperator.NE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import ua.com.fielden.platform.entity.query.fluent.enums.JoinType;
import ua.com.fielden.platform.eql.stage3.elements.conditions.ComparisonTest3;
import ua.com.fielden.platform.eql.stage3.elements.conditions.Conditions3;
import ua.com.fielden.platform.eql.stage3.elements.conditions.ICondition3;
import ua.com.fielden.platform.eql.stage3.elements.operands.EntProp3;
import ua.com.fielden.platform.eql.stage3.elements.operands.EntQuery3;
import ua.com.fielden.platform.eql.stage3.elements.sources.CompoundSource3;
import ua.com.fielden.platform.eql.stage3.elements.sources.IQrySource3;
import ua.com.fielden.platform.eql.stage3.elements.sources.QrySource3BasedOnTable;
import ua.com.fielden.platform.eql.stage3.elements.sources.Sources3;

public class TestSqlGeneration {

    @Test
    public void simple_query_generates_sql() {
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
         
         final ComparisonTest3 cond = eq(pc1, pc2);
         final ComparisonTest3 cond2 = ne(pc1, pc2);
         
         final Sources3 sources = sources(veh, JoinType.LJ, replacingVeh, orConditions(andConditions(cond), andConditions(cond2)));

         final EntQuery3 qry = qry(sources, yields(y1, y2));
         
         System.out.println(qry.sql());
    }
    
    
    static ComparisonTest3 eq(final EntProp3 op1, final EntProp3 op2) {
        return new ComparisonTest3(op1, EQ, op2);
    }
    
    static ComparisonTest3 ne(final EntProp3 op1, final EntProp3 op2) {
        return new ComparisonTest3(op1, NE, op2);
    }
        
    static Conditions3 cond(final ICondition3 condition) {
        final List<List<? extends ICondition3>> disjunctiveConds = new ArrayList<>();
        final List<ICondition3> firstConjunctiveGroup = new ArrayList<>();
        firstConjunctiveGroup.add(condition);
        disjunctiveConds.add(firstConjunctiveGroup);
        return new Conditions3(false, disjunctiveConds);
    }
    
    static Sources3 sources(final IQrySource3 main, final JoinType jt, final IQrySource3 second, final Conditions3 conditions) {
        final List<CompoundSource3> compounds = new ArrayList<>();
        compounds.add(new CompoundSource3(second, jt, conditions));
        return new Sources3(main, compounds);
    }
    
    static Sources3 sources(final IQrySource3 main, final JoinType jt, final IQrySource3 second, final ICondition3 condition) {
        final List<CompoundSource3> compounds = new ArrayList<>();
        compounds.add(new CompoundSource3(second, jt, cond(condition)));
        return new Sources3(main, compounds);
    }
    
    static EntQuery3 qry(final Sources3 sources) {
        return new EntQuery3(new EntQueryBlocks3(sources, new Conditions3(), yields(), groups(), orders()));
    }

    static EntQuery3 qry(final Sources3 sources, final Yields3 yields) {
        return new EntQuery3(new EntQueryBlocks3(sources, new Conditions3(), yields, groups(), orders()));
    }

    static Yields3 yields(final Yield3 ... yields) {
        return new Yields3(asList(yields));
    }

    static GroupBys3 groups(final GroupBy3 ... groups) {
        return new GroupBys3(asList(groups));
    }

    static OrderBys3 orders(final OrderBy3 ... orders) {
        return new OrderBys3(asList(orders));
    }

    static List<? extends ICondition3> andConditions(final ICondition3 ... conditions) {
        return asList(conditions);
    }

    static Conditions3 orConditions(final List<? extends ICondition3> ... conditions) {
        final List<List<? extends ICondition3>> list = new ArrayList<>();
        for (final List<? extends ICondition3> condList : conditions) {
            list.add(condList);
        }
        return new Conditions3(false, list);
    }

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
 }