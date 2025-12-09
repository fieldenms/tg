package ua.com.fielden.platform.processors.metamodel.utils;

import metamodels.MetaModels;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.meta.AbstractUnionEntityMetaModel;
import ua.com.fielden.platform.entity.meta.AbstractUnionEntityMetaModelAliased;
import ua.com.fielden.platform.processors.metamodel.MetaModelProcessor;
import ua.com.fielden.platform.processors.metamodel.concepts.MetaModelConcept;
import ua.com.fielden.platform.processors.metamodel.elements.EntityElement;
import ua.com.fielden.platform.processors.metamodel.elements.MetaModelElement;
import ua.com.fielden.platform.processors.metamodel.elements.MetaModelsElement;
import ua.com.fielden.platform.processors.metamodel.models.EntityMetaModel;
import ua.com.fielden.platform.processors.test_entities.*;
import ua.com.fielden.platform.processors.test_entities.meta.*;
import ua.com.fielden.platform.processors.test_utils.ProcessingRule;
import ua.com.fielden.platform.utils.CollectionUtil;
import ua.com.fielden.platform.utils.Pair;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static org.junit.Assert.*;
import static ua.com.fielden.platform.utils.Pair.pair;

/**
 * A test case for utilities provided by {@link MetaModelFinder}.
 * <p>
 * Meta-models for test entities have to be generated prior to running these tests in order for them to be recognised by the compiler.
 * One implication of this approach is that any modifications to the processing logic should be repackaged followed by the recompilation of 
 * test entities to regenerate their meta-models.
 *
 * @author TG Team
 */
public class MetaModelFinderTest {

    @ClassRule
    public static ProcessingRule rule = new ProcessingRule(List.of(), new MetaModelProcessor());
    private static ElementFinder finder;
    private static EntityFinder entityFinder;
    private static MetaModelFinder metaModelFinder;

    @BeforeClass
    public static void setupOnce() {
        finder = new ElementFinder(rule.getProcessingEnvironment());
        entityFinder = new EntityFinder(rule.getProcessingEnvironment());
        metaModelFinder = new MetaModelFinder(rule.getProcessingEnvironment());
    }

    @Test
    public void isMetaModel_returns_true_if_the_type_element_represents_a_meta_model() {
        assertFalse(metaModelFinder.isMetaModel(finder.getTypeElement(String.class)));
        assertFalse(metaModelFinder.isMetaModel(finder.getTypeElement(PersistentEntity.class)));
        assertTrue(metaModelFinder.isMetaModel(finder.getTypeElement(PersistentEntityMetaModel.class)));
        assertTrue(metaModelFinder.isMetaModel(finder.getTypeElement(PersistentEntityMetaModelAliased.class)));
    }

    @Test
    public void isMetaModelAliased_returns_true_if_the_element_represents_an_alised_meta_model() {
        final MetaModelElement mmElt = metaModelFinder.findMetaModel(PersistentEntityMetaModel.class);
        assertFalse(metaModelFinder.isMetaModelAliased(mmElt));
        final MetaModelElement mmAliasedElt = metaModelFinder.findMetaModel(PersistentEntityMetaModelAliased.class);
        assertTrue(metaModelFinder.isMetaModelAliased(mmAliasedElt));
    }

    @Test
    public void findMetaModel_finds_the_meta_model_element_by_class() {
        final MetaModelElement elt = metaModelFinder.findMetaModel(PersistentEntityMetaModel.class);
        assertTrue(finder.isSameType(elt.asType(), PersistentEntityMetaModel.class));
        assertFalse(finder.isSameType(elt.asType(), PersistentEntityMetaModelAliased.class));
    }

    @Test
    public void listMetaModelHierarchy_returns_a_hierarchy_of_meta_model_elements() {
        final BiConsumer<Stream<Class<? extends EntityMetaModel>>, Class<? extends EntityMetaModel>> assertor = 
                (expectedHirerachy, metaModel) -> assertEquals(
                        expectedHirerachy.map(c -> metaModelFinder.findMetaModel(c)).toList(),
                        metaModelFinder.listMetaModelHierarchy(metaModelFinder.findMetaModel(metaModel)));

        // simplest hierarchy of 1 meta-model
        assertor.accept(Stream.of(PersistentEntityMetaModel.class), PersistentEntityMetaModel.class);
        // hierarchy of 2 meta-models
        assertor.accept(Stream.of(SubEntityMetaModel.class, SuperEntityMetaModel.class), SubEntityMetaModel.class);
        // hierarchy for an aliased meta-model
        assertor.accept(Stream.of(SubEntityMetaModelAliased.class, SubEntityMetaModel.class, SuperEntityMetaModel.class),
                SubEntityMetaModelAliased.class);
    }

