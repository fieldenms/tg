package ua.com.fielden.platform.entity.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.entity.annotation.factory.CalculatedAnnotation;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.reflection.asm.api.NewProperty;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.sample.domain.TgOrgUnit5;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.sample.domain.TgVehicleMake;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;
import ua.com.fielden.platform.serialisation.api.impl.ProvidedSerialisationClassProvider;
import ua.com.fielden.platform.serialisation.api.impl.Serialiser;
import ua.com.fielden.platform.test.CommonTestEntityModuleWithPropertyFactory;
import ua.com.fielden.platform.types.Money;

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

    private final Serialiser kryoWriter = new Serialiser(factory, new ProvidedSerialisationClassProvider(types.toArray(new Class[] {})));
    private final Serialiser kryoReader = new Serialiser(factory, new ProvidedSerialisationClassProvider(types.toArray(new Class[] {})));

    private static final String NEW_PROPERTY_DESC = "Description  for new money property";
    private static final String NEW_PROPERTY_TITLE = "New money property";
    private static final String NEW_PROPERTY_EXPRESSION = "2 * 3 - [integerProp]";
    private static final String NEW_PROPERTY_1 = "newProperty_1";
    private static final String NEW_PROPERTY_2 = "newProperty_2";
    private boolean observed = false;
    private DynamicEntityClassLoader cl;

    private final Calculated calculated = new CalculatedAnnotation().contextualExpression(NEW_PROPERTY_EXPRESSION).newInstance();

    private final NewProperty pd1 = new NewProperty(NEW_PROPERTY_1, Money.class, false, NEW_PROPERTY_TITLE, NEW_PROPERTY_DESC, calculated);
    private final NewProperty pd2 = new NewProperty(NEW_PROPERTY_2, Money.class, false, NEW_PROPERTY_TITLE, NEW_PROPERTY_DESC, calculated);

    @Before
    public void setUp() {
        observed = false;
        cl = DynamicEntityClassLoader.getInstance(ClassLoader.getSystemClassLoader());
    }

    @Test
    public void seralisation_of_simple_query_for_dynamically_genereated_type_should_not_have_failed() throws Exception {
        final Class<? extends AbstractEntity> newType1 = (Class<? extends AbstractEntity>) cl.startModification(TgVehicle.class.getName()).addProperties(pd1).endModification();

        final EntityResultQueryModel q = select(newType1).modelAsEntity(newType1);
        final QueryExecutionModel original = from(q).model();

        final byte[] data = cl.getCachedByteArray(newType1.getName());
        final List<byte[]> listOfClasses = new ArrayList<byte[]>();
        listOfClasses.add(data);

        final DynamicallyTypedQueryContainer container = new DynamicallyTypedQueryContainer(listOfClasses, original);

        final DynamicallyTypedQueryContainer restored = serialiseAndRestore(container);

        assertEquals(container, restored);
        assertEquals(container.hashCode(), restored.hashCode());
        assertEquals(original, restored.getQem());
        assertNotNull(original.getQueryModel().getResultType());
        assertEquals(original.getQueryModel().getResultType(), restored.getQem().getQueryModel().getResultType());
    }

    @Test
    public void seralisation_of_simple_query_for_dynamically_genereated_type_as_part_of_list_should_not_have_failed() throws Exception {
        final Class<? extends AbstractEntity> newType1 = (Class<? extends AbstractEntity>) cl.startModification(TgVehicle.class.getName()).addProperties(pd1).endModification();

        final EntityResultQueryModel q = select(newType1).modelAsEntity(newType1);
        final QueryExecutionModel original = from(q).model();

        final byte[] data = cl.getCachedByteArray(newType1.getName());
        final List<byte[]> listOfClasses = new ArrayList<byte[]>();
        listOfClasses.add(data);

        final DynamicallyTypedQueryContainer container = new DynamicallyTypedQueryContainer(listOfClasses, original);

        final List<Object> requestContent = new ArrayList<Object>();
        requestContent.add(container);
        requestContent.add("value of some other type");

        final List<?> restored = serialiseAndRestore(requestContent);

        assertEquals(requestContent.size(), restored.size());
        assertEquals(requestContent.get(0), restored.get(0));
        assertEquals(container, restored.get(0));
        assertNotNull(original.getQueryModel().getResultType());
        assertEquals(original.getQueryModel().getResultType(), ((DynamicallyTypedQueryContainer) restored.get(0)).getQem().getQueryModel().getResultType());
    }

    /**
     * A convenient method to serialise and deserialise the passed in model allocating a buffer of the specified size.
     */
    private DynamicallyTypedQueryContainer serialiseAndRestore(final DynamicallyTypedQueryContainer originalQuery) {
        try {
            return kryoReader.deserialise(kryoWriter.serialise(originalQuery), DynamicallyTypedQueryContainer.class);
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * A convenient method to serialise and deserialise the passed in model allocating a buffer of the specified size.
     */
    private List<?> serialiseAndRestore(final List<Object> original) {
        try {
            return kryoReader.deserialise(kryoWriter.serialise(original), List.class);
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
    }

}
