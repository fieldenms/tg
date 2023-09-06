package ua.com.fielden.platform.processors.metamodel.utils;

import org.apache.commons.lang3.StringUtils;
import ua.com.fielden.platform.processors.metamodel.MetaModelConstants;
import ua.com.fielden.platform.processors.metamodel.concepts.MetaModelConcept;
import ua.com.fielden.platform.processors.metamodel.elements.EntityElement;
import ua.com.fielden.platform.processors.metamodel.elements.MetaModelElement;
import ua.com.fielden.platform.processors.metamodel.elements.MetaModelsElement;
import ua.com.fielden.platform.processors.metamodel.exceptions.ElementFinderException;
import ua.com.fielden.platform.processors.metamodel.models.EntityMetaModel;
import ua.com.fielden.platform.processors.metamodel.models.PropertyMetaModel;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static ua.com.fielden.platform.processors.metamodel.MetaModelConstants.*;

/**
 * A class to provide utility methods for identifying various parts of domain meta-models.
 *
 * @author TG Team
 *
 */
public class MetaModelFinder extends ElementFinder {

    public MetaModelFinder(final Elements elements, final Types types) {
        super(elements, types);
    }

    /**
     * Returns true iff the type element represents a meta-model.
     * 
     * @param typeElement
     * @return
     */
    public boolean isMetaModel(final TypeElement typeElement) {
        return isMetaModel(typeElement.asType());
    }

    /**
     * Returns true iff the type mirror represents a meta-model type.
     */
    public boolean isMetaModel(final TypeMirror type) {
        return isSubtype(type, META_MODEL_SUPERCLASS);
    }

    /**
     * Returns true iff the meta-model element represents an aliased meta-model.
     * 
     * @param mme
     * @return
     */
    public boolean isMetaModelAliased(final MetaModelElement mme) {
        return mme.getSimpleName().toString().endsWith(META_MODEL_ALIASED_NAME_SUFFIX);
    }

    /**
     * Returns the element representing the given meta-model type.
     * 
     * @see ElementFinder#getTypeElement(Class)
     * @throws ElementFinderException if no coresponding type element was found
     */
    public MetaModelElement findMetaModel(final Class<? extends EntityMetaModel> clazz) {
        return newMetaModelElement(getTypeElement(clazz));
    }

    /**
     * Returns a stream of meta-model elements forming a type hierarchy from the given meta-model element up to (excluding) {@link EntityMetaModel}. 
     * 
     * @param mme
     * @return
     */
    public Stream<MetaModelElement> streamMetaModelHierarchy(final MetaModelElement mme) {
        return Stream.concat(Stream.of(mme), streamSuperclassesBelow(mme.element(), META_MODEL_SUPERCLASS).map(this::newMetaModelElement));
    }
    
    /**
     * Collects the elements of {@link #streamMetaModelHierarchy(MetaModelElement)} into an unmodifiable list.
     */
    public List<MetaModelElement> listMetaModelHierarchy(final MetaModelElement mme) {
        return streamMetaModelHierarchy(mme).toList();
    }

    /**
     * Returns a stream of elements representing methods of a meta-model (both declared and inherited) that model properties of the 
     * underlying entity.
     * 
     * @param mme the target meta-model element
     * @return a stream of method elements that model properties of the underlying entity
     */
    public Stream<ExecutableElement> streamPropertyMethods(final MetaModelElement mme) {
        return streamMetaModelHierarchy(mme).flatMap(this::streamDeclaredPropertyMethods);
    }

    /**
     * Collects the elements of {@link #streamPropertyMethods(MetaModelElement)} into an unmodifiable list.
     * <p>
     * <b>NOTE</b>: The returned list may include methods with equivalent signatures declared by different types (e.g. when a method is overriden).
     * For a finer control of what is included use {@link #streamPropertyMethods(MetaModelElement)}.
     */
    public List<ExecutableElement> findPropertyMethods(final MetaModelElement mme) {
        return streamPropertyMethods(mme).toList();
    }

