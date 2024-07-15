package ua.com.fielden.platform.domaintree.testing;

import java.util.Set;

import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeManager;
import ua.com.fielden.platform.entity.factory.EntityFactory;

public class DomainTreeManager1 extends AbstractDomainTreeManager {
    /**
     * A <i>manager</i> constructor for the first time instantiation.
     * 
     * @param entityFactory
     * @param dtr
     * @param firstTick
     * @param secondTick
     */
    public DomainTreeManager1(final EntityFactory entityFactory, final Set<Class<?>> rootTypes) {
        this(entityFactory, new DomainTreeRepresentation1(entityFactory, rootTypes), new TickManager1ForTest(), new TickManager());
    }

    /**
     * A <i>manager</i> constructor.
     * 
     * @param entityFactory
     * @param dtr
     * @param firstTick
     * @param secondTick
     */
    /* protected */public DomainTreeManager1(final EntityFactory entityFactory, final DomainTreeRepresentation1 dtr, final TickManager1ForTest firstTick, final TickManager secondTick) {
        super(entityFactory, dtr, firstTick, secondTick);
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
