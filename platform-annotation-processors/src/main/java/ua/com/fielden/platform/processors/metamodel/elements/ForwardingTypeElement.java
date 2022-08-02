package ua.com.fielden.platform.processors.metamodel.elements;

import java.util.List;
import java.util.Objects;

import javax.lang.model.element.Name;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeMirror;

class ForwardingTypeElement extends ForwardingElement<TypeElement> implements TypeElement {

    protected ForwardingTypeElement(final TypeElement element) {
        super(element);
    }

    @Override
    public NestingKind getNestingKind() {
        return element.getNestingKind();
    }

    @Override
    public Name getQualifiedName() {
        return element.getQualifiedName();
    }

    @Override
    public TypeMirror getSuperclass() {
        return element.getSuperclass();
    }

    @Override
    public List<? extends TypeMirror> getInterfaces() {
        return element.getInterfaces();
    }

    @Override
    public List<? extends TypeParameterElement> getTypeParameters() {
        return element.getTypeParameters();
    }

    @Override
    public int hashCode() {
        return 31 + Objects.hash(getQualifiedName().toString());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MetaModelElement)) {
            return false;
        }
        final MetaModelElement that = (MetaModelElement) obj;
        return Objects.equals(this.getQualifiedName().toString(), that.getQualifiedName().toString());
    }

}
