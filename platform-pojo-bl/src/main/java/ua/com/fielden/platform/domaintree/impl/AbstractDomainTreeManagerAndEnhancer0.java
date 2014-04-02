package ua.com.fielden.platform.domaintree.impl;

import java.util.List;
import java.util.Set;

import ua.com.fielden.platform.domaintree.Function;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer;
import ua.com.fielden.platform.domaintree.IDomainTreeManager;
import ua.com.fielden.platform.domaintree.IDomainTreeManager.IDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.IDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.IDomainTreeRepresentation.IPropertyListener;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeManager.ITickManagerWithMutability;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeManager.ITickRepresentationWithMutability;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeManager.IncludedAndCheckedPropertiesSynchronisationListener;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeManager.TickManager;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeManagerAndEnhancer.DomainTreeEnhancerWithPropertiesPopulation;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeRepresentation.AbstractTickRepresentation;
import ua.com.fielden.platform.utils.Pair;

/**
 * WARNING: this is an OLD version!
 * 
 * @author TG Team
 * 
 */
@Deprecated
public abstract class AbstractDomainTreeManagerAndEnhancer0 implements IDomainTreeManagerAndEnhancer {
    private final AbstractDomainTreeManager base;
    private final IDomainTreeEnhancer enhancer;
    private transient final IDomainTreeEnhancer enhancerWithPropertiesPopulation;

    private transient final DomainTreeRepresentationAndEnhancer0 dtr;
    private transient final TickManagerAndEnhancer0 firstTick;
    private transient final TickManagerAndEnhancer0 secondTick;

    protected IDomainTreeManager base() {
        return base;
    }

    protected IDomainTreeEnhancer enhancer() {
        return enhancer;
    }

    protected TickManagerAndEnhancer0 createFirstTick(final TickManager base) {
        return new TickManagerAndEnhancer0(base);
    }

    protected TickManagerAndEnhancer0 createSecondTick(final TickManager base) {
        return new TickManagerAndEnhancer0(base);
    }

    protected DomainTreeRepresentationAndEnhancer0 createRepresentation(final AbstractDomainTreeRepresentation base) {
        return new DomainTreeRepresentationAndEnhancer0(base);
    }

    /**
     * A <i>manager with enhancer</i> constructor.
     */
    protected AbstractDomainTreeManagerAndEnhancer0(final AbstractDomainTreeManager base, final DomainTreeEnhancer0 enhancer) {
        this.base = base;
        this.enhancer = enhancer;

        // load tree of properties after managedType is known
        for (final Class<?> rootType : base.getRepresentation().rootTypes()) {
            base.getRepresentation().includedProperties(enhancer.getManagedType(rootType));
        }

        dtr = createRepresentation((AbstractDomainTreeRepresentation) base.getRepresentation());
        firstTick = createFirstTick((TickManager) base.getFirstTick());
        secondTick = createSecondTick((TickManager) base.getSecondTick());
        enhancerWithPropertiesPopulation = createEnhancerWrapperWithPropertiesPopulation();

        final IPropertyListener oldListener = this.base.listener();
        final IPropertyListener newListener = new IncludedAndCheckedPropertiesSynchronisationListener(this.firstTick, this.secondTick, (ITickRepresentationWithMutability) this.getRepresentation().getFirstTick(), (ITickRepresentationWithMutability) this.getRepresentation().getSecondTick(), (IDomainTreeRepresentationWithMutability) this.getRepresentation());
        this.base.getRepresentation().removePropertyListener(oldListener);
        this.getRepresentation().addPropertyListener(newListener);
    }

    /**
     * Creates a domain tree enhancer wrapper that takes care about population of domain tree changes (calc props) in representation "included properties" (which triggers other
     * population like manager's "checked properties" automatically).
     * 
     * @return
     */
    protected DomainTreeEnhancerWithPropertiesPopulation createEnhancerWrapperWithPropertiesPopulation() {
        return new DomainTreeEnhancerWithPropertiesPopulation(enhancer(), dtr);
    }

    @Override
    public IDomainTreeEnhancer getEnhancer() {
        return enhancerWithPropertiesPopulation;
    }

    @Override
    public ITickManager getFirstTick() {
        return firstTick;
    }

    @Override
    public ITickManager getSecondTick() {
        return secondTick;
    }

    /**
     * A <i>tick manager with enhancer</i>.
     * 
     * @author TG Team
     * 
     */
    protected class TickManagerAndEnhancer0 extends TickManager implements ITickManagerWithMutability {
        private final TickManager base;

        protected TickManager base() {
            return base;
        }

        protected TickManagerAndEnhancer0(final TickManager base) {
            this.base = base;
        }