    @Test
    public void findDeclaredPropertyMethods_finds_only_declared_methods_that_model_entity_properties() {
        final BiConsumer<Collection<String>, Class<? extends EntityMetaModel>> assertor = 
                (expectedProperties, metaModel) -> {
                    final MetaModelElement mme = metaModelFinder.findMetaModel(metaModel);
                    assertEqualContents(expectedProperties,
                            metaModelFinder.findDeclaredPropertyMethods(mme).stream().map(elt -> elt.getSimpleName().toString()).toList());
                };

        assertor.accept(List.of("desc", "entity1", "entity2", "id", "key", "prop1"), EntityWithEntityTypedAndOrdinaryPropsMetaModel.class);
        assertor.accept(List.of(), EntityWithEntityTypedAndOrdinaryPropsMetaModelAliased.class); // alised meta-model
        assertor.accept(List.of("desc", "id", "key", "parent", "prop1"), SubEntityMetaModel.class);
    }

    @Test
    public void findPropertyMethods_finds_all_methods_that_model_entity_properties() {
        final BiConsumer<Collection<String>, Class<? extends EntityMetaModel>> assertor = 
                (expectedProperties, metaModel) -> {
                    final MetaModelElement mme = metaModelFinder.findMetaModel(metaModel);
                    assertEqualContents(expectedProperties,
                            metaModelFinder.findPropertyMethods(mme).stream().map(elt -> elt.getSimpleName().toString()).toList());
                };

        assertor.accept(List.of("desc", "entity1", "entity2", "id", "key", "prop1"), EntityWithEntityTypedAndOrdinaryPropsMetaModel.class);
        assertor.accept(List.of("desc", "entity1", "entity2", "id", "key", "prop1"), EntityWithEntityTypedAndOrdinaryPropsMetaModelAliased.class);
    }

    @Test
    public void findPropertyMethods_includes_both_overriden_methods_and_their_overriders() {
        final BiConsumer<Collection<String>, Class<? extends EntityMetaModel>> assertor = 
                (expectedProperties, metaModel) -> {
                    final MetaModelElement mme = metaModelFinder.findMetaModel(metaModel);
                    assertEqualContents(expectedProperties,
                            metaModelFinder.findPropertyMethods(mme).stream().map(elt -> elt.getSimpleName().toString()).toList());
                };

        assertor.accept(List.of("desc", "id", "key", "parent", "prop1", "desc", "id", "key", "prop1", "prop2"), SubEntityMetaModel.class);
        assertor.accept(List.of("desc", "id", "key", "parent", "prop1", "desc", "id", "key", "prop1", "prop2"), SubEntityMetaModelAliased.class);
    }

    @Test
    public void findPropertyMethod_finds_the_property_method_by_name_traversing_the_whole_hierarchy() {
        final MetaModelElement mme = metaModelFinder.findMetaModel(SubEntityMetaModel.class);
        // declared property
        assertTrue(metaModelFinder.findPropertyMethod(mme, "parent").isPresent());
        // inherited property
        assertTrue(metaModelFinder.findPropertyMethod(mme, "prop2").isPresent());
        // non-existent property
        assertTrue(metaModelFinder.findPropertyMethod(mme, "stub").isEmpty());
        // non-property method
        assertTrue(metaModelFinder.findPropertyMethod(mme, "getEntityClass").isEmpty());
    }

    @Test
    public void findDeclaredPropertyMethod_finds_the_declared_property_method_by_name() {
        final MetaModelElement mme = metaModelFinder.findMetaModel(SubEntityMetaModel.class);
        // declared property
        assertTrue(metaModelFinder.findDeclaredPropertyMethod(mme, "parent").isPresent());
        // inherited property
        assertTrue(metaModelFinder.findDeclaredPropertyMethod(mme, "prop2").isEmpty());
        // non-existent property
        assertTrue(metaModelFinder.findDeclaredPropertyMethod(mme, "stub").isEmpty());
        // non-property method
        assertTrue(metaModelFinder.findDeclaredPropertyMethod(mme, "getEntityClass").isEmpty());
    }

