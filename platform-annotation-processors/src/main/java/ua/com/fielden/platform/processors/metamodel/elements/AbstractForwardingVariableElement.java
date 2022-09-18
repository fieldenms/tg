package ua.com.fielden.platform.processors.metamodel.elements;

import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;

/**
 * A base type that implements {@link VariableElement}, which is a subtype of {@link Element}.
 *
 * @author TG Team
 *
 */
abstract class AbstractForwardingVariableElement extends AbstractForwardingElement<VariableElement> implements VariableElement {

    protected AbstractForwardingVariableElement(final VariableElement element) {
        super(element);
    }

    @Override
    public Object getConstantValue() {
        return element.getConstantValue();
    }

}