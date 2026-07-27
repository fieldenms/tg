package ua.com.fielden.platform.domaintree.impl;

import static java.lang.String.format;
import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toCollection;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader.isGenerated;
import static ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader.modifiedClass;
import static ua.com.fielden.platform.reflection.asm.impl.DynamicTypeNamingService.generateTypeName;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.types.tuples.T3.t3;
import static ua.com.fielden.platform.utils.CollectionUtil.linkedMapOf;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;

import ua.com.fielden.platform.domaintree.ICalculatedProperty;
import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyAttribute;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer;
import ua.com.fielden.platform.domaintree.IProperty;
import ua.com.fielden.platform.domaintree.exceptions.DomainTreeException;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.annotation.factory.CalculatedAnnotation;
import ua.com.fielden.platform.entity.annotation.factory.CustomPropAnnotation;
import ua.com.fielden.platform.entity.annotation.factory.IsPropertyAnnotation;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.Reflector;
import ua.com.fielden.platform.reflection.asm.api.NewProperty;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.types.tuples.T3;
import ua.com.fielden.platform.utils.EntityUtils;

/**
 * A domain manager implementation with all sufficient logic for domain modification / loading. <br>
 * <br>
 *
 * <b>Implementation notes:</b><br>
 * 1. After the modifications have been applied manager consists of a map of (entityType -> real enhanced entityType). To play correctly with any type information with enhanced
 * domain you need to use ({@link #getManagedType(Class)} of entityType; dotNotationName) instead of (entityType; dotNotationName).<br>
 * 2. The current version of manager after some modifications (calcProperty has been added/removed/changed) holds a full list of calculated properties for all types. This list
 * should be applied or discarded using {@link #apply()} or {@link #discard()} interface methods.<br>
 * 3.
 *
 * @author TG Team
 *
 */
public final class DomainTreeEnhancer extends AbstractDomainTree implements IDomainTreeEnhancer {
    private static final Logger LOGGER = getLogger(DomainTreeEnhancer.class);
    private static final String ERR_TYPE_COULD_NOT_BE_GENERATED = "Type for [%s] could not be generated.";
    private static final String ERR_COULD_NOT_PROPAGATE_ENHANCED_TYPE_TO_ROOT = "Enhanced type [%s] could not be propagated to [%s] root along path [%s].";
    private static final String ERR_COULD_NOT_MODIFY_ROOT_TYPE_NAME = "Root type [%s] name could not be modified to [%s].";
    /**
     * Type diff object's key for calculated properties, used in generated type naming.
     */
    private static final String CALCULATED_PROPERTIES = "calculatedProperties";
    /**
     * Type diff object's key for custom properties, used in generated type naming.
     */
    private static final String CUSTOM_PROPERTIES = "customProperties";

    /**
     * Comparator for {@link CalculatedPropertyInfo} instances to impose order in type diff object during SHA-256 calculation.
     */
    private static final Comparator<CalculatedPropertyInfo> CALCULATED_PROPERTY_INFO_COMPARATOR =
        comparing(CalculatedPropertyInfo::contextPath) // in most cases this property defines the path where calculated property will be placed
        .thenComparing(CalculatedPropertyInfo::customPropertyName, nullsFirst(naturalOrder())) // in most cases this property defines the actual name of calculated property
        .thenComparing(CalculatedPropertyInfo::contextualExpression) // expression may influence the path, where calculated property will be placed (in future; currently it does not)
        .thenComparing(CalculatedPropertyInfo::title); // title may define actual name of calculated property without customPropertyName (in future; currently it does not)

    /**
     * Cache of {@link DomainTreeEnhancer}s with keys as [rootTypes; calcProps; customProps].
     * <p>
     * The main purpose of this accidental (by means of complexity) cache is to reduce heavy calculated properties re-computation and to 
     * reduce the count of resultant generated entity types.
     */
    private static final ConcurrentMap<T3<Set<Class<?>>, Map<Class<?>, Set<CalculatedPropertyInfo>>, Map<Class<?>, List<CustomProperty>>>, DomainTreeEnhancer> domainTreeEnhancers = new ConcurrentHashMap<>();

    /**
     * Holds a mapping between the original and the enhanced types. Contains pairs of [original -> real] or [original -> original] (in case of not enhanced type).
     */
    private final Map<Class<?>, Class<?>> originalAndEnhancedRootTypes;

    /** Holds current domain differences from "standard" domain (all calculated properties for all root types). */
    private final Map<Class<?>, List<CalculatedProperty>> calculatedProperties;

    /** Holds current domain differences from "standard" domain (all custom properties for all root types). */
    private final Map<Class<?>, List<CustomProperty>> customProperties;

    /**
     * Constructs a new instance of domain enhancer with clean, not enhanced, domain.
     * <p>
     * However, no calculated properties have been added -- the resultant types will be enhanced. They will use a marker property (for more information see method {@link #generateHierarchy(Set, Map, Map)}).
     *
     * @param rootTypes -- root types
     *
     */
    public DomainTreeEnhancer(final EntityFactory entityFactory, final Set<Class<?>> rootTypes) {
        this(entityFactory, rootTypes, createEmptyCalculatedPropsFromRootTypes(rootTypes), createEmptyCustomPropsFromRootTypes(rootTypes));
    }

