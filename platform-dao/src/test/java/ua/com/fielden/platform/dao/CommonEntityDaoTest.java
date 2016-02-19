package ua.com.fielden.platform.dao;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import org.joda.time.DateTime;
import org.junit.Test;

import ua.com.fielden.platform.dao.exceptions.EntityCompanionException;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.persistence.composite.EntityWithDynamicCompositeKey;
import ua.com.fielden.platform.persistence.types.EntityWithMoney;
import ua.com.fielden.platform.test.DbDrivenTestCase;
import ua.com.fielden.platform.types.Money;

/**
 * This test case ensures correct implementation of the common DAO functionality in conjunction with Session injection by means of method intercepter.
 *
 * @author TG Team
 *
 */
public class CommonEntityDaoTest extends DbDrivenTestCase {
    private final EntityWithMoneyDao dao = injector.getInstance(EntityWithMoneyDao.class);
    private final EntityWithDynamicCompositeKeyDao daoComposite = injector.getInstance(EntityWithDynamicCompositeKeyDao.class);

    //TODO test count, delete

    public void test_that_entity_with_simple_key_is_handled_correctly() {
        // find all
        final List<EntityWithMoney> result = dao.getPage(0, 25).data();
        assertEquals("Incorrect number of retrieved entities.", 4, result.size());
        assertEquals("Incorrect key value.", "key1", result.get(0).getKey());
        // find by id
        assertEquals("Incorrect key value.", "key1", dao.findById(1L).getKey());
        // find by key
        assertEquals("Incorrect key value.", "key1", dao.findByKey("key1").getKey());
        // find by criteria
        final EntityResultQueryModel<EntityWithMoney> model1 = select(EntityWithMoney.class).where().prop("key").like().val("k%").model();
        assertEquals("Incorrect number of found entities.", 4, dao.getPage(from(model1).model(), 0, 25).data().size());
        final EntityResultQueryModel<EntityWithMoney> model2 = select(EntityWithMoney.class).where().prop("key").like().val("e%").model();
        assertEquals("Incorrect number of found entities.", 0, dao.getPage(from(model2).model(), 0, 25).data().size());
    }

    public void test_that_entity_with_composite_key_is_handled_correctly() {
        // find all
        final List<EntityWithDynamicCompositeKey> result = daoComposite.getPage(0, 25).data();
        assertEquals("Incorrect number of retrieved entities.", 1, result.size());
        assertEquals("Incorrect key value.", new DynamicEntityKey(result.get(0)), result.get(0).getKey());
        // find by id
        assertEquals("Incorrect key value.", new DynamicEntityKey(result.get(0)), daoComposite.findById(1L).getKey());
        // find by key
        final EntityWithMoney keyPartTwo = dao.findById(1L);
        assertEquals("Incorrect key value.", new DynamicEntityKey(result.get(0)), daoComposite.findByKey("key-1-1", keyPartTwo).getKey());
        // find by criteria
        final EntityResultQueryModel<EntityWithDynamicCompositeKey> model1 = select(EntityWithDynamicCompositeKey.class).where().prop("keyPartOne").like().val("k%").model();
        assertEquals("Incorrect number of found entities.", 1, daoComposite.getPage(from(model1).model(), 0, 25).data().size());
        final EntityResultQueryModel<EntityWithDynamicCompositeKey> model2 = select(EntityWithDynamicCompositeKey.class).where().prop("keyPartOne").like().val("e%").model();
        assertEquals("Incorrect number of found entities.", 0, daoComposite.getPage(from(model2).model(), 0, 25).data().size());
    }