    @Test
    public void isPropertyMetaModelMethod_returns_true_for_methods_that_model_ordinary_properties() {
        final BiConsumer<Boolean, Pair<Class<?>, String>> assertor =
                (expected, clazzAndMethodName) -> {
                    final var clazz = clazzAndMethodName.getKey();
                    final String name = clazzAndMethodName.getValue();
                    final TypeElement typeElt = finder.getTypeElement(clazz);
                    final ExecutableElement ee = finder.findMethods(typeElt).stream()
                            .filter(elt -> elt.getSimpleName().toString().equals(name))
                            .findFirst().orElseThrow();
                    assertEquals(expected, metaModelFinder.isPropertyMetaModelMethod(ee));
                };

        // ordinary property
        assertor.accept(true, pair(SubEntityMetaModel.class, "prop1"));
        // metamodeled entity type property
        assertor.accept(false, pair(SubEntityMetaModel.class, "parent"));
        // something completely different
        assertor.accept(false, pair(SubEntityMetaModelAliased.class, "toPath"));
        assertor.accept(false, pair(Object.class, "toString"));
    }

    @Test
    public void isEntityMetaModelMethod_returns_true_for_methods_that_model_properties_of_metamodeled_entity_types() {
        final BiConsumer<Boolean, Pair<Class<?>, String>> assertor =
                (expected, clazzAndMethodName) -> {
                    final var clazz = clazzAndMethodName.getKey();
                    final String name = clazzAndMethodName.getValue();
                    final TypeElement typeElt = finder.getTypeElement(clazz);
                    final ExecutableElement ee = finder.findMethods(typeElt).stream()
                            .filter(elt -> elt.getSimpleName().toString().equals(name))
                            .findFirst().orElseThrow();
                    assertEquals(expected, metaModelFinder.isEntityMetaModelMethod(ee));
                };

        // ordinary property
        assertor.accept(false, pair(SubEntityMetaModel.class, "prop1"));
        // metamodeled entity type property
        assertor.accept(true, pair(SubEntityMetaModel.class, "parent"));
        // something completely different
        assertor.accept(false, pair(SubEntityMetaModelAliased.class, "toPath"));
        assertor.accept(false, pair(Object.class, "toString"));
    }

    @Test
    public void isSameMetaModel_returns_true_if_concept_and_element_represent_the_same_meta_model() {
        final BiConsumer<Boolean, Pair<Class<? extends AbstractEntity>, Class<? extends EntityMetaModel>>> assertor =
                // accepts a pair of (entity type for meta-model concept, meta-model type for element)
                (expected, pair) -> {
                    final Class<? extends AbstractEntity> entityClass = pair.getKey();
                    final Class<? extends EntityMetaModel> metaModelClass = pair.getValue();
                    final MetaModelConcept mmc = new MetaModelConcept(entityFinder.findEntity(entityClass));
                    final MetaModelElement mme = metaModelFinder.findMetaModel(metaModelClass);
                    assertEquals(expected, metaModelFinder.isSameMetaModel(mmc, mme));
                };

        assertor.accept(true, pair(SubEntity.class, SubEntityMetaModel.class));
        assertor.accept(true, pair(SubEntity.class, SubEntityMetaModelAliased.class));
        assertor.accept(false, pair(SubEntity.class, SuperEntityMetaModel.class));
        assertor.accept(false, pair(SubEntity.class, SuperEntityMetaModelAliased.class));
        assertor.accept(false, pair(SubEntity.class, PersistentEntityMetaModel.class));
        assertor.accept(false, pair(SubEntity.class, PersistentEntityMetaModelAliased.class));
    }

