package ua.com.fielden.web.test;

import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyAttribute.NO_ATTR;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.restlet.Restlet;
import org.restlet.routing.Router;

import ua.com.fielden.platform.dao.IGeneratedEntityController;
import ua.com.fielden.platform.domaintree.IDomainTreeManager.IDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.impl.DomainTreeEnhancer.ByteArray;
import ua.com.fielden.platform.domaintree.testing.DomainTreeManagerAndEnhancer1;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.rao.GeneratedEntityRao;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.api.impl.ProvidedSerialisationClassProvider;
import ua.com.fielden.platform.serialisation.api.impl.Serialiser;
import ua.com.fielden.platform.test.CommonTestEntityModuleWithPropertyFactory;
import ua.com.fielden.platform.test.DbDrivenTestCase;
import ua.com.fielden.platform.web.resources.RouterHelper;
import ua.com.fielden.platform.web.test.WebBasedTestCase;
import ua.com.fielden.web.entities.IInspectedEntityDao;
import ua.com.fielden.web.entities.InspectedEntity;

import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * Provides a unit test to ensure correct operation of EQL with generated types when it comes to calculation of totals.
 * 
 * @author TG Team
 * 
 */
public class WebResourceForGeneratedEntityTotalsTestCase extends WebBasedTestCase {
    private final IGeneratedEntityController rao = new GeneratedEntityRao(config.restClientUtil());

    private final Module module = new CommonTestEntityModuleWithPropertyFactory();
    private final Injector injector = new ApplicationInjectorFactory().add(module).getInjector();
    private final EntityFactory factory = injector.getInstance(EntityFactory.class);

    private final ISerialiser serialiser = new Serialiser(factory, new ProvidedSerialisationClassProvider(new Class[] { InspectedEntity.class }));
    private final Set<Class<?>> rootTypes = new HashSet<Class<?>>() {
        {
            add(InspectedEntity.class);
        }
    };
    private IDomainTreeManagerAndEnhancer dtm;
    private List<byte[]> binaryTypes;
    private Class<? extends AbstractEntity<?>> type;

    @Override
    protected String[] getDataSetPaths() {
        return new String[] { "src/test/resources/data-files/query-for-generated-type-web-resource-test-case.flat.xml" };
    }

    @Override
    public synchronized Restlet getInboundRoot() {
        final Router router = new Router(getContext());

        final RouterHelper helper = new RouterHelper(DbDrivenTestCase.injector, DbDrivenTestCase.entityFactory);
        helper.register(router, IInspectedEntityDao.class);
        helper.registerAggregates(router);
        helper.registerGeneratedTypeResources(router);

        return router;
    }

    @Override
    public void setUp() {
        super.setUp();

        dtm = new DomainTreeManagerAndEnhancer1(serialiser, rootTypes);

        // totalProperty1
        dtm.getEnhancer().addCalculatedProperty(InspectedEntity.class, "", "SUM(intProperty) * 2", "intProperty Sum", "desc", NO_ATTR, "intProperty");
        // totalProperty2
        dtm.getEnhancer().addCalculatedProperty(InspectedEntity.class, "", "MAX(intProperty) - MIN(intProperty)", "minimaxDiff", "desc", NO_ATTR, "intProperty");
        dtm.getEnhancer().apply();
        final List<ByteArray> byteListOfTypes = dtm.getEnhancer().getManagedTypeArrays(InspectedEntity.class);
        binaryTypes = toByteArray(byteListOfTypes);
        type = (Class<? extends AbstractEntity<?>>) dtm.getEnhancer().getManagedType(InspectedEntity.class);
        rao.setEntityType(type);

    }

    private List<byte[]> toByteArray(final List<ByteArray> list) {
        final List<byte[]> byteArray = new ArrayList<byte[]>(list.size());
        for (final ByteArray array : list) {
            byteArray.add(array.getArray());
        }
        return byteArray;
    }

    @Test
    public void test_selection_of_totals() {
        final EntityResultQueryModel model = select(type).where().prop("intProperty").lt().val(30).//
        yield().prop("intPropertySum").as("intPropertySum").//
        yield().prop("minimaxDiff").as("minimaxDiff").//
        modelAsEntity(type);
        final IPage firstPage = rao.firstPage(from(model).with(fetchAll(type).with("intPropertySum").with("minimaxDiff")).model(), 15, binaryTypes);

        assertEquals("Incorrect value of returned items.", 1, firstPage.data().size());
        final AbstractEntity instance = (AbstractEntity) firstPage.data().get(0);
        assertEquals("Incorrect value.", 1020, instance.get("intPropertySum"));
        assertEquals("Incorrect value.", 10, instance.get("minimaxDiff"));
    }
}
