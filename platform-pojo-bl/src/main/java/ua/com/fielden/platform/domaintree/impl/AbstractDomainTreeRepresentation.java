package ua.com.fielden.platform.domaintree.impl;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.domaintree.Function;
import ua.com.fielden.platform.domaintree.FunctionUtils;
import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyCategory;
import ua.com.fielden.platform.domaintree.IDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.exceptions.DomainTreeException;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeManager.ITickRepresentationWithMutability;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.reflection.asm.impl.DynamicTypeUtils;
import ua.com.fielden.platform.reflection.development.EntityDescriptor;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.lang.String.format;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.entity.AbstractEntity.*;
import static ua.com.fielden.platform.reflection.Finder.*;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.*;
import static ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader.getOriginalType;
import static ua.com.fielden.platform.utils.CollectionUtil.setOf;
import static ua.com.fielden.platform.utils.EntityUtils.isCompositeEntity;
import static ua.com.fielden.platform.utils.EntityUtils.isEntityType;

/**
 * A base domain tree representation for all TG trees. Includes strict TG domain rules that should be used by all specific tree implementations. <br>
 * <br>
 *
 * @author TG Team
 *
 */
public abstract class AbstractDomainTreeRepresentation extends AbstractDomainTree implements IDomainTreeRepresentationWithMutability {
    private final Logger logger = getLogger(this.getClass());

    /**
     * 0 -- to load only first level properties, Integer.MAX_VALUE -- to load all properties (obviously without cross-references ones);
     */
    private static final Integer LOADING_LEVEL = 0;
    private final EnhancementLinkedRootsSet rootTypes;
    private final EnhancementSet manuallyExcludedProperties;
    private final AbstractTickRepresentation firstTick;
    private final AbstractTickRepresentation secondTick;
    /** Please do not use this field directly, use {@link #includedPropertiesMutable(Class)} lazy getter instead. */
    private final transient EnhancementRootsMap<ListenedArrayList> includedProperties;

    /**
     * A <i>representation</i> constructor. Initialises also children references on itself.
     */
    protected AbstractDomainTreeRepresentation(final EntityFactory entityFactory, final Set<Class<?>> rootTypes, final Set<Pair<Class<?>, String>> excludedProperties, final AbstractTickRepresentation firstTick, final AbstractTickRepresentation secondTick) {
        super(entityFactory);
        this.rootTypes = new EnhancementLinkedRootsSet();
        this.rootTypes.addAll(rootTypes);
        this.manuallyExcludedProperties = createSet();
        this.manuallyExcludedProperties.addAll(excludedProperties);
        this.firstTick = firstTick;
        this.secondTick = secondTick;

        // initialise the references on this instance in its children
        try {
            final Field dtrField = Finder.findFieldByName(AbstractTickRepresentation.class, "dtr");
            final boolean isAccessible = dtrField.isAccessible();
            dtrField.setAccessible(true);
            dtrField.set(firstTick, this);
            dtrField.set(secondTick, this);
            dtrField.setAccessible(isAccessible);
        } catch (final Exception e) {
            logger.fatal(e);
            throw new DomainTreeException("Could not instantiate doman tree representation.", e);
        }

        this.includedProperties = createRootsMap();
        // managedType is not known at this stage. Included properties tree should be loaded after "enhancer" exists with all managedTypes.
    }

    private int level(final String path) {
        return !PropertyTypeDeterminator.isDotExpression(path) ? 0 : 1 + level(PropertyTypeDeterminator.penultAndLast(path).getKey());
    }

