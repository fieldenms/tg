package ua.com.fielden.platform.processors.metamodel.utils;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import ua.com.fielden.platform.annotations.metamodel.DomainEntity;
import ua.com.fielden.platform.annotations.metamodel.WithMetaModel;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.processors.metamodel.elements.EntityElement;
import ua.com.fielden.platform.processors.metamodel.elements.PropertyElement;
import ua.com.fielden.platform.processors.test_entities.ExampleEntity;
import ua.com.fielden.platform.processors.test_entities.ExampleUnionEntity;
import ua.com.fielden.platform.processors.test_entities.SubEntity;
import ua.com.fielden.platform.processors.test_utils.ProcessingRule;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.utils.Pair;

import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;
import static metamodels.MetaModels.ExampleEntity_;
import static metamodels.MetaModels.SubEntity_;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static ua.com.fielden.platform.entity.AbstractEntity.*;

/// A test case for utility functions in [EntityFinder].
///
public class EntityFinderTest {

    @ClassRule
    public static ProcessingRule rule = new ProcessingRule();
    private static Elements elements;
    private static Types types;
    private static EntityFinder entityFinder;
    
    @BeforeClass
    public static void setup() {
        elements = rule.getElements();
        types = rule.getTypes();
        entityFinder = new EntityFinder(rule.getProcessingEnvironment());
    }

    @Test
    public void isEntityType_type_element_for_AbstractEntity_is_recognised_as_an_element_for_an_entity_type() {
      final TypeElement typeElt = entityFinder.getTypeElement(AbstractEntity.class);
      assertTrue(entityFinder.isEntityType(typeElt.asType()));
    }

    @Test
    public void isEntityType_type_element_for_User_is_recognised_as_an_element_for_an_entity_type() {
      final TypeElement typeElt = entityFinder.getTypeElement(User.class);
      assertTrue(entityFinder.isEntityType(typeElt.asType()));
    }

    @Test
    public void isEntityType_type_element_for_String_is_not_recognised_as_an_element_for_an_entity_type() {
      final TypeElement typeElt = entityFinder.getTypeElement(String.class);
      assertFalse(entityFinder.isEntityType(typeElt.asType()));
    }

    @Test
    public void isPersistentEntityType_type_element_for_User_is_recognised_as_an_element_for_a_persistent_entity_type() {
      final EntityElement entity = entityFinder.findEntity(User.class);
      assertTrue(entityFinder.isPersistentEntityType(entity));
    }

    @Test
    public void isPersistentEntityType_type_element_for_AbstractEntity_is_not_recognised_as_an_element_for_a_persistent_entity_type() {
      final EntityElement entity = entityFinder.findEntity(AbstractEntity.class);
      assertFalse(entityFinder.isPersistentEntityType(entity));
    }

    @Test
    public void doesExtendPersistentEntity_type_element_for_AbstractEntity_is_not_recognised_as_an_element_that_extends_a_persistent_entity_type() {
      final EntityElement entity = entityFinder.findEntity(AbstractEntity.class);
      assertFalse(entityFinder.doesExtendPersistentEntity(entity));
    }

    @Test
    public void doesExtendPersistentEntity_type_element_for_User_is_not_recognised_as_an_element_that_extends_a_persistent_entity_type() {
      final EntityElement entity = entityFinder.findEntity(User.class);
      assertFalse(entityFinder.doesExtendPersistentEntity(entity));
    }

    @Test
    public void doesExtendPersistentEntity_type_element_for_SuperUser_is_recognised_as_an_element_that_extends_a_persistent_entity_type() {
      final EntityElement entity = entityFinder.findEntity(SuperUser.class);
      assertTrue(entityFinder.doesExtendPersistentEntity(entity));
    }

    @Test
    public void findDeclaredProperties_finds_only_declared_properties_in_User() {
      final EntityElement entity = entityFinder.findEntity(User.class);
      final List<PropertyElement> props = entityFinder.findDeclaredProperties(entity);
      assertEquals(8, props.size());
      final String expectedProps = "key, activeRoles, inactiveRoles, base, basedOnUser, email, active, ssoOnly";
      assertEquals(expectedProps, props.stream().map(p -> p.getSimpleName().toString()).collect(joining(", "))); 
    }

