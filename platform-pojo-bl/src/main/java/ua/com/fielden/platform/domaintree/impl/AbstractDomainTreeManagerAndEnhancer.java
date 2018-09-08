package ua.com.fielden.platform.domaintree.impl;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.domaintree.Function;
import ua.com.fielden.platform.domaintree.ICalculatedProperty;
import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyAttribute;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer;
import ua.com.fielden.platform.domaintree.IDomainTreeManager;
import ua.com.fielden.platform.domaintree.IDomainTreeManager.IDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.IDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.IUsageManager;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeManager.ITickManagerWithMutability;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeManager.ITickRepresentationWithMutability;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeManager.TickManager;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeRepresentation.AbstractTickRepresentation;
import ua.com.fielden.platform.domaintree.impl.DomainTreeEnhancer.ByteArray;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

/**
 * Domain tree manager with "power" of managing domain with calculated properties. The calculated properties can be managed exactly as simple properties.<br>
 *
 * @author TG Team
 *
 */
public abstract class AbstractDomainTreeManagerAndEnhancer implements IDomainTreeManagerAndEnhancer {
    private final AbstractDomainTreeManager base;
    private final IDomainTreeEnhancer enhancer;
    private transient final IDomainTreeEnhancer enhancerWithPropertiesPopulation;

    private transient final DomainTreeRepresentationAndEnhancer dtr;
    private transient final TickManagerAndEnhancer firstTick;
    private transient final TickManagerAndEnhancer secondTick;

    protected IDomainTreeManager base() {
        return base;
    }

    protected IDomainTreeEnhancer enhancer() {
        return enhancer;
    }

    protected TickManagerAndEnhancer createFirstTick(final TickManager base) {
        return new TickManagerAndEnhancer(base);
    }

    protected TickManagerAndEnhancer createSecondTick(final TickManager base) {
        return new TickManagerAndEnhancer(base);
    }

    protected DomainTreeRepresentationAndEnhancer createRepresentation(final AbstractDomainTreeRepresentation base) {
        return new DomainTreeRepresentationAndEnhancer(base);
    }

    /**
     * The {@link DomainTreeEnhancer} wrapper that reflects the changes in manager.
     *
     * @author TG Team
     *
     */
    public static class DomainTreeEnhancerWithPropertiesPopulation implements IDomainTreeEnhancer {
        private static final Logger logger = Logger.getLogger(DomainTreeEnhancerWithPropertiesPopulation.class);
        private final IDomainTreeEnhancer baseEnhancer;
        private final IDomainTreeRepresentationWithMutability dtr;

        /**
         * A {@link DomainTreeEnhancerWithPropertiesPopulation} constructor which requires a base implementations of {@link DomainTreeEnhancer} and
         * {@link AbstractDomainTreeRepresentation}.
         *
         * @param baseEnhancer
         * @param dtr
         */
        protected DomainTreeEnhancerWithPropertiesPopulation(final IDomainTreeEnhancer baseEnhancer, final IDomainTreeRepresentationWithMutability dtr) {
            this.baseEnhancer = baseEnhancer;
            this.dtr = dtr;
        }

        /**
         * Finds a first index of property that does not start with "path".
         *
         * @param pathIndex
         * @param path
         * @param props
         * @return
         */
        private static int nextBranchIndex(final int pathIndex, final String path, final List<String> props) {
            int i = pathIndex + 1;
            final String prefix = "".equals(path) ? "" : (path + ".");
            while (i < props.size() && props.get(i).startsWith(prefix)) {
                i++;
            }
            return i;
        }

        /**
         * Migrate <code>calcProps</code> from map form to a set form.
         *
         * @param calculatedProperties
         * @return
         */
        private Set<Pair<Class<?>, String>> migrateToSet(final Map<Class<?>, List<CalculatedProperty>> calculatedProperties) {
            final Set<Pair<Class<?>, String>> set = new HashSet<>();
            for (final Entry<Class<?>, List<CalculatedProperty>> entry : calculatedProperties.entrySet()) {
                for (final CalculatedProperty prop : entry.getValue()) {
                    set.add(new Pair<Class<?>, String>(entry.getKey(), prop.pathAndName()));
                }
            }
            return set;
        }

        protected static Set<Pair<Class<?>, String>> union(final Set<Pair<Class<?>, String>> a, final Set<Pair<Class<?>, String>> b) {
            final Set<Pair<Class<?>, String>> a_union_b = new HashSet<>(a);
            a_union_b.addAll(b);
            return a_union_b;
        }

