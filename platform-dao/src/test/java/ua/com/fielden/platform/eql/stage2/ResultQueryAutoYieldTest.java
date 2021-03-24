package ua.com.fielden.platform.eql.stage2;

import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import org.junit.Test;

import ua.com.fielden.platform.eql.meta.EqlStage2TestCase;
import ua.com.fielden.platform.eql.stage2.etc.Yields2;
import ua.com.fielden.platform.eql.stage2.operands.ResultQuery2;
import ua.com.fielden.platform.eql.stage2.sources.Source2BasedOnPersistentType;

public class ResultQueryAutoYieldTest extends EqlStage2TestCase {

    @Test
    public void auto_yield_for_type_based_query_works() {
        final ResultQuery2 actQry = qry(select(BOGIE).model());
        final Source2BasedOnPersistentType bogie = source("1", BOGIE);
        final Yields2 yields = yields(
                yield(prop(bogie, pi(BOGIE, "key")), "key"),
                yield(prop(bogie, pi(BOGIE, "desc")), "desc"),
                yield(prop(bogie, pi(BOGIE, "id")), "id"),
                yield(prop(bogie, pi(BOGIE, "version")), "version"),
                yield(prop(bogie, pi(BOGIE, "location")), "location"),
                yield(prop(bogie, pi(BOGIE, "location"), pi(BOGIE, "location", "wagonSlot")), "location.wagonSlot"),
                yield(prop(bogie, pi(BOGIE, "location"), pi(BOGIE, "location", "workshop")), "location.workshop")
                );
        assertEquals(yields, actQry.yields);
    }
}