    /**
     * Constructs a new instance of domain enhancer with full information about containing root types (<b>enhanced</b> or not). This primary constructor should be used for
     * serialisation and copying. Please also note that calculated property changes, that were not applied, will be disappeared! So every enhancer should be carefully applied (or
     * discarded) before serialisation.
     *
     */
    private DomainTreeEnhancer(final EntityFactory entityFactory, final Set<Class<?>> rootTypes, final Map<Class<?>, Set<CalculatedPropertyInfo>> calculatedPropertiesInfo, final Map<Class<?>, List<CustomProperty>> customProperties) {
        super(entityFactory);

        this.originalAndEnhancedRootTypes = new LinkedHashMap<>();
        // init a map with NOT enhanced types and empty byte arrays.
        this.originalAndEnhancedRootTypes.putAll(createOriginalAndEnhancedRootTypes(rootTypes));

        this.customProperties = new LinkedHashMap<>();
        this.customProperties.putAll(customProperties);

        this.calculatedProperties = new LinkedHashMap<>();
        this.calculatedProperties.putAll(createCalculatedPropertiesFrom(this, calculatedPropertiesInfo));

        apply();

        this.customProperties.clear();
        this.customProperties.putAll(customProperties);

        this.calculatedProperties.clear();
        this.calculatedProperties.putAll(extractAll(this, true));
        
        // Perform some post-creation validation.
        validateEnhancer();
    }
    
    /**
     * Copy constructor for {@link DomainTreeEnhancer} taking benefit from shared inner resources (like 'ast's of corresponding CalculatedProperty'es).
     * <p>
     * This is performance-friendly version of copying function without unnecessary parsing of {@link CalculatedProperty#getContextualExpression()},
     * which is costly operation.
     * 
     * @param enhancer
     */
    private DomainTreeEnhancer(final DomainTreeEnhancer enhancer) {
        super(enhancer.getFactory());
        
        this.originalAndEnhancedRootTypes = new LinkedHashMap<>();
        for (final Entry<Class<?>, Class<?>> entry: enhancer.originalAndEnhancedRootTypes.entrySet()) {
            this.originalAndEnhancedRootTypes.put(entry.getKey(), entry.getValue());
        }
        
        // CustomProperty is fully immutable and it is also safe to use the same shared instances of that type across all DomainTreeEnhancer copies.
        this.customProperties = new LinkedHashMap<>();
        this.customProperties.putAll(enhancer.customProperties);
        
        // CalculatedProperty instances will be copied through CalculatedProperty.copy method which shares inner 'ast's and other derived information across all CalculatedProperty copies.
        this.calculatedProperties = new LinkedHashMap<>();
        for (final Entry<Class<?>, List<CalculatedProperty>> entry: enhancer.calculatedProperties.entrySet()) {
            this.calculatedProperties.put(entry.getKey(), entry.getValue().stream().map(cp -> cp.copy(this)).collect(toCollection(ArrayList::new)));
        }
        
        // Perform some post-creation validation.
        validateEnhancer();
    }
    
    /**
     * Validates this instance on subject of conformity of resultant managed type and presence of calculated / custom properties.
     */
    private void validateEnhancer() {
        for (final Class<?> rootType : rootTypes()) {
            // check whether the type WITH calculated properties IS enhanced 
            if (!hasNoAdditionalProperties(rootType) && !isGenerated(getManagedType(rootType))) {
                throw new IllegalStateException(format("The type [%s] should be enhanced -- it has %s properties.", rootType.getSimpleName(), additionalPropDefinitionsAsString(rootType)));
            }
            // check whether the type WITHOUT calculated properties IS NOT enhanced 
            if (hasNoAdditionalProperties(rootType) && isGenerated(getManagedType(rootType))) {
                throw new IllegalStateException(format("The type [%s] should be NOT enhanced -- it has no additional properties.", rootType.getSimpleName()));
            }
        }
    }
    
    /**
     * A constructor <b>strictly</b> for version maintenance.
     *
     * @param entityFactory
     * @param originalAndEnhancedRootTypes
     * @param calculatedProperties
     */
    public DomainTreeEnhancer(final EntityFactory entityFactory, final Map<Class<?>, Class<?>> originalAndEnhancedRootTypes, final Map<Class<?>, List<CalculatedProperty>> calculatedProperties, final Map<Class<?>, List<CustomProperty>> customProperties) {
        super(entityFactory);
        this.originalAndEnhancedRootTypes = originalAndEnhancedRootTypes;
        this.calculatedProperties = calculatedProperties;
        this.customProperties = customProperties;
    }
    
