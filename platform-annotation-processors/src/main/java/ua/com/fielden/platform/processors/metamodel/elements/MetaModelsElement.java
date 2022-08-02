package ua.com.fielden.platform.processors.metamodel.elements;

import static java.util.Collections.unmodifiableSet;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

import javax.lang.model.element.TypeElement;

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

    public MetaModelsElement(final TypeElement typeElement, final Collection<MetaModelElement> metaModelElements) {
        this.typeElement = typeElement;
        this.metaModels = Set.copyOf(metaModelElements);
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