package ua.com.fielden.platform.reflection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.reflection.AnnotationReflector.*;
import static ua.com.fielden.platform.test_utils.TestUtils.assertOptEquals;

import java.util.Optional;

import org.junit.Test;

import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.entity.annotation.mutator.AfterChange;
import ua.com.fielden.platform.reflection.test_entities.FirstLevelEntity;
import ua.com.fielden.platform.reflection.test_entities.SecondLevelEntity;
import ua.com.fielden.platform.reflection.test_entities.SimpleEntity;
import ua.com.fielden.platform.sample.domain.UnionEntity;
import ua.com.fielden.platform.test_utils.TestUtils;

/**
 * Test case for {@link AnnotationReflector}.
 *
 * @author TG Team
 *
 */
public class AnnotationReflectorTest {

    @Test
    public void testGetKeyType() throws Exception {
        assertEquals("Incorrect ket type for SimpleEntity.", String.class, AnnotationReflector.getKeyType(SimpleEntity.class));
        assertEquals("Incorrect ket type for FirstLevelEntity.", DynamicEntityKey.class, AnnotationReflector.getKeyType(FirstLevelEntity.class));
        assertEquals("Incorrect ket type for SecondLevelEntity.", DynamicEntityKey.class, AnnotationReflector.getKeyType(SecondLevelEntity.class));
    }

    @Test
    public void testHierarchicalAnnotationDetermination() {
        // Class FirstLevelEntity is annotated with KeyTitle; SecondLevelEntity is not, but it is derived from First Level
        // Expect KeyTitle from FirstLevelEntity to be picked up for SecondLevel
        assertTrue("KeyTitle annotation should have been determined for SecondLevelEntity.", AnnotationReflector.isAnnotationPresentForClass(KeyTitle.class, SecondLevelEntity.class));
        assertEquals("Incorrect title for key property in SecondLevelEntity.", "Leveled Entity No", AnnotationReflector.getAnnotation(SecondLevelEntity.class, KeyTitle.class).value());
    }

    @Test
    public void isAnnotationPresentInHierarchy_is_true_for_properties_with_annotations_at_different_levels_of_type_hierarchy() {
        assertTrue(isAnnotationPresentInHierarchy(CritOnly.class, SecondLevelEntity.class, "propertyTwo"));
        assertTrue(isAnnotationPresentInHierarchy(CritOnly.class, FirstLevelEntity.class, "propertyTwo"));
        assertTrue(isAnnotationPresentInHierarchy(CritOnly.class, SecondLevelEntity.class, "critOnlyAEProperty"));
        assertTrue(isAnnotationPresentInHierarchy(CritOnly.class, FirstLevelEntity.class, "critOnlyAEProperty"));
    }

    @Test
    public void isAnnotationPresentInHierarchy_is_true_for_dotnotated_properties_with_annotations_at_different_levels_of_type_hierarchy() {
        assertTrue(isAnnotationPresentInHierarchy(CritOnly.class, SecondLevelEntity.class, "critOnlyAEProperty.property"));
        assertTrue(isAnnotationPresentInHierarchy(CritOnly.class, FirstLevelEntity.class, "critOnlyAEProperty.property"));
        assertTrue(isAnnotationPresentInHierarchy(CritOnly.class, SecondLevelEntity.class, "critOnlyAEProperty.propertyTwo"));
        assertTrue(isAnnotationPresentInHierarchy(CritOnly.class, FirstLevelEntity.class, "critOnlyAEProperty.propertyTwo"));
    }

    @Test
    public void getPropertyAnnotationInHierarchy_returns_correct_annotation_for_properties_with_annotations_at_different_levels_of_type_hierarchy() {
        assertEquals(CritOnly.class, getPropertyAnnotationInHierarchy(CritOnly.class, SecondLevelEntity.class, "propertyTwo").get().annotationType());
        assertEquals(CritOnly.class, getPropertyAnnotationInHierarchy(CritOnly.class, FirstLevelEntity.class, "propertyTwo").get().annotationType());
        assertEquals(CritOnly.class, getPropertyAnnotationInHierarchy(CritOnly.class, SecondLevelEntity.class, "critOnlyAEProperty").get().annotationType());
        assertEquals(CritOnly.class, getPropertyAnnotationInHierarchy(CritOnly.class, FirstLevelEntity.class, "critOnlyAEProperty").get().annotationType());
    }

    @Test
    public void getPropertyAnnotationInHierarchy_returns_empty_result_for_properties_without_annotations_at_different_levels_of_type_hierarchy() {
        assertFalse(getPropertyAnnotationInHierarchy(AfterChange.class, SecondLevelEntity.class, "propertyTwo").isPresent());
        assertFalse(getPropertyAnnotationInHierarchy(AfterChange.class, FirstLevelEntity.class, "propertyTwo").isPresent());
        assertFalse(getPropertyAnnotationInHierarchy(AfterChange.class, SecondLevelEntity.class, "critOnlyAEProperty").isPresent());
        assertFalse(getPropertyAnnotationInHierarchy(AfterChange.class, FirstLevelEntity.class, "critOnlyAEProperty").isPresent());
    }

