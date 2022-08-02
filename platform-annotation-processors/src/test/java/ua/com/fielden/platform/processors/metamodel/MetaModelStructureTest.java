package ua.com.fielden.platform.processors.metamodel;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.processors.metamodel.MetaModelConstants.META_MODEL_ALIASED_NAME_SUFFIX;
import static ua.com.fielden.platform.processors.metamodel.MetaModelConstants.META_MODEL_NAME_SUFFIX;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

import org.apache.commons.lang3.StringUtils;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.processors.metamodel.elements.EntityElement;
import ua.com.fielden.platform.processors.metamodel.elements.MetaModelElement;
import ua.com.fielden.platform.processors.metamodel.elements.PropertyElement;
import ua.com.fielden.platform.processors.metamodel.models.PropertyMetaModel;
import ua.com.fielden.platform.processors.metamodel.utils.ElementFinder;
import ua.com.fielden.platform.processors.metamodel.utils.EntityFinder;
import ua.com.fielden.platform.processors.metamodel.utils.MetaModelFinder;
import ua.com.fielden.platform.processors.test_entities.AdjacentToOtherEntities;
import ua.com.fielden.platform.processors.test_entities.Child;
import ua.com.fielden.platform.processors.test_entities.NotPersistent;
import ua.com.fielden.platform.processors.test_entities.Parent;
import ua.com.fielden.platform.processors.test_entities.Persistent;
import ua.com.fielden.platform.processors.test_entities.SinkNodesOnly;
import ua.com.fielden.platform.processors.test_entities.WithDescTitle;
import ua.com.fielden.platform.processors.test_entities.WithoutDescTitle;
import ua.com.fielden.platform.processors.test_utils.CompilationRule;


/**
 * Tests that verify the structure of the generated meta-models that are based on a set of categories for structural representation of entities.
 * 
 * @author TG Team
 */
public class MetaModelStructureTest {
    public static final String TEST_ENTITIES_PKG_NAME = "ua.com.fielden.platform.processors.test_entities";
    public static final String TEST_META_MODELS_PKG_NAME = TEST_ENTITIES_PKG_NAME + MetaModelConstants.META_MODEL_PKG_NAME_SUFFIX;

    @ClassRule
    public static CompilationRule rule = new CompilationRule(List.of(), new MetaModelProcessor());
    private static ElementFinder elementFinder;
    private static EntityFinder entityFinder;
    private static MetaModelFinder metaModelFinder;
    
    @BeforeClass
    public static void setupOnce() {
        // these values are guaranteed to have been initialized since the class rule will evaluate this method during the last round of processing
        elementFinder = new ElementFinder(rule.getElements(), rule.getTypes());
        entityFinder = new EntityFinder(rule.getElements(), rule.getTypes());
        metaModelFinder = new MetaModelFinder(rule.getElements(), rule.getTypes());
        validateSetup();
    }
    
    private static void validateSetup() {
        if (elementFinder.getElements().getPackageElement(TEST_ENTITIES_PKG_NAME) == null) {
            throw new RuntimeException("Package with test entities wasn't found");
        }
        if (elementFinder.getElements().getPackageElement(TEST_META_MODELS_PKG_NAME) == null) {
            throw new RuntimeException("Package with test meta-models wasn't found");
        }
    }

    @Test
    public void entity_annotated_with_DescTitle_has_property_desc_metamodeled() {
        final EntityElement entityWithDesc = findEntity(WithDescTitle.class);
        final MetaModelElement metaModelWithDesc = findMetaModel(entityWithDesc);

        // Meta-model for TestEntityWithDescTitle has method desc()
        assertTrue(metaModelFinder.findPropertyMethods(metaModelWithDesc).stream()
                .anyMatch(el -> StringUtils.equals(el.getSimpleName(), "desc")));


        final EntityElement entityWithoutDesc = findEntity(WithoutDescTitle.class);
        final MetaModelElement metaModelWithoutDesc = findMetaModel(entityWithoutDesc);

        // Meta-model for TestEntityWithoutDescTitle does NOT have method desc()
        assertTrue(metaModelFinder.findPropertyMethods(metaModelWithoutDesc).stream()
                .noneMatch(el -> StringUtils.equals(el.getSimpleName(), "desc")));
    }
    