    /**
     * Constructs recursively the list of properties using given list of fields.
     *
     * @param managedType
     * @param path
     * @param fieldsAndKeys
     * @return
     */
    private List<String> constructProperties(final Class<?> managedType, final String path, final List<Field> fieldsAndKeys) {
        final List<String> newIncludedProps = new ArrayList<>();
        for (final Field field : fieldsAndKeys) {
            final String property = StringUtils.isEmpty(path) ? field.getName() : path + "." + field.getName();
            final String reflectionProperty = reflectionProperty(property);
            if (!isExcludedImmutably(managedType, reflectionProperty)) {
                newIncludedProps.add(property);

                // determine the type of property, which can be:
                // a) "union entity" property 
                // b) property under "union entity" 
                // c) collection property 
                // d) entity property 
                // e) simple property
                final Pair<Class<?>, String> penultAndLast = PropertyTypeDeterminator.transform(managedType, reflectionProperty);
                final Class<?> parentType = penultAndLast.getKey();
                final Class<?> propertyType = PropertyTypeDeterminator.determineClass(parentType, penultAndLast.getValue(), true, true);

                // add the children for "property" based on its nature
                if (EntityUtils.isEntityType(propertyType)) {
                    final boolean propertyTypeWasInHierarchyBefore = typesInHierarchy(managedType, reflectionProperty, true)
                            .contains(DynamicEntityClassLoader.getOriginalType(propertyType));

                    // The logic below (determining whether property represents link property) is important to maintain the integrity of domain trees.
                    // However, it also causes performance bottlenecks when invoking multiple times.
                    // In current Web UI logic, that uses centre domain trees, the following logic does not add any significant value.
                    // Reintroducing may be significant when management of domain trees from UI will be implemented.
                    // Please note number 1. indicates "old" simplified version and number 2. indicates newer version with link property handling.
                    // Perhaps some "hybrid" of both can be used to achieve acceptable performance.

                    // 1. final boolean isKeyPart = Finder.getKeyMembers(parentType).contains(field); // indicates if field is the part of the key.
                    // 2. final boolean isEntityItself = "".equals(property); // empty property means "entity itself"
                    // 2. final Pair<Class<?>, String> transformed = PropertyTypeDeterminator.transform(managedType, property);
                    // 2. final String penultPropertyName = PropertyTypeDeterminator.isDotNotation(property) ? PropertyTypeDeterminator.penultAndLast(property).getKey() : null;
                    // 2. final String lastPropertyName = transformed.getValue();
                    // 2. final boolean isLinkProperty = !isEntityItself && PropertyTypeDeterminator.isDotNotation(property)
                    // 2.         && Finder.isOne2Many_or_One2One_association(managedType, penultPropertyName)
                    // 2.         && lastPropertyName.equals(Finder.findLinkProperty((Class<? extends AbstractEntity<?>>) managedType, penultPropertyName)); // exclude link properties in one2many and one2one associations

                    if (level(property) >= LOADING_LEVEL && !EntityUtils.isUnionEntityType(propertyType) //
                            || propertyTypeWasInHierarchyBefore /* && 2. !isLinkProperty */ /* && 1. !isKeyPart */) {
                        newIncludedProps.add(createDummyMarker(property));
                    }
                    // TODO Need to review the following commet during removal of the "common properties" concept for union entities.
                    //		    else if (EntityUtils.isUnionEntityType(propertyType)) { // "union entity" property
                    //			final Pair<List<Field>, List<Field>> commonAndUnion = commonAndUnion((Class<? extends AbstractUnionEntity>) propertyType);
                    //			// a new tree branch should be created for "common" properties under "property"
                    //			final String commonBranch = createCommonBranch(property);
                    //			newIncludedProps.add(commonBranch); // final DefaultMutableTreeNode nodeForCommonProperties = addHotNode("common", null, false, klassNode, new Pair<String, String>("Common", TitlesDescsGetter.italic("<b>Common properties</b>")));
                    //			newIncludedProps.addAll(constructProperties(managedType, commonBranch, commonAndUnion.getKey()));
                    //			// "union" properties should be added directly to "property"
                    //			newIncludedProps.addAll(constructProperties(managedType, property, commonAndUnion.getValue()));
                    //		    } else if (EntityUtils.isUnionEntityType(parentType)) { // property under "union entity"
                    //			// the property under "union entity" should have only "non-common" properties added
                    //			final List<Field> propertiesWithoutCommon = constructKeysAndProperties(propertyType);
                    //			final List<String> parentCommonNames = AbstractUnionEntity.commonProperties((Class<? extends AbstractUnionEntity>) parentType);
                    //			propertiesWithoutCommon.removeAll(constructKeysAndProperties(propertyType, parentCommonNames));
                    //			newIncludedProps.addAll(constructProperties(managedType, property, propertiesWithoutCommon));
                    //		    }
                    else { // collectional or non-collectional entity property
                        newIncludedProps.addAll(constructProperties(managedType, property, constructKeysAndProperties(propertyType)));
                    }
                }
            }
        }
        return newIncludedProps;
    }

    @Override
    public Set<Pair<Class<?>, String>> excludedPropertiesMutable() {
        return manuallyExcludedProperties;
    }

    /**
     * Determines the lists of common and union fields for concrete union entity type.
     *
     * @param unionClass
     * @return
     */
    private static Pair<List<Field>, List<Field>> commonAndUnion(final Class<? extends AbstractUnionEntity> unionClass) {
        final List<Field> unionProperties = AbstractUnionEntity.unionProperties(unionClass);
        final Class<? extends AbstractEntity> concreteUnionClass = (Class<? extends AbstractEntity>) unionProperties.get(0).getType();
        final Set<String> commonNames = AbstractUnionEntity.commonProperties(unionClass);
        final List<Field> commonProperties = constructKeysAndProperties(concreteUnionClass, commonNames);
        return new Pair<>(commonProperties, unionProperties);
    }

    /**
     * Forms a list of fields for "type" in order ["key" or key members => "desc" (if exists) => other properties in order as declared in domain].
     *
     * @param type
     * @return
     */
    private static List<Field> constructKeysAndProperties(final Class<?> type) {
        return constructKeysAndProperties(type, false);
    }

    /**
     * Forms a list of fields for "type" in order ["key" or key members => ["id" => "version" => ]"desc" (if exists) => other properties in order as declared in domain].
     *
     * @param type
     * @param withIdAndVersion -- {@code true} to include {@link AbstractEntity#ID} and {@link AbstractEntity#VERSION} properties right after keys and before {@link AbstractEntity#DESC}, {@code false} otherwise.
     * @return
     */
    public static List<Field> constructKeysAndProperties(final Class<?> type, final boolean withIdAndVersion) {
        // logger().info("Started constructKeysAndProperties for [" + type.getSimpleName() + "].");
        final var keys = getKeyMembers((Class<? extends AbstractEntity<?>>) type);
        final var properties = streamProperties(type)
                // let's remove desc and key properties as they will be added separately a couple of lines below
                .filter(prop -> !KEY.equals(prop.getName()) && !DESC.equals(prop.getName()))
                .filter(prop -> !keys.contains(prop)) // remove composite key members if any
                .collect(toImmutableList());

        // now let's ensure that that key related properties and desc are first in the list of properties
        final List<Field> fieldsAndKeys = new ArrayList<>();
        fieldsAndKeys.addAll(keys);
        if (withIdAndVersion) {
            fieldsAndKeys.add(getFieldByName(type, ID));
            fieldsAndKeys.add(getFieldByName(type, VERSION));
        }
        fieldsAndKeys.add(getFieldByName(type, DESC));

        // add the rest (take care of ordering if the type is a generated one)
        fieldsAndKeys.addAll(DynamicEntityClassLoader.isGenerated(type) ? DynamicTypeUtils.orderedProperties(properties) : properties);
        // logger().info("Ended constructProperties for [" + type.getSimpleName() + "].");
        return fieldsAndKeys;
    }

