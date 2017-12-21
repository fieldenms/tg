package ua.com.fielden.platform.dao;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.orderBy;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.types.try_wrapper.TryWrapper.Try;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.persistence.types.EntityWithMoney;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.test.ioc.UniversalConstantsForTesting;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.types.either.Either;
import ua.com.fielden.platform.types.either.Left;
import ua.com.fielden.platform.utils.IUniversalConstants;

/**
 * This test case ensures correct implementation of the db-driven companion streaming functionality.
 *
 * @author TG Team
 *
 */
public class CommonEntityDaoStreamingTestCase extends AbstractDaoTestCase {

    @Test
    public void streaming_of_unconditional_query_result_with_different_fetch_size_contains_all_available_entities_at_all_times() {
        final EntityResultQueryModel<EntityWithMoney> query = select(EntityWithMoney.class).model();
        final QueryExecutionModel<EntityWithMoney, EntityResultQueryModel<EntityWithMoney>> qem = from(query).model();

        try (final Stream<EntityWithMoney> streamBy3 = co$(EntityWithMoney.class).stream(qem, 3)) {
            assertEquals("Incorrect number of entities in the stream", co$(EntityWithMoney.class).count(query), streamBy3.count());
        }
        
        try (final Stream<EntityWithMoney> streamBy1 = co$(EntityWithMoney.class).stream(qem, 1)) {
            assertFalse("The stream should not be parallel", streamBy1.isParallel());
            assertEquals("Incorrect number of entities in the stream", co$(EntityWithMoney.class).count(query), streamBy1.count());
        }
    }

    @Test
    public void there_is_streaming_API_with_default_fetch_size() {
        try (final Stream<EntityWithMoney> stream = co$(EntityWithMoney.class).stream(from(select(EntityWithMoney.class).model()).model())) {
            assertEquals("Incorrect number of entities in the stream", 4, stream.count());
        }
    }

    @Test
    public void streaming_based_on_ordered_qem_should_have_the_same_traversal_order() {
        final EntityResultQueryModel<EntityWithMoney> query = select(EntityWithMoney.class).model();
        final OrderingModel orderBy = orderBy().prop("key").asc().model();
        final QueryExecutionModel<EntityWithMoney, EntityResultQueryModel<EntityWithMoney>> qem = from(query).with(orderBy).model();

        final Iterator<EntityWithMoney> iterator = co$(EntityWithMoney.class).getAllEntities(qem).iterator();
        try (final Stream<EntityWithMoney> stream = co$(EntityWithMoney.class).stream(qem, 2)) {
            stream.forEach(entity -> assertEquals(iterator.next(), entity));
        }
    }

    @Test
    public void streaming_based_on_conditional_qem_should_contain_only_matching_entities() {
        final EntityResultQueryModel<EntityWithMoney> query = select(EntityWithMoney.class)
                .where().prop("money.amount").ge().val(new BigDecimal("30.00"))//
                .model();
        final QueryExecutionModel<EntityWithMoney, EntityResultQueryModel<EntityWithMoney>> qem = from(query).model();

        try (final Stream<EntityWithMoney> stream = co$(EntityWithMoney.class).stream(qem, 2)) {
            assertEquals("Incorrect number of entities in the stream", co$(EntityWithMoney.class).count(query), stream.count());
        }
    }

    @Test
    public void stream_should_not_be_parallel() {
        try (final Stream<EntityWithMoney> streamBy3 = co$(EntityWithMoney.class).stream(from(select(EntityWithMoney.class).model()).model(), 2)) {
            assertFalse("The stream should not be parallel", streamBy3.isParallel());
        }
    }

    @Test
    public void stream_should_not_be_accecible_once_traversed() {
        final Either<Exception, Long> result = Try(() -> {
            try (final Stream<EntityWithMoney> stream = co$(EntityWithMoney.class).stream(from(select(EntityWithMoney.class).model()).model(), 2)) {
                // consume the stream by traversing it
                stream.forEach(e -> e.getMoney()/* basically do nothing*/);
                // try to consume the stream again by counting the number of elements in it
                return stream.count();
            }});
        
        assertTrue(result instanceof Left);
        assertEquals("stream has already been operated upon or closed", ((Left<Exception, Long>) result).value.getMessage());
    }

    @Test
    public void streams_that_are_used_outside_an_existing_db_session_are_responsible_for_its_closing() {
        final EntityWithMoneyDao co = co$(EntityWithMoney.class);
        try (final Stream<EntityWithMoney> stream = co.stream(from(select(EntityWithMoney.class).model()).model())) {
            assertTrue("Session should still be open.", co.getSession().isOpen());
        }
        assertFalse("Session should already be closed", co.getSession().isOpen());
    }