    /**
     * Returns an optional describing a method of the meta-model element that meta-models a property with the specified name.
     * Both declared and inherited methods are searched.
     */
    public Optional<ExecutableElement> findPropertyMethod(final MetaModelElement mme, final String name) {
        return findPropertyMethod(streamPropertyMethods(mme), name);
    }

    /**
     * Consumes the given stream of methods of a meta-model until a method modelling a property with the specified name is found.
     * It is assumed that the stream will contain only methods of a meta-model to avoid excessive checks.
     */
    public Optional<ExecutableElement> findPropertyMethod(final Stream<ExecutableElement> methods, final String name) {
        return methods.filter(elt -> elt.getSimpleName().contentEquals(name)).findFirst();
    }

    /**
     * Searched the given collection of methods of a meta-model for a method modelling a property with the specified name.
     * It is assumed that the collection will contain only methods of a meta-model to avoid excessive checks.
     */
    public Optional<ExecutableElement> findPropertyMethod(final Collection<ExecutableElement> methods, final String name) {
        return findPropertyMethod(methods.stream(), name);
    }

    /**
     * Returns a stream of elements representing declared methods of a meta-model that model properties of the underlying entity.
     * 
     * @param mme the target meta-model element
     * @return a stream of method elements that model properties of the underlying entity
     */
    public Stream<ExecutableElement> streamDeclaredPropertyMethods(final MetaModelElement mme) {
        return streamDeclaredMethods(mme.element()).filter(el -> isPropertyMetaModelMethod(el) || isEntityMetaModelMethod(el));
    }

    /**
     * Collects the elements of {@link #streamDeclaredPropertyMethods(MetaModelElement)} into an unmodifiable list.
     */
    public List<ExecutableElement> findDeclaredPropertyMethods(final MetaModelElement mme) {
        return streamDeclaredPropertyMethods(mme).toList();
    }

    /**
     * Returns an optional describing a declared method of the meta-model element that meta-models a property with the specified name.
     */
    public Optional<ExecutableElement> findDeclaredPropertyMethod(final MetaModelElement mme, final String name) {
        return findPropertyMethod(streamDeclaredPropertyMethods(mme), name);
    }

    /**
     * Tests whether a method of a meta-model models an ordinary property (one that has a non-metamodeled type).
     * @param method
     * @return true if the method's return type is {@link PropertyMetaModel}, false otherwise
     */
    public boolean isPropertyMetaModelMethod(final ExecutableElement method) {
        return !method.getModifiers().contains(Modifier.STATIC) &&
                isSameType(method.getReturnType(), PropertyMetaModel.class);
    }

    /**
     * Tests whether a method of a meta-model models a property of a metamodeled entity type.
     * @param method
     * @return true if the method's return type is a subtype of {@link EntityMetaModel}, false otherwise
     */
    public boolean isEntityMetaModelMethod(final ExecutableElement method) {
        return !method.getModifiers().contains(Modifier.STATIC) && isMetaModel(method.getReturnType());
    }

    /**
     * Identifies whether {@code mmc} and {@code mme} represent the same meta-model type.
     *
     * @param mmc
     * @param mme
     * @return
     */
    public boolean isSameMetaModel(final MetaModelConcept mmc, final MetaModelElement mme) {
        return mmc.getQualifiedName().contentEquals(mme.getQualifiedName()) ||
               mmc.getAliasedQualifiedName().contentEquals(mme.getQualifiedName());
    }

    /**
     * Returns an optional describing a meta-model element for the specified entity element.
     */
    public Optional<MetaModelElement> findMetaModelForEntity(final EntityElement entityElement) {
        final MetaModelConcept mmc = new MetaModelConcept(entityElement);
        return Optional.ofNullable(elements.getTypeElement(mmc.getQualifiedName()))
                .map(te -> newMetaModelElement(te));
    }

