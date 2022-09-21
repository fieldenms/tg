package ua.com.fielden.platform.reflection.asm.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.reflection.Finder.findFieldByName;
import static ua.com.fielden.platform.reflection.Finder.getFieldValue;
import static ua.com.fielden.platform.reflection.asm.api.test_utils.NewPropertyTestUtils.assertAnnotationsEquals;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.google.inject.Injector;

import ua.com.fielden.platform.associations.one2many.DetailsEntityForOneToManyAssociation;
import ua.com.fielden.platform.associations.one2many.MasterEntityWithOneToManyAssociation;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.Entity;
import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.Title;
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
import ua.com.fielden.platform.reflection.asm.impl.entities.EntityBeingEnhanced;
import ua.com.fielden.platform.reflection.asm.impl.entities.TopLevelEntity;
import ua.com.fielden.platform.test.CommonTestEntityModuleWithPropertyFactory;
import ua.com.fielden.platform.test.EntityModuleWithPropertyFactory;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.utils.Pair;

/**
 * A test case to ensure correct dynamic modification of entity types by means of adding new properties.
 *
 * @author TG Team
 *
 */
public class DynamicEntityTypePropertiesAdditionTest {
    private static final Class<Entity> DEFAULT_ORIG_TYPE = Entity.class;

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

    private final IsProperty atIsPropertyWithPrecision = new IsPropertyAnnotation(19, 4).newInstance();
    private final Calculated atCalculated = new CalculatedAnnotation().contextualExpression(NEW_PROPERTY_EXPRESSION).newInstance();
    private final Calculated atCalculatedBool = new CalculatedAnnotation().contextualExpression(NEW_PROPERTY_EXPRESSION_BOOL)
            .newInstance();

    private final NewProperty<Money> np1 = NewProperty.create(NEW_PROPERTY_1, Money.class, NEW_PROPERTY_TITLE, NEW_PROPERTY_DESC,
            atIsPropertyWithPrecision, atCalculated);
    private final NewProperty<Money> np2 = NewProperty.create(NEW_PROPERTY_2, Money.class, NEW_PROPERTY_TITLE, NEW_PROPERTY_DESC, 
            atCalculated);
    private final NewProperty<Boolean> npBool = NewProperty.create(NEW_PROPERTY_BOOL, boolean.class, NEW_PROPERTY_TITLE, NEW_PROPERTY_DESC,
            atCalculatedBool);
    @SuppressWarnings("rawtypes")
    private final NewProperty<List> npRawList = NewProperty.create("rawListTestProperty", List.class, "Collectional Property", 
            "Collectional Property Description", new IsPropertyAnnotation(String.class).newInstance());
    @SuppressWarnings("rawtypes")
    private final NewProperty<List> npParamList = NewProperty.create("paramListTestProperty", List.class, List.of(String.class),
            "Collectional Property", "Collectional Property Description", new IsPropertyAnnotation(String.class).newInstance());

    @Before
    public void setUp() {
        cl = DynamicEntityClassLoader.getInstance(ClassLoader.getSystemClassLoader());
        Reflector.cleanUp();
    }