    /**
     * Forms a list of fields for "type" in order ["key" or key members => "desc" (if exists) => other properties in order as declared in domain] and chooses only fields with
     * <code>names</code>.
     *
     * @param type
     * @param names
     * @return
     */
    private static List<Field> constructKeysAndProperties(final Class<?> type, final Set<String> names) {
        final List<Field> allProperties = constructKeysAndProperties(type);
        final List<Field> properties = new ArrayList<>();
        for (final Field f : allProperties) {
            if (names.contains(f.getName())) {
                properties.add(f);
            }
        }
        return properties;
    }

    /**
     * Returns <code>true</code> if property is collection itself.
     *
     * @param root
     * @param property
     * @return
     */
    protected static boolean isCollection(final Class<?> root, final String property) {
        final boolean isEntityItself = "".equals(property); // empty property means "entity itself"
        if (isEntityItself) {
            return false;
        }
        final Pair<Class<?>, String> penultAndLast = PropertyTypeDeterminator.transform(root, property);
        final Class<?> realType = PropertyTypeDeterminator.determineClass(penultAndLast.getKey(), penultAndLast.getValue(), true, false);
        return realType != null && Collection.class.isAssignableFrom(realType); // or collections itself
    }

    /**
     * Returns <code>true</code> if property is short collection itself.
     *
     * @param root
     * @param property
     * @return
     */
    public static boolean isShortCollection(final Class<?> root, final String property) {
        final boolean isEntityItself = "".equals(property); // empty property means "entity itself"
        if (isEntityItself) {
            return false;
        }
        final Pair<Class<?>, String> penultAndLast = transform(root, property);
        final Class<?> realType = determineClass(penultAndLast.getKey(), penultAndLast.getValue(), true, false);
        final Class<?> elementType = determineClass(penultAndLast.getKey(), penultAndLast.getValue(), true, true);

        // return !isEntityItself && realType != null && Collection.class.isAssignableFrom(realType); // or collections itself
        return Collection.class.isAssignableFrom(realType) &&
                isEntityType(elementType) &&
                isCompositeEntity((Class<AbstractEntity<?>>) elementType) &&
                getKeyMembers((Class<? extends AbstractEntity<?>>) elementType).size() == 2 &&
                getKeyMembers((Class<? extends AbstractEntity<?>>) elementType).stream().allMatch(field -> isEntityType(field.getType())) &&
                getKeyMembers((Class<? extends AbstractEntity<?>>) elementType).stream().anyMatch(field -> isShortCollectionKeyCompatible(field.getType(), penultAndLast.getKey()));
    }

    /**
     * Returns <code>true</code> if the type of collectional prop element key (<code>keyType</code>) is equal or supertype of the entity type (<code>parentType</code>) holding collection.
     * 
     * @param keyType
     * @param parentType
     * @return
     */
    private static boolean isShortCollectionKeyCompatible(final Class<?> keyType, final Class<?> parentType) {
        return stripIfNeeded(getOriginalType(keyType)).isAssignableFrom(stripIfNeeded(getOriginalType(parentType)));
    }

    /**
     * Returns the name of significant key member of short collectional prop element; aka key member with type not compatible with parent type.
     * <p>
     * This must be used only inside <code>if (isShortCollection(root, property)) {...}</code> clause.
     * 
     * @param root
     * @param property
     * @return
     */
    public static String shortCollectionKey(final Class<?> root, final String property) {
        final Pair<Class<?>, String> penultAndLast = transform(root, property);
        final Class<? extends AbstractEntity<?>> elementType = (Class<? extends AbstractEntity<?>>) determineClass(penultAndLast.getKey(), penultAndLast.getValue(), true, true);
        return getKeyMembers(elementType).stream()
            .filter(field -> !isShortCollectionKeyCompatible(field.getType(), penultAndLast.getKey()))
            .findAny() // stream should return exactly one key field out of two entity-typed key fields after above filtering
            .map(field -> field.getName())
            .orElseThrow(() -> new IllegalStateException(format("Short collection (%s; %s) does not have significant key that is not compatible with parent type.", root.getSimpleName(), property)));
    }

    /**
     * Returns parent collection for specified property.
     *
     * @param root
     * @param property
     * @return
     */
    public static String parentCollection(final Class<?> root, final String property) {
        if (!isCollectionOrInCollectionHierarchy(root, property)) {
            throw new DomainTreeException("The property [" + property + "] is not in collection hierarchy.");
        }
        return isCollection(root, property) ? property : parentCollection(root, PropertyTypeDeterminator.penultAndLast(property).getKey());
    }

    /**
     * Returns <code>true</code> if property is in collectional hierarchy.
     *
     * @param root
     * @param property
     * @return
     */
    public static boolean isInCollectionHierarchy(final Class<?> root, final String property) {
        final boolean isEntityItself = "".equals(property); // empty property means "entity itself"
        return !isEntityItself && typesInHierarchy(root, property, false).contains(Collection.class); // properties in collectional hierarchy
    }

