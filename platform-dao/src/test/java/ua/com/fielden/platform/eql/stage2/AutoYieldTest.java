package ua.com.fielden.platform.eql.stage2;

import org.junit.Test;
import ua.com.fielden.platform.eql.meta.EqlStage2TestCase;
import ua.com.fielden.platform.eql.stage2.queries.ResultQuery2;
import ua.com.fielden.platform.eql.stage2.sources.Source2BasedOnPersistentType;
import ua.com.fielden.platform.eql.stage2.sources.Source2BasedOnQueries;
import ua.com.fielden.platform.eql.stage2.sundries.Yields2;
import ua.com.fielden.platform.persistence.types.EntityWithRichText;

import static org.junit.Assert.*;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.test_utils.TestUtils.assertInstanceOf;
import static ua.com.fielden.platform.test_utils.TestUtils.assertPresent;

public class AutoYieldTest extends EqlStage2TestCase {

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

    @Test
    public void searchText_is_not_autoyielded_in_result_queries() {
        // Empty yields
        {
            final ResultQuery2 query = qry(select(EntityWithRichText.class).model());
            assertFalse(query.yields.yieldsMap().containsKey("text.searchText"));
        }

        // yieldAll
        {
            final ResultQuery2 query = qry(select(EntityWithRichText.class).yieldAll().modelAsEntity(EntityWithRichText.class));
            assertFalse(query.yields.yieldsMap().containsKey("text.searchText"));
        }
    }

    @Test
    public void searchText_is_autoyielded_in_source_queries() {
        // Empty yields
        {
            final ResultQuery2 query = qry(select(select(EntityWithRichText.class).model()).model());
            final var joinRoot = assertPresent(query.maybeJoinRoot);
            final var source = assertInstanceOf(Source2BasedOnQueries.class, joinRoot.mainSource());
            assertEquals(1, source.models().size());
            assertTrue(source.models().getFirst().yields.yieldsMap().containsKey("text.searchText"));
        }

        // yieldAll
        {
            final ResultQuery2 query = qry(
                    select(select(EntityWithRichText.class).yieldAll().modelAsEntity(EntityWithRichText.class))
                            .model());
            final var joinRoot = assertPresent(query.maybeJoinRoot);
            final var source = assertInstanceOf(Source2BasedOnQueries.class, joinRoot.mainSource());
            assertEquals(1, source.models().size());
            assertTrue(source.models().getFirst().yields.yieldsMap().containsKey("text.searchText"));
        }
    }

}