    @Test
    public void findMetaModelForEntity_finds_the_meta_model_element_only_for_those_entities_that_have_a_meta_model() {
        final BiConsumer<Class<? extends AbstractEntity>, Class<? extends EntityMetaModel>> assertor =
                // metaModelClass may be null if no meta-model is expected to be found
                (entityClass, metaModelClass) -> {
                    final EntityElement entityElt = entityFinder.findEntity(entityClass);
                    final Optional<MetaModelElement> maybeMme = metaModelFinder.findMetaModelForEntity(entityElt);
                    if (metaModelClass == null) {
                        assertTrue(maybeMme.isEmpty());
                    } else {
                        assertTrue(finder.isSameType(maybeMme.get().asType(), metaModelClass));
                    }
                };

        assertor.accept(PersistentEntity.class, PersistentEntityMetaModel.class);
        assertor.accept(SubEntity.class, SubEntityMetaModel.class);
        assertor.accept(SuperEntity.class, SuperEntityMetaModel.class);
        assertor.accept(EntityWithDescTitleWithoutMetaModel.class, null);
        assertor.accept(AbstractEntity.class, null);
    }

    @Test
    public void findMetaModelAliased_finds_the_aliased_version_of_a_meta_model() {
        final BiConsumer<Class<? extends EntityMetaModel>, Class<? extends EntityMetaModel>> assertor =
                // expectedAliasedMetaModelClass may be null if no aliased meta-model is expected to be found
                (expectedAliasedMetaModelClass, inputMetaModelClass) -> {
                    final MetaModelElement mme = metaModelFinder.findMetaModel(inputMetaModelClass);
                    final Optional<MetaModelElement> maybeAliasedMme = metaModelFinder.findMetaModelAliased(mme);
                    if (expectedAliasedMetaModelClass == null) {
                        maybeAliasedMme.ifPresent(aliasedMme -> 
                            fail("Unexpectedly found an aliased meta-model [%s]".formatted(aliasedMme)));
                    } else {
                        assertTrue(finder.isSameType(maybeAliasedMme.get().asType(), expectedAliasedMetaModelClass));
                    }
                };

        // ordinary meta-models as input
        assertor.accept(SubEntityMetaModelAliased.class, SubEntityMetaModel.class);
        assertor.accept(SuperEntityMetaModelAliased.class, SuperEntityMetaModel.class);
        // aliased meta-model as input
        assertor.accept(SubEntityMetaModelAliased.class, SubEntityMetaModelAliased.class);
        // nothing should be found 
        assertor.accept(null, EntityMetaModel.class);
    }

