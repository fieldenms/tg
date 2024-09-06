package ua.com.fielden.platform.reflection.asm.impl;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.reflection.Finder.findFieldByName;
import static ua.com.fielden.platform.reflection.Finder.getFieldValue;
import static ua.com.fielden.platform.reflection.asm.api.test_utils.NewPropertyTestUtils.assertPropertyEquals;
import static ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader.startModification;
import static ua.com.fielden.platform.reflection.asm.impl.DynamicEntityTypeTestUtils.assertFieldExists;
import static ua.com.fielden.platform.reflection.asm.impl.DynamicEntityTypeTestUtils.assertGeneratedPropertyAccessorSignature;
import static ua.com.fielden.platform.reflection.asm.impl.DynamicEntityTypeTestUtils.assertGeneratedPropertyCorrectness;
import static ua.com.fielden.platform.reflection.asm.impl.DynamicEntityTypeTestUtils.assertGeneratedPropertySetterSignature;
import static ua.com.fielden.platform.reflection.asm.impl.DynamicEntityTypeTestUtils.assertInstantiation;
import static ua.com.fielden.platform.utils.CollectionUtil.linkedSetOf;
import static ua.com.fielden.platform.utils.Pair.pair;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Before;
import org.junit.Test;

import com.google.inject.Injector;

import ua.com.fielden.platform.associations.one2many.DetailsEntityForOneToManyAssociation;
import ua.com.fielden.platform.associations.one2many.MasterEntityWithOneToManyAssociation;
import ua.com.fielden.platform.associations.one2many.MasterEntityWithOneToManyCollectionalAssociationProvidedWithLinkPropValue;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.Entity;
import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.entity.annotation.Generated;
import ua.com.fielden.platform.entity.annotation.factory.CalculatedAnnotation;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.meta.IAfterChangeEventHandler;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.asm.api.NewProperty;
import ua.com.fielden.platform.reflection.asm.impl.entities.EntityBeingEnhanced;
import ua.com.fielden.platform.reflection.asm.impl.entities.EntityBeingModified;
import ua.com.fielden.platform.reflection.asm.impl.entities.EntityBeingModifiedWithInnerTypes;
import ua.com.fielden.platform.reflection.asm.impl.entities.EntityBeingModifiedWithInnerTypes.InnerEnum;
import ua.com.fielden.platform.reflection.asm.impl.entities.EntityWithCollectionalPropety;
import ua.com.fielden.platform.reflection.asm.impl.entities.TopLevelEntity;
import ua.com.fielden.platform.test.CommonTestEntityModuleWithPropertyFactory;
import ua.com.fielden.platform.test.EntityModuleWithPropertyFactory;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.utils.CollectionUtil;

/**
 * A test case to ensure correct dynamic modification of entity types by means of changing existing properties.
 *
 * @author TG Team
 *
 */
public class DynamicEntityTypePropertiesModificationTest {
    private static final String NEW_PROPERTY_DESC = "Description  for new money property";
    private static final String NEW_PROPERTY_TITLE = "New money property";
    private static final String NEW_PROPERTY_EXPRESSION = "2 * 3 - [integerProp]";
    private static final String NEW_PROPERTY = "newProperty";

    private static final Calculated calculated = new CalculatedAnnotation().contextualExpression(NEW_PROPERTY_EXPRESSION).newInstance();

    private static final NewProperty<Money> npMoney = NewProperty.create(NEW_PROPERTY, Money.class, NEW_PROPERTY_TITLE, NEW_PROPERTY_DESC,
            calculated);

    private boolean observed = false;
    private final EntityModuleWithPropertyFactory module = new CommonTestEntityModuleWithPropertyFactory();
    private final Injector injector = new ApplicationInjectorFactory().add(module).getInjector();
    private final EntityFactory factory = injector.getInstance(EntityFactory.class);

    @Before
    public void setUp() {
        observed = false;
    }

