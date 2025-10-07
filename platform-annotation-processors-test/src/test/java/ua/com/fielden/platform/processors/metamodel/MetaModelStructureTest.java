package ua.com.fielden.platform.processors.metamodel;

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
import ua.com.fielden.platform.processors.test_entities.*;
import ua.com.fielden.platform.processors.test_entities.meta.EntityWithOrdinaryPropsMetaModel;
import ua.com.fielden.platform.processors.test_entities.meta.SubEntityMetaModel;
import ua.com.fielden.platform.processors.test_utils.ProcessingRule;
import ua.com.fielden.platform.processors.test_utils.exceptions.TestCaseConfigException;
import ua.com.fielden.platform.reflection.AnnotationReflector;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.processors.metamodel.MetaModelConstants.META_MODEL_ALIASED_NAME_SUFFIX;
import static ua.com.fielden.platform.processors.metamodel.MetaModelConstants.META_MODEL_NAME_SUFFIX;
import static ua.com.fielden.platform.processors.metamodel.utils.ElementFinder.TYPE_ELEMENT_FILTER;

/**
 * Tests that verify the structure of the generated meta-models that are based on a set of categories for structural representation of entities.
 * 
 * @author TG Team
 */
public class MetaModelStructureTest {
    public static final String TEST_ENTITIES_PKG_NAME = "ua.com.fielden.platform.processors.test_entities";
    public static final String TEST_META_MODELS_PKG_NAME = TEST_ENTITIES_PKG_NAME + MetaModelConstants.META_MODEL_PKG_NAME_SUFFIX;

    @ClassRule
    public static ProcessingRule rule = new ProcessingRule(List.of(), new MetaModelProcessor());
    private static ElementFinder elementFinder;
    private static EntityFinder entityFinder;
    private static MetaModelFinder metaModelFinder;
    private static Types typeUtils;
    
    @BeforeClass
    public static void setupOnce() {
        // these values are guaranteed to have been initialized since the class rule will evaluate this method during the last round of processing
        typeUtils = rule.getTypes();
        elementFinder = new ElementFinder(rule.getProcessingEnvironment());
        entityFinder = new EntityFinder(rule.getProcessingEnvironment());
        metaModelFinder = new MetaModelFinder(rule.getProcessingEnvironment());
        validateSetup();
    }
    
    private static void validateSetup() {
        if (elementFinder.elements.getPackageElement(TEST_ENTITIES_PKG_NAME) == null) {
            throw new TestCaseConfigException("Package with test entities wasn't found");
        }
        if (elementFinder.elements.getPackageElement(TEST_META_MODELS_PKG_NAME) == null) {
            throw new TestCaseConfigException("Package with test meta-models wasn't found");
        }
    }

    // >>>>>>>>>>>>>>>>>>>> METAMODELING OF PROPERTY `desc` >>>>>>>>>>>>>>>>>>>>
    /* A meta-model should include property `desc` in the following cases:
     * 1. Entity type or one of its supertypes is annotated with @DescTitle
     * 2. `desc` is declared by an entity type or any of its super types, with the exclusion of `AbstractEntity`
     * 
     * Reference: https://github.com/fieldenms/tg/issues/1898 */

    @Test
    public void entity_annotated_with_DescTitle_has_property_desc_metamodeled() {
        // 1. top-level entity annotated with @DescTitle
        final EntityElement entityWithDesc = entityFinder.findEntity(EntityWithDescTitle.class);
        final MetaModelElement metaModelWithDesc = findMetaModel(entityWithDesc);

        assertTrue(metaModelFinder.findPropertyMethods(metaModelWithDesc).stream()
                .anyMatch(el -> el.getSimpleName().toString().equals(AbstractEntity.DESC)));

        // 2. metamodeled entity without @DescTitle, but superclass with @DescTitle and not metamodeled
        final EntityElement subEntityWithoutDesc = entityFinder.findEntity(EntityWithoutDescTitle_extends_EntityWithDescTitleWithoutMetaModel.class);
        final MetaModelElement subEntityWithoutDescMetaModel = findMetaModel(subEntityWithoutDesc);

        assertTrue(metaModelFinder.findPropertyMethods(subEntityWithoutDescMetaModel).stream()
                .anyMatch(el -> el.getSimpleName().toString().equals(AbstractEntity.DESC)));
    }

    @Test
    public void entity_declaring_property_desc_has_it_metamodeled() {
        final EntityElement entityWithPropDesc = entityFinder.findEntity(EntityWithPropertyDesc.class);
        final MetaModelElement entityWithPropDescMetaModel = findMetaModel(entityWithPropDesc);

        assertTrue(metaModelFinder.findPropertyMethods(entityWithPropDescMetaModel).stream()
                .anyMatch(el -> el.getSimpleName().toString().equals(AbstractEntity.DESC)));
    }

