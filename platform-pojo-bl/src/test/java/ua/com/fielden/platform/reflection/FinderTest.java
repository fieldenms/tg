package ua.com.fielden.platform.reflection;

import com.google.inject.Injector;
import org.junit.Test;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.reflection.Finder.IPropertyPathFilteringCondition;
import ua.com.fielden.platform.reflection.exceptions.ReflectionException;
import ua.com.fielden.platform.reflection.test_entities.*;
import ua.com.fielden.platform.sample.domain.UnionEntity;
import ua.com.fielden.platform.test.CommonEntityTestIocModuleWithPropertyFactory;
import ua.com.fielden.platform.types.tuples.T2;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static ua.com.fielden.platform.entity.AbstractEntity.*;
import static ua.com.fielden.platform.reflection.Finder.streamUnionMembersWithSubProperty;
import static ua.com.fielden.platform.reflection.Finder.streamUnionSubProperties;

/**
 * Test case for {@link Finder}.
 *
 * @author TG Team
 *
 */
public class FinderTest {
    private final Injector injector = new ApplicationInjectorFactory().add(new CommonEntityTestIocModuleWithPropertyFactory()).getInjector();
    private final EntityFactory factory = injector.getInstance(EntityFactory.class);

    private final IPropertyPathFilteringCondition filter = new IPropertyPathFilteringCondition() {
        @Override
        public boolean ignore(final String propertyName) {
            return false;
        }

        @Override
        public boolean ignore(final Class<?> entityType) {
            return false;
        }
    };

    @Test
    public void meta_poperties_can_be_found_for_dot_notated_expressions() {
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

        try {
            Finder.findMetaProperties(entity, "propertyOfSelfType.nonExistentProperty");
            fail("Exception is expected when invalid property path is specified for Finder.findMetaProperties.");
        } catch (final ReflectionException e) {
        }
    }