    @Test
    public void properties_can_be_added() throws Exception {
        // 1. add a single property
        final Class<? extends AbstractEntity<String>> newType1 = cl.startModification(DEFAULT_ORIG_TYPE)
                .addProperties(npBool)
                .endModification();

        assertEquals("Incorrect number of properties.", 
                Finder.getPropertyDescriptors(DEFAULT_ORIG_TYPE).size() + 1,
                Finder.getPropertyDescriptors(newType1).size());

        final Field field1 = Finder.findFieldByName(newType1, npBool.getName());
        assertNotNull("Added property %s was not found.".formatted(npBool.getName()), field1);
        assertEquals("Incorrect type of the added property.", 
                npBool.genericTypeAsDeclared().toString(), field1.getGenericType().toString());

        // make sure all provided property annotations were generated
        assertAnnotationsEquals(npBool, field1);

        // 2. add multiple properties at once
        final List<NewProperty<?>> newProperties = List.of(npBool, np1, np2);
        final Class<? extends AbstractEntity<String>> newType2 = cl.startModification(DEFAULT_ORIG_TYPE)
                .addProperties(newProperties)
                .endModification();

        assertEquals("Incorrect number of properties.", 
                Finder.getPropertyDescriptors(DEFAULT_ORIG_TYPE).size() + newProperties.size(),
                Finder.getPropertyDescriptors(newType2).size());

        newProperties.forEach(np -> {
            final Field field = Finder.findFieldByName(newType2, np.getName());
            assertNotNull("Added property %s was not found.".formatted(np.getName()), field);
            assertEquals("Incorrect type of the added property.", 
                    np.genericTypeAsDeclared().toString(), field.getGenericType().toString());

            // make sure all provided property annotations were generated
            assertAnnotationsEquals(np, field);
        });


        // 3. add multiple properties sequentially
        final TypeMaker<? extends AbstractEntity<String>> builder = cl.startModification(DEFAULT_ORIG_TYPE);
        newProperties.forEach(builder::addProperties);
        final Class<? extends AbstractEntity<String>> newType3 = builder.endModification();

        assertEquals("Incorrect number of properties.", 
                Finder.getPropertyDescriptors(DEFAULT_ORIG_TYPE).size() + newProperties.size(),
                Finder.getPropertyDescriptors(newType3).size());

        newProperties.forEach(np -> {
            final Field field = Finder.findFieldByName(newType3, np.getName());
            assertNotNull("Added property %s was not found.".formatted(np.getName()), field);
            assertEquals("Incorrect type of the added property.",
                    np.genericTypeAsDeclared().toString(), field.getGenericType().toString());

            // make sure all provided property annotations were generated
            assertAnnotationsEquals(np, field);
        });
    }

    @Test
    public void generated_types_can_be_renamed_after_adding_new_properties() throws Exception {
        final String newTypeName = DEFAULT_ORIG_TYPE.getName() + "_enhanced";
        final Class<? extends AbstractEntity<String>> newType = cl.startModification(DEFAULT_ORIG_TYPE)
                .addProperties(np1)
                .modifyTypeName(newTypeName)
                .endModification();

        assertEquals("Incorrect type name.", newTypeName, newType.getName());
        assertEquals("Incorrect number of properties.", 
                Finder.getPropertyDescriptors(DEFAULT_ORIG_TYPE).size() + 1,
                Finder.getPropertyDescriptors(newType).size());
    }

    @Test
    public void only_distinct_properties_are_added() throws Exception {
        final Class<? extends AbstractEntity<String>> newType1 = cl.startModification(DEFAULT_ORIG_TYPE)
                .addProperties(np1, np1, np1)
                .endModification(); 

        assertEquals("Incorrect number of properties.", 
                Finder.getPropertyDescriptors(DEFAULT_ORIG_TYPE).size() + 1,
                Finder.getPropertyDescriptors(newType1).size());

    }

    // TODO is this property useful?
    // is not satisfied in case collectional properties are added or modified, 
    // since special fields for ByteBuddy auxiliary types will be generated
    @Test
    @Ignore
    public void new_properties_are_ordered_as_provided_appearing_at_the_end_of_the_class() throws Exception {
        final Class<? extends AbstractEntity<String>> newType = cl.startModification(DEFAULT_ORIG_TYPE)
                .addProperties(np1, np2, npBool)
                .endModification();
        final int size = newType.getDeclaredFields().length;
        assertEquals("The last field of class should correspond to a last 'freshly added' property.", 
                npBool.name, newType.getDeclaredFields()[size - 1].getName());
        assertEquals("The last - 1 field of class should correspond to a last - 1 'freshly added' property.",
                np2.name, newType.getDeclaredFields()[size - 2].getName());
        assertEquals("The last - 2 field of class should correspond to a last - 2 'freshly added' property.",
                np1.name, newType.getDeclaredFields()[size - 3].getName());
    }

