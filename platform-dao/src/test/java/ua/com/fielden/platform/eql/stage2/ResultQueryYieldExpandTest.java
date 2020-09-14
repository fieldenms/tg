package ua.com.fielden.platform.eql.stage2;

import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import org.junit.Test;

import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.eql.meta.EqlStage2TestCase;
import ua.com.fielden.platform.eql.stage2.elements.Yields2;
import ua.com.fielden.platform.eql.stage2.elements.operands.ResultQuery2;
import ua.com.fielden.platform.eql.stage2.elements.operands.SourceQuery2;
import ua.com.fielden.platform.eql.stage2.elements.sources.QrySource2BasedOnPersistentType;
import ua.com.fielden.platform.eql.stage2.elements.sources.QrySource2BasedOnSubqueries;

public class ResultQueryYieldExpandTest extends EqlStage2TestCase {

    @Test
    public void explicit_yield_of_union_type_property_from_type_based_source_is_properly_expanded() {
        final ResultQuery2 actQry = qry(select(BOGIE).where().prop("key").eq().val("BOGIE1").yield().prop("location").as("l").modelAsAggregate());
        final QrySource2BasedOnPersistentType bogie = source("1", BOGIE);
        final Yields2 yields = yields(
                yield(prop(bogie, pi(BOGIE, "location")), "l"),
                yield(prop(bogie, pi(BOGIE, "location"), pi(BOGIE, "location", "wagonSlot")), "l.wagonSlot"),
                yield(prop(bogie, pi(BOGIE, "location"), pi(BOGIE, "location", "workshop")), "l.workshop")
                );
        assertEquals(yields, actQry.yields);
    }
    
    @Test
    public void explicit_yield_of_union_type_property_from_query_based_source_is_properly_expanded() {
        final AggregatedResultQueryModel srcQry = select(BOGIE).yield().prop("location").as("l").modelAsAggregate();
        final ResultQuery2 actResultQry = qry(select(srcQry).yield().prop("l").as("loc").modelAsAggregate());

        final QrySource2BasedOnPersistentType bogie = source("1", BOGIE);
        final Yields2 yields = yields(
                yield(prop(bogie, pi(BOGIE, "location")), "l"),
                yield(prop(bogie, pi(BOGIE, "location"), pi(BOGIE, "location", "wagonSlot")), "l.wagonSlot"),
                yield(prop(bogie, pi(BOGIE, "location"), pi(BOGIE, "location", "workshop")), "l.workshop")
                );
        
        final SourceQuery2 srcQry2 = srcqry(sources(bogie), yields);
        
        final QrySource2BasedOnSubqueries resultQrySource = source("2", srcQry2);

        final Yields2 resultQryYields = yields(
                yield(prop(resultQrySource, pi(BOGIE, "location").cloneRenamed("l")), "loc"),
                yield(prop(resultQrySource, pi(BOGIE, "location").cloneRenamed("l"), pi(BOGIE, "location", "wagonSlot")), "loc.wagonSlot"),
                yield(prop(resultQrySource, pi(BOGIE, "location").cloneRenamed("l"), pi(BOGIE, "location", "workshop")), "loc.workshop")
                );
        assertEquals(resultQryYields, actResultQry.yields);
    }
}