        @Override
        public List<String> checkedPropertiesMutable(final Class<?> root) {
            // inject an enhanced type into method implementation
            return base.checkedPropertiesMutable(enhancerWithPropertiesPopulation.getManagedType(root));
        }

        @Override
        protected boolean isCheckedMutably(final Class<?> root, final String property) {
            // inject an enhanced type into method implementation
            return base.isCheckedMutably(enhancerWithPropertiesPopulation.getManagedType(root), property);
        }

        @Override
        public boolean isCheckedNaturally(final Class<?> root, final String property) {
            // inject an enhanced type into method implementation
            return base.isCheckedNaturally(enhancerWithPropertiesPopulation.getManagedType(root), property);
        }

        @Override
        public boolean isChecked(final Class<?> root, final String property) {
            // inject an enhanced type into method implementation
            return base.isChecked(enhancerWithPropertiesPopulation.getManagedType(root), property);
        }

        @Override
        public boolean isCheckedLightweight(final Class<?> root, final String property) {
            // inject an enhanced type into method implementation
            return base.isCheckedLightweight(enhancerWithPropertiesPopulation.getManagedType(root), property);
        }

        @Override
        public ITickManager check(final Class<?> root, final String property, final boolean check) {
            // inject an enhanced type into method implementation
            base.check(enhancerWithPropertiesPopulation.getManagedType(root), property, check);
            return this;
        }

        @Override
        public List<String> checkedProperties(final Class<?> root) {
            // inject an enhanced type into method implementation
            return base.checkedProperties(enhancerWithPropertiesPopulation.getManagedType(root));
        }

        @Override
        public void addPropertyCheckingListener(final IPropertyCheckingListener listener) {
            // inject an enhanced type into method implementation
            base.addPropertyCheckingListener(listener);
        }

        @Override
        public void removePropertyCheckingListener(final IPropertyCheckingListener listener) {
            // inject an enhanced type into method implementation
            base.removePropertyCheckingListener(listener);
        }

        @Override
        public ITickManager swap(final Class<?> root, final String property1, final String property2) {
            // inject an enhanced type into method implementation
            base.swap(enhancerWithPropertiesPopulation.getManagedType(root), property1, property2);
            return this;
        }

        @Override
        public ITickManager move(final Class<?> root, final String what, final String beforeWhat) {
            // inject an enhanced type into method implementation
            base.move(enhancerWithPropertiesPopulation.getManagedType(root), what, beforeWhat);
            return this;
        }

        @Override
        public ITickManager moveToTheEnd(final Class<?> root, final String what) {
            // inject an enhanced type into method implementation
            base.moveToTheEnd(enhancerWithPropertiesPopulation.getManagedType(root), what);
            return this;
        }

        protected IDomainTreeEnhancer enhancer() {
            return enhancerWithPropertiesPopulation;
        }
    }

    @Override
    public IDomainTreeRepresentation getRepresentation() {
        return dtr;
    }

    /**
     * A <i>representation with enhancer</i>.
     * 
     * @author TG Team
     * 
     */
    protected class DomainTreeRepresentationAndEnhancer0 implements IDomainTreeRepresentationWithMutability {
        private final AbstractDomainTreeRepresentation base;
        private final ITickRepresentation firstTick;
        private final ITickRepresentation secondTick;

        /**
         * A <i>representation with enhancer</i> constructor.
         */
        protected DomainTreeRepresentationAndEnhancer0(final AbstractDomainTreeRepresentation base) {
            this.base = base;

            firstTick = createFirstTick((AbstractTickRepresentation) base.getFirstTick());
            secondTick = createSecondTick((AbstractTickRepresentation) base.getSecondTick());
        }

        @Override
        public void addPropertyListener(final IPropertyListener listener) {
            base.addPropertyListener(listener);
        }

        @Override
        public void removePropertyListener(final IPropertyListener listener) {
            base.removePropertyListener(listener);
        }

        /**
         * Getter of mutable "included properties" cache for internal purposes.
         * <p>
         * Please note that you can only mutate this list with methods {@link List#add(Object)} and {@link List#remove(Object)} to correctly reflect the changes on depending
         * objects. (e.g. UI tree models, checked properties etc.)
         * 
         * @param root
         * @return
         */
        public List<String> includedPropertiesMutable(final Class<?> root) {
            return this.base.includedPropertiesMutable(enhancerWithPropertiesPopulation.getManagedType(root));
        }

        @Override
        public Set<Pair<Class<?>, String>> excludedPropertiesMutable() {
            return this.base.excludedPropertiesMutable();
        }

        protected ITickRepresentation createFirstTick(final AbstractTickRepresentation base) {
            return new TickRepresentationAndEnhancer0(base);
        }

