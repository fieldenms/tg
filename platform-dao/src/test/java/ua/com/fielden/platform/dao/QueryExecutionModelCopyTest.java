package ua.com.fielden.platform.dao;

import static org.junit.Assert.*;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.*;

import org.junit.Test;

import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.persistence.types.EntityWithMoney;

/**
 * This is a test case for method {@link QueryExecutionModel#copy()}.
 * 
 * @author TG Team
 *
 */
public class QueryExecutionModelCopyTest {

    @Test
    public void copying_a_fully_initialised_QEM_results_in_identical_model() {
        final EntityResultQueryModel<EntityWithMoney> query = select(EntityWithMoney.class).model();
        final fetch<EntityWithMoney> fetch = fetch(EntityWithMoney.class).with("key");
        final OrderingModel orderBy = orderBy().prop("key").asc().model();
        final QueryExecutionModel<EntityWithMoney, EntityResultQueryModel<EntityWithMoney>> qem1 = from(query).with(fetch).with(orderBy).lightweight().model();

        final QueryExecutionModel<EntityWithMoney, EntityResultQueryModel<EntityWithMoney>> qem2 = qem1.copy();

        assertEquals(qem1, qem2);
        assertNotEquals(System.identityHashCode(qem1), System.identityHashCode(qem2));
    }

    @Test
    public void copying_QEM_with_query_only_results_in_identical_model() {
        final EntityResultQueryModel<EntityWithMoney> query = select(EntityWithMoney.class).model();
        final QueryExecutionModel<EntityWithMoney, EntityResultQueryModel<EntityWithMoney>> qem1 = from(query).model();

        final QueryExecutionModel<EntityWithMoney, EntityResultQueryModel<EntityWithMoney>> qem2 = qem1.copy();

        assertEquals(qem1, qem2);
        assertNotEquals(System.identityHashCode(qem1), System.identityHashCode(qem2));
    }
    
    @Test
    public void copying_QEM_with_orderBy_results_in_identical_model() {
        final EntityResultQueryModel<EntityWithMoney> query = select(EntityWithMoney.class).model();
        final OrderingModel orderBy = orderBy().prop("key").asc().model();
        final QueryExecutionModel<EntityWithMoney, EntityResultQueryModel<EntityWithMoney>> qem1 = from(query).with(orderBy).model();

        final QueryExecutionModel<EntityWithMoney, EntityResultQueryModel<EntityWithMoney>> qem2 = qem1.copy();

        assertEquals(qem1, qem2);
        assertNotEquals(System.identityHashCode(qem1), System.identityHashCode(qem2));
    }

    @Test
    public void copying_QEM_with_fetch_results_in_identical_model() {
        final EntityResultQueryModel<EntityWithMoney> query = select(EntityWithMoney.class).model();
        final fetch<EntityWithMoney> fetch = fetch(EntityWithMoney.class).with("key");
        final QueryExecutionModel<EntityWithMoney, EntityResultQueryModel<EntityWithMoney>> qem1 = from(query).with(fetch).model();

        final QueryExecutionModel<EntityWithMoney, EntityResultQueryModel<EntityWithMoney>> qem2 = qem1.copy();

        assertEquals(qem1, qem2);
        assertNotEquals(System.identityHashCode(qem1), System.identityHashCode(qem2));
    }
    
    @Test
    public void copying_QEM_with_lightweihgt_results_in_identical_model() {
        final EntityResultQueryModel<EntityWithMoney> query = select(EntityWithMoney.class).model();
        final fetch<EntityWithMoney> fetch = fetch(EntityWithMoney.class).with("key");
        final QueryExecutionModel<EntityWithMoney, EntityResultQueryModel<EntityWithMoney>> qem1 = from(query).with(fetch).lightweight().model();

        final QueryExecutionModel<EntityWithMoney, EntityResultQueryModel<EntityWithMoney>> qem2 = qem1.copy();

        assertEquals(qem1, qem2);
        assertNotEquals(System.identityHashCode(qem1), System.identityHashCode(qem2));
    }
    
    @Test
    public void copying_QEM_with_lightweihgt_provided_results_in_a_different_model() {
        final EntityResultQueryModel<EntityWithMoney> query = select(EntityWithMoney.class).model();
        final fetch<EntityWithMoney> fetch = fetch(EntityWithMoney.class).with("key");
        final QueryExecutionModel<EntityWithMoney, EntityResultQueryModel<EntityWithMoney>> qem1 = from(query).with(fetch).model();

        final QueryExecutionModel<EntityWithMoney, EntityResultQueryModel<EntityWithMoney>> qem2 = qem1.lightweight();

        assertNotEquals(qem1, qem2);
        assertFalse(qem1.isLightweight());
        assertTrue(qem2.isLightweight());
    }

}
