package ua.com.fielden.platform.dao;

import static org.junit.Assert.*;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.*;

import java.util.List;
import java.util.stream.Stream;

import org.junit.Test;

import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.persistence.composite.EntityWithDynamicCompositeKey;
import ua.com.fielden.platform.persistence.types.EntityWithMoney;
import ua.com.fielden.platform.test.ioc.UniversalConstantsForTesting;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.utils.IUniversalConstants;

/**
 * A test case to ensure correct instantiation of entities from the instrumentation perspective during retrieval. 
 * 
 * @author TG Team
 *
 */
public class CommonEntityDaoInstrumentationTest extends AbstractDaoTestCase {

    @Test
    public void by_default_find_by_key_returns_instrumented_instances() {
        final EntityWithMoney entity = co(EntityWithMoney.class).findByKey("KEY1");
        assertTrue(entity.isInstrumented());
    }

    @Test
    public void uninstrumented_find_by_key_returns_unnstrumented_instances() {
        final EntityWithMoney entity = co(EntityWithMoney.class).uninstrumented().findByKey("KEY1");
        assertFalse(entity.isInstrumented());
    }
    
    @Test
    public void by_default_find_by_id_returns_instrumented_instances() {
        final IEntityDao<EntityWithMoney> co = co(EntityWithMoney.class);
        final EntityWithMoney entity = co.findById(co.findByKey("KEY1").getId());
        assertTrue(entity.isInstrumented());
    }

    @Test
    public void uninstrumented_find_by_id_returns_uninstrumented_instances() {
        final IEntityDao<EntityWithMoney> co = co(EntityWithMoney.class);
        final EntityWithMoney entity = co.uninstrumented().findById(co.findByKey("KEY1").getId());
        assertFalse(entity.isInstrumented());
    }

    @Test
    public void by_default_find_by_id_and_fetch_returns_instrumented_instances() {
        final IEntityDao<EntityWithMoney> co = co(EntityWithMoney.class);
        final EntityWithMoney entity = co.findById(co.findByKey("KEY1").getId(), fetchAll(EntityWithMoney.class));
        assertTrue(entity.isInstrumented());
    }

    @Test
    public void uninstrumented_find_by_id_and_fetch_returns_uninstrumented_instances() {
        final IEntityDao<EntityWithMoney> co = co(EntityWithMoney.class);
        final EntityWithMoney entity = co.uninstrumented().findById(co.findByKey("KEY1").getId(), fetchAll(EntityWithMoney.class));
        assertFalse(entity.isInstrumented());
    }

    @Test
    public void by_default_find_by_key_and_fetch_returns_instrumented_instances() {
        final IEntityDao<EntityWithMoney> co = co(EntityWithMoney.class);
        final EntityWithMoney entity = co.findByKeyAndFetch(fetchAll(EntityWithMoney.class), "KEY1");
        assertTrue(entity.isInstrumented());
    }

    @Test
    public void uninstrumented_find_by_key_and_fetch_returns_uninstrumented_instances() {
        final IEntityDao<EntityWithMoney> co = co(EntityWithMoney.class);
        final EntityWithMoney entity = co.uninstrumented().findByKeyAndFetch(fetchAll(EntityWithMoney.class), "KEY1");
        assertFalse(entity.isInstrumented());
    }

    @Test
    public void by_default_first_page_returns_instrumented_instances() {
        final List<EntityWithMoney> entities = co(EntityWithMoney.class).firstPage(10).data();
        assertTrue(entities.size() > 0);
        assertEquals("All entities are instrumented", entities.size(), entities.stream().filter(e -> e.isInstrumented()).count());
    }

    @Test
    public void uninstrumented_first_page_returns_uninstrumented_instances() {
        final List<EntityWithMoney> entities = co(EntityWithMoney.class).uninstrumented().firstPage(10).data();
        assertTrue(entities.size() > 0);
        assertEquals("All entities are instrumented", 0, entities.stream().filter(e -> e.isInstrumented()).count());
    }

    @Test
    public void by_default_first_page_with_EQL_model_returns_instrumented_instances() {
        final QueryExecutionModel<EntityWithMoney, EntityResultQueryModel<EntityWithMoney>> qem = 
                from(select(EntityWithMoney.class).where().prop("money.amount").gt().val(20).model())
                .with(fetchAll(EntityWithMoney.class))
                .with(orderBy().prop("key").asc().model()).model();

        final List<EntityWithMoney> entities = co(EntityWithMoney.class).firstPage(qem, 10).data();
        assertTrue(entities.size() > 0);
        assertEquals("All entities are instrumented", entities.size(), entities.stream().filter(e -> e.isInstrumented()).count());
    }