    /**
     * Returns <code>true</code> if property is in collectional hierarchy or is collection itself.
     *
     * @param root
     * @param property
     * @return
     */
    public static boolean isCollectionOrInCollectionHierarchy(final Class<?> root, final String property) {
        return isCollection(root, property) || isInCollectionHierarchy(root, property);
    }

    /**
     * Returns <code>true</code> if property is in collectional hierarchy or is collection itself (not <i>short</i>).
     * <p>
     * <i>Short</i> collections are represented with 'one-to-many' association, where 'many' type contains strictly two composite keys: one key is of 'one' type and other is of
     * other entity type.
     *
     * @param root
     * @param property
     * @return
     */
    public static boolean isNotShortCollectionOrInCollectionHierarchy(final Class<?> root, final String property) {
        return (isCollection(root, property) && !isShortCollection(root, property)) || isInCollectionHierarchy(root, property);
    }

    /**
     * Returns the standard {@link #isExcludedImmutably(Class, String)} contract implementation with few relaxations applicable to Web API:
     * <ul>
     * <li>1. do not exclude 'key' that have no {@link KeyTitle} assigned</li>
     * <li>2. include 'key's with different types, not only entity-typed ones</li>
     * <li>3. include non-{@link IsProperty} properties, e.g. {@link AbstractEntity#ID} or {@link AbstractEntity#VERSION} </li>
     * </ul>
     * 
     * @param root
     * @param property
     * @return
     */
    public static boolean isExcluded(final Class<?> root, final String property) {
        return isExcluded(root, property, createSet(), setOf(root), false, false);
    }

    /**
     * Main implementation point for {@link #isExcludedImmutably(Class, String)} contract.
     * 
     * @param root
     * @param property
     * @param manuallyExcludedProperties
     * @param rootTypes
     * @param strictKeys -- <code>true</code> to exclude keys without {@link KeyTitle} annotation or non-entity-typed ones, <code>false</code> otherwise
     * @param strictIsPropertyProps -- <code>true</code> to exclude non-{@link IsProperty} properties, <code>false</code> otherwise
     * @return
     */
    private static boolean isExcluded(final Class<?> root, final String property, final EnhancementSet manuallyExcludedProperties, final Set<Class<?>> rootTypes, final boolean strictKeys, final boolean strictIsPropertyProps) {
        // logger().info("\t\tStarted isExcludedImmutably for property [" + property + "].");
        final boolean isEntityItself = "".equals(property); // empty property means "entity itself"
        // logger().info("\t\t\ttransform.");
        final Pair<Class<?>, String> transformed = PropertyTypeDeterminator.transform(root, property);
        // logger().info("\t\t\tpenultAndLast.");
        final String penultPropertyName = PropertyTypeDeterminator.isDotExpression(property) ? PropertyTypeDeterminator.penultAndLast(property).getKey() : null;
        final Class<?> penultType = transformed.getKey();
        final String lastPropertyName = transformed.getValue();
        // logger().info("\t\t\tdetermineClass.");
        final Class<?> propertyType = isEntityItself ? root : PropertyTypeDeterminator.determineClass(penultType, lastPropertyName, true, true);
        // final Field field = isEntityItself ? null : Finder.getFieldByName(penultType, lastPropertyName);
        // logger().info("\t\t\tstarted conditions...");
        final boolean excl = manuallyExcludedProperties.contains(key(root, property)) || // exclude manually excluded properties
                Money.class.isAssignableFrom(penultType) || // all properties within type Money should be excluded at this stage
                !isEntityItself && AbstractEntity.KEY.equals(lastPropertyName) && propertyType == null || // exclude "key" -- no KeyType annotation exists in direct owner of "key"
                strictKeys && !isEntityItself && AbstractEntity.KEY.equals(lastPropertyName) && !AnnotationReflector.isAnnotationPresentForClass(KeyTitle.class, penultType) || // exclude "key" -- no KeyTitle annotation exists in direct owner of "key"
                strictKeys && !isEntityItself && AbstractEntity.KEY.equals(lastPropertyName) && !EntityUtils.isEntityType(propertyType) || // exclude "key" -- "key" is not of entity type
                !isEntityItself && AbstractEntity.DESC.equals(lastPropertyName) && !EntityDescriptor.hasDesc(penultType) || // exclude "desc" -- no DescTitle annotation exists in direct owner of "desc"
                strictIsPropertyProps && !isEntityItself && !AnnotationReflector.isAnnotationPresent(Finder.findFieldByName(root, property), IsProperty.class) || // exclude non-TG properties (not annotated by @IsProperty)
                isEntityItself && !rootTypes.contains(propertyType) || // exclude entities of non-"root types"
                EntityUtils.isEnum(propertyType) || // exclude enumeration properties / entities
                EntityUtils.isEntityType(propertyType) && Modifier.isAbstract(propertyType.getModifiers()) || // exclude properties / entities of entity type with 'abstract' modifier
                EntityUtils.isEntityType(propertyType) && !AnnotationReflector.isAnnotationPresentForClass(KeyType.class, propertyType) || // exclude properties / entities of entity type without KeyType annotation
                EntityUtils.isEntityType(propertyType) && EntityUtils.isIntrospectionDenied(propertyType) || // exclude properties / entities of entity type with DenyIntrospection annotation
                !isEntityItself && AnnotationReflector.isPropertyAnnotationPresent(Invisible.class, penultType, lastPropertyName) || // exclude invisible properties
                !isEntityItself && AnnotationReflector.isPropertyAnnotationPresent(Ignore.class, penultType, lastPropertyName) || // exclude ignored properties
                // The logic below (determining whether property represents link property) is important to maintain the integrity of domain trees.
                // However, it also causes performance bottlenecks when invoking multiple times (findLinkProperty, getOriginalValue, etc.).
                // In current Web UI logic, that uses centre domain trees, the following logic does not add any significant value.
                // Reintroducing may be significant when management of domain trees from UI will be implemented.
                // Please note number 1. indicates "old" simplified version and number 2. indicates newer version with link property handling.
                // Perhaps some "hybrid" of both can be used to achieve acceptable performance.
                // 1. !isEntityItself && Finder.getKeyMembers(penultType).contains(field) && typesInHierarchy(root, property, true).contains(DynamicEntityClassLoader.getOriginalType(propertyType)) || // exclude key parts which type was in hierarchy
                // 2. !isEntityItself && PropertyTypeDeterminator.isDotNotation(property) && Finder.isOne2Many_or_One2One_association(DynamicEntityClassLoader.getOriginalType(root), penultPropertyName) && lastPropertyName.equals(Finder.findLinkProperty((Class<? extends AbstractEntity<?>>) DynamicEntityClassLoader.getOriginalType(root), penultPropertyName)) || // exclude link properties in one2many and one2one associations
                !isEntityItself && isExcluded(root, PropertyTypeDeterminator.isDotExpression(property) ? penultPropertyName : "", manuallyExcludedProperties, rootTypes, strictKeys, strictIsPropertyProps); // exclude property if it is an ascender (any level) of already excluded property
        // logger().info("\t\tEnded isExcludedImmutably for property [" + property + "].");
        return excl;
    }

