package ua.com.fielden.platform.domaintree.centre.impl;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import ua.com.fielden.platform.domaintree.centre.ILocatorDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.ILocatorDomainTreeManager.ILocatorDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.ILocatorDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTree;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.impl.DomainTreeEnhancer0;
import ua.com.fielden.platform.serialisation.api.ISerialiser0;
import ua.com.fielden.platform.serialisation.api.SerialiserEngines;
import ua.com.fielden.platform.serialisation.kryo.serialisers.TgSimpleSerializer;

import com.esotericsoftware.kryo.Kryo;

/**
 * WARNING: this is an OLD version!
 *
 * @author TG Team
 *
 */
@Deprecated
public class LocatorDomainTreeManagerAndEnhancer0 extends CentreDomainTreeManagerAndEnhancer0 implements ILocatorDomainTreeManagerAndEnhancer {
    public LocatorDomainTreeManagerAndEnhancer0(final ISerialiser0 serialiser, final Set<Class<?>> rootTypes) {
        this(serialiser, new LocatorDomainTreeManager0(serialiser, AbstractDomainTree.validateRootTypes(rootTypes)), new DomainTreeEnhancer0(serialiser, AbstractDomainTree.validateRootTypes(rootTypes)), new HashMap<String, IAbstractAnalysisDomainTreeManager>(), new HashMap<String, IAbstractAnalysisDomainTreeManager>(), new HashMap<String, IAbstractAnalysisDomainTreeManager>());
    }

    protected LocatorDomainTreeManagerAndEnhancer0(final ISerialiser0 serialiser, final LocatorDomainTreeManager0 base, final DomainTreeEnhancer0 enhancer, final Map<String, IAbstractAnalysisDomainTreeManager> persistentAnalyses, final Map<String, IAbstractAnalysisDomainTreeManager> currentAnalyses, final Map<String, IAbstractAnalysisDomainTreeManager> freezedAnalyses) {
        super(serialiser, base, enhancer, persistentAnalyses, currentAnalyses, freezedAnalyses);
    }

    @Override
    protected DomainTreeRepresentationAndEnhancer0 createRepresentation(final AbstractDomainTreeRepresentation base) {
        return new LocatorDomainTreeRepresentationAndEnhancer0(base);
    }

    @Override
    public ILocatorDomainTreeManager base() {
        return (ILocatorDomainTreeManager) super.base();
    }

    @Override
    public ILocatorDomainTreeRepresentation getRepresentation() {
        return (ILocatorDomainTreeRepresentation) super.getRepresentation();
    }

    @Override
    public SearchBy getSearchBy() {
        return base().getSearchBy();
    }

    @Override
    public ILocatorDomainTreeManager setSearchBy(final SearchBy searchBy) {
        base().setSearchBy(searchBy);
        return this;
    }

    @Override
    public boolean isUseForAutocompletion() {
        return base().isUseForAutocompletion();
    }

    @Override
    public ILocatorDomainTreeManager setUseForAutocompletion(final boolean useForAutocompletion) {
        base().setUseForAutocompletion(useForAutocompletion);
        return this;
    }

    /**
     * Overridden to take into account calculated properties.
     *
     * @author TG Team
     *
     */
    protected class LocatorDomainTreeRepresentationAndEnhancer0 extends CentreDomainTreeRepresentationAndEnhancer0 implements ILocatorDomainTreeRepresentation {
        protected LocatorDomainTreeRepresentationAndEnhancer0(final AbstractDomainTreeRepresentation base) {
            super(base);
        }
    }

    /**
     * WARNING: this is an OLD version!
     *
     * @author TG Team
     *
     */
    @Deprecated
    public static class LocatorDomainTreeManagerAndEnhancer0WithTransientAnalysesSerialiser extends TgSimpleSerializer<LocatorDomainTreeManagerAndEnhancer0> {
        private final ISerialiser0 serialiser;

        /**
         * WARNING: this is an OLD version!
         *
         * @author TG Team
         *
         */
        @Deprecated
        public LocatorDomainTreeManagerAndEnhancer0WithTransientAnalysesSerialiser(final ISerialiser0 serialiser) {
            super((Kryo) serialiser.getEngine(SerialiserEngines.KRYO));
            this.serialiser = serialiser;
        }

        @Override
        public LocatorDomainTreeManagerAndEnhancer0 read(final ByteBuffer buffer) {
            final LocatorDomainTreeManager0 base = readValue(buffer, LocatorDomainTreeManager0.class);
            final DomainTreeEnhancer0 enhancer = readValue(buffer, DomainTreeEnhancer0.class);
            final Map<String, IAbstractAnalysisDomainTreeManager> persistentAnalyses = readValue(buffer, HashMap.class);
            final Map<String, IAbstractAnalysisDomainTreeManager> currentAnalyses = readValue(buffer, HashMap.class);
            final Map<String, IAbstractAnalysisDomainTreeManager> freezedAnalyses = readValue(buffer, HashMap.class);
            return new LocatorDomainTreeManagerAndEnhancer0(serialiser(), base, enhancer, persistentAnalyses, currentAnalyses, freezedAnalyses);
        }

        @Override
        public void write(final ByteBuffer buffer, final LocatorDomainTreeManagerAndEnhancer0 manager) {
            writeValue(buffer, manager.base());
            writeValue(buffer, manager.enhancer());
            writeValue(buffer, manager.persistentAnalyses());
            writeValue(buffer, manager.currentAnalyses());
            writeValue(buffer, manager.freezedAnalyses());
        }

        public ISerialiser0 serialiser() {
            return serialiser;
        }
    }
}