    public void test_that_unfiltered_pagination_works() {
        final IPage<EntityWithMoney> page = dao.firstPage(2);
        assertEquals("Incorrect number of instances on the page.", 2, page.data().size());
        assertTrue("Page should have the next one.", page.hasNext());

        final IPage<EntityWithMoney> nextPage = page.next();
        assertFalse("Page should not have the next one.", nextPage.hasNext());
        assertEquals("Incorrect number of instances on the next page.", 2, nextPage.data().size());

        final IPage<EntityWithMoney> prevPage = nextPage.prev();
        assertEquals("Incorrect number of instances on the page.", 2, prevPage.data().size());
        assertTrue("Page should have the next one.", prevPage.hasNext());

        final IPage<EntityWithMoney> lastPage = page.last();
        assertFalse("Last page should not have the next one.", lastPage.hasNext());
        assertTrue("Last page should have the previous one.", lastPage.hasPrev());
        assertEquals("Incorrect number of instances on the last page.", 2, lastPage.data().size());
    }

    public void test_that_custom_query_pagination_works() {
        final EntityResultQueryModel<EntityWithMoney> q = select(EntityWithMoney.class)//
        .where().prop("money.amount").ge().val(new BigDecimal("30.00"))//
        .model();

        final IPage<EntityWithMoney> page = dao.firstPage(from(q).model(), 2);
        assertEquals("Incorrect number of instances on the page.", 2, page.data().size());
        assertTrue("Page should have the next one.", page.hasNext());

        final IPage<EntityWithMoney> nextPage = page.next();
        assertFalse("Page should not have the next one.", nextPage.hasNext());
        assertEquals("Incorrect number of instances on the next page.", 1, nextPage.data().size());

        final IPage<EntityWithMoney> firstPage = nextPage.first();
        assertTrue("First page should have the next one.", firstPage.hasNext());
        assertFalse("First page should not have the previous one.", firstPage.hasPrev());
        assertEquals("Incorrect number of instances on the first page.", 2, firstPage.data().size());

        final IPage<EntityWithMoney> lastPage = firstPage.last();
        assertFalse("Last page should not have the next one.", lastPage.hasNext());
        assertTrue("Last page should have the previous one.", lastPage.hasPrev());
        assertEquals("Incorrect number of instances on the last page.", 1, lastPage.data().size());
    }

    public void test_unconditional_streaming_should_contain_all_matching_entities() {
        final EntityResultQueryModel<EntityWithMoney> query = select(EntityWithMoney.class).model();
        final QueryExecutionModel<EntityWithMoney, EntityResultQueryModel<EntityWithMoney>> qem = from(query).model();

        final Stream<EntityWithMoney> streamBy3 = dao.stream(qem, 3);
        assertEquals("Incorrect number of entities in the stream", dao.count(query), streamBy3.count());
        
        final Stream<EntityWithMoney> streamBy1 = dao.stream(qem, 1);
        assertFalse("The stream should not be parallel", streamBy1.isParallel());
        assertEquals("Incorrect number of entities in the stream", dao.count(query), streamBy1.count());
    }

    public void test_that_there_is_API_for_streaming_with_default_page_capacity() {
        final Stream<EntityWithMoney> stream = dao.stream(from(select(EntityWithMoney.class).model()).model());
        assertEquals("Incorrect number of entities in the stream", 4, stream.count());
    }

    public void test_streaming_based_on_ordered_qem_should_have_the_same_traversal_order() {
        final EntityResultQueryModel<EntityWithMoney> query = select(EntityWithMoney.class).model();
        final OrderingModel orderBy = orderBy().prop("key").asc().model();
        final QueryExecutionModel<EntityWithMoney, EntityResultQueryModel<EntityWithMoney>> qem = from(query).with(orderBy).model();

        final Iterator<EntityWithMoney> iterator = dao.getAllEntities(qem).iterator();
        final Stream<EntityWithMoney> stream = dao.stream(qem, 2);
        stream.forEach(entity -> assertEquals(iterator.next(), entity));
    }