    @Test
    public void properties_can_be_added_to_enhanced_types() throws Exception {
        final Class<? extends AbstractEntity<String>> newType1 = cl.startModification(DEFAULT_ORIG_TYPE)
                .addProperties(np1)
                .endModification();
        final Class<? extends AbstractEntity<String>> newType2 = cl.startModification(newType1)
                .addProperties(np2)
                .endModification();

        assertEquals("Incorrect number of properties.", 
                Finder.getPropertyDescriptors(DEFAULT_ORIG_TYPE).size() + 2,
                Finder.getPropertyDescriptors(newType2).size());

        for (final NewProperty<?> np: List.of(np1, np2)) {
            final Field field = Finder.findFieldByName(newType2, np.getName());
            assertNotNull("Added property %s was not found.".formatted(np.getName()), field);
            assertEquals("Incorrect type of the added property %s.".formatted(np.getName()),
                    np.genericType().toString(), field.getGenericType().toString());

            // make sure all provided property annotations were generated
            assertAnnotationsEquals(np, field);
        }
    }

    @Test
    public void added_properties_can_have_explicitly_initialized_value() throws Exception {
        final Double value = 125d;
        final NewProperty<Double> npExplicitInit = NewProperty.create("newPropWithInitializedValue", Double.class, 
                "title", "desc").setValue(value);

        final Class<? extends AbstractEntity<String>> newType = cl.startModification(DEFAULT_ORIG_TYPE)
                .addProperties(npExplicitInit)
                .endModification();

        final Field field = Finder.getFieldByName(newType, npExplicitInit.getName());
        assertNotNull("Added property %s was not found.".formatted(npExplicitInit.getName()), field);
        final AbstractEntity<String> instance = factory.newByKey(newType, "new");
        assertEquals("Incorrect value of the added property %s.".formatted(npExplicitInit.getName()),
                value, Finder.getFieldValue(field, instance));
    }

    @Test
    public void MetaProperty_can_be_obtained_for_an_added_property() throws Exception {
        final Class<? extends AbstractEntity<String>> newType = cl.startModification(DEFAULT_ORIG_TYPE)
                .addProperties(np1)
                .endModification();

        final AbstractEntity<?> instance = factory.newByKey(newType, "key");
        final MetaProperty<?> mp = instance.getProperty(np1.getName());
        assertNotNull("Could not obtain MetaProperty for added property %s.".formatted(np1.getName()), mp);
        final Title atTitle = np1.getAnnotationByType(Title.class);
        assertEquals("Incorrect MetaProperty title.", atTitle.value(), mp.getTitle());
        assertEquals("Incorrect MetaProperty desc.", atTitle.desc(), mp.getDesc());
        assertEquals("Incorrect MetaProperty type.", np1.genericTypeAsDeclared(), mp.getType());
    }

    @Test
    public void setter_is_observed_for_an_added_property() throws Exception {
        final Class<? extends AbstractEntity<String>> newType = cl.startModification(DEFAULT_ORIG_TYPE)
                .addProperties(np1)
                .endModification();

        final boolean[] observed = {false}; // a small hack to avoid having `observed` as a class field
        module.getDomainMetaPropertyConfig().setDefiner(newType, np1.getName(), new IAfterChangeEventHandler<Object>() {
            @Override
            public void handle(final MetaProperty<Object> property, final Object entityPropertyValue) {
                observed[0] = true;
            }
        });

        final AbstractEntity<String> instance = factory.newByKey(newType, "key");
        instance.set(np1.getName(), new Money("23.32"));
        assertTrue("Setter for the added property %s was not observed.".formatted(np1.getName()), observed[0]);
    }

    @Test
    public void added_collectional_properties_are_initialized_by_default() throws Exception {
        // test both raw collectional properties and parameterized ones
        for (final NewProperty<? extends Collection> np: List.of(npRawList, npParamList)) {
            final Class<? extends AbstractEntity<String>> newType = cl.startModification(DEFAULT_ORIG_TYPE)
                    .addProperties(np)
                    .endModification();

            final Field field = Finder.findFieldByName(newType, np.getName());
            final AbstractEntity<String> instance = factory.newByKey(newType, "new");

            final Object value = Finder.getFieldValue(field, instance);
            assertNotNull("Added collectional property %s was not initialized.".formatted(np.getName()), value);
        }
    }

