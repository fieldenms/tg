package ua.com.fielden.web.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetch;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAggregates;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.orderBy;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.restlet.Restlet;
import org.restlet.routing.Router;

import ua.com.fielden.platform.dao.IEntityAggregatesDao;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.rao.CommonEntityAggregatesRao;
import ua.com.fielden.platform.rao.EntityAggregatesRao;
import ua.com.fielden.platform.test.DbDrivenTestCase;
import ua.com.fielden.platform.web.resources.RouterHelper;
import ua.com.fielden.platform.web.test.WebBasedTestCase;
import ua.com.fielden.web.entities.InspectedEntity;

/**
 * Provides a unit test for entity aggregates web resource.
 * 
 * @author TG Team
 * 
 */
public class WebResourceEntityAggregatesTestCase extends WebBasedTestCase {
    private final IEntityAggregatesDao rao = new EntityAggregatesRao(new CommonEntityAggregatesRao(config.restClientUtil()));

    @Test
    public void test_aggregated_data_retrieval() {
        final AggregatedResultQueryModel model = select(InspectedEntity.class)//
        .groupBy().prop("intProperty")//
        .yield().prop("intProperty").as("intProperty").yield().beginExpr().countOf().prop("id").endExpr().as("kount").modelAsAggregate();

        final OrderingModel orderBy = orderBy().prop("intProperty").asc().model();
        final List<EntityAggregates> result = rao.getAllEntities(from(model).with(orderBy).model());

        assertEquals("Incorrect number of fetched aggregated items.", 2, result.size());
        assertEquals("Incorrect value of aggregated property.", 10, result.get(0).get("intProperty"));
        assertEquals("Incorrect value of aggregated result.", "9", result.get(0).get("kount").toString());
    }

    @Test
    public void test_aggregated_data_first_entities_retrieval() {
        final AggregatedResultQueryModel model = select(InspectedEntity.class)//
        .groupBy().prop("intProperty")//
        .yield().prop("intProperty").as("intProperty").yield().beginExpr().countOf().prop("id").endExpr().as("kount").modelAsAggregate();

        final List<EntityAggregates> result = rao.getFirstEntities(from(model).model(), 1);

        assertEquals("Incorrect number of fetched aggregated items.", 1, result.size());
    }

    @Test
    public void test_pagination_of_aggregates() {
        final AggregatedResultQueryModel model = select(InspectedEntity.class)//
        .groupBy().prop("intProperty")//
        .yield().prop("intProperty").as("intProperty").yield().beginExpr().countOf().prop("id").endExpr().as("kount").modelAsAggregate();

        final OrderingModel orderBy = orderBy().prop("intProperty").asc().model();

        final IPage<EntityAggregates> page = rao.firstPage(from(model).with(orderBy).model(), 1);

        assertEquals("Incorrect page number", 0, page.no());
        assertEquals("Incorrect number of pages", 2, page.numberOfPages());
        assertEquals("Incorrect number of instances on the page.", 1, page.data().size());
        assertTrue("Should have next", page.hasNext());
    }

    @Test
    public void test_entity_query_export() {
        final AggregatedResultQueryModel model = select(InspectedEntity.class)//
        .groupBy().prop("intProperty")//
        .yield().prop("intProperty").as("intProperty").yield().beginExpr().countOf().prop("id").endExpr().as("kount").modelAsAggregate();

        final OrderingModel orderBy = orderBy().prop("intProperty").asc().model();

        try {
            final byte[] bytes = rao.export(from(model).with(orderBy).model(), new String[] { "intProperty", "kount" }, new String[] { "Integer Property", "Count" });
            assertNotNull("The export result should exist.", bytes);
            assertTrue("The export result should not be empty.", bytes.length > 0);
        } catch (final IOException e) {
            fail("Unexpected exception.");
        }
    }

    @Test
    public void test_entity_query_export_with_subproperties() {
        final AggregatedResultQueryModel model = select(InspectedEntity.class).//
        where().prop("entityPropertyOne").isNotNull().//
        groupBy().prop("entityPropertyOne").//
        yield().prop("entityPropertyOne").as("entityPropertyOne").//
        yield().beginExpr().countOf().prop("id").endExpr().as("kount").modelAsAggregate();

        final OrderingModel orderBy = orderBy().prop("entityPropertyOne.intProperty").asc().model();
        final fetch<EntityAggregates> fetch = fetchAggregates().with("kount").with("entityPropertyOne", fetch(InspectedEntity.class));
        try {
            final byte[] bytes = rao.export(from(model).with(orderBy).with(fetch).model(), new String[] { "entityPropertyOne.intProperty", "kount" }, new String[] {
                    "Integer Property", "Count" });
            assertNotNull("The export result should exist.", bytes);
            assertTrue("The export result should not be empty.", bytes.length > 0);
        } catch (final IOException e) {
            e.printStackTrace();
            fail("Unexpected exception.");
        }
    }

    @Override
    public synchronized Restlet getInboundRoot() {
        final Router router = new Router(getContext());

        final RouterHelper helper = new RouterHelper(DbDrivenTestCase.injector, DbDrivenTestCase.entityFactory);
        helper.registerAggregates(router);

        return router;
    }

    @Override
    protected String[] getDataSetPaths() {
        return new String[] { "src/test/resources/data-files/web-test-case.flat.xml" };
    }

}