    @Test
    public void findMetaModels_finds_all_meta_models_declared_by_MetaModels() {
        final List<Class<?>> expectedClasses = List.of(
                AbstractUnionEntityMetaModel.class,
                EntityWithDescTitleMetaModel.class,
                EntityWithEntityTypedAndOrdinaryPropsMetaModel.class,
                EntityWithKeyTypeNoKeyMetaModel.class,
                EntityWithKeyTypeOfEntityTypeMetaModel.class,
                EntityWithOrdinaryPropsMetaModel.class,
                EntityWithPropertyDescMetaModel.class,
                EntityWithoutDescTitleAndPropertyDescMetaModel.class,
                EntityWithoutDescTitleAndPropertyDesc_extends_EntityWithPropertyDescWithoutMetaModelMetaModel.class,
                EntityWithoutDescTitle_extends_EntityWithDescTitleWithoutMetaModelMetaModel.class,
                ExampleEntityMetaModel.class,
                ExampleUnionEntityMetaModel.class,
                KeyTypeAsComposite_SubEntityExtendingAbstractSuperEntityWithoutKeyTypeMetaModel.class,
                KeyTypeAsEntity_SubEntityExtendingAbstractSuperEntityWithoutKeyTypeMetaModel.class,
                KeyTypeAsString_SubEntityExtendingAbstractSuperEntityWithoutKeyTypeMetaModel.class,
                KeyType_AbstractSuperEntityWithoutKeyTypeMetaModel.class,
                NonPersistentButDomainEntityMetaModel.class,
                NonPersistentButWithMetaModelEntityMetaModel.class,
                PersistentEntityMetaModel.class,
                SubEntityMetaModel.class,
                SuperEntityMetaModel.class,
                AbstractUnionEntityMetaModelAliased.class,
                EntityWithDescTitleMetaModelAliased.class,
                EntityWithEntityTypedAndOrdinaryPropsMetaModelAliased.class,
                EntityWithKeyTypeNoKeyMetaModelAliased.class,
                EntityWithKeyTypeOfEntityTypeMetaModelAliased.class,
                EntityWithOrdinaryPropsMetaModelAliased.class,
                EntityWithPropertyDescMetaModelAliased.class,
                EntityWithoutDescTitleAndPropertyDescMetaModelAliased.class,
                EntityWithoutDescTitleAndPropertyDesc_extends_EntityWithPropertyDescWithoutMetaModelMetaModelAliased.class,
                EntityWithoutDescTitle_extends_EntityWithDescTitleWithoutMetaModelMetaModelAliased.class,
                ExampleEntityMetaModelAliased.class,
                ExampleUnionEntityMetaModelAliased.class,
                KeyTypeAsComposite_SubEntityExtendingAbstractSuperEntityWithoutKeyTypeMetaModelAliased.class,
                KeyTypeAsEntity_SubEntityExtendingAbstractSuperEntityWithoutKeyTypeMetaModelAliased.class,
                KeyTypeAsString_SubEntityExtendingAbstractSuperEntityWithoutKeyTypeMetaModelAliased.class,
                KeyType_AbstractSuperEntityWithoutKeyTypeMetaModelAliased.class,
                NonPersistentButDomainEntityMetaModelAliased.class,
                NonPersistentButWithMetaModelEntityMetaModelAliased.class,
                PersistentEntityMetaModelAliased.class,
                SubEntityMetaModelAliased.class,
                SuperEntityMetaModelAliased.class);

        final TypeElement metaModelsElt = finder.getTypeElement(MetaModels.class);
        final List<MetaModelElement> metaModelElts = metaModelFinder.streamMetaModels(metaModelsElt).toList();

        // sorting beforehand is important 
        final List<TypeElement> expected = expectedClasses.stream()
                .map(clazz -> finder.getTypeElement(clazz))
                .sorted((e1, e2) -> e1.getQualifiedName().toString().compareTo(e2.getQualifiedName().toString()))
                .toList();
        final List<TypeElement> actual = metaModelElts.stream()
                .map(elt -> elt.element())
                .sorted((e1, e2) -> e1.getQualifiedName().toString().compareTo(e2.getQualifiedName().toString()))
                .toList();

        assertEquals(CollectionUtil.toString(expected, "\n"), CollectionUtil.toString(actual, "\n"));
    }

    @Test
    public void findMetaModelsElement_finds_the_element_representing_MetaModels() {
        final Optional<MetaModelsElement> elt = metaModelFinder.findMetaModelsElement();
        assertTrue("MetaModels element was not found.", elt.isPresent());
        assertTrue(metaModelFinder.isSameType(elt.get().asType(), MetaModels.class));
    }

    @Test
    public void newMetaModelElement_returns_a_new_instance_with_package_information() {
        final TypeElement elt = finder.getTypeElement(ExampleEntityMetaModel.class);
        final MetaModelElement mme = metaModelFinder.newMetaModelElement(elt);
        assertEquals(ExampleEntityMetaModel.class.getPackageName(), mme.getPackageName());
    }

    @Test
    public void resolveMetaModelName_returns_the_qualified_name_of_ExampleEntityMetaModel_for_ExampleEntity() {
        final String metaModelName = MetaModelFinder.resolveMetaModelName(ExampleEntity.class.getPackageName(), ExampleEntity.class.getSimpleName());
        assertEquals(ExampleEntityMetaModel.class.getCanonicalName(), metaModelName);
    }

    @Test
    public void resolveAliasedMetaModelName_returns_the_qualified_name_of_ExampleEntityMetaModelAliased_for_ExampleEntity() {
        final String metaModelName = MetaModelFinder.resolveAliasedMetaModelName(ExampleEntity.class.getPackageName(), ExampleEntity.class.getSimpleName());
        assertEquals(ExampleEntityMetaModelAliased.class.getCanonicalName(), metaModelName);
    }

    // ==================== HELPER METHODS ====================
    private static void assertEqualContents(final Collection<?> c1, final Collection<?> c2) {
        if (CollectionUtil.areEqualByContents(c1, c2)) {}
        else {
            fail("expected:<%s> but was:<%s>".formatted(CollectionUtil.toString(c1, ", "), CollectionUtil.toString(c2, ", ")));
        }
    }

}
