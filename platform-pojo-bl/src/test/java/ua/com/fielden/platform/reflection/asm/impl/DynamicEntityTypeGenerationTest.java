package ua.com.fielden.platform.reflection.asm.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.google.inject.Injector;

import ua.com.fielden.platform.associations.one2many.DetailsEntityForOneToManyAssociation;
import ua.com.fielden.platform.associations.one2many.MasterEntityWithOneToManyAssociation;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.ChildEntity;
import ua.com.fielden.platform.entity.Entity;
import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.factory.BeforeChangeAnnotation;
import ua.com.fielden.platform.entity.annotation.factory.CalculatedAnnotation;
import ua.com.fielden.platform.entity.annotation.factory.DescTitleAnnotation;
import ua.com.fielden.platform.entity.annotation.factory.HandlerAnnotation;
import ua.com.fielden.platform.entity.annotation.factory.IsPropertyAnnotation;
import ua.com.fielden.platform.entity.annotation.factory.ParamAnnotation;
import ua.com.fielden.platform.entity.annotation.mutator.BeforeChange;
import ua.com.fielden.platform.entity.annotation.mutator.DateParam;
import ua.com.fielden.platform.entity.annotation.mutator.Handler;
import ua.com.fielden.platform.entity.before_change_event_handling.BeforeChangeEventHandler;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.meta.IAfterChangeEventHandler;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.Reflector;
import ua.com.fielden.platform.reflection.asm.api.NewProperty;
import ua.com.fielden.platform.reflection.asm.impl.entities.Annotation1;
import ua.com.fielden.platform.reflection.asm.impl.entities.Annotation1.ENUM1;
import ua.com.fielden.platform.reflection.asm.impl.entities.Annotation2;
import ua.com.fielden.platform.reflection.asm.impl.entities.EntityBeingEnhanced;
import ua.com.fielden.platform.reflection.asm.impl.entities.TopLevelEntity;
import ua.com.fielden.platform.test.CommonTestEntityModuleWithPropertyFactory;
import ua.com.fielden.platform.test.EntityModuleWithPropertyFactory;
import ua.com.fielden.platform.types.Money;

/**
 * A test case to ensure correct dynamic modification of entity types by means of adding new properties.
 *
 * @author TG Team
 *
 */
@SuppressWarnings("unchecked")
public class DynamicEntityTypeGenerationTest {
    private static final String NEW_PROPERTY_DESC = "Description  for new money property";
    private static final String NEW_PROPERTY_TITLE = "New money property";
    private static final String NEW_PROPERTY_EXPRESSION = "2 * 3 - [integerProp]";
    private static final String NEW_PROPERTY_1 = "newProperty_1";
    private static final String NEW_PROPERTY_2 = "newProperty_2";
    private static final String NEW_PROPERTY_BOOL = "newProperty_BOOL";
    private static final String NEW_PROPERTY_EXPRESSION_BOOL = "2 < 3";
    private final EntityModuleWithPropertyFactory module = new CommonTestEntityModuleWithPropertyFactory();
    private final Injector injector = new ApplicationInjectorFactory().add(module).getInjector();
    private final EntityFactory factory = injector.getInstance(EntityFactory.class);
    private DynamicEntityClassLoader cl;

    private final IsProperty propertyWithPrecision = new IsPropertyAnnotation(19, 4).newInstance();
    private final Calculated calculated = new CalculatedAnnotation().contextualExpression(NEW_PROPERTY_EXPRESSION).newInstance();
    private final Calculated boolCalculated = new CalculatedAnnotation().contextualExpression(NEW_PROPERTY_EXPRESSION_BOOL).newInstance();

    private final NewProperty pd1 = new NewProperty(NEW_PROPERTY_1, Money.class, false, NEW_PROPERTY_TITLE, NEW_PROPERTY_DESC,
            propertyWithPrecision, calculated);
    private final NewProperty pd2 = new NewProperty(NEW_PROPERTY_2, Money.class, false, NEW_PROPERTY_TITLE, NEW_PROPERTY_DESC, 
            calculated);
    private final NewProperty pdBool = new NewProperty(NEW_PROPERTY_BOOL, boolean.class, false, NEW_PROPERTY_TITLE, NEW_PROPERTY_DESC,
            boolCalculated);
    
    private static final Class<Entity> DEFAULT_ORIG_TYPE = Entity.class;

