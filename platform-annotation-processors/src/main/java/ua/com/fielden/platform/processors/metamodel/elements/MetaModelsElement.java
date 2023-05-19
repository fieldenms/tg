package ua.com.fielden.platform.processors.metamodel.elements;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import javax.lang.model.element.TypeElement;

/**
 * A convenient wrapper around {@code TypeElement}, which represents an entry point to all domain meta-models (i.e., class {@code MetaModels} 
 * where all meta-models are referenced as static fields).
 *
 * @author TG Team
 *
 */
public final class MetaModelsElement extends AbstractForwardingTypeElement {
    private final Set<MetaModelElement> metaModels;
    private Supplier<Set<MetaModelElement>> metaModelsLazyView;

    public MetaModelsElement(final TypeElement typeElement, final Iterator<MetaModelElement> metaModelElementsIterator) {
        super(typeElement);
        this.metaModels = new HashSet<>();
        metaModelElementsIterator.forEachRemaining(this.metaModels::add);
        this.metaModelsLazyView = () -> {
            final var view = Collections.unmodifiableSet(metaModels);
            this.metaModelsLazyView = () -> view;
            return view;
        };
    }

    public Set<MetaModelElement> getMetaModels() {
        return metaModelsLazyView.get();
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