    @Override
    public boolean isExcludedImmutably(final Class<?> root, final String property) {
        return isExcluded(root, property, manuallyExcludedProperties, rootTypes(), true, true);
    }

    /**
     * Finds a complete set of <b>NOT ENHANCED</b> types in hierarchy of dot-notation expression, excluding the type of last property and including the type of root class.<br>
     * <br>
     *
     * E.g. : "WorkOrder$$1.vehicle.fuelUsages.vehicle.fuelCards.initDate" => <br>
     * => [WorkOrder.class, Vehicle.class, FuelUsage.class, FuelCard.class] (if addCollectionalElementType = true) or <br>
     * => [WorkOrder.class, Vehicle.class, Collection.class] (if addCollectionalElementType = false)
     *
     * @param root
     * @param property
     * @param addCollectionalElementType
     *            if {@code true} then a correct element type of collectional property will be added to the result, otherwise – {@code Collection.class} will be added.
     * @return
     */
    protected static Set<Class<?>> typesInHierarchy(final Class<?> root, final String property, final boolean addCollectionalElementType) {
        if (!PropertyTypeDeterminator.isDotExpression(property)) {
            return new HashSet<Class<?>>() {
                private static final long serialVersionUID = 6314144790005942324L;
                {
                    add(DynamicEntityClassLoader.getOriginalType(root));
                }
            };
        } else {
            final Pair<String, String> penultAndLast = PropertyTypeDeterminator.penultAndLast(property);
            final String penult = penultAndLast.getKey();
            final Pair<Class<?>, String> transformed = PropertyTypeDeterminator.transform(root, penult);

            return new HashSet<Class<?>>() {
                private static final long serialVersionUID = 6314144760005942324L;
                {
                    if (addCollectionalElementType) {
                        add(DynamicEntityClassLoader.getOriginalType(PropertyTypeDeterminator.determineClass(transformed.getKey(), transformed.getValue(), true, true)));
                    } else {
                        final Class<?> type = PropertyTypeDeterminator.determineClass(transformed.getKey(), transformed.getValue(), true, false);
                        add(DynamicEntityClassLoader.getOriginalType(Collection.class.isAssignableFrom(type) ? Collection.class : type));
                    }
                    addAll(typesInHierarchy(root, PropertyTypeDeterminator.penultAndLast(property).getKey(), addCollectionalElementType)); // recursively add other types
                }
            };
        }
    }

    @Override
    public IDomainTreeRepresentation excludeImmutably(final Class<?> root, final String property) {
        if (includedPropertiesMutable(root).contains(property)) {
            final int index = includedPropertiesMutable(root).indexOf(property);
            while (index < includedPropertiesMutable(root).size() && includedPropertiesMutable(root).get(index).startsWith(property)) {
                includedPropertiesMutable(root).remove(includedPropertiesMutable(root).get(index));
            }
        }
        manuallyExcludedProperties.add(key(root, property));
        return this;
    }

    /**
     * An {@link ArrayList} specific implementation which listens to structure modifications (add / remove elements) and fires appropriate events.
     *
     * @author TG Team
     *
     */
    public static class ListenedArrayList extends ArrayList<String> {
        private static final long serialVersionUID = -4295706377290507263L;
        private final transient Class<?> root;
        private final transient AbstractDomainTreeRepresentation parentDtr;

        public ListenedArrayList() {
            this(null, null);
        }

        public ListenedArrayList(final Class<?> root, final AbstractDomainTreeRepresentation parentDtr) {
            super();
            this.root = root;
            this.parentDtr = parentDtr;
        }

