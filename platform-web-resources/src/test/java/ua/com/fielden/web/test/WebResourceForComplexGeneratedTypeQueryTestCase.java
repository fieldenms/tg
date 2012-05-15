package ua.com.fielden.web.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyAttribute.NO_ATTR;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;
import org.restlet.Restlet;
import org.restlet.Router;

import ua.com.fielden.platform.dao.IGeneratedEntityController;
import ua.com.fielden.platform.domaintree.IDomainTreeManager.IDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.impl.CalculatedProperty;
import ua.com.fielden.platform.domaintree.impl.DomainTreeEnhancer.ByteArray;
import ua.com.fielden.platform.domaintree.testing.DomainTreeManagerAndEnhancer1;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.rao.GeneratedEntityRao;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
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
 * Provides a unit test to ensure correct operation of EQL with generated types.
 *
 * @author TG Team
 *
 */
public class WebResourceForComplexGeneratedTypeQueryTestCase extends WebBasedTestCase {
    private final IGeneratedEntityController rao = new GeneratedEntityRao(config.restClientUtil());
    private final IInspectedEntityDao dao = DbDrivenTestCase.injector.getInstance(IInspectedEntityDao.class);

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

    @Override
    protected String[] getDataSetPaths() {
	return new String[] { "src/test/resources/data-files/query-for-generated-type-web-resource-test-case.flat.xml" };
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

	dtm = new DomainTreeManagerAndEnhancer1(serialiser, rootTypes);
	final CalculatedProperty secondLevelLocalCalculatedProperty = CalculatedProperty.createAndValidate(factory, InspectedEntity.class, "entityPropertyOne", "intProperty * 2", "Calculated property", "desc", NO_ATTR, "intProperty", dtm.getEnhancer());
	dtm.getEnhancer().addCalculatedProperty(secondLevelLocalCalculatedProperty);
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
    @Ignore
    public void test_query_with_condition_on_one_level_deep_calcualted_property() {
	final EntityResultQueryModel model = select(type).where().prop("entityPropertyOne.calculatedProperty").eq().val(20).model();
	final IPage firstPage = rao.firstPage(from(model).build(), 15, binaryTypes);

	assertEquals("Incorrect value of returned items.", 1, firstPage.data().size());
	final AbstractEntity instance = (AbstractEntity) firstPage.data().get(0);
	assertNull("Property should not have been fetched.", instance.get("entityPropertyOne"));
    }

    @Test
    @Ignore
    public void test_query_with_fetch_model_with_one_level_deep_calcualted_property() {
	final Class<? extends AbstractEntity<?>> propertyType = (Class<? extends AbstractEntity<?>>) PropertyTypeDeterminator.determinePropertyType(type, "entityPropertyOne");

	final EntityResultQueryModel model1 = select(propertyType).where().prop("id").eq().val(2).model();
	rao.setEntityType(propertyType);
	final IPage firstPage1 = rao.firstPage(from(model1).build(), 15, binaryTypes); //
	final AbstractEntity instance1 = (AbstractEntity) firstPage1.data().get(0);
	assertEquals("Incorrect value.", 20, instance1.get("calculatedProperty"));
    }

}
