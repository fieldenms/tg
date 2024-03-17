package ua.com.fielden.platform.reflection.asm.impl;

import com.google.inject.Injector;
import org.junit.Ignore;
import org.junit.Test;
import ua.com.fielden.platform.associations.one2many.DetailsEntityForOneToManyAssociation;
import ua.com.fielden.platform.associations.one2many.MasterEntityWithOneToManyAssociation;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.Entity;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.entity.annotation.factory.*;
import ua.com.fielden.platform.entity.annotation.mutator.BeforeChange;
import ua.com.fielden.platform.entity.annotation.mutator.DateParam;
import ua.com.fielden.platform.entity.annotation.mutator.Handler;
import ua.com.fielden.platform.entity.before_change_event_handling.BeforeChangeEventHandler;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.meta.IAfterChangeEventHandler;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.Reflector;
import ua.com.fielden.platform.reflection.asm.api.NewProperty;
import ua.com.fielden.platform.reflection.asm.impl.entities.TopLevelEntity;
import ua.com.fielden.platform.test.CommonTestEntityModuleWithPropertyFactory;
import ua.com.fielden.platform.test.EntityModuleWithPropertyFactory;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.utils.MiscUtilities;
import ua.com.fielden.platform.utils.Pair;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;

import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.junit.Assert.*;
import static ua.com.fielden.platform.entity.AbstractEntity.DESC;
import static ua.com.fielden.platform.reflection.Finder.*;
import static ua.com.fielden.platform.reflection.asm.api.test_utils.NewPropertyTestUtils.assertAnnotationsEquals;
import static ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader.startModification;
import static ua.com.fielden.platform.reflection.asm.impl.DynamicEntityTypeTestUtils.*;

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

    private static final IsProperty atIsPropertyWithPrecision = new IsPropertyAnnotation(19, 4).newInstance();
    private static final Calculated atCalculated = new CalculatedAnnotation().contextualExpression(NEW_PROPERTY_EXPRESSION).newInstance();
    private static final Calculated atCalculatedBool = new CalculatedAnnotation().contextualExpression(NEW_PROPERTY_EXPRESSION_BOOL)
            .newInstance();

    // simple properties
    private static final NewProperty<Money> np1 = NewProperty.create(NEW_PROPERTY_1, Money.class, NEW_PROPERTY_TITLE, NEW_PROPERTY_DESC,
            atIsPropertyWithPrecision, atCalculated);
    private static final NewProperty<Money> np2 = NewProperty.create(NEW_PROPERTY_2, Money.class, NEW_PROPERTY_TITLE, NEW_PROPERTY_DESC, 
            atCalculated);
    private static final NewProperty<Boolean> npBool = NewProperty.create(NEW_PROPERTY_BOOL, boolean.class, 
            NEW_PROPERTY_TITLE, NEW_PROPERTY_DESC, atCalculatedBool);

    // collectional properties
    @SuppressWarnings("rawtypes")
    private static final NewProperty<List> npRawList = NewProperty.create("rawListTestProperty", List.class, "Collectional Property", 
            "Collectional Property Description", new IsPropertyAnnotation(String.class).newInstance());
    @SuppressWarnings("rawtypes")
    private static final NewProperty<List> npParamList = NewProperty.create("paramListTestProperty", List.class, List.of(String.class),
            "Collectional Property", "Collectional Property Description", new IsPropertyAnnotation(String.class).newInstance());

    // PropertyDescriptor properties
    @SuppressWarnings("rawtypes")
    private static final NewProperty<PropertyDescriptor> npRawPropDescriptor = NewProperty.create("propertyDescriptorTestProperyt",
            PropertyDescriptor.class, "PropertyDescriptor property", "PropertyDescriptor property description",
            new IsPropertyAnnotation(TopLevelEntity.class).newInstance());
    @SuppressWarnings("rawtypes")
    private static final NewProperty<PropertyDescriptor> npParamPropDescriptor = NewProperty.create("propertyDescriptorTestProperyt",
            PropertyDescriptor.class, List.of(TopLevelEntity.class),
            "PropertyDescriptor property", "PropertyDescriptor property description",
            new IsPropertyAnnotation(TopLevelEntity.class).newInstance());

    @Test
    public void properties_can_be_added() throws Exception {
        // 1. add a single property
        final Class<? extends AbstractEntity<String>> newType1 = startModification(DEFAULT_ORIG_TYPE)
                .addProperties(npBool)
                .endModification();

        assertEquals("Incorrect number of properties in the generated type.", 
                Finder.findProperties(DEFAULT_ORIG_TYPE).size() + 1, Finder.findProperties(newType1).size());

        final Field field1 = Finder.findFieldByName(newType1, npBool.getName());
        assertNotNull("Added property %s was not found.".formatted(npBool.getName()), field1);
        assertEquals("Incorrect type of the added property.", 
                npBool.genericTypeAsDeclared().toString(), field1.getGenericType().toString());

        // make sure all provided property annotations were generated
        assertAnnotationsEquals(npBool, field1);

        // 2. add multiple properties at once
        final Set<NewProperty<?>> newProperties = Set.of(npBool, np1, np2);
        final Class<? extends AbstractEntity<String>> newType2 = startModification(DEFAULT_ORIG_TYPE)
                .addProperties(newProperties)
                .endModification();

        assertEquals("Incorrect number of properties in the generated type.", 
                Finder.findProperties(DEFAULT_ORIG_TYPE).size() + newProperties.size(),
                Finder.findProperties(newType2).size());

        newProperties.forEach(np -> {
            final Field field = Finder.findFieldByName(newType2, np.getName());
            assertNotNull("Added property %s was not found.".formatted(np.getName()), field);
            assertEquals("Incorrect type of the added property.", 
                    np.genericTypeAsDeclared().toString(), field.getGenericType().toString());

            // make sure all provided property annotations were generated
            assertAnnotationsEquals(np, field);
        });


        // 3. add multiple properties sequentially
        final TypeMaker<? extends AbstractEntity<String>> builder = startModification(DEFAULT_ORIG_TYPE);
        newProperties.forEach(builder::addProperties);
        final Class<? extends AbstractEntity<String>> newType3 = builder.endModification();

        assertEquals("Incorrect number of properties in the generated type.", 
                Finder.findProperties(DEFAULT_ORIG_TYPE).size() + newProperties.size(),
                Finder.findProperties(newType3).size());

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
    public void added_properties_are_annotated_with_Generated() throws Exception {
        final Set<NewProperty<?>> newProperties = Set.of(np1, npBool, npRawList, npParamPropDescriptor);
        final Class<? extends AbstractEntity<String>> newType = startModification(DEFAULT_ORIG_TYPE)
                .addProperties(newProperties)
                .endModification();
        
        for (final NewProperty<?> np: newProperties) {
            final Field field = newType.getDeclaredField(np.getName());
            assertNotNull("Added property %s was not found.".formatted(np.getName()), field);
            assertTrue("Added property %s is missing @Generated annotation.".formatted(np.getName()),
                    field.isAnnotationPresent(Generated.class));
        }
    }

    @Test
    public void generated_types_can_be_renamed_after_adding_new_properties() throws Exception {
        final String newTypeName = DynamicTypeNamingService.nextTypeName(DEFAULT_ORIG_TYPE.getName());
        final Class<? extends AbstractEntity<String>> newType = startModification(DEFAULT_ORIG_TYPE)
                .addProperties(np1)
                .modifyTypeName(newTypeName)
                .endModification();

        assertEquals("Incorrect type name.", newTypeName, newType.getName());

        assertHasProperties(DEFAULT_ORIG_TYPE, newType);
        // assert that the added property is generated correctly
        assertGeneratedPropertyCorrectness(np1, newType);
        assertEquals("Incorrect number of properties found in the generated type.",
                findProperties(DEFAULT_ORIG_TYPE).size() + 1, findProperties(newType).size());
    }

    @Test
    public void only_distinct_properties_are_added() throws Exception {
        final Class<? extends AbstractEntity<String>> newType1 = startModification(DEFAULT_ORIG_TYPE)
                .addProperties(np1, np1, np1)
                .endModification(); 

        assertEquals("Incorrect number of properties in the generated type.", 
                Finder.findProperties(DEFAULT_ORIG_TYPE).size() + 1, Finder.findProperties(newType1).size());

        final Class<? extends AbstractEntity<String>> newType2 = startModification(DEFAULT_ORIG_TYPE)
                .addProperties(np1)
                .addProperties(np1)
                .addProperties(np1)
                .endModification(); 
        assertEquals("Incorrect number of properties in the generated type.", 
                Finder.findProperties(DEFAULT_ORIG_TYPE).size() + 1, Finder.findProperties(newType2).size());
    }

    /**
     * Adding a property should result in the generated type declaring:
     * <ul>
	 *   <li>A single field with the name of the added property</li>
	 *   <li>A single accessor method for that property with a respective name</li>
	 *   <li>A single setter method for that property</li>
	 * </ul>
	 * The purpose of this test is to ensure that no class members with the same name are generated as a result of adding properties.
     */
    @Test
    public void for_each_modified_property_generated_type_contains_a_field_and_acessor_and_setter_methods() throws Exception {
        final Class<? extends Entity> newType = startModification(Entity.class)
                .addProperties(np1)
                .endModification();
        
        // declared fields
        assertEquals("Incorrect number of declared fields by a generated type for a modified property.",
                1, Arrays.stream(newType.getDeclaredFields()).filter(field -> field.getName().equals(np1.getName())).count());
        // declared accessor method
        assertEquals("Incorrect number of declared accessor methods by a generated type for a modified property.",
                1, Arrays.stream(newType.getDeclaredMethods())
                        .filter(method -> method.getName().equals("get" + capitalize(np1.getName())))
                        .count());
        // declared setter method
        assertEquals("Incorrect number of declared setter methods by a generated type for a modified property.",
                1, Arrays.stream(newType.getDeclaredMethods())
                        .filter(method -> method.getName().equals("set" + capitalize(np1.getName())))
                        .count());
    }

    @Test
    public void adding_a_property_with_the_same_name_as_one_declared_by_the_original_type_has_no_effect() throws Exception {
        // property with name "money" is already present in Entity
        final NewProperty<Money> existingProp = NewProperty.create("money", Money.class, "title", "desc");
        final Class<? extends Entity> newType = startModification(Entity.class)
                .addProperties(existingProp) // should be ignored
                .endModification();
        
        assertThrows(NoSuchFieldException.class, () -> newType.getDeclaredField(existingProp.getName()));
    }

    @Test
    public void adding_a_property_with_the_same_name_as_one_inherited_by_the_original_type_has_no_effect() throws Exception {
        // let's add a genuinely new property
        final Class<? extends Entity> newType1 = startModification(Entity.class)
                .addProperties(np1)
                .endModification();

        // and now try to add a property, which exists in Entity, to the generated type that is based on Entity
        final NewProperty<Money> existingProp = NewProperty.create("money", Money.class, "title", "desc");
        final Class<? extends Entity> newType2 = startModification(newType1)
                .addProperties(existingProp) // should be ignored
                .endModification();
        
        assertFieldExists(newType2, "money");
        assertThrows(NoSuchFieldException.class, () -> newType2.getDeclaredField("money"));
    }

    @Test
    public void properties_can_be_added_to_enhanced_types() throws Exception {
        final Class<? extends AbstractEntity<String>> newType1 = startModification(DEFAULT_ORIG_TYPE)
                .addProperties(np1)
                .endModification();
        final Class<? extends AbstractEntity<String>> newType2 = startModification(newType1)
                .addProperties(np2)
                .endModification();

        assertEquals("Incorrect number of properties in the generated type.", 
                Finder.findProperties(DEFAULT_ORIG_TYPE).size() + 2, Finder.findProperties(newType2).size());

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
                "title", "desc").setValueSupplier(() -> value);

        final Class<? extends AbstractEntity<String>> newType = startModification(DEFAULT_ORIG_TYPE)
                .addProperties(npExplicitInit)
                .endModification();

        final Field field = Finder.getFieldByName(newType, npExplicitInit.getName());
        assertNotNull("Added property %s was not found.".formatted(npExplicitInit.getName()), field);
        final AbstractEntity<String> instance = factory.newByKey(newType, "new");
        assertEquals("Incorrect value of the added property %s.".formatted(npExplicitInit.getName()),
                value, Finder.getFieldValue(field, instance));
    }

    @Test
    public void added_list_property_has_unique_empty_ArrayList_as_value_in_every_instance() throws Exception {
        final var newPropName = "newPropList";
        final var newProp = NewProperty.create(newPropName, List.class, "title", "desc", new IsPropertyAnnotation(String.class, "--stub-link-property--").newInstance());
        final var newType = startModification(DEFAULT_ORIG_TYPE).addProperties(newProp).endModification();

        final var field = Finder.getFieldByName(newType, newProp.getName());
        assertNotNull(field);
        final var entity = factory.newByKey(newType, "new1");
        assertNotNull(entity.get(newPropName));
        assertEquals(ArrayList.class, entity.get(newPropName).getClass());
        assertEquals(new ArrayList<>(), entity.get(newPropName));

        final var otherEntityWithSameType = factory.newByKey(newType, "new2");
        // collection must be unique in different instances, otherwise we are risking with mutability of shared collections, especially in our standard scenario with clear()+addAll() -based setters
        assertFalse(otherEntityWithSameType.get(newPropName) == entity.get(newPropName)); 
    }

    @Test
    public void added_set_property_has_unique_empty_HashSet_as_value_in_every_instance() throws Exception {
        final var newPropName = "newPropSet";
        final var newProp = NewProperty.create(newPropName, Set.class, "title", "desc", new IsPropertyAnnotation(String.class, "--stub-link-property--").newInstance());
        final var newType = startModification(DEFAULT_ORIG_TYPE).addProperties(newProp).endModification();

        final var field = Finder.getFieldByName(newType, newProp.getName());
        assertNotNull(field);
        final var entity = factory.newByKey(newType, "new1");
        assertNotNull(entity.get(newPropName));
        assertEquals(HashSet.class, entity.get(newPropName).getClass());
        assertEquals(new HashSet<>(), entity.get(newPropName));

        final var otherEntityWithSameType = factory.newByKey(newType, "new2");
        // collection must be unique in different instances, otherwise we are risking with mutability of shared collections, especially in our standard scenario with clear()+addAll() -based setters
        assertFalse(otherEntityWithSameType.get(newPropName) == entity.get(newPropName)); 
    }

    @Test
    public void added_concrete_collectional_property_has_unique_empty_concrete_collection_as_value_in_every_instance() throws Exception {
        final var newPropName = "newPropLinkedHashSet";
        final var newProp = NewProperty.create(newPropName, LinkedHashSet.class, "title", "desc", new IsPropertyAnnotation(String.class, "--stub-link-property--").newInstance());
        final var newType = startModification(DEFAULT_ORIG_TYPE).addProperties(newProp).endModification();

        final var field = Finder.getFieldByName(newType, newProp.getName());
        assertNotNull(field);
        final var entity = factory.newByKey(newType, "new1");
        assertNotNull(entity.get(newPropName));
        assertEquals(LinkedHashSet.class, entity.get(newPropName).getClass());
        assertEquals(new LinkedHashSet<>(), entity.get(newPropName));

        final var otherEntityWithSameType = factory.newByKey(newType, "new2");
        // collection must be unique in different instances, otherwise we are risking with mutability of shared collections, especially in our standard scenario with clear()+addAll() -based setters
        assertFalse(otherEntityWithSameType.get(newPropName) == entity.get(newPropName)); 
    }

    @Test
    public void MetaProperty_can_be_obtained_for_an_added_property() throws Exception {
        final Class<? extends AbstractEntity<String>> newType = startModification(DEFAULT_ORIG_TYPE)
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
        final Class<? extends AbstractEntity<String>> newType = startModification(DEFAULT_ORIG_TYPE)
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

    /**
     * If a {@link PropertyDescriptor} property prototype has the following form:
     * <pre>
     * {@literal @}IsProperty(Entity.class) PropertyDescriptor ${name}
     * </pre>
     * Then it will be generated in the following form:
     * <pre>
     * {@literal @}IsProperty(Entity.class) PropertyDescriptor{@literal <Entity>} ${name}
     * </pre>
     * 
     * @throws Exception
     */
    @Test
    public void the_type_of_added_raw_PropertyDescriptor_property_is_parameterized_with_the_value_of_IsProperty() throws Exception {
        final Function<String, String> formatter = MiscUtilities.stringFormatter(npRawPropDescriptor.getName());

        final Class<? extends AbstractEntity<String>> newType = startModification(DEFAULT_ORIG_TYPE)
                .addProperties(npRawPropDescriptor)
                .endModification();

        final Field field = Finder.findFieldByName(newType, npRawPropDescriptor.getName());
        assertNotNull("Added property %s was not found.".formatted(npRawPropDescriptor.getName()), field);

        // make sure all provided property annotations were generated
        assertAnnotationsEquals(npRawPropDescriptor, field);

        final Type fieldGenericType = field.getGenericType();
        assertTrue(formatter.apply("Type of added PropertyDescriptor property %s is not a parameterized one."),
                ParameterizedType.class.isInstance(fieldGenericType));
        final ParameterizedType fieldParamType = (ParameterizedType) fieldGenericType;

        // check the raw type
        assertEquals(formatter.apply("Incorrect raw type of added PropertyDescriptor property %s."),
                npRawPropDescriptor.getRawType(), fieldParamType.getRawType());

        // check the type arguments
        final Type[] typeArguments = fieldParamType.getActualTypeArguments();
        assertEquals(formatter.apply("Incorrect number of type arguments for added PropertyDescriptor property %s."),
                1, typeArguments.length);
        assertEquals(formatter.apply("Incorrect type argument of added PropertyDescriptor property %s."),
                npRawPropDescriptor.getIsProperty().value(), typeArguments[0]);
    }

    /**
     * If a collectional property prototype has the following form:
     * <pre>
     * {@literal @}IsProperty(Entity.class) PropertyDescriptor{@literal <Entity>} ${name}
     * </pre>
     * Then it will be generated having the same form.
     * 
     * @throws Exception
     */
    @Test
    public void the_type_of_added_parameterized_PropertyDescriptor_property_is_parameterized_with_provided_type_arguments() throws Exception {
        final Function<String, String> formatter = MiscUtilities.stringFormatter(npParamPropDescriptor.getName());

        final Class<? extends AbstractEntity<String>> newType = startModification(DEFAULT_ORIG_TYPE)
                .addProperties(npParamPropDescriptor)
                .endModification();

        final Field field = Finder.findFieldByName(newType, npParamPropDescriptor.getName());
        assertNotNull(formatter.apply("Added property %s was not found."), field);

        // make sure all provided property annotations were generated
        assertAnnotationsEquals(npParamPropDescriptor, field);

        assertTrue(formatter.apply("Type of added PropertyDescriptor property %s is not a parameterized one."),
                ParameterizedType.class.isInstance(field.getGenericType()));
        final ParameterizedType fieldParamType = (ParameterizedType) field.getGenericType();

        // check the raw type
        assertEquals(formatter.apply("Incorrect raw type of added PropertyDescriptor property %s."),
                npParamPropDescriptor.getRawType(), field.getType());

        // check the type arguments
        final Type[] typeArguments = fieldParamType.getActualTypeArguments();
        assertEquals(formatter.apply("Incorrect number of type arguments for added PropertyDescriptor property %s."),
                npParamPropDescriptor.getTypeArguments().size(), typeArguments.length);
        assertEquals(formatter.apply("Incorrect type arguments of added PropertyDescriptor property %s."),
                npParamPropDescriptor.getTypeArguments(), Arrays.asList(typeArguments));
    }


    /**
     * If a collectional property prototype has the following form:
     * <pre>
     * {@literal @}IsProperty(String.class) List ${name}
     * </pre>
     * Then it will be generated in the following form:
     * <pre>
     * {@literal @}IsProperty(String.class) List{@literal <String>} ${name}
     * </pre>
     * @throws Exception
     */
    @Test
    public void the_type_of_added_raw_collectional_property_is_parameterized_with_the_value_of_IsProperty() throws Exception {
        final Function<String, String> formatter = MiscUtilities.stringFormatter(npRawList.getName());

        final Class<? extends AbstractEntity<String>> newType = startModification(DEFAULT_ORIG_TYPE)
                .addProperties(npRawList)
                .endModification();

        final Field field = Finder.findFieldByName(newType, npRawList.getName());
        assertNotNull(formatter.apply("Added property %s was not found."), field);
        assertTrue(formatter.apply("Added collectional property %s is not of collectional type."),
                Collection.class.isAssignableFrom(field.getType()));

        // make sure all provided property annotations were generated
        assertAnnotationsEquals(npRawList, field);

        assertTrue(formatter.apply("Type of added collectional property %s is not a parameterized one."),
                ParameterizedType.class.isInstance(field.getGenericType()));
        final ParameterizedType fieldParamType = (ParameterizedType) field.getGenericType();

        // check the raw type
        assertEquals(formatter.apply("Incorrect raw type of added collectional property %s."),
                npRawList.getRawType(), fieldParamType.getRawType());

        // check the type arguments
        final Type[] typeArguments = fieldParamType.getActualTypeArguments();
        assertEquals(formatter.apply("Incorrect number of type arguments for added collectional property %s."),
                1, typeArguments.length);
        assertEquals(formatter.apply("Incorrect type argument of added collectional property %s."),
                npRawList.getAnnotationByType(IsProperty.class).value(), typeArguments[0]);
    }

    /**
     * If a collectional property prototype has the following form:
     * <pre>
     * {@literal @}IsProperty(String.class) List{@literal <String>} ${name}
     * </pre>
     * Then it will be generated having the same form.
     * @throws Exception
     */
    @Test
    public void the_type_of_added_parameterized_collectional_property_is_parameterized_with_provided_type_arguments() throws Exception {
        final Function<String, String> formatter = MiscUtilities.stringFormatter(npParamList.getName());

        final Class<? extends AbstractEntity<String>> newType = startModification(DEFAULT_ORIG_TYPE)
                .addProperties(npParamList)
                .endModification();

        final Field field = Finder.findFieldByName(newType, npParamList.getName());
        assertNotNull(formatter.apply("Added property %s was not found."), field);
        assertTrue(formatter.apply("Added collectional property %s is not of collectional type."),
                Collection.class.isAssignableFrom(field.getType()));

        // make sure all provided property annotations were generated
        assertAnnotationsEquals(npParamList, field);

        assertTrue(formatter.apply("Type of added collectional property %s is not a parameterized one."),
                ParameterizedType.class.isInstance(field.getGenericType()));
        final ParameterizedType fieldParamType = (ParameterizedType) field.getGenericType();

        // check the raw type
        assertEquals(formatter.apply("Incorrect raw type of added collectional property %s."),
                npParamList.getRawType(), field.getType());

        // check the type arguments
        final Type[] typeArguments = fieldParamType.getActualTypeArguments();
        assertEquals(formatter.apply("Incorrect number of type arguments for added collectional property %s."),
                npParamList.getTypeArguments().size(), typeArguments.length);
        assertArrayEquals(formatter.apply("Incorrect type arguments of added collectional property %s."),
                npParamList.getTypeArguments().toArray(Type[]::new), typeArguments);
    }

    @Test
    public void added_collectional_properties_are_initialized_by_default() throws Exception {
        // test both raw collectional properties and parameterized ones
        for (final NewProperty<? extends Collection> np: List.of(npRawList, npParamList)) {
            final Class<? extends AbstractEntity<String>> newType = startModification(DEFAULT_ORIG_TYPE)
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

                final Class<? extends AbstractEntity<String>> newType = startModification(DEFAULT_ORIG_TYPE)
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

                final Class<? extends AbstractEntity<String>> newType = startModification(DEFAULT_ORIG_TYPE)
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
            final Function<String, String> formatter = MiscUtilities.stringFormatter(np.getName());

            final Class<? extends AbstractEntity<String>> newType = startModification(DEFAULT_ORIG_TYPE)
                    .addProperties(np)
                    .endModification();

            final Method getter;
            try {
                getter = Reflector.obtainPropertyAccessor(newType, np.getName());
            } catch (final Exception e) {
                fail(formatter.apply("Getter for added collectional property %s was not found."));
                return;
            }

            assertEquals(formatter.apply("Incorrect getter return type for added collectional property %s."),
                    np.genericType().toString(), getter.getGenericReturnType().toString());

            // instantiate the generated type and try to invoke the getter
            final AbstractEntity<String> instance = factory.newByKey(newType, "new");
            final List<String> list = List.of("hello");

            try {
                getter.invoke(instance);
            } catch (final Exception e) {
                fail(formatter.apply("Failed to invoke getter for added collectional property %s."));
                return;
            }
        }
    }

    @Test
    public void setters_are_generated_correctly_for_added_collectional_properties() throws Exception {
        // test both raw and parameterized collectional properties
        for (final NewProperty<? extends Collection> np: List.of(npRawList, npParamList)) {
            final Function<String, String> formatter = MiscUtilities.stringFormatter(np.getName());

            final Class<? extends AbstractEntity<String>> newType = startModification(DEFAULT_ORIG_TYPE)
                    .addProperties(np)
                    .endModification();

            final Method setter;
            try {
                setter = Reflector.obtainPropertySetter(newType, np.getName());
            } catch (final Exception e) {
                fail(formatter.apply("Setter for added collectional property %s was not found."));
                return;
            }

            final Type[] parameterTypes = setter.getGenericParameterTypes();
            assertEquals(formatter.apply("Incorrect number of setter generic parameters for added collectional property %s."),
                    1, parameterTypes.length);
            assertEquals(formatter.apply("Incorrect setter parameter type for added collectional property %s."),
                    np.genericType().toString(), parameterTypes[0].toString());

            // instantiate the generated type and try to set the value of added property 
            final AbstractEntity<String> instance = factory.newByKey(newType, "new");
            final List<String> list1 = List.of("hello");
            setter.invoke(instance, list1);
            assertEquals(formatter.apply("The value of added collectional property %s was set incorrectly."),
                    list1, getFieldValue(findFieldByName(newType, np.getName()), instance));
            
            // now set the value once again to make sure the setter indeed is generated correctly
            // the old collection contents should be cleared, then provided elements should be added
            final List<String> list2 = List.of("world");
            setter.invoke(instance, list2);
            assertEquals(formatter.apply("The value of added collectional property %s was set incorrectly."),
                    list2, getFieldValue(findFieldByName(newType, np.getName()), instance));
        }
    }

    @Test
    public void getter_returns_correct_value_after_setter_invokation_for_added_collectional_property() throws Exception {
        // test both raw and parameterized collectional properties
        for (final NewProperty<? extends Collection> np: List.of(npRawList, npParamList)) {
            final Function<String, String> formatter = MiscUtilities.stringFormatter(np.getName());

            final Class<? extends AbstractEntity<String>> newType = startModification(DEFAULT_ORIG_TYPE)
                    .addProperties(np)
                    .endModification();

            final Method setter;
            try {
                setter = Reflector.obtainPropertySetter(newType, np.getName());
            } catch (final Exception e) {
                fail(formatter.apply("Setter for added collectional property %s was not found."));
                return;
            }

            // instantiate the generated type and try to set the value of added property 
            final AbstractEntity<String> instance = factory.newByKey(newType, "new");
            final List<String> list1 = List.of("hello");
            setter.invoke(instance, list1);

            final Method getter;
            try {
                getter = Reflector.obtainPropertyAccessor(newType, np.getName());
            } catch (final Exception e) {
                fail(formatter.apply("Getter for added collectional property %s was not found."));
                return;
            }

            assertEquals(formatter.apply("Incorrect getter return value for added collectional property %s."),
                    list1, getter.invoke(instance));
            
            // now set the value once again
            // the old collection contents should be cleared, then provided elements should be added
            final List<String> list2 = List.of("world");
            setter.invoke(instance, list2);
            assertEquals(formatter.apply("Incorrect getter return value for added collectional property %s."),
                    list2, getter.invoke(instance));
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

        final Class<? extends AbstractEntity<String>> newType = startModification(DEFAULT_ORIG_TYPE)
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

        final var newType = startModification(MasterEntityWithOneToManyAssociation.class)
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

        final var newType = startModification(MasterEntityWithOneToManyAssociation.class)
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
        final Class<? extends AbstractEntity<String>> newType = startModification(DEFAULT_ORIG_TYPE)
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
        final Class<? extends TopLevelEntity> newType = startModification(TopLevelEntity.class)
                .addProperties(np1)
                .addClassAnnotations(atDescTitle)
                .endModification();

        final DescTitle actualAtDescTitle = newType.getAnnotation(DescTitle.class);
        assertNotNull("Generated type is missing the provided class annotation.", actualAtDescTitle);
        assertEquals("Incorrect value of added annotation element.", atDescTitle.value(), actualAtDescTitle.value());
        assertEquals("Incorrect value of added annotation element.", atDescTitle.desc(), actualAtDescTitle.desc());

        assertHasProperties(TopLevelEntity.class, newType);

        // assert that the added property exists
        assertGeneratedPropertyCorrectness(np1, newType);
        // assert that "desc" property is a real property
        assertTrue("Real property [%s] wasn't found in the generated type.".formatted(DESC),
                findRealProperties(newType).stream().map(Field::getName).anyMatch(name -> name.equals(DESC)));
        // generated type should have 2 new real properties: np1 and "desc"
        assertEquals("Incorrect number of real properties found in the generated type.",
                findRealProperties(TopLevelEntity.class).size() + 2, findRealProperties(newType).size());
    }

    @Test
    public void added_collectional_properties_can_be_initialised_explicitly_with_equal_but_referentially_different_values() throws Exception {
        final String npName = "paramListTestProperty";
        final NewProperty<Set> npParamSet = NewProperty.create(npName, Set.class, List.of(String.class),
                "Collectional Property", "Collectional Property Description", new IsPropertyAnnotation(String.class).newInstance())
                .setValueSupplier(() -> new TreeSet<String>());

        final Class<? extends AbstractEntity<String>> newType = startModification(DEFAULT_ORIG_TYPE)
                .addProperties(npParamSet)
                .endModification();

        final Field field = Finder.getFieldByName(newType, npParamSet.getName());
        assertNotNull(field);
        final AbstractEntity<String> instance1 = factory.newByKey(newType, "new1");
        final AbstractEntity<String> instance2 = factory.newByKey(newType, "new2");
        final Set<String> instance1_paramListTestProperty = instance1.get(npName);
        final Set<String> instance2_paramListTestProperty = instance2.get(npName);

        assertTrue(instance1_paramListTestProperty instanceof TreeSet<String>);
        assertTrue(instance2_paramListTestProperty instanceof TreeSet<String>);
        assertEquals(instance1_paramListTestProperty, instance2_paramListTestProperty);
        assertNotEquals(System.identityHashCode(instance1_paramListTestProperty), System.identityHashCode(instance2_paramListTestProperty));
    }

}
