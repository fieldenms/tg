package ua.com.fielden.platform.reflection;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.stream.Collectors;

import org.junit.Test;

import com.google.inject.Injector;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.reflection.Finder.IPropertyPathFilteringCondition;
import ua.com.fielden.platform.reflection.exceptions.ReflectionException;
import ua.com.fielden.platform.reflection.test_entities.CollectionalEntity;
import ua.com.fielden.platform.reflection.test_entities.ComplexEntity;
import ua.com.fielden.platform.reflection.test_entities.ComplexKeyEntity;
import ua.com.fielden.platform.reflection.test_entities.ComplexPartEntity1;
import ua.com.fielden.platform.reflection.test_entities.DynamicKeyEntity;
import ua.com.fielden.platform.reflection.test_entities.DynamicKeyPartEntity;
import ua.com.fielden.platform.reflection.test_entities.EntityWithoutDesc;
import ua.com.fielden.platform.reflection.test_entities.FirstLevelEntity;
import ua.com.fielden.platform.reflection.test_entities.MultiLevelEntity;
import ua.com.fielden.platform.reflection.test_entities.SecondLevelEntity;
import ua.com.fielden.platform.reflection.test_entities.SimpleEntity;
import ua.com.fielden.platform.reflection.test_entities.SimpleEntityWithCommonProperties;
import ua.com.fielden.platform.reflection.test_entities.SimplePartEntity;
import ua.com.fielden.platform.reflection.test_entities.UnionEntityForReflector;
import ua.com.fielden.platform.reflection.test_entities.UnionEntityHolder;
import ua.com.fielden.platform.reflection.test_entities.UnionEntityWithoutDesc;
import ua.com.fielden.platform.test.CommonTestEntityModuleWithPropertyFactory;

/**
 * Test case for {@link Finder}.
 *
 * @author TG Team
 *
 */
public class FinderTest {
    private final Injector injector = new ApplicationInjectorFactory().add(new CommonTestEntityModuleWithPropertyFactory()).getInjector();
    private final EntityFactory factory = injector.getInstance(EntityFactory.class);

    private final IPropertyPathFilteringCondition filter = new IPropertyPathFilteringCondition() {
        @Override
        public boolean ignore(final String propertyName) {
            return false;
        }

        @Override
        public boolean ignore(final Class<?> enttyType) {
            return false;
        }
    };

    @Test
    public void test_that_can_find_meta_poperties() {
        final SecondLevelEntity entity = factory.newByKey(SecondLevelEntity.class, "property", "propertyTwo", 1L);
        final SecondLevelEntity entity2 = factory.newByKey(SecondLevelEntity.class, "property2", "propertyTwo2", 2L);
        final SecondLevelEntity entity3 = factory.newByKey(SecondLevelEntity.class, "property3", "propertyTwo3", 3L);

        entity.setPropertyOfSelfType(entity2);
        entity2.setPropertyOfSelfType(entity3);
        entity3.setPropertyOfSelfType(entity);

        List<MetaProperty<?>> metaProperties = Finder.findMetaProperties(entity, "propertyOfSelfType.propertyOfSelfType.propertyOfSelfType.propertyOfSelfType");
        assertEquals(4, metaProperties.size());
        assertEquals(entity, metaProperties.get(0).getEntity());
        assertEquals(entity2, metaProperties.get(1).getEntity());
        assertEquals(entity3, metaProperties.get(2).getEntity());
        assertEquals(entity, metaProperties.get(3).getEntity());

        metaProperties = Finder.findMetaProperties(entity, "propertyOfSelfType.property.nonExistentProperty");
        assertEquals(2, metaProperties.size());
        assertEquals(entity2, metaProperties.get(1).getEntity());

        boolean exceptionHasBeenThrown = false;
        try {
            metaProperties = Finder.findMetaProperties(entity, "propertyOfSelfType.nonExistentProperty");
        } catch (final ReflectionException e) {
            exceptionHasBeenThrown = true;
        }
        assertTrue(exceptionHasBeenThrown);
    }

    @Test
    public void non_filtered_property_descriptor_creation_includes_all_properties_in_entity_hierarchy() {
        assertEquals("Incorrect number of properties.", 4, Finder.getPropertyDescriptors(SimpleEntity.class).size());
        assertEquals("Incorrect number of properties.", 5, Finder.getPropertyDescriptors(FirstLevelEntity.class).size());
        assertEquals("Incorrect number of properties.", 8, Finder.getPropertyDescriptors(SecondLevelEntity.class).size());
    }

