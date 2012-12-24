package ua.com.fielden.web.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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
public class WebResourceWithSummaryTestCase extends WebBasedTestCase {

    private final IGeneratedEntityController rao = new GeneratedEntityRao(config.restClientUtil());

    private final Module module = new CommonTestEntityModuleWithPropertyFactory();
    private final Injector injector = new ApplicationInjectorFactory().add(module).getInjector();
    private final EntityFactory factory = injector.getInstance(EntityFactory.class);

    private final ISerialiser serialiser = new TgKryo(factory, new ProvidedSerialisationClassProvider(new Class[] { InspectedEntity.class }));
    private final Set<Class<?>> rootTypes = new HashSet<Class<?>>() {
	{
	    add(InspectedEntity.class);
	}
    };
    private IDomainTreeManagerAndEnhancer dtm;
    private List<byte[]> binaryTypes;
    private Class<? extends AbstractEntity<?>> type;

    private IPage page;

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
	dtm.getEnhancer().addCalculatedProperty(InspectedEntity.class, "", "SUM(intProperty)", "intProperty Sum", "desc", NO_ATTR, "intProperty");
	dtm.getEnhancer().addCalculatedProperty(InspectedEntity.class, "", "MAX(intProperty) - MIN(intProperty)", "minimaxDiff", "desc", NO_ATTR, "intProperty");
	dtm.getEnhancer().apply();
	final List<ByteArray> byteListOfTypes = dtm.getEnhancer().getManagedTypeArrays(InspectedEntity.class);
	binaryTypes = toByteArray(byteListOfTypes);
	type = (Class<? extends AbstractEntity<?>>) dtm.getEnhancer().getManagedType(InspectedEntity.class);
	rao.setEntityType(type);

	final EntityResultQueryModel model = select(type).//
		yield().prop("id").as("id").//
		yield().prop("version").as("version").//
		yield().prop("key").as("key").//
		yield().prop("desc").as("desc").//
		yield().prop("intProperty").as("intProperty").//
		yield().prop("decimalProperty").as("decimalProperty").//
		modelAsEntity(type);
	final EntityResultQueryModel summaryModel = select(type).where().prop("intProperty").lt().val(30).//
		yield().prop("intPropertySum").as("intPropertySum").//
		yield().prop("minimaxDiff").as("minimaxDiff").//
		modelAsEntity(type);
	page = rao.firstPage(from(model).model(), from(summaryModel).model(), 15, binaryTypes);

    }

    @Test
    public void test_first_page() {
	assertEquals("Incorrect value of returned items.", 15, page.data().size());
	assertNotNull("Summary should have been calculated.", page.summary());
	assertEquals("Incorrect value.", 830, page.summary().get("intPropertySum"));
	assertEquals("Incorrect value.", 10, page.summary().get("minimaxDiff"));
    }


    @Test
    public void test_next_page() {
	final IPage<?> nextPage = page.next();

	assertEquals("Incorrect value of returned items.", 15, nextPage.data().size());
	assertNotNull("Summary should have been calculated.", nextPage.summary());
	assertEquals("Incorrect value.", 830, nextPage.summary().get("intPropertySum"));
	assertEquals("Incorrect value.", 10, nextPage.summary().get("minimaxDiff"));
    }

    @Test
    public void test_prev_page() {
	final IPage<?> prevPage = page.next().prev();

	assertEquals("Incorrect value of returned items.", 15, prevPage.data().size());
	assertNotNull("Summary should have been calculated.", prevPage.summary());
	assertEquals("Incorrect value.", 830, prevPage.summary().get("intPropertySum"));
	assertEquals("Incorrect value.", 10, prevPage.summary().get("minimaxDiff"));
    }

    @Test
    public void test_last_page() {
	final IPage<?> lastPage = page.last();

	assertEquals("Incorrect value of returned items.", 1, lastPage.data().size());
	assertNotNull("Summary should have been calculated.", lastPage.summary());
	assertEquals("Incorrect value.", 830, lastPage.summary().get("intPropertySum"));
	assertEquals("Incorrect value.", 10, lastPage.summary().get("minimaxDiff"));
    }

    @Test
    public void test_from_first_to_last_page() {
	final IPage<?> firstPage = page.last().first();

	assertEquals("Incorrect value of returned items.", 15, firstPage.data().size());
	assertNotNull("Summary should have been calculated.", firstPage.summary());
	assertEquals("Incorrect value.", 830, firstPage.summary().get("intPropertySum"));
	assertEquals("Incorrect value.", 10, firstPage.summary().get("minimaxDiff"));
    }

    private List<byte[]> toByteArray(final List<ByteArray> list) {
	final List<byte[]> byteArray = new ArrayList<byte[]>(list.size());
	for (final ByteArray array : list) {
	    byteArray.add(array.getArray());
	}
	return byteArray;
    }

    @Override
    protected String[] getDataSetPaths() {
	return new String[] { "src/test/resources/data-files/query-with-summary-test-case.flat.xml" };
    }
}
