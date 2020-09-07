package ua.com.fielden.platform.domaintree.impl;

import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.determinePropertyType;
import static ua.com.fielden.platform.utils.EntityUtils.isBoolean;
import static ua.com.fielden.platform.utils.EntityUtils.isRangeType;

import java.util.Set;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.domaintree.IDomainTreeManager.ITickManager;
import ua.com.fielden.platform.domaintree.IUsageManager;
import ua.com.fielden.platform.domaintree.exceptions.DomainTreeException;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.entity.annotation.CritOnly.Type;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

/**
 * A base class for representations and managers with useful utility methods.
 *
 * @author TG Team
 *
 */
public abstract class AbstractDomainTree {
    private final EntityFactory entityFactory;
    private static final Logger logger = Logger.getLogger(AbstractDomainTree.class);
    private static final String COMMON_SUFFIX = ".common-properties", DUMMY_SUFFIX = ".dummy-property";
    protected static final String PLACEHOLDER = "-placeholder-origin-";

    protected static Logger logger() {
        return logger;
    }

    protected static String getDummySuffix() {
        return DUMMY_SUFFIX;
    }

    /**
     * Constructs base domain tree with <code>entityFactory</code> instance.
     *
     * @param entityFactory
     */
    protected AbstractDomainTree(final EntityFactory entityFactory) {
        this.entityFactory = entityFactory;
    }

    /**
     * Returns an entity factory that is essential for inner {@link AbstractEntity} instances (e.g. calculated properties) creation.
     *
     * @return
     */
    protected EntityFactory getFactory() {
        return entityFactory;
    }

    /**
     * Validates root types for raw domain tree creation. Root types should be 1) {@link AbstractEntity} descendants 2) NOT enhanced types.
     *
     * @param rootTypes
     */
    public static Set<Class<?>> validateRootTypes(final Set<Class<?>> rootTypes) {
        for (final Class<?> klass : rootTypes) {
            validateRootType(klass);
        }
        return rootTypes;
    }

    /**
     * Validates root type for raw domain tree creation. Root types should be 1) {@link AbstractEntity} descendants 2) NOT enhanced types.
     *
     * @param rootTypes
     */
    public static void validateRootType(final Class<?> klass) {
        if (klass == null) {
            throw new DomainTreeException("Root type [" + klass + "] should be NOT NULL.");
        }
        if (!EntityUtils.isEntityType(klass)) {
            throw new DomainTreeException("Root type [" + klass + "] should be entity-typed.");
        }
        if (DynamicEntityClassLoader.isGenerated(klass)) {
            throw new DomainTreeException("Root type [" + klass + "] should be NOT ENHANCED type.");
        }
    }

    /**
     * Returns <code>true</code> if the "property" represents just a marker for <i>not loaded children</i> of its parent property.
     *
     * @param property
     * @return
     */
    public static boolean isDummyMarker(final String property) {
        return property.endsWith(DUMMY_SUFFIX);
    }

    /**
     * Returns <code>true</code> if the "property" represents a root of common properties branch.
     *
     * @param property
     * @return
     */
    public static boolean isCommonBranch(final String property) {
        return property.endsWith(COMMON_SUFFIX);
    }

    /**
     * Returns <code>true</code> if the "property" represents a placeholder.
     *
     * @param string
     * @return
     */
    public static boolean isPlaceholder(final String string) {
        return string.contains(PLACEHOLDER);
    }

    /**
     * Creates a common branch "property" under the specified property.
     *
     * @param property
     * @return
     */
    protected static String createCommonBranch(final String property) {
        return property + COMMON_SUFFIX;
    }

    /**
     * Creates a dummy marker "property" under the specified property, which sub-properties are not supposed to be loaded.
     *
     * @param property
     * @return
     */
    protected static String createDummyMarker(final String property) {
        return property + DUMMY_SUFFIX;
    }

    /**
     * Converts a property in Entity Tree naming contract (with ".common-properties" suffixes) into a property that TG Reflection API understands. "Dummy" property will be
     * converted to its parent property.
     *
     * @param property
     * @return
     */
    public static String reflectionProperty(final String property) {
        return property.replaceAll(DUMMY_SUFFIX, "").replaceAll(COMMON_SUFFIX, "");
    }

    /**
     * Throws an {@link DomainTreeException} if the property is unchecked.
     *
     * @param tm
     * @param root
     * @param property
     * @param message
     */
    protected static void illegalUncheckedProperties(final ITickManager tm, final Class<?> root, final String property, final String message) {
        // The check below is important to maintain the integrity of domain trees.
        // However, it also causes performance bottlenecks when invoking multiple times.
        // In current Web UI logic, that uses centre domain trees, this check does not add any significant value due to other checks implemented as part of Centre DSL.
        // Reintroducing of this check may be significant when management of domain trees from UI will be implemented.
        // if (!tm.isChecked(root, property)) {
        //     throw new DomainTreeException(message);
        // }
    }

    /**
     * Throws an {@link DomainTreeException} if the property can not represent a "double criterion" or boolean criterion.
     *
     * @param root
     * @param property
     * @param message
     */
    protected static void illegalNonDoubleEditorOrBooleanProperties(final Class<?> root, final String property, final String message) {
        if (!isDoubleCriterion(root, property) || isBooleanCriterion(root, property)) {
            throw new DomainTreeException(message);
        }
    }