    @Test
    public void findInheritedProperties_finds_only_inherited_properties_in_User() {
      final EntityElement entity = entityFinder.findEntity(User.class);
      final Set<PropertyElement> props = entityFinder.findInheritedProperties(entity);
      // it is expected that some of the re-declared properties in User would appear as both declared and inherited
      // more specifically, properties "active" and "key" appear as both declared and inherited
      final List<String> expectedProps = List.of("active", "refCount", "createdBy", "createdDate", "createdTransactionGuid", "lastUpdatedBy",
              "lastUpdatedDate", "lastUpdatedTransactionGuid", "key", "desc");
      assertEquals(expectedProps, props.stream().map(p -> p.getSimpleName().toString()).toList()); 
    }

    @Test
    public void findProperties_finds_all_properties_in_User() {
      final EntityElement entity = entityFinder.findEntity(User.class);

      final List<String> expectedProps = List.of("key", "activeRoles", "inactiveRoles", "base", "basedOnUser", "email", "active", "ssoOnly", "refCount", "createdBy",
              "createdDate", "createdTransactionGuid", "lastUpdatedBy", "lastUpdatedDate", "lastUpdatedTransactionGuid", "desc");
      assertEquals(expectedProps, entityFinder.findProperties(entity).stream().map(p -> p.getSimpleName().toString()).toList()); 
    }

    @Test
    public void findProperties_for_a_union_entity_type_finds_exactly_union_members_and_common_properties_and_ID_and_KEY_and_DESC() {
        final var entity = entityFinder.findEntity(ExampleUnionEntity.class);

        assertEquals(Set.of("prop1", "prop2",
                            "common1", "common2",
                            ID, KEY, DESC),
                     entityFinder.findProperties(entity).stream().map(p -> p.getSimpleName().toString()).collect(toSet()));
    }

    @Test
    public void findDeclaredProperties_for_a_union_entity_type_finds_exactly_union_members() {
        final var entity = entityFinder.findEntity(ExampleUnionEntity.class);

        assertEquals(Set.of("prop1", "prop2"),
                     entityFinder.findDeclaredProperties(entity).stream().map(p -> p.getSimpleName().toString()).collect(toSet()));
    }

    @Test
    public void findInheritedProperties_for_a_union_entity_type_finds_exactly_KEY_and_DESC() {
        final var entity = entityFinder.findEntity(ExampleUnionEntity.class);

        assertEquals(Set.of(KEY, DESC),
                     entityFinder.findInheritedProperties(entity).stream().map(p -> p.getSimpleName().toString()).collect(toSet()));
    }

    @Test
    public void findProperty_finds_a_common_property_in_a_union_entity_type() {
        final var entity = entityFinder.findEntity(ExampleUnionEntity.class);

        assertThat(entityFinder.findProperty(entity, "common1")).isPresent();
    }

    @Test
    public void findProperty_finds_a_property_by_name_in_the_whole_type_hierarchy() {
        final EntityElement entity = entityFinder.findEntity(FindProperty_Example.class); 
        entityFinder.findProperty(entity, "key").ifPresentOrElse(prop -> {
            assertEquals("key", prop.getSimpleName().toString());
            assertTrue(entityFinder.isSameType(prop.getType(), String.class));
            assertTrue(entityFinder.isSameType(prop.getEnclosingElement().asType(), FindProperty_Example.class));
        }, () -> fail("Property was not found"));;

        entityFinder.findProperty(entity, "desc").ifPresentOrElse(prop -> {
            assertEquals("desc", prop.getSimpleName().toString());
            assertTrue(entityFinder.isSameType(prop.getType(), String.class));
            assertTrue(entityFinder.isSameType(prop.getEnclosingElement().asType(), AbstractEntity.class));
        }, () -> fail("Property was not found"));;

        assertTrue(entityFinder.findProperty(entity, "stub").isEmpty());
        assertTrue(entityFinder.findProperty(entity, "id").isEmpty());
    }
    // where
    private static class FindProperty_Example extends AbstractEntity<String> {
        @IsProperty
        private String key;
    }