    @Test
    public void filtered_out_by_name_properties_are_skipped_from_property_descriptor_creation() {
        assertEquals("Incorrect number of properties.", 3, Finder.getPropertyDescriptors(FirstLevelEntity.class, f -> "key".equals(f.getName()) || "desc".equals(f.getName())).size());
    }
    
    @Test
    public void properties_lower_than_top_level_type_are_skiped_from_property_descriptor_creation() {
        final List<PropertyDescriptor<SecondLevelEntity>> propertyDescriptors = Finder.getPropertyDescriptors(SecondLevelEntity.class, f -> f.getDeclaringClass() != SecondLevelEntity.class);
        assertEquals("Incorrect number of properties.", 3, propertyDescriptors.size());
        assertEquals("Result should have properties ordered in accordance with their declaration.", new ArrayList<String>(Arrays.asList(new String[]{"Another", "Self Type", "Dummy Reference"})), propertyDescriptors.stream().map(p -> p.getKey()).collect(Collectors.toList()));
    }

    @Test
    public void properties_other_than_from_the_middle_of_type_hierarchy_are_skiped_from_property_descriptor_creation() {
        final List<PropertyDescriptor<SecondLevelEntity>> propertyDescriptors = Finder.getPropertyDescriptors(SecondLevelEntity.class, f -> f.getDeclaringClass() != FirstLevelEntity.class);
        assertEquals("Incorrect number of properties.", 3, propertyDescriptors.size());
        assertEquals("Result should have properties ordered in accordance with their declaration.", new ArrayList<String>(Arrays.asList(new String[]{"Two", "Property", "Simple Entity"})), propertyDescriptors.stream().map(p -> p.getKey()).collect(Collectors.toList()));
    }

    
    @Test
    public void testFindCommonProperties() {
        final List<Class<? extends AbstractEntity<?>>> types = new ArrayList<>();
        types.add(SimpleEntityWithCommonProperties.class);
        types.add(ComplexEntity.class);
        types.add(DynamicKeyEntity.class);
        final List<String> commonProperties = Finder.findCommonProperties(types);
        assertEquals("The number of common properties must be 2", 2, commonProperties.size());
        assertFalse("Key can not be common property", commonProperties.contains("key"));
        assertTrue("Desc must be common property", commonProperties.contains("desc"));
        assertTrue("commonProperty must be common for all tested classes", commonProperties.contains("commonProperty"));
        assertFalse("uncommonProperty can not be common", commonProperties.contains("uncommonProperty"));
    }

    @Test
    public void testFindCommonPropertiesWithoutDesc() {
        final List<Class<? extends AbstractEntity<?>>> types = new ArrayList<>();
        types.add(SimpleEntityWithCommonProperties.class);
        types.add(ComplexEntity.class);
        types.add(DynamicKeyEntity.class);
        types.add(EntityWithoutDesc.class);
        final List<String> commonProperties = Finder.findCommonProperties(types);
        System.out.println(commonProperties);
        assertEquals("The number of common properties must be 2", 2, commonProperties.size());
        assertFalse("Key can not be common property", commonProperties.contains("key"));
        assertTrue("commonProperty must be common for all tested classes", commonProperties.contains("commonProperty"));
        assertFalse("uncommonProperty can not be common", commonProperties.contains("uncommonProperty"));
        assertTrue("desc must be common for all tested classes", commonProperties.contains("desc"));
    }

