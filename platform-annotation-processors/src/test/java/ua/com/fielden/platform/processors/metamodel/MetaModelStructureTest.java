package ua.com.fielden.platform.processors.metamodel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.JavaFileObject;

import org.apache.commons.lang3.StringUtils;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import com.google.testing.compile.JavaFileObjects;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.processors.metamodel.elements.EntityElement;
import ua.com.fielden.platform.processors.metamodel.elements.MetaModelElement;
import ua.com.fielden.platform.processors.metamodel.elements.PropertyElement;
import ua.com.fielden.platform.processors.metamodel.models.PropertyMetaModel;
import ua.com.fielden.platform.processors.metamodel.test_entities.TestEntityAdjacentToOtherEntities;
import ua.com.fielden.platform.processors.metamodel.test_entities.TestEntityChild;
import ua.com.fielden.platform.processors.metamodel.test_entities.TestEntityNotPersistent;
import ua.com.fielden.platform.processors.metamodel.test_entities.TestEntityParent;
import ua.com.fielden.platform.processors.metamodel.test_entities.TestEntityPersistent;
import ua.com.fielden.platform.processors.metamodel.test_entities.TestEntitySinkNodesOnly;
import ua.com.fielden.platform.processors.metamodel.test_entities.TestEntityWithDescTitle;
import ua.com.fielden.platform.processors.metamodel.test_entities.TestEntityWithoutDescTitle;
import ua.com.fielden.platform.processors.metamodel.utils.ElementFinder;
import ua.com.fielden.platform.processors.metamodel.utils.EntityFinder;
import ua.com.fielden.platform.processors.metamodel.utils.MetaModelFinder;
import ua.com.fielden.platform.processors.test_utils.CompilationRule;


/**
 * Tests that verify the structure of the generated meta-models that are based on a set of categories for structural representation of entities.
 * <p>
 * A setup must be performed before the tests are run in order to generate the meta-models by compiling the input entities.
 * Java sources for the entities themselves must be placed in the {@code src/test/resources} directory so that they are not compiled by default. We want to compile them manually, storing the result in memory, and process them with the {@link MetaModelProcessor}. This directory needs to be included in the build path (classpath).
 * 
 * @author TG Team
 */
public class MetaModelStructureTest {
    private final static String TEST_ENTITIES_PKG_NAME = "ua.com.fielden.platform.processors.metamodel.test_entities";
    
    // this class rule compiles test entities and then executes all tests during the last round of processing so that instances of Elements and Types are available in those tests
    @ClassRule
    public static CompilationRule rule = new CompilationRule(getTestEntities(), new MetaModelProcessor());
    public static Elements elements;
    public static Types types;

    @BeforeClass
    public static void setupOnce() {
        // these values are guaranteed to have been initialized since the class rule will evaluate this method during the last round of processing
        elements = rule.getElements();
        types = rule.getTypes();
    }
    
    @Test
    public void entity_annotated_with_DescTitle_should_have_property_desc_metamodeled() {
        final EntityElement entityWithDesc = findEntity(TestEntityWithDescTitle.class);
        final MetaModelElement metaModelWithDesc = findMetaModel(entityWithDesc);

        // Meta-model for TestEntityWithDescTitle should have method desc()
        assertTrue(MetaModelFinder.findPropertyMethods(metaModelWithDesc, types).stream()
                .anyMatch(el -> StringUtils.equals(el.getSimpleName(), "desc")));


        final EntityElement entityWithoutDesc = findEntity(TestEntityWithoutDescTitle.class);
        final MetaModelElement metaModelWithoutDesc = findMetaModel(entityWithoutDesc);

        // Meta-model for TestEntityWithoutDescTitle should NOT have method desc()
        assertTrue(MetaModelFinder.findPropertyMethods(metaModelWithoutDesc, types).stream()
                .noneMatch(el -> StringUtils.equals(el.getSimpleName(), "desc")));
    }
    
    @Test
    public void entity_with_sink_node_properties_only_should_have_all_properties_metamodeled_with_PropertyMetaModel() {
        final EntityElement entity = findEntity(TestEntitySinkNodesOnly.class);
        final MetaModelElement metaModel = findMetaModel(entity);
        
        // find all distinct return types of methods that model properies of an underlying entity
        // there should be only one such type - PropertyMetaModel
        final List<TypeMirror> distinctReturnTypes = MetaModelFinder.findPropertyMethods(metaModel, types).stream()
            .map(ExecutableElement::getReturnType)
            .distinct()
            .toList();
        assertEquals(1, distinctReturnTypes.size());
        assertTrue(ElementFinder.isSubtype(distinctReturnTypes.get(0), PropertyMetaModel.class, types));
    }

    /**
     * If a metamodeled entity has properties of metamodeled entity types, then the generated meta-model should capture these relationships modeled by properties of corresponding meta-model types.
     */
    @Test
    public void entity_adjacent_to_other_metamodeled_entities_should_have_properties_metamodeled_with_EntityMetaModel() {
        final EntityElement entity = findEntity(TestEntityAdjacentToOtherEntities.class);
        final MetaModelElement metaModel = findMetaModel(entity);

        final Set<ExecutableElement> metamodeledProps = MetaModelFinder.findPropertyMethods(metaModel, types);
        for (final PropertyElement prop: EntityFinder.findProperties(entity)) {
            // find the metamodeled prop
            // TODO the logic handling transformations between entity properties and meta-model properties should be abstracted
            // consider that transformation of names changes, then this code would have to be modified too
            final Optional<ExecutableElement> maybeMetamodeledProp = metamodeledProps.stream().filter(el -> el.getSimpleName().toString().equals(prop.getName())).findAny();
            assertTrue(maybeMetamodeledProp.isPresent());
            final ExecutableElement metamodeledProp = maybeMetamodeledProp.get();

            if (prop.hasClassOrInterfaceType() && EntityFinder.isEntityThatNeedsMetaModel(prop.getTypeAsTypeElementOrThrow())) {
                assertTrue(MetaModelFinder.isEntityMetaModelMethod(metamodeledProp, types));
            }
            else {
                assertTrue(MetaModelFinder.isPropertyMetaModelMethod(metamodeledProp));
            }
        }
    }

