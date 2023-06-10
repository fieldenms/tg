package ua.com.fielden.platform.entity.union;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.meta.MetaProperty.ERR_REQUIRED;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getTitleAndDesc;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.google.inject.Injector;

import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.sample.domain.EntityOne;
import ua.com.fielden.platform.sample.domain.EntityTwo;
import ua.com.fielden.platform.sample.domain.UnionEntity;
import ua.com.fielden.platform.test.CommonTestEntityModuleWithPropertyFactory;

/**
 * A test case covering union rules and definition of {@link AbstractUnionEntity} descendants.
 *
 * @author TG Team
 *
 */
public class AbstractUnionEntityTest {
    final Injector injector = new ApplicationInjectorFactory().add(new CommonTestEntityModuleWithPropertyFactory()).getInjector();
    final EntityFactory factory = injector.getInstance(EntityFactory.class);

    @Test
    public void test_definition_validation_for_correct_union_entity() {
        try {
            factory.newEntity(UnionEntity.class);
        } catch (final Exception e) {
            fail("Creation should have succeeded");
        }
    }

    @Test
    public void test_definition_validation_for_union_entity_with_kind_one_error() {
        try {
            factory.newEntity(UnionEntityWithKindOneError.class);
            fail("Creation should have been prevented");
        } catch (final Exception e) {
        }
    }

    @Test
    public void test_definition_validation_for_union_entity_with_kind_two_error() {
        try {
            factory.newEntity(UnionEntityWithKindTwoError.class);
            fail("Creation should have been prevented");
        } catch (final Exception e) {
        }
    }

    @Test
    public void union_entity_without_active_property_is_not_valid() {
        final var unionEntity = factory.newEntity(UnionEntity.class);
        final var isValidResult = unionEntity.isValid();
        assertFalse(isValidResult.isSuccessful());
        final var expectedError = format(ERR_REQUIRED, getTitleAndDesc(KEY, UnionEntity.class).getKey(), getEntityTitleAndDesc(UnionEntity.class).getKey());
        assertEquals(expectedError, isValidResult.getMessage());
    }

    @Test
    public void union_entity_with_active_property_is_valid() {
        final var unionEntity = factory.newEntity(UnionEntity.class)
            .setPropertyOne(factory.newEntity(EntityOne.class, 1L, "KEY VALUE"));
        assertTrue(unionEntity.isValid().isSuccessful());
    }

    @Test
    public void test_retrieval_of_common_properties() {
        final Set<String> list = AbstractUnionEntity.commonProperties(UnionEntity.class);
        assertEquals("Incorrect number of common properties.", 2, list.size());
    }

    @Test
    public void test_that_unionProperties_works_correctly() {
        final List<Field> unionPropertiesList = AbstractUnionEntity.unionProperties(UnionEntity.class);
        assertEquals("Incorrect number of union properties", 2, unionPropertiesList.size());
        assertTrue("propertyOne", Finder.getFieldNames(unionPropertiesList).contains("propertyOne"));
        assertTrue("propertyTwo", Finder.getFieldNames(unionPropertiesList).contains("propertyTwo"));
    }

    @Test
    public void test_that_commonMethods_works_correctly() {
        List<String> list = null;
        try {
            list = AbstractUnionEntity.commonMethodNames(UnionEntity.class);
        } catch (final Exception e) {
            fail("There shouldn't be any exception");
        }
        assertEquals("Incorrect number of common methods", 4, list.size());
        assertTrue("List of common methods must contain the getPropertyOne method name", list.contains("getStringProperty"));
        assertTrue("List of common methods must contain the getDesc method name", list.contains("getDesc"));

        assertFalse("List of common methods must contain the getKey method name", list.contains("getKey"));
        assertFalse("List of common methods must contain the setKey method name", list.contains("setKey"));
        assertTrue("List of common methods must contain the setPropertyOne method name", list.contains("setStringProperty"));
        assertTrue("List of common methods must contain the setDesc method name", list.contains("setDesc"));

    }

    @Test
    public void test_correct_key_assignment_for_new_union_entity() {
        try {
            factory.newEntity(UnionEntity.class);
        } catch (final Exception e) {
            fail("Creation should have been successful");
        }
    }

