package ua.com.fielden.platform.processors.metamodel.utils;

import static java.util.stream.Collectors.joining;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.google.testing.compile.CompilationRule;

import ua.com.fielden.platform.annotations.metamodel.DomainEntity;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.processors.metamodel.elements.EntityElement;
import ua.com.fielden.platform.processors.metamodel.elements.PropertyElement;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserSecret;
import ua.com.fielden.platform.utils.Pair;

/**
 * A test case for utility functions in {@link EntityFinder}.
 *
 * @author TG Team
 *
 */
public class EntityFinderTest {

    public @Rule CompilationRule rule = new CompilationRule();
    private Elements elements;
    private Types types;
    
    @Before
    public void setup() {
      elements = rule.getElements();
      types = rule.getTypes();
    }

    @Test
    public void isEntityType_type_element_for_AbstractEntity_is_recognised_as_an_element_for_an_entity_type() {
      final TypeElement typeElement = elements.getTypeElement(AbstractEntity.class.getCanonicalName());
      assertTrue(EntityFinder.isEntityType(typeElement));
    }

    @Test
    public void isEntityType_type_element_for_User_is_recognised_as_an_element_for_an_entity_type() {
      final TypeElement typeElement = elements.getTypeElement(User.class.getCanonicalName());
      assertTrue(EntityFinder.isEntityType(typeElement));
    }

    @Test
    public void isEntityType_type_element_for_String_is_not_recognised_as_an_element_for_an_entity_type() {
      final TypeElement typeElement = elements.getTypeElement(String.class.getCanonicalName());
      assertFalse(EntityFinder.isEntityType(typeElement));
    }

    @Test
    public void isPersistentEntityType_type_element_for_User_is_recognised_as_an_element_for_a_persistent_entity_type() {
      final TypeElement typeElement = elements.getTypeElement(User.class.getCanonicalName());
      final EntityElement entityElement = EntityElement.wrapperFor(typeElement);
      assertTrue(EntityFinder.isPersistentEntityType(entityElement));
    }

    @Test
    public void isPersistentEntityType_type_element_for_AbstractEntity_is_not_recognised_as_an_element_for_a_persistent_entity_type() {
      final TypeElement typeElement = elements.getTypeElement(AbstractEntity.class.getCanonicalName());
      final EntityElement entityElement = EntityElement.wrapperFor(typeElement);
      assertFalse(EntityFinder.isPersistentEntityType(entityElement));
    }

    @Test
    public void doesExtendPersistentEntity_type_element_for_AbstractEntity_is_not_recognised_as_an_element_that_extends_a_persistent_entity_type() {
      final TypeElement typeElement = elements.getTypeElement(AbstractEntity.class.getCanonicalName());
      final EntityElement entityElement = EntityElement.wrapperFor(typeElement);
      assertFalse(EntityFinder.doesExtendPersistentEntity(entityElement));
    }

    @Test
    public void doesExtendPersistentEntity_type_element_for_User_is_not_recognised_as_an_element_that_extends_a_persistent_entity_type() {
      final TypeElement typeElement = elements.getTypeElement(User.class.getCanonicalName());
      final EntityElement entityElement = EntityElement.wrapperFor(typeElement);
      assertFalse(EntityFinder.doesExtendPersistentEntity(entityElement));
    }

    @Test
    public void doesExtendPersistentEntity_type_element_for_SuperUser_is_recognised_as_an_element_that_extends_a_persistent_entity_type() {
      final TypeElement typeElement = elements.getTypeElement(SuperUser.class.getCanonicalName());
      final EntityElement entityElement = EntityElement.wrapperFor(typeElement);
      assertTrue(EntityFinder.doesExtendPersistentEntity(entityElement));
    }

    @Test
    public void findDeclaredProperties_finds_only_declared_properties_in_User() {
      final TypeElement typeElement = elements.getTypeElement(User.class.getCanonicalName());
      final EntityElement entityElement = EntityElement.wrapperFor(typeElement);
      final Set<PropertyElement> props = EntityFinder.findDeclaredProperties(entityElement);
      assertEquals(7, props.size());
      final String expectedProps = "key, roles, base, basedOnUser, email, active, ssoOnly";
      assertEquals(expectedProps, props.stream().map(p -> p.getName()).collect(joining(", "))); 
    }