    @Test 
    public void default_initialization_of_new_collectional_property_for_standard_subinterfaces_of_Collection() throws Exception {
        // test both raw and parameterized collectional properties
        for (final NewProperty<? extends Collection> np: List.of(npRawList, npParamList)) {

            // hardcoding String type argument
            for (final var pair: List.of(Pair.pair(List.class, new ArrayList<String>()), 
                                         Pair.pair(Set.class, new HashSet<String>()))) 
            {
                final Class<? extends Collection> collectionType = pair.getKey();
                final Collection<String> expectedInitValue = pair.getValue();

                final Class<? extends AbstractEntity<String>> newType = cl.startModification(DEFAULT_ORIG_TYPE)
                        .addProperties(np.changeType(collectionType))
                        .endModification();

                final Field field = Finder.findFieldByName(newType, np.getName());
                final AbstractEntity<String> instance = factory.newByKey(newType, "new");
                final Object actualInitValue = Finder.getFieldValue(field, instance);
                assertEquals("Incorrect initialized value of added collectional property %s.".formatted(np.getName()),
                        expectedInitValue, actualInitValue);
            }
        }
    }

    @Test 
    public void default_initialization_of_new_collectional_property_for_implementations_of_Collection() throws Exception {
        // test both raw and parameterized collectional properties
        for (final NewProperty<? extends Collection> np: List.of(npRawList, npParamList)) {

            // hardcoding String type argument
            for (final var pair : List.of(Pair.pair(ArrayList.class, new ArrayList<String>()),
                                          Pair.pair(LinkedList.class, new LinkedList<String>()),
                                          Pair.pair(HashSet.class, new HashSet<String>()),
                                          Pair.pair(LinkedHashSet.class, new LinkedHashSet<String>()),
                                          Pair.pair(TreeSet.class, new TreeSet<String>()))) 
            {
                final Class<? extends Collection> collectionType = pair.getKey();
                final Collection<String> expectedValue = pair.getValue();

                final Class<? extends AbstractEntity<String>> newType = cl.startModification(DEFAULT_ORIG_TYPE)
                        .addProperties(np.changeType(collectionType))
                        .endModification();

                final Field field = Finder.findFieldByName(newType, np.getName());
                final AbstractEntity<String> instance = factory.newByKey(newType, "new");
                final Object actualValue = Finder.getFieldValue(field, instance);
                assertEquals("Incorrect initialized value of added collectional property %s.".formatted(np.getName()),
                        expectedValue, actualValue);
            }
        }
    }

    @Test
    public void getters_are_generated_correctly_for_added_collectional_properties() throws Exception {
        // test both raw and parameterized collectional properties
        for (final NewProperty<? extends Collection> np: List.of(npRawList, npParamList)) {
            final Class<? extends AbstractEntity<String>> newType = cl.startModification(DEFAULT_ORIG_TYPE)
                    .addProperties(np)
                    .endModification();

            final Method getter;
            try {
                getter = Reflector.obtainPropertyAccessor(newType, np.getName());
            } catch (final Exception e) {
                fail("Getter for added collectional property %s was not found.".formatted(np.getName()));
                return;
            }

            assertEquals("Incorrect getter return type for added collectional property %s.".formatted(np.getName()),
                    np.genericType().toString(), getter.getGenericReturnType().toString());

            // instantiate the generated type and try to invoke the getter
            final AbstractEntity<String> instance = factory.newByKey(newType, "new");
            final List<String> list = List.of("hello");

            try {
                getter.invoke(instance);
            } catch (final Exception e) {
                fail("Failed to invoke getter for added collectional property %s.".formatted(np.getName()));
                return;
            }
        }
    }