    /**
     * Creates {@link DomainTreeEnhancer} instance from <code>rootTypes</code> that are used for calculated/custom properties mapped to each root type.
     * <p>
     * Please note that this method works with {@link DomainTreeEnhancer} cache. At this stage, instances of {@link DomainTreeEnhancer} are mutable, 
     * but in practice we do not use their mutability and we can safely cache every instance of {@link DomainTreeEnhancer}.
     * <p>
     * Mutability is not an issue due to the following:<br>
     * 1. EntityCentre.createDefaultCentre method applies calculated/custom properties and after that {@link DomainTreeEnhancer} remains in the same state.<br>
     * 2. Instances, that were deserialised after retrieval from database or during copying, are not mutated afterwards.<br>
     * We need to consider {@link DomainTreeEnhancer} cache management when implementing ability to add calculated properties from UI.
     *
     * @param entityFactory
     * @param rootTypes
     * @param calculatedPropertiesInfo
     * @param customProperties
     * @return
     */
    public static DomainTreeEnhancer createFrom(
            final EntityFactory entityFactory,
            final Set<Class<?>> rootTypes,
            final Map<Class<?>, Set<CalculatedPropertyInfo>> calculatedPropertiesInfo,
            final Map<Class<?>, List<CustomProperty>> customProperties) {
        final var cachedInstance = domainTreeEnhancers.computeIfAbsent(
            t3(rootTypes, calculatedPropertiesInfo, customProperties),
            key -> new DomainTreeEnhancer(entityFactory, rootTypes, calculatedPropertiesInfo, customProperties)
        );
        return new DomainTreeEnhancer(cachedInstance); // need to perform copy here; this is to avoid further mutation of cached instance (e.g. in postCentreCreated hooks)
    }
    
    private boolean hasNoAdditionalProperties(final Class<?> rootType) {
        return this.calculatedProperties.get(rootType) == null && (this.customProperties.get(rootType) == null || this.customProperties.get(rootType).isEmpty());
    }
    
    private String additionalPropDefinitionsAsString(final Class<?> rootType) {
        final StringBuilder sb = new StringBuilder();
        if (this.calculatedProperties.get(rootType) != null) {
            sb.append(this.calculatedProperties.get(rootType).size() + " calculated ");
        }
        if (this.customProperties.get(rootType) != null && !this.customProperties.get(rootType).isEmpty()) {
            sb.append(this.customProperties.get(rootType).size() + " custom ");
        }
        return StringUtils.isEmpty(sb.toString()) ? " no " : sb.toString();
    }

    /**
     * Creates an empty map of calc props for <code>rootTypes</code>.
     *
     * @param rootTypes
     * @return
     */
    private static final Map<Class<?>, Set<CalculatedPropertyInfo>> createEmptyCalculatedPropsFromRootTypes(final Set<Class<?>> rootTypes) {
        final Map<Class<?>, Set<CalculatedPropertyInfo>> map = new LinkedHashMap<>();
        for (final Class<?> rootType : rootTypes) {
            map.put(rootType, new HashSet<CalculatedPropertyInfo>());
        }
        return map;
    }

    /**
     * Creates an empty map of custom props for <code>rootTypes</code>.
     *
     * @param rootTypes
     * @return
     */
    private static final Map<Class<?>, List<CustomProperty>> createEmptyCustomPropsFromRootTypes(final Set<Class<?>> rootTypes) {
        final Map<Class<?>, List<CustomProperty>> map = new LinkedHashMap<>();
        for (final Class<?> rootType : rootTypes) {
            map.put(rootType, new ArrayList<>());
        }
        return map;
    }

    /**
     * Creates a map of [original -> original] for provided <code>rootTypes</code>.
     *
     * @return a modifiable map
     */
    private static Map<Class<?>, Class<?>> createOriginalAndEnhancedRootTypes(final Set<Class<?>> rootTypes) {
        final Map<Class<?>, Class<?>> originalAndEnhancedRootTypes = new LinkedHashMap<>();
        for (final Class<?> rootType : rootTypes) {
            originalAndEnhancedRootTypes.put(rootType, rootType);
        }
        return originalAndEnhancedRootTypes;
    }

    /**
     * Groups properties by their domain paths.
     * In the returned map, a type may be mapped to a {@code null} value if it's not associated with any of the given properties.
     *
     * @param calculatedProperties  a map of the form {@code { type = calculatedProperties }}
     * @param customProperties  a map of the form {@code { type = custom properties }}
     * @param rootTypes  types owning the given properties (may also include types that aren't present in any properties map)
     *
     * @return a map of the form {@code { type = { propertyPath = { propertyName = property } } }}
     */
    private static Map<Class<?>, Map<String, Map<String, IProperty>>> groupByPaths(
            final Map<Class<?>, List<CalculatedProperty>> calculatedProperties,
            final Map<Class<?>, List<CustomProperty>> customProperties,
            final Set<Class<?>> rootTypes)
    {
        final Map<Class<?>, Map<String, Map<String, IProperty>>> grouped = new LinkedHashMap<>();
        calculatedProperties.forEach((root, props) -> {
            if (props != null && !props.isEmpty()) {
                final var paths = grouped.computeIfAbsent(root, $ -> new LinkedHashMap<>());
                for (final CalculatedProperty prop : props) {
                    final var names = paths.computeIfAbsent(prop.path(), $ -> new LinkedHashMap<>());
                    names.put(prop.name(), prop);
                }
            } else {
                grouped.put(root, null);
            }
        });
        customProperties.forEach((root, props) -> {
            if (props != null && !props.isEmpty()) {
                final var paths = grouped.computeIfAbsent(root, $ -> new LinkedHashMap<>());
                for (final CustomProperty prop : props) {
                    final var names = paths.computeIfAbsent(prop.path(), $ -> new LinkedHashMap<>());
                    names.put(prop.name(), prop);
                }
            } else if (!grouped.containsKey(root)) {
                grouped.put(root, null);
            }
        });
        // add the types, not enhanced with any calc prop
        for (final Class<?> originalRoot : rootTypes) {
            if (!grouped.containsKey(originalRoot)) {
                grouped.put(originalRoot, null);
            }
        }
        return grouped;
    }