        private String getElem(final int index) {
            try {
                return get(index);
            } catch (final IndexOutOfBoundsException e) {
                return null;
            }
        }

        @Override
        public boolean add(final String property) {
            if (property == null) {
                throw new DomainTreeException("'null' properties can not be added into properties set (implemented as natural ordered list).");
            } else if (!EntityUtils.equalsEx(getElem(size() - 1), property)) { // when last property is equal to attempted (addition) property -- ignore addition
                final boolean added = super.add(property);
                return added;
            }
            return false;
        }

        @Override
        public void add(final int index, final String property) {
            if (property == null) {
                throw new DomainTreeException("'null' properties can not be added into properties set (implemented as natural ordered list).");
            } else if (!EntityUtils.equalsEx(getElem(index - 1), property)) { // when last property is equal to attempted (addition) property -- ignore addition
                super.add(index, property);
            }
        }

        @Override
        public boolean addAll(final Collection<? extends String> properties) {
            for (final String property : properties) {
                final boolean added = add(property);
                if (!added) {
                    return added;
                }
            }
            return true;
        }

        @Override
        public boolean addAll(final int index, final Collection<? extends String> properties) {
            int currIndex = index;
            for (final String property : properties) {
                add(currIndex++, property);
            }
            return true;
        }

        ////////////////////////////////////////////////////////////
        @Override
        public boolean remove(final Object obj) {
            final String property = (String) obj;
            final boolean removed = super.remove(obj);
            if (!removed) {
                throw new IllegalStateException("DANGEROUS: the property [" + property + "] can not be removed, because it does not exist in the list. "
                        + "But the meta-state associated with this property has been removed! Please ensure that property removal is correct!");
            }
            return removed;
        }
    }

    /**
     * Getter of mutable "included properties" cache for internal purposes.
     * <p>
     * Please note that you can only mutate this list with methods {@link List#add(Object)} and {@link List#remove(Object)} to correctly reflect the changes on depending objects.
     * (e.g. UI tree models, checked properties etc.)
     *
     * @param managedType
     * @return
     */
    @Override
    public List<String> includedPropertiesMutable(final Class<?> managedType) {
        final Class<?> root = DynamicEntityClassLoader.getOriginalType(managedType);
        if (includedProperties.get(root) == null) { // not yet loaded
            final Date st = new Date();
            // initialise included properties using isExcluded contract and manually excluded properties
            final ListenedArrayList includedProps = new ListenedArrayList(root, this);
            if (!isExcludedImmutably(root, "")) { // the entity itself is included -- add it to "included properties" list
                includedProps.add("");
                if (!EntityUtils.isEntityType(root)) {
                    throw new DomainTreeException("Can not add children properties to non-entity type [" + root.getSimpleName() + "] in path [" + root.getSimpleName() + "=>"
                            + "" + "].");
                }
                // logger().info("Started constructKeysAndProperties for [" + managedType.getSimpleName() + "].");
                final List<Field> fields = constructKeysAndProperties(managedType);
                // logger().info("Ended constructKeysAndProperties for [" + managedType.getSimpleName() + "].");

                // logger().info("Started constructProperties for [" + managedType.getSimpleName() + "].");
                final List<String> props = constructProperties(managedType, "", fields);
                // logger().info("Ended constructProperties for [" + managedType.getSimpleName() + "].");

                includedProps.addAll(props);
            }
            includedProperties.put(root, includedProps);
            logger().debug("Root [" + root.getSimpleName() + "] has been processed within " + (new Date().getTime() - st.getTime()) + "ms with " + includedProps.size()
                    + " included properties."); // => [" + includedProps + "]
        }
        return includedProperties.get(root);
    }

    @Override
    public List<String> includedProperties(final Class<?> root) {
        return Collections.unmodifiableList(includedPropertiesMutable(root));
    }

    /**
     * This method loads all missing properties on the tree path as defined in <code>fromPath</code> and <code>toPath</code> for type <code>root</code>. Please note that property
     * <code>fromPath</code> should be loaded itself (perhaps without its children).
     *
     * @param managedType
     * @param fromPath
     * @param toPath
     */
    protected final boolean warmUp(final Class<?> managedType, final String fromPath, final String toPath) {
        // System.out.println("Warm up => from = " + fromPath + "; to = " + toPath);
        if (includedPropertiesMutable(managedType).contains(fromPath)) { // the property itself exists in "included properties" cache
            final String dummyMarker = createDummyMarker(fromPath);
            final boolean shouldBeLoaded = includedPropertiesMutable(managedType).contains(dummyMarker);
            if (shouldBeLoaded) { // the property is circular and has no children loaded -- it has to be done now
                final int index = includedPropertiesMutable(managedType).indexOf(dummyMarker);
                includedPropertiesMutable(managedType).remove(dummyMarker); // remove dummy property

                // logger().info("Started constructKeysAndProperties for [" + managedType.getSimpleName() + "] from = " + fromPath + ".");
                final List<Field> fields = constructKeysAndProperties(PropertyTypeDeterminator.determinePropertyType(managedType, fromPath));
                // logger().info("Ended constructKeysAndProperties for [" + managedType.getSimpleName() + "] from = " + fromPath + ".");

                // logger().info("Started constructProperties for [" + managedType.getSimpleName() + "] from = " + fromPath + ".");
                final List<String> props = constructProperties(managedType, fromPath, fields);
                // logger().info("Ended constructProperties for [" + managedType.getSimpleName() + "] from = " + fromPath + ".");

                // logger().info("Started includedPropertiesMutable.addAll for [" + managedType.getSimpleName() + "] from = " + fromPath + ".");

                // enableListening(false);
                includedPropertiesMutable(managedType).addAll(index, props);
                // enableListening(true);

                // logger().info("Ended includedPropertiesMutable.addAll for [" + managedType.getSimpleName() + "] from = " + fromPath + ".");
            }
            if (!EntityUtils.equalsEx(fromPath, toPath)) { // not the leaf is trying to be warmed up
                final String part = "".equals(fromPath) ? toPath : toPath.replaceFirst(fromPath + ".", "");
                final String part2 = part.indexOf(".") > 0 ? part.substring(0, part.indexOf(".")) : part;
                final String part3 = "".equals(fromPath) ? part2 : fromPath + "." + part2;
                final boolean hasBeenWarmedUp = warmUp(managedType, part3, toPath);
                return shouldBeLoaded || hasBeenWarmedUp;
            } else {
                return shouldBeLoaded;
            }
        } else {
            throw new DomainTreeException("The property [" + fromPath + "] in root [" + managedType.getSimpleName() + "] should be already loaded into 'included properties'.");
        }
    }