    @Test
    public void by_default_first_page_with_lightweight_EQL_model_returns_uninstrumented_instances() {
        final QueryExecutionModel<EntityWithMoney, EntityResultQueryModel<EntityWithMoney>> qem = 
                from(select(EntityWithMoney.class).where().prop("money.amount").gt().val(20).model())
                .with(fetchAll(EntityWithMoney.class))
                .with(orderBy().prop("key").asc().model()).lightweight().model();

        final List<EntityWithMoney> entities = co(EntityWithMoney.class).firstPage(qem, 10).data();
        assertTrue(entities.size() > 0);
        assertEquals("All entities are instrumented", 0, entities.stream().filter(e -> e.isInstrumented()).count());
    }

    @Test
    public void uninstrumented_first_page_with_EQL_model_returns_uninstrumented_instances() {
        final QueryExecutionModel<EntityWithMoney, EntityResultQueryModel<EntityWithMoney>> qem = 
                from(select(EntityWithMoney.class).where().prop("money.amount").gt().val(20).model())
                .with(fetchAll(EntityWithMoney.class))
                .with(orderBy().prop("key").asc().model()).model();

        final List<EntityWithMoney> entities = co(EntityWithMoney.class).uninstrumented().firstPage(qem, 10).data();
        assertTrue(entities.size() > 0);
        assertEquals("All entities are instrumented", 0, entities.stream().filter(e -> e.isInstrumented()).count());
    }

    @Test
    public void uninstrumented_first_page_with_lightweight_EQL_model_returns_uninstrumented_instances() {
        final QueryExecutionModel<EntityWithMoney, EntityResultQueryModel<EntityWithMoney>> qem = 
                from(select(EntityWithMoney.class).where().prop("money.amount").gt().val(20).model())
                .with(fetchAll(EntityWithMoney.class))
                .with(orderBy().prop("key").asc().model()).lightweight().model();

        final List<EntityWithMoney> entities = co(EntityWithMoney.class).uninstrumented().firstPage(qem, 10).data();
        assertTrue(entities.size() > 0);
        assertEquals("All entities are instrumented", 0, entities.stream().filter(e -> e.isInstrumented()).count());
    }


    @Test
    public void by_default_get_page_returns_instrumented_instances() {
        final List<EntityWithMoney> entities = co(EntityWithMoney.class).getPage(1, 2).data();
        assertTrue(entities.size() > 0);
        assertEquals("All entities are instrumented", entities.size(), entities.stream().filter(e -> e.isInstrumented()).count());
    }

    @Test
    public void uninstrumented_get_page_returns_uninstrumented_instances() {
        final List<EntityWithMoney> entities = co(EntityWithMoney.class).uninstrumented().getPage(1, 2).data();
        assertTrue(entities.size() > 0);
        assertEquals("All entities are instrumented", 0, entities.stream().filter(e -> e.isInstrumented()).count());
    }

    @Test
    public void by_default_get_page_with_lightweight_EQL_model_returns_uninstrumented_instances() {
        final QueryExecutionModel<EntityWithMoney, EntityResultQueryModel<EntityWithMoney>> qem = 
                from(select(EntityWithMoney.class).where().prop("money.amount").gt().val(20).model())
                .with(fetchAll(EntityWithMoney.class))
                .with(orderBy().prop("key").asc().model()).lightweight().model();

        final List<EntityWithMoney> entities = co(EntityWithMoney.class).getPage(qem, 1, 2).data();
        assertTrue(entities.size() > 0);
        assertEquals("All entities are instrumented", 0, entities.stream().filter(e -> e.isInstrumented()).count());
    }

    @Test
    public void uninstrumented_get_page_with_EQL_model_returns_uninstrumented_instances() {
        final QueryExecutionModel<EntityWithMoney, EntityResultQueryModel<EntityWithMoney>> qem = 
                from(select(EntityWithMoney.class).where().prop("money.amount").gt().val(20).model())
                .with(fetchAll(EntityWithMoney.class))
                .with(orderBy().prop("key").asc().model()).model();

        final List<EntityWithMoney> entities = co(EntityWithMoney.class).uninstrumented().getPage(qem, 1, 2).data();
        assertTrue(entities.size() > 0);
        assertEquals("All entities are instrumented", 0, entities.stream().filter(e -> e.isInstrumented()).count());
    }

    @Test
    public void uninstrumented_get_page_with_lightweight_EQL_model_returns_uninstrumented_instances() {
        final QueryExecutionModel<EntityWithMoney, EntityResultQueryModel<EntityWithMoney>> qem = 
                from(select(EntityWithMoney.class).where().prop("money.amount").gt().val(20).model())
                .with(fetchAll(EntityWithMoney.class))
                .with(orderBy().prop("key").asc().model()).lightweight().model();

        final List<EntityWithMoney> entities = co(EntityWithMoney.class).uninstrumented().getPage(qem, 1, 2).data();
        assertTrue(entities.size() > 0);
        assertEquals("All entities are instrumented", 0, entities.stream().filter(e -> e.isInstrumented()).count());
    }
    