    @Test
    public void getPropertyAnnotationInHierarchy_returns_correct_annotation_for_dotnotated_properties_with_annotations_at_different_levels_of_type_hierarchy() {
        assertEquals(CritOnly.class, getPropertyAnnotationInHierarchy(CritOnly.class, SecondLevelEntity.class, "critOnlyAEProperty.property").get().annotationType());
        assertEquals(CritOnly.class, getPropertyAnnotationInHierarchy(CritOnly.class, FirstLevelEntity.class, "critOnlyAEProperty.property").get().annotationType());
        assertEquals(CritOnly.class, getPropertyAnnotationInHierarchy(CritOnly.class, SecondLevelEntity.class, "critOnlyAEProperty.propertyTwo").get().annotationType());
        assertEquals(CritOnly.class, getPropertyAnnotationInHierarchy(CritOnly.class, FirstLevelEntity.class, "critOnlyAEProperty.propertyTwo").get().annotationType());
    }

    @Test
    public void getPropertyAnnotationInHierarchy_returns_empty_result_for_dotnotated_properties_without_annotations_at_different_levels_of_type_hierarchy() {
        assertFalse(getPropertyAnnotationInHierarchy(AfterChange.class, SecondLevelEntity.class, "critOnlyAEProperty.property").isPresent());
        assertFalse(getPropertyAnnotationInHierarchy(AfterChange.class, FirstLevelEntity.class, "critOnlyAEProperty.property").isPresent());
        assertFalse(getPropertyAnnotationInHierarchy(AfterChange.class, SecondLevelEntity.class, "critOnlyAEProperty.propertyTwo").isPresent());
        assertFalse(getPropertyAnnotationInHierarchy(AfterChange.class, FirstLevelEntity.class, "critOnlyAEProperty.propertyTwo").isPresent());
    }

    @Test
    public void testPropertyAnnotationRetrieval() {
        // ordinary property
        assertOptEquals("Property", getPropertyAnnotationOptionally(Title.class, FirstLevelEntity.class, "property").map(Title::value));
        assertOptEquals("Property", getPropertyAnnotationOptionally(Title.class, SecondLevelEntity.class, "property").map(Title::value));
        // key property
        assertOptEquals("Leveled Entity No", getPropertyAnnotationOptionally(KeyTitle.class, SecondLevelEntity.class, "key").map(KeyTitle::value));
        assertOptEquals(DynamicEntityKey.class, getPropertyAnnotationOptionally(KeyType.class, SecondLevelEntity.class, "key").map(KeyType::value));
        assertOptEquals("Leveled Entity No", getPropertyAnnotationOptionally(KeyTitle.class, SecondLevelEntity.class, "key").map(KeyTitle::value));
        // desc property
        assertOptEquals("Description", getPropertyAnnotationOptionally(DescTitle.class, FirstLevelEntity.class, "desc").map(DescTitle::value));
        assertOptEquals("Description", getPropertyAnnotationOptionally(DescTitle.class, SecondLevelEntity.class, "desc").map(DescTitle::value));

        // dot-notated ordinary property
        assertOptEquals("Property", getPropertyAnnotationOptionally(Title.class, SecondLevelEntity.class, "propertyOfSelfType.property").map(Title::value));
        // dot-notated key property
        assertOptEquals("Leveled Entity No", getPropertyAnnotationOptionally(KeyTitle.class, SecondLevelEntity.class, "propertyOfSelfType.key").map(KeyTitle::value));
        assertOptEquals(DynamicEntityKey.class, getPropertyAnnotationOptionally(KeyType.class, SecondLevelEntity.class, "propertyOfSelfType.key").map(KeyType::value));
        assertOptEquals("Leveled Entity No", getPropertyAnnotationOptionally(KeyTitle.class, SecondLevelEntity.class, "propertyOfSelfType.key").map(KeyTitle::value));
        // dot-notated desc property
        assertOptEquals("Description", getPropertyAnnotationOptionally(DescTitle.class, SecondLevelEntity.class, "propertyOfSelfType.desc").map(DescTitle::value));
    }

    @Test
    public void retrieval_of_annotations_for_non_existing_properties_succeeds_with_null_result() {
        assertNull(getPropertyAnnotation(Title.class, FirstLevelEntity.class, "property_that_does_not_exist"));
        assertNull(getPropertyAnnotation(Title.class, SecondLevelEntity.class, "propertyOfSelfType.sub_property_that_does_not_exist"));
        assertNull(getPropertyAnnotation(Title.class, SecondLevelEntity.class, "property_that_does_not_exist.sub_property_that_does_not_exist"));
    }

    @Test
    public void test_isClassHasMethodAnnotatedWith() {
        assertTrue("", AnnotationReflector.isClassHasMethodAnnotatedWith(UnionEntity.class, Observable.class));
        // 3 -- for common properties, 2 -- for union entity, 2 -- AbstractUnionEntity, 2 -- AbstractEntity
        assertEquals("", 8, AnnotationReflector.getMethodsAnnotatedWith(UnionEntity.class, Optional.of(Observable.class)).size());
        assertTrue("", AnnotationReflector.isClassHasMethodAnnotatedWith(SecondLevelEntity.class, Observable.class));
        // 4 -- for SecondLevelEntity, 2 -- for FirstLevelEntity 2 - for AbstractEntity
        assertEquals("", 9, AnnotationReflector.getMethodsAnnotatedWith(SecondLevelEntity.class, Optional.of(Observable.class)).size());
    }
}