    @Override
    public IDomainTreeRepresentation warmUp(final Class<?> managedType, final String property) {
        final Date st = new Date();
        illegalExcludedProperties(this, managedType, reflectionProperty(property), "Could not 'warm up' an 'excluded' property [" + property + "] in type ["
                + managedType.getSimpleName() + "]. Only properties that are not excluded can be 'warmed up'.");
        includedPropertiesMutable(managedType); // ensure "included properties" to be loaded
        if (warmUp(managedType, "", property)) {
            logger().debug("Warmed up root's [" + managedType.getSimpleName() + "] property [" + property + "] within " + (new Date().getTime() - st.getTime()) + "ms.");
        }
        return this;
    }

    /**
     * Throws an {@link DomainTreeException} if the property is excluded.
     *
     * @param dtr
     * @param root
     * @param property
     * @param message
     */
    protected static void illegalExcludedProperties(final IDomainTreeRepresentation dtr, final Class<?> root, final String property, final String message) {
        // The check below is important to maintain the integrity of domain trees.
        // However, it also causes performance bottlenecks when invoking multiple times.
        // In current Web UI logic, that uses centre domain trees, this check does not add any significant value due to other checks implemented as part of Centre DSL.
        // This check is currently performed only in STRICT_MODEL_VERIFICATION mode (and thus skipped in deployment mode).
        if (isStrictModelVerification() && dtr.isExcludedImmutably(root, property)) {
            throw new DomainTreeException(message);
        }
    }

    /**
     * An abstract tick representation. <br>
     * <br>
     *
     * Includes default implementations of "disabling/immutable checking", that contain: <br>
     * a) manual state management; <br>
     * b) resolution of conflicts with excluded properties; <br>
     * c) automatic disabling of "immutably checked" properties.
     *
     * @author TG Team
     *
     */
    public static abstract class AbstractTickRepresentation implements ITickRepresentationWithMutability {
        private final EnhancementSet disabledManuallyProperties;
        private final transient AbstractDomainTreeRepresentation dtr;

        /**
         * Used for serialisation and for normal initialisation. IMPORTANT : To use this tick it should be passed into representation constructor and then into manager constructor,
         * which should initialise "dtr" and "tickManager" fields.
         */
        protected AbstractTickRepresentation() {
            this.disabledManuallyProperties = createSet();

            this.dtr = null; // IMPORTANT : to use this tick it should be passed into representation constructor, which should initialise "dtr" field.
        }

        @Override
        public final boolean isDisabledImmutably(final Class<?> root, final String property) {
            illegalExcludedProperties(dtr, root, property, "Could not ask a 'disabled' state for already 'excluded' property [" + property + "] in type [" + root.getSimpleName()
                    + "].");
            return isDisabledImmutablyLightweight(root, property);
        }

        @Override
        public boolean isDisabledImmutablyLightweight(final Class<?> root, final String property) {
            // I know that this property is not excluded. So there is no need to invoke heavy-weight method "illegalExcludedProperties"
            return disabledManuallyProperties.contains(key(root, property)) || // disable manually disabled properties
                    isCheckedImmutablyLightweight(root, property); // the checked by default properties should be disabled (immutable checking)
        }

        @Override
        public ITickRepresentation disableImmutably(final Class<?> root, final String property) {
            illegalExcludedProperties(dtr, root, property, "Could not disable already 'excluded' property [" + property + "] in type [" + root.getSimpleName() + "].");
            disabledManuallyProperties.add(key(root, property));

            return this;
        }

        protected boolean isDisabledImmutablyPropertiesOfEntityType(final Class<?> propertyType, final KeyType keyTypeAnnotation) {
            // (EntityUtils.isEntityType(propertyType) && DynamicEntityKey.class.isAssignableFrom(keyTypeAnnotation.value())); // properties of "entity with composite key" type has been enabled
            return EntityUtils.isEntityType(propertyType) && EntityUtils.isEntityType(keyTypeAnnotation.value()); // disable properties of "entity with AE key" type
        }

        @Override
        public boolean isCheckedImmutably(final Class<?> root, final String property) {
            illegalExcludedProperties(dtr, root, property, "Could not ask a 'checked' state for already 'excluded' property [" + property + "] in type [" + root.getSimpleName()
                    + "].");
            return isCheckedImmutablyLightweight(root, property);
        }