    @Before
    public void setUp() {
        cl = DynamicEntityClassLoader.getInstance(ClassLoader.getSystemClassLoader());
        Reflector.cleanUp();
    }
    
    @Test
    public void dynamically_created_types_have_the_same_class_loader() throws Exception {
        final Class<? extends AbstractEntity<String>> newType = (Class<? extends AbstractEntity<String>>)
                cl.startModification(DEFAULT_ORIG_TYPE)
                .modifyTypeName(DEFAULT_ORIG_TYPE.getName() + "_enhanced")
                .endModification();
        final Class<? extends AbstractEntity<String>> newType0 = (Class<? extends AbstractEntity<String>>)
                cl.startModification(DEFAULT_ORIG_TYPE)
                .modifyTypeName(DEFAULT_ORIG_TYPE.getName() + "_enhanced0")
                .endModification();
        assertEquals(newType.getClassLoader(), newType0.getClassLoader());
    }

    @Test
    public void types_can_be_renamed() throws Exception {
        assertEquals("Incorrect setter return type.", 
                DEFAULT_ORIG_TYPE,
                Reflector.obtainPropertySetter(DEFAULT_ORIG_TYPE, "firstProperty").getReturnType());
        assertEquals("Incorrect setter argument type.",
                DEFAULT_ORIG_TYPE,
                Reflector.obtainPropertySetter(DEFAULT_ORIG_TYPE, "entity").getParameterTypes()[0]);
        
        final String newTypeName = DEFAULT_ORIG_TYPE.getName() + "_enhanced1";
        final Class<? extends AbstractEntity<String>> newType = (Class<? extends AbstractEntity<String>>)
                cl.startModification(DEFAULT_ORIG_TYPE)
                .modifyTypeName(newTypeName)
                .endModification();
        assertTrue("Incorrect type name.", 
                newType.getName().equals(newTypeName));
        assertEquals("Incorrect inheritance.", DEFAULT_ORIG_TYPE, newType.getSuperclass());
        assertEquals("Incorrect setter return type.",
                DEFAULT_ORIG_TYPE,
                Reflector.obtainPropertySetter(newType, "firstProperty").getReturnType());

        assertEquals("Incorrect setter argument type.",
                DEFAULT_ORIG_TYPE,
                Reflector.obtainPropertySetter(newType, "entity").getParameterTypes()[0]);
        
    }

    @Test
    public void should_support_supertype_modification() throws Exception {
        final String newTypeName = DEFAULT_ORIG_TYPE.getName() + "_enhanced2";
        final Class<? extends AbstractEntity<String>> newType = (Class<? extends AbstractEntity<String>>)
                cl.startModification(DEFAULT_ORIG_TYPE)
                .modifyTypeName(newTypeName)
                .modifySupertypeName(DEFAULT_ORIG_TYPE.getName())
                .endModification();
        assertTrue("Incorrect type name.", newType.getName().equals(newTypeName));
        assertEquals("Incorrect inheritance.", DEFAULT_ORIG_TYPE, newType.getSuperclass());

        assertEquals("Incorrect setter return type.", 
                DEFAULT_ORIG_TYPE,
                Reflector.obtainPropertySetter(newType, "firstProperty").getReturnType());
    }

    @Test
    public void generated_types_can_be_renamed_after_adding_new_properties() throws Exception {
        final String newTypeName = DEFAULT_ORIG_TYPE.getName() + "_enhanced3";
        final Class<? extends AbstractEntity<String>> newType = (Class<? extends AbstractEntity<String>>) 
                cl.startModification(DEFAULT_ORIG_TYPE)
                .addProperties(pd1)
                .modifyTypeName(newTypeName)
                .endModification();
        assertTrue("Incorrect type name.", newType.getName().equals(newTypeName));
        assertEquals("Incorrect inheritance.", DEFAULT_ORIG_TYPE, newType.getSuperclass());

        assertEquals("Incorrect number of properties.", 
                Finder.getPropertyDescriptors(DEFAULT_ORIG_TYPE).size() + 1,
                Finder.getPropertyDescriptors(newType).size());
        assertEquals("Incorrect setter return type.", 
                DEFAULT_ORIG_TYPE,
                Reflector.obtainPropertySetter(newType, "firstProperty").getReturnType());
    }