    @Test
    public void test_union_rule_id_key_desc_cannot_be_accessed_before_active_entity_is_speficied() {
        final UnionEntity unionEntity = factory.newEntity(UnionEntity.class);

        try {
            unionEntity.getId();
            fail("Should not be able to access id before active property is specified");
        } catch (final Exception e) {
        }

        try {
            unionEntity.getKey();
            fail("Should not be able to access key before active property is specified");
        } catch (final Exception e) {
        }

        try {
            unionEntity.getDesc();
            fail("Should not be able to access desc before active property is specified");
        } catch (final Exception e) {
        }
    }

    @Test
    public void test_union_rule_active_property_can_be_assigned_only_once() {
        final UnionEntity unionEntity = factory.newEntity(UnionEntity.class);
        unionEntity.setPropertyOne(factory.newEntity(EntityOne.class, 1L, "KEY VALUE"));
        try {
            unionEntity.setPropertyOne(null);
            fail("Should not be able to set active property more than once.");
        } catch (final Exception e) {
        }
        try {
            unionEntity.setPropertyTwo(factory.newEntity(EntityTwo.class, 1L, 12));
            fail("Should not be able to set active property more than once.");
        } catch (final Exception e) {
        }
    }

    @Test
    public void test_union_rule_active_property_can_be_assigned_only_once_making_core_property_accessible_when_value_is_not_null() {
        final UnionEntity unionEntity = factory.newEntity(UnionEntity.class);

        final EntityOne one = factory.newEntity(EntityOne.class, 1L, "KEY VALUE");
        one.setDesc("DESC");
        unionEntity.setPropertyOne(one);

        assertEquals("Incorrect active property name", "propertyOne", unionEntity.activePropertyName());
        assertNotNull("Active property was not set.", unionEntity.activeEntity());
        assertEquals("Incorrect id.", one.getId(), unionEntity.getId());
        assertEquals("Incorrect key.", one.getKey(), unionEntity.getKey());
        assertEquals("Incorrect desc.", one.getDesc(), unionEntity.getDesc());

        one.setDesc("NEW DESC");
        assertEquals("Incorrect desc.", one.getDesc(), unionEntity.getDesc());
    }

    @Test
    public void union_entities_with_same_key_representations_but_different_active_properties_arent_equal() {
        final UnionEntity unionEntity1 = factory.newEntity(UnionEntity.class).setPropertyOne(factory.newEntity(EntityOne.class, 1L, "1"));
        final UnionEntity unionEntity2 = factory.newEntity(UnionEntity.class).setPropertyTwo(factory.newEntity(EntityTwo.class, 2L, 1));
        assertNotEquals(unionEntity1, unionEntity2);
    }

    /**
     * Remark: "the same IDs" situation should not be possible due to contiguous nature of ID values across all entities. 
     */
    @Test
    public void union_entities_with_same_key_representations_and_same_ids_but_different_active_properties_arent_equal() {
        final UnionEntity unionEntity1 = factory.newEntity(UnionEntity.class).setPropertyOne(factory.newEntity(EntityOne.class, 1L, "1"));
        final UnionEntity unionEntity2 = factory.newEntity(UnionEntity.class).setPropertyTwo(factory.newEntity(EntityTwo.class, 1L, 1));
        assertNotEquals(unionEntity1, unionEntity2);
    }

    /**
     * Remark: "the same IDs" situation should not be possible due to contiguous nature of ID values across all entities. 
     */
    @Test
    public void union_entities_with_different_key_representations_and_same_ids_and_same_active_property_arent_equal() {
        final var entity1 = factory.newEntity(EntityOne.class, 1L, "1");
        final var entity2 = factory.newEntity(EntityOne.class, 1L, "2");
        assertNotEquals(entity1, entity2);
        final UnionEntity unionEntity1 = factory.newEntity(UnionEntity.class).setPropertyOne(entity1);
        final UnionEntity unionEntity2 = factory.newEntity(UnionEntity.class).setPropertyOne(entity2);
        assertNotEquals(unionEntity1, unionEntity2);
    }

}