    @Test
    public void a_single_property_can_be_modified() throws Exception {
        final Field oldField = EntityBeingEnhanced.class.getDeclaredField("prop1");

        final NewProperty<Double> np = NewProperty.fromField(oldField).changeType(Double.class);
        final Class<? extends AbstractEntity<?>> newType = startModification(EntityBeingEnhanced.class)
                .modifyProperties(np)
                .endModification();

        assertEquals("Incorrect number of property descriptors in the generated type.", 
                Finder.getPropertyDescriptors(EntityBeingEnhanced.class).size(),
                Finder.getPropertyDescriptors(newType).size());

        final Field newField = assertFieldExists(newType, np.getName());
        assertEquals("Incorrect type of modified property.", 
                np.genericType().toString(), newField.getGenericType().toString());

        assertTrue("Not all property annotations were preserved.",
                asList(newField.getDeclaredAnnotations()).containsAll(asList(oldField.getDeclaredAnnotations())));

        assertInstantiation(newType, factory);
    }

    @Test
    public void multiple_properties_can_be_modified_at_once() throws Exception {
        final Class<? extends AbstractEntity<?>> origType = Entity.class;
        final Field oldField1 = origType.getDeclaredField("firstProperty");
        final Field oldField2 = origType.getDeclaredField("entity");
        final Field oldField3 = origType.getDeclaredField("observableProperty");

        final NewProperty<Double> np1 = NewProperty.fromField(oldField1).changeType(Double.class);
        final NewProperty<TopLevelEntity> np2 = NewProperty.fromField(oldField2).changeType(TopLevelEntity.class);
        final NewProperty<String> np3 = NewProperty.fromField(oldField3).changeType(String.class);
        final Class<? extends AbstractEntity<?>> newType = startModification(origType)
                .modifyProperties(np1, np2, np3)
                .endModification();

        assertEquals("Incorrect number of property descriptors in the generated type.", 
                Finder.getPropertyDescriptors(origType).size(),
                Finder.getPropertyDescriptors(newType).size());

        for (final var npAndOldField: List.of(pair(np1, oldField1), pair(np2, oldField2), pair(np3, oldField3))) {
            final NewProperty<?> np = npAndOldField.getKey();
            final Field oldField = npAndOldField.getValue();

            final Field newField = assertFieldExists(newType, np.getName());
            assertEquals("Incorrect type of modified property.", 
                    np.genericType().toString(), newField.getGenericType().toString());

            assertTrue("Not all property annotations were preserved.",
                    asList(newField.getDeclaredAnnotations()).containsAll(asList(oldField.getDeclaredAnnotations())));
        }

        assertInstantiation(newType, factory);
    }

    @Test
    public void multiple_properties_can_be_modified_sequentially() throws Exception {
        final Class<? extends AbstractEntity<?>> origType = Entity.class;
        final Field oldField1 = origType.getDeclaredField("firstProperty");
        final Field oldField2 = origType.getDeclaredField("entity");
        final Field oldField3 = origType.getDeclaredField("observableProperty");

        final NewProperty<Double> np1 = NewProperty.fromField(oldField1).changeType(Double.class);
        final NewProperty<TopLevelEntity> np2 = NewProperty.fromField(oldField2).changeType(TopLevelEntity.class);
        final NewProperty<String> np3 = NewProperty.fromField(oldField3).changeType(String.class);

        final TypeMaker<? extends AbstractEntity<?>> builder = startModification(origType);
        List.of(np1, np2, np3).forEach(builder::modifyProperties);
        final Class<? extends AbstractEntity<?>> newType = builder.endModification();

        assertEquals("Incorrect number of property descriptors in the generated type.", 
                Finder.getPropertyDescriptors(origType).size(),
                Finder.getPropertyDescriptors(newType).size());

        for (final var npAndOldField: List.of(pair(np1, oldField1), pair(np2, oldField2), pair(np3, oldField3))) {
            final NewProperty<?> np = npAndOldField.getKey();
            final Field oldField = npAndOldField.getValue();

            final Field newField = assertFieldExists(newType, np.getName());
            assertEquals("Incorrect type of modified property %s.".formatted(np.getName()), 
                    np.genericType().toString(), newField.getGenericType().toString());

            assertTrue("Not all property annotations were preserved.",
                    asList(newField.getDeclaredAnnotations()).containsAll(asList(oldField.getDeclaredAnnotations())));
        }

        assertInstantiation(newType, factory);
    }

