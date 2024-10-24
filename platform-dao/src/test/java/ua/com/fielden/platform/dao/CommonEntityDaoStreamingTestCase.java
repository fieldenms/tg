package ua.com.fielden.platform.dao;

import org.junit.Test;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
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
import ua.com.fielden.platform.utils.IUniversalConstants;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.*;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.*;
import static ua.com.fielden.platform.types.try_wrapper.TryWrapper.Try;

/**
 * This test case ensures correct implementation of the db-driven companion streaming functionality.
 *
 * @author TG Team
 *
 */
public class CommonEntityDaoStreamingTestCase extends AbstractDaoTestCase {

    @Test
    @SessionRequired
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
    @SessionRequired
    public void there_is_streaming_API_with_default_fetch_size() {
        try (final Stream<EntityWithMoney> stream = co$(EntityWithMoney.class).stream(from(select(EntityWithMoney.class).model()).model())) {
            assertEquals("Incorrect number of entities in the stream", 4, stream.count());
        }
    }

    @Test
    @SessionRequired
    public void streaming_based_on_ordered_qem_has_the_intended_order_of_elements() {
        final EntityResultQueryModel<EntityWithMoney> query = select(EntityWithMoney.class).model();
        final OrderingModel orderBy = orderBy().prop("key").asc().model();
        final QueryExecutionModel<EntityWithMoney, EntityResultQueryModel<EntityWithMoney>> qem = from(query).with(orderBy).model();

        final Iterator<EntityWithMoney> iterator = co$(EntityWithMoney.class).getAllEntities(qem).iterator();
        try (final Stream<EntityWithMoney> stream = co$(EntityWithMoney.class).stream(qem, 2)) {
            stream.forEach(entity -> assertEquals(iterator.next(), entity));
        }
    }

    @Test
    @SessionRequired
    public void streaming_based_on_conditional_qem_contain_only_matching_entities() {
        final EntityResultQueryModel<EntityWithMoney> query = select(EntityWithMoney.class)
                .where().prop("money.amount").ge().val(new BigDecimal("30.00"))//
                .model();
        final QueryExecutionModel<EntityWithMoney, EntityResultQueryModel<EntityWithMoney>> qem = from(query).model();

        try (final Stream<EntityWithMoney> stream = co$(EntityWithMoney.class).stream(qem, 2)) {
            assertEquals("Incorrect number of entities in the stream", co$(EntityWithMoney.class).count(query), stream.count());
        }
    }

    @Test
    @SessionRequired
    public void stream_is_not_parallel() {
        try (final Stream<EntityWithMoney> streamBy3 = co$(EntityWithMoney.class).stream(from(select(EntityWithMoney.class).model()).model(), 2)) {
            assertFalse("The stream should not be parallel", streamBy3.isParallel());
        }
    }

    @Test
    @SessionRequired
    public void stream_cannot_be_processed_again_after_a_terminal_operation() {
        final Either<Exception, Long> result = Try(() -> {
            try (final Stream<EntityWithMoney> stream = co$(EntityWithMoney.class).stream(from(select(EntityWithMoney.class).model()).model(), 2)) {
                // the first terminal operation
                stream.forEach(e -> e.getMoney());
                // try to run another terminal operation, which should throw
                return stream.count();
            }});
        
        assertTrue(result.isLeft());
        assertTrue(result.asLeft().value() instanceof IllegalStateException);
        assertEquals("stream has already been operated upon or closed", result.asLeft().value().getMessage());
    }

    @Test
    public void streams_created_outside_of_existing_db_session_are_responsible_for_opening_and_closing_their_own_session() {
        final EntityWithMoneyDao co = co$(EntityWithMoney.class);
        assertNull("No session is expected.", co.getSessionUnsafe());
        try (final Stream<EntityWithMoney> stream = co.stream(from(select(EntityWithMoney.class).model()).model())) {
            assertNotNull("A new session is expected.", co.getSessionUnsafe());
            assertTrue("Session should be open.", co.getSessionUnsafe().isOpen());
        }
        assertFalse("Session should already be closed", co.getSessionUnsafe().isOpen());
    }

