package ua.com.fielden.platform.processors.metamodel.elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.Elements;

import ua.com.fielden.platform.processors.metamodel.utils.ElementFinder;

/**
 * A convenient wrapper around {@code TypeElement}, which represents an entry point to all domain meta-models (i.e., class {@code MetaModels} where all meta-models are referenced
 * as static fields).
 *
 * @author TG Team
 *
 */
public final class MetaModelsElement {

    private final TypeElement typeElement;
    private final List<MetaModelElement> metaModels;

    public MetaModelsElement(final TypeElement typeElement, final Elements elementUtils) {
        this.typeElement = typeElement;
        this.metaModels = findMetaModels(typeElement, elementUtils);
    }

    private static List<MetaModelElement> findMetaModels(final TypeElement typeElement, final Elements elementUtils) {
        final List<MetaModelElement> metaModels = new ArrayList<>();
        final Set<VariableElement> fields = ElementFinder.findDeclaredFields(typeElement);
        for (final VariableElement field : fields) {
            final TypeElement fieldType = (TypeElement) ((DeclaredType) field.asType()).asElement();
            final MetaModelElement mme = new MetaModelElement(fieldType, elementUtils);
            metaModels.add(mme);
        }

        return metaModels;
    }

    public List<MetaModelElement> getMetaModels() {
        return Collections.unmodifiableList(metaModels);
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