    @Test
    public void inherited_properties_can_be_modified() throws Exception {
        // 1. create a generated type
        final NewProperty<Integer> np1 = NewProperty.fromField(Entity.class, "observableProperty").changeType(Integer.class);
        final Class<? extends Entity> newType1 = startModification(Entity.class)
                .modifyProperties(np1)
                .endModification();

        // 2. try to modify newType1 by modifying a property from its original type
        final NewProperty<BigDecimal> np2 = NewProperty.fromField(Entity.class, "money").changeType(BigDecimal.class);
        final Class<? extends Entity> newType2 = startModification(newType1)
                .modifyProperties(np2)
                .endModification();

        assertFieldExists(newType2, np1.getName());
        assertGeneratedPropertyCorrectness(np2, newType2);
    }

    @Test
    public void modifying_a_property_with_the_same_name_more_than_once_leads_to_a_runtime_exception() throws Exception {
        final NewProperty<Double> np = NewProperty.fromField(EntityBeingEnhanced.class.getDeclaredField("prop1"))
                .changeType(Double.class);
        final TypeMaker<EntityBeingEnhanced> builder = startModification(EntityBeingEnhanced.class)
                .modifyProperties(np);

        assertThrows(RuntimeException.class, () -> {
            builder.modifyProperties(np);
        });
    }

    @Test
    public void modification_of_a_non_existent_property_leads_to_a_runtime_exception() throws Exception {
        final NewProperty<Double> np = NewProperty.fromField(EntityBeingEnhanced.class.getDeclaredField("prop1"))
                .changeType(Double.class);
        final TypeMaker<Entity> builder = startModification(Entity.class);

        assertThrows(RuntimeException.class, () -> {
            builder.modifyProperties(np);
        });
    }

    @Test
    public void modified_properties_are_annotated_with_Generated() throws Exception {
        final Set<NewProperty<?>> newProperties = linkedSetOf(
                NewProperty.fromField(Entity.class, "firstProperty").changeType(String.class),
                NewProperty.fromField(Entity.class, "entity").changeType(TopLevelEntity.class),
                NewProperty.fromField(Entity.class, "observableProperty").setValueSupplierOrThrow(() -> new BigDecimal("123")));
        final Class<? extends AbstractEntity<?>> newType = startModification(Entity.class)
                .modifyProperties(newProperties)
                .endModification();

        for (final NewProperty<?> np: newProperties) {
            final Field field = newType.getDeclaredField(np.getName());
            assertNotNull("Modified property %s was not found.".formatted(np.getName()), field);
            assertTrue("Modified property %s is missing @Generated annotation.".formatted(np.getName()),
                    field.isAnnotationPresent(Generated.class));
        }

        assertInstantiation(newType, factory);
    }

    @Test
    public void generated_types_can_be_used_as_original_types_for_modification() throws Exception {
        final Class<? extends AbstractEntity<?>> newType1 = startModification(Entity.class)
                .modifyProperties(NewProperty.fromField(Entity.class, "firstProperty").changeType(String.class))
                .endModification();

        final Set<NewProperty<?>> newProperties =  linkedSetOf(NewProperty.fromField(newType1, "firstProperty").changeType(Double.class),
                NewProperty.fromField(newType1, "number").setValueSupplierOrThrow(() -> 123));
        final Class<? extends AbstractEntity<?>> newType2 = startModification(newType1)
                .modifyProperties(newProperties)
                .endModification();

        assertNotNull("Could not obtain a generated type based on another generated type.", newType2);
        assertEquals("Incorrect type hierarchy for the generated type.", newType1, newType2.getSuperclass());

        for (final var np: newProperties) {
            final Field field = newType2.getDeclaredField(np.getName());
            assertNotNull("Modified property %s was not found.".formatted(np.getName()), field);
            assertPropertyEquals(np, field);
        }

        assertInstantiation(newType2, factory);
    }