    public void test_streaming_based_on_conditional_qem_should_contain_only_matching_entities() {
        final EntityResultQueryModel<EntityWithMoney> query = select(EntityWithMoney.class)
                .where().prop("money.amount").ge().val(new BigDecimal("30.00"))//
                .model();
        final QueryExecutionModel<EntityWithMoney, EntityResultQueryModel<EntityWithMoney>> qem = from(query).model();

        final Stream<EntityWithMoney> stream = dao.stream(qem, 2);
        assertEquals("Incorrect number of entities in the stream", dao.count(query), stream.count());
    }

    public void test_stream_should_not_be_parallel() {
        final Stream<EntityWithMoney> streamBy3 = dao.stream(from(select(EntityWithMoney.class).model()).model(), 2);
        assertFalse("The stream should not be parallel", streamBy3.isParallel());
    }

    public void test_stream_should_not_be_accecible_once_traversed() {
        final Stream<EntityWithMoney> stream = dao.stream(from(select(EntityWithMoney.class).model()).model(), 2);
        
        // consume the stream by traversing it
        stream.forEach(e -> e.getMoney()/* basically do nothing*/);
        
        // try to consume the stream again by counting the number of elements in it
        try {
            stream.count();
            fail("Should have failed due to illegal state exception of the stream");
        } catch (final IllegalStateException ex) {
        }
    }

    
    public void test_entity_exists_using_entity() {
        final EntityWithMoney entity = dao.findByKey("key1");
        assertTrue(dao.entityExists(entity));
    }

    public void test_entity_exists_by_key() {
        assertTrue(dao.entityWithKeyExists("key1"));
        assertFalse(dao.entityWithKeyExists("non-existent-key"));
    }

    public void test_entity_exists_by_composite_key() {
        final String keyPartOne = "key-1-1";
        final EntityWithMoney keyPartTwo = dao.findById(1L);

        assertTrue(daoComposite.entityWithKeyExists(keyPartOne, keyPartTwo));
        assertFalse(daoComposite.entityWithKeyExists("non-existent-key", keyPartTwo));
    }

    public void test_entity_is_dirty_support() {
        EntityWithMoney entity = dao.findByKey("key1");
        assertFalse("Entity should not be dirty after retrieval", entity.isDirty());
        entity.setCalculatedProperty(new BigDecimal("0.00"));
        entity.setCalculatedProperty(new BigDecimal("1.00"));
        assertFalse("Entity should not be dirty after calcualted propeties have changed.", entity.isDirty());

        entity.setDesc("modified desc");
        assertTrue("Entity should be dirty after property modification", entity.isDirty());
        entity.setDesc("modified desc to something else");
        assertTrue("Entity should be dirty after property modification", entity.isDirty());

        dao.save(entity);
        entity = dao.findByKey("key1");
        assertFalse("Entity should not be dirty after save", entity.isDirty());

        entity.setDesc("modified desc");
        assertTrue("Entity should be dirty after property modification after resetting the state", entity.isDirty());

        EntityWithMoney newEntity = entityFactory.newByKey(EntityWithMoney.class, "some key");
        assertTrue("New entity should be dirty", newEntity.isDirty());
        newEntity.setMoney(new Money("12"));
        assertTrue("New entity should be dirty after modiciation", newEntity.isDirty());
        newEntity = dao.save(newEntity);
        assertFalse("New entity should not be dirty after save", newEntity.isDirty());
    }

