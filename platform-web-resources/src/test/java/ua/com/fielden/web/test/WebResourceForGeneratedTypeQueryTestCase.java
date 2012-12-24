package ua.com.fielden.web.test;

import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyAttribute.NO_ATTR;
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
import ua.com.fielden.platform.serialisation.impl.ProvidedSerialisationClassProvider;
import ua.com.fielden.platform.serialisation.impl.TgKryo;
import ua.com.fielden.platform.test.CommonTestEntityModuleWithPropertyFactory;
import ua.com.fielden.platform.test.DbDrivenTestCase;
import ua.com.fielden.platform.web.resources.RouterHelper;
import ua.com.fielden.platform.web.test.WebBasedTestCase;
import ua.com.fielden.web.entities.IInspectedEntityDao;
import ua.com.fielden.web.entities.InspectedEntity;

import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * Provides a unit test to ensure correct interaction with IPage summary model.
 *
 * @author TG Team
 *
 */
public class WebResourceForGeneratedTypeQueryTestCase extends WebBasedTestCase {
    private final IGeneratedEntityController rao = new GeneratedEntityRao(config.restClientUtil());
    private final IInspectedEntityDao dao = DbDrivenTestCase.injector.getInstance(IInspectedEntityDao.class);

    private IPage firstPage;

    private final Module module = new CommonTestEntityModuleWithPropertyFactory();
    private final Injector injector = new ApplicationInjectorFactory().add(module).getInjector();
    private final EntityFactory factory = injector.getInstance(EntityFactory.class);

    private final ISerialiser serialiser = new TgKryo(factory, new ProvidedSerialisationClassProvider(new Class[] {InspectedEntity.class}));
    private final Set<Class<?>> rootTypes = new HashSet<Class<?>>() {
	{
	    add(InspectedEntity.class);
	}
    };
    private IDomainTreeManagerAndEnhancer dtm;

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
	dtm.getEnhancer().addCalculatedProperty(InspectedEntity.class, "", "2 * intProperty", "Calculated property", "desc", NO_ATTR, "intProperty");
	dtm.getEnhancer().apply();
	final List<ByteArray> binaryTypes = dtm.getEnhancer().getManagedTypeArrays(InspectedEntity.class);

	final Class<? extends AbstractEntity<?>> type = (Class<? extends AbstractEntity<?>>) dtm.getEnhancer().getManagedType(InspectedEntity.class);
	rao.setEntityType(type);
	final EntityResultQueryModel model = select(type).model();
	firstPage = rao.firstPage(from(model).model(), 15, toByteArray(binaryTypes));
    }

    private List<byte[]> toByteArray(final List<ByteArray> list) {
	final List<byte[]> byteArray = new ArrayList<byte[]>(list.size());
	for (final ByteArray array : list) {
	    byteArray.add(array.getArray());
	}
	return byteArray;
    }

    @Test
    public void test_first_page() {
	assertEquals("Incorrect value for max_key.", 15, firstPage.data().size());
	final AbstractEntity instance = (AbstractEntity) firstPage.data().get(0);
	assertEquals("Incorrect value.", (Integer) instance.get("intProperty") * 2, instance.get("calculatedProperty"));
	assertEquals("Incorrect value.", 20, instance.get("calculatedProperty"));
    }

    @Test
    public void test_next_page() {
	final IPage next = firstPage.next();
	assertEquals("Incorrect value for max_key.", 15, next.data().size());
	final AbstractEntity instance = (AbstractEntity) next.data().get(0);
	assertEquals("Incorrect value.", 40, instance.get("calculatedProperty"));
    }

    @Test
    public void test_last_page() {
	final IPage last = firstPage.last();
	assertEquals("Incorrect value for max_key.", 15, last.data().size());
	final AbstractEntity instance = (AbstractEntity) last.data().get(0);
	assertEquals("Incorrect value.", (Integer) instance.get("intProperty") * 2, instance.get("calculatedProperty"));
	assertEquals("Incorrect value.", 60, instance.get("calculatedProperty"));
    }

    @Test
    public void test_back_to_first_page() {
	final IPage first = firstPage.last().first();
	assertEquals("Incorrect value for max_key.", 15, first.data().size());
	final AbstractEntity instance = (AbstractEntity) first.data().get(0);
	assertEquals("Incorrect value.", (Integer) instance.get("intProperty") * 2, instance.get("calculatedProperty"));
	assertEquals("Incorrect value.", 20, instance.get("calculatedProperty"));
    }

}
