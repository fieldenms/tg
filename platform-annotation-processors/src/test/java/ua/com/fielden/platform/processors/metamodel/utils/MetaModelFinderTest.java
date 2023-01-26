package ua.com.fielden.platform.processors.metamodel.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.utils.Pair.pair;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import ua.com.fielden.platform.processors.metamodel.MetaModelProcessor;
import ua.com.fielden.platform.processors.metamodel.elements.EntityElement;
import ua.com.fielden.platform.processors.metamodel.elements.MetaModelElement;
import ua.com.fielden.platform.processors.metamodel.models.EntityMetaModel;
import ua.com.fielden.platform.processors.test_entities.EntityWithDescTitleWithoutMetaModel;
import ua.com.fielden.platform.processors.test_entities.MetamodeledEntity;
import ua.com.fielden.platform.processors.test_entities.meta.EntityWithEntityTypedAndOrdinaryPropsMetaModel;
import ua.com.fielden.platform.processors.test_entities.meta.EntityWithEntityTypedAndOrdinaryPropsMetaModelAliased;
import ua.com.fielden.platform.processors.test_entities.meta.MetamodeledEntityMetaModel;
import ua.com.fielden.platform.processors.test_entities.meta.MetamodeledEntityMetaModelAliased;
import ua.com.fielden.platform.processors.test_entities.meta.SubEntityMetaModel;
import ua.com.fielden.platform.processors.test_entities.meta.SubEntityMetaModelAliased;
import ua.com.fielden.platform.processors.test_entities.meta.SuperEntityMetaModel;
import ua.com.fielden.platform.processors.test_utils.ProcessingRule;
import ua.com.fielden.platform.utils.CollectionUtil;
import ua.com.fielden.platform.utils.Pair;

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
        final Elements elements = rule.getElements();
        final Types types = rule.getTypes();
        finder = new ElementFinder(elements, types);
        entityFinder = new EntityFinder(elements, types);
        metaModelFinder = new MetaModelFinder(elements, types);
    }

    @Test
    public void isMetaModel_returns_true_if_the_type_element_represents_a_meta_model() {
        assertFalse(metaModelFinder.isMetaModel(finder.getTypeElement(String.class)));
        assertFalse(metaModelFinder.isMetaModel(finder.getTypeElement(MetamodeledEntity.class)));
        assertTrue(metaModelFinder.isMetaModel(finder.getTypeElement(MetamodeledEntityMetaModel.class)));
        assertTrue(metaModelFinder.isMetaModel(finder.getTypeElement(MetamodeledEntityMetaModelAliased.class)));
    }

    @Test
    public void isMetaModelAliased_returns_true_if_the_element_represents_an_alised_meta_model() {
        final MetaModelElement mmElt = metaModelFinder.findMetaModel(MetamodeledEntityMetaModel.class);
        assertFalse(metaModelFinder.isMetaModelAliased(mmElt));
        final MetaModelElement mmAliasedElt = metaModelFinder.findMetaModel(MetamodeledEntityMetaModelAliased.class);
        assertTrue(metaModelFinder.isMetaModelAliased(mmAliasedElt));
    }

    @Test
    public void findMetaModel_finds_the_meta_model_element_by_class() {
        final MetaModelElement elt = metaModelFinder.findMetaModel(MetamodeledEntityMetaModel.class);
        assertTrue(finder.isSameType(elt.asType(), MetamodeledEntityMetaModel.class));
        assertFalse(finder.isSameType(elt.asType(), MetamodeledEntityMetaModelAliased.class));
    }

    @Test
    public void listMetaModelHierarchy_returns_a_hierarchy_of_meta_model_elements() {
        final BiConsumer<Stream<Class<? extends EntityMetaModel>>, Class<? extends EntityMetaModel>> assertor = 
                (expectedHirerachy, metaModel) -> assertEquals(
                        expectedHirerachy.map(c -> metaModelFinder.findMetaModel(c)).toList(),
                        metaModelFinder.listMetaModelHierarchy(metaModelFinder.findMetaModel(metaModel)));

        // simplest hierarchy of 1 meta-model
        assertor.accept(Stream.of(MetamodeledEntityMetaModel.class), MetamodeledEntityMetaModel.class);
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
    public void findMetaModelForEntity_finds_a_meta_model_for_a_metamodeled_entity() {
        final EntityElement metamodeledEntityElt = entityFinder.findEntity(MetamodeledEntity.class);
        metaModelFinder.findMetaModelForEntity(metamodeledEntityElt).ifPresentOrElse(metaModelElt -> {
            final Optional<EntityElement> maybeEntityForMetaModel = entityFinder.findEntityForMetaModel(metaModelElt);
            assertTrue(maybeEntityForMetaModel.isPresent());
            assertTrue(finder.types.isSameType(metamodeledEntityElt.asType(), maybeEntityForMetaModel.get().asType()));
        }, () -> fail("Meta-model was not found"));
    }

    @Test
    public void findMetaModelForEntity_does_not_find_a_meta_model_for_a_non_metamodeled_entity() {
        final EntityElement nonMetamodeledEntityElt = entityFinder.findEntity(EntityWithDescTitleWithoutMetaModel.class);
        assertTrue(metaModelFinder.findMetaModelForEntity(nonMetamodeledEntityElt).isEmpty());
    }

    private static void assertEqualContents(final Collection<?> c1, final Collection<?> c2) {
        if (CollectionUtil.isEqualContents(c1, c2)) {}
        else {
            fail("expected:<%s> but was:<%s>".formatted(CollectionUtil.toString(c1, ", "), CollectionUtil.toString(c2, ", ")));
        }
    }

}