    @Test
    public void test_type_name_modification_after_properties_modification() throws Exception {
        final String newTypeName = DEFAULT_ORIG_TYPE.getName() + "_enhanced4";
        final NewProperty np = NewProperty.changeType("firstProperty", BigDecimal.class);
        final Class<? extends AbstractEntity<String>> newType = (Class<? extends AbstractEntity<String>>)
                cl.startModification(DEFAULT_ORIG_TYPE)
                .modifyTypeName(newTypeName)
                .modifyProperties(np)
                .endModification();
        assertTrue("Incorrect type name.", newType.getName().equals(newTypeName));
        assertEquals("Incorrect inheritance.", DEFAULT_ORIG_TYPE, newType.getSuperclass());
        final AbstractEntity<?> entity = factory.newByKey(newType, "key");
        Reflector.getMethod(entity, "getFirstProperty").invoke(entity);
        assertEquals("Incorrect setter return type.", 
                newType,
                Reflector.obtainPropertySetter(newType, np.name).getReturnType());
    }

    @Test
    public void test_loading_and_inheritance_and_number_of_properties_in_generated_entity_type_with_one_new_property() throws Exception {
        final Class<? extends AbstractEntity<String>> newType = (Class<? extends AbstractEntity<String>>)
                cl.startModification(DEFAULT_ORIG_TYPE)
                .addProperties(pd1)
                .endModification();
        assertTrue("Incorrect type name.", newType.getName().startsWith(DEFAULT_ORIG_TYPE.getName() + DynamicTypeNamingService.APPENDIX + "_"));
        assertEquals("Incorrect inheritance.", DEFAULT_ORIG_TYPE, newType.getSuperclass());
        assertEquals("Incorrect number of properties.", 
                Finder.getPropertyDescriptors(DEFAULT_ORIG_TYPE).size() + 1,
                Finder.getPropertyDescriptors(newType).size());
    }

    @Test
    public void test_preconditions() {
        try {
            final TypeMaker<Entity> tp = new TypeMaker<Entity>(cl, Entity.class);
            tp.addProperties(pd1).endModification();
            fail("An exception should have been thrown due to omitted startModification call.");
        } catch (final Exception e) {}

        try {
            final TypeMaker<Entity> tp = new TypeMaker<Entity>(cl, Entity.class);
            tp.endModification();
            fail("An exception should have been thrown due to omitted startModification call.");
        } catch (final Exception e) {}
    }

    @Test
    public void test_to_ensure_primitive_boolean_new_property_can_be_added() throws Exception {
        final Class<? extends AbstractEntity<String>> newType = (Class<? extends AbstractEntity<String>>)
                cl.startModification(DEFAULT_ORIG_TYPE)
                .addProperties(pdBool)
                .endModification();
        assertEquals("Incorrect number of properties.", 
                Finder.getPropertyDescriptors(DEFAULT_ORIG_TYPE).size() + 1,
                Finder.getPropertyDescriptors(newType).size());

        final Field field = Finder.findFieldByName(newType, NEW_PROPERTY_BOOL);
        assertNotNull("The field should exist.", field);
        assertEquals("Incorrect type.", boolean.class, field.getType());
        final Calculated calcAnno = AnnotationReflector.getAnnotation(field, Calculated.class);
        assertNotNull("The annotation Calculated should exist.", calcAnno);
        assertEquals("Incorrect expression.", NEW_PROPERTY_EXPRESSION_BOOL, calcAnno.value());
    }

    @Test
    public void test_to_ensure_that_duplicate_new_properties_are_eliminated() throws Exception {
        final Class<? extends AbstractEntity<String>> newType = (Class<? extends AbstractEntity<String>>) cl.startModification(Entity.class).addProperties(pd1, pd1, pd1).endModification();
        assertEquals("Incorrect number of properties.", 
                Finder.getPropertyDescriptors(Entity.class).size() + 1,
                Finder.getPropertyDescriptors(newType).size());
    }

    @Test
    public void new_properties_are_ordered_as_provided_appearing_at_the_end_of_the_class() throws Exception {
        final Class<? extends AbstractEntity<String>> newType = (Class<? extends AbstractEntity<String>>)
                cl.startModification(DEFAULT_ORIG_TYPE)
                .addProperties(pd1, pd2, pdBool)
                .endModification();
        final int size = newType.getDeclaredFields().length;
        assertEquals("The last field of class should correspond to a last 'freshly added' property.", 
                pdBool.name, newType.getDeclaredFields()[size - 1].getName());
        assertEquals("The last - 1 field of class should correspond to a last - 1 'freshly added' property.",
                pd2.name, newType.getDeclaredFields()[size - 2].getName());
        assertEquals("The last - 2 field of class should correspond to a last - 2 'freshly added' property.",
                pd1.name, newType.getDeclaredFields()[size - 3].getName());
    }