    @Test
    public void by_default_get_entity_with_lightweight_EQL_model_returns_uninstrumented_instance() {
        final QueryExecutionModel<EntityWithMoney, EntityResultQueryModel<EntityWithMoney>> qem = 
                from(select(EntityWithMoney.class).where().prop("key").eq().val("KEY1").model())
                .with(fetchAll(EntityWithMoney.class))
                .lightweight().model();

        final EntityWithMoney entity = co(EntityWithMoney.class).getEntity(qem);
        assertTrue(entity !=  null);
        assertFalse(entity.isInstrumented());
    }

    @Test
    public void uninstrumented_get_entity_returns_uninstrumented_instances() {
        final QueryExecutionModel<EntityWithMoney, EntityResultQueryModel<EntityWithMoney>> qem = 
                from(select(EntityWithMoney.class).where().prop("key").eq().val("KEY1").model())
                .with(fetchAll(EntityWithMoney.class))
                .model();

        final EntityWithMoney entity = co(EntityWithMoney.class).uninstrumented().getEntity(qem);
        assertTrue(entity !=  null);
        assertFalse(entity.isInstrumented());
    }

    @Test
    public void uninstrumented_get_entity_with_lightweight_EQL_model_returns_uninstrumented_instances() {
        final QueryExecutionModel<EntityWithMoney, EntityResultQueryModel<EntityWithMoney>> qem = 
                from(select(EntityWithMoney.class).where().prop("key").eq().val("KEY1").model())
                .with(fetchAll(EntityWithMoney.class))
                .lightweight().model();

        final EntityWithMoney entity = co(EntityWithMoney.class).uninstrumented().getEntity(qem);
        assertTrue(entity !=  null);
        assertFalse(entity.isInstrumented());
    }

    @Test
    public void by_default_get_all_entities_with_lightweight_EQL_model_returns_uninstrumented_instances() {
        final QueryExecutionModel<EntityWithMoney, EntityResultQueryModel<EntityWithMoney>> qem = 
                from(select(EntityWithMoney.class).where().prop("money.amount").gt().val(20).model())
                .with(fetchAll(EntityWithMoney.class))
                .with(orderBy().prop("key").asc().model()).lightweight().model();

        final List<EntityWithMoney> entities = co(EntityWithMoney.class).getAllEntities(qem);
        assertTrue(entities.size() > 0);
        assertEquals("All entities are instrumented", 0, entities.stream().filter(e -> e.isInstrumented()).count());
    }

    @Test
    public void uninstrumented_get_all_entities_with_EQL_model_returns_uninstrumented_instances() {
        final QueryExecutionModel<EntityWithMoney, EntityResultQueryModel<EntityWithMoney>> qem = 
                from(select(EntityWithMoney.class).where().prop("money.amount").gt().val(20).model())
                .with(fetchAll(EntityWithMoney.class))
                .with(orderBy().prop("key").asc().model()).model();

        final List<EntityWithMoney> entities = co(EntityWithMoney.class).uninstrumented().getAllEntities(qem);
        assertTrue(entities.size() > 0);
        assertEquals("All entities are instrumented", 0, entities.stream().filter(e -> e.isInstrumented()).count());
    }

    @Test
    public void uninstrumented_get_all_entities_with_lightweight_EQL_model_returns_uninstrumented_instances() {
        final QueryExecutionModel<EntityWithMoney, EntityResultQueryModel<EntityWithMoney>> qem = 
                from(select(EntityWithMoney.class).where().prop("money.amount").gt().val(20).model())
                .with(fetchAll(EntityWithMoney.class))
                .with(orderBy().prop("key").asc().model()).lightweight().model();

        final List<EntityWithMoney> entities = co(EntityWithMoney.class).uninstrumented().getAllEntities(qem);
        assertTrue(entities.size() > 0);
        assertEquals("All entities are instrumented", 0, entities.stream().filter(e -> e.isInstrumented()).count());
    }
    
    @Test
    public void by_default_get_first_entities_with_lightweight_EQL_model_returns_uninstrumented_instances() {
        final QueryExecutionModel<EntityWithMoney, EntityResultQueryModel<EntityWithMoney>> qem = 
                from(select(EntityWithMoney.class).where().prop("money.amount").gt().val(20).model())
                .with(fetchAll(EntityWithMoney.class))
                .with(orderBy().prop("key").asc().model()).lightweight().model();

        final List<EntityWithMoney> entities = co(EntityWithMoney.class).getFirstEntities(qem, 10);
        assertTrue(entities.size() > 0);
        assertEquals("All entities are instrumented", 0, entities.stream().filter(e -> e.isInstrumented()).count());
    }

