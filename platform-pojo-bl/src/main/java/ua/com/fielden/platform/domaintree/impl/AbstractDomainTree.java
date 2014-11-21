package ua.com.fielden.platform.domaintree.impl;

import java.util.Set;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.domaintree.IDomainTreeManager.ITickManager;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager.IUsageManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.entity.annotation.CritOnly.Type;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.kryo.serialisers.TgSimpleSerializer;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

import com.esotericsoftware.kryo.Kryo;

/**
 * A base class for representations and managers with useful utility methods.
 * 
 * @author TG Team
 * 
 */
public abstract class AbstractDomainTree {
    private final transient ISerialiser serialiser;
    private final static transient Logger logger = Logger.getLogger(AbstractDomainTree.class);
    private static final String COMMON_SUFFIX = ".common-properties", DUMMY_SUFFIX = ".dummy-property";
    protected final static String PLACEHOLDER = "-placeholder-origin-";

    protected static Logger logger() {
        return logger;
    }

    protected static String getDummySuffix() {
        return DUMMY_SUFFIX;
    }

    /**
     * Constructs base domain tree with a <code>serialiser</code> and <code>factory</code> instances.
     * 
     * @param serialiser
     * @param factory
     */
    protected AbstractDomainTree(final ISerialiser serialiser) {
        this.serialiser = serialiser;
    }

    /**
     * Returns an instance of serialiser for persistence and copying.
     * 
     * @return
     */
    protected ISerialiser getSerialiser() {
        return serialiser;
    }

    /**
     * Returns an entity factory that is essential for inner {@link AbstractEntity} instances (e.g. calculated properties) creation.
     * 
     * @return
     */
    protected EntityFactory getFactory() {
        return serialiser.factory();
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
            throw new IllegalArgumentException("Root type [" + klass + "] should be NOT NULL.");
        }
        if (!EntityUtils.isEntityType(klass)) {
            throw new IllegalArgumentException("Root type [" + klass + "] should be entity-typed.");
        }
        if (DynamicEntityClassLoader.isEnhanced(klass)) {
            throw new IllegalArgumentException("Root type [" + klass + "] should be NOT ENHANCED type.");
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
     * Throws an {@link IllegalArgumentException} if the property is unchecked.
     * 
     * @param tm
     * @param root
     * @param property
     * @param message
     */
    protected static void illegalUncheckedProperties(final ITickManager tm, final Class<?> root, final String property, final String message) {
        if (!tm.isChecked(root, property)) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Throws an {@link IllegalArgumentException} if the property can not represent a "double criterion".
     * 
     * @param root
     * @param property
     * @param message
     */
    protected static void illegalNonDoubleEditorProperties(final Class<?> root, final String property, final String message) {
        if (!isDoubleCriterion(root, property)) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Throws an {@link IllegalArgumentException} if the property can not represent a "double criterion".
     * 
     * @param root
     * @param property
     * @param message
     */
    protected static void illegalNonDoubleEditorAndNonBooleanProperties(final Class<?> root, final String property, final String message) {
        if (!isDoubleCriterionOrBoolean(root, property)) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Throws an {@link IllegalArgumentException} if the property is unused.
     * 
     * @param tm
     * @param root
     * @param property
     * @param message
     */
    protected static void illegalUnusedProperties(final IUsageManager um, final Class<?> root, final String property, final String message) {
        if (!um.isUsed(root, property)) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Throws an {@link IllegalArgumentException} if the property type is not legal.
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
        throw new IllegalArgumentException(message);
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
        return new EnhancementPropertiesMap<T>();
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
        return new EnhancementRootsMap<T>();
    }

    /**
     * Returns a key pair for [root + property].
     * 
     * @param root
     * @param property
     * @return
     */
    public static Pair<Class<?>, String> key(final Class<?> root, final String property) {
        return new Pair<Class<?>, String>(root, property);
    }

    /**
     * A specific Kryo serialiser for {@link AbstractDomainTree}.
     * 
     * @author TG Team
     * 
     */
    protected abstract static class AbstractDomainTreeSerialiser<T> extends TgSimpleSerializer<T> {
        private final ISerialiser kryo;
        private final EntityFactory factory;

        public AbstractDomainTreeSerialiser(final ISerialiser kryo) {
            super((Kryo) kryo);
            this.kryo = kryo;
            this.factory = kryo.factory();
        }

        protected ISerialiser kryo() {
            return kryo;
        }

        protected EntityFactory factory() {
            return factory;
        }
    }

    /**
     * Returns <code>true</code> when the property can represent criterion with two editors, <code>false</code> otherwise.
     * 
     * TODO unit test.
     * 
     * @param root
     *            -- a root type that contains property.
     * @param property
     *            -- a dot-notation expression that defines a property.
     * @return
     */
    public static boolean isDoubleCriterionOrBoolean(final Class<?> root, final String property) {
        final boolean isEntityItself = "".equals(property); // empty property means "entity itself"
        final Class<?> propertyType = isEntityItself ? root : PropertyTypeDeterminator.determinePropertyType(root, property);
        return EntityUtils.isBoolean(propertyType) || isDoubleCriterion(root, property);
    }

    /**
     * Returns <code>true</code> when the property can represent criterion with two editors, <code>false</code> otherwise.
     * 
     * TODO unit test.
     * 
     * @param root
     *            -- a root type that contains property.
     * @param property
     *            -- a dot-notation expression that defines a property.
     * @return
     */
    public static boolean isDoubleCriterion(final Class<?> root, final String property) {
        final boolean isEntityItself = "".equals(property); // empty property means "entity itself"
        final Class<?> propertyType = isEntityItself ? root : PropertyTypeDeterminator.determinePropertyType(root, property);
        final CritOnly critOnlyAnnotation = isEntityItself ? null : AnnotationReflector.getPropertyAnnotation(CritOnly.class, root, property);
        return EntityUtils.isRangeType(propertyType) && !(critOnlyAnnotation != null && Type.SINGLE.equals(critOnlyAnnotation.value()));
    }
}
