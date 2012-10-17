package ua.com.fielden.platform.swing.review;

import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchOnly;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyAttribute;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer;
import ua.com.fielden.platform.domaintree.impl.DomainTreeEnhancer;
import ua.com.fielden.platform.domaintree.testing.ClassProviderForTestingPurposes;
import ua.com.fielden.platform.domaintree.testing.EntityWithKeyTitleAndWithAEKeyType;
import ua.com.fielden.platform.domaintree.testing.EntityWithNormalNature;
import ua.com.fielden.platform.domaintree.testing.MasterEntity;
import ua.com.fielden.platform.domaintree.testing.TgKryoForDomainTreesTestingPurposes;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.test.CommonTestEntityModuleWithPropertyFactory;
import ua.com.fielden.platform.test.EntityModuleWithPropertyFactory;

import com.google.inject.Injector;

@SuppressWarnings({ "unchecked", "serial" })
public class DynamicFetchBuilderTest {

    private final static ISerialiser serialiser = createSerialiser(createFactory());

    private static EntityFactory createFactory() {
	final EntityModuleWithPropertyFactory module = new CommonTestEntityModuleWithPropertyFactory();
	final Injector injector = new ApplicationInjectorFactory().add(module).getInjector();
	return injector.getInstance(EntityFactory.class);
    }

    private static ISerialiser createSerialiser(final EntityFactory factory) {
	return new TgKryoForDomainTreesTestingPurposes(factory, new ClassProviderForTestingPurposes());
    }

    private static final Class<? extends AbstractEntity<?>> masterKlass, slaveKlass, evenSlaveKlass, stringKeyKlass, mutableKeyType;