        private static Set<Pair<Class<?>, String>> subtract(final Set<Pair<Class<?>, String>> a, final Set<Pair<Class<?>, String>> b) {
            final Set<Pair<Class<?>, String>> a_subtract_b = new HashSet<>(a);
            a_subtract_b.removeAll(b);
            return a_subtract_b;
        }

        private static Set<Pair<Class<?>, String>> intersect(final Set<Pair<Class<?>, String>> a, final Set<Pair<Class<?>, String>> b) {
            final Set<Pair<Class<?>, String>> a_intersect_b = new HashSet<>(a);
            a_intersect_b.retainAll(b);
            return a_intersect_b;
        }

        @Override
        public void apply() {
            final Map<Class<?>, List<CalculatedProperty>> oldCalculatedProperties = DomainTreeEnhancer.extractAll(baseEnhancer(), false);
            final Map<Class<?>, List<CalculatedProperty>> newCalculatedProperties = new HashMap<>(baseEnhancer().calculatedProperties());

            final Set<Pair<Class<?>, String>> was = migrateToSet(oldCalculatedProperties);
            final Set<Pair<Class<?>, String>> is = migrateToSet(newCalculatedProperties);

            // form a set of retained calculated properties:
            final Set<Pair<Class<?>, String>> retained = intersect(was, is);
            final Set<Pair<Class<?>, String>> retainedAndSignificantlyChanged = new HashSet<>();
            for (final Pair<Class<?>, String> rootAndProp : retained) {
                final CalculatedProperty newProp = DomainTreeEnhancer.calculatedProperty(newCalculatedProperties.get(rootAndProp.getKey()), rootAndProp.getValue());
                final CalculatedProperty oldProp = DomainTreeEnhancer.calculatedProperty(oldCalculatedProperties.get(rootAndProp.getKey()), rootAndProp.getValue());
                if (significantlyChanged(newProp, oldProp)) {
                    retainedAndSignificantlyChanged.add(rootAndProp);
                }
            }

            // remove obsolete calc properties from included properties list
            final Set<Pair<Class<?>, String>> removed = subtract(union(was, is), is);
            beforeApplyPopulation(retainedAndSignificantlyChanged, removed);

            baseEnhancer().apply();

            // add new calc properties to included properties list
            final Set<Pair<Class<?>, String>> neew = subtract(union(was, is), was);
            afterApplyPopulation(retainedAndSignificantlyChanged, neew);
        }

        protected void beforeApplyPopulation(final Set<Pair<Class<?>, String>> retainedAndSignificantlyChanged, final Set<Pair<Class<?>, String>> removed) {
            for (final Pair<Class<?>, String> rootAndRemovalProp : union(removed, retainedAndSignificantlyChanged)) {
                removeMetaStateFromPropertyToBeRemoved(rootAndRemovalProp.getKey(), rootAndRemovalProp.getValue(), dtr);
            }
        }

        protected void afterApplyPopulation(final Set<Pair<Class<?>, String>> retainedAndSignificantlyChanged, final Set<Pair<Class<?>, String>> neew) {
            for (final Pair<Class<?>, String> rootAndProp : union(neew, retainedAndSignificantlyChanged)) {
                populateMetaStateForActuallyAddedNewProperty(DynamicEntityClassLoader.getOriginalType(rootAndProp.getKey()), rootAndProp.getValue(), dtr);
            }
        }

        /**
         * Returns <code>true</code> when the property has been changed <b>significantly</b> which means that maybe category or place of the property has been changed. This can be
         * result of the {@link CalculatedProperty#setContextualExpression(String)} or {@link CalculatedProperty#setTitle(String)} or
         * {@link CalculatedProperty#setAttribute(CalculatedPropertyAttribute)} actions.
         *
         * @param newProp
         * @param oldProp
         * @return
         */
        private boolean significantlyChanged(final CalculatedProperty newProp, final CalculatedProperty oldProp) {
            //	    return !EntityUtils.equalsEx(newProp.getContextualExpression(), oldProp.getContextualExpression()) || //
            //	    !EntityUtils.equalsEx(newProp.getTitle(), oldProp.getTitle()) || //
            //	    !EntityUtils.equalsEx(newProp.getAttribute(), oldProp.getAttribute());
            return !EntityUtils.equalsEx(newProp.category(), oldProp.category()) || //
            !EntityUtils.equalsEx(newProp.path(), oldProp.path());
        }

