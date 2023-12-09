package ua.com.fielden.platform.eql.stage2;

import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import org.junit.Test;

import ua.com.fielden.platform.eql.meta.EqlStage2TestCase;
import ua.com.fielden.platform.eql.stage2.conditions.Conditions2;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage2.queries.ResultQuery2;
import ua.com.fielden.platform.eql.stage2.sources.IJoinNode2;
import ua.com.fielden.platform.eql.stage2.sources.Source2BasedOnPersistentType;
import ua.com.fielden.platform.eql.stage3.sources.IJoinNode3;

public class DotIdHandlingTest extends EqlStage2TestCase {

    @Test
    public void id_is_not_added_to_path_and_its_plain_entity_parent_prop_has_is_id_set_true() {
        final ResultQuery2 actQry = qryCountAll(select(MODEL).where().prop("make.id").isNotNull());
        
        final Source2BasedOnPersistentType source = source(1, MODEL);
        final IJoinNode2<? extends IJoinNode3> sources = sources(source);
        final Prop2 makeProp = propWithIsId(source, pi(MODEL, "make"));
        final Conditions2 conditions = cond(isNotNull(makeProp));
        final ResultQuery2 expQry = qryCountAll(sources, conditions);

        assertEquals(expQry, actQry);
    }
    
    @Test
    public void id_is_not_added_to_path_and_its_calc_entity_parent_prop_has_is_id_set_true() {
        final ResultQuery2 actQry = qryCountAll(select(VEHICLE).where().prop("modelMake.id").isNotNull());
        
        final Source2BasedOnPersistentType source = source(1, VEHICLE);
        final IJoinNode2<? extends IJoinNode3> sources = sources(source);
        final Prop2 makeProp = propWithIsId(source, pi(VEHICLE, "modelMake"));
        final Conditions2 conditions = cond(isNotNull(makeProp));
        final ResultQuery2 expQry = qryCountAll(sources, conditions);

        assertEquals(expQry, actQry);
    }
}