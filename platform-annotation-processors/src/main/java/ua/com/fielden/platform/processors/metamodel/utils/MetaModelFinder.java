package ua.com.fielden.platform.processors.metamodel.utils;

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toSet;
import static ua.com.fielden.platform.processors.metamodel.MetaModelConstants.METAMODEL_SUPERCLASS;
import static ua.com.fielden.platform.processors.metamodel.MetaModelConstants.META_MODEL_ALIASED_NAME_SUFFIX;
import static ua.com.fielden.platform.processors.metamodel.MetaModelConstants.META_MODEL_NAME_SUFFIX;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

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

import ua.com.fielden.platform.processors.metamodel.concepts.MetaModelConcept;
import ua.com.fielden.platform.processors.metamodel.elements.EntityElement;
import ua.com.fielden.platform.processors.metamodel.elements.MetaModelElement;
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

    public boolean isMetaModel(final TypeElement typeElement) {
        return doesExtend(typeElement, METAMODEL_SUPERCLASS);
    }

    public boolean isMetaModelAliased(final MetaModelElement mme) {
        return mme.getSimpleName().toString().endsWith(META_MODEL_ALIASED_NAME_SUFFIX);
    }

    public Set<VariableElement> findPropertyMetaModelFields(final MetaModelElement mme) {
        return findNonStaticFields(mme).stream()
                .filter(field -> isFieldOfType(field, PropertyMetaModel.class))
                .collect(toSet());
    }

    public Set<VariableElement> findEntityMetaModelFields(final MetaModelElement mme) {
        return findNonStaticFields(mme).stream()
                .filter(field -> {
                    final TypeMirror fieldType = field.asType();
                    final TypeKind fieldTypeKind = fieldType.getKind();

                    if (TypeKind.DECLARED == fieldTypeKind) {
                        final TypeElement fieldTypeElement = (TypeElement) ((DeclaredType) field.asType()).asElement();

                        // EntityMetaModel fields have type Supplier<[METAMODEL]>
                        if (equals(fieldTypeElement, Supplier.class)) {
                            final DeclaredType fieldTypeArgument = (DeclaredType) ((DeclaredType) fieldType).getTypeArguments().get(0);
                            return doesExtend((TypeElement) fieldTypeArgument.asElement(), EntityMetaModel.class);
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
                isMethodReturnType(method, PropertyMetaModel.class);
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
        return mmc.getQualifiedName().equals(mme.getQualifiedName().toString());
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
    public TypeElement findMetaModelAliased(final MetaModelElement mme) {
        // aliasedMetaModelName = metaModelName - META_MODEL_NAME_SUFFIX + META_MODEL_ALIASED_NAME_SUFFIX
        final String entitySimpleName = StringUtils.substringBeforeLast(mme.getSimpleName().toString(), META_MODEL_NAME_SUFFIX);
        final String qualName = "%s.%s%s".formatted(mme.getPackageName(), entitySimpleName, META_MODEL_ALIASED_NAME_SUFFIX);
        return elements.getTypeElement(qualName);
    }

    /**
     * Identifies and collects all declared class-typed fields in the MetaModels element, which represent meta-models (i.e., extend {@link EntityMetaModel}).  
     *
     * @param typeElement
     * @param elementUtils
     * @return
     */
    public Set<MetaModelElement> findMetaModels(final TypeElement typeElement) {
        return findDeclaredFields(typeElement, field -> field.asType().getKind() == TypeKind.DECLARED).stream()
                .map(field -> (TypeElement) ((DeclaredType) field.asType()).asElement())
                .filter(te -> doesExtend(te, EntityMetaModel.class))
                .map(te -> newMetaModelElement(te))
                .collect(toCollection(LinkedHashSet::new));
    }

    public MetaModelElement newMetaModelElement(final TypeElement typeElement) {
        return new MetaModelElement(typeElement, getPackageName(typeElement));
    }

}