    @Test
    public void findInheritedProperties_finds_only_inherited_properties_in_User() {
      final TypeElement typeElement = elements.getTypeElement(User.class.getCanonicalName());
      final EntityElement entityElement = EntityElement.wrapperFor(typeElement);
      final Set<PropertyElement> props = EntityFinder.findInheritedProperties(entityElement);
      assertEquals(10, props.size());
      // it is expected that some of the re-declared properties in User would appear as both declared and inherited
      // more specifically, properties "active" and "key" appear as both declared and inherited
      // property "desc" is missing as it was @DescTitle was not declared for either User or its supertypes
      final String expectedProps = "active, refCount, createdBy, createdDate, createdTransactionGuid, lastUpdatedBy, lastUpdatedDate, lastUpdatedTransactionGuid, key, id";
      assertEquals(expectedProps, props.stream().map(p -> p.getName()).collect(joining(", "))); 
    }

    @Test
    public void findProperties_finds_all_properties_in_User() {
      final TypeElement typeElement = elements.getTypeElement(User.class.getCanonicalName());
      final EntityElement entityElement = EntityElement.wrapperFor(typeElement);
      final Set<PropertyElement> props = EntityFinder.findProperties(entityElement);
      assertEquals(15, props.size());
      // property "desc" is missing as it was @DescTitle was not declared for either User or its supertypes
      final String expectedProps = "key, roles, base, basedOnUser, email, active, ssoOnly, refCount, createdBy, createdDate, createdTransactionGuid, lastUpdatedBy, lastUpdatedDate, lastUpdatedTransactionGuid, id";
      assertEquals(expectedProps, props.stream().map(p -> p.getName()).collect(joining(", "))); 
    }

    @Test
    public void findProperties_finds_all_properties_in_SuperUser() {
      final TypeElement typeElement = elements.getTypeElement(SuperUser.class.getCanonicalName());
      final EntityElement entityElement = EntityElement.wrapperFor(typeElement);
      final Set<PropertyElement> props = EntityFinder.findProperties(entityElement);
      assertEquals(16, props.size());
      // unlike User, SuperUser has property "desc" due to @DescTitle
      final String expectedProps = "key, roles, base, basedOnUser, email, active, ssoOnly, refCount, createdBy, createdDate, createdTransactionGuid, lastUpdatedBy, lastUpdatedDate, lastUpdatedTransactionGuid, desc, id";
      assertEquals(expectedProps, props.stream().map(p -> p.getName()).collect(joining(", "))); 
    }

    @Test
    public void findProperties_finds_all_properties_in_SuperUserWithDeclaredDesc() {
      final TypeElement typeElement = elements.getTypeElement(SuperUserWithDeclaredDesc.class.getCanonicalName());
      final EntityElement entityElement = EntityElement.wrapperFor(typeElement);
      final Set<PropertyElement> props = EntityFinder.findProperties(entityElement);
      assertEquals(16, props.size());
      // unlike User, SuperUserWithDeclaredDesc has property "desc" declared explicitly
      final String expectedProps = "desc, key, roles, base, basedOnUser, email, active, ssoOnly, refCount, createdBy, createdDate, createdTransactionGuid, lastUpdatedBy, lastUpdatedDate, lastUpdatedTransactionGuid, id";
      assertEquals(expectedProps, props.stream().map(p -> p.getName()).collect(joining(", "))); 
    }

    @Test
    public void isPropertyOfEntityType_correctly_distiguishes_between_properties_of_an_entity_type_and_other_types() {
        final TypeElement typeElement = elements.getTypeElement(User.class.getCanonicalName());
        final EntityElement entityElement = EntityElement.wrapperFor(typeElement);
        final Map<String, PropertyElement> props = EntityFinder.findProperties(entityElement).stream().collect(Collectors.toMap(PropertyElement::getName, Function.identity()));
        assertFalse(EntityFinder.isPropertyOfEntityType(props.get("key")));
        assertFalse(EntityFinder.isPropertyOfEntityType(props.get("roles")));
        assertFalse(EntityFinder.isPropertyOfEntityType(props.get("base")));
        assertTrue(EntityFinder.isPropertyOfEntityType(props.get("basedOnUser")));
        assertFalse(EntityFinder.isPropertyOfEntityType(props.get("email")));
        assertFalse(EntityFinder.isPropertyOfEntityType(props.get("active")));
        assertFalse(EntityFinder.isPropertyOfEntityType(props.get("ssoOnly")));
        assertFalse(EntityFinder.isPropertyOfEntityType(props.get("refCount")));
        assertTrue(EntityFinder.isPropertyOfEntityType(props.get("createdBy")));
        assertFalse(EntityFinder.isPropertyOfEntityType(props.get("createdDate")));
        assertFalse(EntityFinder.isPropertyOfEntityType(props.get("createdTransactionGuid")));
        assertTrue(EntityFinder.isPropertyOfEntityType(props.get("lastUpdatedBy")));
        assertFalse(EntityFinder.isPropertyOfEntityType(props.get("lastUpdatedDate")));
        assertFalse(EntityFinder.isPropertyOfEntityType(props.get("lastUpdatedTransactionGuid")));
        assertFalse(EntityFinder.isPropertyOfEntityType(props.get("lastUpdatedDate")));
        assertFalse(EntityFinder.isPropertyOfEntityType(props.get("id")));
    }

