package ua.com.fielden.platform.domaintree.testing;

import java.util.Set;

import ua.com.fielden.platform.domaintree.impl.AbstractDomainTree;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeRepresentation;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.utils.Pair;

public class DomainTreeRepresentation1 extends AbstractDomainTreeRepresentation {
    /**
     * A <i>representation</i> constructor for the first time instantiation.
     * 
     * @param serialiser
     */
    public DomainTreeRepresentation1(final ISerialiser serialiser, final Set<Class<?>> rootTypes) {
        this(serialiser, rootTypes, AbstractDomainTree.createSet(), new TickRepresentationForTest(), new TickRepresentationForTest());
    }

    /**
     * A <i>representation</i> constructor. Initialises also children references on itself.
     */
    protected DomainTreeRepresentation1(final ISerialiser serialiser, final Set<Class<?>> rootTypes, final Set<Pair<Class<?>, String>> excludedProperties, final AbstractTickRepresentation firstTick, final AbstractTickRepresentation secondTick) {
        super(serialiser, rootTypes, excludedProperties, firstTick, secondTick);
    }

    public static class TickRepresentationForTest extends AbstractTickRepresentation {
        /**
         * Used for serialisation and for normal initialisation. IMPORTANT : To use this tick it should be passed into representation constructor, which should initialise "dtr"
         * field.
         */
        public TickRepresentationForTest() {
            super();
        }
    }
}
