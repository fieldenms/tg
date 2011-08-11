package ua.com.fielden.platform.reflection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import org.junit.Test;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.Collectional;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.union.UnionEntity;
import ua.com.fielden.platform.reflection.test_entities.FirstLevelEntity;
import ua.com.fielden.platform.reflection.test_entities.SecondLevelEntity;
import ua.com.fielden.platform.reflection.test_entities.SimpleEntity;

/**
 * Test case for {@link AnnotationReflector}.
 *
 * @author TG Team
 *
 */
public class AnnotationReflectorTest {
    @Test
    public void testGetValidationAnnotations() throws Exception {
	final Method mutatorForProperty = Reflector.getMethod(SimpleEntity.class, "setProperty", String.class);
	assertNotNull("Could not find mutator for property 'property'", mutatorForProperty);
	assertEquals("Incorrect number for validation annotations.", 2, AnnotationReflector.getValidationAnnotations(mutatorForProperty).size());

	final Method mutatorForPropertyTwo = Reflector.getMethod(SimpleEntity.class, "setPropertyTwo", String.class);
	assertNotNull("Could not find mutator for property 'propertyTwo'", mutatorForProperty);
	assertEquals("Incorrect number for validation annotations.", 0, AnnotationReflector.getValidationAnnotations(mutatorForPropertyTwo).size());
    }

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
	assertTrue("KeyTitle annotation should have been determined for SecondLevelEntity.", AnnotationReflector.isAnnotationPresent(KeyTitle.class, SecondLevelEntity.class));
	assertEquals("Incorrect title for key property in SecondLevelEntity.", "Leveled Entity No", AnnotationReflector.getAnnotation(KeyTitle.class, SecondLevelEntity.class).value());
    }

    @Test
    public void testHierarchicalPropertyAnnotationDetermination() {
	// Property "propertyTwo" in FirstLevelEntity is annotated with @CritOnly; SecondLevelEntity is not, but it is derived from FirstLevelEntity
	// Expect CritOnly from FirstLevelEntity to be picked up for SecondLevel
	assertTrue("CritOnly annotation should have been determined for SecondLevelEntity.", AnnotationReflector.isAnnotationPresentInHierarchy(CritOnly.class, SecondLevelEntity.class, "propertyTwo"));
	assertTrue("CritOnly annotation should have been determined for FirstLevelEntity.", AnnotationReflector.isAnnotationPresentInHierarchy(CritOnly.class, FirstLevelEntity.class, "propertyTwo"));
	assertTrue("CritOnly annotation should have been determined for SecondLevelEntity.", AnnotationReflector.isAnnotationPresentInHierarchy(CritOnly.class, SecondLevelEntity.class, "critOnlyAEProperty"));
	assertTrue("CritOnly annotation should have been determined for FirstLevelEntity.", AnnotationReflector.isAnnotationPresentInHierarchy(CritOnly.class, FirstLevelEntity.class, "critOnlyAEProperty"));
	assertTrue("CritOnly annotation should have been determined for SecondLevelEntity.", AnnotationReflector.isAnnotationPresentInHierarchy(CritOnly.class, SecondLevelEntity.class, "critOnlyAEProperty.property"));
	assertTrue("CritOnly annotation should have been determined for FirstLevelEntity.", AnnotationReflector.isAnnotationPresentInHierarchy(CritOnly.class, FirstLevelEntity.class, "critOnlyAEProperty.property"));
	assertTrue("CritOnly annotation should have been determined for SecondLevelEntity.", AnnotationReflector.isAnnotationPresentInHierarchy(CritOnly.class, SecondLevelEntity.class, "critOnlyAEProperty.propertyTwo"));
	assertTrue("CritOnly annotation should have been determined for FirstLevelEntity.", AnnotationReflector.isAnnotationPresentInHierarchy(CritOnly.class, FirstLevelEntity.class, "critOnlyAEProperty.propertyTwo"));
    }

    @Test
    public void testPropertyAnnotationRetrieval() {
	// ordinary property tests
	assertNotNull("Property annotation should have been determined", AnnotationReflector.getPropertyAnnotation(Title.class, FirstLevelEntity.class, "property"));
	assertNotNull("Property annotation should have been determined", AnnotationReflector.getPropertyAnnotation(Title.class, SecondLevelEntity.class, "property"));
	assertEquals("Incorrect property annotation value", "Property", AnnotationReflector.getPropertyAnnotation(Title.class, SecondLevelEntity.class, "property").value());
	// key property
	assertNotNull("Property annotation should have been determined", AnnotationReflector.getPropertyAnnotation(KeyTitle.class, SecondLevelEntity.class, "key"));
	assertNotNull("Property annotation should have been determined", AnnotationReflector.getPropertyAnnotation(KeyType.class, SecondLevelEntity.class, "key"));
	assertNotNull("Property annotation should have been determined", AnnotationReflector.getPropertyAnnotation(IsProperty.class, SecondLevelEntity.class, "key"));
	assertEquals("Incorrect property annotation value", "Leveled Entity No", AnnotationReflector.getPropertyAnnotation(KeyTitle.class, SecondLevelEntity.class, "key").value());
	// dot-notated property
	// ordinary property tests
	assertNotNull("Property annotation should have been determined", AnnotationReflector.getPropertyAnnotation(Title.class, SecondLevelEntity.class, "propertyOfSelfType.property"));
	assertEquals("Incorrect property annotation value", "Property", AnnotationReflector.getPropertyAnnotation(Title.class, SecondLevelEntity.class, "propertyOfSelfType.property").value());
    }

    @Test
    public void test_isClassHasMethodAnnotatedWith() {
	assertTrue("", AnnotationReflector.isClassHasMethodAnnotatedWith(UnionEntity.class, Observable.class));
	// 3 -- for common properties, 2 -- for union entity, 2 -- AbstractUnionEntity, 2 -- AbstractEntity
	assertEquals("", 8, AnnotationReflector.getMethodsAnnotatedWith(UnionEntity.class, Observable.class).size());
	assertTrue("", AnnotationReflector.isClassHasMethodAnnotatedWith(SecondLevelEntity.class, Observable.class));
	// 4 -- for SecondLevelEntity, 2 -- for FirstLevelEntity 2 - for AbstractEntity
	assertEquals("", 9, AnnotationReflector.getMethodsAnnotatedWith(SecondLevelEntity.class, Observable.class).size());
    }

    @Test
    public void test_getCollectionalPropertyTypes_method() {
	assertEquals("The specified collectional property type should be also actually collectional.", Arrays.asList(Entity1.class), AnnotationReflector.getCollectionalPropertyTypes(Entity2.class));
	assertEquals("The actually collectional but not explicitly specified property type should not be retrieved as collectional.", Collections.emptyList(), AnnotationReflector.getCollectionalPropertyTypes(Entity3.class));

	try {
	    AnnotationReflector.getCollectionalPropertyTypes(Entity5.class);
	    fail("The type [" + Entity4.class.getSimpleName() + "] has non-composite key, so it could not be used as a collectional property for type [" + Entity5.class.getSimpleName() + "].");
	} catch (final RuntimeException e) {
	    System.out.println("All is ok.");
	}

	try {
	    AnnotationReflector.getCollectionalPropertyTypes(Entity7.class);
	    fail("The type [" + Entity6.class.getSimpleName() + "] has duplicate composite key member of type [" + Entity7.class.getSimpleName() + "].");
	} catch (final RuntimeException e) {
	    System.out.println("All is ok.");
	}

	try {
	    AnnotationReflector.getCollectionalPropertyTypes(Entity9.class);
	    fail("The type [" + Entity8.class.getSimpleName() + "] has no composite key member of type [" + Entity9.class.getSimpleName() + "].");
	} catch (final RuntimeException e) {
	    System.out.println("All is ok.");
	}
    }

    @KeyType(DynamicEntityKey.class)
    private class Entity1 extends AbstractEntity<DynamicEntityKey> {
	@IsProperty
	@CompositeKeyMember(1)
	private Entity2 entity2;
	@IsProperty
	@CompositeKeyMember(2)
	private Entity3 entity3;
	@IsProperty
	@CompositeKeyMember(3)
	private Date otherCompositeKeyMember;
    }

    @Collectional(Entity1.class)
    @KeyType(String.class)
    private class Entity2 extends AbstractEntity<String> {
    }

    @KeyType(String.class)
    private class Entity3 extends AbstractEntity<String> {
    }

    // errors testing entities:

    @KeyType(Entity5.class)
    private class Entity4 extends AbstractEntity<Entity5> {
    }

    @Collectional(Entity4.class)
    @KeyType(String.class)
    private class Entity5 extends AbstractEntity<String> {
    }
    ///////

    @KeyType(DynamicEntityKey.class)
    private class Entity6 extends AbstractEntity<DynamicEntityKey> {
	@IsProperty
	@CompositeKeyMember(1)
	private Entity7 entity7_1;
	@IsProperty
	@CompositeKeyMember(2)
	private Entity7 entity7_2;
    }

    @Collectional(Entity6.class)
    @KeyType(String.class)
    private class Entity7 extends AbstractEntity<String> {
    }

   ///////
    @KeyType(DynamicEntityKey.class)
    private class Entity8 extends AbstractEntity<DynamicEntityKey> {
	@IsProperty
	@CompositeKeyMember(1)
	private String str;
	@IsProperty
	@CompositeKeyMember(2)
	private Date date;
    }

    @Collectional(Entity8.class)
    @KeyType(String.class)
    private class Entity9 extends AbstractEntity<String> {
    }


}
