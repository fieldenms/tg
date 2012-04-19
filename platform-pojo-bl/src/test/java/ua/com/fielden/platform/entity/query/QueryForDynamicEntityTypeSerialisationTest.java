package ua.com.fielden.platform.entity.query;

import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.sample.domain.TgOrgUnit5;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.sample.domain.TgVehicleMake;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;
import ua.com.fielden.platform.serialisation.impl.ProvidedSerialisationClassProvider;
import ua.com.fielden.platform.serialisation.impl.TgKryo;
import ua.com.fielden.platform.test.CommonTestEntityModuleWithPropertyFactory;

import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * Ensures correct serialisation/deserialisation of EQL when created for dynamically generated entity types.
 *
 * @author TG Team
 *
 */
public class QueryForDynamicEntityTypeSerialisationTest {

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

    private final TgKryo kryoWriter = new TgKryo(factory, new ProvidedSerialisationClassProvider(types.toArray(new Class[]{})));
    private final TgKryo kryoReader = new TgKryo(factory, new ProvidedSerialisationClassProvider(types.toArray(new Class[]{})));

    @Test
    public void seralisation_of_simple_query_should_not_have_failed() {
	final EntityResultQueryModel<TgVehicle> original =  select(TgVehicle.class).model();
	final EntityResultQueryModel<TgVehicle> restored = serialiseAndRestore(original);

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
