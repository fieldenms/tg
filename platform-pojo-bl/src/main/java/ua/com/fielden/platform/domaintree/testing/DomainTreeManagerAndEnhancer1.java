package ua.com.fielden.platform.domaintree.testing;

import java.util.Set;

import ua.com.fielden.platform.domaintree.impl.AbstractDomainTree;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeManager;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.impl.DomainTreeEnhancer;
import ua.com.fielden.platform.serialisation.api.ISerialiser;

public class DomainTreeManagerAndEnhancer1 extends AbstractDomainTreeManagerAndEnhancer {

    /**
     * A <i>manager with enhancer</i> constructor for the first time instantiation.
     */
    public DomainTreeManagerAndEnhancer1(final ISerialiser serialiser, final Set<Class<?>> rootTypes) {
        this(new DomainTreeManager1(serialiser, AbstractDomainTree.validateRootTypes(rootTypes)), new DomainTreeEnhancer(serialiser, AbstractDomainTree.validateRootTypes(rootTypes)));
    }

    protected DomainTreeManagerAndEnhancer1(final AbstractDomainTreeManager base, final DomainTreeEnhancer enhancer) {
        super(base, enhancer);
    }
}