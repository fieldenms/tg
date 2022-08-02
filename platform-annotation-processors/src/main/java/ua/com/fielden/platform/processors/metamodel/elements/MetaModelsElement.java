package ua.com.fielden.platform.processors.metamodel.elements;

import static java.util.Collections.unmodifiableSet;

import java.util.Collection;
import java.util.Set;

import javax.lang.model.element.TypeElement;

/**
 * A convenient wrapper around {@code TypeElement}, which represents an entry point to all domain meta-models (i.e., class {@code MetaModels} where all meta-models are referenced
 * as static fields).
 *
 * @author TG Team
 *
 */
public final class MetaModelsElement extends ForwardingTypeElement {
    private final Set<MetaModelElement> metaModels;

    public MetaModelsElement(final TypeElement typeElement, final Collection<MetaModelElement> metaModelElements) {
        super(typeElement);
        this.metaModels = Set.copyOf(metaModelElements);
    }

    public Set<MetaModelElement> getMetaModels() {
        return unmodifiableSet(metaModels);
    }

}