    @Test
    public void testThatFindMetaPropertyWorks() {
        // /////////////////////// simple case -- first level property ////////////////////
        final FirstLevelEntity firstLevelEntity = factory.newByKey(FirstLevelEntity.class, "key-1-1", "key-1-2");
        MetaProperty<?> metaProperty = Finder.findMetaProperty(firstLevelEntity, "property");
        assertNotNull("Should have found meta-property.", metaProperty);
        assertEquals("Incorrect property name as present in the meta-property.", "property", metaProperty.getName());
        // /////////////////////// complex case -- multilevel property ///////////////////
        final SecondLevelEntity secondLevelEntity = factory.newByKey(SecondLevelEntity.class, "key-1-1", "key-1-2", 1L);
        assertNotNull("Should have created an instance of SecondLevelEntity.", secondLevelEntity);
        metaProperty = Finder.findMetaProperty(secondLevelEntity, "anotherProperty");
        assertNotNull("Should have found meta-property.", metaProperty);
        assertEquals("Incorrect property name as present in the meta-property.", "anotherProperty", metaProperty.getName());

        metaProperty = Finder.findMetaProperty(secondLevelEntity, "propertyOfSelfType.anotherProperty");
        assertNull("Meta property should not exist due to null value in property propertyOfSelfType.", metaProperty);
        // set the property propertyOfSelfType to itself and test retrieval of the second level meta-property
        secondLevelEntity.setPropertyOfSelfType(secondLevelEntity);
        metaProperty = Finder.findMetaProperty(secondLevelEntity, "propertyOfSelfType.anotherProperty");
        assertNotNull("Should have found meta-property.", metaProperty);
        assertEquals("Incorrect property name as present in the meta-property.", "anotherProperty", metaProperty.getName());
        // test retrieval of the third level meta-property
        secondLevelEntity.setPropertyOfSelfType(secondLevelEntity);
        metaProperty = Finder.findMetaProperty(secondLevelEntity, "propertyOfSelfType.propertyOfSelfType.property");
        assertNotNull("Should have found meta-property.", metaProperty);
        assertEquals("Incorrect property name as present in the meta-property.", "property", metaProperty.getName());
    }

    @Test
    public void testGetMetaProperties() {
        // /////////////////////// simple case -- first level property ////////////////////
        final SecondLevelEntity entity = factory.newByKey(SecondLevelEntity.class, "key-1-1", "key-1-2", 1L);
        final SortedSet<MetaProperty<?>> metaProperties = Finder.getMetaProperties(entity);
        assertEquals("Incorrect number of properties.", 7, metaProperties.size());
    }

    @Test
    public void testGetCollectionalMetaProperties() {
        // simple case -- first level property
        final SecondLevelEntity entity = factory.newByKey(SecondLevelEntity.class, "key-1-1", "key-1-2", 1L);
        assertEquals("Incorrect number of collectional properties.", 0, Finder.getCollectionalMetaProperties(entity, String.class).size());
        // collectional case
        final CollectionalEntity collectionalentity = factory.newByKey(CollectionalEntity.class, "key-1-1");
        assertEquals("Incorrect number of collectional properties.", 2, Finder.getCollectionalMetaProperties(collectionalentity, String.class).size());
        assertEquals("Incorrect number of collectional properties.", 1, Finder.getCollectionalMetaProperties(collectionalentity, Integer.class).size());
    }

    @Test
    public void test_find_properties_that_are_entities() {
        final List<Field> fields = Finder.findPropertiesThatAreEntities(ComplexEntity.class);
        assertEquals("Incorrect number of properties.", 1, fields.size());
        assertEquals("Incorrect property name.", "anotherUncommonProperty", fields.get(0).getName());
    }

    @Test
    public void test_find_paths_for_properties_of_specified_type_in_one_level_case() {
        final List<String> fields = Finder.findPathsForPropertiesOfType(ComplexEntity.class, SimpleEntity.class, filter);
        assertEquals("Incorrect number of SimpleEntity property paths.", 1, fields.size());
        assertEquals("Incorrect path for SimpleEntity property.", "anotherUncommonProperty", fields.get(0));
    }

    @Test
    public void test_find_paths_for_properties_of_specified_type_in_multi_level_case() {
        final List<String> fields = Finder.findPathsForPropertiesOfType(MultiLevelEntity.class, SecondLevelEntity.class, filter);
        assertEquals("Incorrect number of SecondLevelEntity property paths.", 3 /*4*/, fields.size());
        //assertTrue("Incorrect path for SecondLevelEntity property.", fields.contains("propertyOfSelfType"));
        assertTrue("Incorrect path for SecondLevelEntity property.", fields.contains("propertyOfParentType"));
        assertTrue("Incorrect path for SecondLevelEntity property.", fields.contains("simplePartEntity.levelEntity"));
        assertTrue("Incorrect path for SecondLevelEntity property.", fields.contains("anotherSimplePartType.simplePartEntity.levelEntity"));
    }

    @Test
    public void test_find_paths_for_self() {
        final List<String> fields = Finder.findPathsForPropertiesOfType(MultiLevelEntity.class, MultiLevelEntity.class, filter);
        assertEquals("Incorrect number property paths.", 1, fields.size());
        assertTrue("Incorrect path for self.", fields.contains("id"));
    }