    /**
     * Throws an {@link DomainTreeException} if the property can not represent a "double criterion".
     *
     * @param root
     * @param property
     * @param message
     */
    protected static void illegalNonDoubleEditorProperties(final Class<?> root, final String property, final String message) {
        if (!isDoubleCriterion(root, property)) {
            throw new DomainTreeException(message);
        }
    }

    /**
     * Throws an {@link DomainTreeException} if the property is unused.
     *
     * @param tm
     * @param root
     * @param property
     * @param message
     */
    protected static void illegalUnusedProperties(final IUsageManager um, final Class<?> root, final String property, final String message) {
        if (!um.isUsed(root, property)) {
            throw new DomainTreeException(message);
        }
    }

    /**
     * Throws an {@link DomainTreeException} if the property type is not legal.
     *
     * @param root
     * @param property
     * @param message
     */
    protected static void illegalType(final Class<?> root, final String property, final String message, final Class<?>... legalTypes) {
        final boolean isEntityItself = "".equals(property); // empty property means "entity itself"
        final Class<?> propertyType = isEntityItself ? root : PropertyTypeDeterminator.determinePropertyType(root, property);
        for (final Class<?> legalType : legalTypes) {
            if (legalType.isAssignableFrom(propertyType)) {
                return;
            }
        }
        throw new DomainTreeException(message);
    }

    protected static String generateKey(final Class<?> forType) {
        return PropertyTypeDeterminator.stripIfNeeded(forType).getName();
    }

    /**
     * Creates a set of linked (ordered) roots. This set will correctly handle "enhanced" root types. It can be used with enhanced types, but inner mechanism will "persist" not
     * enhanced ones.
     *
     * @return
     */
    public static EnhancementLinkedRootsSet createLinkedRootsSet() {
        return new EnhancementLinkedRootsSet();
    }

    /**
     * Creates a set of properties (pairs root+propertyName). This set will correctly handle "enhanced" root types. It can be used with enhanced types, but inner mechanism will
     * "persist" not enhanced ones.
     *
     * @return
     */
    public static EnhancementSet createSet() {
        return new EnhancementSet();
    }

    /**
     * Creates a map of properties => values (pairs root+propertyName). This map will correctly handle "enhanced" root types. It can be used with enhanced types, but inner
     * mechanism will "persist" not enhanced ones.
     *
     * @param <T>
     *            -- a type of values in map
     * @return
     */
    public static <T> EnhancementPropertiesMap<T> createPropertiesMap() {
        return new EnhancementPropertiesMap<>();
    }

    /**
     * Creates a map of properties => values (pairs root+propertyName). This map will correctly handle "enhanced" root types. It can be used with enhanced types, but inner
     * mechanism will "persist" not enhanced ones.
     *
     * @param <T>
     *            -- a type of values in map
     * @return
     */
    public static <T> EnhancementRootsMap<T> createRootsMap() {
        return new EnhancementRootsMap<>();
    }

    /**
     * Returns a key pair for [root + property].
     *
     * @param root
     * @param property
     * @return
     */
    public static Pair<Class<?>, String> key(final Class<?> root, final String property) {
        return new Pair<>(root, property);
    }

    /**
     * Returns <code>true</code> when the property can represent criterion with two editors, <code>false</code> otherwise.
     *
     * @param root -- a root type that contains property.
     * @param property -- a dot-notation expression that defines a property.
     * 
     * @return
     */
    public static boolean isDoubleCriterion(final Class<?> root, final String property) {
        final boolean isEntityItself = "".equals(property); // empty property means "entity itself"
        final Class<?> propertyType = isEntityItself ? root : determinePropertyType(root, property);
        return (isRangeType(propertyType) || isBoolean(propertyType)) && !isCritOnlySingle(root, property);
    }
    
    /**
     * Returns <code>true</code> when the property can represent boolean criterion with one (crit-only single) or two (otherwise) editors, <code>false</code> otherwise.
     *
     * @param root -- a root type that contains property.
     * @param property -- a dot-notation expression that defines a property.
     * 
     * @return
     */
    public static boolean isBooleanCriterion(final Class<?> root, final String property) {
        final boolean isEntityItself = "".equals(property); // empty property means "entity itself"
        final Class<?> propertyType = isEntityItself ? root : determinePropertyType(root, property);
        return isBoolean(propertyType);
    }
    
    /**
     * Returns <code>true</code> when the property represents crit-only single criterion, <code>false</code> otherwise.
     *
     * @param root
     *            -- a root type that contains property.
     * @param property
     *            -- a dot-notation expression that defines a property.
     * @return
     */
    public static boolean isCritOnlySingle(final Class<?> root, final String property) {
        final boolean isEntityItself = "".equals(property); // empty property means "entity itself"
        final CritOnly critOnlyAnnotation = isEntityItself ? null : AnnotationReflector.getPropertyAnnotation(CritOnly.class, root, property);
        return critOnlyAnnotation != null && Type.SINGLE.equals(critOnlyAnnotation.value());
    }
}
