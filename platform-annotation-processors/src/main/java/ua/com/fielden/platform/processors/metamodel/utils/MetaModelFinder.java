package ua.com.fielden.platform.processors.metamodel.utils;

import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toSet;
import static ua.com.fielden.platform.processors.metamodel.MetaModelConstants.METAMODELS_CLASS_QUAL_NAME;
import static ua.com.fielden.platform.processors.metamodel.MetaModelConstants.META_MODEL_ALIASED_NAME_SUFFIX;
import static ua.com.fielden.platform.processors.metamodel.MetaModelConstants.META_MODEL_NAME_SUFFIX;
import static ua.com.fielden.platform.processors.metamodel.MetaModelConstants.META_MODEL_SUPERCLASS;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import org.apache.commons.lang3.StringUtils;

import ua.com.fielden.platform.processors.metamodel.MetaModelConstants;
import ua.com.fielden.platform.processors.metamodel.concepts.MetaModelConcept;
import ua.com.fielden.platform.processors.metamodel.elements.EntityElement;
import ua.com.fielden.platform.processors.metamodel.elements.MetaModelElement;
import ua.com.fielden.platform.processors.metamodel.elements.MetaModelsElement;
import ua.com.fielden.platform.processors.metamodel.models.EntityMetaModel;
import ua.com.fielden.platform.processors.metamodel.models.PropertyMetaModel;

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
     * 
     * @param typeElement
     * @return
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

    public Set<VariableElement> findPropertyMetaModelFields(final MetaModelElement mme) {
        return findNonStaticDeclaredFields(mme).stream()
                .filter(field -> isSameType(field.asType(), PropertyMetaModel.class))
                .collect(toSet());
    }

    public Set<VariableElement> findEntityMetaModelFields(final MetaModelElement mme) {
        return findNonStaticDeclaredFields(mme).stream()
                .filter(field -> {
                    final TypeMirror fieldType = field.asType();
                    final TypeKind fieldTypeKind = fieldType.getKind();

                    if (TypeKind.DECLARED == fieldTypeKind) {
                        final TypeElement fieldTypeElement = (TypeElement) ((DeclaredType) field.asType()).asElement();

                        // EntityMetaModel fields have type Supplier<[METAMODEL]>
                        if (isSameType(fieldTypeElement, Supplier.class)) {
                            final TypeMirror fieldTypeArgument = ((DeclaredType) fieldType).getTypeArguments().get(0);
                            return isSubtype(fieldTypeArgument, META_MODEL_SUPERCLASS);
                        }
                    }
                    return false;
                })
                .collect(toSet());
    }

    /**
     * Finds all methods of a meta-model that model properties of the underlying entity. Processes the whole meta-model hierarchy (i.e. meta-models that extend other meta-models).
     * @param mme the target meta-model
     * @return a set of methods that model properties of the underlying entity
     */
    public Set<ExecutableElement> findPropertyMethods(final MetaModelElement mme) {
        return findMethods(mme).stream()
                .filter(el -> isPropertyMetaModelMethod(el) || isEntityMetaModelMethod(el))
                .collect(toCollection(LinkedHashSet::new));
    }

    /**
     * Finds all declared methods of a meta-model that model properties of the underlying entity.
     * @param mme the target meta-model
     * @return a set of methods that model properties of the underlying entity
     */
    public Set<ExecutableElement> findDeclaredPropertyMethods(final MetaModelElement mme) {
        return findDeclaredMethods(mme).stream()
                .filter(el -> isPropertyMetaModelMethod(el) || isEntityMetaModelMethod(el))
                .collect(toCollection(LinkedHashSet::new));
    }

    /**
     * Returns an optional describing a method of the meta-model element that meta-models a property with the specified name.
     * 
     * @param mme
     * @param name
     * @return
     */
    public Optional<ExecutableElement> findDeclaredPropertyMethod(final MetaModelElement mme, final String name) {
        return findDeclaredPropertyMethods(mme).stream()
                .filter(el -> el.getSimpleName().toString().equals(name))
                .findFirst();
    }

    /**
     * Returns a set of meta-model elements for each field that is of type {@code Supplier<? extends EntityMetaModel>}.
     * @param mme
     * @return
     */
    public Set<MetaModelElement> findReferencedMetaModels(final MetaModelElement mme) {
        return findEntityMetaModelFields(mme).stream()
                .map(field -> {
                    // casting here is safe, since field is of type Supplier<[METAMODEL]>, thus DeclaredType
                    // fieldType will be the Supplier type
                    final DeclaredType fieldType = (DeclaredType) field.asType();
                    // fieldTypeArgument will be the [METAMODEL] type
                    final DeclaredType fieldTypeArgument = (DeclaredType) fieldType.getTypeArguments().get(0);
                    final TypeElement fieldTypeArgumentTypeElement = (TypeElement) fieldTypeArgument.asElement();
                    return newMetaModelElement(fieldTypeArgumentTypeElement);
                })
                .collect(toSet());
    }

    /**
     * Tests whether a given meta-model method metamodels a property of a non-metamodeled type.
     * @param method
     * @return true if the method's return type is {@link PropertyMetaModel}, false otherwise
     */
    public boolean isPropertyMetaModelMethod(final ExecutableElement method) {
        return !method.getModifiers().contains(Modifier.STATIC) &&
                isSameType(method.getReturnType(), PropertyMetaModel.class);
    }

    /**
     * Tests whether a given meta-model method metamodels a property of a metamodeled type.
     * @param method
     * @return true if the method's return type is a subtype of {@link EntityMetaModel}, false otherwise
     */
    public boolean isEntityMetaModelMethod(final ExecutableElement method) {
        return !method.getModifiers().contains(Modifier.STATIC) &&
                isSubtype(method.getReturnType(), EntityMetaModel.class);
    }

    /**
     * Identifies whether {@code mmc} and {@code mme} represent the same meta-model type.
     *
     * @param mmc
     * @param mme
     * @return
     */
    public boolean isSameMetaModel(final MetaModelConcept mmc, final MetaModelElement mme) {
        return mmc.getQualifiedName().equals(mme.getQualifiedName().toString()) ||
               mmc.getAliasedQualifiedName().equals(mme.getQualifiedName().toString());
    }

    /**
     * Attempts to find a meta-model for a given entity.
     * @param entityElement
     * @param elements
     * @return {@link MetaModelElement} instance wrapped into {@link Optional} if found, else empty optional
     */
    public Optional<MetaModelElement> findMetaModelForEntity(final EntityElement entityElement) {
        final MetaModelConcept mmc = new MetaModelConcept(entityElement);
        return Optional.ofNullable(elements.getTypeElement(mmc.getQualifiedName()))
                .map(te -> newMetaModelElement(te));
    }

    /**
     * An aliased meta-model class resides in the same package as the regular meta-model, but with a slightly different name.
     * @param mme
     * @param elements
     * @return
     */
    public Optional<MetaModelElement> findMetaModelAliased(final MetaModelElement mme) {
        // aliasedMetaModelName = metaModelName - META_MODEL_NAME_SUFFIX + META_MODEL_ALIASED_NAME_SUFFIX
        final String entitySimpleName = StringUtils.substringBeforeLast(mme.getSimpleName().toString(), META_MODEL_NAME_SUFFIX);
        final String qualName = "%s.%s%s".formatted(mme.getPackageName(), entitySimpleName, META_MODEL_ALIASED_NAME_SUFFIX);
        return Optional.ofNullable(elements.getTypeElement(qualName)).map(this::newMetaModelElement);
    }

    /**
     * Identifies and collects all declared fields in the MetaModels element, which represent meta-models (i.e., extend {@link EntityMetaModel}).  
     *
     * @param typeElement
     * @param elementUtils
     * @return
     */
    public Set<MetaModelElement> findMetaModels(final TypeElement typeElement) {
        final Set<MetaModelElement> metaModels = new HashSet<>();
        // find regular meta-models that are declared as fields
        findDeclaredFields(typeElement).stream()
                .filter(field -> isSubtype(field.asType(), EntityMetaModel.class))
                .map(field -> newMetaModelElement(asTypeElementOfTypeMirror(field.asType())))
                .forEach(mme -> metaModels.add(mme));
        // find aliased meta-models that are declared as methods
        findDeclaredMethods(typeElement).stream()
                .filter(method -> isSubtype(method.getReturnType(), EntityMetaModel.class))
                .map(method -> newMetaModelElement(asTypeElementOfTypeMirror(method.getReturnType())))
                .forEach(mme -> metaModels.add(mme));
        
        return unmodifiableSet(metaModels);
    }

    public MetaModelElement newMetaModelElement(final TypeElement typeElement) {
        return new MetaModelElement(typeElement, getPackageOfTypeElement(typeElement).getQualifiedName().toString());
    }

    public boolean isActive(final MetaModelElement mme) {
        return mme.getKind() == ElementKind.CLASS;
    }
    
    public Optional<MetaModelsElement> findMetaModelsElement() {
        return Optional.ofNullable(elements.getTypeElement(METAMODELS_CLASS_QUAL_NAME))
                .map(te -> new MetaModelsElement(te, findMetaModels(te)));
    }

    public String resolveMetaModelPkgName(final String entityPkgName) {
        return entityPkgName + MetaModelConstants.META_MODEL_PKG_NAME_SUFFIX;
    }
    
    public String resolveMetaModelSimpleName(final String entitySimpleName) {
        return entitySimpleName + MetaModelConstants.META_MODEL_NAME_SUFFIX;
    }

    public String resolveAliasedMetaModelSimpleName(final String entitySimpleName) {
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
    public String resolveMetaModelName(final String entityPkgName, final String entitySimpleName) {
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
    public String resolveAliasedMetaModelName(final String entityPkgName, final String entitySimpleName) {
        return "%s.%s".formatted(resolveMetaModelPkgName(entityPkgName), resolveAliasedMetaModelSimpleName(entitySimpleName));
    }

}