    static {
 	final IDomainTreeEnhancer dte = new DomainTreeEnhancer(serialiser, new HashSet<Class<?>>() {{ add(MasterEntity.class); }});
 	dte.addCalculatedProperty(MasterEntity.class, "", "3 + integerProp", "firstCalc", "firstCalc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
 	dte.addCalculatedProperty(MasterEntity.class, "", "SUM(integerProp)", "sumInt", "Int Summary", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
 	dte.addCalculatedProperty(MasterEntity.class, "", "AVG(integerProp)", "avgInt", "Int Average", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
 	dte.addCalculatedProperty(MasterEntity.class, "entityProp.mutablyCheckedProp", "3 + integerProp", "secondCalc", "secondCalc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
 	dte.addCalculatedProperty(MasterEntity.class, "entityProp.mutablyCheckedProp", "SUM(integerProp)", "mutIntSum", "Integer another summary", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
 	dte.addCalculatedProperty(MasterEntity.class, "entityProp.entityProp.simpleEntityProp", "3 + integerProp", "thirdCalc", "thirdCalc", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
 	dte.addCalculatedProperty(MasterEntity.class, "entityProp.entityProp.simpleEntityProp", "SUM(integerProp)", "propIntSum", "Property int summary", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
 	dte.addCalculatedProperty(MasterEntity.class, "entityProp.entityProp.simpleEntityProp", "AVG(integerProp)", "propIntAvg", "Property Int average", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
 	dte.addCalculatedProperty(MasterEntity.class, "entityProp.entityProp.simpleEntityProp", "MIN(integerProp)", "propIntMin", "Property Int minimum", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
 	dte.apply();

 	masterKlass = (Class<? extends AbstractEntity<?>>) dte.getManagedType(MasterEntity.class);
 	slaveKlass = (Class<? extends AbstractEntity<?>>) PropertyTypeDeterminator.determinePropertyType(masterKlass, "entityProp");
 	mutableKeyType = (Class<? extends AbstractEntity<?>>) PropertyTypeDeterminator.determinePropertyType(masterKlass, "entityProp.mutablyCheckedProp");
 	evenSlaveKlass = (Class<? extends AbstractEntity<?>>) PropertyTypeDeterminator.determinePropertyType(masterKlass, "entityProp.entityProp");
 	stringKeyKlass = (Class<? extends AbstractEntity<?>>) PropertyTypeDeterminator.determinePropertyType(masterKlass, "entityProp.entityProp.simpleEntityProp");
 	//
 	//final IDomainTreeEnhancer dteForComplexEntity = new DomainTreeEnhancer(serialiser, new HashSet<Class<?>>() {{ add(EntityWithKeyTitleAndWithAEKeyType.class); }});
    }

    @Test
    public void test_that_fetch_first_level_properties_works(){
	final Set<String> fetchProperties = new HashSet<String>(Arrays.asList(new String[] {
 		"integerProp", //
 		"doubleProp", //
 		"bigDecimalProp", //
 		"moneyProp", //
 		"dateProp", //
 		"booleanProp", //
 		"stringProp"//
	}));
	final fetch<? extends AbstractEntity<?>> fetchModel = fetchOnly(masterKlass).with("integerProp").with("doubleProp")//
		.with("bigDecimalProp").with("moneyProp").with("dateProp").with("booleanProp").with("stringProp");
	assertEquals("The fetch for first level property is incorrect", fetchModel, DynamicFetchBuilder.createFetchOnlyModel(masterKlass, fetchProperties));
    }

    @Test
    public void test_that_entity_propertie_fetch_works(){
	final Set<String> fetchProperties = new HashSet<String>(Arrays.asList(new String[] {
		"", //
		"stringProp", //
		"entityProp"//
	}));
	final fetch<? extends AbstractEntity<?>> slaveEntityFetch = fetchOnly(slaveKlass).with("key").with("desc");
	final fetch<? extends AbstractEntity<?>> masterEntityFetch = fetchOnly(masterKlass).with("key").with("stringProp").//
		with("entityProp", slaveEntityFetch);
	assertEquals("The fetch for entity property is incorrect", masterEntityFetch, DynamicFetchBuilder.createFetchOnlyModel(masterKlass, fetchProperties));
    }

    @Test
    public void test_that_second_and_higher_level_fetch_works(){
	final Set<String> fetchProperties = new HashSet<String>(Arrays.asList(new String[] {
		"", //
		"stringProp", //
		"entityProp.mutablyCheckedProp", //
		"entityProp.mutablyCheckedProp.integerProp",//
		"entityProp.entityProp.simpleEntityProp",//
		"entityProp.entityProp.simpleEntityProp.integerProp"//
	}));
	final fetch<? extends AbstractEntity<?>> slaveSimpleFetch = fetchOnly(mutableKeyType).with("key").with("desc").with("integerProp");
	final fetch<? extends AbstractEntity<?>> stringKeyfetch = fetchOnly(stringKeyKlass).with("key").with("desc").with("integerProp");
	final fetch<? extends AbstractEntity<?>> evenSlaveEntityFetch = fetchOnly(evenSlaveKlass).with("simpleEntityProp", stringKeyfetch);
	final fetch<? extends AbstractEntity<?>> slaveEntityFetch = fetchOnly(slaveKlass).with("mutablyCheckedProp", slaveSimpleFetch)//
		.with("entityProp", evenSlaveEntityFetch);
	final fetch<? extends AbstractEntity<?>> masterEntityFetch = fetchOnly(masterKlass).with("key").with("stringProp")//
		.with("entityProp", slaveEntityFetch);
	assertEquals("The fetch for second and higher level fetch proerties doesn't work", masterEntityFetch, DynamicFetchBuilder.createFetchOnlyModel(masterKlass, fetchProperties));
    }

    @Test
    public void test_that_calculated_properties_fetch_works(){
	final Set<String> fetchProperties = new HashSet<String>(Arrays.asList(new String[] {
		"", //
		"stringProp", //
		"firstCalc", //
		"entityProp.mutablyCheckedProp", //
		"entityProp.mutablyCheckedProp.integerProp", //
		"entityProp.mutablyCheckedProp.secondCalc", //
		"entityProp.entityProp.simpleEntityProp", //
		"entityProp.entityProp.simpleEntityProp.integerProp", //
		"entityProp.entityProp.simpleEntityProp.thirdCalc",//
	}));
	final fetch<? extends AbstractEntity<?>> slaveSimpleFetch = fetchOnly(mutableKeyType).with("key").with("desc").with("integerProp").with("secondCalc");
	final fetch<? extends AbstractEntity<?>> stringKeyfetch = fetchOnly(stringKeyKlass).with("key").with("desc").with("integerProp").with("thirdCalc");
	final fetch<? extends AbstractEntity<?>> evenSlaveEntityFetch = fetchOnly(evenSlaveKlass).with("simpleEntityProp", stringKeyfetch);
	final fetch<? extends AbstractEntity<?>> slaveEntityFetch = fetchOnly(slaveKlass).with("mutablyCheckedProp", slaveSimpleFetch)//
		.with("entityProp", evenSlaveEntityFetch);
	final fetch<? extends AbstractEntity<?>> masterEntityFetch = fetchOnly(masterKlass).with("key").with("stringProp").with("firstCalc")//
		.with("entityProp", slaveEntityFetch);
	assertEquals("The fetch for second and higher level fetch proerties doesn't work", masterEntityFetch, DynamicFetchBuilder.createFetchOnlyModel(masterKlass, fetchProperties));
    }

    @Test
    public void test_that_total_properties_were_fetch_correctly(){
	final Set<String> fetchProperties = new HashSet<String>(Arrays.asList(new String[] {
		"sumInt", //
		"avgInt", //
		"mutIntSum", //
		"propIntSum", //
		"propIntAvg", //
		"propIntMin", //
	}));
	final fetch<? extends AbstractEntity<?>> masterEntityFetch = fetchOnly(masterKlass).with("sumInt").with("avgInt").with("mutIntSum")//
		.with("propIntSum").with("propIntAvg").with("propIntMin").without("id").without("version");
	assertEquals("The fetch for total proerties doesn't work", masterEntityFetch, DynamicFetchBuilder.createTotalFetchModel(masterKlass, fetchProperties));
    }

    @Test
    public void test_that_fetch_model_for_entity_with_AE_key_was_composed_correctlly(){
	final Set<String> fetchProperties = new HashSet<String>(Arrays.asList(new String[] {
		"key", //
	}));
	final fetch<? extends AbstractEntity<?>> complexEntityFetch = fetchOnly(EntityWithKeyTitleAndWithAEKeyType.class).with("key", fetchOnly(EntityWithNormalNature.class).with("key"));
	assertEquals("The fetch for entity with entity key doesn't work", complexEntityFetch, DynamicFetchBuilder.createFetchOnlyModel(EntityWithKeyTitleAndWithAEKeyType.class, fetchProperties));
    }
}
