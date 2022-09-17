package ua.com.fielden.platform.processors.metamodel.elements;

import javax.lang.model.element.VariableElement;

abstract class AbstractForwardingVariableElement extends AbstractForwardingElement<VariableElement> implements VariableElement {

    protected AbstractForwardingVariableElement(final VariableElement element) {
        super(element);
    }

    @Override
    public Object getConstantValue() {
        return element.getConstantValue();
    }

}