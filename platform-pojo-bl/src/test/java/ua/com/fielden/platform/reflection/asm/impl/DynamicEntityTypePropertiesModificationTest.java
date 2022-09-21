package ua.com.fielden.platform.reflection.asm.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;

import com.google.inject.Injector;

import ua.com.fielden.platform.associations.one2many.DetailsEntityForOneToManyAssociation;
import ua.com.fielden.platform.associations.one2many.MasterEntityWithOneToManyAssociation;
import ua.com.fielden.platform.associations.one2many.MasterEntityWithOneToManyCollectionalAssociationProvidedWithLinkPropValue;
import ua.com.fielden.platform.entity.annotation.Calculated;
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

/**
 * A test case to ensure correct dynamic modification of entity types by means of changing existing properties.
 *
 * @author TG Team
 *
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class DynamicEntityTypePropertiesModificationTest {
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

    private final NewProperty pd = new NewProperty(NEW_PROPERTY, Money.class, NEW_PROPERTY_TITLE, NEW_PROPERTY_DESC, calculated);

    @Before
    public void setUp() {
        observed = false;
        cl = DynamicEntityClassLoader.getInstance(ClassLoader.getSystemClassLoader());
    }

    @Test
    public void test_to_ensure_several_properties_can_be_modified() throws Exception {
        // enhance(EntityBeingEnhanced)
        final Class<? extends EntityBeingEnhanced> modEntityBeingEnhanced = cl.startModification(EntityBeingEnhanced.class)
                .addProperties(pd)
                .endModification();

        // enhance(EntityBeingModified)
        //      prop1: EntityBeingEnhanced -> modEntityBeingEnhanced
        final NewProperty np = NewProperty.fromField(EntityBeingModified.class, "prop1").changeType(modEntityBeingEnhanced);
        final Class<? extends EntityBeingModified> modEntityBeingModified = cl.startModification(EntityBeingModified.class)
                .modifyProperties(np)
                .endModification();

        // enhance(TopLevelEntity)
        //      prop1: EntityBeingModified -> modEntityBeingModified
        //      prop2: EntityBeingModified -> modEntityBeingModified
        final NewProperty topLevelNp1 = NewProperty.fromField(TopLevelEntity.class, "prop1").changeType(modEntityBeingModified);
        final NewProperty topLevelNp2 = NewProperty.fromField(TopLevelEntity.class, "prop2").changeType(modEntityBeingModified);
        final Class<?> topLevelEntityModifiedType = cl.startModification(TopLevelEntity.class)
                .modifyProperties(topLevelNp1, topLevelNp2)
                .endModification();

        final Field prop1 = topLevelEntityModifiedType.getDeclaredField("prop1");
        assertNotNull("Modified property should be declared by the enhanced type.", prop1);
        assertEquals("Incorrect property type after modification", modEntityBeingModified, prop1.getType());

        final Field prop2 = topLevelEntityModifiedType.getDeclaredField("prop2");
        assertNotNull("Modified property should be declared by the enhanced type.", prop2);
        assertEquals("Incorrect property type after modification", modEntityBeingModified, prop2.getType());

        assertEquals("prop 1 and prop 2 should be of the same type", prop2.getType(), prop1.getType());
    }

    @Test
    public void test_sequential_modificaton_of_properties() throws Exception {
        // enhance(EntityBeingEnhanced)
        final Class<? extends EntityBeingEnhanced> modEntityBeingEnhanced = cl.startModification(EntityBeingEnhanced.class)
                .addProperties(pd)
                .endModification();

        // enhance(EntityBeingModified)
        //      prop1: EntityBeingEnhanced -> modEntityBeingEnhanced
        final NewProperty np = NewProperty.fromField(EntityBeingModified.class, "prop1").changeType(modEntityBeingEnhanced);
        final Class<?> modEntityBeingModified = cl.startModification(EntityBeingModified.class)
                .modifyProperties(np)
                .endModification();


        // enhance(TopLevelEntity)
        //      prop1: EntityBeingModified -> modEntityBeingModified
        //      prop2: EntityBeingModified -> modEntityBeingModified
        final NewProperty topLevelNp1 = NewProperty.fromField(TopLevelEntity.class, "prop1").changeType(modEntityBeingModified);
        final NewProperty topLevelNp2 = NewProperty.fromField(TopLevelEntity.class, "prop2").changeType(modEntityBeingModified);
        final Class<?> topLevelEntityModifiedType = cl.startModification(TopLevelEntity.class)
                .modifyProperties(topLevelNp1)
                .modifyProperties(topLevelNp2)
                .endModification();

        final Field prop1 = topLevelEntityModifiedType.getDeclaredField("prop1");
        assertNotNull("Modified property should be declared by the enhanced type.", prop1);
        assertEquals("Incorrect property type after modification", modEntityBeingModified, prop1.getType());

        final Field prop2 = topLevelEntityModifiedType.getDeclaredField("prop2");
        assertNotNull("Modified property should be declared by the enhanced type.", prop2);
        assertEquals("Incorrect property type after modification", modEntityBeingModified, prop2.getType());

        assertEquals("prop 1 and prop 2 should be of the same type", prop2.getType(), prop1.getType());
    }

    @Test
    public void test_modification_of_properties_to_modified_type() throws Exception {
        // enhance(EntityBeingEnhanced)
        final Class<?> modEntityBeingEnhanced = cl.startModification(EntityBeingEnhanced.class)
                .addProperties(pd)
                .endModification();

        // enhance(EntityBeingModified)
        //      prop1: EntityBeingEnhanced -> modEntityBeingEnhanced
        final NewProperty np = NewProperty.fromField(EntityBeingModified.class, "prop1").changeType(modEntityBeingEnhanced);
        final Class<?> modEntityBeingModified = cl.startModification(EntityBeingModified.class)
                .modifyProperties(np)
                .endModification();

        // enhance(TopLevelEntity)
        //      prop1: EntityBeingModified -> modEntityBeingModified
        final NewProperty topLevelNp1 = NewProperty.fromField(TopLevelEntity.class, "prop1").changeType(modEntityBeingModified);
        final Class<?> mod1TopLevelEntity = cl.startModification(TopLevelEntity.class)
                .modifyProperties(topLevelNp1)
                .endModification();

        // enhance(mod1TopLevelEntity)
        //      prop2: EntityBeingModified -> modEntityBeingModified
        final NewProperty topLevelNp2 = NewProperty.fromField(mod1TopLevelEntity, "prop2").changeType(modEntityBeingModified);
        final Class<?> mod2TopLevelEntity = cl.startModification(mod1TopLevelEntity)
                .modifyProperties(topLevelNp2)
                .endModification();

        final Field prop1 = Finder.getFieldByName(mod2TopLevelEntity, "prop1");
        assertNotNull("Property modified by an enhanced type should be accessible to a derived enhanced type.", prop1);
        assertEquals("Incorrect property type after modification", modEntityBeingModified, prop1.getType());

        final Field prop2 = mod2TopLevelEntity.getDeclaredField("prop2");
        assertNotNull("Modified property should be declared by the enhanced type.", prop2);
        assertEquals("Incorrect property type after modification", modEntityBeingModified, prop2.getType());

        assertEquals("prop 1 and prop 2 should be of the same type", prop2.getType(), prop1.getType());
    }

    @Test
    public void test_new_type_name_generation() throws Exception {
        // enhance(EntityBeingEnhanced)
        final Class<? extends EntityBeingEnhanced> modEntityBeingEnhanced = cl.startModification(EntityBeingEnhanced.class)
                .addProperties(pd)
                .endModification();

        // enhance(EntityBeingModified)
        //      prop1: EntityBeingEnhanced -> modEntityBeingEnhanced
        final NewProperty np = NewProperty.fromField(EntityBeingModified.class, "prop1").changeType(modEntityBeingEnhanced);
        final Class<?> modEntityBeingModified = cl.startModification(EntityBeingModified.class)
                .modifyProperties(np)
                .endModification();

        // enhance(TopLevelEntity)
        //      prop1: EntityBeingModified -> modEntityBeingModified
        final NewProperty topLevelNp1 = NewProperty.fromField(TopLevelEntity.class, "prop1").changeType(modEntityBeingModified);
        final Class<?> mod1TopLevelEntity = cl.startModification(TopLevelEntity.class)
                .modifyProperties(topLevelNp1)
                .endModification();

        // enhance(mod1TopLevelEntity)
        //      prop2: EntityBeingModified -> modEntityBeingModified
        final NewProperty topLevelNp2 = NewProperty.fromField(mod1TopLevelEntity, "prop2")
                .changeType(modEntityBeingModified);
        final Class<?> mod2TopLevelEntity = cl.startModification(mod1TopLevelEntity)
                .modifyProperties(topLevelNp2)
                .endModification();

        assertTrue("Incorrect type name.", modEntityBeingEnhanced.getName().startsWith(
                        EntityBeingEnhanced.class.getName() + DynamicTypeNamingService.APPENDIX + "_"));
        assertTrue("Incorrect type name.", modEntityBeingModified.getName().startsWith(
                        EntityBeingModified.class.getName() + DynamicTypeNamingService.APPENDIX + "_"));
        assertTrue("Incorrect type name.", mod1TopLevelEntity.getName().startsWith(
                        TopLevelEntity.class.getName() + DynamicTypeNamingService.APPENDIX + "_"));
        assertTrue("Incorrect type name.", mod2TopLevelEntity.getName().startsWith(
                        TopLevelEntity.class.getName() + DynamicTypeNamingService.APPENDIX + "_"));

        // make sure that names are unique
        final List<String> names = Stream.of(modEntityBeingEnhanced, modEntityBeingModified,
                                             mod1TopLevelEntity, mod2TopLevelEntity)
                                         .map(Class::getName).toList();
        assertEquals("Generated types' names are not unique", names.size(), names.stream().distinct().count());
    }

    @Test
    public void test_instantiation_of_enhanced_type() throws Exception {
        final Class<? extends EntityBeingEnhanced> modEntityBeingEnhanced = cl.startModification(EntityBeingEnhanced.class)
                .addProperties(pd)
                .endModification();

        // enhance(EntityBeingModified)
        //      prop1: EntityBeingEnhanced -> modEntityBeingEnhanced
        final NewProperty np = NewProperty.fromField(EntityBeingModified.class, "prop1").changeType(modEntityBeingEnhanced);
        final Class<? extends EntityBeingModified> entityBeingModifiedModifiedType = cl.startModification(EntityBeingModified.class)
                .modifyProperties(np)
                .endModification();

        // create a new instance of the modified type
        final EntityBeingModified modifiedEntity = entityBeingModifiedModifiedType.getConstructor().newInstance();
        assertNotNull("Should not be null.", modifiedEntity);
    }

    @Test
    public void test_correct_modification_of_entity_being_modified() throws Exception {
        final Class<? extends EntityBeingEnhanced> modEntityBeingEnhanced = cl.startModification(EntityBeingEnhanced.class)
                .addProperties(pd)
                .endModification();

        // enhance(EntityBeingModified)
        //      prop1: EntityBeingEnhanced -> modEntityBeingEnhanced
        final NewProperty np = NewProperty.fromField(EntityBeingModified.class, "prop1").changeType(modEntityBeingEnhanced);
        final Class<? extends EntityBeingModified> entityBeingModifiedModifiedType = cl.startModification(EntityBeingModified.class)
                .modifyProperties(np)
                .endModification();

        // create a new instance of the modified type
        final EntityBeingModified modifiedEntity = entityBeingModifiedModifiedType.getConstructor().newInstance();
        assertNotNull("Should not be null.", modifiedEntity);

        // let's ensure that property types are compatible 
        // original prop2 and prop1 are of the same type - EntityBeingEnhanced
        // *unenhanced* prop2 should be compatible with the *enhanced* prop1
        final Field enhancedProp = Finder.findFieldByName(entityBeingModifiedModifiedType, "prop1");
        final Field unenhancedProp = Finder.findFieldByName(entityBeingModifiedModifiedType, "prop2");
        assertTrue("Incorrect enhanced property type.", enhancedProp.getType().getName().startsWith(
                EntityBeingEnhanced.class.getName() + DynamicTypeNamingService.APPENDIX + "_"));
        assertEquals("Incorrect unenhanced property type.", EntityBeingEnhanced.class.getName(), unenhancedProp.getType().getName());
        assertTrue("Original type should be assignable FROM the enhanced type",
                unenhancedProp.getType().isAssignableFrom(enhancedProp.getType()));
        assertFalse("Enhanced type should be assignable TO the original type",
                // in other words, enhanced type should NOT be assignable FROM the original type
                enhancedProp.getType().isAssignableFrom(unenhancedProp.getType()));

        // let's see what happens with the selfTypeProperty's type
        // self-referencing properties should NOT be implicitly modified
        final Field selfTypeProperty = Finder.getFieldByName(entityBeingModifiedModifiedType, "selfTypeProperty");
        assertEquals("Self-referencing properties should NOT be implicitly modified.", 
                EntityBeingModified.class, selfTypeProperty.getType());
    }

    @Test
    public void test_correct_modification_of_top_level_entity() throws Exception {
        // enhance(EntityBeingEnhanced)
        final Class<? extends EntityBeingEnhanced> modEntityBeingEnhanced = cl.startModification(EntityBeingEnhanced.class)
                .addProperties(pd)
                .endModification();

        // enhance(EntityBeingModified)
        //      prop1: EntityBeingEnhanced -> modEntityBeingEnhanced
        final NewProperty np = NewProperty.fromField(EntityBeingModified.class, "prop1").changeType(modEntityBeingEnhanced);
        final Class<? extends EntityBeingModified> entityBeingModifiedModifiedType = cl.startModification(EntityBeingModified.class)
                .modifyProperties(np)
                .endModification();

        // enhance(TopLevelEntity)
        //      prop1: EntityBeingModified -> modEntityBeingModified
        //      prop2: EntityBeingModified -> modEntityBeingModified
        // TopLevelEntity.prop1 and TopLevelEntity.prop2 should have their type replaced with entityBeingModifiedModifiedType
        final NewProperty topLevelNp1 = NewProperty.fromField(TopLevelEntity.class, "prop1").changeType(entityBeingModifiedModifiedType);
        final NewProperty topLevelNp2 = NewProperty.fromField(TopLevelEntity.class, "prop2").changeType(entityBeingModifiedModifiedType);
        final Class<? extends TopLevelEntity> topLevelEntityModifiedType = cl.startModification(TopLevelEntity.class)
                .modifyProperties(topLevelNp1, topLevelNp2)
                .endModification();

        // create a new instance of the modified TopLevelEntity type
        final TopLevelEntity topLevelEntity = topLevelEntityModifiedType.getConstructor().newInstance();
        assertNotNull("Should not be null.", topLevelEntity);

        // let's ensure that property types are compatible 
        // original prop2 and prop1 are of the same type - EntityBeingModified
        final Field prop1 = topLevelEntityModifiedType.getDeclaredField("prop1");
        final Field prop2 = topLevelEntityModifiedType.getDeclaredField("prop2");
        assertEquals("Enhanced prop1 and prop2 should be of the same type.", prop2.getType(), prop1.getType());

        // now take one of the modified properties from the enhanced TopLevelEntity and ensure that its type is indeed modified 
        final Field enhancedProp = prop1.getType().getDeclaredField("prop1");
        final Field unenhancedProp = Finder.getFieldByName(prop1.getType(), "prop2");
        assertTrue("Incorrect property type.", enhancedProp.getType().getName().startsWith(
                        EntityBeingEnhanced.class.getName() + DynamicTypeNamingService.APPENDIX + "_"));
        assertEquals("Incorrect property type.", EntityBeingEnhanced.class, unenhancedProp.getType());
        assertTrue("Original type should be assignable FROM the enhanced type.",
                unenhancedProp.getType().isAssignableFrom(enhancedProp.getType()));
        assertFalse("Enhanced type should be assignable TO the original type.",
                // in other words, enhanced type should NOT be assignable FROM the original type
                enhancedProp.getType().isAssignableFrom(unenhancedProp.getType()));
    }

    @Test
    public void test_observation_of_setter_for_new_property_in_instance_of_generated_entity_type_used_for_property_in_higher_order_type()
            throws Exception 
    {
        // enhance(EntityBeingEnhanced)
        final Class<? extends EntityBeingEnhanced> modEntityBeingEnhanced = cl.startModification(EntityBeingEnhanced.class)
                .addProperties(pd)
                .endModification();
        module.getDomainMetaPropertyConfig().setDefiner(modEntityBeingEnhanced, NEW_PROPERTY, 
                new IAfterChangeEventHandler<Object>() 
        {
            @Override
            public void handle(final MetaProperty<Object> property, final Object entityPropertyValue) {
                observed = true;
            }
        });

        // enhance(EntityBeingModified)
        //      prop1: EntityBeingEnhanced -> modEntityBeingEnhanced
        final NewProperty np = NewProperty.fromField(EntityBeingModified.class, "prop1").changeType(modEntityBeingEnhanced);
        final Class<? extends EntityBeingModified> modEntityBeingModified = cl.startModification(EntityBeingModified.class)
                .modifyProperties(np)
                .endModification();

        // enhance(TopLevelEntity)
        //      prop1: EntityBeingModified -> modEntityBeingModified
        //      prop2: EntityBeingModified -> modEntityBeingModified
        final NewProperty topLevelNp1 = NewProperty.fromField(TopLevelEntity.class, "prop1").changeType(modEntityBeingModified);
        final NewProperty topLevelNp2 = NewProperty.fromField(TopLevelEntity.class, "prop2").changeType(modEntityBeingModified);
        // get the modified TopLevelEntity type
        final Class<? extends TopLevelEntity> modTopLevelEntity = cl.startModification(TopLevelEntity.class)
                .modifyProperties(topLevelNp1, topLevelNp2)
                .endModification();

        // create new instances of the modified TopLevelEntity and EntityBeingModified types using entity factory
        final TopLevelEntity topLevelEntity = factory.newByKey(modTopLevelEntity, "key");
        assertNotNull("Should not be null.", topLevelEntity);

        final EntityBeingModified entityBeingModified = factory.newByKey(modEntityBeingModified, "key");
        assertNotNull("Should not be null.", entityBeingModified);

        final EntityBeingEnhanced entityBeingEnhanced = factory.newByKey(modEntityBeingEnhanced, "key");
        assertNotNull("Should not be null.", entityBeingEnhanced);

        topLevelEntity.set("prop1", entityBeingModified);
        entityBeingModified.set("prop1", entityBeingEnhanced);
        final Money newPropertyValue = new Money("23.32");
        entityBeingEnhanced.set(NEW_PROPERTY, newPropertyValue);

        assertTrue("Setter for the new property should have been observed.", observed);
        assertEquals("Incorrect property value", newPropertyValue, entityBeingEnhanced.get(NEW_PROPERTY));
        assertEquals("Incorrect property value", newPropertyValue, entityBeingModified.get("prop1." + NEW_PROPERTY));
        assertEquals("Incorrect property value", newPropertyValue, topLevelEntity.get("prop1.prop1." + NEW_PROPERTY));
        assertEquals("Incorrect property value", entityBeingModified, topLevelEntity.get("prop1"));
    }

    @Test
    public void test_modification_of_collectional_property_signature() throws Exception {
        // Collection<EntityBeingEnhanced> prop1;
        final Field field = Finder.findFieldByName(EntityWithCollectionalPropety.class, "prop1");
        assertTrue("Incorrect collectional type.", Collection.class.isAssignableFrom(field.getType()));
        assertEquals("Incorrect signature for collectional property.",
                EntityBeingEnhanced.class,
                PropertyTypeDeterminator.determinePropertyType(EntityWithCollectionalPropety.class, "prop1"));

        // enhance(EntityBeingEnhanced)
        final Class<? extends EntityBeingEnhanced> modEntityBeingEnhanced = cl.startModification(EntityBeingEnhanced.class)
                .addProperties(pd)
                .endModification();

        // enhance(EntityWithCollectionalProperty)
        //      prop1: Collection<EntityBeingEnhanced> -> Collection<modEntityBeingEnhanced>
        final NewProperty collectionalPropModification = NewProperty.fromField(EntityWithCollectionalPropety.class, "prop1")
                .setTypeArguments(modEntityBeingEnhanced);
        final Class<? extends EntityWithCollectionalPropety> modifiedType = cl.startModification(EntityWithCollectionalPropety.class)
                .modifyProperties(collectionalPropModification)
                .endModification();

        // test the modified field attributes such as type and IsProperty annotation
        final Field modField = Finder.findFieldByName(modifiedType, "prop1");
        assertTrue("Incorrect collectional type.", Collection.class.isAssignableFrom(modField.getType()));
        assertEquals("Incorrect signature for collectional property.", 
                modEntityBeingEnhanced,
                PropertyTypeDeterminator.determinePropertyType(modifiedType, "prop1"));

        final IsProperty annotation = AnnotationReflector.getAnnotation(modField, IsProperty.class);
        assertNotNull("There should be IsProperty annotation.", annotation);
        assertEquals("Incorrect value in IsProperty annotation.", modEntityBeingEnhanced, annotation.value());
    }

    @Test
    public void test_getting_setting_and_observation_of_modified_collectional_property() throws Exception {
        // enhance(EntityBeingEnhanced)
        final Class<? extends EntityBeingEnhanced> modEntityBeingEnhanced = cl.startModification(EntityBeingEnhanced.class)
                .addProperties(pd)
                .endModification();

        // enhance(EntityWithCollectionalPropety)
        //      prop1: Collection<EntityBeingEnhanced> -> Collection<modEntityBeingEnhanced>
        final NewProperty collectionalPropModification = NewProperty.fromField(EntityWithCollectionalPropety.class, "prop1")
                .setTypeArguments(modEntityBeingEnhanced);
        final Class<? extends EntityWithCollectionalPropety> modEntityWithCollectionalPropety =
                cl.startModification(EntityWithCollectionalPropety.class)
                .modifyProperties(collectionalPropModification)
                .endModification();

        module.getDomainMetaPropertyConfig().setDefiner(modEntityWithCollectionalPropety, "prop1", new IAfterChangeEventHandler<Object>() {
            @Override
            public void handle(final MetaProperty<Object> property, final Object entityPropertyValue) {
                observed = true;
            }
        });

        final EntityWithCollectionalPropety entity = factory.newByKey(modEntityWithCollectionalPropety, "key");
        assertNotNull("Should have been instantiated.", entity);
        assertNotNull("Initial collectional property value should not be null.", entity.get("prop1"));

        // test mutator set and getter
        final ArrayList value = new ArrayList();
        value.add(factory.newByKey(modEntityBeingEnhanced, "key1"));
        entity.set("prop1", value);
        Collection result = (Collection) entity.get("prop1");
        assertNotNull("Collectional property should not be null after setting.", result);
        assertEquals("Incorrect number of elements in the collectional property.", 1, result.size());
        assertTrue("Observation should have been triggered.", observed);
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
                .addProperties(pd)
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
                .addProperties(pd)
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
                .addProperties(pd)
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
}
