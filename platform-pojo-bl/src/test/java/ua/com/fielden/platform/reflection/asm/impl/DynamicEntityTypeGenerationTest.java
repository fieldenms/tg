package ua.com.fielden.platform.reflection.asm.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.Entity;
import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.meta.IMetaPropertyDefiner;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.asm.api.AnnotationDescriptor;
import ua.com.fielden.platform.reflection.asm.api.NewProperty;
import ua.com.fielden.platform.reflection.asm.impl.entities.Annotation1;
import ua.com.fielden.platform.reflection.asm.impl.entities.Annotation1.ENUM1;
import ua.com.fielden.platform.reflection.asm.impl.entities.Annotation2;
import ua.com.fielden.platform.reflection.asm.impl.entities.EntityBeingEnhanced;
import ua.com.fielden.platform.test.CommonTestEntityModuleWithPropertyFactory;
import ua.com.fielden.platform.test.EntityModuleWithPropertyFactory;
import ua.com.fielden.platform.types.Money;

import com.google.inject.Injector;
import com.google.inject.asm.Type;


/**
 * A test case to ensure correct dynamic modification of entity types by means of adding new properties.
 *
 * @author TG Team
 *
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class DynamicEntityTypeGenerationTest {
    private static final String NEW_PROPERTY_DESC = "Description  for new money property";
    private static final String NEW_PROPERTY_TITLE = "New money property";
    private static final String NEW_PROPERTY_EXPRESSION = "2 * 3 - [integerProp]";
    private static final String NEW_PROPERTY_ORIGINATION = "integerProp";
    private static final String NEW_PROPERTY_1 = "newProperty_1";
    private static final String NEW_PROPERTY_2 = "newProperty_2";
    private boolean observed = false;
    private final EntityModuleWithPropertyFactory module = new CommonTestEntityModuleWithPropertyFactory();
    private final Injector injector = new ApplicationInjectorFactory().add(module).getInjector();
    private final EntityFactory factory = injector.getInstance(EntityFactory.class);
    private DynamicEntityClassLoader cl;

    private final NewProperty pd1 = new NewProperty(NEW_PROPERTY_1, Money.class, false, NEW_PROPERTY_TITLE, NEW_PROPERTY_DESC, new AnnotationDescriptor(Calculated.class, new HashMap<String, Object>() {{ put("expression", NEW_PROPERTY_EXPRESSION); put("origination", NEW_PROPERTY_ORIGINATION); }} ));
    private final NewProperty pd2 = new NewProperty(NEW_PROPERTY_2, Money.class, false,  NEW_PROPERTY_TITLE, NEW_PROPERTY_DESC, new AnnotationDescriptor(Calculated.class, new HashMap<String, Object>() {{ put("expression", NEW_PROPERTY_EXPRESSION); put("origination", NEW_PROPERTY_ORIGINATION); }} ));
    @Before
    public void setUp() {
	observed = false;
	cl = new DynamicEntityClassLoader(ClassLoader.getSystemClassLoader());
    }

    @Test
    public void test_loading_and_inheritance_and_number_of_properties_in_generated_entity_type_with_one_new_property() throws Exception {
	final Class<? extends AbstractEntity> newType = (Class<? extends AbstractEntity>) cl.startModification(Entity.class.getName()).addProperties(pd1).endModification();
	assertEquals("Incorrect type name.", Entity.class.getName() + DynamicTypeNamingService.APPENDIX + "1", newType.getName());
	assertEquals("Incorrect inheritance.", AbstractEntity.class, newType.getSuperclass());
	assertEquals("Incorrect number of properties.", Finder.getPropertyDescriptors(Entity.class).size() + 1, Finder.getPropertyDescriptors(newType).size());
    }

    @Test
    public void test_preconditions() {
	try {
	    cl.addProperties(pd1).endModification();
	    fail("An exception should have been thrown due to omitted startModification call.");
	} catch (final Exception e) {
	}

	try {
	    cl.endModification();
	    fail("An exception should have been thrown due to omitted startModification call.");
	} catch (final Exception e) {
	}
    }

    @Test
    public void test_to_ensure_that_duplicate_new_properties_are_eliminated() throws Exception {
	final Class<? extends AbstractEntity> newType = (Class<? extends AbstractEntity>) cl.startModification(Entity.class.getName()).addProperties(pd1, pd1, pd1).endModification();
	assertEquals("Incorrect number of properties.", Finder.getPropertyDescriptors(Entity.class).size() + 1, Finder.getPropertyDescriptors(newType).size());
    }

    @Test
    public void test_to_ensure_that_conflicting_new_properties_are_not_added() throws Exception {
	final NewProperty npConflicting = new NewProperty("firstProperty", Money.class, false, NEW_PROPERTY_TITLE, NEW_PROPERTY_DESC, new AnnotationDescriptor(Calculated.class, new HashMap<String, Object>() {{ put("expression", NEW_PROPERTY_EXPRESSION); put("origination", NEW_PROPERTY_ORIGINATION); }} ));
	final Class<? extends AbstractEntity> newType = (Class<? extends AbstractEntity>) cl.startModification(Entity.class.getName()).addProperties(npConflicting).endModification();
	assertEquals("Incorrect number of properties.", Finder.getPropertyDescriptors(Entity.class).size(), Finder.getPropertyDescriptors(newType).size());
    }

    @Test
    public void test_to_ensure_several_new_properties_can_be_added() throws Exception {
	final Class<? extends AbstractEntity> newType = (Class<? extends AbstractEntity>) cl.startModification(Entity.class.getName()).addProperties(pd1, pd2).endModification();
	assertEquals("Incorrect number of properties.", Finder.getPropertyDescriptors(Entity.class).size() + 2, Finder.getPropertyDescriptors(newType).size());
    }

    @Test
    public void test_sequential_addition_of_new_properties() throws Exception {
	final Class<? extends AbstractEntity> newType = (Class<? extends AbstractEntity>)//
		cl.startModification(Entity.class.getName()).//
		addProperties(pd1).//
		addProperties(pd2).//
		endModification();
	assertEquals("Incorrect number of properties.", Finder.getPropertyDescriptors(Entity.class).size() + 2, Finder.getPropertyDescriptors(newType).size());
    }

    @Test
    public void test_addition_of_new_property_to_enhanced_type() throws Exception {
	final Class<? extends AbstractEntity> newType1 = (Class<? extends AbstractEntity>) cl.startModification(Entity.class.getName()).addProperties(pd1).endModification();
	final Class<? extends AbstractEntity> newType2 = (Class<? extends AbstractEntity>) cl.startModification(newType1.getName()).addProperties(pd2).endModification();

	assertEquals("Incorrect type name.", Entity.class.getName() + DynamicTypeNamingService.APPENDIX + "1", newType1.getName());
	assertEquals("Incorrect type name.", Entity.class.getName() + DynamicTypeNamingService.APPENDIX + "2", newType2.getName());
	assertEquals("Incorrect number of properties.", Finder.getPropertyDescriptors(Entity.class).size() + 2, Finder.getPropertyDescriptors(newType2).size());
    }

    @Test
    public void test_new_type_name_generation() throws Exception {
	final Class<? extends AbstractEntity> newType1 = (Class<? extends AbstractEntity>) cl.startModification(Entity.class.getName()).addProperties(pd1).endModification();
	assertEquals("Incorrect type name.", Entity.class.getName() + DynamicTypeNamingService.APPENDIX + "1", newType1.getName());
	final Class<? extends AbstractEntity> newType2 = (Class<? extends AbstractEntity>) cl.startModification(Entity.class.getName()).addProperties(pd1).endModification();
	assertEquals("Incorrect type name.", Entity.class.getName() + DynamicTypeNamingService.APPENDIX + "2", newType2.getName());
    }

    @Test
    public void test_instantiation_of_generated_entity_type_using_entity_factory() throws Exception {
	final Class<? extends AbstractEntity> newType = (Class<? extends AbstractEntity>) cl.startModification(Entity.class.getName()).addProperties(pd1).endModification();
	final AbstractEntity<?> entity = factory.newByKey(newType, "key");
	assertNotNull("Should have been created", entity);
    }

    @Test
    public void test_meta_data_for_new_property_in_instance_of_generated_entity_type() throws Exception {
	final Class<? extends AbstractEntity> newType = (Class<? extends AbstractEntity>) cl.startModification(Entity.class.getName()).addProperties(pd1).endModification();

	final Field field = Finder.findFieldByName(newType, NEW_PROPERTY_1);
	assertNotNull("The field should exist.", field);
	final Calculated calcAnno = field.getAnnotation(Calculated.class);
	assertNotNull("The annotation Calculated should exist.", calcAnno);
	assertEquals("Incorrect expression.", "2 * 3 - [integerProp]", calcAnno.expression());
	assertEquals("Incorrect origination property.", "integerProp", calcAnno.origination());

	final AbstractEntity entity = factory.newByKey(newType, "key");
	final MetaProperty newPropertyMeta = entity.getProperty(NEW_PROPERTY_1);
	assertNotNull("Should have been created", newPropertyMeta);
	assertEquals("Incorrect new property title", NEW_PROPERTY_TITLE, newPropertyMeta.getTitle());
	assertEquals("Incorrect new property desc", NEW_PROPERTY_DESC, newPropertyMeta.getDesc());
	assertEquals("Incorrect new property type", Money.class, newPropertyMeta.getType());
    }

    @Test
    public void test_observation_of_setter_for_new_property_in_instance_of_generated_entity_type() throws Exception {
	final Class<? extends AbstractEntity> newType = (Class<? extends AbstractEntity>) cl.startModification(Entity.class.getName()).addProperties(pd1, pd1, pd1).endModification();
	module.getDomainMetaPropertyConfig().setDefiner(newType, NEW_PROPERTY_1, new IMetaPropertyDefiner() {
	    @Override
	    public void define(final MetaProperty property, final Object entityPropertyValue) {
		observed = true;
	    }
	});

	final AbstractEntity entity = factory.newByKey(newType, "key");
	entity.set(NEW_PROPERTY_1, new Money("23.32"));
	assertTrue("Setter for the new property should have been observed.", observed);
    }

    @Test
    public void test_correct_generation_of_property_with_multiple_annotations() throws Exception {
	Map<String, Object> params = new HashMap<String, Object>();
	params.put("value", "string");
	params.put("doubleValue", 0.1);
	params.put("enumValue", ENUM1.E2);
	final AnnotationDescriptor ad1 = new AnnotationDescriptor(Annotation1.class, params);

	params = new HashMap<String, Object>();
	params.put("intValue", 1);
	params.put("type", Type.getType(Money.class));
	final AnnotationDescriptor ad2 = new AnnotationDescriptor(Annotation2.class, params);

	final NewProperty pd = new NewProperty(NEW_PROPERTY_1, Money.class, false, NEW_PROPERTY_TITLE, NEW_PROPERTY_DESC, ad1, ad2);
	final Class<? extends AbstractEntity> newType = (Class<? extends AbstractEntity>) cl.startModification(Entity.class.getName()).addProperties(pd).endModification();

	final Field field = Finder.findFieldByName(newType, NEW_PROPERTY_1);
	final Annotation1 an1 = field.getAnnotation(Annotation1.class);
	assertNotNull("Annotation should be present.", an1);
	assertEquals("Incorrect annotation parameter value.", "string", an1.value());
	assertEquals("Incorrect annotation parameter value.", 0.1, an1.doubleValue(), 0.0000000001);
	assertEquals("Incorrect annotation parameter value.", ENUM1.E2, an1.enumValue());

	final Annotation2 an2 = field.getAnnotation(Annotation2.class);
	assertNotNull("Annotation should be present.", an2);
	assertEquals("Incorrect annotation parameter value.", "value", an2.value());
	assertEquals("Incorrect annotation parameter value.", 1, an2.intValue());
	assertEquals("Incorrect annotation parameter value.", Money.class, an2.type());
    }

    @Test
    public void test_additiona_of_collectional_property() throws Exception {
	// create
	final NewProperty pd = new NewProperty("collectionalProperty", List.class, false, NEW_PROPERTY_TITLE, NEW_PROPERTY_DESC,
		new AnnotationDescriptor(Calculated.class, new HashMap<String, Object>() {{ put("expression", NEW_PROPERTY_EXPRESSION); put("origination", NEW_PROPERTY_ORIGINATION); }}),
		new AnnotationDescriptor(IsProperty.class, new HashMap<String, Object>() {{ put("value", String.class);}}));
	final Class<? extends AbstractEntity> enhancedType = (Class<? extends AbstractEntity>) cl.startModification(EntityBeingEnhanced.class.getName()).addProperties(pd).endModification();

	// test the modified field attributes such as type and IsProperty annotation
	final Field collectionalPropertyField = Finder.findFieldByName(enhancedType, "collectionalProperty");
	assertTrue("Incorrect collectional type.", Collection.class.isAssignableFrom(collectionalPropertyField.getType()));

	final IsProperty annotation = collectionalPropertyField.getAnnotation(IsProperty.class);
	assertNotNull("There should be IsProperty annotation", annotation);
	assertEquals("Incorrect value in IsProperty annotation", String.class, annotation.value());
    }

}
