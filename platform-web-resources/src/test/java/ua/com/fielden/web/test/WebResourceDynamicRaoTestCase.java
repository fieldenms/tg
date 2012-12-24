package ua.com.fielden.web.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import org.junit.Test;
import org.restlet.Restlet;
import org.restlet.routing.Router;

import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.rao.DynamicEntityRao;
import ua.com.fielden.platform.test.DbDrivenTestCase;
import ua.com.fielden.platform.web.resources.RouterHelper;
import ua.com.fielden.platform.web.test.WebBasedTestCase;
import ua.com.fielden.web.entities.IInspectedEntityDao;
import ua.com.fielden.web.entities.InspectedEntity;

/**
 * Provides a unit test for entity driven web resources using dynamic RAO.
 *
 * @author TG Team
 *
 */
public class WebResourceDynamicRaoTestCase extends WebBasedTestCase {
    final DynamicEntityRao rao = new DynamicEntityRao(config.restClientUtil());
    {
	rao.setEntityType(InspectedEntity.class);
    }
    final IInspectedEntityDao dao = DbDrivenTestCase.injector.getInstance(IInspectedEntityDao.class);

    @Override
    protected String[] getDataSetPaths() {
	return new String[] { "src/test/resources/data-files/web-test-case.flat.xml" };
    }

    @Test
    public void test_pagination() {
	final IPage<InspectedEntity> page = rao.getPage(1, 10);
	assertEquals("Incorrect page number", 1, page.no());
	assertEquals("Incorrect number of pages", 5, page.numberOfPages());
	assertEquals("Incorrect number of instances on the page.", 10, page.data().size());

	final IPage<InspectedEntity> lastPage = page.last();
	assertEquals("Incorrect last page number", 4, lastPage.no());
	assertEquals("Incorrect number of pages", 5, lastPage.numberOfPages());
	assertEquals("Incorrect number of instances on the last page.", 5, lastPage.data().size());

	final IPage<InspectedEntity> firstPage = rao.firstPage(15);
	assertEquals("Incorrect number of instances on the first page.", 15, firstPage.data().size());
	assertEquals("Incorrect first page number", 0, firstPage.no());
	assertEquals("Incorrect number of pages", 3, firstPage.numberOfPages());
	assertEquals("Incorrect number of instances on the last page.", 15, firstPage.last().data().size());
    }

    @Test
    public void test_pagination_with_query() {
	final EntityResultQueryModel<InspectedEntity> q = select(InspectedEntity.class)
	.where().prop("intProperty").le().val(10).model();

	final IPage<InspectedEntity> page = rao.getPage(from(q).model(), 0, 5);
	assertEquals("Incorrect page number", 0, page.no());
	assertEquals("Incorrect number of pages", 2, page.numberOfPages());
	assertEquals("Incorrect number of instances on the page.", 5, page.data().size());

	final IPage<InspectedEntity> nextPage = page.next();
	assertEquals("Incorrect next page number", 1, nextPage.no());
	assertEquals("Incorrect number of pages", 2, nextPage.numberOfPages());
	assertEquals("Incorrect number of instances on the next page.", 4, nextPage.data().size());
    }

    @Test
    public void test_entity_exists() {
	assertTrue("Entity should exist.", rao.entityExists(dao.findById(1L)));
	assertFalse("Entity should exist.", rao.entityExists(dao.findById(46L)));
    }

    @Test
    public void test_entity_with_key_exists() {
	assertTrue("Entity should exist.", rao.entityWithKeyExists("key1"));
	assertFalse("Entity should exist.", rao.entityWithKeyExists("key46"));
    }

    @Test
    public void test_find_by_id() {
	assertNotNull("Entity should exist.", rao.findById(1L));
	assertNull("Entity should not have been found.", rao.findById(46L));
    }

    @Test
    public void test_find_by_key() {
	assertNotNull("Entity should exist.", rao.findByKey("key1"));
	assertNull("Entity should not exist.", rao.findByKey("key46"));
    }

    @Test
    public void test_that_save_method_fails() {
	final InspectedEntity newEntity = config.entityFactory().newByKey(InspectedEntity.class, "key46");
	newEntity.setDesc("new item");
	try {
	    rao.save(newEntity);
	    fail("Save should not be supported by dynamic RAO.");
	} catch (final Exception e) {
	}
    }

    @Override
    public synchronized Restlet getInboundRoot() {
	final Router router = new Router(getContext());

	final RouterHelper helper = new RouterHelper(DbDrivenTestCase.injector, DbDrivenTestCase.entityFactory);
	helper.register(router, IInspectedEntityDao.class);

	return router;
    }

}
