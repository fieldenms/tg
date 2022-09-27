package ua.com.fielden.platform.reflection.asm.impl;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.reflection.Finder.findFieldByName;
import static ua.com.fielden.platform.reflection.Finder.getFieldValue;
import static ua.com.fielden.platform.reflection.asm.api.test_utils.NewPropertyTestUtils.assertPropertyEquals;
import static ua.com.fielden.platform.reflection.asm.impl.DynamicEntityTypeTestUtils.assertFieldExists;
import static ua.com.fielden.platform.reflection.asm.impl.DynamicEntityTypeTestUtils.assertGeneratedPropertyCorrectness;
import static ua.com.fielden.platform.reflection.asm.impl.DynamicEntityTypeTestUtils.assertGeneratedPropertySetterSignature;
import static ua.com.fielden.platform.reflection.asm.impl.DynamicEntityTypeTestUtils.assertGeneratedPropertyAccessorSignature;
import static ua.com.fielden.platform.reflection.asm.impl.DynamicEntityTypeTestUtils.assertInstantiation;
import static ua.com.fielden.platform.reflection.asm.impl.DynamicEntityTypeTestUtils.extractTypeArguments;
import static ua.com.fielden.platform.utils.Pair.pair;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

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
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.factory.CalculatedAnnotation;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.meta.IAfterChangeEventHandler;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.reflection.AnnotationReflector;
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
import ua.com.fielden.platform.utils.Pair;

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
    private DynamicEntityClassLoader cl;

    @Before
    public void setUp() {
        observed = false;
        cl = DynamicEntityClassLoader.getInstance(ClassLoader.getSystemClassLoader());
    }

    @Test
    public void a_single_property_can_be_modified() throws Exception {
        final Field oldField = EntityBeingEnhanced.class.getDeclaredField("prop1");

        final NewProperty<Double> np = NewProperty.fromField(oldField).changeType(Double.class);
        final Class<? extends AbstractEntity<?>> newType = cl.startModification(EntityBeingEnhanced.class)
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
        final Class<? extends AbstractEntity<?>> newType = cl.startModification(origType)
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

        final TypeMaker<? extends AbstractEntity<?>> builder = cl.startModification(origType);
        List.of(np1, np2, np3).forEach(builder::addProperties);
        final Class<? extends AbstractEntity<?>> newType = builder.endModification();

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
    public void modified_properties_are_annotated_with_Generated() throws Exception {
        final List<NewProperty<?>> newProperties = List.of(
                NewProperty.fromField(Entity.class, "firstProperty").changeType(String.class),
                NewProperty.fromField(Entity.class, "entity").changeType(TopLevelEntity.class),
                NewProperty.fromField(Entity.class, "observableProperty").setValueThrows(123d));
        final Class<? extends AbstractEntity<?>> newType = cl.startModification(Entity.class)
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
        final Class<? extends AbstractEntity<?>> newType1 = cl.startModification(Entity.class)
                .modifyProperties(NewProperty.fromField(Entity.class, "firstProperty").changeType(String.class))
                .endModification();

        final List<NewProperty<?>> newProperties = List.of(NewProperty.fromField(newType1, "firstProperty").changeType(Double.class),
                NewProperty.fromField(newType1, "number").setValueThrows(123));
        final Class<? extends AbstractEntity<?>> newType2 = cl.startModification(newType1)
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
        final Class<? extends AbstractEntity<?>> newType = cl.startModification(Entity.class)
                .modifyProperties(np)
                .endModification();
        
        assertNotNull("Modified property %s is not declared by the generated type.".formatted(np.getName()),
                newType.getDeclaredField(np.getName()));

        assertInstantiation(newType, factory);
    }

    @Test
    public void a_property_can_be_modified_to_have_its_type_set_to_a_generated_type() throws Exception {
        final Class<? extends AbstractEntity<?>> modEntityBeingEnhanced = cl.startModification(EntityBeingEnhanced.class)
                .addProperties(npMoney)
                .endModification();

        // enhance(EntityBeingModified)
        //      prop1: EntityBeingEnhanced -> modEntityBeingEnhanced
        //      prop2: EntityBeingEnhanced -> modEntityBeingEnhanced
        final NewProperty<? extends AbstractEntity<?>> np1 = NewProperty.fromField(EntityBeingModified.class, "prop1")
                .changeType(modEntityBeingEnhanced);
        final NewProperty<? extends AbstractEntity<?>> np2 = NewProperty.fromField(EntityBeingModified.class, "prop2")
                .changeType(modEntityBeingEnhanced);
        final Class<? extends AbstractEntity<?>> modEntityBeingModified = cl.startModification(EntityBeingModified.class)
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

    @Test
    public void accessor_method_is_generated_correctly_for_a_modified_property() throws Exception {
        final NewProperty<String> np = NewProperty.fromField(Entity.class, "observableProperty").changeType(String.class);
        final Class<? extends Entity> newType = cl.startModification(Entity.class)
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
    public void setters_are_generated_correctly_for_added_collectional_properties() throws Exception {
        final NewProperty<String> np = NewProperty.fromField(Entity.class, "observableProperty").changeType(String.class);
        final Class<? extends Entity> newType = cl.startModification(Entity.class)
                .modifyProperties(np)
                .endModification();

        final Method setter = assertGeneratedPropertySetterSignature(np, newType);

        // instantiate the generated type and try to invoke the accessor
        final Entity instance = assertInstantiation(newType, factory);
        try {
            setter.invoke(instance, "value");
        } catch (final Exception e) {
            fail("Failed to invoke setter for modified property %s.".formatted(np.getName()));
            return;
        }
    }

    @Test
    public void setters_are_generated_correctly_for_modified_collectional_properties() throws Exception {
        final NewProperty<?> np = NewProperty.fromField(EntityWithCollectionalPropety.class, "items").setTypeArguments(String.class);
        final Class<? extends EntityWithCollectionalPropety> newType = cl.startModification(EntityWithCollectionalPropety.class)
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
        final Class<? extends Entity> newType = cl.startModification(Entity.class)
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
        final Class<? extends AbstractEntity<?>> newType = cl.startModification(Entity.class)
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
        final Class<? extends EntityWithCollectionalPropety> newType = cl.startModification(EntityWithCollectionalPropety.class)
                .modifyProperties(np1)
                .endModification();

        assertGeneratedPropertyCorrectness(np1, newType);
    }
    
    @Test
    public void collectional_property_with_modified_raw_type_is_generated_with_correct_initializer() throws Exception {
         // from List<Double> to Set<Double>
        final NewProperty<?> np1 = NewProperty.fromField(EntityWithCollectionalPropety.class, "items").changeType(Set.class);
        final Class<? extends EntityWithCollectionalPropety> newType = cl.startModification(EntityWithCollectionalPropety.class)
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
        final Class<? extends EntityWithCollectionalPropety> newType1 = cl.startModification(EntityWithCollectionalPropety.class)
                .modifyProperties(np1)
                .endModification();
        assertGeneratedPropertyCorrectness(np1, newType1);
        
        // 2. in this test EntityWithCollectionalProperty.prop1 is modified
        // type argument EntityBeingEnhanced is replaced by a generated type based on it
        // enhance(EntityBeingEnhanced)
        final Class<? extends EntityBeingEnhanced> modEntityBeingEnhanced = cl.startModification(EntityBeingEnhanced.class)
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
                cl.startModification(EntityWithCollectionalPropety.class)
                .modifyProperties(np2)
                .endModification();

        assertGeneratedPropertyCorrectness(np2, modEntityWithCollectionalProperty);
    }

    @Test
    public void test_inner_types_usage_in_generated_classes() throws Exception {
        // enhance(EntityBeingModifiedWithInnerTypes)
        //      integerProp: Integer -> BigInteger
        final NewProperty np = NewProperty.fromField(EntityBeingModifiedWithInnerTypes.class, "integerProp").changeType(BigInteger.class);
        final Class<? extends EntityBeingModifiedWithInnerTypes> modEntityBeingModifiedWithInnerTypes = 
                cl.startModification(EntityBeingModifiedWithInnerTypes.class)
                .modifyProperties(np)
                .endModification();
        // EntityBeingModifiedWithInnerTypes class contains an inner type that also has a field named "integerProp"
        // this fact should not have any effect

        // instance creation of the generated class with inner types does not fail
        final var instance = modEntityBeingModifiedWithInnerTypes.getConstructor().newInstance();
        assertNotNull("Should not be null.", instance);
        try {
            instance.set("enumProp", InnerEnum.ONE);
        } catch (final Throwable e) {
            e.printStackTrace();
            fail("The setter should not fail -- inner classes can not be loaded.");
        }
    }

    @Test
    public void test_generated_class_with_inner_types_factory_instantiation() throws Exception {
        // enhance(EntityBeingModifiedWithInnerTypes)
        //      integerProp: Integer -> BigInteger
        final NewProperty np = NewProperty.fromField(EntityBeingModifiedWithInnerTypes.class, "integerProp").changeType(BigInteger.class);
        final Class<? extends EntityBeingModifiedWithInnerTypes> modEntityBeingModifiedWithInnerTypes = 
                cl.startModification(EntityBeingModifiedWithInnerTypes.class)
                .modifyProperties(np)
                .endModification();
        try {
            factory.newByKey(modEntityBeingModifiedWithInnerTypes, "key");
        } catch (final Throwable e) {
            e.printStackTrace();
            fail("The instantiation with entity factory shouldn't fail -- inner classes can not be loaded.");
        }
    }

    @Test
    public void type_modification_does_not_modify_existing_getters_for_untouched_properties() throws Exception {
        final Class<? extends EntityName> enhancedType = cl.startModification(EntityName.class)
                .addProperties(npMoney)
                .endModification();

        // no properties were modified so original getters should not be overriden
        final Field prop = Finder.getFieldByName(enhancedType, "prop");
        assertEquals("Incorrect property type.", EntityNameProperty.class, prop.getType());

        final Method getter = Reflector.obtainPropertyAccessor(enhancedType, "prop");
        assertEquals("Incorrect getter return type.", EntityNameProperty.class, getter.getReturnType());
    }

    @Test
    public void modified_one2Many_special_case_property_is_generated_correctly() throws Exception {
        final Class<? extends DetailsEntityForOneToManyAssociation> modOneToManyDetailsEntity = 
                cl.startModification(DetailsEntityForOneToManyAssociation.class)
                .addProperties(npMoney)
                .endModification();

        // enhance(MasterEntityWithOneToManyAssociation)
        //      one2manyAssociationSpecialCase: DetailsEntityForOneToManyAssociation ---> modOneToManyDetailsEntity
        final NewProperty npOne2ManySpecialCase = NewProperty.fromField(MasterEntityWithOneToManyAssociation.class,
                "one2manyAssociationSpecialCase").changeType(modOneToManyDetailsEntity);
        final Class<? extends MasterEntityWithOneToManyAssociation> modOneToManyMasterEntity = 
                cl.startModification(MasterEntityWithOneToManyAssociation.class)
                .modifyProperties(npOne2ManySpecialCase)
                .endModification();

        assertEquals("key1", AnnotationReflector.getAnnotation(
                Finder.findFieldByName(modOneToManyMasterEntity, "one2manyAssociationSpecialCase"), IsProperty.class
                ).linkProperty());
    }

    // NOTE: test name says "when IsProperty is not provided", but target entity that is being enhanced in this class
    // has @IsProperty on the field that is being modified
    @Test
    public void modified_one2Many_collectional_property_is_generated_correctly_when_IsProperty_is_not_provided() throws Exception {
        final Class<? extends DetailsEntityForOneToManyAssociation> modOneToManyDetailsEntity =
                cl.startModification(DetailsEntityForOneToManyAssociation.class)
                .addProperties(npMoney)
                .endModification();

        // enhance(MasterEntityWithOneToManyCollectionalAssociationProvidedWithLinkPropValue)
        //      one2manyAssociationCollectional: List<DetailsEntityForOneToManyAssociation> ---> List<modOneToManyDetailsEntity> 
        final NewProperty npOne2ManyCollectional = NewProperty.fromField(
                MasterEntityWithOneToManyCollectionalAssociationProvidedWithLinkPropValue.class, "one2manyAssociationCollectional"
                ).setTypeArguments(modOneToManyDetailsEntity);
        final Class<? extends MasterEntityWithOneToManyCollectionalAssociationProvidedWithLinkPropValue> modOneToManyMasterEntity = 
                cl.startModification(MasterEntityWithOneToManyCollectionalAssociationProvidedWithLinkPropValue.class)
                .modifyProperties(npOne2ManyCollectional)
                .endModification();

        assertEquals("key1", AnnotationReflector.getAnnotation(
                    Finder.findFieldByName(modOneToManyMasterEntity, "one2manyAssociationCollectional"), 
                    IsProperty.class)
                .linkProperty());
        assertEquals(modOneToManyDetailsEntity, AnnotationReflector.getAnnotation(
                    Finder.findFieldByName(modOneToManyMasterEntity, "one2manyAssociationCollectional"),
                    IsProperty.class)
                .value());
    }
    
    @Test
    public void initialization_value_of_a_property_can_be_modified() throws Exception {
        // modify a simple property
        final NewProperty np = NewProperty.fromField(EntityBeingEnhanced.class, "prop1").setValueThrows("Hello");
        final Class<? extends EntityBeingEnhanced> modEntityBeingEnhanced = cl.startModification(EntityBeingEnhanced.class)
                .modifyProperties(np)
                .endModification();
        // instantiate
        final EntityBeingEnhanced instance = factory.newByKey(modEntityBeingEnhanced, "new");
        assertEquals("Incorrect property initialization value.", np.getValue(), instance.get(np.getName()));

        // modify a collectional property
        final NewProperty npColl = NewProperty.fromField(EntityWithCollectionalPropety.class, "prop1").setValueThrows(new ArrayList<>());
        final Class<? extends EntityWithCollectionalPropety> modEntityWithCollectionalPropety =
                cl.startModification(EntityWithCollectionalPropety.class)
                .modifyProperties(npColl)
                .endModification();
        // instantiate
        final EntityWithCollectionalPropety instanceColl = factory.newByKey(modEntityWithCollectionalPropety, "new");
        assertEquals("Incorrect property initialization value.", npColl.getValue(), instanceColl.get(npColl.getName()));
    }

    @Test
    public void deprecated_NewProperty_can_be_used_to_modify_property_type() throws Exception {
        // Here we are testing 2 things:
        // 1. changing the type of an existing *simple* property
        // 2. changing the raw type of an existing collectional property
        for (final var nameAndNewType: List.of(Pair.pair("firstProperty", BigDecimal.class),
                                               Pair.pair("entities", Set.class)))
        {
            final String name = nameAndNewType.getKey();
            final Field origField = Finder.getFieldByName(Entity.class, name);
            assertNotNull(origField); // make sure such a property exists

            final Class<?> newPropType = nameAndNewType.getValue();
            // make sure that new property type is actually different
            assertNotEquals(newPropType, origField.getType());

            // access original field type arguments, if any 
            final List<Type> origFieldTypeArguments = extractTypeArguments(origField.getGenericType());

            final NewProperty np = NewProperty.changeType(name, newPropType);
            final Class<? extends AbstractEntity<String>> enhancedType = cl.startModification(Entity.class)
                    .modifyProperties(np)
                    .endModification();

//            assertGeneratedPropertyCorrectness(enhancedType, name, newPropType, 
//                    // we only changed the raw type, so the last argument is the type arguments of the original field, 
//                    // which should have been preserved
//                    origFieldTypeArguments);
        }
    }

    @Test
    public void deprecated_NewProperty_can_be_used_to_modify_property_type_arguments() throws Exception {
        // Here we are testing 3 things:
        // 1. changing type argument of a collectional property
        // 2. changing type argument of a PropertyDescriptor property
        // 3. changing type argument of any other parameterized type (e.g. Optional)

        // first, test 1. and 2. which affect @IsProperty.value()
        for (final var nameAndTypeArg: List.of(Pair.pair("entities", EntityBeingEnhanced.class),
                                               Pair.pair("propertyDescriptor", EntityBeingEnhanced.class)))
        {
            final String name = nameAndTypeArg.getKey();
            final Field origField = Finder.getFieldByName(Entity.class, name);
            assertNotNull(origField); // make sure such a property exists

            final Class<?> typeArg = nameAndTypeArg.getValue();

            // access original field type arguments, if any 
            final List<Type> origFieldTypeArguments = extractTypeArguments(origField.getGenericType());

            final NewProperty np = NewProperty.changeTypeSignature(name, typeArg);
            final Class<? extends AbstractEntity<String>> newType = cl.startModification(Entity.class)
                    .modifyProperties(np)
                    .endModification();

//            final Field modifiedProperty = assertGeneratedPropertyCorrectness(newType, name, 
//                    origField.getType(), // expect the same raw type
//                    List.of(typeArg)     // expect new type argument
//                    );
//
//            // make sure @IsProperty.value() was also modified
//            final IsProperty atIsProperty = modifiedProperty.getAnnotation(IsProperty.class);
//            assertNotNull("@IsProperty should be present.", atIsProperty);
//            assertEquals("Incorrect value of @IsProperty.", typeArg, atIsProperty.value());
        }

        // now test 3.
        final String name = "maybeText";
        final Field origField = Finder.getFieldByName(Entity.class, name);
        assertNotNull(origField); // make sure such a property exists

        final Class<?> typeArg = Integer.class;

        // access original field type arguments, if any 
        final List<Type> origFieldTypeArguments = extractTypeArguments(origField.getGenericType());

        final NewProperty np = NewProperty.changeTypeSignature(name, typeArg);
        final Class<? extends AbstractEntity<String>> newType = cl.startModification(Entity.class)
                .modifyProperties(np)
                .endModification();

//        final Field modifiedProperty = assertGeneratedPropertyCorrectness(newType, name, 
//                origField.getType(), // expect the same raw type
//                List.of(typeArg)     // expect new type argument
//                );
//
//        // make sure @IsProperty.value() was unchanged
//        final Class<?> origIsPropertyValue = origField.getAnnotation(IsProperty.class).value();
//        final IsProperty atIsProperty = modifiedProperty.getAnnotation(IsProperty.class);
//        assertNotNull("@IsProperty should be present.", atIsProperty);
//        assertEquals("Incorrect value of @IsProperty.", origIsPropertyValue, atIsProperty.value());
    }
}