    @Test
    public void desc_is_metamodeled_for_entity_with_supertype_below_AbstractEntity_that_declares_property_desc() {
        final EntityElement entity = entityFinder.findEntity(EntityWithoutDescTitleAndPropertyDesc_extends_EntityWithPropertyDescWithoutMetaModel.class);
        final MetaModelElement metaModel = findMetaModel(entity);

        assertTrue(metaModelFinder.findPropertyMethods(metaModel).stream()
                .anyMatch(el -> el.getSimpleName().toString().equals(AbstractEntity.DESC)));
    }

    @Test
    public void entity_without_DescTitle_and_property_desc_does_not_have_property_desc_metamodeled() {
        final EntityElement entityWithoutDesc = entityFinder.findEntity(EntityWithoutDescTitleAndPropertyDesc.class);
        final MetaModelElement metaModelWithoutDesc = findMetaModel(entityWithoutDesc);

        assertTrue(metaModelFinder.findPropertyMethods(metaModelWithoutDesc).stream()
                .noneMatch(el -> el.getSimpleName().toString().equals(AbstractEntity.DESC)));
    }
    // <<<<<<<<<<<<<<<<<<<< METAMODELING OF PROPERTY `desc` <<<<<<<<<<<<<<<<<<<<

    @Test
    public void entity_with_entity_type_as_key_type_has_property_key_metamodeled_with_entity_meta_model_type() {
        final EntityElement entity = entityFinder.findEntity(EntityWithKeyTypeOfEntityType.class);
        final MetaModelElement metaModel = findMetaModel(entity);
        
        final ExecutableElement keyMethod = metaModelFinder.findDeclaredPropertyMethod(metaModel, AbstractEntity.KEY)
                .orElseThrow(() -> new AssertionError("Property [key] must be metamodeled."));
        assertTrue("Property [key] must be metamodeled with entity meta-model type.", metaModelFinder.isEntityMetaModelMethod(keyMethod));
        
        final Class<?> declaredKeyType = AnnotationReflector.getKeyType(EntityWithKeyTypeOfEntityType.class);

        // return type of keyMethod must be a meta-model for declaredKeyType
        final MetaModelElement expected = findMetaModel(entityFinder.findEntity((Class<? extends AbstractEntity<?>>) declaredKeyType));
        assertTrue("%s.key must be metamodeled with %s.".formatted(EntityWithKeyTypeOfEntityType.class.getSimpleName(), expected.getSimpleName()),
                typeUtils.isSameType(keyMethod.getReturnType(), expected.asType()));
    }
    
    @Test
    public void entity_with_NoType_as_key_type_does_not_have_property_key_metamodeled() {
        final EntityElement entity = entityFinder.findEntity(EntityWithKeyTypeNoKey.class);
        final MetaModelElement metaModel = findMetaModel(entity);
        
        assertTrue("Property [key] must not be metamodeled.", metaModelFinder.findDeclaredPropertyMethod(metaModel, AbstractEntity.KEY).isEmpty());
    }
    