    @Test
    public void precision_and_scale_are_generated_correctly_when_specified_as_part_of_new_property_definition() throws Exception {
        final Class<? extends AbstractEntity<String>> newType = (Class<? extends AbstractEntity<String>>)
                cl.startModification(DEFAULT_ORIG_TYPE)
                .modifyTypeName(DEFAULT_ORIG_TYPE.getName() + "_enhanced")
                .addProperties(pd1, pd2)
                .endModification();
        
        final IsProperty pd1PropAnnot = AnnotationReflector.getPropertyAnnotation(IsProperty.class, newType, pd1.name); 
        assertEquals(19, pd1PropAnnot.precision());
        assertEquals(4, pd1PropAnnot.scale());

        final IsProperty pd2PropAnnot = AnnotationReflector.getPropertyAnnotation(IsProperty.class, newType, pd2.name); 
        assertEquals(IsProperty.DEFAULT_PRECISION, pd2PropAnnot.precision());
        assertEquals(IsProperty.DEFAULT_SCALE, pd2PropAnnot.scale());
    }

    @Test
    public void conflicting_new_properties_are_not_added() throws Exception {
        final NewProperty npConflicting = new NewProperty("firstProperty", Money.class, false, NEW_PROPERTY_TITLE, NEW_PROPERTY_DESC, calculated);
        final Class<? extends AbstractEntity<String>> newType = (Class<? extends AbstractEntity<String>>)
                cl.startModification(DEFAULT_ORIG_TYPE)
                .addProperties(npConflicting)
                .endModification();
        assertEquals("Incorrect number of properties.", 
                Finder.getPropertyDescriptors(DEFAULT_ORIG_TYPE).size(),
                Finder.getPropertyDescriptors(newType).size());
    }

    @Test
    public void multiple_new_properties_can_be_added() throws Exception {
        final Class<? extends AbstractEntity<String>> newType = (Class<? extends AbstractEntity<String>>)
                cl.startModification(DEFAULT_ORIG_TYPE)
                .addProperties(pd1, pd2)
                .endModification();
        assertEquals("Incorrect number of properties.", 
                Finder.getPropertyDescriptors(DEFAULT_ORIG_TYPE).size() + 2,
                Finder.getPropertyDescriptors(newType).size());
    }

    @Test
    public void test_sequential_addition_of_new_properties() throws Exception {
        final Class<? extends AbstractEntity<String>> newType = (Class<? extends AbstractEntity<String>>)
                cl.startModification(DEFAULT_ORIG_TYPE)
                .addProperties(pd1)
                .addProperties(pd2)
                .endModification();
        assertEquals("Incorrect number of properties.", 
                Finder.getPropertyDescriptors(DEFAULT_ORIG_TYPE).size() + 2,
                Finder.getPropertyDescriptors(newType).size());
    }

    @Test
    public void test_addition_of_new_property_to_enhanced_type() throws Exception {
        final Class<? extends AbstractEntity<String>> newType1 = (Class<? extends AbstractEntity<String>>)
                cl.startModification(DEFAULT_ORIG_TYPE)
                .addProperties(pd1)
                .endModification();
        final Class<? extends AbstractEntity<String>> newType2 = (Class<? extends AbstractEntity<String>>)
                cl.startModification(newType1)
                .addProperties(pd2)
                .endModification();

        assertTrue("Incorrect type name.", newType1.getName().startsWith(DEFAULT_ORIG_TYPE.getName() + DynamicTypeNamingService.APPENDIX + "_"));
        assertTrue("Incorrect type name.", newType2.getName().startsWith(DEFAULT_ORIG_TYPE.getName() + DynamicTypeNamingService.APPENDIX + "_"));
        assertEquals("Incorrect number of properties.", 
                Finder.getPropertyDescriptors(DEFAULT_ORIG_TYPE).size() + 2,
                Finder.getPropertyDescriptors(newType2).size());
    }

