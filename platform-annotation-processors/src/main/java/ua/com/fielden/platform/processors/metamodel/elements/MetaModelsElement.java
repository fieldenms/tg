package ua.com.fielden.platform.processors.metamodel.elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.Elements;

public final class MetaModelsElement {
    private TypeElement typeElement;
    private List<MetaModelElement> metaModels;
    
    public MetaModelsElement(TypeElement typeElement, Elements elementUtils) {
        this.typeElement = typeElement;
        this.metaModels = findMetaModels(typeElement, elementUtils);
    }
    
    private List<MetaModelElement> findMetaModels(TypeElement typeElement, Elements elementUtils) {
        final List<MetaModelElement> metaModels = new ArrayList<>();

        final Set<VariableElement> fields = ElementFinder.findDeclaredFields(typeElement);
        for (final VariableElement field: fields) {
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
}