    @Test
    public void ordinary_properties_are_metamodeled_with_PropertyMetaModel() {
        final EntityElement entity = entityFinder.findEntity(EntityWithOrdinaryProps.class);
        final MetaModelElement metaModel = findMetaModel(entity);
        
        // find all distinct return types of methods that model properties of an underlying entity
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
    public void entity_typed_properties_are_metamodeled_with_EntityMetaModel() {
        final EntityElement entity = entityFinder.findEntity(EntityWithEntityTypedAndOrdinaryProps.class);
        final MetaModelElement metaModel = findMetaModel(entity);

        for (final PropertyElement prop: entityFinder.findProperties(entity)) {
            // find the metamodeled prop
            final Optional<ExecutableElement> maybeMetamodeledProp = metaModelFinder.findPropertyMethod(metaModel, prop.getSimpleName().toString());
            assertTrue("Property \"%s\" was not metamodeled.".formatted(prop.getSimpleName()), maybeMetamodeledProp.isPresent());

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
     * Meta-model of an entity (SubEntity) that extends another metamodeled entity (SuperEntity) models the hierarchy in a similar way.
     * <p>
     * <ul>
     * <li>SubEntity's meta-model directly extends SuperEntity's meta-model</li>
     * <li>Only declared properties of SubEntity + property {@code key} are explicitly meta-modelled.</li>
     * </ul>
     */
    @Test
    public void meta_model_of_sub_entity_extends_meta_model_of_super_entity_and_metamodels_only_declared_properties() {
        // find SubEntity
        final EntityElement subEntity = entityFinder.findEntity(SubEntity.class);
        final MetaModelElement subEntityMetaModel = findMetaModel(subEntity);
        // find SuperEntity
        final EntityElement superEntity = entityFinder.findEntity(SuperEntity.class);
        final MetaModelElement superEntityMetaModel = findMetaModel(superEntity);

        // SubEntity's meta-model extends SuperEntity's meta-model ?
        assertTrue(elementFinder.types.isSameType(subEntityMetaModel.getSuperclass(), superEntityMetaModel.asType()));
        
        final List<PropertyElement> subEntityDeclaredProps = entityFinder.findDeclaredProperties(subEntity);
        final List<ExecutableElement> subEntityDeclaredMetamodeledProps = metaModelFinder.findDeclaredPropertyMethods(subEntityMetaModel);
        // +3 for properties "key", "id" and "desc"
        assertEquals(subEntityDeclaredProps.size() + 3, subEntityDeclaredMetamodeledProps.size());

        for (final PropertyElement prop: subEntityDeclaredProps) {
            // find the metamodeled prop by name
            final Optional<ExecutableElement> maybeMetamodeledProp = metaModelFinder.findPropertyMethod(subEntityDeclaredMetamodeledProps, prop.getSimpleName().toString());
            assertTrue(maybeMetamodeledProp.isPresent());
            final ExecutableElement metamodeledProp = maybeMetamodeledProp.get();

            // TODO make sure that property types are consistent
            // for example, consider a case when a sub-entity redeclares a field with a different type
            // right now the information about the original property's type is stored in the javadoc
            // There is Elements#getDocComment(Element), which presumably returns the element's javadoc.
            // However, it has only been returning null so far.
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
        final EntityElement entityPersistent = entityFinder.findEntity(PersistentEntity.class);
        final MetaModelElement persistentMetaModel = findMetaModel(entityPersistent);
        // make sure property "id" is metamodeled
        assertTrue(metaModelFinder.findPropertyMethods(persistentMetaModel).stream()
            .anyMatch(el -> ID.equals(el.getSimpleName().toString())));

        final EntityElement domainNotPersistentEntity = entityFinder.findEntity(NonPersistentButDomainEntity.class);
        final MetaModelElement domainNotPersistentMetaModel = findMetaModel(domainNotPersistentEntity);
        // make sure property "id" is not metamodeled
        assertTrue(metaModelFinder.findPropertyMethods(domainNotPersistentMetaModel).stream()
            .noneMatch(el -> ID.equals(el.getSimpleName().toString())));

        final EntityElement notDomainAndNotPersistent = entityFinder.findEntity(NonPersistentButWithMetaModelEntity.class);
        final MetaModelElement notDomainAndNotPersistentMetaModel = findMetaModel(notDomainAndNotPersistent);
        // make sure property "id" is not metamodeled
        assertTrue(metaModelFinder.findPropertyMethods(notDomainAndNotPersistentMetaModel).stream()
            .noneMatch(el -> ID.equals(el.getSimpleName().toString())));
    }
    
    /**
     * For a metamodeled entity there are 2 meta-model forms that get generated. One of them is an aliased meta-model.
     * <p>
     * An aliased meta-model extends a regular one.
     */
    @Test
    public void aliased_meta_model_extends_a_regular_one() {
        final Elements elements = elementFinder.elements;
        final List<MetaModelElement> aliasedMetaModels = elements.getPackageElement(TEST_META_MODELS_PKG_NAME).getEnclosedElements().stream()
                .mapMulti(TYPE_ELEMENT_FILTER)
                .filter(metaModelFinder::isMetaModel)
                .map(te -> metaModelFinder.newMetaModelElement(te))
                .filter(metaModelFinder::isMetaModelAliased)
                .toList();
        assertFalse(aliasedMetaModels.isEmpty());

        aliasedMetaModels.stream()
            .forEach(mme -> {
                final TypeElement superclass = ElementFinder.findSuperclass(mme).orElseThrow();
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
        final Elements elements = elementFinder.elements;
        final List<MetaModelElement> aliasedMetaModels = elements.getPackageElement(TEST_META_MODELS_PKG_NAME).getEnclosedElements().stream()
                .mapMulti(TYPE_ELEMENT_FILTER)
                .filter(metaModelFinder::isMetaModel)
                .map(te -> metaModelFinder.newMetaModelElement(te))
                .filter(metaModelFinder::isMetaModelAliased)
                .toList();
        assertFalse(aliasedMetaModels.isEmpty());
        
        aliasedMetaModels.stream()
            .forEach(mme -> {
                assertNotNull(elementFinder.findDeclaredField(mme, "alias", 
                        varEl -> varEl.getModifiers().containsAll(List.of(Modifier.PUBLIC, Modifier.FINAL)) &&
                        elementFinder.isSameType(varEl.asType(), String.class)));
            });
    }

    @Test
    public void meta_model_of_abstract_entity_with_no_key_type_information_has_no_model_for_property_key() {
        // find abstract super entity with no @KeyType annotation present
        final EntityElement abstractSuperEntityWithoutKeyType = entityFinder.findEntity(KeyType_AbstractSuperEntityWithoutKeyType.class);
        final MetaModelElement mmAbstractSuperEntityWithoutKeyType = findMetaModel(abstractSuperEntityWithoutKeyType);

        final List<PropertyElement> abstractSuperEntityWithoutKeyTypeDeclaredProps = entityFinder.findDeclaredProperties(abstractSuperEntityWithoutKeyType);
        final List<ExecutableElement> abstractSuperEntityWithoutKeyTypeDeclaredMetamodeledProps = metaModelFinder.findDeclaredPropertyMethods(mmAbstractSuperEntityWithoutKeyType);
        assertEquals(abstractSuperEntityWithoutKeyTypeDeclaredProps.size(), abstractSuperEntityWithoutKeyTypeDeclaredMetamodeledProps.size());

        assertFalse("Property %s should not have been modelled.".formatted(KEY), abstractSuperEntityWithoutKeyTypeDeclaredMetamodeledProps.stream().anyMatch(pe -> KEY.equals(pe.getSimpleName().toString())));
    }

    @Test
    public void meta_model_of_entity_with_KeyType_as_String_extending_abstract_entity_contains_model_for_property_key() {
        // find SuperEntity
        final EntityElement abstractSuperEntityWithoutKeyType = entityFinder.findEntity(KeyType_AbstractSuperEntityWithoutKeyType.class);
        final MetaModelElement mmAbstractSuperEntityWithouKeyType = findMetaModel(abstractSuperEntityWithoutKeyType);
        // find SubEntity
        final EntityElement subEntityWithKeyType = entityFinder.findEntity(KeyTypeAsString_SubEntityExtendingAbstractSuperEntityWithoutKeyType.class);
        final MetaModelElement mmSubEntityWithKeyType = findMetaModel(subEntityWithKeyType);

        // SubEntity's meta-model should extend SuperEntity's meta-model
        assertTrue(elementFinder.types.isSameType(mmSubEntityWithKeyType.getSuperclass(), mmAbstractSuperEntityWithouKeyType.asType()));

        final List<PropertyElement> subEntityWithKeyTypeDeclaredProps = entityFinder.findDeclaredProperties(subEntityWithKeyType);
        final List<ExecutableElement> subEntityWithKeyTypeDeclaredMetamodeledProps = metaModelFinder.findDeclaredPropertyMethods(mmSubEntityWithKeyType);

        assertEquals(1, subEntityWithKeyTypeDeclaredProps.size());
        // +3 for properties "key", "id" and "desc"
        assertEquals(1 + 3, subEntityWithKeyTypeDeclaredMetamodeledProps.size());

        final Optional<ExecutableElement> maybeKey = subEntityWithKeyTypeDeclaredMetamodeledProps.stream().filter(pe -> KEY.equals(pe.getSimpleName().toString())).findFirst();
        assertTrue("Property %s should have been modelled.".formatted(KEY), maybeKey.isPresent());
        assertTrue("Unexpected type for meta-modelled property %s.".formatted(KEY), 
                elementFinder.isSameType(maybeKey.get().getReturnType(), PropertyMetaModel.class));
    }

    @Test
    public void meta_model_of_entity_with_KeyType_as_Entity_extending_abstract_entity_contains_model_for_property_key() {
        // find SuperEntity
        final EntityElement abstractSuperEntityWithoutKeyType = entityFinder.findEntity(KeyType_AbstractSuperEntityWithoutKeyType.class);
        final MetaModelElement mmAbstractSuperEntityWithouKeyType = findMetaModel(abstractSuperEntityWithoutKeyType);
        // find SubEntity
        final EntityElement subEntityWithKeyType = entityFinder.findEntity(KeyTypeAsEntity_SubEntityExtendingAbstractSuperEntityWithoutKeyType.class);
        final MetaModelElement mmSubEntityWithKeyType = findMetaModel(subEntityWithKeyType);

        // SubEntity's meta-model should extend SuperEntity's meta-model
        assertTrue(elementFinder.types.isSameType(mmSubEntityWithKeyType.getSuperclass(), mmAbstractSuperEntityWithouKeyType.asType()));
        
        final List<PropertyElement> subEntityWithKeyTypeDeclaredProps = entityFinder.findDeclaredProperties(subEntityWithKeyType);
        final List<ExecutableElement> subEntityWithKeyTypeDeclaredMetamodeledProps = metaModelFinder.findDeclaredPropertyMethods(mmSubEntityWithKeyType);

        assertEquals(1, subEntityWithKeyTypeDeclaredProps.size());
        // +3 for properties "key", "id" and "desc"
        assertEquals(1 + 3, subEntityWithKeyTypeDeclaredMetamodeledProps.size());

        final Optional<ExecutableElement> maybeKey = subEntityWithKeyTypeDeclaredMetamodeledProps.stream().filter(pe -> KEY.equals(pe.getSimpleName().toString())).findFirst();
        assertTrue("Property %s should have been modelled.".formatted(KEY), maybeKey.isPresent());
        assertTrue("Unexpected type for meta-modelled property %s.".formatted(KEY),
                elementFinder.isSameType(maybeKey.get().getReturnType(), SubEntityMetaModel.class));
    }

    @Test
    public void meta_model_of_entity_with_KeyType_as_Composite_extending_abstract_entity_contains_model_for_property_key() {
        // find SuperEntity
        final EntityElement abstractSuperEntityWithoutKeyType = entityFinder.findEntity(KeyType_AbstractSuperEntityWithoutKeyType.class);
        final MetaModelElement mmAbstractSuperEntityWithouKeyType = findMetaModel(abstractSuperEntityWithoutKeyType);
        // find SubEntity
        final EntityElement subEntityWithKeyType = entityFinder.findEntity(KeyTypeAsComposite_SubEntityExtendingAbstractSuperEntityWithoutKeyType.class);
        final MetaModelElement mmSubEntityWithKeyType = findMetaModel(subEntityWithKeyType);

        // SubEntity's meta-model extends SuperEntity's meta-model ?
        assertTrue(elementFinder.types.isSameType(mmSubEntityWithKeyType.getSuperclass(), mmAbstractSuperEntityWithouKeyType.asType()));
        
        final List<PropertyElement> subEntityWithKeyTypeDeclaredProps = entityFinder.findDeclaredProperties(subEntityWithKeyType);
        final List<ExecutableElement> subEntityWithKeyTypeDeclaredMetamodeledProps = metaModelFinder.findDeclaredPropertyMethods(mmSubEntityWithKeyType);

        assertEquals(1, subEntityWithKeyTypeDeclaredProps.size());
        // +3 for properties "key", "id" and "desc"
        assertEquals(1 + 3, subEntityWithKeyTypeDeclaredMetamodeledProps.size());

        final Optional<ExecutableElement> maybeKey = subEntityWithKeyTypeDeclaredMetamodeledProps.stream().filter(pe -> KEY.equals(pe.getSimpleName().toString())).findFirst();
        assertTrue("Property %s should have been modelled.".formatted(KEY), maybeKey.isPresent());
        assertTrue("Unexpected type for meta-modelled property %s.".formatted(KEY),
                elementFinder.isSameType(maybeKey.get().getReturnType(), PropertyMetaModel.class));
    }

    @Test
    public void meta_model_for_union_entity_includes_common_properties() {
        final var exampleUnionEntity = entityFinder.findEntity(ExampleUnionEntity.class);
        final var mmExampleUnionEntity = findMetaModel(exampleUnionEntity);

        assertThat(metaModelFinder.findDeclaredPropertyMethod(mmExampleUnionEntity, "common1"))
                .get()
                .matches(it -> elementFinder.isSameType(it.getReturnType(), PropertyMetaModel.class));

        assertThat(metaModelFinder.findDeclaredPropertyMethod(mmExampleUnionEntity, "common2"))
                .get()
                .matches(it -> elementFinder.isSameType(it.getReturnType(), EntityWithOrdinaryPropsMetaModel.class));
    }

    // ============================ HELPER METHODS ============================
    /**
     * Wraps a call to {@link MetaModelFinder#findMetaModelForEntity} that returns an optional in order to assert the presence of the returned value. 
     */
    private static MetaModelElement findMetaModel(final EntityElement entityElement) {
        final Optional<MetaModelElement> maybeMetaModel = metaModelFinder.findMetaModelForEntity(entityElement);
        assertTrue(maybeMetaModel.isPresent());
        return maybeMetaModel.get();
    }

}
