package ua.com.fielden.platform.processors.metamodel.elements;

import static java.util.Collections.unmodifiableSet;
import static ua.com.fielden.platform.processors.metamodel.utils.ElementFinder.doesExtend;
import static ua.com.fielden.platform.processors.metamodel.utils.ElementFinder.findDeclaredFields;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.Elements;

import ua.com.fielden.platform.processors.metamodel.models.EntityMetaModel;

/**
 * A convenient wrapper around {@code TypeElement}, which represents an entry point to all domain meta-models (i.e., class {@code MetaModels} where all meta-models are referenced
 * as static fields).
 *
 * @author TG Team
 *
 */
public final class MetaModelsElement {

    private final TypeElement typeElement;
    private final Set<MetaModelElement> metaModels;

    public MetaModelsElement(final TypeElement typeElement, final Elements elementUtils) {
        this.typeElement = typeElement;
        this.metaModels = findMetaModels(typeElement, elementUtils);
    }

    /**
     * Identifies and collects all declared class-typed fields in the MetaModels element, which represent meta-models (i.e., extend {@link EntityMetaModel}).  
     *
     * @param typeElement
     * @param elementUtils
     * @return
     */
    private static Set<MetaModelElement> findMetaModels(final TypeElement typeElement, final Elements elementUtils) {
        return findDeclaredFields(typeElement, field -> field.asType().getKind() == TypeKind.DECLARED).stream()
                .map(field -> (TypeElement) ((DeclaredType) field.asType()).asElement())
                .filter(te -> doesExtend(te, EntityMetaModel.class))
                .map(te -> new MetaModelElement(te, elementUtils))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public Set<MetaModelElement> getMetaModels() {
        return unmodifiableSet(metaModels);
    }

    public String getSimpleName() {
        return typeElement.getSimpleName().toString();
    }

    @Override
    public int hashCode() {
        return 31 + Objects.hash(typeElement);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof MetaModelsElement)) {
            return false;
        }
        final MetaModelsElement that = (MetaModelsElement) obj;
        return Objects.equals(this.typeElement, that.typeElement);
    }

}