    @Override
    public Class<?> getManagedType(final Class<?> type) {
        CalculatedProperty.validateRootWithoutRootTypeEnforcement(this, type);
        return originalAndEnhancedRootTypes.get(type) == null ? type : originalAndEnhancedRootTypes.get(type);
    }

    @Override
    public void apply() {
        //////////// Performs migration [calculatedProperties => originalAndEnhancedRootTypes] ////////////
        final Map<Class<?>, Class<?>> freshOriginalAndEnhancedRootTypes = generateHierarchy(originalAndEnhancedRootTypes.keySet(), calculatedProperties, customProperties);
        originalAndEnhancedRootTypes.clear();
        originalAndEnhancedRootTypes.putAll(freshOriginalAndEnhancedRootTypes);
    }

    /**
     * Fully generates a new hierarchy of "originalAndEnhancedRootTypes" that conform to "calculatedProperties".
     * <p>
     * If no calculated properties are specified for some rootType, a marker calculated property will be used to ensure that the resultant rootType will be enhanced.
     */
    protected static Map<Class<?>, Class<?>> generateHierarchy(final Set<Class<?>> rootTypes, final Map<Class<?>, List<CalculatedProperty>> calculatedProperties, final Map<Class<?>, List<CustomProperty>> customProperties) {
        // single classLoader instance is needed for single "apply" transaction
        final Map<Class<?>, Class<?>> originalAndEnhancedRootTypes = createOriginalAndEnhancedRootTypes(rootTypes);
        final Map<Class<?>, Map<String, Map<String, IProperty>>> groupedCalculatedProperties = groupByPaths(calculatedProperties, customProperties, rootTypes);

        // iterate through calculated property places (e.g. Vehicle.class+"" or WorkOrder.class+"veh.status") with no care about order
        for (final Entry<Class<?>, Map<String, Map<String, IProperty>>> entry : groupedCalculatedProperties.entrySet()) {
            final Class<?> originalRoot = entry.getKey();
            // generate predefined root type name for all calculated properties
            final String predefinedRootTypeName = generateTypeName(originalRoot, linkedMapOf(t2(CALCULATED_PROPERTIES, calculatedPropertiesInfo(calculatedProperties, rootTypes)), t2(CUSTOM_PROPERTIES, customPropertiesInfo(customProperties))));
            final boolean calcOrCustomPropsOnlyInRoot = entry.getValue() != null && entry.getValue().size() == 1 && StringUtils.isEmpty(entry.getValue().entrySet().iterator().next().getKey());
            if (entry.getValue() == null) {
                originalAndEnhancedRootTypes.put(originalRoot, originalRoot);
            } else {
                for (final Entry<String, Map<String, IProperty>> placeAndProps : entry.getValue().entrySet()) {
                    final Map<String, IProperty> props = placeAndProps.getValue();
                    if (props != null && !props.isEmpty()) {
                        final Supplier<Set<NewProperty>> createNewProperties = () -> {
                            return props.entrySet().stream().map(nameWithProp -> {
                                final IProperty iProp = nameWithProp.getValue();
                                if (iProp instanceof final CalculatedProperty prop) {
                                    final String originationProperty = prop.getOriginationProperty() == null ? "" : prop.getOriginationProperty();
                                    final Annotation calcAnnotation = new CalculatedAnnotation().contextualExpression(prop.getContextualExpression()).rootTypeName(predefinedRootTypeName).contextPath(prop.getContextPath()).origination(originationProperty).attribute(prop.getAttribute()).category(prop.category()).newInstance();
                                    final IsProperty isPropAnnot = prop.getPrecision() != null && prop.getScale() != null ? new IsPropertyAnnotation(prop.getPrecision(), prop.getScale()).newInstance() : NewProperty.DEFAULT_IS_PROPERTY_ANNOTATION;
                                    return new NewProperty(nameWithProp.getKey(), prop.resultType(), prop.getTitle(), prop.getDesc(), isPropAnnot, calcAnnotation);
                                } else { // this should be CustomProperty!
                                    final CustomProperty prop = (CustomProperty) iProp;
                                    return new NewProperty(nameWithProp.getKey(), prop.resultType(), prop.getTitle(), prop.getDesc(), new CustomPropAnnotation().newInstance());
                                }
                            }).collect(toCollection(LinkedHashSet::new));
                        };
                        final Class<?> realRoot = originalAndEnhancedRootTypes.get(originalRoot);
                        // a path to calculated properties
                        final String path = placeAndProps.getKey();

                        // determine a "real" parent type:
                        final Class<?> realParentToBeEnhanced = StringUtils.isEmpty(path) ? realRoot : PropertyTypeDeterminator.determinePropertyType(realRoot, path);
                        final Class<?> realParentEnhanced;
                        try {
                            // generate & load new type enhanced by calculated properties
                            if (calcOrCustomPropsOnlyInRoot) {
                                realParentEnhanced = modifiedClass(predefinedRootTypeName, realParentToBeEnhanced, typeMaker -> typeMaker.addProperties(createNewProperties.get()));
                            } else {
                                realParentEnhanced = modifiedClass(realParentToBeEnhanced, typeMaker -> typeMaker.addProperties(createNewProperties.get()));
                            }
                        } catch (final Exception ex) {
                            final var typeGenEx = new DomainTreeException(ERR_TYPE_COULD_NOT_BE_GENERATED.formatted(realParentToBeEnhanced.getSimpleName()), ex);
                            LOGGER.error(typeGenEx);
                            throw typeGenEx;
                        }
                        // propagate enhanced type to root
                        final Class<?> rootAfterPropagation = propagateEnhancedTypeToRoot(realParentEnhanced, realRoot, path);
                        // replace relevant root type in cache
                        originalAndEnhancedRootTypes.put(originalRoot, rootAfterPropagation);
                    }
                }
            }
            if (!calcOrCustomPropsOnlyInRoot) {
                final Class<?> enhancedRoot = originalAndEnhancedRootTypes.get(originalRoot);
                // modify root type name with predefinedRootTypeName
                if (originalRoot != enhancedRoot) { // calculated properties exist -- root type should be enhanced
                    final Class<?> rootWithPredefinedName;
                    try {
                        rootWithPredefinedName = modifiedClass(predefinedRootTypeName, enhancedRoot, identity());
                    } catch (final Exception ex) {
                        final var typeGenEx = new DomainTreeException(ERR_COULD_NOT_MODIFY_ROOT_TYPE_NAME.formatted(enhancedRoot.getSimpleName(), predefinedRootTypeName), ex);
                        LOGGER.error(typeGenEx);
                        throw typeGenEx;
                    }
                    originalAndEnhancedRootTypes.put(originalRoot, rootWithPredefinedName);
                }
            }
        }
        return originalAndEnhancedRootTypes;
    }
    