    @Test
    public void findPropertyBelow_finds_a_property_by_name_in_the_whole_type_hierarchy_below_the_specified_type() {
        final EntityElement entity = entityFinder.findEntity(FindProperty_Example.class);
        entityFinder.findPropertyBelow(entity, "key", AbstractEntity.class).ifPresentOrElse(prop -> {
            assertEquals("key", prop.getSimpleName().toString());
            assertTrue(entityFinder.isSameType(prop.getType(), String.class));
            assertTrue(entityFinder.isSameType(prop.getEnclosingElement().asType(), FindProperty_Example.class));
        }, () -> fail("Property was not found"));;

        assertTrue(entityFinder.findPropertyBelow(entity, "desc", AbstractEntity.class).isEmpty());
        // unrelated hierarchies
        assertTrue(entityFinder.findPropertyBelow(entity, "stub", List.class).isEmpty());
    }

    @Test
    public void getPropTitleAndDesc_returns_a_pair_of_property_title_and_description() {
        final EntityElement entity = entityFinder.findEntity(GetPropTitleAndDesc_Example.class);

        final BiConsumer<Pair<String, String>, String> assertor = (pair, propName) -> {
            assertEquals(pair, entityFinder.getPropTitleAndDesc(entityFinder.findProperty(entity, propName).orElseThrow()));
        };

        assertor.accept(Pair.pair("Title", "Description"), "titleAndDesc");
        assertor.accept(Pair.pair("Title", ""),            "titleOnly");
        assertor.accept(Pair.pair("", "Description"),      "descOnly");
        assertor.accept(Pair.pair("", ""),                 "nothing");
    }
    // where
    private static class GetPropTitleAndDesc_Example extends AbstractEntity<String> {
        @IsProperty
        @Title(value = "Title", desc = "Description")
        private String titleAndDesc;

        @IsProperty
        @Title(value = "Title")
        private String titleOnly;

        @IsProperty
        @Title(desc = "Description")
        private String descOnly;

        @IsProperty
        private String nothing;
    }

    @Test
    public void processProperties_excludes_desc_and_includes_id_for_User() {
      final EntityElement entity = entityFinder.findEntity(User.class);
      final Set<PropertyElement> props = entityFinder.findProperties(entity);

      final List<String> expectedProps = List.of("key", "activeRoles", "inactiveRoles", "base", "basedOnUser", "email", "active", "ssoOnly", "refCount", "createdBy",
              "createdDate", "createdTransactionGuid", "lastUpdatedBy", "lastUpdatedDate", "lastUpdatedTransactionGuid", "id");
      assertEquals(expectedProps, 
              entityFinder.processProperties(props, entity).stream().map(p -> p.getSimpleName().toString()).toList()); 
    }

    @Test
    public void processProperties_includes_desc_and_id_for_SuperUser() {
      final EntityElement entity = entityFinder.findEntity(SuperUser.class);
      final Set<PropertyElement> props = entityFinder.findProperties(entity);

      // unlike User, SuperUser also has property "desc" due to @DescTitle
      final List<String> expectedProps = List.of("key", "activeRoles", "inactiveRoles", "base", "basedOnUser", "email", "active", "ssoOnly", "refCount", "createdBy",
              "createdDate", "createdTransactionGuid", "lastUpdatedBy", "lastUpdatedDate", "lastUpdatedTransactionGuid", "desc", "id");
      assertEquals(expectedProps, 
              entityFinder.processProperties(props, entity).stream().map(p -> p.getSimpleName().toString()).toList());
    }

    @Test
    public void processProperties_includes_desc_and_id_for_SuperUserWithDeclaredDesc() {
      final EntityElement entity = entityFinder.findEntity(SuperUserWithDeclaredDesc.class);
      final Set<PropertyElement> props = entityFinder.findProperties(entity);

      // unlike User, SuperUserWithDeclaredDesc has property "desc" declared explicitly
      final String expectedProps = "desc, key, activeRoles, inactiveRoles, base, basedOnUser, email, active, ssoOnly, refCount, createdBy, createdDate, createdTransactionGuid, lastUpdatedBy, lastUpdatedDate, lastUpdatedTransactionGuid, id";
      assertEquals(expectedProps,
              entityFinder.processProperties(props, entity).stream().map(p -> p.getSimpleName().toString()).collect(joining(", "))); 
    }