    @Test
    public void a_modified_property_hides_the_original_property_in_the_generated_type() throws Exception {
        final Field origProp = Finder.findFieldByName(Entity.class, "firstProperty");
        final NewProperty<String> np = NewProperty.fromField(origProp).changeType(String.class);
        final Class<? extends AbstractEntity<?>> newType = startModification(Entity.class)
                .modifyProperties(np)
                .endModification();

        assertNotNull("Modified property %s is not declared by the generated type.".formatted(np.getName()),
                newType.getDeclaredField(np.getName()));

        assertInstantiation(newType, factory);
    }

    @Test
    public void a_property_can_be_modified_to_have_its_type_set_to_a_generated_type() throws Exception {
        final Class<? extends AbstractEntity<?>> modEntityBeingEnhanced = startModification(EntityBeingEnhanced.class)
                .addProperties(npMoney)
                .endModification();

        // enhance(EntityBeingModified)
        //      prop1: EntityBeingEnhanced -> modEntityBeingEnhanced
        //      prop2: EntityBeingEnhanced -> modEntityBeingEnhanced
        final NewProperty<? extends AbstractEntity<?>> np1 = NewProperty.fromField(EntityBeingModified.class, "prop1")
                .changeType(modEntityBeingEnhanced);
        final NewProperty<? extends AbstractEntity<?>> np2 = NewProperty.fromField(EntityBeingModified.class, "prop2")
                .changeType(modEntityBeingEnhanced);
        final Class<? extends AbstractEntity<?>> modEntityBeingModified = startModification(EntityBeingModified.class)
                .modifyProperties(np1, np2)
                .endModification();

        final Field field1 = Finder.getFieldByName(modEntityBeingModified, np1.getName());
        assertNotNull("Modified property %s was not found.".formatted(np1.getName()), field1);
        assertEquals("Incorrect type of modified property.", np1.genericType().toString(), field1.getGenericType().toString());

        final Field field2 = Finder.getFieldByName(modEntityBeingModified, np2.getName());
        assertNotNull("Modified property %s was not found.".formatted(np2.getName()), field2);
        assertEquals("Incorrect type of modified property.", np2.genericType().toString(), field2.getGenericType().toString());

        assertEquals("Modified properties %s and %s should be of the same type.".formatted(np1.getName(), np2.getName()),
                field1.getGenericType(), field2.getGenericType());

        assertInstantiation(modEntityBeingModified, factory);
    }

    /**
     * Modifying a property should result in the generated type declaring:
     * <ul>
	 *   <li>A single field with the name of the modified property</li>
	 *   <li>A single accessor method for that property with a respective name</li>
	 *   <li>A single setter method for that property</li>
	 * </ul>
	 * The purpose of this test is to ensure that no class members with the same name are generated as a result of property modification.
     */
    @Test
    public void for_each_modified_property_generated_type_declares_a_field_and_acessor_and_setter() throws Exception {
        final NewProperty<String> np = NewProperty.fromField(Entity.class, "observableProperty").changeType(String.class);
        final Class<? extends Entity> newType = startModification(Entity.class)
                .modifyProperties(np)
                .endModification();

        // declared fields
        assertEquals("Incorrect number of declared fields by a generated type for a modified property.",
                1, Arrays.stream(newType.getDeclaredFields()).filter(field -> field.getName().equals(np.getName())).count());
        // declared accessor method
        assertEquals("Incorrect number of declared accessor methods by a generated type for a modified property.",
                1, Arrays.stream(newType.getDeclaredMethods())
                        .filter(method -> method.getName().equals("get" + capitalize(np.getName())))
                        .count());
        // declared setter method
        assertEquals("Incorrect number of declared setter methods by a generated type for a modified property.",
                1, Arrays.stream(newType.getDeclaredMethods())
                        .filter(method -> method.getName().equals("set" + capitalize(np.getName())))
                        .count());
    }

