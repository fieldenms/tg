package ua.com.fielden.platform.processors.metamodel.utils;

import static java.util.stream.Collectors.toCollection;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import ua.com.fielden.platform.processors.metamodel.MetaModelConstants;
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
public class MetaModelFinder {
    
    public static boolean isMetaModel(final TypeElement typeElement) {
        return ElementFinder.doesExtend(typeElement, MetaModelConstants.METAMODEL_SUPERCLASS);
    }
    
    public static Set<VariableElement> findStaticFields(final MetaModelElement mme) {
        return ElementFinder.findDeclaredFields(mme.getTypeElement(), f -> ElementFinder.isStatic(f));
    }

    public static Set<VariableElement> findNonStaticFields(final MetaModelElement mme) {
        return ElementFinder.findDeclaredFields(mme.getTypeElement(), f -> !ElementFinder.isStatic(f));
    }
    
    public static Set<VariableElement> findPropertyMetaModelFields(final MetaModelElement mme) {
        return findNonStaticFields(mme).stream()
                .filter(field -> ElementFinder.isFieldOfType(field, PropertyMetaModel.class))
                .collect(Collectors.toSet());
    }

    public static Set<VariableElement> findEntityMetaModelFields(final MetaModelElement mme) {
        return findNonStaticFields(mme).stream()
                .filter(field -> {
                    final TypeMirror fieldType = field.asType();
                    final TypeKind fieldTypeKind = fieldType.getKind();

                    if (TypeKind.DECLARED == fieldTypeKind) {
                        final TypeElement fieldTypeElement = (TypeElement) ((DeclaredType) field.asType()).asElement();

                        // EntityMetaModel fields have type Supplier<[METAMODEL]>
                        if (ElementFinder.equals(fieldTypeElement, Supplier.class)) {
                            final DeclaredType fieldTypeArgument = (DeclaredType) ((DeclaredType) fieldType).getTypeArguments().get(0);
                            return ElementFinder.doesExtend((TypeElement) fieldTypeArgument.asElement(), EntityMetaModel.class);
                        }
                    }
                    return false;
                })
                .collect(Collectors.toSet());
    }
    
    /**
     * Finds all methods of a meta-model that model properties of the underlying entity. Processes the whole meta-model hierarchy (i.e. meta-models that extend other meta-models).
     * @param mme the target meta-model
     * @param typeUtils an instance of {@link Types} for analyzing the meta-model
     * @return a set of methods that model properties of the underlying entity
     */
    public static Set<ExecutableElement> findPropertyMethods(final MetaModelElement mme, final Types typeUtils) {
        return ElementFinder.findMethods(mme.getTypeElement()).stream()
                .filter(el -> isPropertyMetaModelMethod(el) || isEntityMetaModelMethod(el, typeUtils))
                .collect(toCollection(LinkedHashSet::new));
    }

    /**
     * Finds all declared methods of a meta-model that model properties of the underlying entity.
     * @param mme the target meta-model
     * @param typeUtils an instance of {@link Types} for analyzing the meta-model
     * @return a set of methods that model properties of the underlying entity
     */
    public static Set<ExecutableElement> findDeclaredPropertyMethods(final MetaModelElement mme, final Types typeUtils) {
        return ElementFinder.findDeclaredMethods(mme.getTypeElement()).stream()
                .filter(el -> isPropertyMetaModelMethod(el) || isEntityMetaModelMethod(el, typeUtils))
                .collect(toCollection(LinkedHashSet::new));
    }

    /**
     * Returns a set of meta-model elements for each field that is of type {@code Supplier<? extends EntityMetaModel>}.
     * @param mme
     * @return
     */
    public static Set<MetaModelElement> findReferencedMetaModels(final MetaModelElement mme, final Elements elementUtils) {
        return findEntityMetaModelFields(mme).stream()
                .map(field -> {
                    // casting here is safe, since field is of type Supplier<[METAMODEL]>, thus DeclaredType
                    // fieldType will be the Supplier type
                    final DeclaredType fieldType = (DeclaredType) field.asType();
                    // fieldTypeArgument will be the [METAMODEL] type
                    final DeclaredType fieldTypeArgument = (DeclaredType) fieldType.getTypeArguments().get(0);
                    final TypeElement fieldTypeArgumentTypeElement = (TypeElement) fieldTypeArgument.asElement();
                    return new MetaModelElement(fieldTypeArgumentTypeElement, elementUtils);
                })
                .collect(Collectors.toSet());
    }

    /**
     * Tests whether a given meta-model method metamodels a property of a non-metamodeled type.
     * @param method
     * @return true if the method's return type is {@link PropertyMetaModel}, false otherwise
     */
    public static boolean isPropertyMetaModelMethod(final ExecutableElement method) {
        return !method.getModifiers().contains(Modifier.STATIC) &&
                ElementFinder.isMethodReturnType(method, PropertyMetaModel.class);
    }

    /**
     * Tests whether a given meta-model method metamodels a property of a metamodeled type.
     * @param method
     * @return true if the method's return type is a subtype of {@link EntityMetaModel}, false otherwise
     */
    public static boolean isEntityMetaModelMethod(final ExecutableElement method, final Types typeUtils) {
        return !method.getModifiers().contains(Modifier.STATIC) &&
                ElementFinder.isSubtype(method.getReturnType(), EntityMetaModel.class, typeUtils);
    }
    
    public static boolean isSameMetaModel(final MetaModelConcept mmc, final MetaModelElement mme) {
        return mmc.getQualifiedName().equals(mme.getQualifiedName());
    }
    
    /**
     * Attempts to find a meta-model for a given entity.
     * @param entityElement
     * @param elementUtils
     * @return {@link MetaModelElement} instance wrapped into {@link Optional} if found, else empty optional
     */
    public static Optional<MetaModelElement> findMetaModelForEntity(final EntityElement entityElement, final Elements elementUtils) {
        final MetaModelConcept mmc = new MetaModelConcept(entityElement);
        final Optional<TypeElement> maybeMmte = Optional.ofNullable(elementUtils.getTypeElement(mmc.getQualifiedName()));
        if (maybeMmte.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new MetaModelElement(maybeMmte.get(), elementUtils));
    }
}