    /**
     * Propagates recursively the <code>enhancedType</code> from place [root; path] to place [root; ""].
     *
     * @param enhancedType
     *            -- the type to replace the current type of property "path" in "root" type
     * @param root
     * @param path
     * @return
     */
    private static Class<?> propagateEnhancedTypeToRoot(final Class<?> enhancedType, final Class<?> root, final String path) {
        if (StringUtils.isEmpty(path)) { // replace current root type with new one
            return enhancedType;
        }

        try {
            final var owningTypeAndField = Finder.findFieldByNameWithOwningType(root, path);
            final Class<?> typeToAdapt = owningTypeAndField._1;
            final Field field = owningTypeAndField._2;
            // change type if simple field and change signature in case of collectional field
            final boolean isCollectional = Collection.class.isAssignableFrom(PropertyTypeDeterminator.determineClass(typeToAdapt, field.getName(), true, false));
            final NewProperty<?> propertyToBeModified = !isCollectional
                                                        ? NewProperty.fromField(field).changeType(enhancedType)
                                                        : NewProperty.fromField(field).setTypeArguments(enhancedType);
            final Class<?> nextEnhancedType = modifiedClass(typeToAdapt, typeMaker -> typeMaker.modifyProperties(propertyToBeModified));
            final String nextProp = PropertyTypeDeterminator.isDotExpression(path) ? PropertyTypeDeterminator.penultAndLast(path).getKey() : "";
            return propagateEnhancedTypeToRoot(nextEnhancedType, root, nextProp);
        } catch (final Exception ex) {
            final var typeGenEx = new DomainTreeException(ERR_COULD_NOT_PROPAGATE_ENHANCED_TYPE_TO_ROOT.formatted(enhancedType.getSimpleName(), root.getSimpleName(), path), ex);
            LOGGER.error(typeGenEx);
            throw typeGenEx;
        }
    }

    @Override
    public void discard() {
        //////////// Performs migration [originalAndEnhancedRootTypes => calculatedProperties] ////////////
        calculatedProperties.clear();
        calculatedProperties.putAll(extractAll(this, true));
        
        customProperties.clear(); // FIXME this piece of logic is not properly workable -- this must be considered when discard action will be used anywhere. Low priority for now.
    }

    /**
     * Extracts all calculated properties from enhanced root types.
     *
     * @param dte
     * @return
     */
    protected static Map<Class<?>, List<CalculatedProperty>> extractAll(final IDomainTreeEnhancer dte, final boolean validateTitleContextOfExtractedProperties) {
        final Map<Class<?>, List<CalculatedProperty>> newCalculatedProperties = new LinkedHashMap<>();
        for (final Entry<Class<?>, Class<?>> originalAndEnhanced : dte.originalAndEnhancedRootTypes().entrySet()) {
            final List<CalculatedProperty> calc = reload(originalAndEnhanced.getValue(), originalAndEnhanced.getKey(), "", dte, validateTitleContextOfExtractedProperties);
            for (final CalculatedProperty calculatedProperty : calc) {
                addCalculatedProperty(calculatedProperty, newCalculatedProperties);
            }
        }
        return newCalculatedProperties;
    }