    @Test
    public void accessor_method_is_generated_correctly_for_a_modified_property() throws Exception {
        final NewProperty<String> np = NewProperty.fromField(Entity.class, "observableProperty").changeType(String.class);
        final Class<? extends Entity> newType = startModification(Entity.class)
                .modifyProperties(np)
                .endModification();

        final Method accessor = assertGeneratedPropertyAccessorSignature(np, newType);

        // instantiate the generated type and try to invoke the accessor
        final Entity instance = assertInstantiation(newType, factory);
        try {
            accessor.invoke(instance);
        } catch (final Exception e) {
            fail("Failed to invoke accessor for modified property %s.".formatted(np.getName()));
            return;
        }
    }

    @Test
    public void setters_are_generated_correctly_for_modified_collectional_properties() throws Exception {
        final NewProperty<?> np = NewProperty.fromField(EntityWithCollectionalPropety.class, "items").setTypeArguments(String.class);
        final Class<? extends EntityWithCollectionalPropety> newType = startModification(EntityWithCollectionalPropety.class)
                .modifyProperties(np)
                .endModification();

        final Method setter = assertGeneratedPropertySetterSignature(np, newType);

        // instantiate the generated type and try to set the value of modified property 
        final EntityWithCollectionalPropety instance = assertInstantiation(newType, factory);
        final List<String> list1 = List.of("hello");
        setter.invoke(instance, list1);
        assertEquals("The value of modified collectional property %s was set incorrectly.".formatted(np.getName()),
                list1, getFieldValue(findFieldByName(newType, np.getName()), instance));

        // now set the value once again to make sure the setter indeed is generated correctly
        // the old collection contents should be cleared, then provided elements should be added
        final List<String> list2 = List.of("world");
        setter.invoke(instance, list2);
        assertEquals("The value of added collectional property %s was set incorrectly.".formatted(np.getName()),
                list2, getFieldValue(findFieldByName(newType, np.getName()), instance));
    }

    @Test
    public void accessor_returns_correct_value_after_setter_invokation_for_modified_properties() throws Exception {
        final NewProperty<String> np = NewProperty.fromField(Entity.class, "observableProperty").changeType(String.class);
        final Class<? extends Entity> newType = startModification(Entity.class)
                .modifyProperties(np)
                .endModification();

        final Method setter = assertGeneratedPropertySetterSignature(np, newType);

        // instantiate the generated type and try to invoke the accessor
        final Entity instance = assertInstantiation(newType, factory);
        final String newValue = "value";
        try {
            setter.invoke(instance, newValue);
        } catch (final Exception e) {
            fail("Failed to invoke setter for modified property %s.".formatted(np.getName()));
            return;
        }

        final Method accessor = assertGeneratedPropertyAccessorSignature(np, newType);
        assertEquals("Incorrect accessor return value for modified property %s.".formatted(np.getName()), 
                newValue, accessor.invoke(instance));
    }

    @Test
    public void setter_is_observed_for_a_modified_property() throws Exception {
        final NewProperty<Money> np = NewProperty.fromField(Entity.class, "observableProperty").changeType(Money.class);
        final Class<? extends AbstractEntity<?>> newType = startModification(Entity.class)
                .modifyProperties(np)
                .endModification();

        module.getDomainMetaPropertyConfig().setDefiner(newType, np.getName(), new IAfterChangeEventHandler<Object>() {
            @Override
            public void handle(final MetaProperty<Object> property, final Object entityPropertyValue) {
                observed = true;
            }
        });

        final AbstractEntity<?> instance = assertInstantiation(newType, factory);
        final Money value = new Money("23.32");
        instance.set(np.getName(), value);
        assertTrue("Setter for the modified property %s was not observed.".formatted(np.getName()), observed);
        assertEquals("Incorrect value of the modified property %s.".formatted(np.getName()), value, instance.get(np.getName()));
    }

    @Test
    public void raw_type_of_parameterized_property_type_can_be_modified() throws Exception {
        // from List<Double> to Set<Double>
        final NewProperty<?> np1 = NewProperty.fromField(EntityWithCollectionalPropety.class, "items").changeType(Set.class);
        final Class<? extends EntityWithCollectionalPropety> newType = startModification(EntityWithCollectionalPropety.class)
                .modifyProperties(np1)
                .endModification();

        assertGeneratedPropertyCorrectness(np1, newType);
    }