    @Test
    public void generation_based_on_a_type_with_an_overriden_method() throws Exception {
        final Class<? extends AbstractEntity<String>> newType1 = (Class<? extends AbstractEntity<String>>)
                cl.startModification(ChildEntity.class)
                .addProperties(pd1)
                .endModification();
        assertTrue("Incorrect type name.", newType1.getName().startsWith(ChildEntity.class.getName() + DynamicTypeNamingService.APPENDIX + "_"));

        final List<?> propDescr = Finder.getPropertyDescriptors(newType1);
        System.out.println(propDescr.size());

        final AbstractEntity<?> entity = factory.newByKey(newType1, "key");
        entity.setDesc("la-la");
    }

    @Test
    public void test_instantiation_of_generated_entity_type_using_entity_factory() throws Exception {
        final Class<? extends AbstractEntity<String>> newType = (Class<? extends AbstractEntity<String>>)
                cl.startModification(DEFAULT_ORIG_TYPE)
                .addProperties(pd1)
                .endModification();
        final AbstractEntity<?> entity = factory.newByKey(newType, "key");
        assertNotNull("Should have been created", entity);
    }

    @Test
    public void test_meta_data_for_new_property_in_instance_of_generated_entity_type() throws Exception {
        final Class<? extends AbstractEntity<String>> newType = (Class<? extends AbstractEntity<String>>)
                cl.startModification(DEFAULT_ORIG_TYPE)
                .addProperties(pd1)
                .endModification();

        final Field field = Finder.findFieldByName(newType, NEW_PROPERTY_1);
        assertNotNull("The field should exist.", field);
        final Calculated calcAnno = AnnotationReflector.getAnnotation(field, Calculated.class);
        assertNotNull("The annotation Calculated should exist.", calcAnno);
        assertEquals("Incorrect expression.", "2 * 3 - [integerProp]", calcAnno.value());

        final AbstractEntity<?> entity = factory.newByKey(newType, "key");
        final MetaProperty<?> newPropertyMeta = entity.getProperty(NEW_PROPERTY_1);
        assertNotNull("Should have been created", newPropertyMeta);
        assertEquals("Incorrect new property title", NEW_PROPERTY_TITLE, newPropertyMeta.getTitle());
        assertEquals("Incorrect new property desc", NEW_PROPERTY_DESC, newPropertyMeta.getDesc());
        assertEquals("Incorrect new property type", Money.class, newPropertyMeta.getType());
    }

    @Test
    public void test_observation_of_setter_for_new_property_in_instance_of_generated_entity_type() throws Exception {
        final Class<? extends AbstractEntity<String>> newType = (Class<? extends AbstractEntity<String>>)
                cl.startModification(DEFAULT_ORIG_TYPE)
                .addProperties(pd1, pd1, pd1)
                .endModification();
        final boolean[] observed = {false}; // a small hack to avoid having `observed` as a class field
        module.getDomainMetaPropertyConfig().setDefiner(newType, NEW_PROPERTY_1, new IAfterChangeEventHandler<Object>() {
            @Override
            public void handle(final MetaProperty<Object> property, final Object entityPropertyValue) {
                observed[0] = true;
            }
        });

        final AbstractEntity<?> entity = factory.newByKey(newType, "key");
        entity.set(NEW_PROPERTY_1, new Money("23.32"));
        assertTrue("Setter for the new property should have been observed.", observed[0]);
    }

    @Test
    public void test_correct_generation_of_property_with_multiple_annotations() throws Exception {
        final Annotation1 ad1 = new Annotation1() {
            @Override public Class<Annotation1> annotationType() { return Annotation1.class; }
            @Override public String value() { return "string"; }
            @Override public double doubleValue() { return 0.1; }
            @Override public ENUM1 enumValue() { return ENUM1.E2; }
        };
        final Annotation2 ad2 = new Annotation2() {
            @Override public Class<Annotation2> annotationType() { return Annotation2.class; }
            @Override public String value() { return "value"; } 
            @Override public int intValue() { return 1; } 
            @Override public Class<?> type() { return Money.class; } 
        };

        final NewProperty pd = new NewProperty(NEW_PROPERTY_1, Money.class, false, NEW_PROPERTY_TITLE, NEW_PROPERTY_DESC, ad1, ad2);
        final Class<? extends AbstractEntity<String>> newType = (Class<? extends AbstractEntity<String>>)
                cl.startModification(DEFAULT_ORIG_TYPE)
                .addProperties(pd)
                .endModification();

        final Field field = Finder.findFieldByName(newType, NEW_PROPERTY_1);
        final Annotation1 an1 = AnnotationReflector.getAnnotation(field, Annotation1.class);
        assertNotNull("Annotation should be present.", an1);
        assertEquals("Incorrect annotation parameter value.", "string", an1.value());
        assertEquals("Incorrect annotation parameter value.", 0.1, an1.doubleValue(), 0.0000000001);
        assertEquals("Incorrect annotation parameter value.", ENUM1.E2, an1.enumValue());

        final Annotation2 an2 = AnnotationReflector.getAnnotation(field, Annotation2.class);
        assertNotNull("Annotation should be present.", an2);
        assertEquals("Incorrect annotation parameter value.", "value", an2.value());
        assertEquals("Incorrect annotation parameter value.", 1, an2.intValue());
        assertEquals("Incorrect annotation parameter value.", Money.class, an2.type());
    }

