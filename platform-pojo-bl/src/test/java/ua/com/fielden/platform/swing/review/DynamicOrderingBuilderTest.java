package ua.com.fielden.platform.swing.review;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.orderBy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;

import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyAttribute;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer;
import ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.domaintree.impl.CalculatedProperty;
import ua.com.fielden.platform.domaintree.impl.DomainTreeEnhancer;
import ua.com.fielden.platform.domaintree.testing.ClassProviderForTestingPurposes;
import ua.com.fielden.platform.domaintree.testing.MasterEntity;
import ua.com.fielden.platform.domaintree.testing.TgKryoForDomainTreesTestingPurposes;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.test.CommonTestEntityModuleWithPropertyFactory;
import ua.com.fielden.platform.test.EntityModuleWithPropertyFactory;
import ua.com.fielden.platform.utils.Pair;

import com.google.inject.Injector;


@SuppressWarnings({ "unchecked", "serial" })
public class DynamicOrderingBuilderTest {

    private final static ISerialiser serialiser = createSerialiser(createFactory());

    private final static String alias = "alias_for_main_criteria_type";

    private static EntityFactory createFactory() {
	final EntityModuleWithPropertyFactory module = new CommonTestEntityModuleWithPropertyFactory();
	final Injector injector = new ApplicationInjectorFactory().add(module).getInjector();
	return injector.getInstance(EntityFactory.class);
    }

    private static ISerialiser createSerialiser(final EntityFactory factory) {
	return new TgKryoForDomainTreesTestingPurposes(factory, new ClassProviderForTestingPurposes());
    }

    private static final Class<? extends AbstractEntity<?>> masterKlass;
    private static final CalculatedProperty firstCalc, secondCalc, thirdCalc;


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

 	firstCalc = (CalculatedProperty) dte.getCalculatedProperty(MasterEntity.class, "firstCalc");
 	secondCalc = (CalculatedProperty) dte.getCalculatedProperty(MasterEntity.class, "entityProp.mutablyCheckedProp.secondCalc");
 	thirdCalc = (CalculatedProperty) dte.getCalculatedProperty(MasterEntity.class, "entityProp.entityProp.simpleEntityProp.thirdCalc");
    }

    @Test
    public void test_that_ordering_builder_throws_NullPointerException_if_ordering_list_is_null(){
	try{
	    DynamicOrderingBuilder.createOrderingModel(masterKlass, null);
	    fail("There should be null pointer exception");
	}catch (final NullPointerException e){

	}catch (final Exception e){
	    fail("There can not be any other exception then null pointer exception");
	}
    }

    @Test
    public void test_that_ordering_builder_throws_NullPointerException_if_root_type_is_null(){
	final List<Pair<Object, Ordering>> orderingPairs = new ArrayList<Pair<Object,Ordering>>();
	orderingPairs.add(new Pair<Object, Ordering>("integerProp", Ordering.ASCENDING));
	try{
	    DynamicOrderingBuilder.createOrderingModel(null, orderingPairs);
	    fail("There should be null pointer exception");
	}catch(final NullPointerException e){

	}catch(final Exception e){
	    fail("There can not be any other exception then null pointer exception");
	}
    }

    @Test
    public void test_that_ordering_builder_returns_null_for_empty_ordering_list(){
	assertEquals("The ordering model should be null", null, DynamicOrderingBuilder.createOrderingModel(masterKlass, new ArrayList<Pair<Object,Ordering>>()));
    }

    @Test
    public void test_that_ordering_builder_works_for_different_type_of_properties(){
	final List<Pair<Object, Ordering>> orderingPairs = new ArrayList<Pair<Object,Ordering>>();
	orderingPairs.add(new Pair<Object, Ordering>("", Ordering.ASCENDING));
	orderingPairs.add(new Pair<Object, Ordering>("stringProp", Ordering.DESCENDING));
	orderingPairs.add(new Pair<Object, Ordering>(firstCalc.getAst().getModel(), Ordering.ASCENDING));
	orderingPairs.add(new Pair<Object, Ordering>("entityProp.mutablyCheckedProp", Ordering.ASCENDING));
	orderingPairs.add(new Pair<Object, Ordering>("entityProp.mutablyCheckedProp.integerProp", Ordering.DESCENDING));
	orderingPairs.add(new Pair<Object, Ordering>(secondCalc.getAst().getModel(), Ordering.DESCENDING));
	orderingPairs.add(new Pair<Object, Ordering>("entityProp.entityProp.simpleEntityProp", Ordering.ASCENDING));
	orderingPairs.add(new Pair<Object, Ordering>("entityProp.entityProp.simpleEntityProp.integerProp", Ordering.ASCENDING));
	orderingPairs.add(new Pair<Object, Ordering>(thirdCalc.getAst().getModel(), Ordering.DESCENDING));

	final OrderingModel expectedOrdering = orderBy().prop(alias + ".key").asc().prop(alias + ".stringProp").desc().expr(firstCalc.getAst().getModel()).asc()//
		.prop(alias + ".entityProp.mutablyCheckedProp.key").asc().prop(alias + ".entityProp.mutablyCheckedProp.integerProp").desc()//
		.expr(secondCalc.getAst().getModel()).desc().prop(alias + ".entityProp.entityProp.simpleEntityProp.key").asc()//
		.prop(alias + ".entityProp.entityProp.simpleEntityProp.integerProp").asc().expr(thirdCalc.getAst().getModel()).desc().model();

	assertEquals("The ordering model is incorrect", expectedOrdering, DynamicOrderingBuilder.createOrderingModel(masterKlass, orderingPairs));
    }
}
