package ua.com.fielden.platform.processors.metamodel.utils;

import static java.util.stream.Collectors.joining;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
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
import ua.com.fielden.platform.annotations.metamodel.WithMetaModel;
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
    private EntityFinder entityFinder;
    
    @Before
    public void setup() {
      elements = rule.getElements();
      types = rule.getTypes();
      entityFinder = new EntityFinder(elements, types);
    }

    @Test
    public void isEntityType_type_element_for_AbstractEntity_is_recognised_as_an_element_for_an_entity_type() {
      final TypeElement typeElement = elements.getTypeElement(AbstractEntity.class.getCanonicalName());
      assertTrue(entityFinder.isEntityType(typeElement.asType()));
    }

    @Test
    public void isEntityType_type_element_for_User_is_recognised_as_an_element_for_an_entity_type() {
      final TypeElement typeElement = elements.getTypeElement(User.class.getCanonicalName());
      assertTrue(entityFinder.isEntityType(typeElement.asType()));
    }

    @Test
    public void isEntityType_type_element_for_String_is_not_recognised_as_an_element_for_an_entity_type() {
      final TypeElement typeElement = elements.getTypeElement(String.class.getCanonicalName());
      assertFalse(entityFinder.isEntityType(typeElement.asType()));
    }

    @Test
    public void isPersistentEntityType_type_element_for_User_is_recognised_as_an_element_for_a_persistent_entity_type() {
      final TypeElement typeElement = elements.getTypeElement(User.class.getCanonicalName());
      final EntityElement entityElement = EntityElement.wrapperFor(typeElement);
      assertTrue(entityFinder.isPersistentEntityType(entityElement));
    }

    @Test
    public void isPersistentEntityType_type_element_for_AbstractEntity_is_not_recognised_as_an_element_for_a_persistent_entity_type() {
      final TypeElement typeElement = elements.getTypeElement(AbstractEntity.class.getCanonicalName());
      final EntityElement entityElement = EntityElement.wrapperFor(typeElement);
      assertFalse(entityFinder.isPersistentEntityType(entityElement));
    }

    @Test
    public void doesExtendPersistentEntity_type_element_for_AbstractEntity_is_not_recognised_as_an_element_that_extends_a_persistent_entity_type() {
      final TypeElement typeElement = elements.getTypeElement(AbstractEntity.class.getCanonicalName());
      final EntityElement entityElement = EntityElement.wrapperFor(typeElement);
      assertFalse(entityFinder.doesExtendPersistentEntity(entityElement));
    }

    @Test
    public void doesExtendPersistentEntity_type_element_for_User_is_not_recognised_as_an_element_that_extends_a_persistent_entity_type() {
      final TypeElement typeElement = elements.getTypeElement(User.class.getCanonicalName());
      final EntityElement entityElement = EntityElement.wrapperFor(typeElement);
      assertFalse(entityFinder.doesExtendPersistentEntity(entityElement));
    }

    @Test
    public void doesExtendPersistentEntity_type_element_for_SuperUser_is_recognised_as_an_element_that_extends_a_persistent_entity_type() {
      final TypeElement typeElement = elements.getTypeElement(SuperUser.class.getCanonicalName());
      final EntityElement entityElement = EntityElement.wrapperFor(typeElement);
      assertTrue(entityFinder.doesExtendPersistentEntity(entityElement));
    }

    @Test
    public void findDeclaredProperties_finds_only_declared_properties_in_User() {
      final TypeElement typeElement = elements.getTypeElement(User.class.getCanonicalName());
      final EntityElement entityElement = EntityElement.wrapperFor(typeElement);
      final List<PropertyElement> props = entityFinder.findDeclaredProperties(entityElement);
      assertEquals(7, props.size());
      final String expectedProps = "key, roles, base, basedOnUser, email, active, ssoOnly";
      assertEquals(expectedProps, props.stream().map(p -> p.getSimpleName().toString()).collect(joining(", "))); 
    }

    @Test
    public void findInheritedProperties_finds_only_inherited_properties_in_User() {
      final TypeElement typeElement = elements.getTypeElement(User.class.getCanonicalName());
      final EntityElement entityElement = EntityElement.wrapperFor(typeElement);
      final Set<PropertyElement> props = entityFinder.findInheritedProperties(entityElement);
      // it is expected that some of the re-declared properties in User would appear as both declared and inherited
      // more specifically, properties "active" and "key" appear as both declared and inherited
      final List<String> expectedProps = List.of("active", "refCount", "createdBy", "createdDate", "createdTransactionGuid", "lastUpdatedBy",
              "lastUpdatedDate", "lastUpdatedTransactionGuid", "key", "desc");
      assertEquals(expectedProps, props.stream().map(p -> p.getSimpleName().toString()).toList()); 
    }

    @Test
    public void findProperties_finds_all_properties_in_User() {
      final TypeElement typeElement = elements.getTypeElement(User.class.getCanonicalName());
      final EntityElement entityElement = EntityElement.wrapperFor(typeElement);

      final List<String> expectedProps = List.of("key", "roles", "base", "basedOnUser", "email", "active", "ssoOnly", "refCount", "createdBy",
              "createdDate", "createdTransactionGuid", "lastUpdatedBy", "lastUpdatedDate", "lastUpdatedTransactionGuid", "desc");
      assertEquals(expectedProps, entityFinder.findProperties(entityElement).stream().map(p -> p.getSimpleName().toString()).toList()); 
    }

    @Test
    public void processProperties_excludes_desc_and_includes_id_for_User() {
      final TypeElement typeElement = elements.getTypeElement(User.class.getCanonicalName());
      final EntityElement entityElement = EntityElement.wrapperFor(typeElement);
      final Set<PropertyElement> props = entityFinder.findProperties(entityElement);

      final List<String> expectedProps = List.of("key", "roles", "base", "basedOnUser", "email", "active", "ssoOnly", "refCount", "createdBy",
              "createdDate", "createdTransactionGuid", "lastUpdatedBy", "lastUpdatedDate", "lastUpdatedTransactionGuid", "id");
      assertEquals(expectedProps, 
              entityFinder.processProperties(props, entityElement).stream().map(p -> p.getSimpleName().toString()).toList()); 
    }

    @Test
    public void processProperties_includes_desc_and_id_for_SuperUser() {
      final TypeElement typeElement = elements.getTypeElement(SuperUser.class.getCanonicalName());
      final EntityElement entityElement = EntityElement.wrapperFor(typeElement);
      final Set<PropertyElement> props = entityFinder.findProperties(entityElement);

      // unlike User, SuperUser also has property "desc" due to @DescTitle
      final List<String> expectedProps = List.of("key", "roles", "base", "basedOnUser", "email", "active", "ssoOnly", "refCount", "createdBy",
              "createdDate", "createdTransactionGuid", "lastUpdatedBy", "lastUpdatedDate", "lastUpdatedTransactionGuid", "desc", "id");
      assertEquals(expectedProps, 
              entityFinder.processProperties(props, entityElement).stream().map(p -> p.getSimpleName().toString()).toList());
    }

    @Test
    public void processProperties_includes_desc_and_id_for_SuperUserWithDeclaredDesc() {
      final TypeElement typeElement = elements.getTypeElement(SuperUserWithDeclaredDesc.class.getCanonicalName());
      final EntityElement entityElement = EntityElement.wrapperFor(typeElement);
      final Set<PropertyElement> props = entityFinder.findProperties(entityElement);

      // unlike User, SuperUserWithDeclaredDesc has property "desc" declared explicitly
      final String expectedProps = "desc, key, roles, base, basedOnUser, email, active, ssoOnly, refCount, createdBy, createdDate, createdTransactionGuid, lastUpdatedBy, lastUpdatedDate, lastUpdatedTransactionGuid, id";
      assertEquals(expectedProps,
              entityFinder.processProperties(props, entityElement).stream().map(p -> p.getSimpleName().toString()).collect(joining(", "))); 
    }

    @Test
    public void isPropertyOfEntityType_correctly_distiguishes_between_properties_of_an_entity_type_and_other_types() {
        final TypeElement typeElement = elements.getTypeElement(User.class.getCanonicalName());
        final EntityElement entityElement = EntityElement.wrapperFor(typeElement);
        final Map<String, PropertyElement> props = entityFinder.findProperties(entityElement).stream().collect(Collectors.toMap(pel -> pel.getSimpleName().toString(), Function.identity()));
        assertFalse(entityFinder.isPropertyOfEntityType(props.get("key")));
        assertFalse(entityFinder.isPropertyOfEntityType(props.get("roles")));
        assertFalse(entityFinder.isPropertyOfEntityType(props.get("base")));
        assertTrue(entityFinder.isPropertyOfEntityType(props.get("basedOnUser")));
        assertFalse(entityFinder.isPropertyOfEntityType(props.get("email")));
        assertFalse(entityFinder.isPropertyOfEntityType(props.get("active")));
        assertFalse(entityFinder.isPropertyOfEntityType(props.get("ssoOnly")));
        assertFalse(entityFinder.isPropertyOfEntityType(props.get("refCount")));
        assertTrue(entityFinder.isPropertyOfEntityType(props.get("createdBy")));
        assertFalse(entityFinder.isPropertyOfEntityType(props.get("createdDate")));
        assertFalse(entityFinder.isPropertyOfEntityType(props.get("createdTransactionGuid")));
        assertTrue(entityFinder.isPropertyOfEntityType(props.get("lastUpdatedBy")));
        assertFalse(entityFinder.isPropertyOfEntityType(props.get("lastUpdatedDate")));
        assertFalse(entityFinder.isPropertyOfEntityType(props.get("lastUpdatedTransactionGuid")));
        assertFalse(entityFinder.isPropertyOfEntityType(props.get("lastUpdatedDate")));
        assertFalse(entityFinder.isPropertyOfEntityType(props.get("id")));
    }

    @Test
    public void isPropertyOfEntityType_correctly_identifies_entity_typed_key_as_an_entity_typed_property() {
        final TypeElement typeElement = elements.getTypeElement(UserSecret.class.getCanonicalName());
        final EntityElement entityElement = EntityElement.wrapperFor(typeElement);
        assertTrue(entityFinder.isPropertyOfEntityType(entityFinder.findProperties(entityElement).stream().filter(p -> "key".equals(p.getSimpleName().toString())).findFirst().get()));
    }

    @Test
    public void findAnnotation_looks_for_annotations_through_type_hierarchy() {
        final EntityElement userElement = EntityElement.wrapperFor(elements.getTypeElement(User.class.getCanonicalName()));
        final EntityElement superUserElement = EntityElement.wrapperFor(elements.getTypeElement(SuperUser.class.getCanonicalName()));
        assertTrue(entityFinder.findAnnotation(userElement, DescTitle.class).isEmpty());
        assertFalse(entityFinder.findAnnotation(userElement, MapEntityTo.class).isEmpty());
        assertFalse(entityFinder.findAnnotation(superUserElement, DescTitle.class).isEmpty());
        assertFalse(entityFinder.findAnnotation(superUserElement, MapEntityTo.class).isEmpty());
    }

    @Test
    public void isEntityThatNeedsMetaModel_is_true_only_entities_that_are_explicityly_annotated_with_MapEntityTo_or_DomainEntity_or_WithMetaModel() {
        assertTrue(entityFinder.isEntityThatNeedsMetaModel(elements.getTypeElement(User.class.getCanonicalName())));
        assertTrue(entityFinder.isEntityThatNeedsMetaModel(elements.getTypeElement(NonDomainEntityWithMetaModel.class.getCanonicalName())));
        assertFalse(entityFinder.isEntityThatNeedsMetaModel(elements.getTypeElement(SuperUser.class.getCanonicalName())));
        assertTrue(entityFinder.isEntityThatNeedsMetaModel(elements.getTypeElement(SuperUserWithDeclaredDesc.class.getCanonicalName())));
        assertFalse("Non-entity should not be recognised as a domain entity.", entityFinder.isEntityThatNeedsMetaModel(elements.getTypeElement(NonEntity.class.getCanonicalName())));
    }

    @Test
    public void getEntityTitleAndDesc_is_equivalent_to_TitlesDescsGetter() {
        final var userTitleAndDesc = Pair.pair("User", "User entity");
        assertEquals(userTitleAndDesc, TitlesDescsGetter.getEntityTitleAndDesc(User.class));
        assertEquals(userTitleAndDesc, entityFinder.getEntityTitleAndDesc(EntityElement.wrapperFor(elements.getTypeElement(User.class.getCanonicalName()))));
        final var superUserTitleAndDesc = Pair.pair("Super User", "A test entity extedning User.");
        assertEquals(superUserTitleAndDesc, TitlesDescsGetter.getEntityTitleAndDesc(SuperUser.class));
        assertEquals(superUserTitleAndDesc, entityFinder.getEntityTitleAndDesc(EntityElement.wrapperFor(elements.getTypeElement(SuperUser.class.getCanonicalName()))));
    }

    /**
     * A type for testing purposes. Represents an entity that extends a persistent one.
     */
    @DescTitle("Desc")
    @EntityTitle(value = "Super User", desc = "A test entity extedning User.")
    public static class SuperUser extends User {
    }

    /**
     * A type for testing purposes. Represents an entity that is not a domain entity, but needs a meta-model.
     */
    @WithMetaModel
    public static class NonDomainEntityWithMetaModel extends AbstractEntity<String> {
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