    @Test
    public void test_addition_of_collectional_property() throws Exception {
        // create
        final Calculated calculated = new CalculatedAnnotation().contextualExpression(NEW_PROPERTY_EXPRESSION).newInstance();
        final IsProperty isProperty = new IsPropertyAnnotation(String.class).newInstance();

        final NewProperty pd = new NewProperty("collectionalProperty", List.class, false, "Collectional Property",
                "Collectional Property Description", calculated, isProperty);
        final Class<? extends AbstractEntity<String>> enhancedType = (Class<? extends AbstractEntity<String>>)
                cl.startModification(EntityBeingEnhanced.class)
                .addProperties(pd)
                .endModification();

        // test the modified field attributes such as type and IsProperty annotation
        final Field collectionalPropertyField = Finder.findFieldByName(enhancedType, "collectionalProperty");
        assertTrue("Incorrect collectional type.", Collection.class.isAssignableFrom(collectionalPropertyField.getType()));

        final IsProperty annotation = AnnotationReflector.getAnnotation(collectionalPropertyField, IsProperty.class);
        assertNotNull("There should be IsProperty annotation", annotation);
        assertEquals("Incorrect value in IsProperty annotation", String.class, annotation.value());
    }

    @Test
    public void test_getter_signature_for_new_collectional_property() throws Exception {
        // create
        final IsProperty isProperty = new IsPropertyAnnotation(String.class, "--stub-for-tests-to-be-passed--").newInstance();

        final NewProperty pd = new NewProperty("collectionalProperty", List.class, false, "Collectional Property", "Collectional Property Description", isProperty);
        final Class<? extends AbstractEntity<String>> enhancedType = (Class<? extends AbstractEntity<String>>)
                cl.startModification(EntityBeingEnhanced.class)
                .addProperties(pd)
                .endModification();

        final Method getter = Reflector.obtainPropertyAccessor(enhancedType, "collectionalProperty");
        assertEquals("Incorrect return type.", "java.util.List<java.lang.String>", getter.getGenericReturnType().toString());
        final AbstractEntity<String> instance = factory.newByKey(enhancedType, "new");
        assertNull("Collectional property should be null by default.", getter.invoke(instance));
    }

    @Test
    public void test_setter_signature_for_new_collectional_property() throws Exception {
        // create
        final IsProperty isProperty = new IsPropertyAnnotation(String.class, "--stub-for-tests-to-be-passed--").newInstance();

        final NewProperty pd = new NewProperty("collectionalProperty", List.class, false, "Collectional Property", "Collectional Property Description", isProperty);
        final Class<? extends AbstractEntity<String>> enhancedType = (Class<? extends AbstractEntity<String>>)
                cl.startModification(EntityBeingEnhanced.class)
                .addProperties(pd)
                .endModification();

        final Method setter = Reflector.obtainPropertySetter(enhancedType, "collectionalProperty");
        final Type[] types = setter.getGenericParameterTypes();
        assertEquals("Incorrect number of generic parameters", 1, types.length);
        assertEquals("Incorrect parameter type", "java.util.List<java.lang.String>", types[0].toString());

        final AbstractEntity<String> instance = factory.newByKey(enhancedType, "new");
        final List<String> list = new ArrayList<String>();
        setter.invoke(instance, list);
        final Method getter = Reflector.obtainPropertyAccessor(enhancedType, "collectionalProperty");
        assertNotNull("Collectional property should not be null once assigned.", getter.invoke(instance));
        assertEquals("Incorrect getter return value", list, getter.invoke(instance));
    }

