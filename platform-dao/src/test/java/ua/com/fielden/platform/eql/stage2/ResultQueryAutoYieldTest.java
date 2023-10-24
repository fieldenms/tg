package ua.com.fielden.platform.eql.stage2;

import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import org.junit.Test;

import ua.com.fielden.platform.eql.meta.EqlStage2TestCase;
import ua.com.fielden.platform.eql.stage2.etc.Yields2;
import ua.com.fielden.platform.eql.stage2.queries.ResultQuery2;
import ua.com.fielden.platform.eql.stage2.sources.Source2BasedOnPersistentType;

public class ResultQueryAutoYieldTest extends EqlStage2TestCase {

    @Test
    public void auto_yield_for_type_based_query_works() {
        final ResultQuery2 actQry = qry(select(BOGIE).model());
        final Source2BasedOnPersistentType bogie = source(1, BOGIE);
        final Yields2 yields = mkYields(
                mkYield(prop(bogie, pi(BOGIE, "key")), "key"),
                mkYield(prop(bogie, pi(BOGIE, "desc")), "desc"),
                mkYield(prop(bogie, pi(BOGIE, "id")), "id"),
                mkYield(prop(bogie, pi(BOGIE, "version")), "version"),
                mkYield(prop(bogie, pi(BOGIE, "location"), pi(BOGIE, "location", "wagonSlot")), "location.wagonSlot"),
                mkYield(prop(bogie, pi(BOGIE, "location"), pi(BOGIE, "location", "workshop")), "location.workshop"),
                mkYield(prop(bogie, pi(BOGIE, "bogieClass")), "bogieClass"),
                mkYield(prop(bogie, pi(BOGIE, "active")), "active"),
                mkYield(prop(bogie, pi(BOGIE, "createdBy")), "createdBy"),
                mkYield(prop(bogie, pi(BOGIE, "createdDate")), "createdDate"),
                mkYield(prop(bogie, pi(BOGIE, "createdTransactionGuid")), "createdTransactionGuid"),
                mkYield(prop(bogie, pi(BOGIE, "lastUpdatedBy")), "lastUpdatedBy"),
                mkYield(prop(bogie, pi(BOGIE, "lastUpdatedDate")), "lastUpdatedDate"),
                mkYield(prop(bogie, pi(BOGIE, "lastUpdatedTransactionGuid")), "lastUpdatedTransactionGuid"),
                mkYield(prop(bogie, pi(BOGIE, "refCount")), "refCount")
                );
        assertEquals(yields, actQry.yields);
    }
}