    /**
     * Property {@code desc} is included for an entity with no {@link DescTitle} and no declared {@code desc} 
     * if it extends an entity annotated with {@link DescTitle}.
     */
    @Test
    public void processProperties_includes_desc_for_NoDescTitleAndNoDeclaredDesc_extending_type_with_DescTitle_annotation() {
        final EntityElement entity = entityFinder.findEntity(NoDescTitleAndNoDeclaredDesc.class);
        final Set<PropertyElement> props = entityFinder.findProperties(entity);

        final String expectedProps = "key, activeRoles, inactiveRoles, base, basedOnUser, email, active, ssoOnly, refCount, createdBy, createdDate, createdTransactionGuid, lastUpdatedBy, lastUpdatedDate, lastUpdatedTransactionGuid, desc, id";
        assertEquals(expectedProps,
                entityFinder.processProperties(props, entity).stream().map(p -> p.getSimpleName().toString()).collect(joining(", "))); 
    }
    // where
    public static class NoDescTitleAndNoDeclaredDesc extends SuperUser { }

    /**
     * Property {@code desc} is included for an entity with no {@link DescTitle} and no declared {@code desc} 
     * if it extends an entity that declares {@code desc}.
     */
    @Test
    public void processProperties_includes_desc_for_NoDescTitleAndNoDeclaredDesc_extending_type_with_property_desc() {
        final EntityElement entity = entityFinder.findEntity(NoDescTitleAndNoDeclaredDesc2.class);
        final Set<PropertyElement> props = entityFinder.findProperties(entity);

        final String expectedProps = "desc, key, activeRoles, inactiveRoles, base, basedOnUser, email, active, ssoOnly, refCount, createdBy, createdDate, createdTransactionGuid, lastUpdatedBy, lastUpdatedDate, lastUpdatedTransactionGuid, id";
        assertEquals(expectedProps,
                entityFinder.processProperties(props, entity).stream().map(p -> p.getSimpleName().toString()).collect(joining(", "))); 
    }
    // where
    public static class NoDescTitleAndNoDeclaredDesc2 extends SuperUserWithDeclaredDesc { }

    @Test
    public void findAnnotation_looks_for_annotations_through_type_hierarchy() {
        final EntityElement userElement = entityFinder.findEntity(User.class);
        final EntityElement superUserElement = entityFinder.findEntity(SuperUser.class);
        assertTrue(entityFinder.findAnnotation(userElement, DescTitle.class).isEmpty());
        assertFalse(entityFinder.findAnnotation(userElement, MapEntityTo.class).isEmpty());
        assertFalse(entityFinder.findAnnotation(superUserElement, DescTitle.class).isEmpty());
        assertFalse(entityFinder.findAnnotation(superUserElement, MapEntityTo.class).isEmpty());
    }

    @Test
    public void isEntityThatNeedsMetaModel_is_true_only_entities_that_are_explicityly_annotated_with_MapEntityTo_or_DomainEntity_or_WithMetaModel() {
        assertTrue(entityFinder.isEntityThatNeedsMetaModel(entityFinder.findEntity(User.class)));
        assertTrue(entityFinder.isEntityThatNeedsMetaModel(entityFinder.findEntity(NonDomainEntityWithMetaModel.class)));
        assertFalse(entityFinder.isEntityThatNeedsMetaModel(entityFinder.findEntity(SuperUser.class)));
        assertTrue(entityFinder.isEntityThatNeedsMetaModel(entityFinder.findEntity(SuperUserWithDeclaredDesc.class)));
        assertFalse("Non-entity should not be recognised as a domain entity.",
                entityFinder.isEntityThatNeedsMetaModel(entityFinder.getTypeElement(NonEntity.class)));
    }

    @Test
    public void union_entities_are_meta_modeled() {
        assertTrue(entityFinder.isEntityThatNeedsMetaModel(entityFinder.findEntity(SimpleUnionEntity.class)));
    }

    @Test
    public void synthetic_entities_are_meta_modeled() {
        assertTrue(entityFinder.isEntityThatNeedsMetaModel(entityFinder.findEntity(SyntheticEntityWithModel.class)));
        assertTrue(entityFinder.isEntityThatNeedsMetaModel(entityFinder.findEntity(SyntheticEntityWithModels.class)));
    }