    /**
     * Extracts recursively contextual <code>calculatedProperties</code> from enhanced domain <code>type</code>.
     *
     * @param type
     *            -- enhanced type to load properties
     * @param root
     *            -- not enhanced root type
     * @param path
     *            -- the path to loaded calculated props
     * @param dte
     */
    private static List<CalculatedProperty> reload(final Class<?> type, final Class<?> root, final String path, final IDomainTreeEnhancer dte, final boolean validateTitleContextOfExtractedProperties) {
        final List<CalculatedProperty> newCalcProperties = new ArrayList<>();
        if (!DynamicEntityClassLoader.isGenerated(type)) {
            return newCalcProperties;
        } else {
            // add all first level calculated properties if any exist
            for (final Field calculatedField : Finder.findRealProperties((Class<? extends AbstractEntity<?>>) type, Calculated.class)) {
                final Calculated calcAnnotation = AnnotationReflector.getAnnotation(calculatedField, Calculated.class);
                if (calcAnnotation != null && !StringUtils.isEmpty(calcAnnotation.value()) && AnnotationReflector.isContextual(calcAnnotation)) {
                    final Title titleAnnotation = AnnotationReflector.getAnnotation(calculatedField, Title.class);
                    final String title = titleAnnotation == null ? "" : titleAnnotation.value();
                    final String desc = titleAnnotation == null ? "" : titleAnnotation.desc();
                    final IsProperty isPropertyAnnotation = AnnotationReflector.getAnnotation(calculatedField, IsProperty.class);
                    final CalculatedProperty calculatedProperty = CalculatedProperty.createCorrect(dte.getFactory(), root, calcAnnotation.contextPath(), calcAnnotation.value(), title, desc, calcAnnotation.attribute(), "".equals(calcAnnotation.origination()) ? null
                            : calcAnnotation.origination(), isPropertyAnnotation.precision(), isPropertyAnnotation.scale(), dte, validateTitleContextOfExtractedProperties);

                    // TODO tricky setting!
                    if (!EntityUtils.equalsEx(calculatedField.getName(), calculatedProperty.name())) {
                        calculatedProperty.provideCustomPropertyName(calculatedField.getName());
                    }
                    calculatedProperty.setNameVeryTricky(calculatedField.getName());

                    newCalcProperties.add(calculatedProperty);
                }
            }
            // reload all "entity-typed" and "collectional entity-typed" sub-properties if they are enhanced
            for (final Field prop : Finder.findProperties(type)) {
                if (EntityUtils.isEntityType(prop.getType()) || EntityUtils.isCollectional(prop.getType())) {
                    final Class<?> propType = PropertyTypeDeterminator.determinePropertyType(type, prop.getName());
                    final String newPath = StringUtils.isEmpty(path) ? prop.getName() : (path + "." + prop.getName());
                    newCalcProperties.addAll(reload(propType, root, newPath, dte, validateTitleContextOfExtractedProperties));
                }
            }
            return newCalcProperties;
        }
    }

    /**
     * Checks whether calculated property with the suggested name exists (if it does not exist throws {@link IncorrectCalcPropertyException}) and return it.
     *
     * @param root
     * @param pathAndName
     * @return
     */
    protected final CalculatedProperty calculatedPropertyWhichShouldExist(final Class<?> root, final String pathAndName) {
        final CalculatedProperty calculatedProperty = calculatedProperty(root, pathAndName);
        if (calculatedProperty == null) {
            throw new IncorrectCalcPropertyException("The calculated property with path & name [" + pathAndName + "] does not exist in type [" + root + "].");
        }
        return calculatedProperty;
    }

    /**
     * Iterates through the set of calculated properties to find appropriate calc property.
     *
     * @param root
     * @param pathAndName
     * @return
     */
    protected final CalculatedProperty calculatedProperty(final Class<?> root, final String pathAndName) {
        return calculatedProperty(calculatedProperties.get(DynamicEntityClassLoader.getOriginalType(root)), pathAndName);
    }

    /**
     * Iterates through the set of calculated properties to find appropriate calc property.
     *
     * @param pathAndName
     * @param calcProperties
     * @return
     */
    protected static final CalculatedProperty calculatedProperty(final List<CalculatedProperty> calcProperties, final String pathAndName) {
        if (calcProperties != null) {
            for (final CalculatedProperty prop : calcProperties) {
                if (prop.pathAndName().equals(pathAndName)) {
                    return prop;
                }
            }
        }
        return null;
    }

    @Override
    public IDomainTreeEnhancer addCustomProperty(final Class<?> root, final String contextPath, final String name, final String title, final String desc, final Class<?> type, final Integer precision, final Integer scale) {
        addCustomProperty(new CustomProperty(root, getManagedType(root), contextPath, name, title, desc, type, precision, scale), customProperties);
        return this;
    }

    /**
     * Validates and adds custom property to a customProperties.
     *
     * @param customProperty
     * @param customProperties
     */
    private static void addCustomProperty(final CustomProperty customProperty, final Map<Class<?>, List<CustomProperty>> customProperties) {
        final Class<?> root = customProperty.getRoot();
        if (!customProperties.containsKey(root)) {
            customProperties.put(root, new ArrayList<CustomProperty>());
        }
        customProperties.get(root).add(customProperty);
    }

