package ua.com.fielden.platform.domaintree.impl;

import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.isDotNotation;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.penultAndLast;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import ua.com.fielden.platform.domaintree.IDomainTreeManager;
import ua.com.fielden.platform.domaintree.IDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.IDomainTreeRepresentation.ITickRepresentation;
import ua.com.fielden.platform.domaintree.IUsageManager;
import ua.com.fielden.platform.domaintree.exceptions.DomainTreeException;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.serialisation.api.ISerialiser;

/**
 * Abstract domain tree manager for all TG trees. Includes support for checking and functions managing. <br>
 * <br>
 *
 * Includes implementation of "mutable checking" logic, that contain: <br>
 * a) default mutable state management; <br>
 * a) manual state management; <br>
 * b) resolution of conflicts with excluded, disabled etc. properties; <br>
 *
 * @author TG Team
 *
 */
public abstract class AbstractDomainTreeManager extends AbstractDomainTree implements IDomainTreeManager {
    private final AbstractDomainTreeRepresentation dtr;
    private final TickManager firstTick;
    private final TickManager secondTick;

    /**
     * A <i>manager</i> constructor.
     *
     * @param serialiser
     * @param dtr
     * @param firstTick
     * @param secondTick
     */
    protected AbstractDomainTreeManager(final ISerialiser serialiser, final AbstractDomainTreeRepresentation dtr, final TickManager firstTick, final TickManager secondTick) {
        super(serialiser);
        this.dtr = dtr;
        this.firstTick = firstTick;
        this.secondTick = secondTick;

        // initialise the references on "dtr" instance in "firstTick" and "secondTick" fields
        // and initialise the references on "firstTick" and "secondTick" instances in "dtr.firstTick" and "dtr.secondTick" fields
        try {
            final Field dtrField = Finder.findFieldByName(TickManager.class, "dtr");
            boolean isAccessible = dtrField.isAccessible();
            dtrField.setAccessible(true);
            dtrField.set(this.firstTick, this.dtr);
            dtrField.set(this.secondTick, this.dtr);
            dtrField.setAccessible(isAccessible);

            final Field trField = Finder.findFieldByName(TickManager.class, "tr");
            isAccessible = trField.isAccessible();
            trField.setAccessible(true);
            trField.set(this.firstTick, this.dtr.getFirstTick());
            trField.set(this.secondTick, this.dtr.getSecondTick());
            trField.setAccessible(isAccessible);
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * This interface is just a wrapper for {@link ITickManager} with accessor to mutable "checked properties".
     *
     * @author TG Team
     *
     */
    public interface ITickManagerWithMutability extends ITickManager {
        /**
         * Getter of mutable "checked properties" cache for internal purposes.
         * <p>
         * These properties are fully lazy. If some "root" has not been used -- it will not be loaded. This partly initialised stuff could be even persisted. After deserialisation
         * lazy mechanism can simply load missing stuff well.
         *
         * @param root
         * @return
         */
        List<String> checkedPropertiesMutable(final Class<?> root);

        /**
         * TODO
         *
         * @param root
         * @param property
         * @return
         */
        boolean isCheckedNaturally(final Class<?> root, final String property);

        /**
         * TODO
         *
         * @param root
         * @param property
         * @return
         */
        boolean isCheckedLightweight(final Class<?> root, final String property);
    }

    /**
     * This interface is just a wrapper for {@link ITickManager} with accessor to mutable "checked properties".
     *
     * @author TG Team
     *
     */
    public interface ITickRepresentationWithMutability extends ITickRepresentation {
        /**
         * Getter of mutable "disabled manually properties" cache for internal purposes.
         *
         * @param root
         * @return
         */
        EnhancementSet disabledManuallyPropertiesMutable();

        /**
         * TODO
         *
         * @return
         */
        boolean isDisabledImmutablyLightweight(final Class<?> root, final String property);
    }

    /**
     * A tick manager with all sufficient logic. <br>
     * <br>
     *
     * Includes implementation of "checking" logic, that contain: <br>
     * a) default mutable state management; <br>
     * a) manual state management; <br>
     * b) resolution of conflicts with excluded etc. properties; <br>
     *
     * @author TG Team
     *
     */
    public static class TickManager implements ITickManagerWithMutability {
        private final EnhancementRootsMap<List<String>> checkedProperties;
        private final EnhancementRootsMap<List<String>> rootsListsOfUsedProperties;

        private final transient AbstractDomainTreeRepresentation dtr;
        private final transient ITickRepresentation tr;
        /** A cache of warmed properties to avoid warming multiple times. This cache improves overall performance when managing domain tree. Usually consists of "" and a couple of other props (parents) originating from dot-notated ones. */
        private transient Set<String> warmedProps = new HashSet<>();
        
        /**
         * Used for the first time instantiation. IMPORTANT : To use this tick it should be passed into manager constructor, which will initialise "dtr" and "tr" fields.
         */
        public TickManager() {
            this(AbstractDomainTree.<List<String>> createRootsMap());
        }

        /**
         * Used for serialisation. IMPORTANT : To use this tick it should be passed into manager constructor, which will initialise "dtr" and "tr" fields.
         */
        protected TickManager(final Map<Class<?>, List<String>> checkedProperties) {
            this.checkedProperties = createRootsMap();
            this.checkedProperties.putAll(checkedProperties);

            rootsListsOfUsedProperties = createRootsMap();

            this.dtr = null;
            this.tr = null;
        }

        /**
         * This method is designed to be overridden in descendants to provide custom "mutable checking" logic.
         *
         * @param root
         * @param property
         * @return
         */
        protected boolean isCheckedMutably(final Class<?> root, final String property) {
            return false;
        }

        @Override
        public boolean isCheckedNaturally(final Class<?> root, final String property) {
            AbstractDomainTreeRepresentation.illegalExcludedProperties(dtr, root, property, "Could not ask a 'checked' state for already 'excluded' property [" + property
                    + "] in type [" + root.getSimpleName() + "].");
            return isCheckedNaturallyLightweight(root, property);
        }

        public boolean isCheckedNaturallyLightweight(final Class<?> root, final String property) {
            return isCheckedMutably(root, property) || // checked properties by a "contract"
                    tr.isCheckedImmutably(root, property); // the checked by default properties should be checked (immutable checking)
        }

        @Override
        public boolean isChecked(final Class<?> root, final String property) {
            loadParent(root, property);
            if (checkedProperties.get(root) == null) { // not yet loaded
                return isCheckedNaturally(root, property);
            } else {
                AbstractDomainTreeRepresentation.illegalExcludedProperties(dtr, root, property, "Could not ask a 'checked' state for already 'excluded' property [" + property
                        + "] in type [" + root.getSimpleName() + "].");
                return checkedPropertiesMutable(root).contains(property);
            }
        }

        @Override
        public boolean isCheckedLightweight(final Class<?> root, final String property) {
            // I know that the parent is already loaded.
            if (checkedProperties.get(root) == null) { // not yet loaded
                return isCheckedNaturallyLightweight(root, property);
            } else {
                // I also know that this property is not excluded. So there is no need to invoke heavy-weight method "illegalExcludedProperties"
                return checkedPropertiesMutable(root).contains(property);
            }
        }

        /**
         * Loads parent property to ensure that working with this property is safe.
         *
         * @param root
         * @param property
         */
        private void loadParent(final Class<?> root, final String property) {
            final String propToWarm = isDotNotation(property) ? penultAndLast(property).getKey() : "";
            if (!warmedProps.contains(propToWarm)) {
                dtr.warmUp(root, propToWarm);
                warmedProps.add(propToWarm);
            }
        }

        @Override
        public ITickManager check(final Class<?> root, final String property, final boolean check) {
            AbstractDomainTreeRepresentation.illegalExcludedProperties(dtr, root, property, "Could not [un]check already 'excluded' property [" + property + "] in type ["
                    + root.getSimpleName() + "].");
            if (tr.isDisabledImmutably(root, property)) {
                throw new DomainTreeException("Could not [un]check 'disabled' property [" + property + "] in type [" + root.getSimpleName() + "].");
            }
            checkSimply(root, property, check);
            return this;
        }

        protected void checkSimply(final Class<?> root, final String property, final boolean check) {
            loadParent(root, property);
            final boolean contains = checkedPropertiesMutable(root).contains(property);
            if (check) {
                if (!contains) {
                    insertCheckedProperty(root, property, checkedPropertiesMutable(root).size());
                } else {
                    logger().warn("Could not check already checked property [" + property + "] in type [" + root.getSimpleName() + "].");
                }
            } else {
                if (contains) {
                    removeCheckedProperty(root, property);
                } else {
                    logger().warn("Could not uncheck already unchecked property [" + property + "] in type [" + root.getSimpleName() + "].");
                }
            }
        }

        protected void removeCheckedProperty(final Class<?> root, final String property) {
            checkedPropertiesMutable(root).remove(property);
        }

        protected void insertCheckedProperty(final Class<?> root, final String property, final int index) {
            checkedPropertiesMutable(root).add(index, property);
        }

        @Override
        public List<String> checkedProperties(final Class<?> root) {
            return Collections.unmodifiableList(checkedPropertiesMutable(root));
        }

        @Override
        public List<String> checkedPropertiesMutable(final Class<?> rootPossiblyEnhanced) {
            final Class<?> root = DynamicEntityClassLoader.getOriginalType(rootPossiblyEnhanced);
            if (checkedProperties.get(root) == null) { // not yet loaded
                final Date st = new Date();
                // initialise checked properties using isChecked contract and "included properties" cache
                final List<String> includedProps = dtr.includedProperties(root);
                final List<String> checkedProps = new ArrayList<String>();
                checkedProperties.put(root, checkedProps);
                // the original order of "included properties" will be used for "checked properties" at first
                for (final String includedProperty : includedProps) {
                    if (!isDummyMarker(includedProperty)) {
                        if (isCheckedNaturally(rootPossiblyEnhanced, reflectionProperty(includedProperty))) {
                            insertCheckedProperty(root, includedProperty, checkedProperties.get(root).size());
                        }
                    }
                }
                logger().debug("Root [" + root.getSimpleName() + "] has been processed within " + (new Date().getTime() - st.getTime()) + "ms with " + checkedProps.size()
                        + " checked properties."); //  => [" + checkedProps + "]
            }
            return checkedProperties.get(root);
        }

        private void checkPropertyExistence(final List<String> props, final String property, final String message) {
            if (!props.contains(property)) {
                throw new DomainTreeException(message);
            }
        }

        @Override
        public ITickManager swap(final Class<?> root, final String property1, final String property2) {
            final List<String> props = checkedPropertiesMutable(root);
            checkPropertyExistence(props, property1, "'Swap' operation for 'checked properties' failed. The property [" + property1 + "] in type [" + root.getSimpleName()
                    + "] is not checked.");
            checkPropertyExistence(props, property2, "'Swap' operation for 'checked properties' failed. The property [" + property2 + "] in type [" + root.getSimpleName()
                    + "] is not checked.");
            Collections.swap(props, props.indexOf(property1), props.indexOf(property2));
            return this;
        }

        @Override
        public ITickManager move(final Class<?> root, final String what, final String beforeWhat) {
            final List<String> props = checkedPropertiesMutable(root);
            checkPropertyExistence(props, what, "'Move' operation for 'checked properties' failed. The property [" + what + "] in type [" + root.getSimpleName()
                    + "] is not checked.");
            checkPropertyExistence(props, beforeWhat, "'Move' operation for 'checked properties' failed. The property [" + beforeWhat + "] in type [" + root.getSimpleName()
                    + "] is not checked.");
            props.remove(what);
            props.add(props.indexOf(beforeWhat), what);
            return this;
        }

        @Override
        public ITickManager moveToTheEnd(final Class<?> root, final String what) {
            final List<String> props = checkedPropertiesMutable(root);
            checkPropertyExistence(props, what, "'Move to the end' operation for 'checked properties' failed. The property [" + what + "] in type [" + root.getSimpleName()
                    + "] is not checked.");
            props.remove(what);
            props.add(what);
            return this;
        }

        protected List<String> getAndInitUsedProperties(final Class<?> root, final String property) {
            illegalUncheckedProperties(this, root, property, "It's illegal to use/unuse the specified property [" + property + "] if it is not 'checked' in type ["
                    + root.getSimpleName() + "].");
            if (!rootsListsOfUsedProperties.containsKey(root)) {
                rootsListsOfUsedProperties.put(root, new ArrayList<String>());
            }
            return rootsListsOfUsedProperties.get(root);
        }

        @Override
        public boolean isUsed(final Class<?> root, final String property) {
            illegalUncheckedProperties(this, root, property, "It's illegal to ask whether the specified property [" + property
                    + "] is 'used' if it is not 'checked' in type [" + root.getSimpleName() + "].");
            return rootsListsOfUsedProperties.containsKey(root) && rootsListsOfUsedProperties.get(root).contains(property);
        }

        @Override
        public IUsageManager use(final Class<?> root, final String property, final boolean check) {
            final List<String> listOfUsedProperties = getAndInitUsedProperties(root, property);
            if (check && !listOfUsedProperties.contains(property)) {
                listOfUsedProperties.add(property);
            } else if (!check) {
                listOfUsedProperties.remove(property);
            }
            return this;
        }

        @Override
        public List<String> usedProperties(final Class<?> root) {
            final List<String> usedProperties = new ArrayList<String>();
            if (rootsListsOfUsedProperties.containsKey(root)) {
                usedProperties.addAll(rootsListsOfUsedProperties.get(root));
            }
            return usedProperties;
        }

        protected ITickRepresentation tr() {
            return tr;
        }

        protected IDomainTreeRepresentation dtr() {
            return dtr;
        }

        protected Map<Class<?>, List<String>> checkedProperties() {
            return checkedProperties;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (checkedProperties == null ? 0 : checkedProperties.hashCode());
            result = prime * result + (rootsListsOfUsedProperties == null ? 0 : rootsListsOfUsedProperties.hashCode());
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this != obj) {
                if (getClass() == obj.getClass()) {
                    final TickManager other = (TickManager) obj;
                    return Objects.equals(checkedProperties, other.checkedProperties) &&
                            Objects.equals(rootsListsOfUsedProperties, other.rootsListsOfUsedProperties);
                }
                return false;
            }
            return true;
        }
    }

    @Override
    public ITickManager getFirstTick() {
        return firstTick;
    }

    @Override
    public ITickManager getSecondTick() {
        return secondTick;
    }

    @Override
    public IDomainTreeRepresentation getRepresentation() {
        return dtr;
    }

    /**
     * A specific Kryo serialiser for {@link AbstractDomainTreeManager}.
     *
     * @author TG Team
     *
     */
    protected abstract static class AbstractDomainTreeManagerSerialiser<T extends AbstractDomainTreeManager> extends AbstractDomainTreeSerialiser<T> {
        public AbstractDomainTreeManagerSerialiser(final ISerialiser serialiser) {
            super(serialiser);
        }

        @Override
        public void write(final ByteBuffer buffer, final T manager) {
            writeValue(buffer, manager.getDtr());
            writeValue(buffer, manager.getFirstTick());
            writeValue(buffer, manager.getSecondTick());
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (dtr == null ? 0 : dtr.hashCode());
        result = prime * result + (firstTick == null ? 0 : firstTick.hashCode());
        result = prime * result + (secondTick == null ? 0 : secondTick.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this != obj) {
            if (getClass() == obj.getClass()) {
                final AbstractDomainTreeManager other = (AbstractDomainTreeManager) obj;
                return Objects.equals(dtr, other.dtr) && Objects.equals(firstTick, other.firstTick) && Objects.equals(secondTick, other.secondTick);
            }
        }
        return false;
    }

    public AbstractDomainTreeRepresentation getDtr() {
        return dtr;
    }
}
