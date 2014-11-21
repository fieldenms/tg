package ua.com.fielden.platform.entity.query;

import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetch;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.orderBy;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.sample.domain.TgOrgUnit5;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.sample.domain.TgVehicleMake;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;
import ua.com.fielden.platform.serialisation.api.ProvidedSerialisationClassProvider;
import ua.com.fielden.platform.serialisation.kryo.TgKryo;
import ua.com.fielden.platform.test.CommonTestEntityModuleWithPropertyFactory;

import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * Ensures correct Kryo serialisation of EQL related types.
 * 
 * @author TG Team
 * 
 */
public class SerialisationTest {

    private final Module module = new CommonTestEntityModuleWithPropertyFactory();
    private final Injector injector = new ApplicationInjectorFactory().add(module).getInjector();
    private final EntityFactory factory = injector.getInstance(EntityFactory.class);

    private final List<Class<?>> types = new ArrayList<Class<?>>();
    {
        types.add(TgVehicle.class);
        types.add(TgVehicleMake.class);
        types.add(TgVehicleModel.class);
        types.add(TgOrgUnit5.class);
    }

    private final TgKryo kryoWriter = new TgKryo(factory, new ProvidedSerialisationClassProvider(types.toArray(new Class[] {})));
    private final TgKryo kryoReader = new TgKryo(factory, new ProvidedSerialisationClassProvider(types.toArray(new Class[] {})));

    @Test
    public void seralisation_of_simple_query_should_not_have_failed() {
        final EntityResultQueryModel<TgVehicle> original = select(TgVehicle.class).model();
        final EntityResultQueryModel<TgVehicle> restored = serialiseAndRestore(original);

        assertEquals(original, restored);
    }

    @Test
    public void seralisation_of_comlex_query_with_aggregation_should_not_have_failed() {
        final EntityResultQueryModel<TgVehicle> original = select(TgVehicle.class).where().prop("model.make.key").in().model(select(TgVehicleMake.class).where().prop("key").like().val("RR%").model()).and(). //
        prop("price.amount").ge().param("amount").and(). //
        exists(select(TgVehicle.class).where().prop("replacedBy.station").isNotNull().yield().avgOf().beginExpr().prop("purchasePrice.amount").add().prop("price.amount").endExpr().as("res1"). //
        yield().expr(expr().prop("key").model()).as("res2").modelAsAggregate()). //
        model();
        final EntityResultQueryModel<TgVehicle> restored = serialiseAndRestore(original);

        assertEquals(original, restored);
    }

    @Test
    public void seralisation_of_comlex_query_with_primitive_result_should_not_have_failed() {
        final EntityResultQueryModel<TgVehicle> original = select(TgVehicle.class).where().prop("model.make.key").in().model(select(TgVehicleMake.class).where().prop("key").like().val("RR%").model()).and(). //
        prop("price.amount").ge().param("amount").and(). //
        exists(select(TgVehicle.class).where().prop("replacedBy.station").isNotNull().yield().avgOf().beginExpr().prop("purchasePrice.amount").add().prop("price.amount").endExpr().modelAsPrimitive()). //
        model();
        final EntityResultQueryModel<TgVehicle> restored = serialiseAndRestore(original);

        assertEquals(original, restored);
    }

    @Test
    public void seralisation_of_comlex_query_with_nested_entity_query_should_not_have_failed() {
        final EntityResultQueryModel<TgVehicle> original = select(TgVehicle.class).where().prop("model.make.key").in().model(select(TgVehicle.class).where().prop("key").like().val("RR%").groupBy().prop("model.make").yield().prop("model.make").modelAsEntity(TgVehicleMake.class)).model();
        final EntityResultQueryModel<TgVehicle> restored = serialiseAndRestore(original);

        assertEquals(original, restored);
    }

    @Test
    public void aggregates_query_model_serialisation_should_have_worked() {
        final AggregatedResultQueryModel original = select(TgVehicle.class).where().prop("replacedBy.station").isNotNull().yield().avgOf().beginExpr().prop("purchasePrice.amount").add().prop("price.amount").endExpr().as("res1"). //
        yield().expr(expr().prop("key").model()).as("res2").modelAsAggregate();

        final AggregatedResultQueryModel restored = serialiseAndRestore(original);

        assertEquals(original, restored);
    }

    @Test
    public void serialisation_of_query_execution_model_should_not_fail() {
        final EntityResultQueryModel<TgVehicle> originalModel = select(TgVehicle.class).where().prop("model.make.key").in().model(select(TgVehicle.class).where().prop("key").like().val("RR%").groupBy().prop("model.make").yield().prop("model.make").modelAsEntity(TgVehicleMake.class)).model();
        final fetch<TgVehicle> originalFetch = fetch(TgVehicle.class).with("model").with("station");
        final OrderingModel originalOrderBy = orderBy().prop("key").asc().model();

        final QueryExecutionModel<TgVehicle, ?> original = from(originalModel).with(originalFetch).with(originalOrderBy).with("param1", 125).model();
        final QueryExecutionModel<TgVehicle, ?> restored = serialiseAndRestore(original);

        assertEquals(original, restored);
    }

    @Test
    public void serialisation_of_aggregates_query_execution_model_should_not_fail() {
        final AggregatedResultQueryModel originalModel = select(TgVehicle.class).where().prop("key").like().val("RR%").groupBy().prop("model.make").yield().prop("model.make").as("make").modelAsAggregate();
        final OrderingModel originalOrderBy = orderBy().prop("make.key").asc().model();

        final QueryExecutionModel<EntityAggregates, ?> original = from(originalModel).with(originalOrderBy).with("param1", 125).model();
        final QueryExecutionModel<EntityAggregates, ?> restored = serialiseAndRestore(original);

        assertEquals(original, restored);
    }

    /**
     * A convenient method to serialise and deserialise the passed in model allocating a buffer of the specified size.
     */
    private <T extends AbstractEntity<?>> EntityResultQueryModel<T> serialiseAndRestore(final EntityResultQueryModel<T> originalQuery) {
        try {
            return kryoReader.deserialise(kryoWriter.serialise(originalQuery), originalQuery.getClass());
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * A convenient method to serialise and deserialise the passed in model allocating a buffer of the specified size.
     */
    private AggregatedResultQueryModel serialiseAndRestore(final AggregatedResultQueryModel originalQuery) {
        try {
            return kryoReader.deserialise(kryoWriter.serialise(originalQuery), originalQuery.getClass());
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * A convenient method to serialise and deserialise the passed in model allocating a buffer of the specified size.
     */
    private <T extends AbstractEntity<?>> QueryExecutionModel<T, ?> serialiseAndRestore(final QueryExecutionModel<T, ?> originalQuery) {
        try {
            return kryoReader.deserialise(kryoWriter.serialise(originalQuery), originalQuery.getClass());
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
    }

}
