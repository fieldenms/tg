package ua.com.fielden.platform.processors.metamodel.elements;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import javax.lang.model.element.TypeElement;

/**
 * A convenient wrapper around {@code TypeElement}, which represents an entry point to all domain meta-models (i.e., class {@code MetaModels} where all meta-models are referenced as static fields).
 *
 * @author TG Team
 *
 */
public final class MetaModelsElement extends AbstractForwardingTypeElement {
    private final List<MetaModelElement> metaModels;

    public MetaModelsElement(final TypeElement typeElement, final Collection<MetaModelElement> metaModelElements) {
        super(typeElement);
        this.metaModels = List.copyOf(metaModelElements);
    }

    public List<MetaModelElement> getMetaModels() {
        return metaModels;
    }

    @Override
    public int hashCode() {
        return 31 + Objects.hash(getQualifiedName());
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MetaModelsElement)) {
            return false;
        }
        final MetaModelsElement that = (MetaModelsElement) obj;
        return Objects.equals(this.getQualifiedName(), that.getQualifiedName());
    }

}