    @Test
    public void non_filtered_property_descriptor_creation_includes_all_properties_in_entity_hierarchy() {
        assertEquals("Incorrect number of properties.", 4, Finder.getPropertyDescriptors(SimpleEntity.class).size());
        assertEquals("Incorrect number of properties.", 4, Finder.getPropertyDescriptors(FirstLevelEntity.class).size());
        assertEquals("Incorrect number of properties.", 7, Finder.getPropertyDescriptors(SecondLevelEntity.class).size());
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
    public void common_properties_for_different_entity_types_are_those_that_match_by_name_and_type() {
        final List<Class<? extends AbstractEntity<?>>> types = new ArrayList<>();
        types.add(SimpleEntityWithCommonProperties.class);
        types.add(ComplexEntity.class);
        types.add(DynamicKeyEntity.class);
        final Set<String> commonProperties = Finder.findCommonProperties(types);
        assertEquals("The number of common properties must be 2", 2, commonProperties.size());
        assertFalse("Key can not be common property", commonProperties.contains("key"));
        assertTrue("Desc must be common property", commonProperties.contains("desc"));
        assertTrue("commonProperty must be common for all tested classes", commonProperties.contains("commonProperty"));
        assertFalse("uncommonProperty can not be common", commonProperties.contains("uncommonProperty"));
    }

    @Test
    public void common_properties_do_not_include_desc_if_one_or_more_entity_types_do_not_define_it() {
        final List<Class<? extends AbstractEntity<?>>> types = new ArrayList<>();
        types.add(SimpleEntityWithCommonProperties.class);
        types.add(ComplexEntity.class);
        types.add(DynamicKeyEntity.class);
        types.add(EntityWithoutDesc.class);
        final Set<String> commonProperties = Finder.findCommonProperties(types);
        assertEquals("The number of common properties must be 1.", 1, commonProperties.size());
        assertFalse("Key can not be common property", commonProperties.contains("key"));
        assertFalse("Desc can not be common property", commonProperties.contains("desc"));
        assertTrue("commonProperty must be common for all tested classes", commonProperties.contains("commonProperty"));
        assertFalse("uncommonProperty can not be common", commonProperties.contains("uncommonProperty"));
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
    public void common_properties_for_union_entity_can_be_found_with_getFieldByNameOptionally() {
        final Optional<Field> unionEntityField = Finder.getFieldByNameOptionally(UnionEntityForReflector.class, "commonProperty");
        assertTrue("Failed to locate field in the UnionEntity class", unionEntityField.isPresent());
        assertTrue("Incorrect commonProperty type.", unionEntityField.filter(f -> f.getType() == String.class).isPresent());
    }

    @Test
    public void findFieldByName_can_process_property_paths() {
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
        } catch (final Exception ex) { }
    }

    @Test
    public void findFieldByNameWithOwningType_can_process_property_paths() {
        T2<Class<?>, Field> typeAndField = Finder.findFieldByNameWithOwningType(SecondLevelEntity.class, "propertyOfSelfType.property");
        assertNotNull("Failed to located a filed.", typeAndField);
        assertEquals("Incorrect owning type.", SecondLevelEntity.class, typeAndField._1);
        assertEquals("Incorrect field type.", String.class, typeAndField._2.getType());

        typeAndField = Finder.findFieldByNameWithOwningType(ComplexKeyEntity.class, "key.key");
        assertNotNull("Faild to locate field in Complex entity class", typeAndField);
        assertEquals("Incorrect owning type.", KeyEntity.class, typeAndField._1);
        assertEquals("Incorrect field type.", Comparable.class, typeAndField._2.getType());

        typeAndField = Finder.findFieldByNameWithOwningType(ComplexKeyEntity.class, "key.simpleEntity");
        assertNotNull("Faild to locate simpleEntity field in Complex entity class", typeAndField);
        assertEquals("Incorrect owning type.", KeyEntity.class, typeAndField._1);
        assertEquals("Incorrect field type.", SimpleEntity.class, typeAndField._2.getType());

        typeAndField = Finder.findFieldByNameWithOwningType(ComplexKeyEntity.class, "key.simpleEntity.key");
        assertNotNull("Faild to locate simpleEntity's key field in Complex entity class", typeAndField);
        assertEquals("Incorrect owning type.", SimpleEntity.class, typeAndField._1);
        assertEquals("Incorrect field type.", Comparable.class, typeAndField._2.getType());

        typeAndField = Finder.findFieldByNameWithOwningType(UnionEntityForReflector.class, "levelEntity.propertyOfSelfType.anotherProperty");
        assertNotNull("Faild to locate levelEntity.propertyOfSelfType.anotherProperty field in the UnionEntity class", typeAndField);
        assertEquals("Incorrect owning type.", SecondLevelEntity.class, typeAndField._1);
        assertEquals("Incorrect field type.", Long.class, typeAndField._2.getType());
        try {
            Finder.findFieldByNameWithOwningType(SecondLevelEntity.class, "propertyOfSelfType.property.nonExistingProperty");
            fail("Should have thrown an exception.");
        } catch (final Exception ex) { }
    }

    @Test
    public void findFieldByName_can_process_paths_with_method_calls() {
        final String fieldName = "propertyOfSelfType.getPropertyOfSelfType().propertyOfSelfType";
        final Field field = Finder.findFieldByName(SecondLevelEntity.class, fieldName);
        assertNotNull("Faild to locate " + fieldName + " field in the SecondLevelEntity class", field);
        assertEquals("Incorrect type of the " + fieldName + " field", SecondLevelEntity.class, field.getType());

        String methodName = "propertyOfSelfType.propertyOfSelfType.propertyOfSelfType.methodSecondLevel()";
        try {
            Finder.findFieldByName(SecondLevelEntity.class, methodName);
            fail("Field should not be found for method [" + methodName + "] definition.");
        } catch (final Finder.MethodFoundException e) {
            // valid case
        } catch (final Exception e) {
            fail("Method [%s] should be found.".formatted(methodName));
        }

        methodName = "propertyOfSelfType.propertyOfSelfType.methodFirstLevel()";
        try {
            Finder.findFieldByName(SecondLevelEntity.class, methodName);
            fail("Field should not be found for method [%s] definition.".formatted(methodName));
        } catch (final Finder.MethodFoundException e) {
            // valid case
        } catch (final Exception e) {
            fail("Method [%s] should be found.".formatted(methodName));
        }

        methodName = "propertyOfSelfType.propertyOfSelfType.methodFirstLevel1()";
        try {
            Finder.findFieldByName(SecondLevelEntity.class, methodName);
            fail("Field should not be found for method [" + methodName + "] definition.");
        } catch (final Finder.MethodFoundException e) {
            fail("Method [%s] should be found.".formatted(methodName));
        } catch (final ReflectionException e) {
            // valid case
        }
    }

    @Test
    public void findFieldByNameWithOwningType_can_process_paths_with_method_calls() {
        final String fieldName = "propertyOfSelfType.getPropertyOfSelfType().propertyOfSelfType";
        final T2<Class<?>, Field> typeAndField = Finder.findFieldByNameWithOwningType(SecondLevelEntity.class, fieldName);
        assertNotNull("Faild to locate %s field in the SecondLevelEntity class".formatted(fieldName), typeAndField);
        assertEquals("Incorrect owning type.", SecondLevelEntity.class, typeAndField._1);
        assertEquals("Incorrect field type.", SecondLevelEntity.class, typeAndField._2.getType());

        String methodName = "propertyOfSelfType.propertyOfSelfType.propertyOfSelfType.methodSecondLevel()";
        try {
            Finder.findFieldByNameWithOwningType(SecondLevelEntity.class, methodName);
            fail("Field should not be found for method [%s] definition.".formatted(methodName));
        } catch (final Finder.MethodFoundException e) {
            // valid case
        } catch (final Exception e) {
            fail("Method [%s] should be found.".formatted(methodName));
        }

        methodName = "propertyOfSelfType.propertyOfSelfType.methodFirstLevel()";
        try {
            Finder.findFieldByNameWithOwningType(SecondLevelEntity.class, methodName);
            fail("Field should not be found for inherited method [" + methodName + "] definition.");
        } catch (final Finder.MethodFoundException e) {
            // valid case
        } catch (final Exception e) {
            fail("Method [%s] should be found.".formatted(methodName));
        }

        methodName = "propertyOfSelfType.propertyOfSelfType.methodFirstLevel1()";
        try {
            Finder.findFieldByNameWithOwningType(SecondLevelEntity.class, methodName);
            fail("Field should not be found for method [" + methodName + "] definition.");
        } catch (final Finder.MethodFoundException e) {
            fail("Method [%s] should be found.".formatted(methodName));
        } catch (final ReflectionException e) {
            // valid case
        }
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
    public void findFieldValueByName_works_for_AbstractUnionEntity() {
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
        assertEquals("Incorrect number of String fields.", 5, Finder.getFieldsOfSpecifiedType(DynamicKeyEntity.class, String.class).stream().peek(f -> System.out.println(f.getName())).toList().size());
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

    @Test
    public void real_properties_for_union_types_only_contain_union_properties() {
        final Set<String> realProperties = Finder.streamRealProperties(UnionEntityForReflector.class).map(Field::getName).collect(Collectors.toSet());
        assertEquals(3, realProperties.size());
        assertTrue(realProperties.contains("simplePartEntity"));
        assertTrue(realProperties.contains("complexPartEntity"));
        assertTrue(realProperties.contains("dynamicKeyPartEntity"));

    }

    @SuppressWarnings("unchecked")
    @Test
    public void properties_for_union_types_are_the_union_of_union_properties_and_their_common_properties() {
        // first let's build a collection of expected properties
        final List<Field> unionProperties = Finder.findRealProperties(UnionEntityForReflector.class);
        final Set<String> commonProperties = Finder.findCommonProperties(unionProperties.stream().map(f -> (Class<? extends AbstractEntity<?>>) f.getType()).collect(toList()));
        final List<String> expectedProperties = new ArrayList<>(commonProperties);
        unionProperties.forEach(f -> expectedProperties.add(f.getName()));
        
        // and now let's get properties of a union type and assert them agains the expected list
        final List<String> properties = Finder.streamProperties(UnionEntityForReflector.class).map(Field::getName).collect(toList());
        assertEquals(expectedProperties.size(), properties.size());
        assertTrue(properties.stream().allMatch(p -> expectedProperties.contains(p) ));
    }

    @Test
    public void commonPropertiesForUnion_identifies_all_common_properties_amongst_union_properties() {
        final Set<String> commonProps = Finder.commonPropertiesForUnion(UnionEntity.class);
        assertEquals(Set.of("desc", "stringProperty", "entityThree", "key"), commonProps);
    }

    @Test
    public void unionProperties_identifies_all_union_properties() {
        final List<Field> unionProperties = Finder.unionProperties(UnionEntity.class);
        assertEquals(Set.of("propertyOne", "propertyTwo"), unionProperties.stream().map(Field::getName).collect(toSet()));
    }

    @Test
    public void properties_and_real_properties_are_the_same_for_product_entities_that_have_both_key_and_desc_as_real_properties() {
        final Set<Field> properties = Finder.streamProperties(SimplePartEntity.class).collect(toSet());
        final Set<Field> realProperties = Finder.streamRealProperties(SimplePartEntity.class).collect(toSet());
        assertEquals(properties, realProperties);
    }

    @Test
    public void real_properties_exclude_property_desc_for_product_entities_without_DescTitle_annotation() {
        final Set<String> realProperties = Finder.streamRealProperties(SimpleWithoutDescEntity.class).map(Field::getName).collect(toSet());
        assertFalse(realProperties.contains(DESC));
    }

    @Test
    public void properties_and_real_properties_differ_for_product_entities_without_DescTitle_annotation() {
        final Set<String> properties = Finder.streamProperties(SimpleWithoutDescEntity.class).map(Field::getName).collect(toSet());
        final Set<String> realProperties = Finder.streamRealProperties(SimpleWithoutDescEntity.class).map(Field::getName).collect(toSet());
        assertEquals(properties.size(), realProperties.size() + 1);
        assertTrue(properties.contains(DESC));
        assertFalse(realProperties.contains(DESC));
    }
    
    @Test
    public void real_properties_exclude_property_key_for_composite_product_entities() {
        final Set<String> realProperties = Finder.streamRealProperties(DynamicKeyEntity.class).map(Field::getName).collect(toSet());
        assertFalse(realProperties.contains(KEY));
    }

    @Test
    public void properties_and_real_properties_differ_for_composite_product_entities() {
        final Set<String> properties = Finder.streamProperties(DynamicKeyEntity.class).map(Field::getName).collect(toSet());
        final Set<String> realProperties = Finder.streamRealProperties(DynamicKeyEntity.class).map(Field::getName).collect(toSet());
        assertEquals(properties.size(), realProperties.size() + 1);
        assertTrue(properties.contains(KEY));
        assertFalse(realProperties.contains(KEY));
    }

    @Test
    public void streamUnionMembersWithSubProperty_returns_all_union_members_that_have_the_subProperty() {
        assertThat(streamUnionMembersWithSubProperty(UnionEntity.class, "entityThree").toList())
                .containsExactlyInAnyOrder("propertyOne", "propertyTwo");
        assertThat(streamUnionMembersWithSubProperty(UnionEntity.class, "unrelatedProperty").toList())
                .isEmpty();
        assertThat(streamUnionMembersWithSubProperty(UnionEntity.class, "integerProperty").toList())
                .containsExactlyInAnyOrder("propertyTwo");
        assertThat(streamUnionMembersWithSubProperty(UnionEntity.class, KEY).toList())
                .containsExactlyInAnyOrder("propertyOne", "propertyTwo");
        assertThat(streamUnionMembersWithSubProperty(UnionEntity.class, ID).toList())
                .containsExactlyInAnyOrder("propertyOne", "propertyTwo");
        assertThat(streamUnionMembersWithSubProperty(UnionEntity.class, DESC).toList())
                .containsExactlyInAnyOrder("propertyOne", "propertyTwo");
    }

    @Test
    public void streamUnionSubProperties_returns_all_paths_to_the_subProperty() {
        assertThat(streamUnionSubProperties(UnionEntity.class, "entityThree").toList())
                .containsExactlyInAnyOrder("propertyOne.entityThree", "propertyTwo.entityThree");
        assertThat(streamUnionSubProperties(UnionEntity.class, "unrelatedProperty").toList())
                .isEmpty();
        assertThat(streamUnionSubProperties(UnionEntity.class, "integerProperty").toList())
                .containsExactlyInAnyOrder("propertyTwo.integerProperty");
        assertThat(streamUnionSubProperties(UnionEntity.class, KEY).toList())
                .containsExactlyInAnyOrder("propertyOne.key", "propertyTwo.key");
        assertThat(streamUnionSubProperties(UnionEntity.class, ID).toList())
                .containsExactlyInAnyOrder("propertyOne.id", "propertyTwo.id");
        assertThat(streamUnionSubProperties(UnionEntity.class, DESC).toList())
                .containsExactlyInAnyOrder("propertyOne.desc", "propertyTwo.desc");
    }

}
