package ua.com.fielden.web.test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.restlet.Restlet;
import org.restlet.routing.Router;

import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.test.DbDrivenTestCase;
import ua.com.fielden.platform.web.resources.RouterHelper;
import ua.com.fielden.platform.web.test.WebBasedTestCase;
import ua.com.fielden.web.entities.IInspectedEntityDao;
import ua.com.fielden.web.entities.InspectedEntity;
import ua.com.fielden.web.rao.InspectedEntityRao;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

/**
 * Provides a unit test for entity driven web resources.
 * 
 * @author TG Team
 * 
 */
@Deprecated
public class WebResourceTestCase extends WebBasedTestCase {
    private final IInspectedEntityDao rao = new InspectedEntityRao(config.restClientUtil());
    private final IInspectedEntityDao dao = DbDrivenTestCase.injector.getInstance(IInspectedEntityDao.class);

    @Override
    protected String[] getDataSetPaths() {
        return new String[] { "src/test/resources/data-files/web-test-case.flat.xml" };
    }

    @Override
    public synchronized Restlet getInboundRoot() {
        final Router router = new Router(getContext());

        final RouterHelper helper = new RouterHelper(DbDrivenTestCase.injector, DbDrivenTestCase.entityFactory);
        helper.register(router, IInspectedEntityDao.class);

        return router;
    }

    @Test
    @Ignore("Due to RAO pahsing out and Kryo related serialisation errors due to introduction of entity proxies")
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
        final EntityResultQueryModel<InspectedEntity> q = select(InspectedEntity.class).where().prop("intProperty").le().val(10).model();

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
    public void test_get_all_entities() {
        final EntityResultQueryModel<InspectedEntity> q = select(InspectedEntity.class).model();
        final List<InspectedEntity> list = rao.getAllEntities(from(q).model());
        assertEquals("Incorrect count value.", 45, list.size());
    }

    @Test
    public void test_get_first_entities() {
        final EntityResultQueryModel<InspectedEntity> q = select(InspectedEntity.class).model();
        final List<InspectedEntity> list = rao.getFirstEntities(from(q).model(), 7);
        assertEquals("Incorrect count value.", 7, list.size());
    }

    @Test
    public void test_entity_query_export() {
        final EntityResultQueryModel<InspectedEntity> q = select(InspectedEntity.class).where().prop("intProperty").le().val(10).model();

        try {
            final byte[] bytes = rao.export(from(q).model(), new String[] { "key", "dateProperty" }, new String[] { "this", "date" });
            assertNotNull("The export result should exist.", bytes);
            assertTrue("The export result should not be empty.", bytes.length > 0);
        } catch (final IOException e) {
            fail("Unexpected exception.");
        }
    }

    @Test
    public void test_entity_query_count() {
        final EntityResultQueryModel<InspectedEntity> q = select(InspectedEntity.class).model();
        assertEquals("Incorrect count value.", 45, rao.count(q));
    }

    @Test
    public void test_delete() {
        rao.delete(rao.findById(1L));
        final EntityResultQueryModel<InspectedEntity> q = select(InspectedEntity.class).model();
        assertEquals("Incorrect count value.", 44, rao.count(q));
    }

    @Test
    public void test_delete_by_model() {
        final EntityResultQueryModel<InspectedEntity> model = select(InspectedEntity.class).where().prop("id").in().values(1L, 2L).model();
        rao.delete(model, Collections.<String, Object> emptyMap());
        final EntityResultQueryModel<InspectedEntity> countModel = select(InspectedEntity.class).model();
        assertEquals("Incorrect count value.", 43, rao.count(countModel));
    }

    @Test
    public void test_entity_exists() {
        DbDrivenTestCase.hibernateUtil.getSessionFactory().getCurrentSession().close();
        assertTrue("Entity should exist.", rao.entityExists(dao.findById(1L)));
        assertFalse("Entity should exist.", rao.entityExists(dao.findById(46L)));
    }

    @Test
    public void test_entity_staleness_check() {
        assertFalse("Should not be stale", rao.isStale(1L, 0L));
        final InspectedEntity entity = rao.findById(1L);
        entity.setDesc("new desc to enforce version update");
        rao.save(entity);
        assertTrue("Should be stale", rao.isStale(1L, 0L));
        assertFalse("Should not be stale", rao.isStale(1L, 1L));
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
    public void test_save_new() {
        final InspectedEntity newEntity = config.entityFactory().newByKey(InspectedEntity.class, "key46");
        newEntity.setDesc("new item");
        final InspectedEntity entity = rao.save(newEntity);
        assertNotNull("ID has not been assigned.", entity.getId());

        final InspectedEntity justSavedEntity = rao.findByKey("key46");
        assertNotNull("Entity should exist.", justSavedEntity);
        assertEquals("Incorrect version.", Long.valueOf(0L), justSavedEntity.getVersion());
    }

    @Test
    public void test_save_existing() {
        final InspectedEntity entity = rao.findByKey("key1");
        assertEquals("Entity has unexpected description.", "desc1", entity.getDesc());

        entity.setDesc("changed desc");
        final InspectedEntity updatedEntity = rao.save(entity);
        assertEquals("Entity has incorrect description.", "changed desc", updatedEntity.getDesc());
        assertEquals("Incorrect version.", Long.valueOf(1L), updatedEntity.getVersion());

        final InspectedEntity justSavedEntity = rao.findByKey("key1");
        assertEquals("Incorrect version.", Long.valueOf(1L), justSavedEntity.getVersion());
    }
}