    @Test
    public void closing_of_derived_streams_closes_the_base_stream_and_session() {
        final EntityWithMoneyDao co = co$(EntityWithMoney.class);
        final Stream<EntityWithMoney> dataStream = co.stream(from(select(EntityWithMoney.class).model()).model());
        final Stream<Money> moneyStream = dataStream.map(e -> e.getMoney());
        
        assertTrue("Session should still be open.", co.getSessionUnsafe().isOpen());
        
        moneyStream.close();
        
        assertFalse("Session should already be closed", co.getSessionUnsafe().isOpen());
    }

    @Test
    public void terminal_op_on_data_stream_with_its_own_session_performed_outside_try_with_resources_does_not_close_that_session() {
        final EntityWithMoneyDao co = co$(EntityWithMoney.class);
        final Stream<EntityWithMoney> dataStream = co.stream(from(select(EntityWithMoney.class).model()).model());
        
        assertTrue("Session should still be open.", co.getSessionUnsafe().isOpen());
        dataStream.count();
        assertTrue("Session should still be open.", co.getSessionUnsafe().isOpen());
        
        dataStream.close();
        
        assertFalse("Session should already be closed", co.getSessionUnsafe().isOpen());
    }

    @Test
    public void terminal_op_on_data_stream_with_its_own_session_performed_inside_try_with_resources_does_not_close_that_session() {
        final EntityWithMoneyDao co = co$(EntityWithMoney.class);
        
        final Map<Boolean, List<EntityWithMoney>> partition;
        try (final Stream<EntityWithMoney> dataStream = co.stream(from(select(EntityWithMoney.class).model()).model());) {
            partition = dataStream.collect(Collectors.partitioningBy(e -> e.getMoney().getAmount().doubleValue() >= 30));
            assertTrue("Session should still be open.", co.getSessionUnsafe().isOpen());
        }
        assertFalse("Session should already be closed", co.getSessionUnsafe().isOpen());

        assertEquals(2, partition.size());
        assertEquals(3, partition.get(true).size());
        assertEquals(1, partition.get(false).size());
    }

    @Test
    @SessionRequired
    public void streams_that_are_used_within_an_existing_db_session_should_not_close_that_session() {
        assertTrue("The test should start with an open session.", getSession().isOpen());

        final EntityResultQueryModel<EntityWithMoney> query = select(EntityWithMoney.class).model();
        final EntityWithMoneyDao co = co$(EntityWithMoney.class);
        
        // The following code uses a stream in try-with-resources and makes an additional query after the stream is closed, expecting that the current session remains open.
        long result = 0;
        try(final var stream = co.stream(from(query).model())) {
            assertEquals("Stream should happen in the same session as the test itself.", getSession(), co.getSessionUnsafe());
            result = result + stream.count();
        }
        assertTrue("Session should still be open.", co.getSessionUnsafe().isOpen());
        result = result + co.count(query); // query in still in the scope of the same session

        assertEquals(co$(EntityWithMoney.class).count(query) * 2, result);
    }

    @Test
    @SessionRequired
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
    @SessionRequired
    public void streaming_of_aggregates_with_custom_fetch_size_is_supported() {
        final AggregatedResultQueryModel qry = select(EntityWithMoney.class).
                groupBy().prop("desc").
                yield().prop("desc").as("distinctDesc").
                yield().countAll().as("kount").
                modelAsAggregate();
        final OrderingModel orderByDesc = orderBy().yield("distinctDesc").asc().model();
        
        final List<EntityAggregates> values;
        try (final Stream<EntityAggregates> stream = co(EntityAggregates.class).stream(from(qry).with(orderByDesc).model(), 1)) {
            values = stream.toList();
        }
        assertEquals(2, values.size());
        assertEquals("desc X", values.get(0).get("distinctDesc"));
        assertEquals(2, values.get(0).<Number>get("kount").intValue());
        assertEquals("desc Y", values.get(1).get("distinctDesc"));
        assertEquals(2, values.get(1).<Number>get("kount").intValue());
    }
    
    @Override
    @SessionRequired
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