        protected static void removeMetaStateFromPropertyToBeRemoved(final Class<?> root, final String removedProperty, final IDomainTreeRepresentationWithMutability dtr) {
            // this is a removed property. "includedProperties" should be updated (the removed property should be removed in incl properties).
            logger.debug("The property to be removed: root == " + root + ", property == " + removedProperty);
            if (dtr.includedPropertiesMutable(root).contains(removedProperty)) {
                dtr.includedPropertiesMutable(root).remove(removedProperty);
            }
            // the "excludedProperties" set should be updated after the property has been physically removed from domain
            if (dtr.excludedPropertiesMutable().contains(AbstractDomainTree.key(root, removedProperty))) {
                dtr.excludedPropertiesMutable().remove(AbstractDomainTree.key(root, removedProperty));
            }
        }

        protected static void populateMetaStateForActuallyAddedNewProperty(final Class<?> root, final String newProperty, final IDomainTreeRepresentationWithMutability dtr) {
            final List<String> inclProps = dtr.includedProperties(root);
            if (!dtr.isExcludedImmutably(root, newProperty)) {
                // the property is not excluded 1) by contract 2) was not excluded manually
                // this is a new property. "includedProperties" should be updated (the new property added).
                logger.debug("The property to be added: root == " + root + ", property == " + newProperty);
                final String parent = PropertyTypeDeterminator.isDotNotation(newProperty) ? PropertyTypeDeterminator.penultAndLast(newProperty).getKey() : "";
                // ! important ! the parent should be warmed up before adding anything to it!
                dtr.warmUp(root, parent);

                final int pathIndex = inclProps.indexOf(parent);
                // add the property on the place of the last parent child (just before next branch of properties)
                final int nextBranchIndex = nextBranchIndex(pathIndex, parent, inclProps);
                if (nextBranchIndex > 0 && !EntityUtils.equalsEx(inclProps.get(nextBranchIndex - 1), newProperty)) { // edge-case : when warming up a NEW calc property WILL BE restored from enhanced type, and it should not be added twice
                    if (nextBranchIndex < inclProps.size()) {
                        dtr.includedPropertiesMutable(root).add(nextBranchIndex, newProperty);
                    } else {
                        dtr.includedPropertiesMutable(root).add(newProperty);
                    }
                }
            }
        }
        
        @Override
        public Class<?> adjustManagedTypeName(final Class<?> root, final String clientGeneratedTypeNameSuffix) {
            return baseEnhancer().adjustManagedTypeName(root, clientGeneratedTypeNameSuffix);
        }
        
        @Override
        public Class<?> adjustManagedTypeAnnotations(final Class<?> root, final Annotation... additionalAnnotations) {
            return baseEnhancer().adjustManagedTypeAnnotations(root, additionalAnnotations);
        }

        @Override
        public void discard() {
            baseEnhancer().discard();
        }

        @Override
        public Class<?> getManagedType(final Class<?> type) {
            return baseEnhancer().getManagedType(type);
        }

        @Override
        public List<ByteArray> getManagedTypeArrays(final Class<?> type) {
            return baseEnhancer().getManagedTypeArrays(type);
        }

        @Override
        public ICalculatedProperty addCalculatedProperty(final ICalculatedProperty calculatedProperty) {
            return baseEnhancer().addCalculatedProperty(calculatedProperty);
        }

        @Override
        public ICalculatedProperty addCalculatedProperty(final Class<?> root, final String contextPath, final String contextualExpression, final String title, final String desc, final CalculatedPropertyAttribute attribute, final String originationProperty) {
            return baseEnhancer().addCalculatedProperty(root, contextPath, contextualExpression, title, desc, attribute, originationProperty);
        }

        @Override
        public ICalculatedProperty addCalculatedProperty(final Class<?> root, final String contextPath, final String customPropertyName, final String contextualExpression, final String title, final String desc, final CalculatedPropertyAttribute attribute, final String originationProperty) {
            return baseEnhancer().addCalculatedProperty(root, contextPath, customPropertyName, contextualExpression, title, desc, attribute, originationProperty);
        }

        @Override
        public IDomainTreeEnhancer addCustomProperty(final Class<?> root, final String contextPath, final String name, final String title, final String desc, final Class<?> type) {
            return baseEnhancer().addCustomProperty(root, contextPath, name, title, desc, type);
        }

        @Override
        public void removeCalculatedProperty(final Class<?> rootType, final String calculatedPropertyName) {
            baseEnhancer().removeCalculatedProperty(rootType, calculatedPropertyName);
        }