    @Test
    public void closing_of_derived_streams_closes_the_base_stream_and_session() {
        final EntityWithMoneyDao co = co$(EntityWithMoney.class);
        final Stream<EntityWithMoney> dataStream = co.stream(from(select(EntityWithMoney.class).model()).model());
        final Stream<Money> moneyStream = dataStream.map(e -> e.getMoney());
        
        assertTrue("Session should still be open.", co.getSession().isOpen());
        
        moneyStream.close();
        
        assertFalse("Session should already be closed", co.getSession().isOpen());
    }

    @Test
    public void counting_data_in_stream_does_not_close_it() {
        final EntityWithMoneyDao co = co$(EntityWithMoney.class);
        final Stream<EntityWithMoney> dataStream = co.stream(from(select(EntityWithMoney.class).model()).model());
        
        assertTrue("Session should still be open.", co.getSession().isOpen());
        dataStream.count();
        assertTrue("Session should still be open.", co.getSession().isOpen());
        
        dataStream.close();
        
        assertFalse("Session should already be closed", co.getSession().isOpen());
    }

    @Test
    public void collecting_data_from_stream_does_not_close_it() {
        final EntityWithMoneyDao co = co$(EntityWithMoney.class);
        
        final Map<Boolean, List<EntityWithMoney>> partition;
        try (final Stream<EntityWithMoney> dataStream = co.stream(from(select(EntityWithMoney.class).model()).model());) {
            partition = dataStream.collect(Collectors.partitioningBy(e -> e.getMoney().getAmount().doubleValue() >= 30));
            assertTrue("Session should still be open.", co.getSession().isOpen());
        }
        
        assertEquals(2, partition.size());
        assertEquals(3, partition.get(true).size());
        assertEquals(1, partition.get(false).size());
        
        assertFalse("Session should already be closed", co.getSession().isOpen());
    }

    @Test
    public void streams_that_are_used_within_an_existing_db_session_should_not_close_it() {
        final EntityResultQueryModel<EntityWithMoney> query = select(EntityWithMoney.class).model();
        final EntityWithMoneyDao co = co$(EntityWithMoney.class);
        
        // the following method uses a stream and make additional query after closing the stream
        // if the stream does not close the current session then that query should succeed
        final long result = co.streamProcessingWithinTransaction(query);
        
        assertEquals(co$(EntityWithMoney.class).count(query) * 2, result);
        assertFalse("Session should already be closed", co.getSession().isOpen());
    }

    @Test
    public void streaming_of_aggregates_with_default_fetch_size_is_supported() {
        final AggregatedResultQueryModel qry = select(EntityWithMoney.class).yield().countOfDistinct().prop("desc").as("kount").modelAsAggregate();
        
        final List<EntityAggregates> values;
        try (final Stream<EntityAggregates> stream = co(EntityAggregates.class).stream(from(qry).model())) {
            values = stream.collect(toList());
            
        }
        assertEquals(1, values.size());
        assertEquals(2, values.get(0).<Number>get("kount").intValue());
    }

    @Test
    public void streaming_of_aggregates_with_custom_fetch_size_is_supported() {
        final AggregatedResultQueryModel qry = select(EntityWithMoney.class).
                groupBy().prop("desc").
                yield().prop("desc").as("distinctDesc").
                yield().countAll().as("kount").
                modelAsAggregate();
        final OrderingModel orderByDesc = orderBy().yield("distinctDesc").asc().model();
        
        final List<EntityAggregates> values;
        try (final Stream<EntityAggregates> stream = co(EntityAggregates.class).stream(from(qry).with(orderByDesc).model(), 1)) {
            values = stream.collect(toList());
        }
        assertEquals(2, values.size());
        assertEquals("desc X", values.get(0).get("distinctDesc"));
        assertEquals(2, values.get(0).<Number>get("kount").intValue());
        assertEquals("desc Y", values.get(1).get("distinctDesc"));
        assertEquals(2, values.get(1).<Number>get("kount").intValue());
    }
    
    @Override
    protected void populateDomain() {
        super.populateDomain();
        
        final UniversalConstantsForTesting constants = (UniversalConstantsForTesting) getInstance(IUniversalConstants.class);
        constants.setNow(dateTime("2016-05-17 16:36:57"));
        
        save(new_(User.class, "USER_1").setBase(true).setEmail("USER1@unit-test.software").setActive(true));
        
        save(new_(EntityWithMoney.class, "key1").setMoney(Money.of("20.00")).setDateTimeProperty(date("2009-03-01 11:00:55")).setDesc("desc X"));
        save(new_(EntityWithMoney.class, "key2").setMoney(Money.of("30.00")).setDateTimeProperty(date("2009-03-01 00:00:00")).setDesc("desc X"));
        save(new_(EntityWithMoney.class, "key3").setMoney(Money.of("40.00")).setDesc("desc Y"));
        save(new_(EntityWithMoney.class, "key4").setMoney(Money.of("50.00")).setDateTimeProperty(date("2009-03-01 10:00:00")).setDesc("desc Y"));
    }
    
}