    @Test
    public void getEntityTitleAndDesc_is_equivalent_to_TitlesDescsGetter() {
        final var userTitleAndDesc = Pair.pair("User", "User entity");
        assertEquals(userTitleAndDesc, TitlesDescsGetter.getEntityTitleAndDesc(User.class));
        assertEquals(userTitleAndDesc, entityFinder.getEntityTitleAndDesc(entityFinder.findEntity(User.class)));
        final var superUserTitleAndDesc = Pair.pair("Super User", "A test entity extedning User.");
        assertEquals(superUserTitleAndDesc, TitlesDescsGetter.getEntityTitleAndDesc(SuperUser.class));
        assertEquals(superUserTitleAndDesc, entityFinder.getEntityTitleAndDesc(entityFinder.findEntity(SuperUser.class)));
    }

    @Test
    public void getKeyType_returns_a_type_mirror_representing_the_value_of_KeyType() {
        final EntityElement entity = entityFinder.findEntity(WithDeclaredKeyType.class);
        final KeyType atKeyType = entity.getAnnotation(KeyType.class);
        assertNotNull(atKeyType);
        assertTrue(entityFinder.isSameType(entityFinder.getKeyType(atKeyType), String.class));
    }
    // where
    @KeyType(value = String.class, keyMemberSeparator = " ")
    private static class WithDeclaredKeyType extends AbstractEntity<String> {}

    @Test
    public void determineKeyType_finds_the_first_value_of_KeyType_for_an_entity_traversing_its_hierarchy() {
        final EntityElement withoutDeclaredKeyType = entityFinder.findEntity(WithoutDeclaredKeyType.class);
        assertTrue(entityFinder.determineKeyType(withoutDeclaredKeyType).isEmpty());

        final EntityElement withoutExtendsWith = entityFinder.findEntity(WithoutDeclaredKeyType_extends_WithDeclaredKeyType.class);
        entityFinder.determineKeyType(withoutExtendsWith).ifPresentOrElse(
                typeMirror -> assertTrue(entityFinder.isSameType(typeMirror, String.class)),
                () -> fail("Key type was not found."));

        final EntityElement withExtendsWith = entityFinder.findEntity(WithDeclaredKeyType_extends_WithDeclaredKeyType.class);
        entityFinder.determineKeyType(withExtendsWith).ifPresentOrElse(
                typeMirror -> assertTrue(entityFinder.isSameType(typeMirror, String.class)),
                () -> fail("Key type was not found."));
    }
    // where
    private static class WithoutDeclaredKeyType extends AbstractEntity<String> {}
    private static class WithoutDeclaredKeyType_extends_WithDeclaredKeyType extends WithDeclaredKeyType {}
    @KeyType(String.class)
    private static class WithDeclaredKeyType_extends_WithDeclaredKeyType extends WithDeclaredKeyType {}

    @Test
    public void streamParents_returns_a_stream_of_superclasses_of_an_entity_up_to_and_including_AbstractEntity() {
        final BiConsumer<Collection<Class<?>>, Class<? extends AbstractEntity>> assertor =
                (expectedParents, entityClass) -> {
                    final EntityElement entity = entityFinder.findEntity(entityClass);
                    assertEquals(expectedParents.stream().map(c -> entityFinder.newEntityElement(entityFinder.getTypeElement(c))).toList(),
                            entityFinder.streamParents(entity).toList());
                };

        assertor.accept(List.of(ActivatableAbstractEntity.class, AbstractPersistentEntity.class, AbstractEntity.class), 
                User.class);
        assertor.accept(List.of(User.class, ActivatableAbstractEntity.class, AbstractPersistentEntity.class, AbstractEntity.class), 
                SuperUser.class);
        assertor.accept(List.of(), AbstractEntity.class);
        assertor.accept(List.of(AbstractEntity.class), AbstractPersistentEntity.class);
    }

    @Test
    public void getParent_returns_the_immediate_superclass_of_an_entity_or_empty_if_given_AbstractEntity() {
        entityFinder.getParent(entityFinder.findEntity(SuperUser.class)).ifPresentOrElse(
                parent -> assertTrue(entityFinder.isSameType(parent.asType(), User.class)),
                () -> fail("Entity parent was not found."));

        assertTrue(entityFinder.getParent(entityFinder.findEntity(AbstractEntity.class)).isEmpty());
    }

