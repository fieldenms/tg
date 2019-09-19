package ua.com.fielden.platform.domaintree.testing;

import java.util.Set;

import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeManager;
import ua.com.fielden.platform.serialisation.api.ISerialiser;

public class DomainTreeManager1 extends AbstractDomainTreeManager {
    /**
     * A <i>manager</i> constructor for the first time instantiation.
     * 
     * @param serialiser
     * @param dtr
     * @param firstTick
     * @param secondTick
     */
    public DomainTreeManager1(final ISerialiser serialiser, final Set<Class<?>> rootTypes) {
        this(serialiser, new DomainTreeRepresentation1(serialiser, rootTypes), new TickManager1ForTest(), new TickManager());
    }

    /**
     * A <i>manager</i> constructor.
     * 
     * @param serialiser
     * @param dtr
     * @param firstTick
     * @param secondTick
     */
    /* protected */public DomainTreeManager1(final ISerialiser serialiser, final DomainTreeRepresentation1 dtr, final TickManager1ForTest firstTick, final TickManager secondTick) {
        super(serialiser, dtr, firstTick, secondTick);
    }

    public static class TickManager1ForTest extends TickManager {
        /**
         * Used for serialisation and for normal initialisation. IMPORTANT : To use this tick it should be passed into manager constructor, which will initialise "dtr" and "tr"
         * fields.
         */
        public TickManager1ForTest() {
            super();
        }

        @Override
        protected boolean isCheckedMutably(final Class<?> root, final String property) {
            return property.endsWith("mutablyCheckedProp");
        }
    }
}