    @Test
    public void testThatFieldsAnnotatedWithWorksForFirstLevelOfInheritance() {
        List<Field> properties = Finder.findProperties(FirstLevelEntity.class);
        assertEquals("Incorrect number of properties in class FirstLevelEntity.", 5, properties.size()); // key is annotated
        assertEquals("Incorrect property name.", "propertyTwo", properties.get(0).getName());
        assertEquals("Incorrect property type.", String.class, properties.get(0).getType());

        properties = Finder.findProperties(FirstLevelEntity.class, Title.class, CompositeKeyMember.class);
        assertEquals("Incorrect number of properties with title in class FirstLevelEntity.", 2, properties.size()); // key is annotated
        assertEquals("Incorrect property name.", "propertyTwo", properties.get(0).getName());
        assertEquals("Incorrect property type.", String.class, properties.get(0).getType());

        properties = Finder.findProperties(UnionEntityForReflector.class);
        assertEquals("Incorrect number of properties in class UnionEntity", 6, properties.size());
        assertTrue("UnionEntity must have commonProperty", properties.contains(Finder.getFieldByName(UnionEntityForReflector.class, "commonProperty")));
        assertTrue("UnionEntity must have commonProperty levelEntity", properties.contains(Finder.getFieldByName(UnionEntityForReflector.class, "levelEntity")));
    }

    @Test
    public void test_that_getSimpleFieldsAnnotatedWith() {
        List<Field> properties = Finder.findRealProperties(FirstLevelEntity.class);
        assertEquals("Incorrect number of properties in class FirstLevelEntity.", 4, properties.size()); // key is annotated
        assertEquals("Incorrect property name.", "propertyTwo", properties.get(0).getName());
        assertEquals("Incorrect property type.", String.class, properties.get(0).getType());

        properties = Finder.findRealProperties(FirstLevelEntity.class, Title.class, CompositeKeyMember.class);
        assertEquals("Incorrect number of properties with title in class FirstLevelEntity.", 2, properties.size()); // key is annotated
        assertEquals("Incorrect property name.", "propertyTwo", properties.get(0).getName());
        assertEquals("Incorrect property type.", String.class, properties.get(0).getType());

        properties = Finder.findRealProperties(UnionEntityForReflector.class);
        assertEquals("Incorrect number of properties in class UnionEntity", 3, properties.size());
        
        assertTrue("UnionEntity must contain simplePartEntity property", Finder.getFieldNames(properties).contains("simplePartEntity"));
        assertTrue("UnionEntity must contain complexPartEntity property", Finder.getFieldNames(properties).contains("complexPartEntity"));
        assertTrue("UnionEntity must contain dynamicKeyPartEntity property", Finder.getFieldNames(properties).contains("dynamicKeyPartEntity"));
    }

    @Test
    public void testThatFieldsAnnotatedWithWorksForSecondLevelOfInheritance() {
        List<Field> properties = Finder.findProperties(SecondLevelEntity.class);
        assertEquals("Incorrect number of properties in class SecondLevelEntity.", 8, properties.size());
        assertEquals("Incorrect property name.", "anotherProperty", properties.get(0).getName());
        assertEquals("Incorrect property type.", Long.class, properties.get(0).getType());
        assertEquals("Incorrect property name.", "propertyOfSelfType", properties.get(4).getName());
        assertEquals("Incorrect property type.", SecondLevelEntity.class, properties.get(4).getType());

        properties = Finder.findProperties(SecondLevelEntity.class, CompositeKeyMember.class);
        assertEquals("Incorrect number of composite key properties in class SecondLevelEntity.", 3, properties.size());

        properties = Finder.findProperties(UnionEntityForReflector.class);
        assertEquals("Incorrect number of properties in the UnionEntity class", 6, properties.size());
        assertTrue("The UnionEntity must have desc property", properties.contains(Finder.getFieldByName(UnionEntityForReflector.class, AbstractEntity.DESC)));

        properties = Finder.findProperties(UnionEntityForReflector.class, CompositeKeyMember.class);
        assertEquals("Incorrect number of composite key properties in class UnionEntity", 0, properties.size());
    }

    @Test
    public void composite_key_members_are_identified_for_first_level_of_inheritance_with_correct_order() {
        List<Field> members = Finder.getKeyMembers(FirstLevelEntity.class);
        assertEquals("Incorrect number of composite key members in FirstLevelEntity.", 2, members.size());
        assertEquals("property", members.get(0).getName());
        assertEquals("propertyTwo", members.get(1).getName());
        
        members = Finder.getKeyMembers(UnionEntityWithoutDesc.class);
        assertEquals("IncorrectNumber of key members in the UnionEntityWithoutDesc", 1, members.size());
    }

