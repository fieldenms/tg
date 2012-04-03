package ua.com.fielden.web.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.math.BigDecimal;

import org.junit.Test;
import org.restlet.Restlet;
import org.restlet.Router;

import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.pagination.IPage2;
import ua.com.fielden.platform.test.DbDrivenTestCase2;
import ua.com.fielden.platform.web.resources.RouterHelper;
import ua.com.fielden.platform.web.test.WebBasedTestCase;
import ua.com.fielden.web.entities.IInspectedEntityDao;
import ua.com.fielden.web.entities.InspectedEntity;
import ua.com.fielden.web.rao.InspectedEntityRao;

/**
 * Provides a unit test to ensure correct interaction with IPage summary model.
 *
 * @author TG Team
 *
 */
public class WebResourceWithSummaryTestCase extends WebBasedTestCase {
    private final IInspectedEntityDao rao = new InspectedEntityRao(config.restClientUtil());
    private final IInspectedEntityDao dao = DbDrivenTestCase2.injector.getInstance(IInspectedEntityDao.class);

    private final EntityResultQueryModel<InspectedEntity> model = select(InspectedEntity.class).model();
    private final AggregatedResultQueryModel summaryModel = select(InspectedEntity.class).yield().beginExpr().sumOf().prop("moneyProperty").endExpr().as("total_money").//
	    yield().beginExpr().maxOf().prop("key").endExpr().as("max_key").modelAsAggregate();
    private IPage2<InspectedEntity> firstPage;

    @Override
    protected String[] getDataSetPaths() {
	return new String[] { "src/test/resources/data-files/query-with-summary-test-case.flat.xml" };
    }

    @Override
    public synchronized Restlet getRoot() {
	final Router router = new Router(getContext());

	final RouterHelper helper = new RouterHelper(DbDrivenTestCase2.injector, DbDrivenTestCase2.entityFactory);
	helper.register(router, IInspectedEntityDao.class);
	helper.registerAggregates(router);

	return router;
    }


    @Override
    public void setUp() {
        super.setUp();
        firstPage = rao.firstPage(from(model).build(), from(summaryModel).build(), 15);
    }

    @Test
    public void test_first_page() {
	assertNotNull("Summary is missing.", firstPage.summary());
	assertEquals("Incorrect value for max_key.", "key9", firstPage.summary().get("max_key"));
	assertEquals("Incorrect value for total_money.", new BigDecimal("450"), firstPage.summary().get("total_money"));
    }

    @Test
    public void test_next_page() {
	final IPage2<InspectedEntity> page = firstPage.next();

	assertNotNull("Summary is missing.", page.summary());
	assertEquals("Incorrect value for max_key.", "key9", page.summary().get("max_key"));
	assertEquals("Incorrect value for total_money.", new BigDecimal("450"), page.summary().get("total_money"));
    }

    @Test
    public void test_prev_page() {
	final IPage2<InspectedEntity> page = firstPage.next().prev();

	assertNotNull("Summary is missing.", page.summary());
	assertEquals("Incorrect value for max_key.", "key9", page.summary().get("max_key"));
	assertEquals("Incorrect value for total_money.", new BigDecimal("450"), page.summary().get("total_money"));
    }

    @Test
    public void test_last_page() {
	final IPage2<InspectedEntity> page = firstPage.last();

	assertNotNull("Summary is missing.", page.summary());
	assertEquals("Incorrect value for max_key.", "key9", page.summary().get("max_key"));
	assertEquals("Incorrect value for total_money.", new BigDecimal("450"), page.summary().get("total_money"));
    }

    @Test
    public void test_first_from_last_page() {
	final IPage2<InspectedEntity> page = firstPage.last().first();

	assertNotNull("Summary is missing.", page.summary());
	assertEquals("Incorrect value for max_key.", "key9", page.summary().get("max_key"));
	assertEquals("Incorrect value for total_money.", new BigDecimal("450"), page.summary().get("total_money"));
    }

}