        protected boolean isCheckedImmutablyLightweight(final Class<?> root, final String property) {
            return false;
        }

        public AbstractDomainTreeRepresentation getDtr() {
            return dtr;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (disabledManuallyProperties == null ? 0 : disabledManuallyProperties.hashCode());
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
            final AbstractTickRepresentation other = (AbstractTickRepresentation) obj;
            if (disabledManuallyProperties == null) {
                if (other.disabledManuallyProperties != null) {
                    return false;
                }
            } else if (!disabledManuallyProperties.equals(other.disabledManuallyProperties)) {
                return false;
            }
            return true;
        }

        @Override
        public EnhancementSet disabledManuallyPropertiesMutable() {
            return disabledManuallyProperties;
        }
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
    public Set<Class<?>> rootTypes() {
        return rootTypes;
    }

    @Override
    public Set<Function> availableFunctions(final Class<?> root, final String property) {
        illegalExcludedProperties(this, root, property, "Could not ask for 'available functions' for already 'excluded' property [" + property + "] in type ["
                + root.getSimpleName() + "].");
        final boolean isEntityItself = "".equals(property); // empty property means "entity itself"
        final Class<?> propertyType = isEntityItself ? root : PropertyTypeDeterminator.determinePropertyType(root, property);
        final Set<Function> availableFunctions = FunctionUtils.functionsFor(propertyType);

        if (!isEntityItself
                && isCalculatedAndOfTypes(root, property, CalculatedPropertyCategory.AGGREGATED_EXPRESSION, CalculatedPropertyCategory.ATTRIBUTED_COLLECTIONAL_EXPRESSION)) {
            final Set<Function> functions = new HashSet<>();
            if (availableFunctions.contains(Function.SELF)) {
                functions.add(Function.SELF);
            }
            return functions;
        }
        if (isEntityItself) {
            availableFunctions.remove(Function.SELF);
        }
        if (!isInCollectionHierarchy(root, property)) {
            availableFunctions.remove(Function.ALL);
            availableFunctions.remove(Function.ANY);
        }
        if (!isEntityItself && Integer.class.isAssignableFrom(propertyType) && !isCalculatedAndOriginatedFromNotIntegerType(root, property)) {
            availableFunctions.remove(Function.COUNT_DISTINCT);
        }
        return availableFunctions;
    }

    /**
     * Returns <code>true</code> if the property is calculated.
     *
     * @param root
     * @param property
     * @return
     */
    public static boolean isCalculated(final Class<?> root, final String property) {
        return AnnotationReflector.getPropertyAnnotation(Calculated.class, root, property) != null;
    }

    /**
     * Returns <code>true</code> if the property is calculated with one of the specified categories.
     *
     * @param root
     * @param property
     * @param types
     * @return
     */
    public static boolean isCalculatedAndOfTypes(final Class<?> root, final String property, final CalculatedPropertyCategory... types) {
        final Calculated ca = AnnotationReflector.getPropertyAnnotation(Calculated.class, root, property);
        if (ca != null) {
            for (final CalculatedPropertyCategory type : types) {
                if (type.equals(ca.category())) {
                    return true;
                }
            }
        }
        return false;
    }

    protected static boolean isCalculatedAndOriginatedFromDateType(final Class<?> root, final String property) {
        final Calculated calculatedAnnotation = AnnotationReflector.getPropertyAnnotation(Calculated.class, root, property);
        return calculatedAnnotation != null && EntityUtils.isDate(PropertyTypeDeterminator.determinePropertyType(root, calculatedAnnotation.origination()));
    }

    private static boolean isCalculatedAndOriginatedFromNotIntegerType(final Class<?> root, final String property) {
        final Calculated calculatedAnnotation = AnnotationReflector.getPropertyAnnotation(Calculated.class, root, property);
        return calculatedAnnotation != null && !Integer.class.isAssignableFrom(PropertyTypeDeterminator.determinePropertyType(root, calculatedAnnotation.origination()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (manuallyExcludedProperties == null ? 0 : manuallyExcludedProperties.hashCode());
        result = prime * result + (firstTick == null ? 0 : firstTick.hashCode());
        result = prime * result + (rootTypes == null ? 0 : rootTypes.hashCode());
        result = prime * result + (secondTick == null ? 0 : secondTick.hashCode());
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
        final AbstractDomainTreeRepresentation other = (AbstractDomainTreeRepresentation) obj;
        if (manuallyExcludedProperties == null) {
            if (other.manuallyExcludedProperties != null) {
                return false;
            }
        } else if (!manuallyExcludedProperties.equals(other.manuallyExcludedProperties)) {
            return false;
        }
        if (firstTick == null) {
            if (other.firstTick != null) {
                return false;
            }
        } else if (!firstTick.equals(other.firstTick)) {
            return false;
        }
        if (rootTypes == null) {
            if (other.rootTypes != null) {
                return false;
            }
        } else if (!rootTypes.equals(other.rootTypes)) {
            return false;
        }
        if (secondTick == null) {
            if (other.secondTick != null) {
                return false;
            }
        } else if (!secondTick.equals(other.secondTick)) {
            return false;
        }
        return true;
    }

    public EnhancementLinkedRootsSet getRootTypes() {
        return rootTypes;
    }

    public EnhancementSet getManuallyExcludedProperties() {
        return manuallyExcludedProperties;
    }
}