    @Test
    public void composite_key_members_are_identified_for_second_level_of_inheritance_with_correct_order() {
        final List<Field> members = Finder.getKeyMembers(SecondLevelEntity.class);
        // two properties annotated as composite keys are inherited from ForstLevelEntity and one declared within SecondLevelEntity
        assertEquals("Incorrect number of composite key members in SecondLevelEntity.", 3, members.size());
        assertEquals("property", members.get(0).getName());
        assertEquals("propertyTwo", members.get(1).getName());
        assertEquals("anotherProperty", members.get(2).getName());

    }

    @Test
    public void testThatGetSimpleKeyMembersWorks() {
        List<Field> members = Finder.getKeyMembers(SimpleEntity.class);
        assertEquals("Incorrect number of simple key members in SimpleEntity.", 1, members.size());
        assertEquals("Incorrect property name used for a simple key in SimpleEntity.", "key", members.get(0).getName());

        members = Finder.getKeyMembers(UnionEntityForReflector.class);
        assertEquals("Incorrect number of key members in the UnionEntity class.", 1, members.size());
        assertEquals("Incorrect property name used for a simple key member", "key", members.get(0).getName());
    }

    @Test
    public void existing_properties_can_be_found_with_getFieldByName() throws Exception {
        final Field field = Finder.getFieldByName(SecondLevelEntity.class, "property");
        assertNotNull("Failed to located a filed.", field);
        assertEquals("Incorrect type.", String.class, field.getType());
    }

    @Test
    public void common_properties_for_uniton_entity_can_be_found_with_getFieldByName() throws Exception {
        final Field unionEntityField = Finder.getFieldByName(UnionEntityForReflector.class, "commonProperty");
        assertNotNull("Failed to locate field in the UnionEntity class", unionEntityField);
        assertEquals("Incorrect commonProperty type.", String.class, unionEntityField.getType());
    }

    @Test
    public void trying_to_locate_non_existing_properties_with_getFieldByName_throws_exception() throws Exception {
        final String name = "nonExistingProperty";
        final Class<SecondLevelEntity> type = SecondLevelEntity.class;
        try {
            Finder.getFieldByName(type, name);
            fail("Should have thrown an exception.");
        } catch (final ReflectionException ex) {
            assertEquals(format("Failed to locate field [%s] in type [%s]", name, type.getName()), ex.getMessage());
        }
    }

    @Test
    public void existing_properties_can_be_found_with_getFieldByNameOptionally() throws Exception {
        final Optional<Field> field = Finder.getFieldByNameOptionally(SecondLevelEntity.class, "property");
        assertTrue("Failed to located a filed.", field.isPresent());
        assertTrue("Incorrect type.", field.filter(f -> f.getType() == String.class).isPresent());
    }

    @Test
    public void common_properties_for_union_entity_can_be_found_with_getFieldByNameOptionally() throws Exception {
        final Optional<Field> unionEntityField = Finder.getFieldByNameOptionally(UnionEntityForReflector.class, "commonProperty");
        assertTrue("Failed to locate field in the UnionEntity class", unionEntityField.isPresent());
        assertTrue("Incorrect commonProperty type.", unionEntityField.filter(f -> f.getType() == String.class).isPresent());
    }