    @Test
    public void entity_with_sink_node_properties_only_has_all_properties_metamodeled_with_PropertyMetaModel() {
        final EntityElement entity = findEntity(SinkNodesOnly.class);
        final MetaModelElement metaModel = findMetaModel(entity);
        
        // find all distinct return types of methods that model properies of an underlying entity
        // there must be only one such type - PropertyMetaModel
        final List<TypeMirror> distinctReturnTypes = metaModelFinder.findPropertyMethods(metaModel).stream()
            .map(ExecutableElement::getReturnType)
            .distinct()
            .toList();
        assertEquals(1, distinctReturnTypes.size());
        assertTrue(elementFinder.isSubtype(distinctReturnTypes.get(0), PropertyMetaModel.class));
    }

    /**
     * If a metamodeled entity has properties of metamodeled entity types, then the generated meta-model captures these relationships modeled by properties of corresponding meta-model types.
     */
    @Test
    public void entity_adjacent_to_other_metamodeled_entities_has_properties_metamodeled_with_EntityMetaModel() {
        final EntityElement entity = findEntity(AdjacentToOtherEntities.class);
        final MetaModelElement metaModel = findMetaModel(entity);

        final Set<ExecutableElement> metamodeledProps = metaModelFinder.findPropertyMethods(metaModel);
        for (final PropertyElement prop: entityFinder.findProperties(entity)) {
            // find the metamodeled prop
            // TODO the logic handling transformations between entity properties and meta-model properties should be abstracted
            // consider that transformation of names changes, then this code would have to be modified too
            final Optional<ExecutableElement> maybeMetamodeledProp = metamodeledProps.stream().filter(el -> el.getSimpleName().toString().equals(prop.getSimpleName().toString())).findAny();
            assertTrue(maybeMetamodeledProp.isPresent());
            final ExecutableElement metamodeledProp = maybeMetamodeledProp.get();

            if (prop.hasClassOrInterfaceType() && entityFinder.isEntityThatNeedsMetaModel(prop.getTypeAsTypeElementOrThrow())) {
                assertTrue(metaModelFinder.isEntityMetaModelMethod(metamodeledProp));
            }
            else {
                assertTrue(metaModelFinder.isPropertyMetaModelMethod(metamodeledProp));
            }
        }
    }

    /**
     * Meta-model of an entity (Child) that extends another metamodeled entity (Parent) models the hierarchy in a similar way.
     * <p>
     * <ul>
     * <li>Child's meta-model directly extends Parent's meta-model</li>
     * <li>Only declared properties of Child are explicitly metamodeled.</li>
     * </ul>
     */
    @Test
    public void meta_model_of_child_entity_extends_meta_model_of_parent_entity_and_metamodels_only_declared_properties() {
        // find Child
        final EntityElement child = findEntity(Child.class);
        final MetaModelElement childMetaModel = findMetaModel(child);
        // find Parent
        final EntityElement parent = findEntity(Parent.class);
        final MetaModelElement parentMetaModel = findMetaModel(parent);

        // Child's meta-model extends Parent's meta-model ?
        assertTrue(elementFinder.getTypes().isSameType(childMetaModel.getSuperclass(), parentMetaModel.asType()));
        
        final Set<PropertyElement> childDeclaredProps = entityFinder.findDeclaredProperties(child);
        final Set<ExecutableElement> childDeclaredMetamodeledProps = metaModelFinder.findDeclaredPropertyMethods(childMetaModel);
        assertEquals(childDeclaredProps.size(), childDeclaredMetamodeledProps.size());

        for (final PropertyElement prop: childDeclaredProps) {
            // find the metamodeled prop by name
            final Optional<ExecutableElement> maybeMetamodeledProp = childDeclaredMetamodeledProps.stream().filter(el -> el.getSimpleName().toString().equals(prop.getSimpleName().toString())).findAny();
            assertTrue(maybeMetamodeledProp.isPresent());
            final ExecutableElement metamodeledProp = maybeMetamodeledProp.get();

            // TODO make sure that property types are consistent
            // for example, consider a case when a child entity redeclares a field with a different type
            // right now the information about the original property's type is stored in the javadoc
            // for PropertyMetaModel methods it is impossible to test the consistency of types, since javax.lang.model API discards javadoc
            if (prop.hasClassOrInterfaceType() && entityFinder.isEntityThatNeedsMetaModel(prop.getTypeAsTypeElementOrThrow())) {
                assertTrue(metaModelFinder.isEntityMetaModelMethod(metamodeledProp));
            }
            else {
                assertTrue(metaModelFinder.isPropertyMetaModelMethod(metamodeledProp));
            }
        }
    }
    