    /**
     * Returns an optional describing an element representing the aliased version of the given meta-model, unless it already represents 
     * an aliased meta-model, in which case it is simply returned.
     *
     * @param mme meta-model element being examined
     * @return element representing the aliased meta-model or {@code mme} if it already represents an aliased one
     */
    public Optional<MetaModelElement> findMetaModelAliased(final MetaModelElement mme) {
        if (isMetaModelAliased(mme)) {
            return Optional.of(mme);
        }
        // aliasedMetaModelName = metaModelName - META_MODEL_NAME_SUFFIX + META_MODEL_ALIASED_NAME_SUFFIX
        final String entitySimpleName = StringUtils.substringBeforeLast(mme.getSimpleName().toString(), META_MODEL_NAME_SUFFIX);
        final String qualName = "%s.%s%s".formatted(mme.getPackageName(), entitySimpleName, META_MODEL_ALIASED_NAME_SUFFIX);
        return Optional.ofNullable(elements.getTypeElement(qualName)).map(this::newMetaModelElement);
    }

    /**
     * Returns a stream of all meta-model elements declared by a type element representing the {@code MetaModels} class.
     * Unresolved meta-model types will not be included.
     */
    public Stream<MetaModelElement> streamMetaModels(final TypeElement typeElement) {
        // find regular meta-models that are declared as fields
        final Stream<MetaModelElement> metaModels = streamDeclaredFields(typeElement)
            .filter(field -> isMetaModel(field.asType()))
            .map(field -> newMetaModelElement(asTypeElementOfTypeMirror(field.asType())));
        // find aliased meta-models that are declared as methods
        final Stream<MetaModelElement> aliasedMetaModels = streamDeclaredMethods(typeElement)
            .filter(method -> isMetaModel(method.getReturnType()))
            .map(method -> newMetaModelElement(asTypeElementOfTypeMirror(method.getReturnType())));

        return Stream.concat(metaModels, aliasedMetaModels);
    }

    /**
     * Returns a new instance of {@link MetaModelElement} composed of the given type element, obtaining the information about its package.
     * 
     * @param typeElement
     * @return
     */
    public MetaModelElement newMetaModelElement(final TypeElement typeElement) {
        return new MetaModelElement(typeElement, getPackageOfTypeElement(typeElement).getQualifiedName().toString());
    }

    /**
     * Returns an optional describing the element representing the {@code MetaModels} class.
     */
    public Optional<MetaModelsElement> findMetaModelsElement() {
        return Optional.ofNullable(elements.getTypeElement(METAMODELS_CLASS_QUAL_NAME))
                .map(te -> new MetaModelsElement(te, streamMetaModels(te).iterator()));
    }

    /**
     * Resolves the package name of a meta-model from the package name of its underlying entity.
     */
    public static String resolveMetaModelPkgName(final String entityPkgName) {
        return entityPkgName + MetaModelConstants.META_MODEL_PKG_NAME_SUFFIX;
    }
    
    /**
     * Resolves the simple name of a meta-model from the simple name of its underlying entity.
     */
    public static String resolveMetaModelSimpleName(final String entitySimpleName) {
        return entitySimpleName + MetaModelConstants.META_MODEL_NAME_SUFFIX;
    }

    /**
     * Resolves the simple name of an aliased meta-model from the simple name of its underlying entity.
     */
    public static String resolveAliasedMetaModelSimpleName(final String entitySimpleName) {
        return entitySimpleName + MetaModelConstants.META_MODEL_ALIASED_NAME_SUFFIX;
    }

    /**
     * Resolves the FQN of a meta-model from the FQN of its underlying entity.
     * <p>
     * FQN - fully-qualified name
     * @param entityPkgName
     * @param entitySimpleName
     * @return FQN of this entity's meta-model
     */
    public static String resolveMetaModelName(final String entityPkgName, final String entitySimpleName) {
        return "%s.%s".formatted(resolveMetaModelPkgName(entityPkgName), resolveMetaModelSimpleName(entitySimpleName));
    }

    /**
     * Resolves the FQN of an aliased meta-model from the FQN of its underlying entity.
     * <p>
     * FQN - fully-qualified name
     * @param entityPkgName
     * @param entitySimpleName
     * @return FQN of this entity's aliased meta-model
     */
    public static String resolveAliasedMetaModelName(final String entityPkgName, final String entitySimpleName) {
        return "%s.%s".formatted(resolveMetaModelPkgName(entityPkgName), resolveAliasedMetaModelSimpleName(entitySimpleName));
    }

}