    public void test_entity_property_dirty_support() {
        EntityWithMoney entity = dao.findByKey("key1");
        assertEquals("There should be no dirty properties after entity retrieval.", 0, entity.getDirtyProperties().size());
        final String originalDesc = entity.getDesc();
        entity.setDesc("modified desc");
        assertTrue("Entity should be dirty after property modification", entity.isDirty());
        assertTrue("Property should be dirty after modification", entity.getProperty("desc").isDirty());

        entity.setDesc(originalDesc);
        assertFalse("Property should be not dirty after modification back to original value", entity.getProperty("desc").isDirty());
        assertFalse("Entity should not stay dirty when all properties are not dirty", entity.isDirty());

        dao.save(entity);
        entity = dao.findByKey("key1");
        assertFalse("Entity should not be dirty after save", entity.isDirty());
        assertEquals("There should be no dirty properties after entity retrieval.", 0, entity.getDirtyProperties().size());

        entity.setDesc("modified desc");
        assertTrue("Entity should be dirty after property modification after resetting the state", entity.isDirty());
        assertTrue("Property should be dirty after modification", entity.getProperty("desc").isDirty());

        EntityWithMoney newEntity = entityFactory.newByKey(EntityWithMoney.class, "some key");
        assertTrue("New entity should be dirty", newEntity.isDirty());
        assertEquals("All properties should be dirty after entity creation.", 5, newEntity.getDirtyProperties().size());

        final Date originalDate = newEntity.getDateTimeProperty();
        newEntity.setDateTimeProperty(new Date());
        assertTrue("Property should be dirty after modification", newEntity.getProperty("dateTimeProperty").isDirty());

        newEntity.setDateTimeProperty(originalDate);
        assertTrue("Property should remain dirty after modification to original value on a non persisted entity", newEntity.getProperty("dateTimeProperty").isDirty());

        newEntity = dao.save(newEntity);
        assertFalse("New entity should not be dirty after save", newEntity.isDirty());
    }

    @Test
    public void test_original_state_can_be_restored_with_dirty_check() {
        final EntityWithMoney entity = dao.findByKey("key1");
        assertFalse("Entity should not be dirty after retrieval", entity.isDirty());
        final String originalDesc = entity.getDesc();
        entity.setDesc("modified desc");
        final Money originalMoney = entity.getMoney();
        entity.setMoney(new Money("23.25"));
        assertTrue("Should have become dirty after modification.", entity.isDirty());

        entity.restoreToOriginal();

        assertEquals("Could not restore to original.", originalMoney, entity.getMoney());
        assertEquals("Could not restore to original.", originalDesc, entity.getDesc());
        assertFalse("Should have become not-dirty after restoration to original.", entity.isDirty());
    }

    @Test
    public void setting_original_property_value_after_change_should_make_entity_not_dirty() {
        EntityWithMoney entity = dao.findByKey("key1");
        entity.setMoney(new Money("23.25"));
        assertTrue(entity.isDirty());

        entity = dao.save(entity);
        entity.setMoney(new Money("26.25"));
        assertTrue(entity.isDirty());
        entity.setMoney(new Money("23.25"));
        assertFalse(entity.isDirty());
    }

    public void test_date_time_support() {
        EntityWithMoney entity = dao.findByKey("key1");
        // test read date time
        final DateTime dateTime = new DateTime(entity.getDateTimeProperty()); // 2009-03-01 11:00:55
        assertEquals("Incorrect year.", 2009, dateTime.getYear());
        assertEquals("Incorrect month.", 03, dateTime.getMonthOfYear());
        assertEquals("Incorrect day.", 01, dateTime.getDayOfMonth());
        assertEquals("Incorrect hour.", 11, dateTime.getHourOfDay());
        assertEquals("Incorrect minute.", 0, dateTime.getMinuteOfHour());
        assertEquals("Incorrect second.", 55, dateTime.getSecondOfMinute());
        assertEquals("Incorrect millisecond.", 0, dateTime.getMillisOfSecond());
        // test save date time
        entity.setDateTimeProperty(new DateTime(2009, 03, 01, 12, 0, 55, 300).toDate());
        dao.save(entity);
        hibernateUtil.getSessionFactory().getCurrentSession().flush();
        hibernateUtil.getSessionFactory().getCurrentSession().clear();
        entity = dao.findByKey("key1");
        final DateTime updatedDateTime = new DateTime(entity.getDateTimeProperty());
        assertEquals("Incorrect year.", 2009, updatedDateTime.getYear());
        assertEquals("Incorrect month.", 03, updatedDateTime.getMonthOfYear());
        assertEquals("Incorrect day.", 01, updatedDateTime.getDayOfMonth());
        assertEquals("Incorrect hour.", 12, updatedDateTime.getHourOfDay());
        assertEquals("Incorrect minute.", 0, updatedDateTime.getMinuteOfHour());
        assertEquals("Incorrect second.", 55, updatedDateTime.getSecondOfMinute());
        assertEquals("Incorrect millisecond.", 300, updatedDateTime.getMillisOfSecond());
        // test save null date time
        entity.setDateTimeProperty(null);
        dao.save(entity);
        hibernateUtil.getSessionFactory().getCurrentSession().flush();
        hibernateUtil.getSessionFactory().getCurrentSession().clear();
        entity = dao.findByKey("key1");
        assertNull("Date property should habve been null.", entity.getDateTimeProperty());
        // test equality
        final Date prevDate = new Date();
        entity.setDateTimeProperty(prevDate);
        dao.save(entity);
        hibernateUtil.getSessionFactory().getCurrentSession().flush();
        hibernateUtil.getSessionFactory().getCurrentSession().clear();
        entity = dao.findByKey("key1");
        assertEquals("Date values should be equal", prevDate.getTime(), entity.getDateTimeProperty().getTime());
        assertEquals("Date values should be equal", prevDate, entity.getDateTimeProperty());

    }