    @Test
    public void test_that_findFieldByName_works() throws Exception {
        Field field = Finder.findFieldByName(SecondLevelEntity.class, "propertyOfSelfType.property");
        assertNotNull("Failed to located a filed.", field);
        assertEquals("Incorrect type.", String.class, field.getType());
        field = Finder.findFieldByName(ComplexKeyEntity.class, "key.key");
        assertNotNull("Faild to locate field in Complex entity class", field);
        assertEquals("Incorrect type of the KeyEntity key field", Comparable.class, field.getType());
        field = Finder.findFieldByName(ComplexKeyEntity.class, "key.simpleEntity");
        assertNotNull("Faild to locate simpleEntity field in Complex entity class", field);
        assertEquals("Incorrect type of the simpleEntity field", SimpleEntity.class, field.getType());
        field = Finder.findFieldByName(ComplexKeyEntity.class, "key.simpleEntity.key");
        assertNotNull("Faild to locate simpleEntity's key field in Complex entity class", field);
        assertEquals("Incorrect type of the simpleEntity's key field", Comparable.class, field.getType());
        field = Finder.findFieldByName(UnionEntityForReflector.class, "levelEntity.propertyOfSelfType.anotherProperty");
        assertNotNull("Faild to locate levelEntity.propertyOfSelfType.anotherProperty field in the UnionEntity class", field);
        assertEquals("Incorrect type of the levelEntity.propertyOfSelfType.anotherProperty field", Long.class, field.getType());
        try {
            Finder.findFieldByName(SecondLevelEntity.class, "propertyOfSelfType.property.nonExistingProperty");
            fail("Should have thrown an exception.");
        } catch (final Exception ex) {
            System.out.println(ex.getMessage());
        }

        // methods finding tests: (including method inheritance & nested dot-notation properties)
        String methodName = "propertyOfSelfType.propertyOfSelfType.propertyOfSelfType.methodSecondLevel()";
        try {
            Finder.findFieldByName(SecondLevelEntity.class, methodName);
            fail("Field should not be found for method [" + methodName + "] definition.");
        } catch (final Finder.MethodFoundException e) {
            // valid case
        } catch (final Exception e) {
            fail("Method [" + methodName + "] should be found.");
        }

        methodName = "propertyOfSelfType.propertyOfSelfType.methodFirstLevel()";
        try {
            Finder.findFieldByName(SecondLevelEntity.class, methodName);
            fail("Field should not be found for inherited method [" + methodName + "] definition.");
        } catch (final Finder.MethodFoundException e) {
            // valid case
        } catch (final Exception e) {
            fail("Inherited method [" + methodName + "] should be found.");
        }

        methodName = "propertyOfSelfType.propertyOfSelfType.methodFirstLevel1()";
        try {
            Finder.findFieldByName(SecondLevelEntity.class, methodName);
            fail("Field should not be found for method [" + methodName + "] definition.");
        } catch (final Finder.MethodFoundException e) {
            fail("Method [" + methodName + "] should not be found.");
        } catch (final ReflectionException e) {
            // valid case
        }

        final String fieldName = "propertyOfSelfType.getPropertyOfSelfType().propertyOfSelfType";
        field = Finder.findFieldByName(SecondLevelEntity.class, fieldName);
        assertNotNull("Faild to locate " + fieldName + " field in the SecondLevelEntity class", field);
        assertEquals("Incorrect type of the " + fieldName + " field", SecondLevelEntity.class, field.getType());
    }

    @Test
    public void testThatFindFieldValueByNameWorks() throws Exception {
        final SecondLevelEntity inst = new SecondLevelEntity();
        inst.setPropertyOfSelfType(inst);
        inst.setProperty("value");

        assertNull("Incorrect value.", Finder.findFieldValueByName(null, "property"));
        assertEquals("Incorrect value.", "value", Finder.findFieldValueByName(inst, "property"));
        assertEquals("Incorrect value.", "value", Finder.findFieldValueByName(inst, "propertyOfSelfType.property"));
        assertEquals("Incorrect value.", "value", Finder.findFieldValueByName(inst, "propertyOfSelfType.propertyOfSelfType.property"));
        assertEquals("Incorrect value.", "value", Finder.findFieldValueByName(inst, "propertyOfSelfType.propertyOfSelfType.propertyOfSelfType.property"));

        inst.setPropertyOfSelfType(null);
        assertNull("Incorrect value.", Finder.findFieldValueByName(inst, "propertyOfSelfType.propertyOfSelfType"));
        assertNull("Incorrect value.", Finder.findFieldValueByName(inst, "propertyOfSelfType.propertyOfSelfType.property"));

    }