        protected ITickRepresentation createSecondTick(final AbstractTickRepresentation base) {
            return new TickRepresentationAndEnhancer0(base);
        }

        /**
         * A <i>representation tick with enhancer</i>.
         * 
         * @author TG Team
         * 
         */
        protected class TickRepresentationAndEnhancer0 implements ITickRepresentationWithMutability {
            private final AbstractTickRepresentation base;

            protected TickRepresentationAndEnhancer0(final AbstractTickRepresentation base) {
                this.base = base;
            }

            protected ITickRepresentation base() {
                return base;
            }

            @Override
            public boolean isDisabledImmutably(final Class<?> root, final String property) {
                // inject an enhanced type into method implementation
                return base.isDisabledImmutably(enhancerWithPropertiesPopulation.getManagedType(root), property);
            }

            @Override
            public ITickRepresentation disableImmutably(final Class<?> root, final String property) {
                // inject an enhanced type into method implementation
                base.disableImmutably(enhancerWithPropertiesPopulation.getManagedType(root), property);
                return this;
            }

            @Override
            public boolean isCheckedImmutably(final Class<?> root, final String property) {
                // inject an enhanced type into method implementation
                return base.isCheckedImmutably(enhancerWithPropertiesPopulation.getManagedType(root), property);
            }

            @Override
            public void addPropertyDisablementListener(final IPropertyDisablementListener listener) {
                // inject an enhanced type into method implementation
                base.addPropertyDisablementListener(listener);
            }

            @Override
            public void removePropertyDisablementListener(final IPropertyDisablementListener listener) {
                // inject an enhanced type into method implementation
                base.removePropertyDisablementListener(listener);
            }

            @Override
            public EnhancementSet disabledManuallyPropertiesMutable() {
                return base.disabledManuallyPropertiesMutable();
            }

            @Override
            public boolean isDisabledImmutablyLightweight(final Class<?> root, final String property) {
                // inject an enhanced type into method implementation
                return base.isDisabledImmutablyLightweight(enhancerWithPropertiesPopulation.getManagedType(root), property);
            }

            protected boolean isCheckedImmutablyLightweight(final Class<?> root, final String property) {
                // inject an enhanced type into method implementation
                return base.isCheckedImmutablyLightweight(enhancerWithPropertiesPopulation.getManagedType(root), property);
            }

            @Override
            public void addWeakPropertyDisablementListener(final IPropertyDisablementListener listener) {
                base.addWeakPropertyDisablementListener(listener);
            }
        }

        @Override
        public Set<Function> availableFunctions(final Class<?> root, final String property) {
            // inject an enhanced type into method implementation
            return base.availableFunctions(enhancerWithPropertiesPopulation.getManagedType(root), property);
        }

        @Override
        public ITickRepresentation getFirstTick() {
            return firstTick;
        }

        @Override
        public ITickRepresentation getSecondTick() {
            return secondTick;
        }

        @Override
        public boolean isExcludedImmutably(final Class<?> root, final String property) {
            // inject an enhanced type into method implementation
            return base.isExcludedImmutably(enhancerWithPropertiesPopulation.getManagedType(root), property);
        }

        @Override
        public IDomainTreeRepresentation excludeImmutably(final Class<?> root, final String property) {
            // inject an enhanced type into method implementation
            base.excludeImmutably(enhancerWithPropertiesPopulation.getManagedType(root), property);
            return this;
        }

        @Override
        public List<String> includedProperties(final Class<?> root) {
            // inject an enhanced type into method implementation
            return base.includedProperties(enhancerWithPropertiesPopulation.getManagedType(root));
        }

        @Override
        public IDomainTreeRepresentation warmUp(final Class<?> root, final String property) {
            // inject an enhanced type into method implementation
            base.warmUp(enhancerWithPropertiesPopulation.getManagedType(root), property);
            return this;
        }

        @Override
        public Set<Class<?>> rootTypes() {
            return base.rootTypes();
        }

        protected IDomainTreeEnhancer enhancer() {
            return enhancerWithPropertiesPopulation;
        }

        @Override
        public void addWeakPropertyListener(final IPropertyListener listener) {
            base.addWeakPropertyListener(listener);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((base == null) ? 0 : base.hashCode());
        result = prime * result + ((enhancer == null) ? 0 : enhancer.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AbstractDomainTreeManagerAndEnhancer0 other = (AbstractDomainTreeManagerAndEnhancer0) obj;
        if (base == null) {
            if (other.base != null) {
                return false;
            }
        } else if (!base.equals(other.base)) {
            return false;
        }
        if (enhancer == null) {
            if (other.enhancer != null) {
                return false;
            }
        } else if (!enhancer.equals(other.enhancer)) {
            return false;
        }
        return true;
    }
}