    @Test
    public void isPropertyOfEntityType_correctly_identifies_entity_typed_key_as_an_entity_typed_property() {
        final TypeElement typeElement = elements.getTypeElement(UserSecret.class.getCanonicalName());
        final EntityElement entityElement = EntityElement.wrapperFor(typeElement);
        assertTrue(EntityFinder.isPropertyOfEntityType(EntityFinder.findProperties(entityElement).stream().filter(p -> "key".equals(p.getName())).findFirst().get()));
    }

    @Test
    public void findAnnotation_looks_for_annotations_through_type_hierarchy() {
        final EntityElement userElement = EntityElement.wrapperFor(elements.getTypeElement(User.class.getCanonicalName()));
        final EntityElement superUserElement = EntityElement.wrapperFor(elements.getTypeElement(SuperUser.class.getCanonicalName()));
        assertTrue(EntityFinder.findAnnotation(userElement, DescTitle.class).isEmpty());
        assertFalse(EntityFinder.findAnnotation(userElement, MapEntityTo.class).isEmpty());
        assertFalse(EntityFinder.findAnnotation(superUserElement, DescTitle.class).isEmpty());
        assertFalse(EntityFinder.findAnnotation(superUserElement, MapEntityTo.class).isEmpty());
    }

    @Test
    public void isDomainEntity_recognises_as_domain_only_entities_that_are_explicityly_annotated_with_MapEntityTo_or_DomainEntity() {
        assertTrue(EntityFinder.isDomainEntity(elements.getTypeElement(User.class.getCanonicalName())));
        assertFalse(EntityFinder.isDomainEntity(elements.getTypeElement(SuperUser.class.getCanonicalName())));
        assertTrue(EntityFinder.isDomainEntity(elements.getTypeElement(SuperUserWithDeclaredDesc.class.getCanonicalName())));
        assertFalse("Non-entity should not be recognised as a domain entity.", EntityFinder.isDomainEntity(elements.getTypeElement(NonEntity.class.getCanonicalName())));
    }

    @Test
    public void getEntityTitleAndDesc_is_equivalent_to_TitlesDescsGetter() {
        final var userTitleAndDesc = Pair.pair("User", "User entity");
        assertEquals(userTitleAndDesc, TitlesDescsGetter.getEntityTitleAndDesc(User.class));
        assertEquals(userTitleAndDesc, EntityFinder.getEntityTitleAndDesc(EntityElement.wrapperFor(elements.getTypeElement(User.class.getCanonicalName()))));
        final var superUserTitleAndDesc = Pair.pair("Super User", "A test entity extedning User.");
        assertEquals(superUserTitleAndDesc, TitlesDescsGetter.getEntityTitleAndDesc(SuperUser.class));
        assertEquals(superUserTitleAndDesc, EntityFinder.getEntityTitleAndDesc(EntityElement.wrapperFor(elements.getTypeElement(SuperUser.class.getCanonicalName()))));
    }

    /**
     * A type for testing purposes. Represents an entity that extends a persistent one.
     */
    @DescTitle("Desc")
    @EntityTitle(value = "Super User", desc = "A test entity extedning User.")
    public static class SuperUser extends User {
    }

    /**
     * A type for testing purposes. Represents an entity that extends a persistent one and has property {@code desc} declared explicitly.
     */
    @DomainEntity
    public static class SuperUserWithDeclaredDesc extends User {
        @IsProperty
        @MapTo
        @Title("Desc")
        private String desc;

        @SuppressWarnings("unchecked")
        @Observable
        public EntityFinderTest.SuperUserWithDeclaredDesc setDesc(final String desc) {
            this.desc = desc;
            return this;
        }

        public String getDesc() {
            return desc;
        }
    }

    /**
     * A non-entity pretending to be one.
     */
    @DomainEntity
    public static class NonEntity {
    }

}