package ua.com.fielden.platform.reflection.asm.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.factory.CalculatedAnnotation;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.meta.IAfterChangeEventHandler;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.Reflector;
import ua.com.fielden.platform.reflection.asm.api.NewProperty;
import ua.com.fielden.platform.reflection.asm.impl.entities.EntityBeingEnhanced;
import ua.com.fielden.platform.reflection.asm.impl.entities.EntityBeingModified;
import ua.com.fielden.platform.reflection.asm.impl.entities.EntityBeingModifiedWithInnerTypes;
import ua.com.fielden.platform.reflection.asm.impl.entities.EntityBeingModifiedWithInnerTypes.InnerEnum;
import ua.com.fielden.platform.reflection.asm.impl.entities.EntityName;
import ua.com.fielden.platform.reflection.asm.impl.entities.EntityNameProperty;
import ua.com.fielden.platform.reflection.asm.impl.entities.EntityWithCollectionalPropety;
import ua.com.fielden.platform.reflection.asm.impl.entities.TopLevelEntity;
import ua.com.fielden.platform.test.CommonTestEntityModuleWithPropertyFactory;
import ua.com.fielden.platform.test.EntityModuleWithPropertyFactory;
import ua.com.fielden.platform.types.Money;

import com.google.inject.Injector;

/**
 * A test case to ensure correct dynamic modification of entity types by means of changing existing properties.
 *
 * @author TG Team
 *
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class DynamicEntityTypeModificationTest {
    private static final String NEW_PROPERTY_DESC = "Description  for new money property";
    private static final String NEW_PROPERTY_TITLE = "New money property";
    private static final String NEW_PROPERTY_EXPRESSION = "2 * 3 - [integerProp]";
    private static final String NEW_PROPERTY = "newProperty";
    private boolean observed = false;
    private final EntityModuleWithPropertyFactory module = new CommonTestEntityModuleWithPropertyFactory();
    private final Injector injector = new ApplicationInjectorFactory().add(module).getInjector();
    private final EntityFactory factory = injector.getInstance(EntityFactory.class);
    private DynamicEntityClassLoader cl;

    private final Calculated calculated = new CalculatedAnnotation().contextualExpression(NEW_PROPERTY_EXPRESSION).newInstance();

    private final NewProperty pd = new NewProperty(NEW_PROPERTY, Money.class, false, NEW_PROPERTY_TITLE, NEW_PROPERTY_DESC, calculated);

    @Before
    public void setUp() {
	observed = false;
	cl = new DynamicEntityClassLoader(ClassLoader.getSystemClassLoader());
    }

    @Test
    public void test_preconditions() throws Exception {
	final Class<? extends AbstractEntity> entityBeingEnhancedEnhancedType = (Class<? extends AbstractEntity>) cl.startModification(EntityBeingEnhanced.class.getName()).addProperties(pd).endModification();
	// specify what property of what owning type should be replaced with the enhanced entity type
	final NewProperty mp = NewProperty.changeType("prop1", entityBeingEnhancedEnhancedType);
	try {
	    cl.modifyProperties(mp).endModification();
	    fail("An exception should have been thrown due to omitted startModification call.");
	} catch (final Exception e) {
	}
    }

    @Test
    public void test_to_ensure_several_properties_can_be_modified() throws Exception {
	// get the enhanced EntityBeingEnhanced type
	final Class<?> entityBeingEnhancedEnhancedType = cl.startModification(EntityBeingEnhanced.class.getName()).addProperties(pd).endModification();
	// specify that what property prop1@EntityBeingModified should have its type replaced with entityBeingEnhancedEnhancedType
	final NewProperty mp = NewProperty.changeType("prop1", entityBeingEnhancedEnhancedType);
	// get the modified EntityBeingModified type
	final Class<?> entityBeingModifiedModifiedType = cl.startModification(EntityBeingModified.class.getName()).modifyProperties(mp).endModification();
	// specify that properties prop1@TopLevelEntity and prop2@TopLevelEntity should have their type replaced with entityBeingModifiedModifiedType
	final NewProperty topLevelMp1 = NewProperty.changeType("prop1", entityBeingModifiedModifiedType);
	final NewProperty topLevelMp2 = NewProperty.changeType("prop2", entityBeingModifiedModifiedType);
	// get the modified TopLevelEntity type
	final Class<?> topLevelEntityModifiedType = cl.startModification(TopLevelEntity.class.getName()).modifyProperties(topLevelMp1, topLevelMp2).endModification();

	final Field prop1 = topLevelEntityModifiedType.getDeclaredField("prop1");
	final Field prop2 = topLevelEntityModifiedType.getDeclaredField("prop2");
	assertEquals("Incorrect property type after modification", entityBeingModifiedModifiedType, prop1.getType());
	assertEquals("Incorrect property type after modification", entityBeingModifiedModifiedType, prop2.getType());
	assertEquals("prop 1 and prop 2 should be of the same type", prop2.getType(), prop1.getType());
    }

    @Test
    public void test_sequential_modificaton_of_properties() throws Exception {
	// get the enhanced EntityBeingEnhanced type
	final Class<?> entityBeingEnhancedEnhancedType = cl.startModification(EntityBeingEnhanced.class.getName()).addProperties(pd).endModification();
	// specify that what property prop1@EntityBeingModified should have its type replaced with entityBeingEnhancedEnhancedType
	final NewProperty mp = NewProperty.changeType("prop1", entityBeingEnhancedEnhancedType);
	// get the modified EntityBeingModified type
	final Class<?> entityBeingModifiedModifiedType = cl.startModification(EntityBeingModified.class.getName()).modifyProperties(mp).endModification();
	// specify that properties prop1@TopLevelEntity and prop2@TopLevelEntity should have their type replaced with entityBeingModifiedModifiedType
	final NewProperty topLevelMp1 = NewProperty.changeType("prop1", entityBeingModifiedModifiedType);
	final NewProperty topLevelMp2 = NewProperty.changeType("prop2", entityBeingModifiedModifiedType);
	// get the modified TopLevelEntity type
	final Class<?> topLevelEntityModifiedType = cl.startModification(TopLevelEntity.class.getName()).//
	modifyProperties(topLevelMp1).//
	modifyProperties(topLevelMp2).//
	endModification();

	final Field prop1 = topLevelEntityModifiedType.getDeclaredField("prop1");
	final Field prop2 = topLevelEntityModifiedType.getDeclaredField("prop2");
	assertEquals("Incorrect property type after modification", entityBeingModifiedModifiedType, prop1.getType());
	assertEquals("Incorrect property type after modification", entityBeingModifiedModifiedType, prop2.getType());
	assertEquals("prop 1 and prop 2 should be of the same type", prop2.getType(), prop1.getType());
    }

    @Test
    public void test_modification_of_properties_to_modified_type() throws Exception {
	// get the enhanced EntityBeingEnhanced type
	final Class<?> entityBeingEnhancedEnhancedType = cl.startModification(EntityBeingEnhanced.class.getName()).addProperties(pd).endModification();
	// specify that what property prop1@EntityBeingModified should have its type replaced with entityBeingEnhancedEnhancedType
	final NewProperty mp = NewProperty.changeType("prop1", entityBeingEnhancedEnhancedType);
	// get the modified EntityBeingModified type
	final Class<?> entityBeingModifiedModifiedType = cl.startModification(EntityBeingModified.class.getName()).modifyProperties(mp).endModification();
	// specify that properties prop1@TopLevelEntity and prop2@TopLevelEntity should have their type replaced with entityBeingModifiedModifiedType
	final NewProperty topLevelMp1 = NewProperty.changeType("prop1", entityBeingModifiedModifiedType);
	final NewProperty topLevelMp2 = NewProperty.changeType("prop2", entityBeingModifiedModifiedType);
	// get the modified TopLevelEntity type
	final Class<?> topLevelEntityModifiedType1 = cl.startModification(TopLevelEntity.class.getName()).modifyProperties(topLevelMp1).endModification();
	final Class<?> topLevelEntityModifiedType2 = cl.startModification(topLevelEntityModifiedType1.getName()).modifyProperties(topLevelMp2).endModification();

	final Field prop1 = topLevelEntityModifiedType2.getDeclaredField("prop1");
	final Field prop2 = topLevelEntityModifiedType2.getDeclaredField("prop2");
	assertEquals("Incorrect property type after modification", entityBeingModifiedModifiedType, prop1.getType());
	assertEquals("Incorrect property type after modification", entityBeingModifiedModifiedType, prop2.getType());
	assertEquals("prop 1 and prop 2 should be of the same type", prop2.getType(), prop1.getType());
    }

    @Test
    public void test_new_type_name_generation() throws Exception {
	// get the enhanced EntityBeingEnhanced type
	final Class<?> entityBeingEnhancedEnhancedType = cl.startModification(EntityBeingEnhanced.class.getName()).addProperties(pd).endModification();
	// specify that what property prop1@EntityBeingModified should have its type replaced with entityBeingEnhancedEnhancedType
	final NewProperty mp = NewProperty.changeType("prop1", entityBeingEnhancedEnhancedType);
	// get the modified EntityBeingModified type
	final Class<?> entityBeingModifiedModifiedType = cl.startModification(EntityBeingModified.class.getName()).modifyProperties(mp).endModification();
	// specify that properties prop1@TopLevelEntity and prop2@TopLevelEntity should have their type replaced with entityBeingModifiedModifiedType
	final NewProperty topLevelMp1 = NewProperty.changeType("prop1", entityBeingModifiedModifiedType);
	final NewProperty topLevelMp2 = NewProperty.changeType("prop2", entityBeingModifiedModifiedType);
	// get the modified TopLevelEntity type
	final Class<?> topLevelEntityModifiedType1 = cl.startModification(TopLevelEntity.class.getName()).modifyProperties(topLevelMp1).endModification();
	final Class<?> topLevelEntityModifiedType2 = cl.startModification(topLevelEntityModifiedType1.getName()).modifyProperties(topLevelMp2).endModification();

	assertTrue("Incorrect type name.", entityBeingEnhancedEnhancedType.getName().startsWith(EntityBeingEnhanced.class.getName() + DynamicTypeNamingService.APPENDIX + "_"));
	assertTrue("Incorrect type name.", entityBeingModifiedModifiedType.getName().startsWith(EntityBeingModified.class.getName() + DynamicTypeNamingService.APPENDIX + "_"));
	assertTrue("Incorrect type name.", topLevelEntityModifiedType1.getName().startsWith(TopLevelEntity.class.getName() + DynamicTypeNamingService.APPENDIX + "_"));
	assertTrue("Incorrect type name.", topLevelEntityModifiedType2.getName().startsWith(TopLevelEntity.class.getName() + DynamicTypeNamingService.APPENDIX + "_"));
	assertNotSame(entityBeingEnhancedEnhancedType.getName(), equals(entityBeingModifiedModifiedType.getName()));
	assertNotSame(topLevelEntityModifiedType1.getName(), equals(topLevelEntityModifiedType2.getName()));
    }

    @Test
    public void test_instantiation_of_entity_being_modified() throws Exception {
	final Class<? extends AbstractEntity> entityBeingEnhancedEnhancedType = (Class<? extends AbstractEntity>) cl.startModification(EntityBeingEnhanced.class.getName()).addProperties(pd).endModification();
	// specify what property of what owning type should be replaced with the enhanced entity type
	final NewProperty mp = NewProperty.changeType("prop1", entityBeingEnhancedEnhancedType);
	final Class<?> entityBeingModifiedModifiedType = cl.startModification(EntityBeingModified.class.getName()).modifyProperties(mp).endModification();
	// create a new instance of the modified type
	final Object modifiedEntity = entityBeingModifiedModifiedType.newInstance();
	assertNotNull("Should not be null.", modifiedEntity);
    }

    @Test
    public void test_correct_modification_of_entity_being_modified() throws Exception {
	final Class<?> entityBeingEnhancedEnhancedType = cl.startModification(EntityBeingEnhanced.class.getName()).addProperties(pd).endModification();
	// specify what property of what owning type should be replaced with the enhanced entity type
	final NewProperty mp = NewProperty.changeType("prop1", entityBeingEnhancedEnhancedType);
	final Class<?> entityBeingModifiedModifiedType = cl.startModification(EntityBeingModified.class.getName()).modifyProperties(mp).endModification();
	// create a new instance of the modified type
	final Object modifiedEntity = entityBeingModifiedModifiedType.newInstance();
	assertNotNull("Should not be null.", modifiedEntity);
	// let's ensure that property types are compatible -- prop2 should be compatible with prop1 as its type is a super class for type of prop1
	final Field enhancedProp = Finder.findFieldByName(entityBeingModifiedModifiedType, "prop1");
	final Field unenhancedProp = Finder.findFieldByName(entityBeingModifiedModifiedType, "prop2");
	assertTrue("Incorrect property type.", enhancedProp.getType().getName().startsWith(EntityBeingEnhanced.class.getName() + DynamicTypeNamingService.APPENDIX + "_"));
	assertEquals("Incorrect property type.", EntityBeingEnhanced.class.getName(), unenhancedProp.getType().getName());
	assertFalse("Original type should not be assignable to the enhanced type", unenhancedProp.getType().isAssignableFrom(enhancedProp.getType()));
	assertFalse("Enhanced type should not be assignable to the original type", enhancedProp.getType().isAssignableFrom(unenhancedProp.getType()));

	// let's see what happens with the selfTypeProperty's type
	final Field selfTypeProperty = entityBeingModifiedModifiedType.getDeclaredField("selfTypeProperty");
	assertEquals("Incorrect property type.", EntityBeingModified.class, selfTypeProperty.getType());
	final Field unenhancedProp1 = selfTypeProperty.getType().getDeclaredField("prop1");
	final Field unenhancedProp2 = selfTypeProperty.getType().getDeclaredField("prop2");
	assertEquals("Incorrect property type.", EntityBeingEnhanced.class, unenhancedProp1.getType());
	assertEquals("Incorrect property type.", EntityBeingEnhanced.class, unenhancedProp2.getType());
    }

    @Test
    public void test_correct_modification_of_top_level_entity() throws Exception {
	// get the enhanced EntityBeingEnhanced type
	final Class<?> entityBeingEnhancedEnhancedType = cl.startModification(EntityBeingEnhanced.class.getName()).addProperties(pd).endModification();
	// specify that what property prop1@EntityBeingModified should have its type replaced with entityBeingEnhancedEnhancedType
	final NewProperty mp = NewProperty.changeType("prop1", entityBeingEnhancedEnhancedType);
	// get the modified EntityBeingModified type
	final Class<?> entityBeingModifiedModifiedType = cl.startModification(EntityBeingModified.class.getName()).modifyProperties(mp).endModification();
	// specify that properties prop1@TopLevelEntity and prop2@TopLevelEntity should have their type replaced with entityBeingModifiedModifiedType
	final NewProperty topLevelMp1 = NewProperty.changeType("prop1", entityBeingModifiedModifiedType);
	final NewProperty topLevelMp2 = NewProperty.changeType("prop2", entityBeingModifiedModifiedType);
	// get the modified TopLevelEntity type
	final Class<?> topLevelEntityModifiedType = cl.startModification(TopLevelEntity.class.getName()).modifyProperties(topLevelMp1, topLevelMp2).endModification();
	// create a new instance of the modified TopLevelEntity type
	final Object topLevelEntity = topLevelEntityModifiedType.newInstance();
	assertNotNull("Should not be null.", topLevelEntity);

	// let's ensure that property types are compatible -- prop2 should be compatible with prop1 as its type is a super class for type of prop1
	final Field prop1 = topLevelEntityModifiedType.getDeclaredField("prop1");
	final Field prop2 = topLevelEntityModifiedType.getDeclaredField("prop2");
	assertEquals("prop 1 and prop 2 should be of the same type", prop2.getType(), prop1.getType());

	// now take one of the properties from top level entity and ensure that it's type is property modified
	final Field enhancedProp = prop1.getType().getDeclaredField("prop1");
	final Field unenhancedProp = prop1.getType().getDeclaredField("prop2");
	assertTrue("Incorrect property type.", enhancedProp.getType().getName().startsWith(EntityBeingEnhanced.class.getName() + DynamicTypeNamingService.APPENDIX + "_"));
	assertEquals("Incorrect property type.", EntityBeingEnhanced.class.getName(), unenhancedProp.getType().getName());
	assertFalse("Original type should not be assignable to the enhanced type", unenhancedProp.getType().isAssignableFrom(enhancedProp.getType()));
	assertFalse("Enhanced type should not be assignable to the original type", enhancedProp.getType().isAssignableFrom(unenhancedProp.getType()));
    }

    @Test
    public void test_observation_of_setter_for_new_property_in_instance_of_generated_entity_type_used_for_property_in_higher_order_type() throws Exception {
	// get the enhanced EntityBeingEnhanced type
	final Class<? extends AbstractEntity> entityBeingEnhancedEnhancedType = (Class<? extends AbstractEntity>) cl.startModification(EntityBeingEnhanced.class.getName()).addProperties(pd).endModification();
	module.getDomainMetaPropertyConfig().setDefiner(entityBeingEnhancedEnhancedType, NEW_PROPERTY, new IAfterChangeEventHandler() {
	    @Override
	    public void handle(final MetaProperty property, final Object entityPropertyValue) {
		observed = true;
	    }
	});
	// specify that what property prop1@EntityBeingModified should have its type replaced with entityBeingEnhancedEnhancedType
	final NewProperty mp = NewProperty.changeType("prop1", entityBeingEnhancedEnhancedType);
	// get the modified EntityBeingModified type
	final Class<? extends AbstractEntity> entityBeingModifiedModifiedType = (Class<? extends AbstractEntity>)cl.startModification(EntityBeingModified.class.getName()).modifyProperties(mp).endModification();
	// specify that properties prop1@TopLevelEntity and prop2@TopLevelEntity should have their type replaced with entityBeingModifiedModifiedType
	final NewProperty topLevelMp1 = NewProperty.changeType("prop1", entityBeingModifiedModifiedType);
	final NewProperty topLevelMp2 = NewProperty.changeType("prop2", entityBeingModifiedModifiedType);
	// get the modified TopLevelEntity type
	final Class<? extends AbstractEntity> topLevelEntityModifiedType = (Class<? extends AbstractEntity>) cl.startModification(TopLevelEntity.class.getName()).modifyProperties(topLevelMp1, topLevelMp2).endModification();

	// create new instances of the modified TopLevelEntity and EntityBeingModified types using entity factory
	final AbstractEntity<?> topLevelEntity = factory.newByKey(topLevelEntityModifiedType, "key");
	final AbstractEntity<?> entityBeingModified = factory.newByKey(entityBeingModifiedModifiedType, "key");
	final AbstractEntity<?> entityBeingEnhanced = factory.newByKey(entityBeingEnhancedEnhancedType, "key");
	assertNotNull("Should not be null.", topLevelEntity);
	assertNotNull("Should not be null.", entityBeingModified);

	topLevelEntity.set("prop1", entityBeingModified);
	entityBeingModified.set("prop1", entityBeingEnhanced);
	final Money newPropertyValue = new Money("23.32");
	entityBeingEnhanced.set(NEW_PROPERTY, newPropertyValue);

	assertTrue("Setter for the new property should have been observed.", observed);
	assertEquals("Incorrect property value", newPropertyValue, entityBeingEnhanced.get(NEW_PROPERTY));
	assertEquals("Incorrect property value", newPropertyValue, entityBeingModified.get("prop1."+ NEW_PROPERTY));
	assertEquals("Incorrect property value", newPropertyValue, topLevelEntity.get("prop1.prop1."+ NEW_PROPERTY));
	assertEquals("Incorrect property value", entityBeingModified, topLevelEntity.get("prop1"));
    }

    @Test
    public void test_modification_of_collectional_property() throws Exception {
	final Field field = Finder.findFieldByName(EntityWithCollectionalPropety.class, "prop1");
	assertTrue("Incorrect collectional type.", Collection.class.isAssignableFrom(field.getType()));
	assertEquals("Incorrect signature for collectional property.", EntityBeingEnhanced.class, PropertyTypeDeterminator.determinePropertyType(EntityWithCollectionalPropety.class, "prop1"));

	// get the enhanced EntityBeingEnhanced type
	final Class<? extends AbstractEntity> entityBeingEnhancedEnhancedType = (Class<? extends AbstractEntity>) cl.startModification(EntityBeingEnhanced.class.getName()).addProperties(pd).endModification();

	// modify type EntityWithCollectionalPropety by changing the signature of the collectional property
	final NewProperty collectionalPropModification = NewProperty.changeTypeSignature("prop1", entityBeingEnhancedEnhancedType);
	final Class<? extends AbstractEntity> modifiedType = (Class<? extends AbstractEntity>) cl.startModification(EntityWithCollectionalPropety.class.getName()).modifyProperties(collectionalPropModification).endModification();

	// test the modified field attributes such as type and IsProperty annotation
	final Field fieldOfModifiedType = Finder.findFieldByName(modifiedType, "prop1");
	assertTrue("Incorrect collectional type.", Collection.class.isAssignableFrom(fieldOfModifiedType.getType()));
	assertEquals("Incorrect signature for collectional property.", entityBeingEnhancedEnhancedType, PropertyTypeDeterminator.determinePropertyType(modifiedType, "prop1"));

	final IsProperty annotation = fieldOfModifiedType.getAnnotation(IsProperty.class);
	assertNotNull("There should be IsProperty annotation", annotation);
	assertEquals("Incorrect value in IsProperty annotation", entityBeingEnhancedEnhancedType, annotation.value());
    }

    @Test
    public void test_getting_setting_and_observation_of_modified_collectional_property() throws Exception {
	// get the enhanced EntityBeingEnhanced type
	final Class<? extends AbstractEntity> entityBeingEnhancedEnhancedType = (Class<? extends AbstractEntity>) cl.startModification(EntityBeingEnhanced.class.getName()).addProperties(pd).endModification();
	// modify type EntityWithCollectionalPropety by changing the signature of the collectional property
	final NewProperty collectionalPropModification = NewProperty.changeTypeSignature("prop1", entityBeingEnhancedEnhancedType);
	final Class<? extends AbstractEntity> modifiedType = (Class<? extends AbstractEntity>) cl.startModification(EntityWithCollectionalPropety.class.getName()).modifyProperties(collectionalPropModification).endModification();
	// get the enhanced EntityBeingEnhanced type
	module.getDomainMetaPropertyConfig().setDefiner(modifiedType, "prop1", new IAfterChangeEventHandler() {
	    @Override
	    public void handle(final MetaProperty property, final Object entityPropertyValue) {
		observed = true;
	    }
	});

	final AbstractEntity<?> entity = factory.newByKey(modifiedType, "key");
	assertNotNull("Should have been instantiated.", entity);
	assertNull("Initial collectional property value should be null", entity.get("prop1"));

	// test mutator set and getter
	final ArrayList value = new ArrayList();
	value.add(factory.newByKey(entityBeingEnhancedEnhancedType, "key1"));
	entity.set("prop1", value);
	Collection result = (Collection) entity.get("prop1");
	assertNotNull("Collectional property should have a value", result);
	assertEquals("Incorrect number of elements in the collectional property", 1, result.size());
	assertTrue("Observation should have been triggered.", observed);

	// test mutator addTo
	observed = false;
	final Method addTo = modifiedType.getMethod("addToProp1", entityBeingEnhancedEnhancedType);
	addTo.invoke(entity, factory.newByKey(entityBeingEnhancedEnhancedType, "key2"));
	result = (Collection) entity.get("prop1");
	assertEquals("Incorrect number of elements in the collectional property", 2, result.size());
	assertTrue("Observation should have been triggered.", observed);

	// test mutator removeFrom
	observed = false;
	final Method removeFrom = modifiedType.getMethod("removeFromProp1", entityBeingEnhancedEnhancedType);
	removeFrom.invoke(entity, factory.newByKey(entityBeingEnhancedEnhancedType, "key2"));
	result = (Collection) entity.get("prop1");
	assertEquals("Incorrect number of elements in the collectional property", 1, result.size());
	assertTrue("Observation should have been triggered.", observed);
    }

    @Test
    public void test_inner_types_usage_in_generated_classes() throws Exception {
	final NewProperty mp = NewProperty.changeType("integerProp", BigInteger.class);
	final Class<? extends AbstractEntity> entityBeingModifiedWithInnerType = (Class<? extends AbstractEntity>) cl.startModification(EntityBeingModifiedWithInnerTypes.class.getName()).modifyProperties(mp).endModification();

	// instance creation of the generated class with inner types does not fail
	final Object modifiedEntity1 = entityBeingModifiedWithInnerType.newInstance();
	assertNotNull("Should not be null.", modifiedEntity1);
	try {
	    ((AbstractEntity)modifiedEntity1).set("enumProp", InnerEnum.ONE);
	} catch (final Throwable e) {
	    e.printStackTrace();
	    fail("The setter should not fail -- inner classes can not be loaded.");
	}

    }

    @Test
    public void test_generated_class_with_inner_types_instantiation() throws Exception {
	final NewProperty mp = NewProperty.changeType("integerProp", BigInteger.class);
	final Class<? extends AbstractEntity> entityBeingModifiedWithInnerType = (Class<? extends AbstractEntity>) cl.startModification(EntityBeingModifiedWithInnerTypes.class.getName()).modifyProperties(mp).endModification();
	try {
	    factory.newByKey(entityBeingModifiedWithInnerType, "key");
	} catch (final Throwable e) {
	    e.printStackTrace();
	    fail("The instantiation with entity factory shouldn't fail -- inner classes can not be loaded.");
	}
    }

    @Test
    public void test_to_ensure_that_type_modification_leads_to_correct_getter_modificaton() throws Exception {
	final Class<?> enhancedType = cl.startModification(EntityName.class.getName()).addProperties(pd).endModification();

	final Field prop = enhancedType.getDeclaredField("prop");
	assertEquals("Incorrect property type", EntityNameProperty.class, prop.getType());

	final Method getter = Reflector.obtainPropertyAccessor(enhancedType, "prop");
	assertEquals("Incorrect property type", EntityNameProperty.class, getter.getReturnType());
    }
}
