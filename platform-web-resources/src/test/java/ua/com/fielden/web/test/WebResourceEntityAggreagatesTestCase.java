package ua.com.fielden.web.test;

import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.restlet.Restlet;
import org.restlet.Router;

import ua.com.fielden.platform.dao.IEntityAggregatesDao;
import ua.com.fielden.platform.equery.EntityAggregates;
import ua.com.fielden.platform.equery.fetch;
import ua.com.fielden.platform.equery.interfaces.IQueryOrderedModel;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.rao.CommonEntityAggregatesRao;
import ua.com.fielden.platform.rao.EntityAggregatesRao;
import ua.com.fielden.platform.test.DbDrivenTestCase;
import ua.com.fielden.platform.web.resources.RouterHelper;
import ua.com.fielden.platform.web.test.WebBasedTestCase;
import ua.com.fielden.web.entities.InspectedEntity;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import static ua.com.fielden.platform.equery.equery.select;

/**
 * Provides a unit test for entity aggregates web resource.
 *
 * @author TG Team
 *
 */
public class WebResourceEntityAggreagatesTestCase extends WebBasedTestCase {
    private final IEntityAggregatesDao rao = new EntityAggregatesRao(new CommonEntityAggregatesRao(config.restClientUtil()));

    @Test
    public void test_aggregated_data_retrieval() {
	final IQueryOrderedModel<EntityAggregates> model = select(InspectedEntity.class)//
		.groupByProp("intProperty")//
		.yieldProp("intProperty").yieldExp("count([id])", "kount").orderBy("intProperty").model(EntityAggregates.class);

	final List<EntityAggregates> result = rao.listAggregates(model, null);

	assertEquals("Incorrect number of fetched aggregated items.", 2, result.size());
	assertEquals("Incorrect value of aggregated property.", 10, result.get(0).get("intProperty"));
	assertEquals("Incorrect value of aggregated result.", "9", result.get(0).get("kount").toString());
    }

    @Test
    public void test_pagination_of_aggregates() {
	final IQueryOrderedModel<EntityAggregates> model = select(InspectedEntity.class)//
		.groupByProp("intProperty")//
		.yieldProp("intProperty").yieldExp("count([id])", "kount").orderBy("intProperty").model(EntityAggregates.class);

	final IPage<EntityAggregates> page = rao.firstPage(model, null, 1);

	assertEquals("Incorrect page number", 0, page.no());
	assertEquals("Incorrect number of pages", 2, page.numberOfPages());
	assertEquals("Incorrect number of instances on the page.", 1, page.data().size());
	assertTrue("Should have next", page.hasNext());
    }

    @Test
    public void test_entity_query_export() {
	final IQueryOrderedModel<EntityAggregates> q = select(InspectedEntity.class)//
		.groupByProp("intProperty")//
		.yieldProp("intProperty").yieldExp("count([id])", "kount").orderBy("intProperty").model(EntityAggregates.class);

	try {
	    final byte[] bytes = rao.export(q, null, new String[] { "intProperty", "kount" }, new String[] { "Integer Property", "Count" });
	    assertNotNull("The export result should exist.", bytes);
	    assertTrue("The export result should not be empty.", bytes.length > 0);
	} catch (final IOException e) {
	    fail("Unexpected exception.");
	}
    }

    @Test
    public void test_entity_query_export_with_subproperties() {
	final IQueryOrderedModel<EntityAggregates> q = select(InspectedEntity.class)//
		.groupByProp("entityPropertyOne")//
		.yieldProp("entityPropertyOne").yieldExp("count([id])", "kount").orderBy("entityPropertyOne.key").model(EntityAggregates.class);

	try {
	    final byte[] bytes = rao.export(q, new fetch(EntityAggregates.class).with("entityPropertyOne", new fetch(InspectedEntity.class)), new String[] { "entityPropertyOne.intProperty", "kount" }, new String[] { "Integer Property", "Count" });
	    assertNotNull("The export result should exist.", bytes);
	    assertTrue("The export result should not be empty.", bytes.length > 0);
	} catch (final IOException e) {
	    fail("Unexpected exception.");
	}
    }

    @Override
    public synchronized Restlet getRoot() {
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
