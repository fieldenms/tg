package ua.com.fielden.platform.processors.metamodel.elements;

import javax.lang.model.element.VariableElement;

class ForwardingVariableElement extends ForwardingElement<VariableElement> implements VariableElement {

    protected ForwardingVariableElement(final VariableElement element) {
        super(element);
    }

    @Override
    public Object getConstantValue() {
        return element.getConstantValue();
    }

}