    @Test
    public void collectional_property_with_modified_raw_type_is_generated_with_correct_initializer() throws Exception {
         // from List<Double> to Set<Double>
        final NewProperty<?> np1 = NewProperty.fromField(EntityWithCollectionalPropety.class, "items").changeType(Set.class);
        final Class<? extends EntityWithCollectionalPropety> newType = startModification(EntityWithCollectionalPropety.class)
                .modifyProperties(np1)
                .endModification();

        assertGeneratedPropertyCorrectness(np1, newType);
        // make sure that modified property is initialized with an empty collection of the new type 
        final EntityWithCollectionalPropety instance = assertInstantiation(newType, factory);
        final Object value = instance.get(np1.getName());
        assertTrue("Incorrect initializer of the modified property %s.".formatted(np1.getName()), Set.class.isInstance(value));
        final Set<?> valueSet = (Set<?>) value;
        assertTrue("Modified collectional property should be empty after initialization.", valueSet.isEmpty());
    }

    @Test
    public void type_arguments_of_parameterized_property_type_can_be_modified() throws Exception {
        // 1. changing type arguments to unrelated types (not in the same hierarchy)
        final NewProperty<?> np1 = NewProperty.fromField(EntityWithCollectionalPropety.class, "items")
                .setTypeArguments(String.class)
                // for consistency, since this is a collectional property
                .changeIsPropertyValue(String.class);
        final Class<? extends EntityWithCollectionalPropety> newType1 = startModification(EntityWithCollectionalPropety.class)
                .modifyProperties(np1)
                .endModification();
        assertGeneratedPropertyCorrectness(np1, newType1);

        // 2. in this test EntityWithCollectionalProperty.prop1 is modified
        // type argument EntityBeingEnhanced is replaced by a generated type based on it
        // enhance(EntityBeingEnhanced)
        final Class<? extends EntityBeingEnhanced> modEntityBeingEnhanced = startModification(EntityBeingEnhanced.class)
                .addProperties(npMoney)
                .endModification();
        assertGeneratedPropertyCorrectness(npMoney, modEntityBeingEnhanced);

        final NewProperty<?> np2 = NewProperty.fromField(EntityWithCollectionalPropety.class, "prop1")
                .setTypeArguments(modEntityBeingEnhanced)
                // for consistency, since this is a collectional property
                .changeIsPropertyValue(modEntityBeingEnhanced);
        // enhance(EntityWithCollectionalProperty)
        //      prop1: Collection<EntityBeingEnhanced> -> Collection<modEntityBeingEnhanced>
        final Class<? extends EntityWithCollectionalPropety> modEntityWithCollectionalProperty =
                startModification(EntityWithCollectionalPropety.class)
                .modifyProperties(np2)
                .endModification();

        assertGeneratedPropertyCorrectness(np2, modEntityWithCollectionalProperty);
    }

    @Test
    public void test_inner_types_usage_in_generated_types() throws Exception {
        // enhance(EntityBeingModifiedWithInnerTypes)
        //      integerProp: Integer -> BigInteger
        final NewProperty<BigInteger> np = NewProperty.fromField(EntityBeingModifiedWithInnerTypes.class, "integerProp")
                .changeType(BigInteger.class);
        final Class<? extends EntityBeingModifiedWithInnerTypes> newType = 
                startModification(EntityBeingModifiedWithInnerTypes.class)
                .modifyProperties(np)
                .endModification();
        // EntityBeingModifiedWithInnerTypes class contains an inner type that also has a field named "integerProp"
        // this fact should not have any effect

        // instantiation of the generated class with inner types
        final var instance = assertInstantiation(newType, factory);
        try {
            instance.set("enumProp", InnerEnum.ONE);
        } catch (final Throwable e) {
            e.printStackTrace();
            fail("Could not set value for a property of inner type.");
            return;
        }
    }