    @Test
    public void test_generation_of_property_with_BCE_declaration() throws Exception {
        final Handler[] handlers = new Handler[] { 
                new HandlerAnnotation(BeforeChangeEventHandler.class)
                .date(new DateParam[] { ParamAnnotation.dateParam("dateParam", "2011-12-01 00:00:00") })
                .newInstance() 
        };
        final BeforeChange bch = new BeforeChangeAnnotation(handlers).newInstance();
        final String PROP_NAME = "prop_name";
        final NewProperty pd = new NewProperty(PROP_NAME, String.class, false, "title", "desc", bch);

        final Class<? extends AbstractEntity<String>> enhancedType = (Class<? extends AbstractEntity<String>>)
                cl.startModification(EntityBeingEnhanced.class)
                .addProperties(pd)
                .endModification();

        final Field field = Finder.findFieldByName(enhancedType, PROP_NAME);
        final BeforeChange bceAnnotation = AnnotationReflector.getAnnotation(field, BeforeChange.class);
        assertNotNull("BeforeChange annotation should be present.", bceAnnotation);

        final Handler[] handlerAnnotations = bceAnnotation.value();
        assertEquals("Incorrect number of handlers.", 1, handlerAnnotations.length);

        final Handler handlerAnnotation = handlerAnnotations[0];
        assertEquals("Incorrect parameter.", BeforeChangeEventHandler.class, handlerAnnotation.value());
        assertEquals("Incorrect parameter.", 0, handlerAnnotation.clazz().length);
        assertEquals("Incorrect parameter.", 1, handlerAnnotation.date().length);
        assertEquals("Incorrect parameter.", 0, handlerAnnotation.date_time().length);
        assertEquals("Incorrect parameter.", 0, handlerAnnotation.dbl().length);
        assertEquals("Incorrect parameter.", 0, handlerAnnotation.integer().length);
        assertEquals("Incorrect parameter.", 0, handlerAnnotation.money().length);
        assertEquals("Incorrect parameter.", 0, handlerAnnotation.non_ordinary().length);
        assertEquals("Incorrect parameter.", 0, handlerAnnotation.str().length);

        final DateParam dateParam = handlerAnnotation.date()[0];
        assertEquals("Incorrect parameter.", "dateParam", dateParam.name());
        assertEquals("Incorrect parameter.", "2011-12-01 00:00:00", dateParam.value());
    }

    @Test
    public void one2Many_special_case_property_is_generated_correctly() throws Exception {
        final IsProperty isProperty = new IsPropertyAnnotation(String.class, "key1").newInstance();
        final NewProperty pd = new NewProperty("one2manyAssociationSpecialCase2", DetailsEntityForOneToManyAssociation.class, false, 
                "One2Many Special Case Association Property", "One2Many Special Case Association Property Description", isProperty);
        final Class<? extends AbstractEntity<String>> enhancedType = (Class<? extends AbstractEntity<String>>)
                cl.startModification(MasterEntityWithOneToManyAssociation.class)
                .addProperties(pd)
                .endModification();

        assertEquals("key1", 
                AnnotationReflector.getAnnotation(
                        Finder.findFieldByName(enhancedType, "one2manyAssociationSpecialCase2"),
                        IsProperty.class)
                .linkProperty());
    }

    @Test
    public void one2Many_collectional_property_is_generated_correctly() throws Exception {
        final IsProperty isProperty = new IsPropertyAnnotation(DetailsEntityForOneToManyAssociation.class, "key1").newInstance();

        final NewProperty pd = new NewProperty("one2manyAssociationCollectional2", List.class, false, 
                "One2Many Collectional Association Property", "One2Many Collectional Association Property Description", isProperty);
        final Class<? extends AbstractEntity<String>> enhancedType = (Class<? extends AbstractEntity<String>>)
                cl.startModification(MasterEntityWithOneToManyAssociation.class)
                .addProperties(pd)
                .endModification();

        assertEquals("key1", 
                AnnotationReflector.getAnnotation(
                        Finder.findFieldByName(enhancedType, "one2manyAssociationCollectional2"),
                        IsProperty.class)
                .linkProperty());
        assertEquals(DetailsEntityForOneToManyAssociation.class, 
                AnnotationReflector.getAnnotation(
                        Finder.findFieldByName(enhancedType, "one2manyAssociationCollectional2"),
                        IsProperty.class)
                .value());
    }