        @Override
        public ICalculatedProperty getCalculatedProperty(final Class<?> rootType, final String calculatedPropertyName) {
            return baseEnhancer().getCalculatedProperty(rootType, calculatedPropertyName);
        }

        @Override
        public ICalculatedProperty copyCalculatedProperty(final Class<?> rootType, final String calculatedPropertyName) {
            return baseEnhancer().copyCalculatedProperty(rootType, calculatedPropertyName);
        }

        @Override
        public Set<Class<?>> rootTypes() {
            return baseEnhancer().rootTypes();
        }

        protected IDomainTreeEnhancer baseEnhancer() {
            return baseEnhancer;
        }

        @Override
        public Map<Class<?>, List<CalculatedProperty>> calculatedProperties() {
            return baseEnhancer().calculatedProperties();
        }

        @Override
        public Map<Class<?>, List<CustomProperty>> customProperties() {
            return baseEnhancer().customProperties();
        }

        @Override
        public Map<Class<?>, Pair<Class<?>, Map<String, ByteArray>>> originalAndEnhancedRootTypesAndArrays() {
            return baseEnhancer().originalAndEnhancedRootTypesAndArrays();
        }

        @Override
        public EntityFactory getFactory() {
            return baseEnhancer().getFactory();
        }
    }

    /**
     * A <i>manager with enhancer</i> constructor.
     */
    protected AbstractDomainTreeManagerAndEnhancer(final AbstractDomainTreeManager base, final DomainTreeEnhancer enhancer) {
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
    protected class TickManagerAndEnhancer extends TickManager implements ITickManagerWithMutability {
        private final TickManager base;

        protected TickManager base() {
            return base;
        }

        protected TickManagerAndEnhancer(final TickManager base) {
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
        
        @Override
        public boolean isUsed(final Class<?> root, final String property) {
            // inject an enhanced type into method implementation
            return base.isUsed(enhancerWithPropertiesPopulation.getManagedType(root), property);
        }

        @Override
        public IUsageManager use(final Class<?> root, final String property, final boolean check) {
            // inject an enhanced type into method implementation
            base.use(enhancerWithPropertiesPopulation.getManagedType(root), property, check);
            return this;
        }

        @Override
        public List<String> usedProperties(final Class<?> root) {
            // inject an enhanced type into method implementation
            return base.usedProperties(enhancerWithPropertiesPopulation.getManagedType(root));
        }

        protected IDomainTreeEnhancer enhancer() {
            return enhancerWithPropertiesPopulation;
        }

        @Override
        public boolean equals(final Object obj) {
            return this == obj ||
                super.equals(obj)
                && getClass() == obj.getClass()
                && Objects.equals(base, ((TickManagerAndEnhancer) obj).base);
        }

        @Override
        public int hashCode() {
            return 31 * super.hashCode() + ((base == null) ? 0 : base.hashCode());
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
    protected class DomainTreeRepresentationAndEnhancer implements IDomainTreeRepresentationWithMutability {
        private final AbstractDomainTreeRepresentation base;
        private final ITickRepresentation firstTick;
        private final ITickRepresentation secondTick;

        /**
         * A <i>representation with enhancer</i> constructor.
         */
        protected DomainTreeRepresentationAndEnhancer(final AbstractDomainTreeRepresentation base) {
            this.base = base;

            firstTick = createFirstTick((AbstractTickRepresentation) base.getFirstTick());
            secondTick = createSecondTick((AbstractTickRepresentation) base.getSecondTick());
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
        @Override
        public List<String> includedPropertiesMutable(final Class<?> root) {
            return this.base.includedPropertiesMutable(enhancerWithPropertiesPopulation.getManagedType(root));
        }

        @Override
        public Set<Pair<Class<?>, String>> excludedPropertiesMutable() {
            return this.base.excludedPropertiesMutable();
        }

        protected ITickRepresentation createFirstTick(final AbstractTickRepresentation base) {
            return new TickRepresentationAndEnhancer(base);
        }

        protected ITickRepresentation createSecondTick(final AbstractTickRepresentation base) {
            return new TickRepresentationAndEnhancer(base);
        }

        /**
         * A <i>representation tick with enhancer</i>.
         *
         * @author TG Team
         *
         */
        protected class TickRepresentationAndEnhancer implements ITickRepresentationWithMutability {
            private final AbstractTickRepresentation base;

            protected TickRepresentationAndEnhancer(final AbstractTickRepresentation base) {
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
        final AbstractDomainTreeManagerAndEnhancer other = (AbstractDomainTreeManagerAndEnhancer) obj;
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