    @Test
    public void test_that_findFieldValueByName_works_for_AbstractUnionEntity() throws Exception {
        final SecondLevelEntity inst = new SecondLevelEntity();
        inst.setPropertyOfSelfType(inst);
        inst.setProperty("value");

        final SimplePartEntity simpleProperty = factory.newEntity(SimplePartEntity.class, 1L, "KEY");
        simpleProperty.setDesc("DESC");
        simpleProperty.setCommonProperty("common value");
        simpleProperty.setLevelEntity(inst);
        simpleProperty.setUncommonProperty("uncommon value");
        final FirstLevelEntity firstLevelEntity = factory.newByKey(FirstLevelEntity.class, "property", "property two");
        firstLevelEntity.setDesc("COMPLEX DESC FOR KEY");

        final UnionEntityForReflector uefr = factory.newEntity(UnionEntityForReflector.class);
        uefr.setSimplePartEntity(simpleProperty);
        final ComplexPartEntity1 complexEntity = factory.newEntity(ComplexPartEntity1.class, 1L, /* firstLevelEntity */uefr);
        complexEntity.setDesc("COMPLEX DESC");
        complexEntity.setCommonProperty("common property");
        complexEntity.setLevelEntity(inst);
        complexEntity.setAnotherUncommonProperty("another uncommon property");

        final SecondLevelEntity secondKeyParameter = factory.newByKey(SecondLevelEntity.class, "property_one", "property_two", 2L);
        secondKeyParameter.setProperty("prp");
        secondKeyParameter.setPropertyOfSelfType(secondKeyParameter);

        final DynamicKeyPartEntity dynamicEntity = factory.newByKey(DynamicKeyPartEntity.class, "first_key", secondKeyParameter);
        dynamicEntity.setDesc("DYNAMIC DESC");
        dynamicEntity.setCommonProperty("common property");
        dynamicEntity.setLevelEntity(inst);
        dynamicEntity.setUncommonProperty("dynamic uncommon property");

        UnionEntityForReflector unionEntity = factory.newEntity(UnionEntityForReflector.class);
        unionEntity.setSimplePartEntity(simpleProperty);

        assertEquals("Incorrect value.", "common value", Finder.findFieldValueByName(unionEntity, "commonProperty"));
        assertEquals("Incorrect value retrieved from getCommonProperty()", "common value", Finder.findFieldValueByName(unionEntity, "getCommonProperty()"));
        assertEquals("Incorrect common property of simple part entity", "common value", Finder.findFieldValueByName(unionEntity, "simplePartEntity.commonProperty"));
        assertEquals("Incorrect common property of simple part entity", "uncommon value", Finder.findFieldValueByName(unionEntity, "simplePartEntity.uncommonProperty"));
        assertEquals("Incorrect levelEntity of simple part entity", "value", Finder.findFieldValueByName(unionEntity, "levelEntity.property"));

        assertEquals("Incorrect key value of the union entity", "KEY", Finder.findFieldValueByName(unionEntity, "key"));
        assertEquals("Incorrect key value of the union entity", "KEY", Finder.findFieldValueByName(unionEntity, "getKey()"));
        assertEquals("Incorrect desc value of the union entity", "DESC", Finder.findFieldValueByName(unionEntity, "desc"));
        assertEquals("Incorrect desc value of the union entity", "DESC", Finder.findFieldValueByName(unionEntity, "getDesc()"));
        try {
            Finder.findFieldValueByName(unionEntity, "uncommonProperty");
            fail("There shouldn't be any uncommonProperty");
        } catch (final Exception ex) {
        }
        try {
            Finder.findFieldValueByName(unionEntity, "getUncommonProperty()");
            fail("There shouldn't be any getUncommonProperty()");
        } catch (final Exception ex) {
        }
        unionEntity = factory.newEntity(UnionEntityForReflector.class);
        unionEntity.setComplexPartEntity(complexEntity);
        assertEquals("Incorrect value.", "common property", Finder.findFieldValueByName(unionEntity, "commonProperty"));
        assertEquals("Incorrect value retrieved from getCommonProperty()", "common property", Finder.findFieldValueByName(unionEntity, "getCommonProperty()"));
        assertEquals("Incorrect common property of simple part entity", "common property", Finder.findFieldValueByName(unionEntity, "complexPartEntity.commonProperty"));
        assertEquals("Incorrect common property of simple part entity", "another uncommon property", Finder.findFieldValueByName(unionEntity, "complexPartEntity.anotherUncommonProperty"));
        assertEquals("Incorrect levelEntity of simple part entity", "value", Finder.findFieldValueByName(unionEntity, "levelEntity.property"));

        assertEquals("Incorrect key value of the union entity", uefr, Finder.findFieldValueByName(unionEntity, "key"));
        assertEquals("Incorrect key value of the union entity", "KEY", Finder.findFieldValueByName(unionEntity, "getKey()"));
        assertEquals("Incorrect desc value of the union entity", "COMPLEX DESC", Finder.findFieldValueByName(unionEntity, "desc"));
        assertEquals("Incorrect desc value of the union entity", "DESC", Finder.findFieldValueByName(unionEntity, "getDesc()"));
        try {
            Finder.findFieldValueByName(unionEntity, "anotherUncommonProperty");
            fail("There shouldn't be any uncommonProperty");
        } catch (final Exception ex) {
        }
        try {
            Finder.findFieldValueByName(unionEntity, "getAnotherUncommonProperty()");
            fail("There shouldn't be any getAnotherUncommonProperty()");
        } catch (final Exception ex) {
        }
        unionEntity = factory.newEntity(UnionEntityForReflector.class);
        unionEntity.setDynamicKeyPartEntity(dynamicEntity);
        assertEquals("Incorrect value.", "common property", Finder.findFieldValueByName(unionEntity, "commonProperty"));
        assertEquals("Incorrect value retrieved from getCommonProperty()", "common property", Finder.findFieldValueByName(unionEntity, "getCommonProperty()"));
        assertEquals("Incorrect common property of simple part entity", "common property", Finder.findFieldValueByName(unionEntity, "dynamicKeyPartEntity.commonProperty"));
        assertEquals("Incorrect common property of simple part entity", "dynamic uncommon property", Finder.findFieldValueByName(unionEntity, "dynamicKeyPartEntity.uncommonProperty"));
        assertEquals("Incorrect levelEntity of simple part entity", "value", Finder.findFieldValueByName(unionEntity, "levelEntity.property"));

        assertEquals("Incorrect key value of the union entity", dynamicEntity.getKey(), Finder.findFieldValueByName(unionEntity, "key"));
        assertEquals("Incorrect key value of the union entity", "first_key prp property_two 2", Finder.findFieldValueByName(unionEntity, "getKey()"));
        assertEquals("Incorrect desc value of the union entity", "DYNAMIC DESC", Finder.findFieldValueByName(unionEntity, "desc"));
        assertEquals("Incorrect desc value of the union entity", "DYNAMIC DESC", Finder.findFieldValueByName(unionEntity, "getDesc()"));
        try {
            Finder.findFieldValueByName(unionEntity, "uncommonProperty");
            fail("There shouldn't be any uncommonProperty");
        } catch (final Exception ex) {
        }
        try {
            Finder.findFieldValueByName(unionEntity, "getUncommonProperty()");
            fail("There shouldn't be any getUncommonProperty()");
        } catch (final Exception ex) {
        }
        // Testing AbstractUnionEntity when it's instance is within another AbstractEntity instance.
        unionEntity = factory.newEntity(UnionEntityForReflector.class);
        unionEntity.setSimplePartEntity(simpleProperty);
        final UnionEntityHolder unionHolder = factory.newByKey(UnionEntityHolder.class, "KEY");
        unionHolder.setUnionEntity(unionEntity);
        assertEquals("Incorrect value of unionEntity.levelEntity.property", "value", Finder.findFieldValueByName(unionHolder, "unionEntity.levelEntity.property"));
        assertEquals("Incorrect value of unionEntity.simplePartEntity.uncommonProperty", "uncommon value", Finder.findFieldValueByName(unionHolder, "unionEntity.simplePartEntity.uncommonProperty"));
        assertEquals("Incorrect value of unionEntity.getLevelEntity().property", "value", Finder.findFieldValueByName(unionHolder, "unionEntity.getLevelEntity().property"));
    }