    @Test
    @Ignore
    public void test_to_ensure_that_property_name_with_dangerous_character_works() throws Exception {
        final String propName = "//firstProperty//";
        final NewProperty exoticProperty = new NewProperty(propName, String.class, false, "title", "desc");
        final Class<? extends AbstractEntity<String>> newType = (Class<? extends AbstractEntity<String>>)
                cl.startModification(DEFAULT_ORIG_TYPE)
                .addProperties(exoticProperty)
                .endModification();
        try {
            Finder.findFieldByName(newType, propName);
        } catch (final Exception e) {
            fail("The exotic field should have been found");
        }
    }

    @Test
    public void valid_class_annotations_can_be_added() throws Exception {
        final DescTitle newAnnot = new DescTitleAnnotation("Title", "Description").newInstance();
        final Class<? extends AbstractEntity<?>> newType = (Class<? extends AbstractEntity<?>>) 
                cl.startModification(TopLevelEntity.class)
                .addClassAnnotations(newAnnot)
                .endModification();

        final DescTitle annot = newType.getAnnotation(DescTitle.class);
        assertNotNull(annot);
        assertEquals(newAnnot.value(), annot.value());
        assertEquals(newAnnot.desc(), annot.desc());
    }

    @Test
    public void existing_class_annotations_are_not_replaced() throws Exception {
        final DescTitle newAnnot = new DescTitleAnnotation("Title", "Description").newInstance();
        final DescTitle oldAnnot = DEFAULT_ORIG_TYPE.getAnnotation(DescTitle.class);
        final Class<? extends AbstractEntity<?>> newType = (Class<? extends AbstractEntity<?>>)
                cl.startModification(DEFAULT_ORIG_TYPE)
                .addClassAnnotations(newAnnot)
                .endModification();
        final DescTitle annot = newType.getAnnotation(DescTitle.class);
        assertNotNull(annot);
        assertEquals(oldAnnot.value(), annot.value());
        assertEquals(oldAnnot.desc(), annot.desc());
    }

    @Test
    public void adding_inappropriate_annotaton_to_a_class_fails() throws Exception {
        assertThrows("The provided annotation IsProperty should have 'type' target.", 
                IllegalArgumentException.class, 
                () -> {
                    cl.startModification(TopLevelEntity.class)
                    .addClassAnnotations(new IsPropertyAnnotation().newInstance())
                    .endModification();
                });
    }

    @Test
    public void can_add_class_annotations_after_adding_properties() throws Exception {
        final DescTitle newAnnot = new DescTitleAnnotation("Title", "Description").newInstance();
        final Class<? extends AbstractEntity<String>> newType = (Class<? extends AbstractEntity<String>>)
                cl.startModification(TopLevelEntity.class)
                .addProperties(pd1)
                .addClassAnnotations(newAnnot)
                .endModification();

        final DescTitle annot = newType.getAnnotation(DescTitle.class);
        assertNotNull(annot);
        assertEquals(newAnnot.value(), annot.value());
        assertEquals(newAnnot.desc(), annot.desc());

        // The new type has new properties 'desc' and 'pd1'
        // so the total number of properties must be greater than in the original type by 2
        assertEquals("Incorrect number of properties",
                Finder.getPropertyDescriptors(TopLevelEntity.class).size() + 2,
                Finder.getPropertyDescriptors(newType).size());
    }

    @Test
    public void original_type_of_a_generated_one_is_its_base_type() throws Exception {
        final Class<? extends AbstractEntity<String>> newType = (Class<? extends AbstractEntity<String>>) cl
                .startModification(TopLevelEntity.class)
                .addProperties(pd1)
                .addClassAnnotations(new DescTitleAnnotation("Title", "Description").newInstance())
                .endModification();
        assertEquals(TopLevelEntity.class, DynamicEntityClassLoader.getOriginalType(newType));
    }

    @Test
    public void original_type_of_a_generated_one_based_on_another_generated_type_is_the_same_for_both_generated_types() throws Exception {
        final Class<? extends AbstractEntity<String>> newBaseType = (Class<? extends AbstractEntity<String>>) cl
                .startModification(TopLevelEntity.class)
                .addClassAnnotations(new DescTitleAnnotation("Title", "Description").newInstance())
                .endModification();
        final Class<? extends AbstractEntity<String>> newType = (Class<? extends AbstractEntity<String>>) cl
                .startModification(newBaseType)
                .addProperties(pd1)
                .endModification();

        assertEquals(TopLevelEntity.class, DynamicEntityClassLoader.getOriginalType(newBaseType));
        assertEquals(TopLevelEntity.class, DynamicEntityClassLoader.getOriginalType(newType));
    }

}