    @Test
    public void test_entity_version_is_updated_after_save() {
        EntityWithMoney entity = dao.findByKey("key1");

        hibernateUtil.getSessionFactory().getCurrentSession().close();

        assertEquals("Incorrect prev version", Long.valueOf(0), entity.getVersion());
        entity.setDesc("new desc");
        entity = dao.save(entity);
        assertEquals("Incorrect curr version", Long.valueOf(1), entity.getVersion());

        hibernateUtil.getSessionFactory().getCurrentSession().close();

        final EntityWithMoney updatedEntity = dao.findByKey("key1");
        assertEquals("Incorrect prev version", Long.valueOf(1), updatedEntity.getVersion());
    }

    @Test
    public void test_entity_staleness_check() {
        final EntityWithMoney entity = dao.findByKey("key1");
        // update entity to simulate staleness
        entity.setDesc("new desc");
        dao.save(entity);
        hibernateUtil.getSessionFactory().getCurrentSession().close();

        final EntityWithMoney entity2 = dao.findByKey("key1");
        assertTrue("This version should have been recognised as stale.", dao.isStale(entity.getId(), 0L));
        //assertTrue("This version should have been recognised as stale.", dao.isStale(entity.getId(), 1L));
        //assertFalse("This version should have been recognised as current.", dao.isStale(entity.getId(), 2L));
        assertFalse("This version should have been recognised as current.", dao.isStale(entity.getId(), 1L));
    }

    
    @Test
    public void test_optimistic_locking_based_on_versioning_works_for_save() {
        // get entity, which will be modified but not saved
        final EntityWithMoney entity = dao.findByKey("key1");
        assertEquals("Incorrect prev version", Long.valueOf(0), entity.getVersion());
        entity.setDesc("new desc");

        hibernateUtil.getSessionFactory().getCurrentSession().close();

        // retrieve another instance of the same entity, modify and save -- this should emulate concurrent modification
        final EntityWithMoney anotherEntityInstance = dao.findByKey("key1");
        anotherEntityInstance.setDesc("another desc");

        hibernateUtil.getSessionFactory().getCurrentSession().close();

        dao.save(anotherEntityInstance);

        hibernateUtil.getSessionFactory().getCurrentSession().close();

        // try to save previously retrieved entity, which should fail due to concurrent modification
        try {
            dao.save(entity);
            fail("Should have failed due to optimistic locking.");
        } catch (final Exception e) {
        }
    }