    @Test
    public void test_field_of_type_search_routine() {
        assertEquals("Incorrect number of string fields.", 13, Finder.getFieldsOfSpecifiedType(DynamicKeyEntity.class, String.class).size());
        assertEquals("Incorrect number of SimpleEntity fields.", 1, Finder.getFieldsOfSpecifiedType(DynamicKeyEntity.class, SimpleEntity.class).size());
    }

    @Test
    public void test_that_isPropertyPresent_works() {
        assertTrue("Should be present.", Finder.isPropertyPresent(SecondLevelEntity.class, "propertyOfSelfType.propertyOfSelfType.propertyOfSelfType"));
        assertTrue("Should be present.", Finder.isPropertyPresent(SecondLevelEntity.class, "propertyOfSelfType.propertyOfSelfType.anotherProperty"));
        assertTrue("Should be present.", Finder.isPropertyPresent(SecondLevelEntity.class, "propertyOfSelfType.propertyOfSelfType.dummyReferenceProperty"));
        assertTrue("Should be present.", Finder.isPropertyPresent(SecondLevelEntity.class, "propertyOfSelfType.propertyOfSelfType.property"));
        assertTrue("Should be present.", Finder.isPropertyPresent(SecondLevelEntity.class, "propertyOfSelfType.propertyOfSelfType.critOnlyAEProperty"));
        assertFalse("Should not be present.", Finder.isPropertyPresent(SecondLevelEntity.class, "propertyOfSelfType.propertyOfSelfType.serialVersionUID"));
        assertFalse("Should not be present.", Finder.isPropertyPresent(SecondLevelEntity.class, "propertyOfSelfType.propertyOfSelfType.blablabla"));
    }

}