    @Test
    public void only_persistent_entities_have_property_id_metamodeled() {
        final EntityElement entityPersistent = findEntity(Persistent.class);
        final MetaModelElement persistentMetaModel = findMetaModel(entityPersistent);
        // make sure property "id" is metamodeled
        assertTrue(metaModelFinder.findPropertyMethods(persistentMetaModel).stream()
            .anyMatch(el -> el.getSimpleName().toString().equals("id")));

        final EntityElement entityNotPersistent = findEntity(NotPersistent.class);
        final MetaModelElement notPersistentMetaModel = findMetaModel(entityNotPersistent);
        // make sure property "id" is not metamodeled
        assertTrue(metaModelFinder.findPropertyMethods(notPersistentMetaModel).stream()
            .noneMatch(el -> el.getSimpleName().toString().equals("id")));
    }
    
    /**
     * For a metamodeled entity there are 2 meta-model forms that get generated. One of them is an aliased meta-model.
     * <p>
     * An aliased meta-model extends a regular one.
     */
    @Test
    public void aliased_meta_model_extends_a_regular_one() {
        final Elements elements = elementFinder.getElements();
        final List<MetaModelElement> aliasedMetaModels = elements.getPackageElement(TEST_META_MODELS_PKG_NAME).getEnclosedElements().stream()
                .filter(el -> el.getKind() == ElementKind.CLASS)
                .map(el -> (TypeElement) el)
                .filter(metaModelFinder::isMetaModel)
                .map(te -> metaModelFinder.newMetaModelElement(te))
                .filter(metaModelFinder::isMetaModelAliased)
                .toList();
        assertFalse(aliasedMetaModels.isEmpty());

        aliasedMetaModels.stream()
            .forEach(mme -> {
                final TypeElement superclass = elementFinder.getSuperclassOrNull(mme);
                assertNotNull(superclass);
                assertTrue(metaModelFinder.isMetaModel(superclass));
                // superclass name = name - "MetaModelAliased" + "MetaModel" = name - "Aliased"
                final String supposedName = format("%s.%s%s", mme.getPackageName(),
                        StringUtils.substringBeforeLast(mme.getSimpleName().toString(), META_MODEL_ALIASED_NAME_SUFFIX), 
                        META_MODEL_NAME_SUFFIX);
                assertEquals(supposedName, superclass.getQualifiedName().toString());
            });
    }
    
    /**
     * For a metamodeled entity there are 2 meta-model forms that get generated. One of them is an aliased meta-model.
     * <p>
     * An alised meta-model provides a public read-only field <code>alias</code> of type {@link String}.
     */
    @Test
    public void aliased_meta_model_provides_public_read_only_alias_String() {
        final Elements elements = elementFinder.getElements();
        final List<MetaModelElement> aliasedMetaModels = elements.getPackageElement(TEST_META_MODELS_PKG_NAME).getEnclosedElements().stream()
                .filter(el -> el.getKind() == ElementKind.CLASS)
                .map(el -> (TypeElement) el)
                .filter(metaModelFinder::isMetaModel)
                .map(te -> metaModelFinder.newMetaModelElement(te))
                .filter(metaModelFinder::isMetaModelAliased)
                .toList();
        assertFalse(aliasedMetaModels.isEmpty());
        
        aliasedMetaModels.stream()
            .forEach(mme -> {
                assertNotNull(elementFinder.findDeclaredField(mme, "alias", 
                        varEl -> varEl.getModifiers().containsAll(List.of(Modifier.PUBLIC, Modifier.FINAL)) &&
                        elementFinder.isFieldOfType(varEl, String.class)));
            });
    }

    // ============================ HELPER METHODS ============================
    /**
     * Wraps a call to {@link EntityFinder#findEntity} that returns an optional in order to assert the presence of the returned value. 
     */
    private static EntityElement findEntity(final Class<? extends AbstractEntity<?>> entityType) {
        final Optional<EntityElement> maybeEntity = entityFinder.findEntity(entityType);
        assertTrue(maybeEntity.isPresent());
        return maybeEntity.get();
    }
    
    /**
     * Wraps a call to {@link MetaModelFinder#findMetaModelForEntity} that returns an optional in order to assert the presence of the returned value. 
     */
    private static MetaModelElement findMetaModel(final EntityElement entityElement) {
        final Optional<MetaModelElement> maybeMetaModel = metaModelFinder.findMetaModelForEntity(entityElement);
        assertTrue(maybeMetaModel.isPresent());
        return maybeMetaModel.get();
    }
    
}