    @Test
    public void findPropertyAccessor_finds_accessor_that_starts_with_get() {
        entityFinder.findPropertyAccessor(entityFinder.findEntity(ExampleEntity.class), ExampleEntity_.prop1()).ifPresentOrElse(
                method -> assertEquals("getProp1", method.getSimpleName().toString()),
                () -> fail("Property accessor was not found."));
    }

    @Test
    public void findPropertyAccessor_finds_accessor_that_starts_with_is() {
        entityFinder.findPropertyAccessor(entityFinder.findEntity(ExampleEntity.class), ExampleEntity_.flag()).ifPresentOrElse(
                method -> assertEquals("isFlag", method.getSimpleName().toString()),
                () -> fail("Property accessor was not found."));
    }

    @Test
    public void findPropertyAccessor_finds_inherited_accessor() {
        entityFinder.findPropertyAccessor(entityFinder.findEntity(SubEntity.class), SubEntity_.prop2()).ifPresentOrElse(
                method -> assertEquals("getProp2", method.getSimpleName().toString()),
                () -> fail("Property accessor was not found."));
    }

    @Test
    public void findDeclaredPropertyAccessor_does_not_find_inherited_accessor() {
        assertTrue("Inherited accessor should not have been found.",
                entityFinder.findDeclaredPropertyAccessor(entityFinder.findEntity(SubEntity.class), SubEntity_.prop2()).isEmpty());
    }

    @Test
    public void findPropertySetter_finds_both_declared_and_inherited_setters() {
        entityFinder.findPropertySetter(entityFinder.findEntity(SubEntity.class), SubEntity_.parent()).ifPresentOrElse(
                method -> assertEquals("setParent", method.getSimpleName().toString()),
                () -> fail("Declared property setter was not found."));

        entityFinder.findPropertySetter(entityFinder.findEntity(SubEntity.class), SubEntity_.prop2()).ifPresentOrElse(
                method -> assertEquals("setProp2", method.getSimpleName().toString()),
                () -> fail("Inherited property setter was not found."));
    }

    @Test
    public void findDeclaredPropertySetter_finds_only_declared_setters() {
        assertTrue("Inherited setter should not have been found.",
                entityFinder.findDeclaredPropertySetter(entityFinder.findEntity(SubEntity.class), SubEntity_.prop2()).isEmpty());

        entityFinder.findDeclaredPropertySetter(entityFinder.findEntity(SubEntity.class), SubEntity_.prop1()).ifPresentOrElse(
                method -> assertEquals("setProp1", method.getSimpleName().toString()),
                () -> fail("Property setter was not found."));
    }

    @Test
    public void isCollectionalProperty_returns_true_for_collectional_properties() {
        final EntityElement exampleEntity = entityFinder.findEntity(ExampleEntity.class);

        final PropertyElement collectionProperty = entityFinder.findProperty(exampleEntity, ExampleEntity_.collection()).orElseThrow();
        assertTrue(entityFinder.isCollectionalProperty(collectionProperty));

        final PropertyElement nonCollectionalProperty = entityFinder.findProperty(exampleEntity, ExampleEntity_.prop1()).orElseThrow();
        assertFalse(entityFinder.isCollectionalProperty(nonCollectionalProperty));
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
        public SuperUserWithDeclaredDesc setDesc(final String desc) {
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

    public static class SimpleUnionEntity extends AbstractUnionEntity {
        @IsProperty
        @MapTo
        @Title(value = "User")
        private User user;

        public User getUser() {
            return user;
        }

        @Observable
        public SimpleUnionEntity setUser(final User user) {
            this.user = user;
            return this;
        }
    }

    public static class SyntheticEntityWithModel extends AbstractEntity<String> {
        static final EntityResultQueryModel<SyntheticEntityWithModel> model_ = null;
    }

    public static class SyntheticEntityWithModels extends AbstractEntity<String> {
        static final List<EntityResultQueryModel<SyntheticEntityWithModels>> models_ = null;
    }

}
