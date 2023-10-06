package ua.com.fielden.platform.eql.stage2;

import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import org.junit.Test;

import ua.com.fielden.platform.eql.meta.EqlStage2TestCase;
import ua.com.fielden.platform.eql.stage2.conditions.Conditions2;
import ua.com.fielden.platform.eql.stage2.operands.queries.ResultQuery2;
import ua.com.fielden.platform.eql.stage2.sources.Source2BasedOnPersistentType;

public class NullPredicateEnhancementTest extends EqlStage2TestCase {

    @Test
    public void is_null_condition_is_correctly_transformed_for_union_property() {
        final ResultQuery2 actQry = qryCountAll(select(BOGIE).where().prop("location").isNull());
        final Source2BasedOnPersistentType bogie = source(1, BOGIE);
        final Conditions2 conditions = or(and(or(and(isNull(prop(bogie, pi(BOGIE, "location"), pi(BOGIE, "location", "wagonSlot"))), isNull(prop(bogie, pi(BOGIE, "location"), pi(BOGIE, "location", "workshop")))))));
        assertEquals(conditions, actQry.conditions);
    }
    
    @Test
    public void is_not_null_condition_is_correctly_transformed_for_union_property() {
        final ResultQuery2 actQry = qryCountAll(select(BOGIE).where().prop("location").isNotNull());
        final Source2BasedOnPersistentType bogie = source(1, BOGIE);
        final Conditions2 conditions = or(and(or(and(isNotNull(prop(bogie, pi(BOGIE, "location"), pi(BOGIE, "location", "wagonSlot")))), and(isNotNull(prop(bogie, pi(BOGIE, "location"), pi(BOGIE, "location", "workshop")))))));
        assertEquals(conditions, actQry.conditions);
    }
}