    @Test
    public void setters_are_generated_correctly_for_added_collectional_properties() throws Exception {
        // test both raw and parameterized collectional properties
        for (final NewProperty<? extends Collection> np: List.of(npRawList, npParamList)) {
            final Class<? extends AbstractEntity<String>> newType = cl.startModification(DEFAULT_ORIG_TYPE)
                    .addProperties(np)
                    .endModification();

            final Method setter;
            try {
                setter = Reflector.obtainPropertySetter(newType, np.getName());
            } catch (final Exception e) {
                fail("Setter for added collectional property %s was not found.".formatted(np.getName()));
                return;
            }

            final Type[] parameterTypes = setter.getGenericParameterTypes();
            assertEquals("Incorrect number of setter generic parameters for added collectional property %s.".formatted(np.getName()),
                    1, parameterTypes.length);
            assertEquals("Incorrect setter parameter type for added collectional property %s.".formatted(np.getName()),
                    np.genericType().toString(), parameterTypes[0].toString());

            // instantiate the generated type and try to set the value of added property 
            final AbstractEntity<String> instance = factory.newByKey(newType, "new");
            final List<String> list = List.of("hello");
            setter.invoke(instance, list);

            assertEquals("Value of added collectional property %s was set incorrectly.".formatted(np.getName()),
                    list, getFieldValue(findFieldByName(newType, np.getName()), instance));
        }
    }

    @Test
    public void added_properties_with_BCE_handlers_are_generated_correctly() throws Exception {
        final Handler[] handlers = new Handler[] { 
                new HandlerAnnotation(BeforeChangeEventHandler.class)
                    .date(new DateParam[] { ParamAnnotation.dateParam("dateParam", "2011-12-01 00:00:00") })
                    .newInstance() 
        };
        final BeforeChange bc = new BeforeChangeAnnotation(handlers).newInstance();
        final NewProperty<String> np = NewProperty.create("testPropertyWithBCE", String.class, "title", "desc", bc);

        final Class<? extends AbstractEntity<String>> newType = cl.startModification(DEFAULT_ORIG_TYPE)
                .addProperties(np)
                .endModification();

        final Field field = Finder.findFieldByName(newType, np.getName());
        final BeforeChange atBeforeChange = AnnotationReflector.getAnnotation(field, BeforeChange.class);
        assertNotNull("BeforeChange annotation is not present.", atBeforeChange);

        final Handler[] actualHandlers = atBeforeChange.value();
        assertEquals("Incorrect number of handlers.", handlers.length, actualHandlers.length);

        final Handler atHandler = actualHandlers[0];
        assertEquals("Incorrect @Handler element value.", BeforeChangeEventHandler.class, atHandler.value());
        assertEquals("Incorrect @Handler element value.", 0, atHandler.clazz().length);
        assertEquals("Incorrect @Handler element value.", 1, atHandler.date().length);
        assertEquals("Incorrect @Handler element value.", 0, atHandler.date_time().length);
        assertEquals("Incorrect @Handler element value.", 0, atHandler.dbl().length);
        assertEquals("Incorrect @Handler element value.", 0, atHandler.integer().length);
        assertEquals("Incorrect @Handler element value.", 0, atHandler.money().length);
        assertEquals("Incorrect @Handler element value.", 0, atHandler.non_ordinary().length);
        assertEquals("Incorrect @Handler element value.", 0, atHandler.str().length);

        final DateParam atDateParam = atHandler.date()[0];
        assertEquals("Incorrect @DateParam element value.", "dateParam", atDateParam.name());
        assertEquals("Incorrect @DateParam element value.", "2011-12-01 00:00:00", atDateParam.value());
    }

    @Test
    public void added_one2many_special_case_property_is_generated_correctly() throws Exception {
        final IsProperty atIsProperty = new IsPropertyAnnotation(String.class, /*linkProperty*/ "key1").newInstance();
        final var np = NewProperty.create("one2manyAssociationSpecialCaseTestProperty",
                DetailsEntityForOneToManyAssociation.class, 
                "One2Many Special Case Association Property", "One2Many Special Case Association Property Description",
                atIsProperty);

        final var newType = cl.startModification(MasterEntityWithOneToManyAssociation.class)
                .addProperties(np)
                .endModification();

        final Field field = Finder.findFieldByName(newType, np.getName());
        assertNotNull("Added property %s was not found.".formatted(np.getName()), field);
        assertEquals("Incorrect type of the added property", 
                np.genericType().toString(), field.getGenericType().toString());

        assertAnnotationsEquals(np, field);
    }