    // FIXME
    //    /**
    //     * This test case covers a situation with validation being performed at the client side based on stale data.
    //     */
    //    @SuppressWarnings("unchecked")
    //    public void test_that_validation_upon_save_worx() {
    //	// retrieve wagon with all its slots and a bogie to be set into a slot
    //	final EntityResultQueryModel<Wagon> wagonModel = select(Wagon.class).where().prop("key").eq().val("WAGON1").model();
    //	final OrderingModel orderBy = orderBy().prop("key").asc().model();
    //	final fetch<Wagon> fetchModel = fetch(Wagon.class).with("wagonClass").with("slots", fetch(WagonSlot.class).with("bogie"));
    //	final Wagon wagon = injector.getInstance(IWagonDao2.class).getEntity(query(wagonModel, orderBy, fetchModel));
    //	final Bogie bogie = injector.getInstance(IBogieDao2.class).findById(5L, fetch(Bogie.class).with("rotableClass"));
    //
    //	// let's close the session in order to mimic proper client/server (and web) communication
    //	hibernateUtil.getSessionFactory().getCurrentSession().close();
    //
    //	// set bogie into the firts wagon's slot
    //	final WagonSlot slot = wagon.getSlot(1);
    //	slot.setBogie(bogie); // no validation yet since there is not validator at this stage
    //
    //	// let's create a validator for property WagonSlot.bogie, which always fail the setter
    //	// since it is only introduced here the only place it will actually be invoked is during the DAO save method invokation
    //	final Result failResult = new Result(slot, new IllegalArgumentException("Bogie class is not compatible with slot."));
    //	config.getDomainValidationConfig().setValidator(WagonSlot.class, "bogie", new IBeforeChangeEventHandler<Object>() {
    //	    @Override
    //	    public Result handle(final MetaProperty property, final Object newValue, final Object oldValue, final Set<Annotation> mutatorAnnotations) {
    //		return failResult;
    //	    }
    //	});
    //	// try to save slot, which already has domain validator that always fails
    //	try {
    //	    injector.getInstance(WagonSlotDao.class).save(slot);
    //	    fail("WagonSlot.bogie validator should have been prevented successful saving.");
    //	} catch (final Exception ex) {
    //	    assertEquals("Unexpected exception has been thrown", failResult, ex);
    //	}
    //	// let's remove validator in order not to affect any other tests
    //	config.getDomainValidationConfig().setValidator(WagonSlot.class, "bogie", null);
    //    }

    @Test
    public void test_transaction_date_property_for_previously_persisted_entity_is_not_reassigned_with_save() {
        final EntityWithMoney entity = dao.findByKey("key1");
        assertNull("Test pre-condition is invalid -- transDate should be null.", dao.findByKey("key1").getTransDate());
        dao.save(entity);
        assertNull("Transaction property should not have been updated for an existing property.", dao.findByKey("key1").getTransDate());
    }

    @Test
    public void test_transaction_date_property_for_new_entity_gets_auto_assigned_with_save() {
        final EntityWithMoney newEntity = entityFactory.newByKey(EntityWithMoney.class, "new entity");
        assertNull("Test pre-condition is invalid -- transDate should be null.", newEntity.getTransDate());
        newEntity.setMoney(new Money("12")); // required property -- has to be set
        dao.save(newEntity);
        assertNotNull("transDate should have been assigned.", dao.findByKey("new entity").getTransDate());
    }
 
    @Test
    public void test_already_assigned_transaction_date_property_for_new_entity_does_not_get_repopulated_with_save() {
        final EntityWithMoney newEntity = entityFactory.newByKey(EntityWithMoney.class, "new entity");
        final Date date = new DateTime(2009, 01, 01, 0, 0, 0, 0).toDate();
        newEntity.setTransDate(date);
        newEntity.setMoney(new Money("12")); // required property -- has to be set
        dao.save(newEntity);
        assertEquals("transDate should not have been re-assigned.", date, dao.findByKey("new entity").getTransDate());
    }

    @Override
    protected String[] getDataSetPathsForInsert() {
        return new String[] {//
        "src/test/resources/data-files/entity-with-dynamic-composite-key-test-case.flat.xml", //
                "src/test/resources/data-files/hibernate-query-test-case.flat.xml" };
    }

}