    @Test
    public void uninstrumented_get_first_entities_with_EQL_model_returns_uninstrumented_instances() {
        final QueryExecutionModel<EntityWithMoney, EntityResultQueryModel<EntityWithMoney>> qem = 
                from(select(EntityWithMoney.class).where().prop("money.amount").gt().val(20).model())
                .with(fetchAll(EntityWithMoney.class))
                .with(orderBy().prop("key").asc().model()).model();

        final List<EntityWithMoney> entities = co(EntityWithMoney.class).uninstrumented().getFirstEntities(qem, 10);
        assertTrue(entities.size() > 0);
        assertEquals("All entities are instrumented", 0, entities.stream().filter(e -> e.isInstrumented()).count());
    }

    @Test
    public void uninstrumented_get_first_entities_with_lightweight_EQL_model_returns_uninstrumented_instances() {
        final QueryExecutionModel<EntityWithMoney, EntityResultQueryModel<EntityWithMoney>> qem = 
                from(select(EntityWithMoney.class).where().prop("money.amount").gt().val(20).model())
                .with(fetchAll(EntityWithMoney.class))
                .with(orderBy().prop("key").asc().model()).lightweight().model();

        final List<EntityWithMoney> entities = co(EntityWithMoney.class).uninstrumented().getFirstEntities(qem, 10);
        assertTrue(entities.size() > 0);
        assertEquals("All entities are instrumented", 0, entities.stream().filter(e -> e.isInstrumented()).count());
    }
    
    @Test
    public void by_default_stream_with_lightweight_EQL_model_returns_uninstrumented_instances() {
        final QueryExecutionModel<EntityWithMoney, EntityResultQueryModel<EntityWithMoney>> qem = 
                from(select(EntityWithMoney.class).where().prop("money.amount").gt().val(20).model())
                .with(fetchAll(EntityWithMoney.class))
                .with(orderBy().prop("key").asc().model()).lightweight().model();

        final Stream<EntityWithMoney> stream = co(EntityWithMoney.class).stream(qem);
        assertEquals("All entities are instrumented", co(EntityWithMoney.class).count(qem.getQueryModel()), stream.filter(e -> !e.isInstrumented()).count());
    }

    @Test
    public void uninstrumented_stream_with_EQL_model_returns_uninstrumented_instances() {
        final QueryExecutionModel<EntityWithMoney, EntityResultQueryModel<EntityWithMoney>> qem = 
                from(select(EntityWithMoney.class).where().prop("money.amount").gt().val(20).model())
                .with(fetchAll(EntityWithMoney.class))
                .with(orderBy().prop("key").asc().model()).model();

        final Stream<EntityWithMoney> stream = co(EntityWithMoney.class).uninstrumented().stream(qem);
        assertEquals("All entities are instrumented", co(EntityWithMoney.class).count(qem.getQueryModel()), stream.filter(e -> !e.isInstrumented()).count());
    }

    @Test
    public void uninstrumented_stream_with_lightweight_EQL_model_returns_uninstrumented_instances() {
        final QueryExecutionModel<EntityWithMoney, EntityResultQueryModel<EntityWithMoney>> qem = 
                from(select(EntityWithMoney.class).where().prop("money.amount").gt().val(20).model())
                .with(fetchAll(EntityWithMoney.class))
                .with(orderBy().prop("key").asc().model()).lightweight().model();

        final Stream<EntityWithMoney> stream = co(EntityWithMoney.class).uninstrumented().stream(qem);
        assertEquals("All entities are instrumented", co(EntityWithMoney.class).count(qem.getQueryModel()), stream.filter(e -> !e.isInstrumented()).count());
    }

    @Override
    protected void populateDomain() {
        super.populateDomain();
        
        final UniversalConstantsForTesting constants = (UniversalConstantsForTesting) getInstance(IUniversalConstants.class);
        constants.setNow(dateTime("2016-02-19 02:47:00"));

        final EntityWithMoney keyPartTwo = save(new_ (EntityWithMoney.class, "KEY1", "desc").setMoney(new Money("20.00")).setDateTimeProperty(date("2009-03-01 11:00:55")));
        save(new_composite(EntityWithDynamicCompositeKey.class, "key-1-1", keyPartTwo));
        save(new_ (EntityWithMoney.class, "KEY2", "desc").setMoney(new Money("30.00")).setDateTimeProperty(date("2009-03-01 00:00:00")));
        save(new_ (EntityWithMoney.class, "KEY3", "desc").setMoney(new Money("40.00")));
        save(new_ (EntityWithMoney.class, "KEY4", "desc").setMoney(new Money("50.00")).setDateTimeProperty(date("2009-03-01 10:00:00")));
    }

}