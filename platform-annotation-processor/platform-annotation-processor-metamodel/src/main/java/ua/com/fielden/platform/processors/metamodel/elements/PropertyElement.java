package ua.com.fielden.platform.processors.metamodel.elements;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

public class PropertyElement {
    private VariableElement varElement;
    
    public PropertyElement(VariableElement varElement) {
        this.varElement = varElement;
    }
    
    public VariableElement toVariableElement() {
        return varElement;
    }
    
    public String getName() {
        return varElement.getSimpleName().toString();
    }
    
    public boolean isTypeDeclared() {
        return varElement.asType().getKind() == TypeKind.DECLARED;
    }
    
    public TypeMirror getType() {
        return varElement.asType();
    }
    
    public TypeElement getTypeAsTypeElement() throws Exception {
        if (!isTypeDeclared()) {
            String message = String.format("Type of property %s is not a declared type (%s)", getName(), getType().toString());
            throw new Exception(message, new ClassCastException());
        }
        return getTypeAsTypeElementOrThrow();
    }

    public TypeElement getTypeAsTypeElementOrThrow() {
        return (TypeElement) ((DeclaredType) varElement.asType()).asElement();
    }
}