    @Test
    public void modified_one2Many_special_case_property_is_generated_correctly() throws Exception {
        // enhance(DetailsEntityForOneToManyAssociation)
        final Class<? extends DetailsEntityForOneToManyAssociation> modOneToManyDetailsEntity = 
                startModification(DetailsEntityForOneToManyAssociation.class)
                .addProperties(npMoney)
                .endModification();

        // enhance(MasterEntityWithOneToManyAssociation)
        //   one2manyAssociationSpecialCase: DetailsEntityForOneToManyAssociation ---> modOneToManyDetailsEntity
        final NewProperty<? extends DetailsEntityForOneToManyAssociation> npOne2ManySpecialCase =
                NewProperty.fromField(MasterEntityWithOneToManyAssociation.class, "one2manyAssociationSpecialCase")
                .changeType(modOneToManyDetailsEntity);
        final Class<? extends MasterEntityWithOneToManyAssociation> modOneToManyMasterEntity = 
                startModification(MasterEntityWithOneToManyAssociation.class)
                .modifyProperties(npOne2ManySpecialCase)
                .endModification();

        assertGeneratedPropertyCorrectness(npOne2ManySpecialCase, modOneToManyMasterEntity);
    }

    // NOTE: test name says "when IsProperty is not provided", but target entity that is being enhanced in this class
    // has @IsProperty on the field that is being modified
    @Test
    public void modified_one2Many_collectional_property_is_generated_correctly_when_IsProperty_is_not_provided() throws Exception {
        final Class<? extends DetailsEntityForOneToManyAssociation> modOneToManyDetailsEntity =
                startModification(DetailsEntityForOneToManyAssociation.class)
                .addProperties(npMoney)
                .endModification();

        // enhance(MasterEntityWithOneToManyCollectionalAssociationProvidedWithLinkPropValue)
        //   one2manyAssociationCollectional: List<DetailsEntityForOneToManyAssociation> ---> List<modOneToManyDetailsEntity> 
        final NewProperty<?> npOne2ManyCollectional = NewProperty.fromField(
                MasterEntityWithOneToManyCollectionalAssociationProvidedWithLinkPropValue.class, "one2manyAssociationCollectional")
                .setTypeArguments(modOneToManyDetailsEntity)
                .changeIsPropertyValue(modOneToManyDetailsEntity);
        final Class<? extends MasterEntityWithOneToManyCollectionalAssociationProvidedWithLinkPropValue> modOneToManyMasterEntity = 
                startModification(MasterEntityWithOneToManyCollectionalAssociationProvidedWithLinkPropValue.class)
                .modifyProperties(npOne2ManyCollectional)
                .endModification();

        assertGeneratedPropertyCorrectness(npOne2ManyCollectional, modOneToManyMasterEntity);
    }

    @Test
    public void initialization_value_of_a_property_can_be_modified() throws Exception {
        // 1. modify a simple property
        final NewProperty<?> np = NewProperty.fromField(EntityBeingEnhanced.class, "prop1").setValueSupplierOrThrow(() -> "Hello");
        final Class<? extends EntityBeingEnhanced> modEntityBeingEnhanced = startModification(EntityBeingEnhanced.class)
                .modifyProperties(np)
                .endModification();

        // instantiate
        final EntityBeingEnhanced instance = assertInstantiation(modEntityBeingEnhanced, factory);
        assertEquals("Incorrect property initialization value.", np.getValueSupplier().get(), instance.get(np.getName()));

        // 2. modify a collectional property
        final NewProperty<?> npColl = NewProperty.fromField(EntityWithCollectionalPropety.class, "prop1").setValueSupplierOrThrow(TreeSet::new);
        final Class<? extends EntityWithCollectionalPropety> modEntityWithCollectionalPropety =
                startModification(EntityWithCollectionalPropety.class)
                .modifyProperties(npColl)
                .endModification();

        // instantiate
        final EntityWithCollectionalPropety instanceColl = assertInstantiation(modEntityWithCollectionalPropety, factory);
        assertEquals("Incorrect property initialization value.", npColl.getValueSupplier().get(), instanceColl.get(npColl.getName()));
    }

}
