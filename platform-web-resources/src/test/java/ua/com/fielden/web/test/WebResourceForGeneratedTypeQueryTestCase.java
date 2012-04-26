package ua.com.fielden.web.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyAttribute.NO_ATTR;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;
import org.restlet.Restlet;
import org.restlet.Router;

import ua.com.fielden.platform.domaintree.IDomainTreeManager.IDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.impl.CalculatedProperty;
import ua.com.fielden.platform.domaintree.impl.DomainTreeEnhancer.ByteArray;
import ua.com.fielden.platform.domaintree.testing.DomainTreeManagerAndEnhancer1;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.impl.ProvidedSerialisationClassProvider;
import ua.com.fielden.platform.serialisation.impl.TgKryo;
import ua.com.fielden.platform.test.CommonTestEntityModuleWithPropertyFactory;
import ua.com.fielden.platform.test.DbDrivenTestCase;
import ua.com.fielden.platform.web.resources.RouterHelper;
import ua.com.fielden.platform.web.test.WebBasedTestCase;
import ua.com.fielden.web.entities.IInspectedEntityDao;
import ua.com.fielden.web.entities.InspectedEntity;
import ua.com.fielden.web.rao.InspectedEntityRao;

import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * Provides a unit test to ensure correct interaction with IPage summary model.
 *
 * @author TG Team
 *
 */
public class WebResourceForGeneratedTypeQueryTestCase extends WebBasedTestCase {
    private final IInspectedEntityDao rao = new InspectedEntityRao(config.restClientUtil());
    private final IInspectedEntityDao dao = DbDrivenTestCase.injector.getInstance(IInspectedEntityDao.class);

    private final EntityResultQueryModel<InspectedEntity> model = select(InspectedEntity.class).model();
    private final AggregatedResultQueryModel summaryModel = select(InspectedEntity.class).yield().beginExpr().sumOf().prop("moneyProperty").endExpr().as("total_money").//
    yield().beginExpr().maxOf().prop("key").endExpr().as("max_key").modelAsAggregate();
    private IPage<InspectedEntity> firstPage;

    private final Module module = new CommonTestEntityModuleWithPropertyFactory();
    private final Injector injector = new ApplicationInjectorFactory().add(module).getInjector();
    private final EntityFactory factory = injector.getInstance(EntityFactory.class);

    private final List<Class<?>> types = new ArrayList<Class<?>>();
    {
	types.add(InspectedEntity.class);
    }

    private final ISerialiser serialiser = new TgKryo(factory, new ProvidedSerialisationClassProvider(types.toArray(new Class[] {})));
    private final Set<Class<?>> rootTypes = new HashSet<Class<?>>() {
	{
	    add(InspectedEntity.class);
	}
    };
    private IDomainTreeManagerAndEnhancer dtm;

    @Override
    protected String[] getDataSetPaths() {
	return new String[] { "src/test/resources/data-files/query-with-summary-test-case.flat.xml" };
    }

    @Override
    public synchronized Restlet getRoot() {
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
	firstPage = rao.firstPage(from(model).build(), from(summaryModel).build(), 15);

	dtm = new DomainTreeManagerAndEnhancer1(serialiser, rootTypes);
	final CalculatedProperty calc = CalculatedProperty.createAndValidate(factory, InspectedEntity.class, "", "2 * intProp", "Calculated property", "desc", NO_ATTR, "intProp", dtm.getEnhancer());
	dtm.getEnhancer().addCalculatedProperty(calc);
	dtm.getEnhancer().apply();
	final List<ByteArray> binaryTypes = dtm.getEnhancer().getManagedTypeArrays(InspectedEntity.class);
    }

    @Test
    @Ignore
    public void test_first_page() {
	assertNotNull("Summary is missing.", firstPage.summary());
	assertEquals("Incorrect value for max_key.", "key9", firstPage.summary().get("max_key"));
	assertEquals("Incorrect value for total_money.", new BigDecimal("450"), firstPage.summary().get("total_money"));
    }

    @Test
    @Ignore
    public void test_next_page() {
	final IPage<InspectedEntity> page = firstPage.next();

	assertNotNull("Summary is missing.", page.summary());
	assertEquals("Incorrect value for max_key.", "key9", page.summary().get("max_key"));
	assertEquals("Incorrect value for total_money.", new BigDecimal("450"), page.summary().get("total_money"));
    }

    @Test
    @Ignore
    public void test_prev_page() {
	final IPage<InspectedEntity> page = firstPage.next().prev();

	assertNotNull("Summary is missing.", page.summary());
	assertEquals("Incorrect value for max_key.", "key9", page.summary().get("max_key"));
	assertEquals("Incorrect value for total_money.", new BigDecimal("450"), page.summary().get("total_money"));
    }

    @Test
    @Ignore
    public void test_last_page() {
	final IPage<InspectedEntity> page = firstPage.last();

	assertNotNull("Summary is missing.", page.summary());
	assertEquals("Incorrect value for max_key.", "key9", page.summary().get("max_key"));
	assertEquals("Incorrect value for total_money.", new BigDecimal("450"), page.summary().get("total_money"));
    }

    @Test
    @Ignore
    public void test_first_from_last_page() {
	final IPage<InspectedEntity> page = firstPage.last().first();

	assertNotNull("Summary is missing.", page.summary());
	assertEquals("Incorrect value for max_key.", "key9", page.summary().get("max_key"));
	assertEquals("Incorrect value for total_money.", new BigDecimal("450"), page.summary().get("total_money"));
    }

}