    @Test
    public void added_one2many_collectional_property_is_generated_correctly() throws Exception {
        final IsProperty atIsProperty = new IsPropertyAnnotation(DetailsEntityForOneToManyAssociation.class, "key1").newInstance();
        final NewProperty<List> np = NewProperty.create("one2manyAssociationCollectionalTestProperty", List.class,
                "One2Many Collectional Association Property", "One2Many Collectional Association Property Description",
                atIsProperty);

        final var newType = cl.startModification(MasterEntityWithOneToManyAssociation.class)
                .addProperties(np)
                .endModification();

        final Field field = Finder.findFieldByName(newType, np.getName());
        assertNotNull("Added property %s was not found.".formatted(np.getName()), field);
        assertEquals("Incorrect type of the added property", 
                np.genericType().toString(), field.getGenericType().toString());

        assertAnnotationsEquals(np, field);
    }

    // TODO doesn't work with ByteBuddy
    @Test
    @Ignore
    public void test_to_ensure_that_property_name_with_dangerous_character_works() throws Exception {
        final String propName = "//firstProperty//";
        final NewProperty<String> exoticProperty = NewProperty.create(propName, String.class, "title", "desc");
        final Class<? extends AbstractEntity<String>> newType = cl.startModification(DEFAULT_ORIG_TYPE)
                .addProperties(exoticProperty)
                .endModification();
        try {
            Finder.findFieldByName(newType, propName);
        } catch (final Exception e) {
            fail("The exotic field should have been found");
        }
    }

    @Test
    public void class_annotations_can_be_added_after_adding_properties() throws Exception {
        final DescTitle atDescTitle = new DescTitleAnnotation("Title", "Description").newInstance();
        final Class<? extends TopLevelEntity> newType = cl.startModification(TopLevelEntity.class)
                .addProperties(np1)
                .addClassAnnotations(atDescTitle)
                .endModification();

        final DescTitle actualAtDescTitle = newType.getAnnotation(DescTitle.class);
        assertNotNull("Generated type is missing the provided class annotation.", actualAtDescTitle);
        assertEquals("Incorrect value of added annotation element.",
                atDescTitle.value(), actualAtDescTitle.value());
        assertEquals("Incorrect value of added annotation element.",
                atDescTitle.desc(), actualAtDescTitle.desc());

        // new type must have 2 new property descriptors: np1 (the added property) and 'desc' (from @DescTitle)
        assertEquals("Incorrect number of property descriptors found for the generated type.",
                Finder.getPropertyDescriptors(TopLevelEntity.class).size() + 2,
                Finder.getPropertyDescriptors(newType).size());
    }

    @Test
    public void deprecated_NewProperty_can_change_property_type() throws Exception {
        // Here we are testing 2 things:
        // 1. changing the type of an existing *simple* property
        // 2. changing the raw type of an existing collectional property
        for (final var nameAndNewType: List.of(Pair.pair("firstProperty", BigDecimal.class),
                                               Pair.pair("entities", Set.class)))
        {
            final String name = nameAndNewType.getKey();
            final Field origField = Finder.getFieldByName(DEFAULT_ORIG_TYPE, name);
            assertNotNull(origField); // make sure such a property exists

            final Class<?> newPropType = nameAndNewType.getValue();
            // make sure that new property type is actually different
            assertNotEquals(newPropType, origField.getType());

            // access original field type arguments, if any 
            final List<Type> origFieldTypeArguments = extractTypeArguments(origField.getGenericType());

            final NewProperty np = NewProperty.changeType(name, newPropType);
            final Class<? extends AbstractEntity<String>> enhancedType = cl.startModification(DEFAULT_ORIG_TYPE)
                    .modifyProperties(np)
                    .endModification();

            assertPropertyCorrectness(enhancedType, name, newPropType, 
                    // we only changed the raw type, so the last argument is the type arguments of the original field, 
                    // which should have been preserved
                    origFieldTypeArguments);
        }
    }