    /**
     * Validates and adds calc property to a calculatedProperties.
     *
     * @param calculatedProperty
     * @param calculatedProperties
     */
    private static ICalculatedProperty addCalculatedProperty(final CalculatedProperty calculatedProperty, final Map<Class<?>, List<CalculatedProperty>> calculatedProperties) {
        final Class<?> root = calculatedProperty.getRoot();
        if (!calculatedProperty.isValid().isSuccessful()) {
            throw calculatedProperty.isValid();
        }
        if (!calculatedProperties.containsKey(root)) {
            calculatedProperties.put(root, new ArrayList<CalculatedProperty>());
        }
        calculatedProperties.get(root).add(calculatedProperty);
        return /*calculatedProperty*/calculatedProperties.get(root).get(calculatedProperties.get(root).size() - 1);
    }

    @Override
    public ICalculatedProperty addCalculatedProperty(final ICalculatedProperty calculatedProperty) {
        return addCalculatedProperty((CalculatedProperty) calculatedProperty, calculatedProperties);
    }

    @Override
    public ICalculatedProperty addCalculatedProperty(final Class<?> root, final String contextPath, final String contextualExpression, final String title, final String desc, final CalculatedPropertyAttribute attribute, final String originationProperty, final Integer precision, final Integer scale) {
        return addCalculatedProperty(CalculatedProperty.createCorrect(getFactory(), root, contextPath, contextualExpression, title, desc, attribute, originationProperty, precision, scale, this));
    }

    @Override
    public ICalculatedProperty addCalculatedProperty(final Class<?> root, final String contextPath, final String customPropertyName, final String contextualExpression, final String title, final String desc, final CalculatedPropertyAttribute attribute, final String originationProperty, final Integer precision, final Integer scale) {
        return addCalculatedProperty(CalculatedProperty.createCorrect(getFactory(), root, contextPath, customPropertyName, contextualExpression, title, desc, attribute, originationProperty, precision, scale, this));
    }

    @Override
    public ICalculatedProperty getCalculatedProperty(final Class<?> rootType, final String calculatedPropertyName) {
        return calculatedPropertyWhichShouldExist(rootType, calculatedPropertyName);
    }

    @Override
    public ICalculatedProperty copyCalculatedProperty(final Class<?> rootType, final String calculatedPropertyName) {
        return calculatedPropertyWhichShouldExist(rootType, calculatedPropertyName).copy();
    }

    @Override
    public void removeCalculatedProperty(final Class<?> rootType, final String calculatedPropertyName) {
        final CalculatedProperty calculatedProperty = calculatedPropertyWhichShouldExist(rootType, calculatedPropertyName);

        /////////////////
        final List<CalculatedProperty> calcs = calculatedProperties.get(rootType);
        boolean existsInOtherExpressionsAsOriginationProperty = false;
        String containingExpression = null;
        for (final CalculatedProperty calc : calcs) {
            if (StringUtils.equals(calc.getOriginationProperty(), Reflector.fromAbsolute2RelativePath(calc.getContextPath(), calculatedPropertyName))) {
                existsInOtherExpressionsAsOriginationProperty = true;
                containingExpression = calc.pathAndName();
                break;
            }
        }
        if (existsInOtherExpressionsAsOriginationProperty) {
            throw new DomainTreeException("Cannot remove a property that exists in other expressions as 'origination' property. See property [" + containingExpression + "].");
        }
        /////////////////

        final boolean removed = calculatedProperties.get(rootType).remove(calculatedProperty);

        if (!removed) {
            throw new IllegalStateException("The property [" + calculatedPropertyName + "] has been validated but can not be removed.");
        }
        if (calculatedProperties.get(rootType).isEmpty()) {
            calculatedProperties.remove(rootType);
        }
    }

    @Override
    public Set<Class<?>> rootTypes() {
        return new LinkedHashSet<>(originalAndEnhancedRootTypes.keySet());
    }

    //    /**
    //     * Extracts only <b>enhanced</b> type's arrays mapped to original types.
    //     *
    //     * @return
    //     */
    //    private static Map<Class<?>, Map<String, ByteArray>> originalTypesAndEnhancedArrays(final Map<Class<?>, Pair<Class<?>, Map<String, ByteArray>>> originalAndEnhancedRootTypesAndArrays) {
    //	final Map<Class<?>, Map<String, ByteArray>> originalTypesAndEnhancedArrays = new LinkedHashMap<Class<?>, Map<String, ByteArray>>();
    //	for (final Entry<Class<?>, Pair<Class<?>, Map<String, ByteArray>>> entry : originalAndEnhancedRootTypesAndArrays.entrySet()) {
    //	    if (!entry.getValue().getValue().isEmpty()) {
    //		originalTypesAndEnhancedArrays.put(entry.getKey(), new LinkedHashMap<String, ByteArray>(entry.getValue().getValue()));
    //	    }
    //	}
    //	return originalTypesAndEnhancedArrays;
    //    }
    //
    //    /**
    //     * Extracts only <b>enhanced</b> type's arrays mapped to original types.
    //     *
    //     * @return
    //     */
    //    private Map<Class<?>, Map<String, ByteArray>> originalTypesAndEnhancedArrays() {
    //	return originalTypesAndEnhancedArrays(originalAndEnhancedRootTypesAndArrays);
    //    }

    /**
     * A current snapshot of calculated properties, possibly not applied.
     *
     * @return
     */
    @Override
    public Map<Class<?>, List<CalculatedProperty>> calculatedProperties() {
        return calculatedProperties;
    }