    /**
     * Meta-model of an entity (Child) that extends another metamodeled entity (Parent) should model the hierarchy in a similar way.
     * <p>
     * <ul>
     * <li>Child's meta-model directly extends Parent's meta-model</li>
     * <li>Only declared properties of Child are explicitly metamodeled.</li>
     * </ul>
     */
    @Test
    public void meta_model_of_child_entity_extends_meta_model_of_parent_entity_and_metamodels_only_declared_properties() {
        // find Child
        final EntityElement child = findEntity(TestEntityChild.class);
        final MetaModelElement childMetaModel = findMetaModel(child);
        // find Parent
        final EntityElement parent = findEntity(TestEntityParent.class);
        final MetaModelElement parentMetaModel = findMetaModel(parent);

        // Child's meta-model extends Parent's meta-model ?
        assertTrue(types.isSameType(childMetaModel.getTypeElement().getSuperclass(), parentMetaModel.getTypeElement().asType()));
        
        final Set<PropertyElement> childDeclaredProps = EntityFinder.findDeclaredProperties(child);
        final Set<ExecutableElement> childDeclaredMetamodeledProps = MetaModelFinder.findDeclaredPropertyMethods(childMetaModel, types);
        assertEquals(childDeclaredProps.size(), childDeclaredMetamodeledProps.size());

        for (final PropertyElement prop: childDeclaredProps) {
            // find the metamodeled prop by name
            final Optional<ExecutableElement> maybeMetamodeledProp = childDeclaredMetamodeledProps.stream().filter(el -> el.getSimpleName().toString().equals(prop.getName())).findAny();
            assertTrue(maybeMetamodeledProp.isPresent());
            final ExecutableElement metamodeledProp = maybeMetamodeledProp.get();

            // TODO make sure that property types are consistent
            // for example, consider a case when a child entity redeclares a field with a different type
            // right now the information about the original property's type is stored in the javadoc
            // for PropertyMetaModel methods it is impossible to test the consistency of types, since javax.lang.model API discards javadoc
            if (prop.hasClassOrInterfaceType() && EntityFinder.isEntityThatNeedsMetaModel(prop.getTypeAsTypeElementOrThrow())) {
                assertTrue(MetaModelFinder.isEntityMetaModelMethod(metamodeledProp, types));
            }
            else {
                assertTrue(MetaModelFinder.isPropertyMetaModelMethod(metamodeledProp));
            }
        }
    }
    
    @Test
    public void only_persistent_entities_have_property_id_metamodeled() {
        final EntityElement entityPersistent = findEntity(TestEntityPersistent.class);
        final MetaModelElement persistentMetaModel = findMetaModel(entityPersistent);
        // make sure property "id" is metamodeled
        assertTrue(MetaModelFinder.findPropertyMethods(persistentMetaModel, types).stream()
            .anyMatch(el -> el.getSimpleName().toString().equals("id")));

        final EntityElement entityNotPersistent = findEntity(TestEntityNotPersistent.class);
        final MetaModelElement notPersistentMetaModel = findMetaModel(entityNotPersistent);
        // make sure property "id" is not metamodeled
        assertTrue(MetaModelFinder.findPropertyMethods(notPersistentMetaModel, types).stream()
            .noneMatch(el -> el.getSimpleName().toString().equals("id")));
    }

    // ============================ HELPER METHODS ============================
    
    private static List<JavaFileObject> getTestEntities() {
        final String pkgNameSlashed = StringUtils.replaceChars(TEST_ENTITIES_PKG_NAME, '.', '/');
        final URL entitiesPkgUrl = ClassLoader.getSystemResource(pkgNameSlashed);
        if (entitiesPkgUrl == null) {
            // What action should be taken if the package with test entities wasn't found?
            // Should this throw a runtime exception?
            throw new IllegalStateException(String.format("%s package not found.", TEST_ENTITIES_PKG_NAME));
        }

        // find all *.java files inside the package
        return Stream.of(new File(entitiesPkgUrl.getFile()).listFiles())
                .map(File::getName)
                .filter(filename -> StringUtils.endsWith(filename, ".java"))
                .map(filename -> JavaFileObjects.forResource(String.format("%s/%s", pkgNameSlashed, filename)))
                .toList();
    }
    
    /**
     * Wraps a call to {@link EntityFinder#findEntity} that returns an optional in order to assert the presence of the returned value. 
     */
    private static EntityElement findEntity(final Class<? extends AbstractEntity<?>> entityType) {
        final Optional<EntityElement> maybeEntity = EntityFinder.findEntity(entityType, elements);
        assertTrue(maybeEntity.isPresent());
        return maybeEntity.get();
    }
    
    /**
     * Wraps a call to {@link MetaModelFinder#findMetaModelForEntity} that returns an optional in order to assert the presence of the returned value. 
     */
    private static MetaModelElement findMetaModel(final EntityElement entityElement) {
        final Optional<MetaModelElement> maybeMetaModel = MetaModelFinder.findMetaModelForEntity(entityElement, elements);
        assertTrue(maybeMetaModel.isPresent());
        return maybeMetaModel.get();
    }
    
}