    @Test
    public void deprecated_NewProperty_can_change_property_type_arguments() throws Exception {
        // Here we are testing 3 things:
        // 1. changing type argument of a collectional property
        // 2. changing type argument of a PropertyDescriptor property
        // 3. changing type argument of any other parameterized type (e.g. Optional)

        // first, test 1. and 2. which affect @IsProperty.value()
        for (final var nameAndTypeArg: List.of(Pair.pair("entities", EntityBeingEnhanced.class),
                                               Pair.pair("propertyDescriptor", EntityBeingEnhanced.class)))
        {
            final String name = nameAndTypeArg.getKey();
            final Field origField = Finder.getFieldByName(DEFAULT_ORIG_TYPE, name);
            assertNotNull(origField); // make sure such a property exists

            final Class<?> typeArg = nameAndTypeArg.getValue();

            // access original field type arguments, if any 
            final List<Type> origFieldTypeArguments = extractTypeArguments(origField.getGenericType());

            final NewProperty np = NewProperty.changeTypeSignature(name, typeArg);
            final Class<? extends AbstractEntity<String>> newType = cl.startModification(DEFAULT_ORIG_TYPE)
                    .modifyProperties(np)
                    .endModification();

            final Field modifiedProperty = assertPropertyCorrectness(newType, name, 
                    origField.getType(), // expect the same raw type
                    List.of(typeArg)     // expect new type argument
                    );

            // make sure @IsProperty.value() was also modified
            final IsProperty atIsProperty = modifiedProperty.getAnnotation(IsProperty.class);
            assertNotNull("@IsProperty should be present.", atIsProperty);
            assertEquals("Incorrect value of @IsProperty.", typeArg, atIsProperty.value());
        }

        // now test 3.
        final String name = "maybeText";
        final Field origField = Finder.getFieldByName(DEFAULT_ORIG_TYPE, name);
        assertNotNull(origField); // make sure such a property exists

        final Class<?> typeArg = Integer.class;

        // access original field type arguments, if any 
        final List<Type> origFieldTypeArguments = extractTypeArguments(origField.getGenericType());

        final NewProperty np = NewProperty.changeTypeSignature(name, typeArg);
        final Class<? extends AbstractEntity<String>> newType = cl.startModification(DEFAULT_ORIG_TYPE)
                .modifyProperties(np)
                .endModification();

        final Field modifiedProperty = assertPropertyCorrectness(newType, name, 
                origField.getType(), // expect the same raw type
                List.of(typeArg)     // expect new type argument
                );

        // make sure @IsProperty.value() was unchanged
        final Class<?> origIsPropertyValue = origField.getAnnotation(IsProperty.class).value();
        final IsProperty atIsProperty = modifiedProperty.getAnnotation(IsProperty.class);
        assertNotNull("@IsProperty should be present.", atIsProperty);
        assertEquals("Incorrect value of @IsProperty.", origIsPropertyValue, atIsProperty.value());
    }

    private List<Type> extractTypeArguments(final Type type) {
        if (ParameterizedType.class.isInstance(type)) {
            return Arrays.asList(((ParameterizedType) type).getActualTypeArguments());
        }
        else return List.of();
    }

    private Field assertPropertyCorrectness(final Class<?> owningEnhancedType, final String name, final Class<?> expectedRawType, 
            final List<Type> expectedTypeArguments) 
    {
        final Field field = Finder.getFieldByName(owningEnhancedType, name);
        assertNotNull("Modified property should exist.", field);
        final List<Type> origFieldTypeArguments = extractTypeArguments(field.getGenericType());

        assertEquals("Incorrect property raw type.", expectedRawType, field.getType());
        assertEquals("Incorrect property type arguments.", expectedTypeArguments, origFieldTypeArguments);

        final Method accessor = Reflector.obtainPropertyAccessor(owningEnhancedType, name);
        assertEquals("Incorrect accessor return raw type.", expectedRawType, accessor.getReturnType());
        assertEquals("Incorrect accessor return type arguments.", 
                expectedTypeArguments, extractTypeArguments(accessor.getGenericReturnType()));

        final Method setter = Reflector.obtainPropertySetter(owningEnhancedType, name);
        assertEquals("Incorrect number of setter parameters.", 1, setter.getParameterCount());
        assertEquals("Incorrect setter parameter raw type.", expectedRawType, setter.getParameterTypes()[0]);
        assertEquals("Incorrect setter parameter type arguments.", 
                expectedTypeArguments, extractTypeArguments(setter.getGenericParameterTypes()[0]));
        return field;
    }
}