    /**
     * Extracts {@link CalculatedPropertyInfo} instances from current snapshot of {@link #calculatedProperties()}.
     * <p>
     * Warning: it is necessary to have applied all changes.
     */
    public static SortedMap<String, SortedSet<CalculatedPropertyInfo>> calculatedPropertiesInfo(final Map<Class<?>, List<CalculatedProperty>> calculatedProperties, final Set<Class<?>> rootTypes) {
        // impose order to ensure equal SHA-256 checksums for the same set of properties
        final SortedMap<String, SortedSet<CalculatedPropertyInfo>> map = new TreeMap<>();

        for (final Entry<Class<?>, List<CalculatedProperty>> entry : calculatedProperties.entrySet()) {
            // impose order to ensure equal SHA-256 checksum for the same set of properties
            final SortedSet<CalculatedPropertyInfo> set = new TreeSet<>(CALCULATED_PROPERTY_INFO_COMPARATOR);
            for (final CalculatedProperty cp : entry.getValue()) {
                set.add(new CalculatedPropertyInfo(cp.getRoot(), cp.getContextPath(), cp.getCustomPropertyName(), cp.getContextualExpression(), cp.getTitle(), cp.getAttribute(), cp.getOriginationProperty(), cp.getDesc(), cp.getPrecision(), cp.getScale()));
            }
            map.put(entry.getKey().getName(), set);
        }

        for (final Class<?> root: rootTypes) {
            map.computeIfAbsent(root.getName(), $ -> new TreeSet<>(CALCULATED_PROPERTY_INFO_COMPARATOR));
        }

        return map;
    }

    /**
     * Converts current snapshot of {@link #customProperties()} into ordered format.
     */
    public static SortedMap<String, SortedSet<CustomProperty>> customPropertiesInfo(final Map<Class<?>, List<CustomProperty>> customProperties) {
        final SortedMap<String, SortedSet<CustomProperty>> map = new TreeMap<>(); // impose order to prevent different SHA-256 checksums for the same set of properties
        for (final Entry<Class<?>, List<CustomProperty>> entry : customProperties.entrySet()) {
            final SortedSet<CustomProperty> set = new TreeSet<>(comparing(CustomProperty::path).thenComparing(CustomProperty::name)); // impose order to prevent different SHA-256 checksums for the same set of properties
            set.addAll(entry.getValue());
            map.put(entry.getKey().getName(), set);
        }
        return map;
    }

    /**
     * Creates calculated properties from raw {@link CalculatedPropertyInfo} instances.
     * <p>
     * Created calculated properties are fully dependent on "dte" {@link DomainTreeEnhancer}. Also validation performs to be sure that all is okay with deserialised (or created
     * from scratch) "dte" {@link DomainTreeEnhancer}.
     *
     * @return
     */
    private static Map<Class<?>, List<CalculatedProperty>> createCalculatedPropertiesFrom(final DomainTreeEnhancer dte, final Map<Class<?>, Set<CalculatedPropertyInfo>> calculatedPropertiesInfo) {
        final Map<Class<?>, List<CalculatedProperty>> map = new LinkedHashMap<>();
        for (final Entry<Class<?>, Set<CalculatedPropertyInfo>> entry : calculatedPropertiesInfo.entrySet()) {
            final List<CalculatedProperty> list = new ArrayList<>();
            for (final CalculatedPropertyInfo cpInfo : entry.getValue()) {
                list.add(CalculatedProperty.createCorrect(dte.getFactory(), cpInfo.root(), cpInfo.contextPath(), cpInfo.customPropertyName(), cpInfo.contextualExpression(), cpInfo.title(), cpInfo.desc(), cpInfo.attribute(), cpInfo.originationProperty(), cpInfo.precision(), cpInfo.scale(), dte, true));
            }
            map.put(entry.getKey(), list);
        }
        return map;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        // IMPORTANT : rootTypes() and calculatedPropertiesInfo() are the mirror for "calculatedProperties".
        // So they should be used for serialisation, comparison and hashCode() implementation.
        result = prime * result + rootTypes().hashCode();
        result = prime * result + calculatedPropertiesInfo(calculatedProperties, rootTypes()).hashCode();
        result = prime * result + customProperties().hashCode();
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
        final DomainTreeEnhancer other = (DomainTreeEnhancer) obj;
        // IMPORTANT : rootTypes() and calculatedPropertiesInfo() are the mirror for "calculatedProperties".
        // So they should be used for serialisation, comparison and hashCode() implementation.
        return rootTypes().equals(other.rootTypes()) && calculatedPropertiesInfo(calculatedProperties, rootTypes()).equals(calculatedPropertiesInfo(other.calculatedProperties, other.rootTypes())) && customProperties().equals(other.customProperties());
    }

    @Override
    public Map<Class<?>, Class<?>> originalAndEnhancedRootTypes() {
        return originalAndEnhancedRootTypes;
    }

    /**
     * Returns an entity factory that is essential for inner {@link AbstractEntity} instances (e.g. calculated properties) creation.
     *
     * @return
     */
    @Override
    public EntityFactory getFactory() {
        return super.getFactory();
    }

    @Override
    public Map<Class<?>, List<CustomProperty>> customProperties() {
        return customProperties;
    }
}
