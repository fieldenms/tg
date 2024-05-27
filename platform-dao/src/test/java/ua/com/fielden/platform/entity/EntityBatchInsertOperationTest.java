package ua.com.fielden.platform.entity;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.types.Colour.BLACK;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.stream.Stream;

import org.junit.Test;

import ua.com.fielden.platform.dao.exceptions.EntityAlreadyExists;
import ua.com.fielden.platform.dao.session.TransactionalExecution;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.entity.query.EntityBatchInsertOperation;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.sample.domain.EntityOne;
import ua.com.fielden.platform.sample.domain.EntityTwo;
import ua.com.fielden.platform.sample.domain.IEntityOne;
import ua.com.fielden.platform.sample.domain.IEntityTwo;
import ua.com.fielden.platform.sample.domain.UnionEntity;
import ua.com.fielden.platform.test.entities.TgEntityWithManyPropTypes;
import ua.com.fielden.platform.test.entities.TgEntityWithManyPropTypesCo;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.types.Hyperlink;
import ua.com.fielden.platform.types.Money;

public class EntityBatchInsertOperationTest extends AbstractDaoTestCase {
    
    private final static String ENTITY_ONE_KEY = "EO 1";
    private final static Integer ENTITY_TWO_KEY = 2;
    
    @Test
    public void batch_insert_operation_works_for_single_enity() {
        final List<TgEntityWithManyPropTypes> entities = createEntitiesForBatchInsert("Ent1");
        assertEquals(1, batchInserEntities(entities, 2));
        assertEqualityForInsertedEntities(entities);
    }
    
    @Test
    public void batch_insert_operation_works_for_single_full_batch() {
        final List<TgEntityWithManyPropTypes> entities = createEntitiesForBatchInsert("Ent1", "Ent2");
        assertEquals(2, batchInserEntities(entities, 2));
        assertEqualityForInsertedEntities(entities);
    }

    @Test
    public void batch_insert_operation_works_for_two_full_batches() {
        final List<TgEntityWithManyPropTypes> entities = createEntitiesForBatchInsert("Ent1", "Ent2", "Ent3", "Ent4");
        assertEquals(4, batchInserEntities(entities, 2));
        assertEqualityForInsertedEntities(entities);
    }

    @Test
    public void batch_insert_operation_works_for_two_full_batches_with_last_batch_not_full() {
        final List<TgEntityWithManyPropTypes> entities = createEntitiesForBatchInsert("Ent1", "Ent2", "Ent3", "Ent4", "Ent5");
        assertEquals(5, batchInserEntities(entities, 2));
        assertEqualityForInsertedEntities(entities);
    }
    
    @Test
    public void batch_insert_operation_fails_while_trying_to_insert_persisted_entities() {
        batchInserEntities(createEntitiesForBatchInsert("Ent1", "Ent2", "Ent3"), 2);
        try {
            batchInserEntities(getInstance(TgEntityWithManyPropTypesCo.class).getAllEntities(from(select(TgEntityWithManyPropTypes.class).model()).with(fetchAll(TgEntityWithManyPropTypes.class)).model()), 2);
            fail("Expected an exception due to an attempt to insert already persisten entity.");
        } catch (final EntityAlreadyExists ex) {
        }
    }
    
    @Test
    public void batch_insert_a_stream_ignores_persistent_entities() {
        final List<TgEntityWithManyPropTypes> fistBatch = createEntitiesForBatchInsert("Ent1", "Ent2", "Ent3");
        batchInserEntitiesAsStream(fistBatch.stream(), 2);
        final List<TgEntityWithManyPropTypes> persistedFirstBatch = getInstance(TgEntityWithManyPropTypesCo.class).getAllEntities(from(select(TgEntityWithManyPropTypes.class).model()).with(fetchAll(TgEntityWithManyPropTypes.class)).model());
        final List<TgEntityWithManyPropTypes> secondBatch = createEntitiesForBatchInsert("Ent4", "Ent5");
        final List<TgEntityWithManyPropTypes> firstAndSecondBatches = Stream.concat(persistedFirstBatch.stream(), secondBatch.stream()).collect(toList());
        assertEquals(2, batchInserEntitiesAsStream(firstAndSecondBatches.stream(), 2));
        assertEqualityForInsertedEntities(firstAndSecondBatches);
    }

    private int batchInserEntities(final List<TgEntityWithManyPropTypes> entities, final int batchSize) {
        final EntityBatchInsertOperation insertOp = new EntityBatchInsertOperation(getInstance(IDomainMetadata.class), () -> getInstance(TransactionalExecution.class));
        return insertOp.batchInsert(entities, batchSize);
    }

    private int batchInserEntitiesAsStream(final Stream<TgEntityWithManyPropTypes> entities, final int batchSize) {
        final EntityBatchInsertOperation insertOp = new EntityBatchInsertOperation(getInstance(IDomainMetadata.class), () -> getInstance(TransactionalExecution.class));
        return insertOp.batchInsert(entities, batchSize);
    }

    private void assertEqualityForInsertedEntities(final List<TgEntityWithManyPropTypes> entities) {
        final List<TgEntityWithManyPropTypes> savedEntities = getInstance(TgEntityWithManyPropTypesCo.class).getAllEntities(from(select(TgEntityWithManyPropTypes.class).model()).with(fetchAll(TgEntityWithManyPropTypes.class)).model());
        assertEquals(entities, savedEntities);
    }

    private List<TgEntityWithManyPropTypes> createEntitiesForBatchInsert(final String ...entitiesKeys) {
        return asList(entitiesKeys).stream().map(e -> createEntityForBatchInsert(e)).collect(toList());
    }

    private TgEntityWithManyPropTypes createEntityForBatchInsert(final String entityKey) {
        final UnionEntity unionEntityValue = co$(UnionEntity.class).new_();
        unionEntityValue.setPropertyTwo(getInstance(IEntityTwo.class).findByKey(ENTITY_TWO_KEY));
        return new_(TgEntityWithManyPropTypes.class, entityKey).
                setEntityProp(getInstance(IEntityOne.class).findByKey(ENTITY_ONE_KEY)).
                setStringProp("someString").
                setBooleanProp(true).
                setBigDecimalProp(new BigDecimal("100.999")).
                setIntegerProp(200).
                setLongProp(100l).
                setColourProp(BLACK).
                setClassProperty(String.class).
                setDateProp(date("2022-02-02 00:00:00")).
                setUtcDateProp(date("2021-01-01 00:00:00")).
                setHyperlinkProp(new Hyperlink("https://fielden.com")).
                setPropertyDescriptorProp(new PropertyDescriptor<>(TgEntityWithManyPropTypes.class, "unionProp")).
                setMoneyUserTypeProp(new Money(new BigDecimal("100"), Currency.getInstance("USD"))).
                setMoneyWithTaxAmountUserTypeProp(new Money(new BigDecimal("2000"), 20, Currency.getInstance("AUD"))).
                setSimpleMoneyTypeProp(new Money("20")).
                setSimplyMoneyWithTaxAmountProp(new Money(new BigDecimal("1000"), 20, Currency.getInstance("AUD"))).
                setSimplyMoneyWithTaxAndExTaxAmountTypeProp(new Money(new BigDecimal("3000"), 15, Currency.getInstance("EUR"))).
                setUnionProp(unionEntityValue);
    }
    
    @Override
    protected void populateDomain() {
        super.populateDomain();
        
        save(new_(EntityOne.class, ENTITY_ONE_KEY));
        save(new_(EntityTwo.class, ENTITY_TWO_KEY));
    }
}
